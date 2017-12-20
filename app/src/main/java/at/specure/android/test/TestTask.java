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

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.specure.opennettest.R;

import at.specure.android.impl.CpuStatAndroidImpl;
import at.specure.android.impl.MemInfoAndroidImpl;
import at.specure.android.impl.TracerouteAndroidImpl;
import at.specure.android.impl.TrafficServiceImpl;
import at.specure.android.impl.WebsiteTestServiceImpl;
import at.specure.android.configs.Config;
import at.specure.android.configs.ConfigHelper;
import at.specure.android.util.InformationCollector;
import at.specure.client.QualityOfServiceTest;
import at.specure.client.QualityOfServiceTest.Counter;
import at.specure.client.TestClient;
import at.specure.client.TestResult;
import at.specure.client.helper.ControlServerConnection;
import at.specure.client.helper.IntermediateResult;
import at.specure.client.helper.NdtStatus;
import at.specure.client.helper.TestStatus;
import at.specure.client.ndt.NDTRunner;
import at.specure.client.tools.impl.CpuStatCollector;
import at.specure.client.tools.impl.MemInfoCollector;
import at.specure.client.v2.task.QoSTestEnum;
import at.specure.client.v2.task.result.QoSResultCollector;
import at.specure.client.v2.task.result.QoSTestResultEnum;
import at.specure.client.v2.task.service.TestSettings;
import at.specure.android.util.net.ZeroMeasurementDetector;
import at.specure.android.configs.LoopModeConfig;
import at.specure.net.measurementlab.ndt.NdtTests;
import at.specure.util.tools.InformationCollectorTool;

public class TestTask {
    private static final String LOG_TAG = "TestTask";

    private final AtomicBoolean started = new AtomicBoolean();
    private final AtomicBoolean running = new AtomicBoolean();
    private final AtomicBoolean finished = new AtomicBoolean();
    private final AtomicBoolean cancelled = new AtomicBoolean();

    private final AtomicReference<QualityOfServiceTest> qosReference = new AtomicReference<QualityOfServiceTest>();

    private Handler handler;
    private final Runnable postExecuteHandler = new Runnable() {
        @Override
        public void run() {
            if (fullInfo != null) {
                fullInfo.unload();
                fullInfo = null;
            }
            if (endTaskListener != null)
                endTaskListener.taskEnded();
        }
    };
    ;

    final private Context context;

    private final ExecutorService executor = Executors.newCachedThreadPool();

    private final AtomicBoolean connectionError = new AtomicBoolean();
    private TestClient client;

    private InformationCollector fullInfo;

    private EndTaskListener endTaskListener;

    interface EndTaskListener {
        public void taskEnded();
    }

    public TestTask(final Context ctx) {
        this.context = ctx;
    }

