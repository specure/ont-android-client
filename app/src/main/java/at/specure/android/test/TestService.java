/*******************************************************************************
 * Copyright 2014-2017 Specure GmbH
 * Copyright 2013-2015 alladin-IT GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package at.specure.android.test;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.location.Location;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;


import com.specure.opennettest.R;

import java.util.ArrayList;
import java.util.Map;

import at.specure.android.configs.ConfigHelper;
import at.specure.android.configs.LoopModeConfig;
import at.specure.android.screens.main.InfoCollector;
import at.specure.android.screens.main.MainActivity;
import at.specure.android.test.TestTask.EndTaskListener;
import at.specure.android.util.InformationCollector;
import at.specure.android.util.NotificationIDs;
import at.specure.android.util.location.GeoLocationX;
import at.specure.android.util.location.LocationChangeListener;
import at.specure.android.util.net.ZeroMeasurementDetector;
import at.specure.client.QualityOfServiceTest;
import at.specure.client.QualityOfServiceTest.Counter;
import at.specure.client.helper.IntermediateResult;
import at.specure.client.helper.NdtStatus;
import at.specure.client.v2.task.QoSTestEnum;
import at.specure.client.v2.task.result.QoSTestResultEnum;
import timber.log.Timber;

public class TestService extends android.app.Service implements EndTaskListener, LocationChangeListener {
    public static String ACTION_START_TEST = "at.specure.android.startTest";
    public static String ACTION_LOOP_TEST = "at.specure.android.loopTest";
    public static String ACTION_START_LOOP = "at.specure.android.startLoop";
    public static final String ACTION_STOP_LOOP = "at.specure.android.stopLoop";
    public static String ACTION_ABORT_TEST = "at.specure.android.abortTest";
    private static final String ACTION_ALARM = "at.specure.android.Alarm";
    private static final String ACTION_WAKEUP_ALARM = "at.specure.android.WakeupAlarm";
    public static String BROADCAST_TEST_FINISHED = "at.specure.android.test.TestService.testFinished";
    private static final String CHANNEL_ID_TEST = "TEST_SERVICE_NOTIFICATION_CHANNEL";
    private static final String CHANNEL_ID_LOOP = "LOOP_SERVICE_NOTIFICATION_CHANNEL";


    private TestTask testTask;
    // private InformationCollector fullInfo;

    private Handler handler;

    private static final String DEBUG_TAG = "TestService";

    private static WifiLock wifiLock;
    private static WakeLock wakeLock;

    private boolean bound = false;

    private final Runnable deadman = new Runnable() {
        @Override
        public void run() {
            stopTest();
        }
    };

    // private BroadcastReceiver mNetworkStateIntentReceiver;

    // This is the object that receives interactions from clients. See
    // RemoteService for a more complete example.
    private final IBinder localRMBTBinder = new RMBTBinder();
    private boolean completed = false;
    private NotificationManager notificationManager;


    ///LOOP FIELDS
    private long lastTestTime = 0; // SystemClock.elapsedRealtime()
    private WakeLock dimWakeLock;
    private AlarmManager alarmManager;
    private NotificationCompat.Builder notificationBuilderLoop;
    private NotificationCompat.Builder notificationBuilderTest;
    private ArrayList<Location> lastLocation;
    private Location lastTestLocation;
    private Receiver receiver = new Receiver();
    private long minDelay;
    private float maxMovement;
    private int maxTests;
    private static final float ACCURACY_ACCEPTED = 100f;
    private boolean isGPSModeEnabled;
    private PendingIntent alarm;
    private PendingIntent wakeupAlarm;
    private static final int WEIGHT_OF_MIDDLE_VALUE = 7; //19 //0
    private static final int LOCATION_BUFFER_SIZE = 6; //5 //10
    private static final long ACCEPT_INACCURACY = 1000; // accept 1 sec inaccuracy
    private String action;
    private NotificationChannel notificationChannel;
    private boolean receiverRegistered = false;
    @SuppressWarnings("UnnecessaryLocalVariable")
    private NotificationCompat.Builder loopNotificationBuilder;
    private boolean firstTest;


    /**
     * Class for clients to access. Because we know this service always runs in
     * the same process as its clients, we don't need to deal with IPC.
     */
    public class RMBTBinder extends Binder {
        public TestService getService() {
            // Return this instance of TestService so clients can call public
            // methods
            return TestService.this;
        }
    }

    @Override
    public void onCreate() {
        Timber.d("created");
        super.onCreate();

        handler = new Handler();

        // initialise the locks
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "RMBT:RMBTWifiLock");
            wifiLock.acquire();
        }
        PowerManager systemService = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        if (systemService != null) {
            wakeLock = systemService.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "RMBT:RMBTWakeLock");
            wakeLock.acquire(5 * 60 * 1000L /*5 minutes*/);
        }

        dimWakeLock = ((PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE)).newWakeLock(
                PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "RMBT:RMBTLoopDimWakeLock");
        dimWakeLock.acquire(5 * 60 * 1000L /*5 minutes*/);

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager != null) {
                NotificationChannel test_service_notification_channel = new NotificationChannel(CHANNEL_ID_TEST, "Test service notification channel", NotificationManager.IMPORTANCE_HIGH);
                notificationManager.createNotificationChannel(test_service_notification_channel);
                notificationManager.createNotificationChannel(new NotificationChannel(CHANNEL_ID_LOOP, "Loop service notification channel", NotificationManager.IMPORTANCE_HIGH));
            }
        }
    }

    /**
     * Stops executing any test and releases locks and alarms, stops whole service
     */
    public void stopLoopMode() {
        stopTest();
        releaseLocks();
        Timber.d("stoppingLoopMode");
        if (receiverRegistered) {
            unregisterReceiver(receiver);
            receiverRegistered = false;
        }
        removeNotifications();
        GeoLocationX.getInstance(this.getApplication()).removeListener(this);
        removeAlarms();
        LoopModeConfig.setCurrentlyPerformingLoopMode(getApplicationContext(), false);
        stopSelf();
        lastTestTime = 0;
        lastTestLocation = null;
        lastLocation = null;
    }

    private void removeNotifications() {
        removeNotificationTest();
        removeNotificationLoop();
    }

    private void removeAlarms() {
        if (alarmManager != null) {
            if (alarm != null) {
                alarmManager.cancel(alarm);
            }
            if (wakeupAlarm != null) {
                alarmManager.cancel(wakeupAlarm);
            }
        }
    }

    private void releaseLocks() {
        if (wakeLock != null && wakeLock.isHeld())
            wakeLock.release();
        if (dimWakeLock != null && dimWakeLock.isHeld())
            dimWakeLock.release();
        if (wifiLock != null && wifiLock.isHeld())
            wifiLock.release();
    }

    @Override
    public void onDestroy() {
        Timber.d("destroyed");
        super.onDestroy();
        LoopModeConfig.setCurrentlyPerformingLoopMode(getApplicationContext(), false);
        removeNotifications();
        releaseLocks();
        removeCallbacks();
        removeAlarms();
        stopTest();
        stopLoopMode();
    }

    private void removeCallbacks() {
        handler.removeCallbacks(addNotificationRunnable);
        handler.removeCallbacks(deadman);
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        String actionLocal = null;
        if (intent != null)
            actionLocal = intent.getAction();

        if (ACTION_START_LOOP.equals(actionLocal)
                || ACTION_START_TEST.equals(actionLocal)
                || ACTION_STOP_LOOP.equals(actionLocal)
                || ACTION_ABORT_TEST.equals(actionLocal)
                || ACTION_LOOP_TEST.equals(actionLocal)
                ) {
            action = actionLocal;
        }

        Timber.i("onStartCommand; action= %s", actionLocal);

        if (ACTION_START_LOOP.equals(actionLocal)) {
            readConfig();
            GeoLocationX.getInstance(this.getApplication()).getLastKnownLocation(this, this);

            firstTest = true;

            registerReceiver(receiver, new IntentFilter(TestService.BROADCAST_TEST_FINISHED));
            receiverRegistered = true;

            final Intent alarmIntent = new Intent(ACTION_ALARM, null, this, getClass());
            alarm = PendingIntent.getService(this, 0, alarmIntent, 0);

            final Intent wakeupAlarmIntent = new Intent(ACTION_WAKEUP_ALARM, null, this, getClass());
            wakeupAlarm = PendingIntent.getService(this, 0, wakeupAlarmIntent, 0);

            final long now = SystemClock.elapsedRealtime();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, now + 10000, wakeupAlarm);
            } else {
                alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, now + 10000, 10000, wakeupAlarm);
            }

            if (lastTestTime == 0) {
                LoopModeConfig.resetCurrentTestNumber(this);
                InfoCollector.getInstance().setLoopModeCurrentTest(1);
                InfoCollector.getInstance().dispatchInfoChangedEvent(InfoCollector.InfoCollectorType.LOOP_MODE, null, InfoCollector.getInstance().getLoopModeCurrent());

                notificationBuilderLoop = createNotificationBuilderLoop(false);
                Notification build = notificationBuilderLoop.build();
                startForeground(NotificationIDs.LOOP_ACTIVE, build);

                onAlarmOrLocation(false);
                Timber.e("LOOP Start");
                Toast.makeText(this, R.string.loop_started, Toast.LENGTH_LONG).show();

            } else
                Toast.makeText(this, R.string.loop_already_active, Toast.LENGTH_LONG).show();
            Timber.e("LOOP already started");



            return START_NOT_STICKY;
        }


        if ((ACTION_ABORT_TEST.equals(actionLocal)) || (ACTION_STOP_LOOP.equals(actionLocal))) {
            Timber.i("ACTION_ABORT_TEST received");
            stopTest();
            if (ACTION_STOP_LOOP.equals(actionLocal)) {
                stopLoopMode();
                this.stopSelf();
            }
            return START_NOT_STICKY;
        } else if (actionLocal != null && actionLocal.equals(ACTION_ALARM)) {
            Timber.e("LOOP Action alarm");
            onAlarmOrLocation(false);
        } else if (ACTION_START_TEST.equals(actionLocal) || ACTION_LOOP_TEST.equals(actionLocal)) {

            if (ACTION_LOOP_TEST.equals(actionLocal)) {

//                createTestNotification();
            }

            if (testTask != null && testTask.isRunning()) {
                if (ACTION_LOOP_TEST.equals(actionLocal)) {// do not cancel test if running in loop mode
                    testTask.checkZeroMeasurement();
                    return START_NOT_STICKY;
                }
                testTask.cancel(); // otherwise cancel
            }

            completed = false;

            // lock wifi + power
            lock();

            if (ACTION_LOOP_TEST.equals(actionLocal)) {
                LoopModeConfig.incrementCurrentTestNumber(this);
                testTask = new TestTask(this, true, LoopModeConfig.getCurrentTestNumber(getApplicationContext()) == 1);
            } else {
                testTask = new TestTask(this);
            }



            testTask.setEndTaskListener(this);
            testTask.execute(handler);
            Timber.d("RMBTTest started");

            handler.postDelayed(addNotificationRunnable, 200);
            long DEADMAN_TIME = 120 * 1000;
            handler.postDelayed(deadman, DEADMAN_TIME);

            return START_STICKY;
        } else if (action != null && action.equals(ACTION_WAKEUP_ALARM)) {
            Timber.e("LOOP Wake up");
            onWakeup();
        }

        return START_NOT_STICKY;
    }

    public void stopTest() {

        if (testTask != null) {
            Timber.d("RMBTTest stopped");
            testTask.cancel();
            taskEnded();
            testTask = null;
        }
        removeNotificationTest();
        stopForeground(true);
        releaseLocks();
        LoopModeConfig.setCurrentlyPerformingLoopMode(getApplicationContext(), false);
    }

    public boolean isTestRunning() {
        return testTask != null && testTask.isRunning();
    }

    private void addNotificationIfTestRunning() {
        if (notificationManager == null) {
            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager != null) {
                notificationChannel = new NotificationChannel(CHANNEL_ID_TEST, "Test service notification channel", NotificationManager.IMPORTANCE_HIGH);
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        if (isTestRunning() && (!bound /*|| isLoopMode()*/)) {
            createTestNotification();
        }
    }

    private void createTestNotification() {
        final Resources res = getResources();

        final PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(
                getApplicationContext(), MainActivity.class), 0);

        final Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID_TEST)
                .setSmallIcon(R.drawable.stat_icon_test)
                .setContentTitle(res.getText(R.string.test_notification_title))
                .setContentText(res.getText(R.string.test_notification_text))
                .setTicker(res.getText(R.string.test_notification_ticker))
                .setContentIntent(contentIntent)
                .setOngoing(false)
                .build();



        startForeground(NotificationIDs.TEST_RUNNING, notification);
        if (notificationManager != null) {
            notificationManager.notify(NotificationIDs.TEST_RUNNING, notification);
        }
    }

    private void removeNotificationTest() {
//        stopForeground(true);
        if (isLoopMode()) {
            updateNotificationLoop(false);
        }
        notificationManager.cancel(NotificationIDs.TEST_RUNNING);
    }

    private void removeNotificationLoop() {
        notificationManager.cancel(NotificationIDs.LOOP_ACTIVE);
    }

    @Override
    public IBinder onBind(final Intent intent) {
        Timber.d("Bounding");
        GeoLocationX.getInstance(this.getApplication()).getLastKnownLocation(this, null);
        GeoLocationX.getInstance(this.getApplication()).addListener(this);
        bound = true;
        return localRMBTBinder;
    }

    private final Runnable addNotificationRunnable = new Runnable() {
        @Override
        public void run() {
            addNotificationIfTestRunning();
        }
    };

    @Override
    public boolean onUnbind(final Intent intent) {
        Timber.d("Unbounding");
        bound = false;
        handler.postDelayed(addNotificationRunnable, 200);
        return true;
    }

    @Override
    public void onRebind(final Intent intent) {
        bound = true;
        Timber.d("Rebounding");
        if (!isLoopMode()) {
            removeNotificationTest();
        }
    }

    public IntermediateResult getIntermediateResult(final IntermediateResult result) {
        if (testTask != null)
            return testTask.getIntermediateResult(result);
        else
            return null;
    }

    public boolean isConnectionError() {
        if (testTask != null)
            return testTask.isConnectionError();
        else
            return false;
    }

    public Integer getSignal() {
        if (testTask != null)
            return testTask.getSignal();
        else
            return null;
    }

    public int getSignalType() {
        if (testTask != null)
            return testTask.getSignalType();
        else
            return InformationCollector.SINGAL_TYPE_NO_SIGNAL;
    }

    public String getOperatorName() {
        if (testTask != null)
            return testTask.getOperatorName();
        else
            return null;
    }

    public int getNetworkType() {
        if (testTask != null)
            return testTask.getNetworkType();
        else
            return 0;
    }

    public Location getLocation() {
        if (testTask != null)
            return testTask.getLocation();
        else
            return null;
    }

    public String getServerName() {
        if (testTask != null)
            return testTask.getServerName();
        else
            return null;
    }

    // protected Status getStatus()
    // {
    // if (testTask != null)
    // return testTask.getStatus();
    // else
    // return null;
    // }

    public String getIP() {
        if (testTask != null)
            return testTask.getIP();
        else
            return null;
    }

    public String getTestUuid() {
        if (testTask != null) {
            return testTask.getTestUuid();
        } else {
            return ConfigHelper.getLastTestUuid(getApplicationContext(), true);
        }
    }

    public float getNDTProgress() {
        if (testTask != null)
            return testTask.getNDTProgress();
        else
            return 0;
    }

    public NdtStatus getNdtStatus() {
        if (testTask != null)
            return testTask.getNdtStatus();
        else
            return null;
    }

    /**
     * @return
     */
    public float getQoSTestProgress() {
        if (testTask != null)
            return testTask.getQoSTestProgress();
        else
            return 0;
    }

    /**
     * @return
     */
    public QualityOfServiceTest getQoSTest() {
        if (testTask != null) {
            return testTask.getQoSTest();
        }

        return null;
    }

    /**
     * @return
     */
    public int getQoSTestSize() {
        if (testTask != null)
            return testTask.getQoSTestSize();
        else
            return 0;
    }

    /**
     * @return
     */
    public QoSTestEnum getQoSTestStatus() {
        if (testTask != null)
            return testTask.getQoSTestStatus();
        else
            return null;
    }

    /**
     * @return
     */
    public Map<QoSTestResultEnum, Counter> getQoSGroupCounterMap() {
        if (testTask != null) {
            return testTask.getQoSGroupCounterMap();
        } else {
            return null;
        }
    }

    public void lock() {
        try {
            if (!wakeLock.isHeld())
                wakeLock.acquire();
            if (!wifiLock.isHeld())
                wifiLock.acquire();

            Timber.d("Lock");
        } catch (final Exception e) {
            Timber.e(e, "Error getting Lock: ");
        }
    }

    @Override
    public void taskEnded() {
        releaseLocks();
        removeNotificationTest();
        ConfigHelper.setHistoryIsDirty(getApplicationContext(), true);
        handler.removeCallbacks(deadman);
        completed = true;
        sendBroadcast(new Intent(BROADCAST_TEST_FINISHED));
        if (testTask != null) {
            ConfigHelper.setLastTestUuid(getApplicationContext(), testTask.getTestUuid());
        }
        Timber.i("TestService stopped!");
        LoopModeConfig.setCurrentlyPerformingLoopMode(getApplicationContext(), false);
        // add incrementing here
        if (!isLoopMode()) {
            stopSelf();
        }
    }

    public boolean isCompleted() {
        return completed;
    }

    public void runNdt() {
        if (testTask != null) {
            testTask.runNDT();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////       LOOP SERVICE PART        //////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onLocationChange(Location curLocation, String decodedLocation, boolean enabledGPS) {
        if (curLocation != null) {
            if (lastTestLocation != null) {
//                final float distance = curLocation.distanceTo(lastTestLocation);
                final float distance = countMediumDistance(lastLocation, lastTestLocation);
                Timber.d("location distance: %s", distance);
//                Toast.makeText(this.context, "DISTANCE: " + distance, Toast.LENGTH_LONG).show();
                int numberOfTests = LoopModeConfig.getCurrentTestNumber(getApplicationContext());
                if (maxTests == 0 || numberOfTests < maxTests) {
                    if ((distance >= maxMovement) && (isGPSModeEnabled))
                        onAlarmOrLocation(true);
                } else {
                    stopSelf();
                }

            } else {
                if (curLocation.getAccuracy() < ACCURACY_ACCEPTED) {
                    lastTestLocation = curLocation;
                }
            }

            if (curLocation.getAccuracy() < ACCURACY_ACCEPTED) {
                if (lastLocation == null) {
                    lastLocation = new ArrayList<Location>();
                }
                lastLocation.add(curLocation);
                if ((lastLocation.size() > LOCATION_BUFFER_SIZE) && (lastLocation.size() > 0)) {
                    lastLocation.remove(0);
                }
            }
            ZeroMeasurementDetector.detectZeroMeasurement(null, this, InformationCollector.getInstance(this, false, false, false));

        }

    }

    private float countMediumDistance(ArrayList<Location> lastLocation, Location lastTestLocation) {
        if ((lastLocation != null) && (lastLocation.size() > 0)) {
            float distanceTo = lastLocation.get(lastLocation.size() - 1).distanceTo(lastTestLocation);
            return distanceTo;
        }
        return 0;
    }

    public void triggerTest() {
//        LoopModeConfig.incrementCurrentTestNumber(this); // moved to TestService only when loop mode is starting new test
        if (lastLocation != null && !lastLocation.isEmpty()) {
            lastTestLocation = lastLocation.get(lastLocation.size() - 1);
        }
        lastTestTime = SystemClock.elapsedRealtime();
        Timber.e("LOOP starting test");


        final Intent service = new Intent(TestService.ACTION_LOOP_TEST, null, this, TestService.class);
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(service);
                } else {
                    startService(service);
                }
            }
        });

