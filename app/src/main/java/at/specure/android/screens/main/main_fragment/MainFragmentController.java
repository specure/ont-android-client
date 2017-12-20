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
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonParseException;
import com.specure.opennettest.R;

import java.text.MessageFormat;
import java.util.Locale;

import at.specure.android.api.calls.CheckTestResultDetailTask;
import at.specure.android.configs.ConfigHelper;
import at.specure.android.screens.main.MainActivity;
import at.specure.android.screens.main.main_fragment.graphs_handlers.GraphHandler;
import at.specure.android.test.ChangeableSpeedTestStatus;
import at.specure.android.test.SpeedTestStatViewController;
import at.specure.android.test.TestService;
import at.specure.android.test.UIUpdateInterface;
import at.specure.android.test.runnables.ResultSwitcher;
import at.specure.android.test.runnables.UITestUpdater;
import at.specure.android.util.EndTaskListener;
import at.specure.android.util.Helperfunctions;
import at.specure.android.util.net.NetworkUtil;
import at.specure.client.helper.IntermediateResult;
import at.specure.client.helper.NdtStatus;
import at.specure.client.helper.TestStatus;
import at.specure.client.v2.task.QoSTestEnum;
import at.specure.client.v2.task.result.QoSServerResultCollection;

import static at.specure.android.screens.main.main_fragment.MainMenuFragment.MAX_COUNTER_WITHOUT_RESULT;
import static at.specure.android.screens.main.main_fragment.MainMenuFragment.PERCENT_FORMAT;
import static at.specure.android.screens.main.main_fragment.MainMenuFragment.PROGRESS_SEGMENTS_PROGRESS_RING;
import static at.specure.android.screens.main.main_fragment.MainMenuFragment.PROGRESS_SEGMENTS_QOS;
import static at.specure.android.screens.main.main_fragment.MainMenuFragment.PROGRESS_SEGMENTS_TOTAL;
import static at.specure.android.screens.result.ResultDetailType.QUALITY_OF_SERVICE_TEST;
import static at.specure.android.util.InformationCollector.SINGAL_TYPE_NO_SIGNAL;
import static at.specure.client.helper.TestStatus.DOWN;
import static at.specure.client.helper.TestStatus.ERROR;
import static at.specure.client.helper.TestStatus.INIT;
import static at.specure.client.helper.TestStatus.INIT_UP;
import static at.specure.client.helper.TestStatus.PING;
import static at.specure.client.helper.TestStatus.UP;
import static at.specure.client.helper.TestStatus.WAIT;

/**This is main controller for the main fragment
 * Created by michal.cadrik on 10/23/2017.
 */

public class MainFragmentController implements ServiceConnection, UIUpdateInterface {

