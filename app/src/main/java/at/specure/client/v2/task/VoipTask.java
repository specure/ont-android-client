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
package at.specure.client.v2.task;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.JsonElement;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.specure.client.QualityOfServiceTest;
import at.specure.client.v2.task.result.QoSTestResult;
import at.specure.client.v2.task.result.QoSTestResultEnum;
import at.specure.util.net.rtp.RealtimeTransportProtocol.PayloadType;
import at.specure.util.net.rtp.RealtimeTransportProtocol.RtpException;
import at.specure.util.net.rtp.RtpPacket;
import at.specure.util.net.rtp.RtpUtil;
import at.specure.util.net.rtp.RtpUtil.RtpControlData;
import at.specure.util.net.rtp.RtpUtil.RtpQoSResult;
import at.specure.util.net.udp.StreamSender.UdpStreamCallback;
import timber.log.Timber;

/**
 * @author lb
 *         <p>
 *         As of RFC 3550 and RFC 3551 most RTP (VoIP) Codecs have a sampling rate of 8kHz.<br>
 *         The delay between the packets is set to 20ms for most codecs.<br>
 *         The sample size varies from 2 (G726-16) to 16 (L16) bits per sample. Some codecs have a variable sample size.<br>
 *         <br>
 *         <p>
 *         The default VoIP test will be:
 *         <ul>
 *         <li>sampling rate: 8000 Hz</li>
 *         <li>size: 8bit per sample</li>
 *         <li>time/packet: 20ms</li>
 *         </ul>
 *         This is similar to the G722 audio codec (ITU-T Recommendation G.722).<br>
 *         The G722 codec's actual sampling rate is 16kHz but because it was erroneously assigned in RFC 1890 with 8kHz
 *         it needs to have this sampling rate to assure backward compatibility.
 */
public class VoipTask extends AbstractQoSTask {

    private static Pattern VOIP_RECEIVE_RESPONSE_PATTERN = Pattern.compile("VOIPRESULT (-?[\\d]*) (-?[\\d]*) (-?[\\d]*) (-?[\\d]*) (-?[\\d]*) (-?[\\d]*) (-?[\\d]*) (-?[\\d]*)");

    private static Pattern VOIP_OK_PATTERN = Pattern.compile("OK ([\\d]*)");

    private Integer outgoingPort = null;

    private Integer incomingPort = null;

    private long callDuration = 0;

    private long timeout = 0;

    private long delay = 0;

    private int sampleRate = 0;

    private int bitsPerSample = 0;

    private PayloadType payloadType = null;

    private static long DEFAULT_TIMEOUT = 3000000000L; //3s

    private static long DEFAULT_CALL_DURATION = 1000000000L; //1s

    private static long DEFAULT_DELAY = 20000000L; //20ms

    private static int DEFAULT_SAMPLE_RATE = 8000; //8kHz

    private static int DEFAULT_BITS_PER_SAMPLE = 8; //8 bits per sample

    private static PayloadType DEFAULT_PAYLOAD_TYPE = PayloadType.PCMA;

    public static String PARAM_BITS_PER_SAMLE = "bits_per_sample";

    public static String PARAM_SAMPLE_RATE = "sample_rate";

    public static String PARAM_DURATION = "call_duration"; //call duration in ns

    public static String PARAM_PORT = "in_port";

    public static String PARAM_PORT_OUT = "out_port";

    public static String PARAM_TIMEOUT = "timeout";

    public static String PARAM_DELAY = "delay";

    public static String PARAM_PAYLOAD = "payload";

    public static String RESULT_PAYLOAD = "voip_objective_payload";

    public static String RESULT_IN_PORT = "voip_objective_in_port";

    public static String RESULT_OUT_PORT = "voip_objective_out_port";

    public static String RESULT_CALL_DURATION = "voip_objective_call_duration";

    public static String RESULT_BITS_PER_SAMPLE = "voip_objective_bits_per_sample";

    public static String RESULT_SAMPLE_RATE = "voip_objective_sample_rate";

    public static String RESULT_DELAY = "voip_objective_delay";

    public static String RESULT_TIMEOUT = "voip_objective_timeout";

    public static String RESULT_STATUS = "voip_result_status";

    public static String RESULT_VOIP_PREFIX = "voip_result";

    public static String RESULT_INCOMING_PREFIX = "_in_";

    public static String RESULT_OUTGOING_PREFIX = "_out_";

    public static String RESULT_SHORT_SEQUENTIAL = "short_seq";