    public void execute(final Handler _handler) {
        fullInfo = new InformationCollector(context, true, true);
        cancelled.set(false);
        started.set(true);
        running.set(true);
        finished.set(false);

        handler = _handler;
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Log.d(LOG_TAG, "executor task started");
                doInBackground();
                Log.d(LOG_TAG, "doInBackground finished");
                running.set(false);
                finished.set(true);
                if (handler != null)
                    handler.post(postExecuteHandler);
                Log.d(LOG_TAG, "executor task finished");
            }
        });
    }

    public void cancel() {
        setPreviousTestStatus();
        cancelled.set(true);
        executor.shutdownNow();
        Log.d(LOG_TAG, "shutdownNow called TestTask=" + this);
//        try
//        {
//            executor.awaitTermination(10, TimeUnit.SECONDS);
//        }
//        catch (InterruptedException e)
//        {
//            Thread.currentThread().interrupt();
//        }
    }

    public boolean isFinished() {
        return finished.get();
    }

    public boolean isRunning() {
        return running.get() && !cancelled.get();
    }

    private void setPreviousTestStatus() {
        final TestStatus status;
        if (client == null)
            status = null;
        else
            status = client.getStatus();

        final String statusString;
        if (status == TestStatus.ERROR) {
            final TestStatus statusBeforeError = client.getStatusBeforeError();
            if (statusBeforeError != null)
                statusString = "ERROR_" + statusBeforeError.toString();
            else
                statusString = "ERROR";
        } else if (status != null)
            statusString = status.toString();
        else
            statusString = null;

        System.out.println("test status at end: " + statusString);
        ConfigHelper.setPreviousTestStatus(context, statusString);
    }

    private void doInBackground() {
        try {
            boolean error = false;
            connectionError.set(false);
            TestResult result = null;
            QoSResultCollector qosResult = null;
            QoSResultCollector voipResult = null;

            // check for zero measurement
            boolean isZeroMeasurement = ZeroMeasurementDetector.detectZeroMeasurement(null, context, fullInfo);
            if (isZeroMeasurement) {
                this.cancel();
                return;
            }

            try {
                final String uuid = fullInfo.getUUID();

                final String controlServer = ConfigHelper.getControlServerName(context);
                final int controlPort = ConfigHelper.getControlServerPort(context);
                final boolean controlSSL = ConfigHelper.isControlSeverSSL(context);
                File cacheDir = context.getCacheDir();

                final ArrayList<String> geoInfo = fullInfo.getCurLocation();

                client = TestClient.getInstance(controlServer, null, controlPort, ConfigHelper.getSelectedMeasurementServerId(context), controlSSL, geoInfo, uuid,
                        Config.RMBT_CLIENT_TYPE, Config.RMBT_CLIENT_NAME,
                        fullInfo.getInfo("CLIENT_SOFTWARE_VERSION"), null, fullInfo.getInitialInfo(), cacheDir);

                if (client != null) {
                    /*
                	 * Example on how to implement the information collector tool:
                	 *
                	 * 
					*/

                    final InformationCollectorTool infoTool = new InformationCollectorTool(TimeUnit.NANOSECONDS, TimeUnit.NANOSECONDS.convert(120, TimeUnit.SECONDS));
                    infoTool.addCollector(new CpuStatCollector(new CpuStatAndroidImpl(), TimeUnit.NANOSECONDS.convert(1, TimeUnit.SECONDS)));
                    infoTool.addCollector(new MemInfoCollector(new MemInfoAndroidImpl(), TimeUnit.NANOSECONDS.convert(1, TimeUnit.SECONDS)));
                    client.setInformationCollectorTool(infoTool);
                    client.startInformationCollectorTool();

                    /////////////////////////////////////////////////////////////////

                    client.setTrafficService(new TrafficServiceImpl());
                    final ControlServerConnection controlConnection = client.getControlConnection();
                    if (controlConnection != null) {
                        fullInfo.setUUID(controlConnection.getClientUUID());
                        fullInfo.setTestServerName(controlConnection.getServerName());
                    }



                }
            } catch (final Exception e) {
                e.printStackTrace();
                error = true;
            }


            if (error || client == null) {
                connectionError.set(true);
            } else {

                if (client.getStatus() != TestStatus.ERROR) {
                    try {
                        if (Thread.interrupted() || cancelled.get())
                            throw new InterruptedException();
                        Log.d(LOG_TAG, "runTest TestTask=" + this);
                        result = client.runTest();
                        final ControlServerConnection controlConnection = client.getControlConnection();

                        final InformationCollectorTool infoCollectorTool = client.getInformationCollectorTool();

                        Gson gson = new Gson();
                        if (result != null && !fullInfo.getIllegalNetworkTypeChangeDetcted()) {
                            final JsonObject infoObject = fullInfo.getResultValues(controlConnection.getStartTimeNs());
                            if (infoCollectorTool != null) {
                                infoCollectorTool.stop();
                                infoObject.add("extended_test_stat", gson.toJsonTree(infoCollectorTool.getJsonObject(true, client.getControlConnection().getStartTimeNs())));
                            }

                            boolean dataFuzzing = context.getResources().getBoolean(R.bool.test_use_personal_data_fuzzing);
                            if (dataFuzzing) {
                                infoObject.add("publish_public_data", gson.toJsonTree(ConfigHelper.isInformationCommissioner(context)));
                            }

                            client.sendResult(infoObject);
                        } else {
                            error = true;
                        }
                    } catch (final Exception e) {
                        e.printStackTrace();
                    } finally {
                        client.shutdown();
                    }
                } else {
                    System.err.println(client.getErrorMsg());
                    error = true;
                }

                //client.shutdown();

                setPreviousTestStatus();
                QualityOfServiceTest qosTest = null;

                boolean runQoS = (client.getTaskDescList() != null && client.getTaskDescList().size() >= 1 && ConfigHelper.isQosEnabled(context));

                // Do not run Qos in Loop mode if it is disabled in settings
                if (runQoS && LoopModeConfig.isLoopMode(context)) {
                    if (LoopModeConfig.isLoopModeQosDisabled(context)) {
                        runQoS = false;
                    }
                }

                //run qos test:
                if (runQoS && !error && !cancelled.get()) {
                    try {

					    TestSettings qosTestSettings = new TestSettings();
			            qosTestSettings.setCacheFolder(context.getCacheDir());
					    qosTestSettings.setWebsiteTestService(new WebsiteTestServiceImpl(context));
					    qosTestSettings.setTrafficService(new TrafficServiceImpl());
					    qosTestSettings.setTracerouteServiceClazz(TracerouteAndroidImpl.class);
						qosTestSettings.setStartTimeNs(getRmbtClient().getControlConnection().getStartTimeNs());
						qosTestSettings.setUseSsl(ConfigHelper.isQoSSeverSSL(context));


                        qosTest = new QualityOfServiceTest(client, qosTestSettings);
                        qosReference.set(qosTest);
                        client.setStatus(TestStatus.QOS_TEST_RUNNING);
                        qosResult = qosTest.call();
                        InformationCollector.qoSResult = qosResult;

                        if (!cancelled.get()) {
                            if (qosResult != null && !qosTest.getStatus().equals(QoSTestEnum.ERROR)) {
                                client.sendQoSResult(qosResult);
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        error = true;
                    }
                }

                if (qosTest != null && !cancelled.get() && qosTest.getStatus().equals(QoSTestEnum.QOS_FINISHED)) {
                    if (ConfigHelper.isNDT(context)) {
                        qosTest.setStatus(QoSTestEnum.NDT_RUNNING);
                        runNDT();
                    }
                    qosTest.setStatus(QoSTestEnum.STOP);
                }
            }
        } catch (final Exception e) {
            if (client != null) {
                client.setStatus(TestStatus.ERROR);
            }
            e.printStackTrace();
            Thread.currentThread().interrupt();
        } finally {
            try {
                if (client != null) {
                    client.stopInformationCollectorTool();
                    final TestStatus status = client.getStatus();
                    if (!(status == TestStatus.ABORTED || status == TestStatus.ERROR))
                        client.setStatus(TestStatus.END);
                }
            } catch (Exception e) {
            }
        }
    }

    private final AtomicReference<NDTRunner> ndtRunnerHolder = new AtomicReference<NDTRunner>();

    public float getNDTProgress() {
        final NDTRunner ndtRunner = ndtRunnerHolder.get();
        if (ndtRunner == null)
            return 0;
        return ndtRunner.getNdtProgress();
    }

    public NdtStatus getNdtStatus() {
        final NDTRunner ndtRunner = ndtRunnerHolder.get();
        if (ndtRunner == null)
            return null;
        return ndtRunner.getNdtStatus();
    }

    public void stopNDT() {
        final NDTRunner ndtRunner = ndtRunnerHolder.get();
        if (ndtRunner != null)
            ndtRunner.setNdtCacelled(true);
    }

    public void runNDT() {
        final NDTRunner ndtRunner = new NDTRunner();
        ndtRunnerHolder.set(ndtRunner);

        Log.d(LOG_TAG, "ndt status RUNNING");

        final String ndtNetworkType;
        final int networkType = getNetworkType();
        switch (networkType) {
            case InformationCollector.NETWORK_WIFI:
                ndtNetworkType = NdtTests.NETWORK_WIFI;
                break;

            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                ndtNetworkType = NdtTests.NETWORK_UNKNOWN;
                break;

            default:
                ndtNetworkType = NdtTests.NETWORK_MOBILE;
                break;
        }

        ndtRunner.runNDT(ndtNetworkType, ndtRunner.new UiServices() {

            @Override
            public void sendResults() {
                client.getControlConnection().sendNDTResult(this, null);
            }

            public boolean wantToStop() {
                if (super.wantToStop())
                    return true;

                if (cancelled.get()) {
                    cancel();
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * @return
     */
    public float getQoSTestProgress() {
        final QualityOfServiceTest nnTest = qosReference.get();
        if (nnTest == null)
            return 0;
        return nnTest.getProgress();
    }

    /**
     * @return
     */
    public int getQoSTestSize() {
        final QualityOfServiceTest nnTest = qosReference.get();
        if (nnTest == null)
            return 0;
        return nnTest.getTestSize();
    }

    /**
     * @return
     */
    public QualityOfServiceTest getQoSTest() {
        return qosReference.get();
    }


    /**
     * @return
     */
    public QoSTestEnum getQoSTestStatus() {
        final QualityOfServiceTest nnTest = qosReference.get();
        if (nnTest == null)
            return null;
        return nnTest.getStatus();
    }

    /**
     * @return
     */
    public Map<QoSTestResultEnum, Counter> getQoSGroupCounterMap() {
        final QualityOfServiceTest nnTest = qosReference.get();
        if (nnTest == null)
            return null;
        return nnTest.getTestGroupCounterMap();
    }

    public void setEndTaskListener(final EndTaskListener endTaskListener) {
        this.endTaskListener = endTaskListener;
    }

    public Integer getSignal() {
        if (fullInfo != null)
            return fullInfo.getSignal();
        else
            return null;
    }

    public int getSignalType() {
        if (fullInfo != null)
            return fullInfo.getSignalType();
        else
            return InformationCollector.SINGAL_TYPE_NO_SIGNAL;
    }

    public IntermediateResult getIntermediateResult(final IntermediateResult result) {
        if (client == null)
            return null;
        return client.getIntermediateResult(result);
    }

    public boolean isConnectionError() {
        return connectionError.get();
    }

    public String getOperatorName() {
        if (fullInfo != null)
            return fullInfo.getOperatorName();
        else
            return null;
    }

    public Location getLocation() {
        if (fullInfo != null)
            return fullInfo.getLastLocation();
        else
            return null;
    }

    public String getServerName() {
        if (fullInfo != null)
            return fullInfo.getTestServerName();
        else
            return null;
    }

    public String getIP() {
        if (client != null)
            return client.getPublicIP();
        else
            return null;
    }

    public String getTestUuid() {
        if (client != null)
            return client.getTestUuid();
        else
            return null;
    }

    public int getNetworkType() {
        if (fullInfo != null) {
            final int networkType = fullInfo.getNetwork();
            if (fullInfo.getIllegalNetworkTypeChangeDetcted()) {
                Log.e(LOG_TAG, "illegal network change detected; cancelling test");
                cancel();
            }
            return networkType;
        } else
            return 0;
    }

    public TestClient getRmbtClient() {
        return client;
    }
}