    private static final String TAG = "MainFragContr";
    private final String waitText;
    MainFragmentInterface mainFragmentInterface;
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
            if (keyCode == KeyEvent.KEYCODE_BACK)
                return onMainBackPressed();
            return false;
        }
    };

    public MainFragmentController(MainFragmentInterface mainFragmentInterface) {
        this.mainFragmentInterface = mainFragmentInterface;
        waitText = mainFragmentInterface.getContext().getResources().getString(R.string.test_progress_text_wait);
    }

    public boolean onMainBackPressed() {
        Log.d("RMBTTestFragment", "onbackpressed");
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
                builder.setTitle(R.string.test_dialog_abort_title);
                builder.setMessage(R.string.test_dialog_abort_text);
                builder.setPositiveButton(R.string.test_dialog_abort_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        if (testTestService != null)
                            isTestStoppedByUser = true;
                        testTestService.stopTest();
                        final Intent service = new Intent(TestService.ACTION_ABORT_TEST, null, activity, TestService.class);
                        activity.startService(service);
                        testTestService = null;
                        unbindTestingService();
                        mainFragmentInterface.changeScreenState(MainScreenState.DEFAULT, "TestAbortDialogPositive", true);
                    }
                });
                builder.setNegativeButton(R.string.test_dialog_abort_no, null);

                dismissDialogs();
                testAbortDialog = builder.show();
            }
            return true;
        }
        return false;
    }


    public void dismissDialogs() {
        dismissDialog(testErrorDialog);
        testErrorDialog = null;
        dismissDialog(testAbortDialog);
        testAbortDialog = null;
        dismissDialog(testProgressDialog);
        testProgressDialog = null;
    }

    public static void dismissDialog(Dialog dialog) {
        if (dialog != null)
            dialog.dismiss();
    }

    public void showErrorDialog(int errorMessageId) {
        if (updateUITask != null) {
            updateUITask.setStopLoop(true);
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(mainFragmentInterface.getMainActivity());
        builder.setTitle(R.string.test_dialog_error_title);
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

    public void bindTestingService() {
        MainActivity activity = mainFragmentInterface.getMainActivity();
        if (activity != null) {
            activity.setToolbarVisible(View.INVISIBLE);
            activity.setLockNavigationDrawer(true);

            // Bind to TestService
            final Intent serviceIntent = new Intent(activity, TestService.class);
            activity.bindService(serviceIntent, this, Context.BIND_AUTO_CREATE);

            // start UI updates according to a test process
            updateUITask = new UITestUpdater(qosMode, stopLoop, this, testTestService, testHandler);
            testHandler.post(updateUITask);

            //getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    public void unbindTestingService() {
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
            Log.e("TEST SERVICE", "unbinding not registered service");
        }

        if (activity != null) {
            activity.setToolbarVisible(View.VISIBLE);
            activity.setLockNavigationDrawer(false);
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        testSpeedTestStatViewController = null;
    }

    TestService getTestTestService() {
        return this.testTestService;
    }


    @Override
    public void onServiceConnected(final ComponentName name, final IBinder service) {
        Log.d(TAG, "service connected");
        final TestService.RMBTBinder binder = (TestService.RMBTBinder) service;
        this.testTestService = binder.getService();
        if (updateUITask != null) {
            updateUITask.setRMBTService(this.testTestService);
        }
    }

    @Override
    public void onServiceDisconnected(final ComponentName name) {
        Log.d(TAG, "service disconnected");
        this.testTestService = null;
        if (updateUITask != null) {
            updateUITask.setRMBTService(null);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void updateTestUI() {
        String teststatus;
        testUpdateCounter++;

        if (testTestService == null)
            return;

        testIntermediateResult = testTestService.getIntermediateResult(testIntermediateResult);

        if (testIntermediateResult == null) {
            if (testTestService.isConnectionError()) {
                if (testProgressDialog != null) {
                    testProgressDialog.dismiss();
                    testProgressDialog = null;
                }
                if (!testTestService.isLoopMode()) {
                    this.showErrorDialog(R.string.test_dialog_error_control_server_conn);
                    return;
                }
            }

            if (!testTestService.isTestRunning() && testUpdateCounter > MAX_COUNTER_WITHOUT_RESULT)
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
                    testProgressDialog.setMessage(MessageFormat.format(waitText, wait));
                }
            }
            return;
        }

        if (testProgressDialog != null) {
            testProgressDialog.dismiss();
            testProgressDialog = null;
        }

        boolean forceUpdate = false;

        if (testTestService.getNdtStatus() == NdtStatus.RUNNING) {
            final String ndtStatus = String.format(Locale.US, "NDT (%d%%)", Math.round(testTestService.getNDTProgress() * 100));
            if (testLastStatusString == null || !ndtStatus.equals(testLastStatusString)) {
                forceUpdate = true;
                testLastStatusString = ndtStatus;
            }
        } else if (testLastStatus != testIntermediateResult.status) {
            testLastStatus = testIntermediateResult.status;
            testLastStatusString = Helperfunctions.getTestStatusString(getContext().getResources(), testIntermediateResult.status);
            forceUpdate = true;
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
                Log.e("SIGNAL", "totalProgressValue: " + progressSegments);
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
                Log.e("SIGNAL", "totalProgressValue: " + progressSegments);

                if (graphHandler != null) {
                    graphHandler.addNewUploadValue(speedValueRelative, progressSegments, relativeSignal);
                }
                break;

            case SPEEDTEST_END:
            case QOS_TEST_RUNNING:
                if (mainFragmentInterface != null) {
                    if (ConfigHelper.isQosEnabled(mainFragmentInterface.getContext())) {
                        qosMode = true;
                        mainFragmentInterface.changeScreenState(MainScreenState.QOS_TESTING, "UI update", false);

                        progressSegments = SpeedTestStatViewController.InfoStat.QOS_TEST_RUNNING.getProgressForGauge(0);
                        speedValueRelative = testIntermediateResult.upBitPerSecLog;

                        progressValue = (double) progressSegments / PROGRESS_SEGMENTS_PROGRESS_RING;
                        correctProgressValue = progressValue;
                        totalProgressValue = correctProgressValue * PROGRESS_SEGMENTS_PROGRESS_RING / (double) PROGRESS_SEGMENTS_TOTAL;

                        mainFragmentInterface.setQosTestProgress(0, true);

                        updateUITask.setQoSModeEnabled(true);
                        qosMode = true;
                    }
                }

                break;

            case ERROR:
            case ABORTED:
                if (testIntermediateResult.status == ERROR) // && !ConfigHelper.isRepeatTest(getActivity()))
                {
                    if (!testTestService.isLoopMode())
                        this.showErrorDialog(R.string.test_dialog_error_text);
                    return;
                }

            default:
                break;
        }


        mainFragmentInterface.setSpeedGaugeProgress((int) (speedValueRelative * PROGRESS_SEGMENTS_PROGRESS_RING));
        mainFragmentInterface.setSignalValue(signal);

        Log.e("PROGRESS SEGMENTS", progressSegments + "");
        Log.e("SpeedValueRelative", speedValueRelative + "");


        if (lastProgressSegments < progressSegments) {
            lastProgressSegments = progressSegments;
            mainFragmentInterface.setTestProgress(progressSegments);
            Log.e("TOTAL PROGRESS", totalProgressValue + "" + correctProgressValue);
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
            mainFragmentInterface.updateDownloadGraph(downStr.split(" ")[0], R.string.test_bottom_test_status_down, R.string.test_mbps);


//            if (testIntermediateResult.status == DOWN) {
//                mainFragmentInterface.setTestTextProgress(SpeedTestStatViewController.InfoStat.DOWNLOAD.format(testIntermediateResult.downBitPerSec));
//            }

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
            mainFragmentInterface.updateUploadGraph(upStr.split(" ")[0], R.string.test_bottom_test_status_up, R.string.test_mbps);


//            if ((testIntermediateResult.status == UP)) {// || (intermediateResult.status == TestStatus.INIT_UP))  {
//                mainFragmentInterface.setTestTextProgress(SpeedTestStatViewController.InfoStat.DOWNLOAD.format(testIntermediateResult.upBitPerSec));
//            }
        }

        final String jitterStr;
        if (testIntermediateResult.jitter < 0) {
            jitterStr = "-";
        } else {
            jitterStr = SpeedTestStatViewController.InfoStat.JITTER.format(testIntermediateResult.jitter);
        }

        if ((getSpeedTestStatus() != null) && (testIntermediateResult != null)) {
            mainFragmentInterface.setJitterText(jitterStr);
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
            mainFragmentInterface.setPacketLossText(packetLossStr);
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
                    _status = lastQoSTestStatus == null ? QoSTestEnum.START : lastQoSTestStatus;
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

//            Log.d(" DEBUG TEST", String.format("status: %s", status == null ? "null" : status.toString()));

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
                    progressSegments = Math.round(PROGRESS_SEGMENTS_QOS * progress / testTestService.getQoSTestSize());
                    break;

                case QOS_RUNNING:
                    progressSegments = Math.round(PROGRESS_SEGMENTS_QOS * progress / testTestService.getQoSTestSize());
                    break;

                case QOS_FINISHED:
                case NDT_RUNNING:
                    progressSegments = PROGRESS_SEGMENTS_QOS - 1;
                    break;

                case STOP:
                    progressSegments = PROGRESS_SEGMENTS_QOS;
                    break;

                case ERROR:
                default:
                    break;
            }

            setSignalValue();

            final double progressValue = progressSegments / PROGRESS_SEGMENTS_QOS;
            mainFragmentInterface.setQosTestProgress((int) progressValue, true);

            mainFragmentInterface.changeScreenState(MainScreenState.QOS_TESTING, "QOS update UI", false);


            Log.e("PROGRESS SEGMENT 2", progressSegments + "");
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


        if (mainFragmentInterface != null) {
            MainActivity activity = mainFragmentInterface.getMainActivity();
            activity.setHistoryDirty(true);
            activity.checkSettings(true, null);

            if (activity == null) {
                return;
            }

            if (testShowQoSErrorToast) {
                final Toast toast = Toast.makeText(activity, R.string.test_toast_error_text_qos, Toast.LENGTH_LONG);
                toast.show();
            }

            this.dismissDialogs();

            String testUuid = testTestService == null ? null : testTestService.getTestUuid();
            mainFragmentInterface.setTestUUID(testUuid);
            if (testUuid == null) {
                this.showErrorDialog(R.string.test_dialog_error_text);
                getContext().stopService(new Intent(getContext(), TestService.class));
                return;
            }

            // when user stop test by dialog then result should not be shown
            if (!isTestStoppedByUser) {
                if (mainFragmentInterface != null) {
                    if (qosMode) {
                        mainFragmentInterface.changeScreenState(MainScreenState.QOS_TEST_RESULT, "ShowResult - testStoppedByUser", true);
                        testTestService.stopTest();
                        getContext().stopService(new Intent(getContext(), TestService.class));
                    } else {
                        mainFragmentInterface.changeScreenState(MainScreenState.TEST_RESULT, "ShowResult - testStoppedByUser", true);
                        testTestService.stopTest();
                        getContext().stopService(new Intent(getContext(), TestService.class));
                    }
                }
                if (testTestService != null) {
                    testTestService.stopTest();
                    final Intent service = new Intent(TestService.ACTION_ABORT_TEST, null, getContext(), TestService.class);
                    getContext().startService(service);
                    testTestService = null;
                }
            } else {
                isTestStoppedByUser = false;
                getContext().stopService(new Intent(getContext(), TestService.class));
            }
        } else {
            testTestService.stopTest();
            getContext().stopService(new Intent(getContext(), TestService.class));
        }

    }

    public synchronized void setTestResultQoSDetails(QoSServerResultCollection qosResults) {

        Log.e("QOS_RESULTS", qosResults + "");

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

    public Double getRelativeSignal() {
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
            signalType = SINGAL_TYPE_NO_SIGNAL;
        }

        boolean signalTypeChanged = false;

        if (signalType != SINGAL_TYPE_NO_SIGNAL) {
            lastSignal = signal;
            signalTypeChanged = testLastSignalType != signalType;
            testLastSignalType = signalType;
        }
        if (signalType == SINGAL_TYPE_NO_SIGNAL && testLastSignalType != SINGAL_TYPE_NO_SIGNAL) {
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
        if (mainFragmentInterface != null) {
            return mainFragmentInterface.isTestVisible();
        } else {
            return false;
        }
    }



    public void initializeTesting(GraphHandler graphHandler) {
        qosMode = false;
        testError = false;
        stopLoop = false;
        testHandler = new Handler();
        testLastShownWaitTime = -1;
        lastProgressSegments = -1;
        isTestStoppedByUser = false;
        bindTestingService();
        if (!ConfigHelper.isQosEnabled(getContext())) {
            PROGRESS_SEGMENTS_TOTAL = PROGRESS_SEGMENTS_PROGRESS_RING;
        } else {
            PROGRESS_SEGMENTS_TOTAL = PROGRESS_SEGMENTS_PROGRESS_RING + PROGRESS_SEGMENTS_QOS;
        }
        this.graphHandler = graphHandler;
        if (graphHandler != null) {
            this.graphHandler.initializeGraphs(getContext());
        }

        testUpdateCounter = 0;
        testSpeedTestStatViewController = new SpeedTestStatViewController(mainFragmentInterface.getMainActivity());


        final String progressTitle = mainFragmentInterface.getContext().getString(R.string.test_progress_title);
        final String progressText = mainFragmentInterface.getContext().getString(R.string.test_progress_text);

        if (testProgressDialog == null) {
            testProgressDialog = ProgressDialog.show(mainFragmentInterface.getMainActivity(), progressTitle, progressText, true, false);
            testProgressDialog.setOnKeyListener(backKeyListener);
        }

    }

    public ChangeableSpeedTestStatus getSpeedTestStatus() {
        return testSpeedTestStatViewController;
    }

    public void onDestroy() {
        dismissDialogs();
        if (graphHandler != null) {
            graphHandler.releaseGraphs();
        }
    }

    public void initializeQoSResults(String uid) {
        MainActivity mainActivity = mainFragmentInterface.getMainActivity();
        if (mainActivity != null) {
            CheckTestResultDetailTask testResultDetailTask = new CheckTestResultDetailTask(mainActivity, QUALITY_OF_SERVICE_TEST);

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
}