//        if (testTask != null && testTask.isRunning()) {
//            testTask.checkZeroMeasurement();
//
//
//            completed = false;
//
//            // lock wifi + power
//            lock();
//            LoopModeConfig.incrementCurrentTestNumber(this);
//
//            testTask = new TestTask(this, true, LoopModeConfig.getCurrentTestNumber(getApplicationContext()) == 1);
//
//            testTask.setEndTaskListener(this);
//            testTask.execute(handler);
//            Timber.d("RMBTTest started");
//
//        }
//        handler.postDelayed(addNotificationRunnable, 200);
//        long DEADMAN_TIME = 120 * 1000;
//        handler.postDelayed(deadman, DEADMAN_TIME);
//
//        this.addNotificationIfTestRunning();
//        NotificationCompat.Builder notificationBuilderLoop = createNotificationBuilderLoop(true);
//        Notification build = notificationBuilderLoop.build();
//        startForeground(NotificationIDs.LOOP_ACTIVE, build);

            updateNotificationLoop(true);

        GeoLocationX.getInstance(this.getApplication()).getLastKnownLocation(this, this);
    }

    private void readConfig() {
        minDelay = LoopModeConfig.getLoopModeMinDelay(this) * 1000L;
//        maxDelay = LoopModeConfig.getLoopModeMaxDelay(this) * 1000;
        maxMovement = LoopModeConfig.getLoopModeMaxMovement(this);
        isGPSModeEnabled = LoopModeConfig.isLoopModeGPS(this);
        maxTests = LoopModeConfig.getLoopModeMaxTests(this);
    }

    @SuppressLint("Wakelock")
    private void onWakeup() {
        if (dimWakeLock != null) {
            if (dimWakeLock.isHeld())
                dimWakeLock.release();
            dimWakeLock.acquire(10 * 60 * 1000L /*10 minutes*/);
        }

        final long now = SystemClock.elapsedRealtime();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, now + 10000, wakeupAlarm);
        }
    }

    private void setAlarm(long millis) {
        Timber.d("setAlarm: " + millis);

        final long now = SystemClock.elapsedRealtime();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, now + millis, alarm);
        } else {
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, now + millis, alarm);
        }
    }

    private void onAlarmOrLocation(boolean forceOnLocation) {
        final long now = SystemClock.elapsedRealtime();
        Timber.e("LOOP on alarm or location");
        final long lastTestDelta = now - lastTestTime;
        // trigger test if location delta was achieved
        if (forceOnLocation) {
            triggerTest();
            LoopModeConfig.setCurrentlyPerformingLoopMode(getApplicationContext(), true);
//            Toast.makeText(this, "GPS triggered test" , Toast.LENGTH_LONG).show();
            return;
        }
        // trigger test if it is first test in a loop test series
        if (lastTestTime == 0) {
            triggerTest();
            LoopModeConfig.setCurrentlyPerformingLoopMode(getApplicationContext(), true);
//            Toast.makeText(this, "First test" , Toast.LENGTH_LONG).show();
            return;
        }
        //trigger test if min delay was achieved
        if (lastTestDelta + ACCEPT_INACCURACY >= minDelay) {
            triggerTest();
            LoopModeConfig.setCurrentlyPerformingLoopMode(getApplicationContext(), true);
//            Toast.makeText(this, "TIMEOUT triggered test" , Toast.LENGTH_LONG).show();
            return;
        }
        setAlarm(minDelay - lastTestDelta);
//        Toast.makeText(this, "Alarm set to: " + (minDelay - lastTestDelta)/1000 + " s ", Toast.LENGTH_LONG).show();
    }


    private NotificationCompat.Builder createNotificationBuilderLoop(boolean runningTest) {
        final Resources res = getResources();

        CharSequence text = "";
        int numberOfTests = LoopModeConfig.getCurrentTestNumber(getApplicationContext());

        if (runningTest) {
           text = res.getString(R.string.loop_notification_text_2, firstTest ? 1: numberOfTests+1);
//            text = string;//MessageFormat.format(textTemplate.toString(), numberOfTests);
            firstTest = false;
        } else {
            text = res.getString(R.string.loop_notification_text, numberOfTests);
//            text = MessageFormat.format(textTemplate.toString(), numberOfTests);
        }


//        final PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(
//                getApplicationContext(), MainActivity.class), 0);

        final Intent stopIntent = new Intent(ACTION_STOP_LOOP, null, getApplicationContext(), getClass());
        final PendingIntent stopPIntent = PendingIntent.getService(getApplicationContext(), 0, stopIntent, 0);

        if (loopNotificationBuilder == null) {
            loopNotificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID_LOOP)
                    .setSmallIcon(R.drawable.stat_icon_loop)
                    .setContentTitle(res.getText(R.string.loop_notification_title))
                    .setContentText(text)
                    .setTicker(res.getText(R.string.loop_notification_ticker))
                    .setContentIntent(stopPIntent);
        } else {
            loopNotificationBuilder.setContentTitle(res.getText(R.string.loop_notification_title))
                    .setContentText(text);

        }

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
//            addActionToNotificationBuilder(builder, stopPIntent);
//        else
//            builder.setContentIntent(stopPIntent);
        return loopNotificationBuilder;
    }

    private void updateNotificationLoop(boolean runningTest) {
        notificationBuilderLoop = createNotificationBuilderLoop(runningTest);
        int numberOfTests = LoopModeConfig.getCurrentTestNumber(getApplicationContext());
        final Notification notification = notificationBuilderLoop.setNumber(numberOfTests).build();
        if (notificationManager == null) {
            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);


//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                if (notificationManager != null) {
//                    notificationManager.createNotificationChannel(new NotificationChannel(CHANNEL_ID_LOOP, "Loop mode notification channel", NotificationManager.IMPORTANCE_HIGH));
//                }
//
//            }
        }
        if (notificationManager != null) {
            notificationManager.notify(NotificationIDs.LOOP_ACTIVE, notification);
        }
    }

    //    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
//    private void addActionToNotificationBuilder(Notification.Builder builder, PendingIntent intent)
//    {
//        builder.addAction(R.drawable.stat_icon_test, "stop", intent);
//    }

    private class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            int numberOfTests = LoopModeConfig.getCurrentTestNumber(context);
            if (maxTests == 0 || numberOfTests < maxTests)
                setAlarm(minDelay);
            else
                stopSelf();
        }
    }

    public boolean isLoopMode() {
        return ((ACTION_START_LOOP.equals(action) || ACTION_LOOP_TEST.equals(action)));
    }
}
