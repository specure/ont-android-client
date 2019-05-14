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
package at.specure.client;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonObject;

import java.io.File;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import at.specure.android.api.jsons.VoipTestResult;
import at.specure.android.configs.LocaleConfig;
import at.specure.android.configs.LoopModeConfig;
import at.specure.android.impl.TracerouteAndroidImpl;
import at.specure.android.impl.TrafficServiceImpl;
import at.specure.android.screens.test_results.VoipTestResultHandler;
import at.specure.android.util.InformationCollector;
import at.specure.client.RMBTTest.CurrentSpeed;
import at.specure.client.helper.Config;
import at.specure.client.helper.ControlServerConnection;
import at.specure.client.helper.IntermediateResult;
import at.specure.client.helper.RMBTOutputCallback;
import at.specure.client.helper.TestStatus;
import at.specure.client.v2.task.TaskDesc;
import at.specure.client.v2.task.VoipTask;
import at.specure.client.v2.task.result.QoSResultCollector;
import at.specure.client.v2.task.result.QoSTestResult;
import at.specure.client.v2.task.service.TestMeasurement;
import at.specure.client.v2.task.service.TestSettings;
import at.specure.client.v2.task.service.TrafficService;
import at.specure.util.tools.InformationCollectorTool;
import timber.log.Timber;

import static at.specure.client.v2.task.VoipTask.RESULT_INCOMING_PREFIX;
import static at.specure.client.v2.task.VoipTask.RESULT_MEAN_JITTER;
import static at.specure.client.v2.task.VoipTask.RESULT_OUTGOING_PREFIX;
import static at.specure.client.v2.task.VoipTask.RESULT_VOIP_PREFIX;

public class TestClient {
    private static final ExecutorService COMMON_THREAD_POOL = Executors.newCachedThreadPool();

    private final TestParameter params;

    private final long durationInitNano = 2500000000L; // TODO
    private final long durationUpNano;
    private final long durationDownNano;

    private final AtomicLong initNano = new AtomicLong(-1);
    private final AtomicLong pingNano = new AtomicLong(-1);
    private final AtomicLong downBitPerSec = new AtomicLong(-1);
    private final AtomicLong upBitPerSec = new AtomicLong(-1);
    private final AtomicLong jitter = new AtomicLong(-1);
    private final AtomicLong packetLossUp = new AtomicLong(-1);
    private final AtomicLong packetLossDown = new AtomicLong(-1);
    private final AtomicReference<QualityOfServiceTest> voipReference = new AtomicReference<QualityOfServiceTest>();

    /* ping status */
    private final AtomicLong pingTsStart = new AtomicLong(-1);
    private final AtomicInteger pingNumDome = new AtomicInteger(-1);
    private final AtomicLong pingTsLastPing = new AtomicLong(-1);

    private final static long MIN_DIFF_TIME = 100000000; // 100 ms

    private final static int KEEP_LAST_ENTRIES = 100; // if more than more smooth values changes during measurement
    private final Context context;
    private int lastCounter;
    private final long[][] lastTransfer;
    private final long[][] lastTime;

    private final ExecutorService testThreadPool;

    private final File cacheDir;

    private final RMBTTest[] testTasks;

    private TotalTestResult result;

    private SSLSocketFactory sslSocketFactory;

    private RMBTOutputCallback outputCallback;
    private final boolean outputToStdout = true;

    private final ControlServerConnection controlConnection;

    private final AtomicBoolean aborted = new AtomicBoolean();

    private String errorMsg = "";
    
    
    /*------------------------------------
        V2 tests
    --------------------------------------*/

    public final static String TASK_UDP = "udp";
    public final static String TASK_TCP = "tcp";
    public final static String TASK_DNS = "dns";
    public final static String TASK_VOIP = "voip";
    public final static String TASK_JITTER = "jitter";
    public final static String TASK_NON_TRANSPARENT_PROXY = "non_transparent_proxy";
    public final static String TASK_HTTP = "http_proxy";
    public final static String TASK_WEBSITE = "website";
    public final static String TASK_TRACEROUTE = "traceroute";

    private List<TaskDesc> taskDescList;

    /*------------------------------------*/

