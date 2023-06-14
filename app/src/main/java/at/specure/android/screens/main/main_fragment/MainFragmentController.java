/*******************************************************************************
 * Copyright 2014-2017 Specure GmbH
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
 *******************************************************************************/

package at.specure.android.screens.main.main_fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonParseException;

import java.util.Locale;

import at.specure.android.api.calls.CheckTestResultDetailTask;
import at.specure.android.configs.ConfigHelper;
import at.specure.android.configs.TestConfig;
import at.specure.android.screens.main.MainActivity;
import at.specure.android.screens.main.main_fragment.graphs_handlers.GraphHandler;
import at.specure.android.screens.result.adapter.result.ResultDetailType;
import at.specure.android.test.ChangeableSpeedTestStatus;
import at.specure.android.test.SpeedTestStatViewController;
import at.specure.android.test.TestService;
import at.specure.android.test.UIUpdateInterface;
import at.specure.android.test.runnables.ResultSwitcher;
import at.specure.android.test.runnables.UITestUpdater;
import at.specure.android.util.EndTaskListener;
import at.specure.android.util.Helperfunctions;
import at.specure.android.util.InformationCollector;
import at.specure.android.util.net.NetworkUtil;
import at.specure.client.helper.IntermediateResult;
import at.specure.client.helper.NdtStatus;
import at.specure.client.helper.TestStatus;
import at.specure.client.v2.task.QoSTestEnum;
import at.specure.client.v2.task.result.QoSServerResultCollection;
import timber.log.Timber;

import static at.specure.android.screens.main.main_fragment.MainMenuFragment.MAX_COUNTER_WITHOUT_RESULT;
import static at.specure.android.screens.main.main_fragment.MainMenuFragment.PERCENT_FORMAT;
import static at.specure.android.screens.main.main_fragment.MainMenuFragment.PROGRESS_SEGMENTS_PROGRESS_RING;
import static at.specure.android.screens.main.main_fragment.MainMenuFragment.PROGRESS_SEGMENTS_QOS;
import static at.specure.android.screens.main.main_fragment.MainMenuFragment.PROGRESS_SEGMENTS_TOTAL;
import static at.specure.android.util.InformationCollector.SIGNAL_TYPE_NO_SIGNAL;
import static at.specure.client.helper.TestStatus.DOWN;
import static at.specure.client.helper.TestStatus.ERROR;
import static at.specure.client.helper.TestStatus.INIT;
import static at.specure.client.helper.TestStatus.INIT_UP;
import static at.specure.client.helper.TestStatus.PACKET_LOSS_AND_JITTER;
import static at.specure.client.helper.TestStatus.PING;
import static at.specure.client.helper.TestStatus.UP;
import static at.specure.client.helper.TestStatus.WAIT;

/**
 * This is main controller for the main fragment
 * Created by michal.cadrik on 10/23/2017.
 */

public class MainFragmentController implements ServiceConnection, UIUpdateInterface {

