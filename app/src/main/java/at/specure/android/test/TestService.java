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

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.specure.opennettest.R;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

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
import at.specure.androidX.data.test.TestResultView;
import at.specure.androidX.test.test_service_connector.TestServiceInterface;
import at.specure.client.QualityOfServiceTest;
import at.specure.client.QualityOfServiceTest.Counter;
import at.specure.client.helper.IntermediateResult;
import at.specure.client.helper.NdtStatus;
import at.specure.client.v2.task.QoSTestEnum;
import at.specure.client.v2.task.result.QoSTestResultEnum;
import timber.log.Timber;

public class TestService extends android.app.Service implements EndTaskListener, LocationChangeListener, TestResultFetcher {
    public static final String ACTION_START_SERVICE = "at.specure.android.startTestService";
    public static final String ACTION_STOP_SERVICE = "at.specure.android.stopTestService";
    public static final String ACTION_START_TEST = "at.specure.android.startTest";
    public static final String ACTION_LOOP_TEST = "at.specure.android.loopTest";
    public static final String ACTION_START_LOOP = "at.specure.android.startLoop";
    public static final String ACTION_STOP_LOOP = "at.specure.android.stopLoop";
    public static final String ACTION_ABORT_TEST = "at.specure.android.abortTest";
    private static final String ACTION_ALARM = "at.specure.android.Alarm";
    private static final String ACTION_WAKEUP_ALARM = "at.specure.android.WakeupAlarm";
    public static String BROADCAST_TEST_FINISHED = "at.specure.android.test.TestService.testFinished";
    private static final String CHANNEL_ID_SERVICE = "SERVICE_NOTIFICATION_CHANNEL";


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
    private final RMBTBinder localRMBTBinder = new RMBTBinder();
    private boolean completed = false;
    private NotificationManagerCompat notificationManager;


    ///LOOP FIELDS
    private long looplastTestTime = 0; // SystemClock.elapsedRealtime()
    private WakeLock dimWakeLock;
    private AlarmManager alarmManager;
    private ArrayList<Location> looplastLocation;
    private Location looplastTestLocation;
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
    private NotificationCompat.Builder serviceNotificationBuilder;
    private List<TestResultView> testResultViews;
    private float currentDistanceFromLastTest;
    private Integer lastNotificationID;
    private Notification lastNotification;


    @Override
    public void sendTestResults(List<TestResultView> results) {
        this.testResultViews = results;
    }

    public boolean isLastTest() {
        int numberOfTests = LoopModeConfig.getCurrentTestNumber(getApplicationContext());
        int loopModeMaxTests = LoopModeConfig.getLoopModeMaxTests(getApplicationContext());
        if (numberOfTests == loopModeMaxTests) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Class for clients to access. Because we know this service always runs in
     * the same process as its clients, we don't need to deal with IPC.
     */
    public class RMBTBinder extends Binder {

        TestServiceInterface testServiceInterface;

        public TestService getService() {
            // Return this instance of TestService so clients can call public
            // methods
            return TestService.this;
        }

        public void setTestServiceInterface(TestServiceInterface testServiceInterface) {
            this.testServiceInterface = testServiceInterface;
        }

        public void notifyTestStarted() {
            if (this.testServiceInterface != null) {
                this.testServiceInterface.testStarted();
            }
        }

        public void notifyTestCancelled() {
            if (this.testServiceInterface != null) {
                this.testServiceInterface.testCancelled();
            }
        }

        public void notifyLoopModeStopped(boolean cancelledByUser) {
            if (this.testServiceInterface != null) {
                this.testServiceInterface.loopModeStopped(cancelledByUser);
            }
        }

        public void notifyDistanceToNextTestHasChanged(Float distance) {
            Timber.e("Next test location changed in binder");
            if (this.testServiceInterface != null) {
                this.testServiceInterface.locationChanged(distance);
            }
        }
    }

    @Override
    public void onCreate() {
        Timber.d("created");
        super.onCreate();

        handler = new Handler();

        acquireLocks();

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        notificationManager = NotificationManagerCompat.from(getApplicationContext());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager != null) {
                NotificationChannel test_service_notification_channel = new NotificationChannel(CHANNEL_ID_SERVICE, "Test service notification channel", NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(test_service_notification_channel);
            }
        }
        createServiceNotification();
    }


    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        String actionLocal = null;
        String previousAction = null;
        if (intent != null)
            actionLocal = intent.getAction();
        else {
            //default if it was killed by system during creation
            actionLocal = ACTION_START_SERVICE;
        }