    private final AtomicReference<TestStatus> testStatus = new AtomicReference<TestStatus>(TestStatus.WAIT);
    private final AtomicReference<TestStatus> statusBeforeError = new AtomicReference<TestStatus>(null);
    private final AtomicLong statusChangeTime = new AtomicLong();

    private TrafficService trafficService;

    private InformationCollectorTool informationCollectorTool;

    public static ExecutorService getCommonThreadPool() {
        return COMMON_THREAD_POOL;
    }

    private ConcurrentHashMap<TestStatus, TestMeasurement> measurementMap = new ConcurrentHashMap<TestStatus, TestMeasurement>();

    public static TestClient getInstance(final String host, final String pathPrefix, final int port, int measurementServerId,
                                         final boolean encryption, final ArrayList<String> geoInfo, final String uuid, final String clientType,
                                         final String clientName, final String clientVersion, final TestParameter overrideParams,
                                         final JsonObject additionalValues, File cacheDir, Context context) {
        final ControlServerConnection controlConnection = new ControlServerConnection();


        final String error = controlConnection.requestNewTestConnection(host, pathPrefix, port, measurementServerId, encryption, geoInfo,
                uuid, clientType, clientName, clientVersion, additionalValues, LocaleConfig.getLocaleForServerRequest(context));

        if (error != null) {
            System.out.println(error);
            return null;
        }

        //TODO: simple and fast solution; make it better
        final String errorNewTest = controlConnection.requestQoSTestParameters(host, pathPrefix, port, encryption, geoInfo,
                uuid, clientType, clientName, clientVersion, additionalValues, LocaleConfig.getLocaleForServerRequest(context));

        if (errorNewTest != null) {
            System.out.println(errorNewTest);
            return null;
        }

        final TestParameter params = controlConnection.getTestParameter(overrideParams);

        return new TestClient(params, controlConnection, cacheDir, context);
    }

    private TestClient(final TestParameter params, final ControlServerConnection controlConnection, File cacheDir, Context context) {
        this.params = params;
        this.controlConnection = controlConnection;
        this.cacheDir = cacheDir;
        this.context = context;

        params.check();

        if (params.getNumThreads() > 0) {
            testThreadPool = Executors.newFixedThreadPool(params.getNumThreads());
            testTasks = new RMBTTest[params.getNumThreads()];
        } else {
            testThreadPool = null;
            testTasks = null;
        }

        durationDownNano = params.getDuration() * 1000000000L;
        durationUpNano = params.getDuration() * 1000000000L;

//        jitter = params.getMeanJitter();
//        packetLossDown = params.getPacketLossDown();
//        packetLossUp = params.getPacketLossUp();

        lastTransfer = new long[params.getNumThreads()][KEEP_LAST_ENTRIES];
        lastTime = new long[params.getNumThreads()][KEEP_LAST_ENTRIES];

        this.taskDescList = controlConnection.v2TaskDesc;
        //if (params.isEncryption())
        //    sslSocketFactory = createSSLSocketFactory();

    }

    public Context getContext() {
        return context;
    }

    public void setTrafficService(TrafficService trafficService) {
        this.trafficService = trafficService;
    }

    public TrafficService getTrafficService() {
        return this.trafficService;
    }

    public void setInformationCollectorTool(InformationCollectorTool tool) {
        this.informationCollectorTool = tool;
    }

    public InformationCollectorTool getInformationCollectorTool() {
        return this.informationCollectorTool;
    }

    public void startInformationCollectorTool() {
        if (this.informationCollectorTool != null && !this.informationCollectorTool.isRunning()) {
            this.informationCollectorTool.start(COMMON_THREAD_POOL);
        }
    }

    public JsonObject getInformationCollectorToolIntermediateResult(boolean clean) {
        if (this.informationCollectorTool != null && !this.informationCollectorTool.isRunning()) {
            return this.informationCollectorTool.getJsonObject(clean);
        }

        return null;
    }

    public void stopInformationCollectorTool() {
        if (this.informationCollectorTool != null && this.informationCollectorTool.isRunning()) {
            this.informationCollectorTool.stop();
        }
    }