    private static final String TAG = "MainFragContr";
    private final String waitText;
    private final GraphInterface graphInterface;
    private MainFragmentInterface mainFragmentInterface;
    private AlertDialog testErrorDialog;
    private AlertDialog testAbortDialog;
    private ProgressDialog testProgressDialog;
    private TestService testTestService;
    private boolean testError = false;
    private boolean isTestStoppedByUser = false;
    private IntermediateResult testIntermediateResult;
    private long testUpdateCounter;
    private boolean qosMode; // this indicates if qos test were running in the test run to decide whether show simple results or results with qos
    private Integer lastSignal;
    private int testLastSignalType;
    private boolean testShowQoSErrorToast;
    private UITestUpdater updateUITask;
    private Handler testHandler;
    private GraphHandler graphHandler;
    private boolean stopLoop;
    private ResultSwitcher testResultSwitcherRunnable;
    private QoSTestEnum lastQoSTestStatus;
    private long testLastShownWaitTime;
    private String testLastStatusString;
    private Integer signal;
    private int signalType;
    private int lastProgressSegments;
    private TestStatus testLastStatus;
    private SpeedTestStatViewController testSpeedTestStatViewController;
    private final DialogInterface.OnKeyListener backKeyListener = new DialogInterface.OnKeyListener() {
        @Override
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            return keyCode == KeyEvent.KEYCODE_BACK && onMainBackPressed();
        }
    };
    private InformationCollector informationCollector;
    private boolean running;
    private TestService.RMBTBinder binder;

    MainFragmentController(MainFragmentInterface mainFragmentInterface, GraphInterface graphInterface, String waitText) {
        this.mainFragmentInterface = mainFragmentInterface;
        this.graphInterface = graphInterface;
        this.waitText = waitText;
    }

    private static void dismissDialog(Dialog dialog) {
        if (dialog != null)
            dialog.dismiss();
    }

    boolean onMainBackPressed() {
        Timber.d("onbackpressed");
        if (mainFragmentInterface != null) {
            final Activity activity = mainFragmentInterface.getMainActivity();
            if (activity == null)
                return false;
            if ((testErrorDialog != null && testErrorDialog.isShowing()) || (testProgressDialog != null && testProgressDialog.isShowing())) {
                if (testTestService != null)
                    testTestService.stopTest();
                else {
                    // to be sure test is stopped:
                    final Intent service = new Intent(TestService.ACTION_ABORT_TEST, null, activity, TestService.class);
                        activity.startService(service);
                    testTestService = null;
                }
                dismissDialogs();
                mainFragmentInterface.changeScreenState(MainScreenState.DEFAULT, "OnBackPress", true);
                return true;
            }
            if (testAbortDialog != null && testAbortDialog.isShowing()) {
                dismissDialog(testAbortDialog);
                testAbortDialog = null;
            } else {
                final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle(mainFragmentInterface.getAbortDialogTitleId());
                builder.setMessage(mainFragmentInterface.getAbortDialogTextId());
                builder.setPositiveButton(mainFragmentInterface.getAbortDialogPositiveButtonText(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        if (testTestService != null) {
                            isTestStoppedByUser = true;
                            testTestService.stopTest();
                        }
                        final Intent service = new Intent(TestService.ACTION_ABORT_TEST, null, activity, TestService.class);
                            activity.startService(service);
                        testTestService = null;
                        unbindTestingService();
                        mainFragmentInterface.changeScreenState(MainScreenState.DEFAULT, "TestAbortDialogPositive", true);
                    }
                });
                builder.setNegativeButton(mainFragmentInterface.getAbortDialogNegativeButtonText(), null);

                dismissDialogs();
                testAbortDialog = builder.show();
            }
            return true;
        }
        return false;
    }

    void dismissDialogs() {
        dismissDialog(testErrorDialog);
        testErrorDialog = null;
        dismissDialog(testAbortDialog);
        testAbortDialog = null;
        dismissDialog(testProgressDialog);
        testProgressDialog = null;
    }

    private void showErrorDialog(int errorMessageId, int dialogTitleId) {
        if (updateUITask != null) {
            updateUITask.setStopLoop(true);
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(mainFragmentInterface.getMainActivity());
        builder.setTitle(dialogTitleId);
        builder.setMessage(errorMessageId);
        builder.setNeutralButton(android.R.string.ok, null);
        this.dismissDialogs();
        testError = true;
        testErrorDialog = builder.create();
        testErrorDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mainFragmentInterface.changeScreenState(MainScreenState.DEFAULT, "TestErrorDialogDismiss", true);
            }
        });
        testErrorDialog.show();
    }

    void bindTestingService() {
        MainActivity activity = mainFragmentInterface.getMainActivity();
        if (activity != null) {

            unbindTestingService();//because there can be some bounded services
            // Bind to TestService
            final Intent serviceIntent = new Intent(activity, TestService.class);
            boolean b = activity.bindService(serviceIntent, this, Context.BIND_AUTO_CREATE);

            Timber.d("TUUI: STS set at bind testing services");
            testSpeedTestStatViewController = new SpeedTestStatViewController(mainFragmentInterface.getMainActivity());
        }
    }

    void unbindTestingService() {
        MainActivity activity = mainFragmentInterface.getMainActivity();
        if (updateUITask != null) {
            updateUITask.setStopLoop(true);
            testHandler.removeCallbacks(updateUITask);
            updateUITask = null;
        }

        if (testResultSwitcherRunnable != null) {
            testHandler.removeCallbacks(testResultSwitcherRunnable);
            testResultSwitcherRunnable = null;
        }

        try {
            activity.unbindService(this);
        } catch (Exception e) {
            Timber.e("TEST SERVICE unbinding not registered service");
        }

        if (activity != null) {
            activity.setToolbarVisible(View.VISIBLE);
            activity.setLockNavigationDrawer(false);
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        Timber.d("TUUI: STS set tu null at unbind testing services");
        testSpeedTestStatViewController = null;
    }

    TestService getTestTestService() {
        return this.testTestService;
    }


    @Override
    public void onServiceConnected(final ComponentName name, final IBinder service) {
        Timber.d("service connected");
        final TestService.RMBTBinder binder = (TestService.RMBTBinder) service;
        this.testTestService = binder.getService();
        if ((testTestService != null) && testTestService.isTestRunning() && !testTestService.isLoopModeRunning()) {
            MainActivity activity = mainFragmentInterface.getMainActivity();
            if (activity != null) {
                activity.setToolbarVisible(View.INVISIBLE);
                activity.setLockNavigationDrawer(true);
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }

            // start UI updates according to a test process
            if (testHandler == null) {
                testHandler = new Handler();
            }
            if (updateUITask == null) {
                updateUITask = new UITestUpdater(qosMode, stopLoop, this, testTestService, testHandler);
            } else {
                updateUITask.setRMBTService(this.testTestService);
            }
            testHandler.post(updateUITask);

            //getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        }
        /*if (updateUITask != null) {
            updateUITask.setRMBTService(this.testTestService);
        }*/
    }

    @Override
    public void onServiceDisconnected(final ComponentName name) {
        Timber.d("service disconnected");
        this.testTestService = null;
        if (updateUITask != null) {
            updateUITask.setRMBTService(null);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void updateTestUI() {
        testUpdateCounter++;

        if (testTestService == null)
            return;

        if (testTestService.isLoopModeRunning()) {
            if ((mainFragmentInterface.getScreenState() == MainScreenState.DEFAULT)) {
                mainFragmentInterface.changeScreenState(MainScreenState.LOOP_MODE_ACTIVE, "UIUpdate - maxcounter1", true);
            }
        } else {
            if ((mainFragmentInterface.getScreenState() == MainScreenState.DEFAULT) && (testTestService.isTestRunning())) {
                mainFragmentInterface.changeScreenState(MainScreenState.TESTING, "UIUpdate - maxcounter0", true);
            }
        }

        testIntermediateResult = testTestService.getIntermediateResult(testIntermediateResult);

        if (testIntermediateResult == null) {
            if (testTestService.isConnectionError()) {
                if (testProgressDialog != null) {
                    testProgressDialog.dismiss();
                    testProgressDialog = null;
                }
                if (!testTestService.isLoopModeRunning()) {
                    this.showErrorDialog(mainFragmentInterface.getErrorControlServerConnectionStringId(), mainFragmentInterface.getTestErrorTitleId());
                    return;
                }
            }

            if (!testTestService.isTestRunning() && testUpdateCounter > MAX_COUNTER_WITHOUT_RESULT && !testTestService.isLoopModeRunning())
                mainFragmentInterface.changeScreenState(MainScreenState.DEFAULT, "UIUpdate - maxcounter", true);
            return;
        }

        if (testIntermediateResult.status == WAIT) {
            if (testProgressDialog != null) {
                long wait = (testIntermediateResult.remainingWait + 999) / 1000; // round
                // up
                if (wait < 0)
                    wait = 0;
                if (wait != testLastShownWaitTime) {
                    testLastShownWaitTime = wait;
                    testProgressDialog.setMessage(waitText);
                }
            }
            return;
        }

        if (testProgressDialog != null) {
            testProgressDialog.dismiss();
            testProgressDialog = null;
        }

        if (testTestService.getNdtStatus() == NdtStatus.RUNNING) {
            final String ndtStatus = String.format(Locale.US, "NDT (%d%%)", Math.round(testTestService.getNDTProgress() * 100));
            if (testLastStatusString == null || !ndtStatus.equals(testLastStatusString)) {
                testLastStatusString = ndtStatus;
            }
        } else if (testLastStatus != testIntermediateResult.status) {
            testLastStatus = testIntermediateResult.status;
            Context context = getContext();
            if (context != null) {
                testLastStatusString = Helperfunctions.getTestStatusString(context.getResources(), testIntermediateResult.status);
            }
        }

        this.setSignalValue();


        double speedValueRelative = 0d;
        int progressSegments = 0;

        double progressValue = (double) progressSegments / PROGRESS_SEGMENTS_PROGRESS_RING;
        double correctProgressValue = progressValue;
        double totalProgressValue = correctProgressValue * PROGRESS_SEGMENTS_PROGRESS_RING / (double) PROGRESS_SEGMENTS_TOTAL;

        Double relativeSignal = getRelativeSignal();
        switch (testIntermediateResult.status) {
            case WAIT:
                break;

            case INIT:
                progressSegments = SpeedTestStatViewController.InfoStat.INIT.getProgressForGauge(testIntermediateResult.progress);
                progressValue = (double) progressSegments / PROGRESS_SEGMENTS_PROGRESS_RING;
                correctProgressValue = progressValue;
                totalProgressValue = correctProgressValue * PROGRESS_SEGMENTS_PROGRESS_RING / (double) PROGRESS_SEGMENTS_TOTAL;
                break;

            case PING:
                progressSegments = SpeedTestStatViewController.InfoStat.PING.getProgressForGauge(testIntermediateResult.progress);
                progressValue = (double) progressSegments / PROGRESS_SEGMENTS_PROGRESS_RING;
                correctProgressValue = progressValue;
                totalProgressValue = correctProgressValue * PROGRESS_SEGMENTS_PROGRESS_RING / (double) PROGRESS_SEGMENTS_TOTAL;
                break;

            case DOWN:
                progressSegments = SpeedTestStatViewController.InfoStat.DOWNLOAD.getProgressForGauge(testIntermediateResult.progress);
                progressValue = (double) progressSegments / PROGRESS_SEGMENTS_PROGRESS_RING;
                correctProgressValue = progressValue;
                totalProgressValue = correctProgressValue * PROGRESS_SEGMENTS_PROGRESS_RING / (double) PROGRESS_SEGMENTS_TOTAL;
                Timber.e("SIGNAL totalProgressValue: %s", progressSegments);
                speedValueRelative = testIntermediateResult.downBitPerSecLog;
                if (graphHandler != null) {
                    graphHandler.addNewDownloadValue(speedValueRelative, progressSegments, relativeSignal);
                }
                break;

            case INIT_UP:
                progressSegments = SpeedTestStatViewController.InfoStat.INIT_UPLOAD.getProgressForGauge(0);
                progressValue = (double) progressSegments / PROGRESS_SEGMENTS_PROGRESS_RING;
                correctProgressValue = progressValue;
                totalProgressValue = correctProgressValue * PROGRESS_SEGMENTS_PROGRESS_RING / (double) PROGRESS_SEGMENTS_TOTAL;
                if (graphHandler != null) {
                    graphHandler.addNewSignalValue(progressSegments, relativeSignal);
                }
                break;

            case UP:
                progressSegments = SpeedTestStatViewController.InfoStat.UPLOAD.getProgressForGauge(testIntermediateResult.progress);
                speedValueRelative = testIntermediateResult.upBitPerSecLog;
                progressValue = (double) progressSegments / PROGRESS_SEGMENTS_PROGRESS_RING;
                correctProgressValue = progressValue;
                totalProgressValue = correctProgressValue * PROGRESS_SEGMENTS_PROGRESS_RING / (double) PROGRESS_SEGMENTS_TOTAL;
                Timber.e("SIGNAL totalProgressValue: %s", progressSegments);

                if (graphHandler != null) {
                    graphHandler.addNewUploadValue(speedValueRelative, progressSegments, relativeSignal);
                }
                break;

            case SPEEDTEST_END:
            case QOS_TEST_RUNNING:

                if ((mainFragmentInterface != null) && (mainFragmentInterface.getContext() != null)) {
                    if (ConfigHelper.isQosEnabled(mainFragmentInterface.getContext())) {
                        qosMode = true;
                        mainFragmentInterface.changeScreenState(MainScreenState.QOS_TESTING, "UI update", false);

                        progressSegments = SpeedTestStatViewController.InfoStat.QOS_TEST_RUNNING.getProgressForGauge(0);
                        speedValueRelative = testIntermediateResult.upBitPerSecLog;

                        progressValue = (double) progressSegments / PROGRESS_SEGMENTS_PROGRESS_RING;
                        correctProgressValue = progressValue;
                        totalProgressValue = correctProgressValue * PROGRESS_SEGMENTS_PROGRESS_RING / (double) PROGRESS_SEGMENTS_TOTAL;

                        mainFragmentInterface.setQosTestProgress(0, true);

                        if (updateUITask != null) {
                            updateUITask.setQoSModeEnabled(true);
                        }
                        qosMode = true;
                    }
                }

                break;

            case ERROR:
            case ABORTED:
                if (testIntermediateResult.status == ERROR) // && !ConfigHelper.isRepeatTest(getActivity()))
                {
                    if (!testTestService.isLoopModeRunning())
                        this.showErrorDialog(mainFragmentInterface.getTestErrorStringId(), mainFragmentInterface.getTestErrorTitleId());
                    unbindTestingService();
                    testTestService = null;
                    return;
                }

            default:
                break;
        }


        if (mainFragmentInterface != null) {
            mainFragmentInterface.setSpeedGaugeProgress((int) (speedValueRelative * PROGRESS_SEGMENTS_PROGRESS_RING));
            mainFragmentInterface.setSignalValue(signal);
        }


        Timber.e("PROGRESS SEGMENTS %s", progressSegments);
        Timber.e("SpeedValueRelative %s", speedValueRelative);


        if (lastProgressSegments < progressSegments) {
            lastProgressSegments = progressSegments;
            mainFragmentInterface.setTestProgress(progressSegments);
            Timber.e("TOTAL PROGRESS %s %s", totalProgressValue , correctProgressValue);
            mainFragmentInterface.setTestTextProgress(PERCENT_FORMAT.format(totalProgressValue));
        }

        final String initStr;

        if (testIntermediateResult.initNano < 0) {
            initStr = "-";
        } else {
            initStr = SpeedTestStatViewController.InfoStat.INIT.format(testIntermediateResult.initNano);
        }

        if (getSpeedTestStatus() != null) {
            getSpeedTestStatus().setResultInitString(initStr,
                    testIntermediateResult.status.equals(INIT) ?
                            SpeedTestStatViewController.FLAG_SHOW_PROGRESSBAR : SpeedTestStatViewController.FLAG_HIDE_PROGRESSBAR);
        }

        final String pingStr;
        if (testIntermediateResult.pingNano < 0) {
            pingStr = testIntermediateResult.status.equals(PING) ? "" : "-";
        } else {
            pingStr = SpeedTestStatViewController.InfoStat.PING.format(testIntermediateResult.pingNano);
        }

        if (getSpeedTestStatus() != null) {
            getSpeedTestStatus().setResultPingString(pingStr,
                    testIntermediateResult.status.equals(PING) ?
                            SpeedTestStatViewController.FLAG_SHOW_PROGRESSBAR : SpeedTestStatViewController.FLAG_HIDE_PROGRESSBAR);
            mainFragmentInterface.setPingText(pingStr);
        }

        final String downStr;
        if (testIntermediateResult.downBitPerSec < 0) {
            downStr = "-";
        } else {
            downStr = SpeedTestStatViewController.InfoStat.DOWNLOAD.format(testIntermediateResult.downBitPerSec);
        }

        if (getSpeedTestStatus() != null) {
            getSpeedTestStatus().setResultDownString(downStr,
                    testIntermediateResult.status.equals(DOWN) ?
                            SpeedTestStatViewController.FLAG_SHOW_PROGRESSBAR : SpeedTestStatViewController.FLAG_HIDE_PROGRESSBAR);
            if (graphInterface != null) {
                graphInterface.updateDownloadGraph(downStr.split(" ")[0], graphInterface.getDownloadStatusStringId(), graphInterface.getUnitStringId());
            }
        }

        final String upStr;
        if (testIntermediateResult.upBitPerSec < 0) {
            upStr = "-";
        } else {
            upStr = SpeedTestStatViewController.InfoStat.UPLOAD.format(testIntermediateResult.upBitPerSec);
        }

        if (getSpeedTestStatus() != null) {
            getSpeedTestStatus().setResultUpString(upStr,
                    testIntermediateResult.status.equals(UP) || testIntermediateResult.status.equals(INIT_UP) ?
                            SpeedTestStatViewController.FLAG_SHOW_PROGRESSBAR : SpeedTestStatViewController.FLAG_HIDE_PROGRESSBAR);
            if (graphInterface != null) {
                graphInterface.updateUploadGraph(upStr.split(" ")[0], graphInterface.getUploadStatusStringId(), graphInterface.getUnitStringId());
            }
        }

        final String jitterStr;
        if (testIntermediateResult.jitter < 0) {
            jitterStr = "-";
        } else {
            jitterStr = SpeedTestStatViewController.InfoStat.JITTER.format(testIntermediateResult.jitter);
        }

        Timber.d("TUUI: prepare to set STS: " + getSpeedTestStatus() + "    TIR: " + testIntermediateResult);
        if ((getSpeedTestStatus() != null) && (testIntermediateResult != null)) {
            if (testIntermediateResult.status.ordinal() > PACKET_LOSS_AND_JITTER.ordinal()) {
                Timber.d("TUUI: prepare to set jitter: " + jitterStr);
                mainFragmentInterface.setJitterText(jitterStr);
            } else {
                mainFragmentInterface.setJitterText("-");
            }
            getSpeedTestStatus().setResultJitterString(jitterStr,
                    testIntermediateResult.status.equals(TestStatus.PACKET_LOSS_AND_JITTER) ?
                            SpeedTestStatViewController.FLAG_SHOW_PROGRESSBAR : SpeedTestStatViewController.FLAG_HIDE_PROGRESSBAR);
        }

        final String packetLossUpStr;
        if (testIntermediateResult.packetLossUp < 0) {
            packetLossUpStr = "-";
        } else {
            packetLossUpStr = SpeedTestStatViewController.InfoStat.PACKET_LOSS_UP.format(testIntermediateResult.packetLossUp);
        }

        if ((getSpeedTestStatus() != null) && (testIntermediateResult != null)) {
            getSpeedTestStatus().setResultPacketLossInString(packetLossUpStr,
                    testIntermediateResult.status.equals(TestStatus.PACKET_LOSS_AND_JITTER) ?
                            SpeedTestStatViewController.FLAG_SHOW_PROGRESSBAR : SpeedTestStatViewController.FLAG_HIDE_PROGRESSBAR);
        }

        final String packetLossDownStr;
        if (testIntermediateResult.packetLossDown < 0) {
            packetLossDownStr = "-";
        } else {
            packetLossDownStr = SpeedTestStatViewController.InfoStat.PACKET_LOSS_DOWN.format(testIntermediateResult.packetLossDown);
        }

        if ((testIntermediateResult.packetLossDown >= 0) && (testIntermediateResult.packetLossUp >= 0)) {
            long meanPacketLoss = (testIntermediateResult.packetLossDown + testIntermediateResult.packetLossUp) / 2;
            String packetLossStr = SpeedTestStatViewController.InfoStat.PACKET_LOSS_DOWN.format(meanPacketLoss);
            if (testIntermediateResult.status.ordinal() > PACKET_LOSS_AND_JITTER.ordinal()) {
                mainFragmentInterface.setPacketLossText(packetLossStr);
            } else {
                mainFragmentInterface.setPacketLossText("-");
            }
        } else {
            mainFragmentInterface.setPacketLossText("-");
        }

        if ((getSpeedTestStatus() != null) && (testIntermediateResult != null)) {
            getSpeedTestStatus().setResultPacketLossOutString(packetLossDownStr,
                    testIntermediateResult.status.equals(TestStatus.PACKET_LOSS_AND_JITTER) ?
                            SpeedTestStatViewController.FLAG_SHOW_PROGRESSBAR : SpeedTestStatViewController.FLAG_HIDE_PROGRESSBAR);
        }
    }

    public void updateQoSUI() {

        if (testProgressDialog != null) {
            testProgressDialog.dismiss();
            testProgressDialog = null;
        }

        QoSTestEnum status = null;
        float progress = 0f;

        try {
            if (testTestService != null) {

                QoSTestEnum _status = testTestService.getQoSTestStatus();
                if (_status == null) {
//                    _status = lastQoSTestStatus == null ? QoSTestEnum.START : lastQoSTestStatus;
                    _status = QoSTestEnum.START;
                }
                status = _status;
                lastQoSTestStatus = status;
                progress = testTestService.getQoSTestProgress();

            } else {
                QoSTestEnum _status = lastQoSTestStatus;
                if (_status == null)
                    _status = QoSTestEnum.START;
                if (_status == QoSTestEnum.QOS_RUNNING)
                    _status = QoSTestEnum.STOP;

                status = _status;
            }

//            Timber.d(" DEBUG TEST", String.format("status: %s", status == null ? "null" : status.toString()));

            switch (status) {
                case START:
                case ERROR:
                    progress = 0f;
                    break;

                case STOP:
                    progress = 1f;
                    break;

                default:
                    break;
            }

            double progressSegments = 0;

            switch (status) {
                case START:
                    if (testTestService != null) {
                        progressSegments = Math.round(PROGRESS_SEGMENTS_QOS * progress / testTestService.getQoSTestSize());
                    }
                    break;

                case QOS_RUNNING:
                    progressSegments = Math.round(PROGRESS_SEGMENTS_QOS * progress / testTestService.getQoSTestSize());
                    break;

                case QOS_FINISHED:
                case NDT_RUNNING:
//                    running = true;
                    progressSegments = PROGRESS_SEGMENTS_QOS - 1;
                    break;

                case STOP:
//                    if (running == true) {
                    progressSegments = PROGRESS_SEGMENTS_QOS;
//                    }
                    break;

                case ERROR:
                default:
                    break;
            }

            setSignalValue();

            final double progressValue = progressSegments / PROGRESS_SEGMENTS_QOS;
            mainFragmentInterface.setQosTestProgress((int) progressValue, true);

            mainFragmentInterface.changeScreenState(MainScreenState.QOS_TESTING, "QOS update UI", false);

            Timber.e("PROGRESS SEGMENT 2 %s status: %s", progressSegments, status);
            final double totalProgressValue = (PROGRESS_SEGMENTS_PROGRESS_RING + progressSegments) / (double) PROGRESS_SEGMENTS_TOTAL;
            mainFragmentInterface.setTestTextProgress(PERCENT_FORMAT.format(totalProgressValue));
            mainFragmentInterface.setQosTestProgress((int) (progressSegments), true);

            mainFragmentInterface.showQoSProgress(testTestService);


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (status != null && status == QoSTestEnum.ERROR)
                testShowQoSErrorToast = true;
        }
    }

    @Override
    public void showResultDelayed() {
        if (testResultSwitcherRunnable == null) {
            testResultSwitcherRunnable = new ResultSwitcher(this);
        }
        testHandler.postDelayed(testResultSwitcherRunnable, 300);
    }


    public void showResult() {

        if (testError) {
            return;
        }

        Context context1 = getContext();
        if (mainFragmentInterface != null) {
            MainActivity activity = mainFragmentInterface.getMainActivity();
            if ((activity != null) && (context1 != null)) {
                ConfigHelper.setHistoryIsDirty(activity, true);
                activity.checkSettings(true, null);

                if (testShowQoSErrorToast) {
                    final Toast toast = Toast.makeText(activity, mainFragmentInterface.getTestErrorQosStringId(), Toast.LENGTH_LONG);
                    toast.show();
                }

                this.dismissDialogs();

                String testUuid = TestConfig.getCurrentlyPerformingTestUUID();
                if (testUuid != null) {
                    mainFragmentInterface.setTestUUID(testUuid);
                }
                if (testUuid == null) {
                    this.showErrorDialog(mainFragmentInterface.getTestErrorStringId(), mainFragmentInterface.getTestErrorTitleId());
                    context1.stopService(new Intent(context1, TestService.class));
                    return;
                }

                // when user stop test by dialog then result should not be shown
                if (!isTestStoppedByUser) {
                    if (mainFragmentInterface != null) {
                        if (qosMode) {
//                            if (context1 != null) {
//                                TestConfig.setShouldShowResults(context1.getApplicationContext(), true);
//                            }
                            mainFragmentInterface.changeScreenState(MainScreenState.QOS_TEST_RESULT, "ShowResult - testStoppedByUser 1", true);
                            testTestService.stopTest();
                            context1.stopService(new Intent(context1, TestService.class));
                        } else {
//                            if (context1 != null) {
//                                TestConfig.setShouldShowResults(context1.getApplicationContext(), true);
//                            }
                            mainFragmentInterface.changeScreenState(MainScreenState.TEST_RESULT, "ShowResult - testStoppedByUser 2", true);
                            testTestService.stopTest();
                            context1.stopService(new Intent(context1, TestService.class));
                        }
                    }
                    if (testTestService != null) {
                        testTestService.stopTest();
                        final Intent service = new Intent(TestService.ACTION_ABORT_TEST, null, context1, TestService.class);
                            context1.startService(service);
                        testTestService = null;
                    }
                } else {
                    isTestStoppedByUser = false;
                    context1.stopService(new Intent(context1, TestService.class));
                }
            }
        } else {
            testTestService.stopTest();
            Context context = context1;
            if (context != null) {
                context.stopService(new Intent(context, TestService.class));
            }
        }
    }

    private synchronized void setTestResultQoSDetails(QoSServerResultCollection qosResults) {

        Timber.e("QOS_RESULTS %s", qosResults);
        int testCounter = 0;
        int failedTestsCounter = 0;
        int percentage = 0;
        if (qosResults != null) {
            QoSServerResultCollection.QoSResultStats qoSStatistics = qosResults.getQoSStatistics();
            testCounter = qoSStatistics.getTestCounter();
            failedTestsCounter = qoSStatistics.getFailedTestsCounter();
            percentage = qoSStatistics.getPercentageForTests();
        }
        mainFragmentInterface.showQosResults(testCounter, failedTestsCounter, percentage);
    }

    private Double getRelativeSignal() {
        Double relativeSignal = null;
        NetworkUtil.MinMax<Integer> signalBounds = NetworkUtil.getSignalStrengthBounds(signalType);
        if (!(signalBounds.min == Integer.MIN_VALUE || signalBounds.max == Integer.MAX_VALUE)) {
            if (signal != null) {
                relativeSignal = (double) (signal - signalBounds.min) / (double) (signalBounds.max - signalBounds.min);
            }
        }
        return relativeSignal;
    }

    private void setSignalValue() {
        signal = testTestService.getSignal();
        signalType = testTestService.getSignalType();

        if (signal == null || signal == 0) {
            signalType = SIGNAL_TYPE_NO_SIGNAL;
        }

        boolean signalTypeChanged = false;

        if (signalType != SIGNAL_TYPE_NO_SIGNAL) {
            lastSignal = signal;
            signalTypeChanged = testLastSignalType != signalType;
            testLastSignalType = signalType;
        }
        if (signalType == SIGNAL_TYPE_NO_SIGNAL && testLastSignalType != SIGNAL_TYPE_NO_SIGNAL) {
            // keep old signal if we had one before
            signal = lastSignal;
        }

        Double relativeSignal = getRelativeSignal();
        NetworkUtil.MinMax<Integer> signalBounds = NetworkUtil.getSignalStrengthBounds(signalType);

        if (signalTypeChanged) {
            if (graphHandler != null) {
                graphHandler.signalTypeChanged(relativeSignal, signalBounds);
            }
        }

        mainFragmentInterface.setSignalValue(signal);
    }

    @Override
    public Context getContext() {
        if (mainFragmentInterface != null) {
            return mainFragmentInterface.getContext();
        } else {
            return null;
        }

    }

    @Override
    public boolean isTestVisible() {
        return mainFragmentInterface != null && mainFragmentInterface.isTestVisible();
    }


    void initializeTesting(GraphHandler graphHandler) {
        qosMode = false;
        testError = false;
        stopLoop = false;
        if (testHandler == null) {
            testHandler = new Handler();
        }
        testLastShownWaitTime = -1;
        lastProgressSegments = -1;
        isTestStoppedByUser = false;
        bindTestingService();
        Context context = getContext();
        if (context != null) {
            if (!ConfigHelper.isQosEnabled(context)) {
                PROGRESS_SEGMENTS_TOTAL = PROGRESS_SEGMENTS_PROGRESS_RING;
            } else {
                PROGRESS_SEGMENTS_TOTAL = PROGRESS_SEGMENTS_PROGRESS_RING + PROGRESS_SEGMENTS_QOS;
            }
            this.graphHandler = graphHandler;
            if (graphHandler != null) {
                this.graphHandler.initializeGraphs(context);
            }

            testUpdateCounter = 0;
            Timber.d("TUUI: STS set");
            testSpeedTestStatViewController = new SpeedTestStatViewController(mainFragmentInterface.getMainActivity());


            final String progressTitle = mainFragmentInterface.getContext().getString(mainFragmentInterface.getProgressTitleId());
            final String progressText = mainFragmentInterface.getContext().getString(mainFragmentInterface.getProgressTextId());

            if (testProgressDialog == null) {
                testProgressDialog = ProgressDialog.show(context, progressTitle, progressText, true, false);
                testProgressDialog.setOnKeyListener(backKeyListener);
            }
        }

    }

    private ChangeableSpeedTestStatus getSpeedTestStatus() {
        return testSpeedTestStatViewController;
    }

    void onDestroy() {
        dismissDialogs();
        if (graphHandler != null) {
            graphHandler.releaseGraphs();
        }
    }

    void initializeQoSResults(String uid) {
        MainActivity mainActivity = mainFragmentInterface.getMainActivity();
        if (mainActivity != null) {
            CheckTestResultDetailTask testResultDetailTask = new CheckTestResultDetailTask(mainActivity, ResultDetailType.QUALITY_OF_SERVICE_TEST);

            testResultDetailTask.setEndTaskListener(new EndTaskListener() {
                @Override
                public void taskEnded(JsonArray result) {
                    try {
                        setTestResultQoSDetails(new QoSServerResultCollection(result));
                    } catch (JsonParseException e) {
                        //e.printStackTrace();
                    }
                }
            });

            testResultDetailTask.execute(uid);
        }
    }

    public void setInformationCollector(InformationCollector informationCollector) {
        this.informationCollector = informationCollector;
    }
}
