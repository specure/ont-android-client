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

import java.text.MessageFormat;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.specure.opennettest.R;

import at.specure.android.screens.main.InfoCollector;
import at.specure.android.util.GeoLocation;
import at.specure.android.util.NotificationIDs;
import at.specure.android.configs.LoopModeConfig;


public class LoopService extends android.app.Service {
    private static final String TAG = "LoopService";

    private WakeLock partialWakeLock;
    private WakeLock dimWakeLock;
    private boolean isGPSModeEnabled;

    public class RMBTLoopBinder extends Binder {
        public LoopService getService() {
            return LoopService.this;
        }
    }

    private class LocalGeoLocation extends GeoLocation {
        public LocalGeoLocation(Context ctx) {
            super(ctx, LoopModeConfig.isLoopModeGPS(ctx), 10000, maxMovement); // TODO: smaller than maxMovement for minDistance??
        }

        @Override
        public void onLocationChanged(Location curLocation) {
            if (lastTestLocation != null) {
                final float distance = curLocation.distanceTo(lastTestLocation);
                Log.d(TAG, "location distance: " + distance);
                int numberOfTests = LoopModeConfig.getCurrentTestNumber(getApplicationContext());
                if (maxTests == 0 || numberOfTests <= maxTests) {
                    if ((distance >= maxMovement) && (isGPSModeEnabled))
                        onAlarmOrLocation(true);
                } else {
                    stopSelf();
                }

            } else {
                lastTestLocation = curLocation;
            }
            lastLocation = curLocation;
        }
    }