        // ignoring wake calls ACTION_WAKEUP_ALARM, ACTION_ALARM for remembering last called action
        if (ACTION_START_LOOP.equals(actionLocal)
                || ACTION_START_TEST.equals(actionLocal)
                || ACTION_STOP_LOOP.equals(actionLocal)
                || ACTION_ABORT_TEST.equals(actionLocal)
                || ACTION_LOOP_TEST.equals(actionLocal)
                || ACTION_START_SERVICE.equals(actionLocal)
                || ACTION_STOP_SERVICE.equals(actionLocal)
        ) {
            previousAction = action;
            action = actionLocal;
        }

        Timber.i("onStartCommand; action= %s", actionLocal);


        switch (actionLocal) {
            case ACTION_START_SERVICE:
                if (previousAction != null && (previousAction.equalsIgnoreCase(ACTION_START_LOOP) || previousAction.equalsIgnoreCase(ACTION_LOOP_TEST) || previousAction.equalsIgnoreCase(ACTION_START_TEST))) {
                    //ignore changing notification when service is already started
                    if ((lastNotificationID != null) && (lastNotification != null)) {
                        startForeground(lastNotificationID, lastNotification);
                    } else {
                        createServiceNotification();
                    }
                } else {
                    Timber.e("service started");
                    GeoLocationX.getInstance(this.getApplication()).getLastKnownLocation(this, this);
                    createServiceNotification();
                }
                break;

            case ACTION_STOP_SERVICE:
                createServiceNotification();
                GeoLocationX.getInstance(this.getApplication()).getLastKnownLocation(this, this);
                stopForeground(true);
                notificationManager.cancel(NotificationIDs.SERVICE_ACTIVE);
                Timber.e("Remove notification of the service");
                removeCallbacks();
                removeAlarms();
                stopSelf();
                Timber.e("service stopped");
                break;

            case ACTION_ALARM:
                Timber.e("LOOP Action alarm");
                onAlarmOrLocation(false);
                return START_NOT_STICKY;

            case ACTION_WAKEUP_ALARM:
                Timber.e("LOOP Wake up");
                onWakeup();
                break;

            case ACTION_START_LOOP: // use only for starting loop mode
                //resetLoopTestNumberAndVariables
                createServiceNotification();
                resetLoopModeVariables();

                Timber.e("service loop started");
                //read config and set up listeners
                acquireLocks();
                readConfig();
                GeoLocationX.getInstance(this.getApplication()).getLastKnownLocation(this, this);
                // todo: set up signal listener when special class will be ready
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

                if (looplastTestTime <= 0) {
                    LoopModeConfig.resetCurrentTestNumber(this);
                    InfoCollector.getInstance().setLoopModeCurrentTest(1);
                    InfoCollector.getInstance().dispatchInfoChangedEvent(InfoCollector.InfoCollectorType.LOOP_MODE, null, InfoCollector.getInstance().getLoopModeCurrent());

                    serviceNotificationBuilder = createNotificationBuilderLoop(true);
                    Notification build = serviceNotificationBuilder.build();
                    lastNotification = build;
                    lastNotificationID = NotificationIDs.SERVICE_ACTIVE;
                    startForeground(NotificationIDs.SERVICE_ACTIVE, build);

                    onAlarmOrLocation(false);
                    Timber.e("LOOP Start");
                    Toast.makeText(this, R.string.loop_started, Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(this, R.string.loop_already_active, Toast.LENGTH_LONG).show();
                    Timber.e("LOOP already started");
                }

                return START_NOT_STICKY;

            case ACTION_ABORT_TEST:
            case ACTION_STOP_LOOP:
                //same for both of them
                Timber.e("service test started");
                Timber.i("ACTION_ABORT_TEST/STOP LOOP received");
                stopTest();
                if (ACTION_STOP_LOOP.equals(actionLocal)) {
                    stopLoopMode(true);
                }
                if (testTask != null) {
                    testTask.cancel();
                }
                completed = false;
                testTask = null;
                createServiceNotification();
                return START_NOT_STICKY;


            case ACTION_START_TEST:
            case ACTION_LOOP_TEST:
                createServiceNotification();
                acquireLocks();
                if (ACTION_START_TEST.equals(actionLocal)) {// do not cancel test if running in loop mode
                    if (testTask != null) {
                        testTask.cancel();
                    }
                }

                if (ACTION_LOOP_TEST.equals(actionLocal)) {
                    if (testTask != null) {
                        if (testTask.isRunning()) {
                            // do not cancel test if running in loop mode
                            testTask.checkZeroMeasurement();
                            return START_NOT_STICKY;
                        } else {
                            testTask.cancel(); // otherwise cancel
                        }
                    }
                }


                completed = false;

                // lock wifi + power
                lock();

                if (testResultViews == null) {
                    testResultViews = new ArrayList<>();
                } else {
                    testResultViews.clear();
                }

                if (ACTION_LOOP_TEST.equals(actionLocal)) {
                    LoopModeConfig.incrementCurrentTestNumber(this);
                    testTask = new TestTask(this, true, LoopModeConfig.getCurrentTestNumber(getApplicationContext()) == 1, this);
                    Toast.makeText(this, R.string.loop_test_started, Toast.LENGTH_LONG).show();
                } else {
                    if (localRMBTBinder != null) {
                        localRMBTBinder.notifyTestStarted();
                    }
                    testTask = new TestTask(this, this);
                    Toast.makeText(this, R.string.test_started, Toast.LENGTH_LONG).show();
                }

                testTask.setEndTaskListener(this);
                testTask.execute(handler);
                Timber.d("RMBTTest started");

                handler.postDelayed(addNotificationRunnable, 200);
                long DEADMAN_TIME = 120 * 1000;
                handler.postDelayed(deadman, DEADMAN_TIME);

                return START_STICKY;
        }

        return START_NOT_STICKY;
    }