    public static String RESULT_LONG_SEQUENTIAL = "long_seq";

    public static String RESULT_MAX_JITTER = "max_jitter";

    public static String RESULT_MEAN_JITTER = "mean_jitter";

    public static String RESULT_MAX_DELTA = "max_delta";

    public static String RESULT_SKEW = "skew";

    public static String RESULT_NUM_PACKETS = "num_packets";

    public static String RESULT_SEQUENCE_ERRORS = "sequence_error";

    private final boolean ignoreErrors;

    /**
     * @param taskDesc
     */
    public VoipTask(QualityOfServiceTest nnTest, TaskDesc taskDesc, int threadId, Long customTimeout, boolean ignoreErrors) {
        super(nnTest, taskDesc, threadId, threadId);
        this.ignoreErrors = ignoreErrors;
        String value = ((JsonElement) taskDesc.getParams().get(PARAM_DURATION)).getAsString();
        this.callDuration = value != null ? Long.valueOf(value) : DEFAULT_CALL_DURATION;

        value = ((JsonElement) taskDesc.getParams().get(PARAM_PORT)).getAsString();
        this.incomingPort = value != null ? Integer.valueOf(value) : null;

        value = ((JsonElement) taskDesc.getParams().get(PARAM_PORT_OUT)).getAsString();
        this.outgoingPort = value != null ? Integer.valueOf(value) : null;

        if (customTimeout == null) {
            value = ((JsonElement) taskDesc.getParams().get(PARAM_TIMEOUT)).getAsString();
            this.timeout = value != null ? Long.valueOf(value) : DEFAULT_TIMEOUT;
        } else {
            this.timeout = customTimeout;
        }

        value = null;
        if (taskDesc.getParams().containsKey(PARAM_DELAY)) {
            value = ((JsonElement) taskDesc.getParams().get(PARAM_DELAY)).getAsString();
        }
        this.delay = value != null ? Long.valueOf(value) : DEFAULT_DELAY;

        value = null;
        if (taskDesc.getParams().containsKey(PARAM_BITS_PER_SAMLE)) {
            value = ((JsonElement) taskDesc.getParams().get(PARAM_BITS_PER_SAMLE)).getAsString();
        }
        this.bitsPerSample = value != null ? Integer.valueOf(value) : DEFAULT_BITS_PER_SAMPLE;

        value = null;
        if (taskDesc.getParams().containsKey(PARAM_SAMPLE_RATE)) {
            value = ((JsonElement) taskDesc.getParams().get(PARAM_SAMPLE_RATE)).getAsString();
        }
        this.sampleRate = value != null ? Integer.valueOf(value) : DEFAULT_SAMPLE_RATE;

        value = null;
        if (taskDesc.getParams().containsKey(PARAM_PAYLOAD)) {
            value = ((JsonElement) taskDesc.getParams().get(PARAM_PAYLOAD)).getAsString();
        }
        this.payloadType = value != null ? PayloadType.getByCodecValue(Integer.valueOf(value), DEFAULT_PAYLOAD_TYPE) : DEFAULT_PAYLOAD_TYPE;
    }