    private class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            int numberOfTests = LoopModeConfig.getCurrentTestNumber(context);
            if (maxTests == 0 || numberOfTests <= maxTests)
                setAlarm(maxDelay);
            else
                stopSelf();
        }
    }

    private static final String ACTION_ALARM = "at.specure.android.Alarm";
    private static final String ACTION_WAKEUP_ALARM = "at.specure.android.WakeupAlarm";
    public static final String ACTION_STOP = "at.specure.android.Stop";

    private static final long ACCEPT_INACCURACY = 1000; // accept 1 sec inaccuracy

    private final RMBTLoopBinder localBinder = new RMBTLoopBinder();

    private AlarmManager alarmManager;
    private PendingIntent alarm;
    private PendingIntent wakeupAlarm;
    private NotificationManager notificationManager;
    private Notification.Builder notificationBuilder;

    private LocalGeoLocation geoLocation;
    private Receiver receiver = new Receiver();

    private Location lastLocation;
    private Location lastTestLocation;
    private long lastTestTime; // SystemClock.elapsedRealtime()

    private long minDelay;
    private long maxDelay;
    private float maxMovement;
    private int maxTests;

    @Override
    public IBinder onBind(Intent intent) {
        return localBinder;
    }

    public void triggerTest() {
//        LoopModeConfig.incrementCurrentTestNumber(this); // moved to TestService only when loop mode is starting new test
        lastTestLocation = lastLocation;
        lastTestTime = SystemClock.elapsedRealtime();
        final Intent service = new Intent(TestService.ACTION_LOOP_TEST, null, this, TestService.class);
        startService(service);

        updateNotification();
    }

    private void readConfig() {
        minDelay = LoopModeConfig.getLoopModeMinDelay(this) * 1000;
        maxDelay = LoopModeConfig.getLoopModeMaxDelay(this) * 1000;
        maxMovement = LoopModeConfig.getLoopModeMaxMovement(this);
        isGPSModeEnabled = LoopModeConfig.isLoopModeGPS(this);
        maxTests = LoopModeConfig.getLoopModeMaxTests(this);
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "created");
        super.onCreate();

        partialWakeLock = ((PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE)).newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK, "RMBTLoopWakeLock");
        partialWakeLock.acquire();

        dimWakeLock = ((PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE)).newWakeLock(
                PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "RMBTLoopDimWakeLock");
        dimWakeLock.acquire();

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        readConfig();

        geoLocation = new LocalGeoLocation(this);
        geoLocation.start();

        notificationBuilder = createNotificationBuilder();

        startForeground(NotificationIDs.LOOP_ACTIVE, notificationBuilder.getNotification());
        registerReceiver(receiver, new IntentFilter(TestService.BROADCAST_TEST_FINISHED));

        final Intent alarmIntent = new Intent(ACTION_ALARM, null, this, getClass());
        alarm = PendingIntent.getService(this, 0, alarmIntent, 0);

        final Intent wakeupAlarmIntent = new Intent(ACTION_WAKEUP_ALARM, null, this, getClass());
        wakeupAlarm = PendingIntent.getService(this, 0, wakeupAlarmIntent, 0);

        final long now = SystemClock.elapsedRealtime();
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, now + 10000, 10000, wakeupAlarm);
    }

    private void setAlarm(long millis) {
        Log.d(TAG, "setAlarm: " + millis);

        final long now = SystemClock.elapsedRealtime();
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, now + millis, alarm);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: " + intent);

        readConfig();
        if (intent != null) {
            final String action = intent.getAction();
            if (action != null && action.equals(ACTION_STOP))
                stopSelf();
            else if (action != null && action.equals(ACTION_ALARM))
                onAlarmOrLocation(false);
            else if (action != null && action.equals(ACTION_WAKEUP_ALARM))
                onWakeup();
            else {
                if (lastTestTime == 0) {
                    LoopModeConfig.resetCurrentTestNumber(this);
                    InfoCollector.getInstance().setLoopModeCurrentTest(1);
                    InfoCollector.getInstance().dispatchInfoChangedEvent(InfoCollector.InfoCollectorType.LOOP_MODE, null, InfoCollector.getInstance().getLoopModeCurrent());
                    Toast.makeText(this, R.string.loop_started, Toast.LENGTH_LONG).show();

                    onAlarmOrLocation(false);
                } else
                    Toast.makeText(this, R.string.loop_already_active, Toast.LENGTH_LONG).show();
            }
        }
        return START_NOT_STICKY;
    }

    @SuppressLint("Wakelock")
    private void onWakeup() {
        if (dimWakeLock != null) {
            if (dimWakeLock.isHeld())
                dimWakeLock.release();
            dimWakeLock.acquire();
        }
    }

    private void onAlarmOrLocation(boolean forceOnLocation) {
        final long now = SystemClock.elapsedRealtime();
        final long lastTestDelta = now - lastTestTime;
        // trigger test if location delta was achieved
        if (forceOnLocation) {
            triggerTest();
            LoopModeConfig.setCurrentlyPerformingLoopMode(getApplicationContext(), true);
            return;
        }
        // trigger test if it is first test in a loop test series
        if (lastTestTime == 0) {
            triggerTest();
            LoopModeConfig.setCurrentlyPerformingLoopMode(getApplicationContext(), true);
            return;
        }
        //trigger test if min delay was achieved
        if (lastTestDelta + ACCEPT_INACCURACY >= minDelay) {
            triggerTest();
            LoopModeConfig.setCurrentlyPerformingLoopMode(getApplicationContext(), true);
            return;
        }
        setAlarm(minDelay - lastTestDelta);
    }

    @Override
    public void onDestroy() {
        if (partialWakeLock != null && partialWakeLock.isHeld())
            partialWakeLock.release();
        if (dimWakeLock != null && dimWakeLock.isHeld())
            dimWakeLock.release();
        Log.d(TAG, "destroyed");
        super.onDestroy();
        unregisterReceiver(receiver);
        stopForeground(true);
        if (geoLocation != null)
            geoLocation.stop();
        if (alarmManager != null) {
            alarmManager.cancel(alarm);
            alarmManager.cancel(wakeupAlarm);
        }
        LoopModeConfig.setCurrentlyPerformingLoopMode(getApplicationContext(), false);
    }

    private Notification.Builder createNotificationBuilder() {
        final Resources res = getResources();

        final CharSequence textTemplate = res.getText(R.string.loop_notification_text);
        int numberOfTests = LoopModeConfig.getCurrentTestNumber(getApplicationContext());
        final CharSequence text = MessageFormat.format(textTemplate.toString(), numberOfTests);

//        final PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(
//                getApplicationContext(), MainActivity.class), 0);

        final Intent stopIntent = new Intent(ACTION_STOP, null, getApplicationContext(), getClass());
        final PendingIntent stopPIntent = PendingIntent.getService(getApplicationContext(), 0, stopIntent, 0);

        final Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.stat_icon_loop)
                .setContentTitle(res.getText(R.string.loop_notification_title))
                .setContentText(text)
                .setTicker(res.getText(R.string.loop_notification_ticker))
                .setContentIntent(stopPIntent);

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
//            addActionToNotificationBuilder(builder, stopPIntent);
//        else
//            builder.setContentIntent(stopPIntent);
        return builder;
    }

//    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
//    private void addActionToNotificationBuilder(Notification.Builder builder, PendingIntent intent)
//    {
//        builder.addAction(R.drawable.stat_icon_test, "stop", intent);
//    }

    private void updateNotification() {
        if (notificationBuilder == null) {
            notificationBuilder = createNotificationBuilder();
        }
        int numberOfTests = LoopModeConfig.getCurrentTestNumber(getApplicationContext());
        final Notification notification = notificationBuilder.setNumber(numberOfTests).getNotification();
        if (notificationManager == null) {
            notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        }
        notificationManager.notify(NotificationIDs.LOOP_ACTIVE, notification);
    }
}