    private SSLSocketFactory createSSLSocketFactory() {
        log("initSSL...");
        try {
            final SSLContext sc = getSSLContext(null, null);

            final SSLSocketFactory factory = sc.getSocketFactory();

            return factory;
        } catch (final Exception e) {
            setErrorStatus();
            log(e);
        }
        return null;
    }

    public static TrustManager getTrustingManager() {
        return new javax.net.ssl.X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[]{};
            }

            public void checkClientTrusted(final X509Certificate[] certs, final String authType)
                    throws CertificateException {
                // System.out.println("[TRUSTING] checkClientTrusted: " +
                // Arrays.toString(certs) + " - " + authType);
            }

            public void checkServerTrusted(final X509Certificate[] certs, final String authType)
                    throws CertificateException {
                // System.out.println("[TRUSTING] checkServerTrusted: " +
                // Arrays.toString(certs) + " - " + authType);
            }
        };
    }

    public static SSLContext getSSLContext(final String caResource, final String certResource)
            throws NoSuchAlgorithmException, KeyManagementException {
        X509Certificate _ca = null;
        try {
            if (caResource != null) {
                final CertificateFactory cf = CertificateFactory.getInstance("X.509");
                _ca = (X509Certificate) cf.generateCertificate(TestClient.class.getClassLoader().getResourceAsStream(
                        caResource));
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }

        final X509Certificate ca = _ca;

        X509Certificate _cert = null;
        try {
            if (certResource != null) {
                final CertificateFactory cf = CertificateFactory.getInstance("X.509");
                _cert = (X509Certificate) cf.generateCertificate(TestClient.class.getClassLoader().getResourceAsStream(
                        certResource));
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
        final X509Certificate cert = _cert;

        // TrustManagerFactory tmf = null;
        // try
        // {
        // if (cert != null)
        // {
        // final KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        // ks.load(null, null);
        // ks.setCertificateEntry("crt", cert);
        //
        // tmf =
        // TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        // tmf.init(ks);
        // }
        // }
        // catch (Exception e)
        // {
        // e.printStackTrace();
        // }

        final TrustManager tm;
        if (cert == null)
            tm = getTrustingManager();
        else
            tm = new javax.net.ssl.X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    // System.out.println("getAcceptedIssuers");
                    if (ca == null)
                        return new X509Certificate[]{cert};
                    else
                        return new X509Certificate[]{ca};
                }

                public void checkClientTrusted(final X509Certificate[] certs, final String authType)
                        throws CertificateException {
                    // System.out.println("checkClientTrusted: " +
                    // Arrays.toString(certs) + " - " + authType);
                }

                public void checkServerTrusted(final X509Certificate[] certs, final String authType)
                        throws CertificateException {
                    // System.out.println("checkServerTrusted: " +
                    // Arrays.toString(certs) + " - " + authType);
                    if (certs == null)
                        throw new CertificateException();
                    for (final X509Certificate c : certs)
                        if (cert.equals(c))
                            return;
                    throw new CertificateException();
                }
            };

        final TrustManager[] trustManagers = new TrustManager[]{tm};

        SSLContext sc;
        sc = SSLContext.getInstance(Config.RMBT_ENCRYPTION_STRING);

        sc.init(null, trustManagers, new java.security.SecureRandom());
        return sc;
    }

    /**
     * This method runs main test (ping, upload download)
     * It calls #RMBTTest contructor #num_threads times and stores them into testTasks
     *
     * @return
     * @throws InterruptedException
     */
    public TestResult runTest() throws InterruptedException {
        System.out.println("starting test...");

        // https://copperegg.zendesk.com/hc/en-us/articles/214633883-What-do-TX-and-RX-refer-to-in-the-Network-Charts-

        long txBytes = 0; //transmit bytes = from server
        long rxBytes = 0; //receive bytes = to server
        final long timeStampStart = System.nanoTime();

        if (testStatus.get() != TestStatus.ERROR && testThreadPool != null) {

            if (trafficService != null) {
                txBytes = trafficService.getTotalTxBytes();
                rxBytes = trafficService.getTotalRxBytes();
            }

            resetSpeed();
            resertResultValues();

            final long waitTime = params.getStartTime() - System.currentTimeMillis();
            if (waitTime > 0) {
                setStatus(TestStatus.WAIT);
                log(String.format(Locale.US, "we have to wait %d ms...", waitTime));
                Thread.sleep(waitTime);
                log(String.format(Locale.US, "...done.", waitTime));
            } else
                log(String.format(Locale.US, "luckily we do not have to wait.", waitTime));

            setStatus(TestStatus.INIT);
            statusBeforeError.set(null);

            if (testThreadPool.isShutdown())
                throw new IllegalStateException("TestClient already shut down");
            log("starting test...");

            final int numThreads = params.getNumThreads();

            aborted.set(false);

            result = new TotalTestResult();

            if (params.isEncryption())
                sslSocketFactory = createSSLSocketFactory();

            log(String.format(Locale.US, "Host: %s; Port: %s; Enc: %s", params.getHost(), params.getPort(), params.isEncryption()));
            log(String.format(Locale.US, "starting %d threads...", numThreads));

            final CyclicBarrier barrier = new CyclicBarrier(numThreads);

            @SuppressWarnings("unchecked")
            final Future<ThreadTestResult>[] results = new Future[numThreads];

            final int storeResults = (int) (params.getDuration() * 1000000000L / MIN_DIFF_TIME);

            final AtomicBoolean fallbackToOneThread = new AtomicBoolean();

            for (int i = 0; i < numThreads; i++) {
                testTasks[i] = new RMBTTest(this, params, i, barrier, storeResults, MIN_DIFF_TIME, fallbackToOneThread);
                results[i] = testThreadPool.submit(testTasks[i]);
            }

            try {

                long shortestPing = Long.MAX_VALUE;

                // wait for all threads first
                for (int i = 0; i < numThreads; i++)
                    results[i].get();

                if (aborted.get())
                    return null;

                final long[][] allDownBytes = new long[numThreads][];
                final long[][] allDownNsecs = new long[numThreads][];
                final long[][] allUpBytes = new long[numThreads][];
                final long[][] allUpNsecs = new long[numThreads][];

                int realNumThreads = 0;
                log("");


                for (int i = 0; i < numThreads; i++) {
                    final ThreadTestResult testResult = results[i].get();

                    if (testResult != null) {
                        realNumThreads++;

                        Timber.e("DOWNLOAD Thread %d: Download: bytes: %d time: %.3f s", i,
                                ThreadTestResult.getLastEntry(testResult.down.bytes),
                                ThreadTestResult.getLastEntry(testResult.down.nsec) / 1e9);
                        Timber.e("UPLOAD Thread %d: Upload:   bytes: %d time: %.3f s", i,
                                ThreadTestResult.getLastEntry(testResult.up.bytes),
                                ThreadTestResult.getLastEntry(testResult.up.nsec) / 1e9);

                        //na co sa tu zistuje shortest ked sa zobrazuje ten median?
                        final long ping = testResult.ping_shortest;
                        if (ping < shortestPing)
                            shortestPing = ping;

                        if (!testResult.pings.isEmpty())
                            result.pings.addAll(testResult.pings);

                        allDownBytes[i] = testResult.down.bytes;
                        allDownNsecs[i] = testResult.down.nsec;
                        allUpBytes[i] = testResult.up.bytes;
                        allUpNsecs[i] = testResult.up.nsec;

                        result.totalDownBytes += testResult.totalDownBytes;
                        result.totalUpBytes += testResult.totalUpBytes;

                        // aggregate speedItems
                        result.speedItems.addAll(testResult.speedItems);
                    }
                }

//                /**** VOIP, JITTER, PACKET LOSS ****/
//                performVoipTest();

                result.calculateDownload(allDownBytes, allDownNsecs);
                result.calculateUpload(allUpBytes, allUpNsecs);

                log("");
                log(String.format(Locale.US, "Total calculated bytes down: %d", result.bytes_download));
                log(String.format(Locale.US, "Total calculated time down:  %.3f s", result.nsec_download / 1e9));
                log(String.format(Locale.US, "Total calculated bytes up:   %d", result.bytes_upload));
                log(String.format(Locale.US, "Total calculated time up:    %.3f s", result.nsec_upload / 1e9));

                // get Connection Info from thread 1 (one thread must run)
                result.ip_local = results[0].get().ip_local;
                result.ip_server = results[0].get().ip_server;
                result.port_remote = results[0].get().port_remote;
                result.encryption = results[0].get().encryption;

                result.num_threads = realNumThreads;

                result.ping_shortest = shortestPing;

                result.speed_download = result.getDownloadSpeedBitPerSec() / 1e3;
                result.speed_upload = result.getUploadSpeedBitPerSec() / 1e3;

                log("");
                log(String.format(Locale.US, "Total Down: %.0f kBit/s", result.getDownloadSpeedBitPerSec() / 1e3));
                log(String.format(Locale.US, "Total UP:   %.0f kBit/s", result.getUploadSpeedBitPerSec() / 1e3));
                log(String.format(Locale.US, "Ping:       %.2f ms", shortestPing / 1e6));

                if (controlConnection != null) {
                    log("");
                    final String testId = controlConnection.getTestId();
                    final String testUUID = params.getUUID();
                    final long testTime = controlConnection.getTestTime();
                    log(String.format(Locale.US, "uid=%s, time=%s, uuid=%s\n", testId, new SimpleDateFormat(
                            "yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(new Date(testTime)), testUUID));
                }

                downBitPerSec.set(Math.round(result.getDownloadSpeedBitPerSec()));
                upBitPerSec.set(Math.round(result.getUploadSpeedBitPerSec()));

                log("end.");
                setStatus(TestStatus.SPEEDTEST_END);

                if (trafficService != null) {
                    txBytes = trafficService.getTotalTxBytes() - txBytes;
                    rxBytes = trafficService.getTotalRxBytes() - rxBytes;
                    result.setTotalTrafficMeasurement(new TestMeasurement(rxBytes, txBytes, timeStampStart, System.nanoTime()));
                    result.setMeasurementMap(measurementMap);
                }

                return result;
            } catch (final ExecutionException e) {
                log(e);
                abortTest(true);
                return null;
            } catch (final InterruptedException e) {
                log("TestClient interrupted!");
                abortTest(false);
                throw e;
            }
        } else {
            setStatus(TestStatus.SPEEDTEST_END);
            return null;
        }
    }

    public void performVoipTest() {
        VoipTest voipTest;

        if (!aborted.get()) {
            try {
                QoSResultCollector voipResult;
                //Implementation for Jitter and Packet loss
                TestSettings qosTestSettings = new TestSettings();
                qosTestSettings.setCacheFolder(getCacheDir());
                qosTestSettings.setTrafficService(new TrafficServiceImpl());
                qosTestSettings.setTracerouteServiceClazz(TracerouteAndroidImpl.class);
                qosTestSettings.setStartTimeNs(getControlConnection().getStartTimeNs());
                qosTestSettings.setUseSsl(params.isEncryption());

                boolean onlyVoipTest = true;
                //noinspection ConstantConditions
                voipTest = new JitterTest(this, qosTestSettings, onlyVoipTest);

                voipReference.set(voipTest);
                setStatus(TestStatus.PACKET_LOSS_AND_JITTER);
                voipResult = voipTest.call();

                List<QoSTestResult> voipTestRsults = voipResult.getResults();
                if ((voipTestRsults != null) && (!voipTestRsults.isEmpty())) {
                    QoSTestResult qoSTestResult = voipTestRsults.get(0);
                    HashMap<String, Object> resultMap = qoSTestResult.getResultMap();

                    VoipTestResultHandler voipTestResultHandler = new VoipTestResultHandler();
                    VoipTestResult voipTestResult = voipTestResultHandler.convertResultsToObject(resultMap);
                    result.voipTestResult = voipTestResult;

                    //TODO: save to shared pref to load to send them together with result

                    final String prefix_out = RESULT_VOIP_PREFIX + RESULT_OUTGOING_PREFIX;
                    final String prefix_in = RESULT_VOIP_PREFIX + RESULT_INCOMING_PREFIX;

                    String format;

                    Long meanJitterOut = (Long) resultMap.get(prefix_out + RESULT_MEAN_JITTER);
                    Long meanJitterIn = (Long) resultMap.get(prefix_in + RESULT_MEAN_JITTER);
                    if ((meanJitterIn != null) && (meanJitterOut != null)) {
                        Long meanJitter = (meanJitterIn + meanJitterOut) / 2;
                        result.jitterMedian = meanJitter;
                        this.jitter.set(meanJitter);
                    }

                    long callDuration = (Long) resultMap.get(VoipTask.RESULT_CALL_DURATION);
                    long delay = (Long) resultMap.get(VoipTask.RESULT_DELAY);
                    long outPacketsNumber = (Long) resultMap.get(prefix_out + VoipTask.RESULT_NUM_PACKETS);
                    int inPacketsNumber = (Integer) resultMap.get(prefix_in + VoipTask.RESULT_NUM_PACKETS);

                    int total = ((int) callDuration / (int) delay);

                    int packetLossDown = (int) (100f * ((float) (total - inPacketsNumber) / (float) total));
                    int packetLossUp = (int) (100f * ((float) (total - outPacketsNumber) / (float) total));

                    result.packetLossPercentDown = packetLossDown;
                    result.packetLossPercentUp = packetLossUp;

                    this.packetLossDown.set(packetLossDown);
                    this.packetLossUp.set(packetLossUp);


                    Timber.e("JITTER: %s, PL_DOWN: %s, PL_UP: %s", jitter, packetLossDown, packetLossUp);

                    InformationCollector.voipResult = voipResult;

                }

            } catch (Exception e) {
                e.printStackTrace();
                Timber.e("JITTER ERROR %s", e.getMessage());
                log(e);
            }
        }
    }

    private void resertResultValues() {
        downBitPerSec.set(-1);
        upBitPerSec.set(-1);
        pingNano.set(-1);
        initNano.set(-1);
        jitter.set(-1);
        packetLossUp.set(-1);
        packetLossDown.set(-1);
    }

    public boolean abortTest(final boolean error) {
        System.out.println("TestClient stopTest");

        if (error)
            setErrorStatus();
        else
            setStatus(TestStatus.ABORTED);
        aborted.set(true);

        if (testThreadPool != null)
            testThreadPool.shutdownNow();

        return true;
    }

    public void shutdown() {
        System.out.println("Shutting down RMBT thread pool...");
        if (testThreadPool != null)
            testThreadPool.shutdownNow();

        System.out.println("Shutdown finished.");
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (testThreadPool != null)
            testThreadPool.shutdownNow();
    }

    public SSLSocketFactory getSslSocketFactory() {
        return sslSocketFactory;
    }

    public void setOutputCallback(final RMBTOutputCallback outputCallback) {
        this.outputCallback = outputCallback;
    }

    private void resetSpeed() {
        lastCounter = 0;
    }

    private float getTotalSpeed() {
        long sumTrans = 0;
        long maxTime = 0;

        final CurrentSpeed currentSpeed = new CurrentSpeed();

        for (int i = 0; i < params.getNumThreads(); i++)
            if (testTasks[i] != null) {
                testTasks[i].getCurrentSpeed(currentSpeed);

                if (currentSpeed.time > maxTime)
                    maxTime = currentSpeed.time;
                sumTrans += currentSpeed.trans;
            }

        return maxTime == 0f ? 0f : (float) sumTrans / (float) maxTime * 1e9f * 8.0f;
    }

    final Map<Integer, List<SpeedItem>> speedMap = new HashMap<Integer, List<SpeedItem>>();

    private float getAvgSpeed() {
        long sumDiffTrans = 0;
        long maxDiffTime = 0;

        final CurrentSpeed currentSpeed = new CurrentSpeed();

        final int currentIndex = lastCounter % KEEP_LAST_ENTRIES;
        int diffReferenceIndex = (lastCounter - KEEP_LAST_ENTRIES + 1) % KEEP_LAST_ENTRIES;
        if (diffReferenceIndex < 0)
            diffReferenceIndex = 0;

        lastCounter++;

        for (int i = 0; i < params.getNumThreads(); i++)
            if (testTasks[i] != null) {
                testTasks[i].getCurrentSpeed(currentSpeed);

                lastTime[i][currentIndex] = currentSpeed.time;
                lastTransfer[i][currentIndex] = currentSpeed.trans;

//                System.out.println("T" + i + ": " + currentSpeed);

                List<SpeedItem> speedList = speedMap.get(i);
                if (speedList == null) {
                    speedList = new ArrayList<SpeedItem>();
                    speedMap.put(i, speedList);
                }

                speedList.add(new SpeedItem(false, i, currentSpeed.time, currentSpeed.trans));

                final long diffTime = currentSpeed.time - lastTime[i][diffReferenceIndex];
                final long diffTrans = currentSpeed.trans - lastTransfer[i][diffReferenceIndex];

                if (diffTime > maxDiffTime)
                    maxDiffTime = diffTime;
                sumDiffTrans += diffTrans;
            }

        //TotalTestResult totalResult = TotalTestResult.calculateAndGet(lastTransfer, lastTime, false);
        //TotalTestResult totalResult = TotalTestResult.calculateAndGet(speedMap);

        final float speedAvg = maxDiffTime == 0f ? 0f : (float) sumDiffTrans / (float) maxDiffTime * 1e9f * 8.0f;
        //final float speedAvg = (float)totalResult.speed_download * 1e3f;

//        System.out.println("calculate: bytes=" + totalResult.bytes_download + " speed=" + (totalResult.speed_download * 1e3) 
//        		+ " nsec=" + totalResult.nsec_download + ", simple: diff=" + sumDiffTrans + " avg=" + speedAvg);

        return speedAvg;
    }

    public IntermediateResult getIntermediateResult(IntermediateResult iResult) {
        if (iResult == null)
            iResult = new IntermediateResult();
        iResult.status = testStatus.get();
        iResult.remainingWait = 0;
        final long diffTime = System.nanoTime() - statusChangeTime.get();
        switch (iResult.status) {
            case WAIT:
                iResult.progress = 0;
                iResult.remainingWait = params.getStartTime() - System.currentTimeMillis();
                break;

            case INIT:
                iResult.progress = (float) diffTime / durationInitNano;
                break;

            case PING:
                iResult.progress = getPingProgress();
                break;

            case PACKET_LOSS_AND_JITTER:
                //TODO: solve how to update progress
//                iResult.progress
                break;

            case DOWN:
                iResult.progress = (float) diffTime / durationDownNano;
                downBitPerSec.set(Math.round(getAvgSpeed()));
                break;

            case INIT_UP:
                iResult.progress = 0;
                break;

            case UP:
                iResult.progress = (float) diffTime / durationUpNano;
                upBitPerSec.set(Math.round(getAvgSpeed()));
                break;

            case SPEEDTEST_END:
                iResult.progress = 1;
                break;

            case ERROR:
            case ABORTED:
                iResult.progress = 0;
                break;
            default:
                break;
        }

        if (iResult.progress > 1)
            iResult.progress = 1;

        iResult.initNano = initNano.get();
        iResult.pingNano = pingNano.get();
        iResult.downBitPerSec = downBitPerSec.get();
        iResult.upBitPerSec = upBitPerSec.get();
        iResult.jitter = jitter.get();
        iResult.packetLossUp = packetLossUp.get();
        iResult.packetLossDown = packetLossDown.get();

        iResult.setLogValues();

        return iResult;
    }

    public TestStatus getStatus() {
        return testStatus.get();
    }

    public TestStatus getStatusBeforeError() {
        return statusBeforeError.get();
    }

    public void setStatus(final TestStatus status) {
        testStatus.set(status);
        statusChangeTime.set(System.nanoTime());
        if (status == TestStatus.INIT_UP) {
            // DOWN is finished
            downBitPerSec.set(Math.round(getTotalSpeed()));
            resetSpeed();
        }

//        /**
//         * JITTER PACKET LOSS
//         */
//        if (status == TestStatus.PACKET_LOSS_AND_JITTER) {
//            Timber.e("JITTER", "START");
//            performVoipTest();
//        }
    }

    public void startTrafficService(final int threadId, final TestStatus status) {
        if (trafficService != null) {
            //a concurrent map is needed in case multiple threads want to start the traffic service
            //only the first thread should be able to start the service
            TestMeasurement tm = new TestMeasurement(status.toString(), trafficService);
            TestMeasurement previousTm = measurementMap.putIfAbsent(status, tm);
            if (previousTm == null) {
                tm.start(threadId);
            }
        }
    }

    public void stopTrafficMeasurement(final int threadId, final TestStatus status) {
        final TestMeasurement testMeasurement = measurementMap.get(status);
        if (testMeasurement != null)
            testMeasurement.stop(threadId);
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void sendResult(final JsonObject additionalValues) {
        if (controlConnection != null) {
            final String errorMsg = controlConnection.sendTestResult(result, additionalValues, LoopModeConfig.getCurrentLoopId(context), LocaleConfig.getLocaleForServerRequest(context));
            if (errorMsg != null) {
                setErrorStatus();
                log("Error sending Result...");
                log(errorMsg);
            }
        }
    }

    public void sendQoSResult(final QoSResultCollector qosResult) {
        if (controlConnection != null) {
            final String errorMsg = controlConnection.sendQoSResult(result, qosResult.toJson(), LocaleConfig.getLocaleForServerRequest(context));
            if (errorMsg != null) {
                setErrorStatus();
                log("Error sending QoS Result...");
                log(errorMsg);
            }
        }
    }

    private void setErrorStatus() {
        final TestStatus lastStatus = testStatus.getAndSet(TestStatus.ERROR);
        if (lastStatus != TestStatus.ERROR)
            statusBeforeError.set(lastStatus);
    }

    void log(final CharSequence text) {
        if (outputToStdout)
            System.out.println(text);
        if (outputCallback != null)
            outputCallback.log(text);
    }

    void log(final Exception e) {
        if (outputToStdout)
            e.printStackTrace(System.out);
        if (outputCallback != null)
            outputCallback.log(String.format(Locale.US, "Error: %s", e.getMessage()));
    }

    void setInitTime(final long initDuration) {
        initNano.set(initDuration);
    }

    long getInitTime() {
        return initNano.get();
    }

    void setPing(final long shortestPing) {
        pingNano.set(shortestPing);
    }

    void updatePingStatus(final long tsStart, int pingsDone, long tsLastPing) {
        pingTsStart.set(tsStart);
        pingNumDome.set(pingsDone);
        pingTsLastPing.set(tsLastPing);
    }

    private float getPingProgress() {
        final long start = pingTsStart.get();

        if (start == -1) // not yet started
            return 0;

        final int numDone = pingNumDome.get();
        final long lastPing = pingTsLastPing.get();
        final long now = System.nanoTime();

        final int numPings = params.getNumPings();

        if (numPings <= 0) // nothing to do
            return 1;

        final float factorPerPing = (float) 1 / (float) numPings;
        final float base = factorPerPing * numDone;

        final long approxTimePerPing;
        if (numDone == 0 || lastPing == -1) // during first ping, assume 100ms
            approxTimePerPing = 100000000;
        else
            approxTimePerPing = (lastPing - start) / numDone;

        float factorLastPing = (float) (now - lastPing) / (float) approxTimePerPing;
        if (factorLastPing < 0)
            factorLastPing = 0;
        if (factorLastPing > 1)
            factorLastPing = 1;

        final float result = base + factorLastPing * factorPerPing;
        if (result < 0)
            return 0;
        if (result > 1)
            return 1;

//        System.out.println("atpp: " + approxTimePerPing + "; flp:" + factorLastPing+ "; res:" +result);
        return result;
    }

    public String getPublicIP() {
        if (controlConnection == null)
            return null;
        return controlConnection.getRemoteIp();
    }

    public String getServerName() {
        if (controlConnection == null)
            return null;
        return controlConnection.getServerName();
    }

    public String getProvider() {
        if (controlConnection == null)
            return null;
        return controlConnection.getProvider();
    }

    public String getTestUuid() {
        if (controlConnection == null)
            return null;
        return controlConnection.getTestUuid();
    }

    public ControlServerConnection getControlConnection() {
        return controlConnection;
    }

    /**
     * @return
     */
    public List<TaskDesc> getTaskDescList() {
        return taskDescList;
    }

    public File getCacheDir() {
        return cacheDir;
    }
}