    /**
     *
     */
    public QoSTestResult call() throws Exception {
        final AtomicInteger ssrc = new AtomicInteger(-1);
        final QoSTestResult result = initQoSTestResult(QoSTestResultEnum.VOIP);

        result.getResultMap().put(RESULT_BITS_PER_SAMPLE, bitsPerSample);
        result.getResultMap().put(RESULT_CALL_DURATION, callDuration);
        result.getResultMap().put(RESULT_DELAY, delay);
        result.getResultMap().put(RESULT_IN_PORT, incomingPort);
        result.getResultMap().put(RESULT_OUT_PORT, outgoingPort);
        result.getResultMap().put(RESULT_SAMPLE_RATE, sampleRate);
        result.getResultMap().put(RESULT_PAYLOAD, payloadType.getValue());
        result.getResultMap().put(RESULT_STATUS, "OK");

        try {
            onStart(result);

            final Random r = new Random();
            final int initialSequenceNumber = r.nextInt(10000);
            final CountDownLatch latch = new CountDownLatch(1);
            final Map<Integer, RtpControlData> rtpControlDataList = new HashMap<Integer, RtpUtil.RtpControlData>();

            final ControlConnectionResponseCallback callback = new ControlConnectionResponseCallback() {

                public void onResponse(String response, String request) {
                    Timber.e("Ignore errors: %s", ignoreErrors);
                    if (response != null && response.startsWith("OK")) {
                        final Matcher m = VOIP_OK_PATTERN.matcher(response);
                        if (m.find()) {
                            DatagramSocket dgsock = null;
                            try {
                                ssrc.set(Integer.parseInt(m.group(1)));
                                dgsock = new DatagramSocket();

                                final UdpStreamCallback receiveCallback = new UdpStreamCallback() {

                                    public boolean onSend(DataOutputStream dataOut, int packetNumber)
                                            throws IOException {
                                        //nothing to do here
                                        return true;
                                    }

                                    public synchronized void onReceive(DatagramPacket dp) throws IOException {
                                        final long receivedNs = System.nanoTime();
                                        final byte[] data = dp.getData();
                                        try {
                                            final RtpPacket rtp = new RtpPacket(data);
                                            rtpControlDataList.put(rtp.getSequnceNumber(), new RtpControlData(rtp, receivedNs));
                                        } catch (RtpException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    public void onBind(Integer port)
                                            throws IOException {
                                        result.getResultMap().put(RESULT_IN_PORT, port);
                                    }
                                };

                                RtpUtil.runVoipStream(null, true, InetAddress.getByName(getTestServerAddr()), outgoingPort, incomingPort, sampleRate, bitsPerSample,
                                        payloadType, initialSequenceNumber, ssrc.get(),
                                        TimeUnit.MILLISECONDS.convert(callDuration, TimeUnit.NANOSECONDS),
                                        TimeUnit.MILLISECONDS.convert(delay, TimeUnit.NANOSECONDS),
                                        TimeUnit.MILLISECONDS.convert(timeout, TimeUnit.NANOSECONDS), true, receiveCallback);

                            } catch (InterruptedException e) {
                                if (!ignoreErrors) {
                                    result.getResultMap().put(RESULT_STATUS, "TIMEOUT");
                                    e.printStackTrace();
                                    FirebaseCrashlytics.getInstance().recordException(new Exception("VoipTask - Interrupted - timeout"));
                                }

                            } catch (TimeoutException e) {
                                if (!ignoreErrors) {
                                    result.getResultMap().put(RESULT_STATUS, "TIMEOUT");
                                    e.printStackTrace();
                                    try {
                                        FirebaseCrashlytics.getInstance().recordException(new Exception("VoipTask - timeout"));
                                    } catch (Exception e1) {
                                        //do nothing
                                    }
                                }
                            } catch (Exception e) {
                                if (!ignoreErrors) {
                                    result.getResultMap().put(RESULT_STATUS, "ERROR");
                                    e.printStackTrace();
                                    try {
                                        FirebaseCrashlytics.getInstance().recordException(new Exception("VoipTask - error"));
                                    } catch (Exception e1) {
                                        //do nothing
                                    }
                                }
                            } finally {
                                if (dgsock != null && !dgsock.isClosed()) {
                                    dgsock.close();
                                }
                            }
                        }
                    } else {
                        if (!ignoreErrors) {
                            result.getResultMap().put(RESULT_STATUS, "ERROR");
                        }
                    }

                    latch.countDown();
                }
            };

	    	/*
             * syntax: VOIPTEST 0 1 2 3 4 5 6 7
	    	 * 	0 = outgoing port (server port)
	    	 * 	1 = incoming port (client port) 
	    	 *  2 = sample rate (in Hz)
	    	 * 	3 = bits per sample
	    	 * 	4 = packet delay in ms 
	    	 * 	5 = call duration (test duration) in ms 
	    	 * 	6 = starting sequence number (see rfc3550, rtp header: sequence number)
	    	 *  7 = payload type
	    	 */
            sendCommand("VOIPTEST " + outgoingPort + " " + incomingPort + " " + sampleRate + " " + bitsPerSample + " "
                    + TimeUnit.MILLISECONDS.convert(delay, TimeUnit.NANOSECONDS) + " "
                    + TimeUnit.MILLISECONDS.convert(callDuration, TimeUnit.NANOSECONDS) + " "
                    + initialSequenceNumber + " " + payloadType.getValue(), callback);

            //wait for countdownlatch or timeout:
            latch.await(timeout, TimeUnit.NANOSECONDS);

            //if rtpreceivestream did not finish cancel the task
			/*
			if (!rtpInTimeoutTask.isDone()) {
				rtpInTimeoutTask.cancel(true);
			}
			*/

            final CountDownLatch resultLatch = new CountDownLatch(1);

            final ControlConnectionResponseCallback incomingResultRequestCallback = new ControlConnectionResponseCallback() {

                public void onResponse(final String response, final String request) {
                    if (response != null && response.startsWith("VOIPRESULT")) {
                        System.out.println(response);
                        Matcher m = VOIP_RECEIVE_RESPONSE_PATTERN.matcher(response);
                        if (m.find()) {
                            final String prefix = RESULT_VOIP_PREFIX + RESULT_OUTGOING_PREFIX;
                            result.getResultMap().put(prefix + RESULT_MAX_JITTER, Long.parseLong(m.group(1)));
                            result.getResultMap().put(prefix + RESULT_MEAN_JITTER, Long.parseLong(m.group(2)));
                            result.getResultMap().put(prefix + RESULT_MAX_DELTA, Long.parseLong(m.group(3)));
                            result.getResultMap().put(prefix + RESULT_SKEW, Long.parseLong(m.group(4)));
                            result.getResultMap().put(prefix + RESULT_NUM_PACKETS, Long.parseLong(m.group(5)));
                            result.getResultMap().put(prefix + RESULT_SEQUENCE_ERRORS, Long.parseLong(m.group(6)));
                            result.getResultMap().put(prefix + RESULT_SHORT_SEQUENTIAL, Long.parseLong(m.group(7)));
                            result.getResultMap().put(prefix + RESULT_LONG_SEQUENTIAL, Long.parseLong(m.group(8)));
                        }
                        resultLatch.countDown();
                    }
                }
            };

            //wait a short amount of time until requesting results
            Thread.sleep(100);
            //request server results:
            if (ssrc.get() >= 0) {
                sendCommand("GET VOIPRESULT " + ssrc.get(), incomingResultRequestCallback);
                resultLatch.await(CONTROL_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
            }

            final RtpQoSResult rtpResults = rtpControlDataList.size() > 0 ? RtpUtil.calculateQoS(rtpControlDataList, initialSequenceNumber, sampleRate) : null;

            final String prefix = RESULT_VOIP_PREFIX + RESULT_INCOMING_PREFIX;
            if (rtpResults != null) {
                result.getResultMap().put(prefix + RESULT_MAX_JITTER, rtpResults.getMaxJitter());
                result.getResultMap().put(prefix + RESULT_MEAN_JITTER, rtpResults.getMeanJitter());
                result.getResultMap().put(prefix + RESULT_MAX_DELTA, rtpResults.getMaxDelta());
                result.getResultMap().put(prefix + RESULT_SKEW, rtpResults.getSkew());
                result.getResultMap().put(prefix + RESULT_NUM_PACKETS, rtpResults.getReceivedPackets());
                result.getResultMap().put(prefix + RESULT_SEQUENCE_ERRORS, rtpResults.getOutOfOrder());
                result.getResultMap().put(prefix + RESULT_SHORT_SEQUENTIAL, rtpResults.getMinSequential());
                result.getResultMap().put(prefix + RESULT_LONG_SEQUENTIAL, rtpResults.getMaxSequencial());
            } else {
                result.getResultMap().put(prefix + RESULT_MAX_JITTER, null);
                result.getResultMap().put(prefix + RESULT_MEAN_JITTER, null);
                result.getResultMap().put(prefix + RESULT_MAX_DELTA, null);
                result.getResultMap().put(prefix + RESULT_SKEW, null);
                result.getResultMap().put(prefix + RESULT_NUM_PACKETS, 0);
                result.getResultMap().put(prefix + RESULT_SEQUENCE_ERRORS, null);
                result.getResultMap().put(prefix + RESULT_SHORT_SEQUENTIAL, null);
                result.getResultMap().put(prefix + RESULT_LONG_SEQUENTIAL, null);
            }

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            onEnd(result);
        }
    }

    /*
     * (non-Javadoc)
     * @see at.specure.client.v2.task.AbstractRmbtTask#initTask()
     */
    @Override
    public void initTask() {

    }

    /*
     * (non-Javadoc)
     * @see at.specure.client.v2.task.QoSTask#getTestType()
     */
    public QoSTestResultEnum getTestType() {
        return QoSTestResultEnum.VOIP;
    }

    /*
     * (non-Javadoc)
     * @see at.specure.client.v2.task.QoSTask#needsQoSControlConnection()
     */
    public boolean needsQoSControlConnection() {
        return true;
    }
}