    private void resetLoopModeVariables() {
        looplastTestTime = -1;
        looplastLocation = null;
        looplastTestLocation = null;
        LoopModeConfig.resetCurrentTestNumber(this.getApplicationContext());
        LoopModeConfig.setCurrentlyPerformingLoopMode(this.getApplicationContext(), true);
        LoopModeConfig.resetMedianValues(this.getApplicationContext());
        LoopModeConfig.resetCurrentLoopId(this.getApplicationContext());
    }

    private void acquireLocks() {
        // initialise the locks
        if (wifiLock == null) {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null) {
                wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "RMBT:RMBTWifiLock");
            }
        }
        if (wifiLock != null && !wifiLock.isHeld()) {
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
    }

    private void releaseLocks() {
        if (wakeLock != null && wakeLock.isHeld())
            wakeLock.release();
        if (dimWakeLock != null && dimWakeLock.isHeld())
            dimWakeLock.release();
        if (wifiLock != null && wifiLock.isHeld())
            wifiLock.release();
    }


    /**
     * Stops executing any test and releases locks and alarms, stops whole service
     */
    public void stopLoopMode(boolean cancelledByUser) {
        stopTest();
        releaseLocks();
        Timber.d("stoppingLoopMode");
        if (receiverRegistered) {
            unregisterReceiver(receiver);
            receiverRegistered = false;
        }
        removeAlarms();
        LoopModeConfig.setCurrentlyPerformingLoopMode(getApplicationContext(), false);
//        stopSelf();
//        stopForeground(true);

        looplastTestTime = 0;
        looplastTestLocation = null;
        looplastLocation = null;
        localRMBTBinder.notifyLoopModeStopped(cancelledByUser);

    }

    private void removeNotifications() {
        removeNotificationService();
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


    @Override
    public void onDestroy() {
        Timber.d("destroyed");
        super.onDestroy();
        LoopModeConfig.setCurrentlyPerformingLoopMode(getApplicationContext(), false);
        removeNotifications();
        releaseLocks();
        removeCallbacks();
        removeAlarms();
        if (notificationManager != null) {
            notificationManager.cancelAll();
        }
        stopTest();
        try {
            FirebaseCrashlytics.getInstance().recordException(new Throwable("Service was destroyed by system at " + DateFormat.getInstance().format(new Date())));
        } catch (IllegalStateException ignored) {

        }

        stopLoopMode(false);
    }

    private void removeCallbacks() {
        handler.removeCallbacks(addNotificationRunnable);
        handler.removeCallbacks(deadman);
    }


    public void stopTest() {

        if (testTask != null) {
            Timber.d("RMBTTest stopped");
            testTask.cancel();
            taskEnded();
            testTask = null;
        }
        releaseLocks();
        LoopModeConfig.setCurrentlyPerformingLoopMode(getApplicationContext(), false);
        localRMBTBinder.notifyTestCancelled();
    }

    public boolean isTestRunning() {
        return testTask != null && testTask.isRunning();
    }

    private void addNotificationIfTestRunning() {
        if (notificationManager == null) {
            notificationManager = NotificationManagerCompat.from(getApplicationContext());
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager != null) {
                Timber.e("Create notification if running O");
                notificationChannel = new NotificationChannel(CHANNEL_ID_SERVICE, "Test service notification channel", NotificationManager.IMPORTANCE_HIGH);
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        if (isTestRunning()) {
            createTestNotification();
        }
    }


    private void createTestNotification() {

        if (isLoopModeRunning()) {
            updateNotificationLoop();
        } else {
            final Resources res = getResources();

            final PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(
                    getApplicationContext(), MainActivity.class), 0);

            if (serviceNotificationBuilder == null) {
                serviceNotificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID_SERVICE);
            }
            Notification notification = serviceNotificationBuilder
                    .setSmallIcon(R.drawable.service_running)
                    .setContentTitle(res.getText(R.string.test_notification_title))
                    .setContentText(res.getText(R.string.test_notification_text))
                    .setTicker(res.getText(R.string.test_notification_ticker))
                    .setContentIntent(contentIntent)
                    .setOngoing(false)
                    .build();


            lastNotificationID = NotificationIDs.SERVICE_ACTIVE;
            lastNotification = notification;
            startForeground(NotificationIDs.SERVICE_ACTIVE, notification);
            if (notificationManager != null) {
                notificationManager.notify(NotificationIDs.SERVICE_ACTIVE, notification);
                Timber.e("Create notification if running");
            }
        }
    }


    private void removeNotificationService() {
        stopForeground(true);
        notificationManager.cancel(NotificationIDs.SERVICE_ACTIVE);
        Timber.e("Remove notification of the service");
    }


    @Override
    public IBinder onBind(final Intent intent) {
        Timber.e("binding");
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
        Timber.e("Unbinding");
        bound = false;
        handler.postDelayed(addNotificationRunnable, 200);
        return true;
    }

    @Override
    public void onRebind(final Intent intent) {
        bound = true;
        Timber.e("Rebinding");
        updateNotificationLoop();
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

    public float getCurrentDistanceFromLastTest() {
        return currentDistanceFromLastTest;
    }

    public int getSignalType() {
        if (testTask != null)
            return testTask.getSignalType();
        else
            return InformationCollector.SIGNAL_TYPE_NO_SIGNAL;
    }

    public List<TestResultView> getTestResults() {
        return testResultViews;
    }

    public void addToTestResults(TestResultView resultToAdd) {
        if (testResultViews == null) {
            testResultViews = new ArrayList<>();
        }
        testResultViews.add(resultToAdd);
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
        if (!isLoopModeRunning()) {
//            stopSelf();
            Timber.d("Results should be shown set from service");
        } else if (isLastTest()) {
            this.action = ACTION_START_SERVICE;
            localRMBTBinder.notifyLoopModeStopped(false);
            removeAlarms();
            InfoCollector.getInstance().notifyLoopModeFinished();
            LoopModeConfig.setCurrentlyPerformingLoopMode(getApplicationContext(), false);

//            stopSelf();
//            notificationManager.cancelAll();

            stopForeground(true);
            looplastLocation = null;
            looplastTestTime = 0L;
            looplastTestLocation = null;
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
            if (looplastTestLocation != null) {
//                final float distance = curLocation.distanceTo(looplastTestLocation);
                currentDistanceFromLastTest = countMediumDistance(looplastLocation, looplastTestLocation);
                if ((localRMBTBinder != null) && (isLoopModeRunning() && (!isTestRunning()))) {
                    localRMBTBinder.notifyDistanceToNextTestHasChanged(currentDistanceFromLastTest);
                }
                Timber.d("location distance: %s", currentDistanceFromLastTest);
//                Toast.makeText(this.context, "DISTANCE: " + distance, Toast.LENGTH_LONG).show();
                int numberOfTests = LoopModeConfig.getCurrentTestNumber(getApplicationContext());
                if (maxTests == 0 || numberOfTests < maxTests) {
                    if ((currentDistanceFromLastTest >= maxMovement) && (isGPSModeEnabled))
                        onAlarmOrLocation(true);
                } else {
//                    stopSelf();
                }

            } else {
                if (curLocation.getAccuracy() < ACCURACY_ACCEPTED) {
                    looplastTestLocation = curLocation;
                }
            }

            if (curLocation.getAccuracy() < ACCURACY_ACCEPTED) {
                if (looplastLocation == null) {
                    looplastLocation = new ArrayList<Location>();
                }
                looplastLocation.add(curLocation);
                if ((looplastLocation.size() > LOCATION_BUFFER_SIZE) && (looplastLocation.size() > 0)) {
                    looplastLocation.remove(0);
                }
            }
            if (this != null && ConfigHelper.detectZeroMeasurementEnabled(this)) {
                ZeroMeasurementDetector.detectZeroMeasurement(null, this, InformationCollector.getInstance(this, true, true, false));
            }

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
        completed = false;
//        LoopModeConfig.incrementCurrentTestNumber(this); // moved to TestService only when loop mode is starting new test
        if (looplastLocation != null && !looplastLocation.isEmpty()) {
            looplastTestLocation = looplastLocation.get(looplastLocation.size() - 1);
        }
        looplastTestTime = SystemClock.elapsedRealtime();
        Timber.e("LOOP service starting test");


        final Intent service = new Intent(TestService.ACTION_LOOP_TEST, null, this, TestService.class);
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                localRMBTBinder.notifyTestStarted();
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
//        lastNotification = build;
//        lastNotificationID = NotificationIDs.LOOP_ACTIVE;
//        startForeground(NotificationIDs.LOOP_ACTIVE, build);

        updateNotificationLoop();

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
        final long lastTestDelta = now - looplastTestTime;
        // trigger test if location delta was achieved
        if (forceOnLocation) {
            triggerTest();
            LoopModeConfig.setCurrentlyPerformingLoopMode(getApplicationContext(), true);
//            Toast.makeText(this, "GPS triggered test" , Toast.LENGTH_LONG).show();
            return;
        }
        // trigger test if it is first test in a loop test series
        if (looplastTestTime == 0) {
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


    private void createServiceNotification() {
//        if (serviceNotificationBuilder == null) {
            serviceNotificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID_SERVICE);
//        }

        Intent stopIntent = new Intent(this, TestService.class);
        stopIntent.setAction(ACTION_STOP_SERVICE);
        PendingIntent actionIntent = PendingIntent.getService(this, 0, stopIntent, 0);
        NotificationCompat.Action action = new NotificationCompat.Action.Builder(0, this.getString(android.R.string.cancel), actionIntent).build();


        serviceNotificationBuilder
                .setOnlyAlertOnce(true)
                .setSmallIcon(R.drawable.service_running)
                .addAction(action)
                .setContentTitle("service is running")
                .setContentText("service is running");

        Notification build = serviceNotificationBuilder.build();
        lastNotification = build;
        lastNotificationID = NotificationIDs.SERVICE_ACTIVE;
        startForeground(NotificationIDs.SERVICE_ACTIVE, build);
    }

    private void createTestIsRunningNotification() {
//        if (serviceNotificationBuilder == null) {
            serviceNotificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID_SERVICE);
//        }

        serviceNotificationBuilder
                .setSmallIcon(R.drawable.service_running)
                .setContentTitle("Test is running")
                .setContentText("Test is running");

        Notification build = serviceNotificationBuilder.build();
        lastNotification = build;
        lastNotificationID = NotificationIDs.SERVICE_ACTIVE;
        startForeground(NotificationIDs.SERVICE_ACTIVE, build);
    }


    private NotificationCompat.Builder createNotificationBuilderLoop(boolean runningTest) {
        final Resources res = getResources();

        CharSequence text = "";
        int numberOfTests = LoopModeConfig.getCurrentTestNumber(getApplicationContext());

        if (runningTest) {
            text = res.getString(R.string.loop_notification_text_2, isLoopFirstTest() ? 1 : numberOfTests);
//            text = string;//MessageFormat.format(textTemplate.toString(), numberOfTests);
        } else {
            text = res.getString(R.string.loop_notification_text, numberOfTests);
//            text = MessageFormat.format(textTemplate.toString(), numberOfTests);
        }


//        final PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(
//                getApplicationContext(), MainActivity.class), 0);

        final Intent stopIntent = new Intent(ACTION_STOP_LOOP, null, getApplicationContext(), getClass());
        final PendingIntent stopPIntent = PendingIntent.getService(getApplicationContext(), 0, stopIntent, 0);

//        if (serviceNotificationBuilder == null) {
            serviceNotificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID_SERVICE);

//        }
        serviceNotificationBuilder.setSmallIcon(R.drawable.service_running)
                .setContentTitle(res.getText(R.string.loop_notification_title))
                .setContentText(text)
                .setTicker(res.getText(R.string.loop_notification_ticker))
                .setContentIntent(stopPIntent);

        serviceNotificationBuilder.build();

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
//            addActionToNotificationBuilder(builder, stopPIntent);
//        else
//            builder.setContentIntent(stopPIntent);
        return serviceNotificationBuilder;
    }

    private boolean isLoopFirstTest() {
        return looplastTestTime <= 0;
    }

    private void updateNotificationLoop() {

        serviceNotificationBuilder = createNotificationBuilderLoop(isTestRunning());
        int numberOfTests = LoopModeConfig.getCurrentTestNumber(getApplicationContext());
        final Notification notification = serviceNotificationBuilder.setNumber(numberOfTests).build();
        if (notificationManager == null) {
            notificationManager = NotificationManagerCompat.from(getApplicationContext());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (notificationManager != null) {
                    Timber.e("Create notification if running O");
                    notificationChannel = new NotificationChannel(CHANNEL_ID_SERVICE, "Test service notification channel", NotificationManager.IMPORTANCE_HIGH);
                    notificationManager.createNotificationChannel(notificationChannel);
                }
            }
        }
        if (notificationManager != null) {
            lastNotification = notification;
            lastNotificationID = NotificationIDs.SERVICE_ACTIVE;
            notificationManager.notify(NotificationIDs.SERVICE_ACTIVE, notification);
            Timber.e("Update notification loop");
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
            else {
//                stopSelf();
            }
        }
    }

    public boolean isLoopModeRunning() {
        Timber.e("State changing service start action: %s", action);
        return ((ACTION_START_LOOP.equals(action) || ACTION_LOOP_TEST.equals(action)));
    }
}
