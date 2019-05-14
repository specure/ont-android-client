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

import android.content.Context;
import android.os.Build;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.xbill.DNS.A6Record;
import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.CNAMERecord;
import org.xbill.DNS.MXRecord;
import org.xbill.DNS.Rcode;
import org.xbill.DNS.Record;
import org.xbill.DNS.ResolverConfig;
import org.xbill.DNS.Section;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import at.specure.client.QualityOfServiceTest;
import at.specure.client.helper.Dig;
import at.specure.client.helper.Dig.DnsRequest;
import at.specure.client.v2.task.result.QoSTestResult;
import at.specure.client.v2.task.result.QoSTestResultEnum;
import at.specure.minidns.core.record.A;
import at.specure.minidns.core.record.AAAA;
import at.specure.minidns.core.record.CNAME;
import at.specure.minidns.core.record.Data;
import at.specure.minidns.core.record.MX;
import at.specure.minidns.hla.minidns.hla.ResolverApi;
import at.specure.minidns.hla.minidns.hla.ResolverResult;

public class DnsTask extends AbstractQoSTask {

    public final static long DEFAULT_TIMEOUT = 5000000000L;

    private String record;

    private String host;

    private String resolver;

    private Context context;

    private final long timeout;

    public final static String PARAM_DNS_HOST = "host";

    public final static String PARAM_DNS_RESOLVER = "resolver";

    public final static String PARAM_DNS_RECORD = "record";

    public final static String PARAM_DNS_TIMEOUT = "timeout";

    public final static String RESULT_STATUS = "dns_result_status";

    public final static String RESULT_ENTRY = "dns_result_entries";

    public final static String RESULT_TTL = "dns_result_ttl";

    public final static String RESULT_ADDRESS = "dns_result_address";

    public final static String RESULT_PRIORITY = "dns_result_priority";

    public final static String RESULT_DURATION = "dns_result_duration";

    public final static String RESULT_QUERY = "dns_result_info";

    public final static String RESULT_RESOLVER = "dns_objective_resolver";

    public final static String RESULT_DNS_HOST = "dns_objective_host";

    public final static String RESULT_DNS_RECORD = "dns_objective_dns_record";

    public final static String RESULT_DNS_TIMEOUT = "dns_objective_timeout";

    public final static String RESULT_DNS_ENTRIES_FOUND = "dns_result_entries_found";

    /**
     * @param taskDesc
     */
    public DnsTask(QualityOfServiceTest nnTest, TaskDesc taskDesc, int threadId, Context context) {
        super(nnTest, taskDesc, threadId, threadId);

        this.context = context;

        this.record = null;
        if (taskDesc.getParams().containsKey(PARAM_DNS_RECORD)) {
            this.record = ((JsonElement) taskDesc.getParams().get(PARAM_DNS_RECORD)).getAsString();
        }

        this.host = null;
        if (taskDesc.getParams().containsKey(PARAM_DNS_HOST)) {
            this.host = ((JsonElement) taskDesc.getParams().get(PARAM_DNS_HOST)).getAsString();
        }

        this.resolver = null;
        if (taskDesc.getParams().containsKey(PARAM_DNS_RESOLVER)) {
            this.resolver = ((JsonElement) taskDesc.getParams().get(PARAM_DNS_RESOLVER)).getAsString();
        }

        String value = null;
        if (taskDesc.getParams().containsKey(PARAM_DNS_TIMEOUT)) {
            value = ((JsonElement) taskDesc.getParams().get(PARAM_DNS_TIMEOUT)).getAsString();
        }
        this.timeout = value != null ? Long.valueOf(value) : DEFAULT_TIMEOUT;
    }

    /**
     *
     */
    public QoSTestResult call() throws Exception {
        final QoSTestResult testResult = initQoSTestResult(QoSTestResultEnum.DNS);

        try {
            onStart(testResult);
            List<JsonObject> dnsResult2 = new ArrayList<>();
            List<JsonObject> dnsResult = null;
            long start = System.nanoTime();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                final ArrayList<JsonObject> resultJson = new ArrayList<>();
                final Class<? extends Data> resolverClass = resolveRecordType(record);

                final long startTime = System.currentTimeMillis();

//                ResolverResult<?> result = null;
                try {
                    ResolverResult<? extends Data> resolverResult = ResolverApi.getInstance(context).resolve(host, resolverClass);/*, new QueryEndListener() {
                        //            ResolverApi.getInstance(this).resolveAsync(host, A.class, new QueryEndListener() {
                        @Override
                        public void queryEndListener(DNSMessage message) {
                        }

                        @Override
                        public <D extends Data> void queryEndListener(ResolverResult<D> result, Exception e) {

                            if (e instanceof TimeoutException) {
                                // TIMEOUT
                                Timber.e("DNS", "TIMEOUT");
                            }

                            long endTime = System.currentTimeMillis();

                            long duration = endTime - startTime;
*/
                    /*if (!result.wasSuccessful()) {
                        DNSMessage.RESPONSE_CODE responseCode = result.getResponseCode();
                        // Perform error handling.
                        return;
                    }
                    if (!result.isAuthenticData()) {
                        // Response was not secured with DNSSEC.
                        return;
                    }*/

                    String name = resolverClass.getName();

//                    if ((result != null) && (result.wasSuccessful())) {
//                        Set answers = (Set<?>) result.getAnswers();
//                        for (Object a : answers) {
//                            if (a instanceof A) {
//                                InetAddress inetAddress = ((A) a).getInetAddress();
//                            }
//
//                            // Do someting with the InetAddress, e.g. connect to.
//                        }
//                    }

                    // Result handling
                    if ((resolverResult != null) && (resolverResult.wasSuccessful())) {
                        testResult.getResultMap().put(RESULT_QUERY, "OK");
                        //dnsLookup = new Lookup(domainName, Type.value(record.toUpperCase()));
                        //dnsLookup.setResolver(new SimpleResolver(resolver));
                        //Record[] records = dnsLookup.run();
                        testResult.getResultMap().put(RESULT_STATUS, resolverResult.getResponseCode());
                        if (resolverResult.wasSuccessful()) {
                            Set records = resolverResult.getAnswers();
                            if (records != null && records.size() > 0) {
                                int i = 0;
                                for (Object a : records) {
                                    JsonObject dnsEntry = new JsonObject();
                                    i++;
                                    {
                                        if (a instanceof MX) {
                                            dnsEntry.addProperty(RESULT_PRIORITY, ((MX) a).priority);//.getPriority()));
                                            dnsEntry.addProperty(RESULT_ADDRESS, ((MX) a).target.toString());
                                        } else if (a instanceof CNAME) {
                                            dnsEntry.addProperty(RESULT_ADDRESS, ((CNAME) a).getTarget().toString());
                                        } else if (a instanceof A) {
                                            dnsEntry.addProperty(RESULT_ADDRESS, ((A) a).getInetAddress().getHostAddress());
                                        } else if (a instanceof AAAA) {
                                            dnsEntry.addProperty(RESULT_ADDRESS, ((AAAA) a).getInetAddress().getHostAddress());
//                                    } else if (a instanceof A6Record) {
//                                        dnsEntry.put(RESULT_ADDRESS, ((A6Record) a).getSuffix().toString());
                                        } else {
                                            dnsEntry.addProperty(RESULT_ADDRESS, ((Data) a).toByteArray().toString());
                                        }

//                                        dnsEntry.put(RESULT_TTL, String.valueOf(((Data) a).getTTL()));

                                        at.specure.minidns.core.Record<? extends Data> record1 = resolverResult.getRawAnswer().answerSection.get(i - 1);
                                        dnsEntry.addProperty(RESULT_TTL, record1.ttl);

                                        //result.add(records[i].toString());
                                        dnsResult2.add(dnsEntry);
                                        System.out.println("record " + i + " toString: " + a.toString());
                                    }
                                }
                            }
                        }
                        long duration = System.nanoTime() - start;
                        if (duration > timeout) {
                            testResult.getResultMap().put(RESULT_QUERY, "TIMEOUT");
                        } else {
                            if (dnsResult2 != null && dnsResult2.size() > 0)
                                testResult.getResultMap().put(RESULT_ENTRY, dnsResult2);
                            if (dnsResult2 != null) {
                                testResult.getResultMap().put(RESULT_DNS_ENTRIES_FOUND, dnsResult2.size());
                            } else {
                                testResult.getResultMap().put(RESULT_DNS_ENTRIES_FOUND, "0");
                            }
                            //testResult.getResultMap().put(RESULT_DURATION, (duration / 1000000));
                            testResult.getResultMap().put(RESULT_DURATION, duration);
                            testResult.getResultMap().put(RESULT_RESOLVER, resolver != null ? resolver : "Standard");
                            testResult.getResultMap().put(RESULT_DNS_RECORD, record);
                            testResult.getResultMap().put(RESULT_DNS_HOST, host);
                            testResult.getResultMap().put(RESULT_DNS_TIMEOUT, timeout);
                        }
                    } else {
                        testResult.getResultMap().put(RESULT_QUERY, "ERROR");
                        long duration = System.nanoTime() - start;
                        if (dnsResult2 != null && dnsResult2.size() > 0)
                            testResult.getResultMap().put(RESULT_ENTRY, dnsResult2);
                        if (dnsResult2 != null) {
                            testResult.getResultMap().put(RESULT_DNS_ENTRIES_FOUND, dnsResult2.size());
                        } else {
                            testResult.getResultMap().put(RESULT_DNS_ENTRIES_FOUND, "0");
                        }
                        //testResult.getResultMap().put(RESULT_DURATION, (duration / 1000000));
                        testResult.getResultMap().put(RESULT_DURATION, duration);
                        testResult.getResultMap().put(RESULT_RESOLVER, resolver != null ? resolver : "Standard");
                        testResult.getResultMap().put(RESULT_DNS_RECORD, record);
                        testResult.getResultMap().put(RESULT_DNS_HOST, host);
                        testResult.getResultMap().put(RESULT_DNS_TIMEOUT, timeout);
                    }
//                    }, timeout);
                } catch (IOException e) {
                    testResult.getResultMap().put(RESULT_QUERY, "ERROR");
                    long duration = System.nanoTime() - start;
                    if (dnsResult2 != null && dnsResult2.size() > 0)
                        testResult.getResultMap().put(RESULT_ENTRY, dnsResult2);
                    if (dnsResult2 != null) {
                        testResult.getResultMap().put(RESULT_DNS_ENTRIES_FOUND, dnsResult2.size());
                    } else {
                        testResult.getResultMap().put(RESULT_DNS_ENTRIES_FOUND, "0");
                    }
                    //testResult.getResultMap().put(RESULT_DURATION, (duration / 1000000));
                    testResult.getResultMap().put(RESULT_DURATION, duration);
                    testResult.getResultMap().put(RESULT_RESOLVER, resolver != null ? resolver : "Standard");
                    testResult.getResultMap().put(RESULT_DNS_RECORD, record);
                    testResult.getResultMap().put(RESULT_DNS_HOST, host);
                    testResult.getResultMap().put(RESULT_DNS_TIMEOUT, timeout);
                    e.printStackTrace();
                }

            } else {
                dnsResult = lookupDns(host, record, resolver, (int) (timeout / 1000000), testResult);
                if (dnsResult != null && dnsResult.size() > 0)
                    testResult.getResultMap().put(RESULT_ENTRY, dnsResult);
                if (dnsResult == null || dnsResult.size() <= 0) {
                    testResult.getResultMap().put(RESULT_DNS_ENTRIES_FOUND, "0");
                } else {
                    testResult.getResultMap().put(RESULT_DNS_ENTRIES_FOUND, dnsResult.size());
                }
                long duration = System.nanoTime() - start;
                //testResult.getResultMap().put(RESULT_DURATION, (duration / 1000000));
                testResult.getResultMap().put(RESULT_DURATION, duration);
                testResult.getResultMap().put(RESULT_RESOLVER, resolver != null ? resolver : "Standard");
                testResult.getResultMap().put(RESULT_DNS_RECORD, record);
                testResult.getResultMap().put(RESULT_DNS_HOST, host);
                testResult.getResultMap().put(RESULT_DNS_TIMEOUT, timeout);
            }


        } catch (Exception e) {
            throw e;
        } finally {
            onEnd(testResult);
        }

        return testResult;
    }


    public Class<? extends Data> resolveRecordType(String resolver) {
        Class<? extends Data> aClass = null;
        try {
            aClass = (Class<? extends Data>) Class.forName(A.class.getPackage().getName() + "." + resolver, false, context.getClassLoader());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return aClass;
    }

    /**
     * @param domainName
     * @param record
     * @param resolver
     * @return
     */
    public static List<JsonObject> lookupDns(String domainName, String record, String resolver,
                                             int timeout, QoSTestResult testResult) {
        //List<String> result = new ArrayList<String>();
        List<JsonObject> result = new ArrayList<JsonObject>();

        //Lookup dnsLookup = null;
        try {
            System.out.println("dns lookup: record = " + record + " for host: " + domainName + ", using resolver:" + resolver);

            ResolverConfig.refresh(); // refresh dns server

            DnsRequest req = Dig.doRequest(domainName, record, resolver, timeout);

            testResult.getResultMap().put(RESULT_QUERY, "OK");
            //dnsLookup = new Lookup(domainName, Type.value(record.toUpperCase()));
            //dnsLookup.setResolver(new SimpleResolver(resolver));
            //Record[] records = dnsLookup.run();
            testResult.getResultMap().put(RESULT_STATUS, Rcode.string(req.getResponse().getRcode()));
            if (req.getRequest().getRcode() == Rcode.NOERROR) {
                Record[] records = req.getResponse().getSectionArray(Section.ANSWER);

                if (records != null && records.length > 0) {
                    for (int i = 0; i < records.length; i++) {
                        JsonObject dnsEntry = new JsonObject();
                        if (records[i] instanceof MXRecord) {
                            dnsEntry.addProperty(RESULT_PRIORITY, String.valueOf(((MXRecord) records[i]).getPriority()));
                            dnsEntry.addProperty(RESULT_ADDRESS, ((MXRecord) records[i]).getTarget().toString());
                        } else if (records[i] instanceof CNAMERecord) {
                            dnsEntry.addProperty(RESULT_ADDRESS, ((CNAMERecord) records[i]).getAlias().toString());
                        } else if (records[i] instanceof ARecord) {
                            dnsEntry.addProperty(RESULT_ADDRESS, ((ARecord) records[i]).getAddress().getHostAddress());
                        } else if (records[i] instanceof AAAARecord) {
                            dnsEntry.addProperty(RESULT_ADDRESS, ((AAAARecord) records[i]).getAddress().getHostAddress());
                        } else if (records[i] instanceof A6Record) {
                            dnsEntry.addProperty(RESULT_ADDRESS, ((A6Record) records[i]).getSuffix().toString());
                        } else {
                            dnsEntry.addProperty(RESULT_ADDRESS, records[i].getName().toString());
                        }

                        dnsEntry.addProperty(RESULT_TTL, String.valueOf(records[i].getTTL()));

                        //result.add(records[i].toString());
                        result.add(dnsEntry);
                        System.out.println("record " + i + " toString: " + records[i].toString());
                    }
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } catch (SocketTimeoutException e) {
            testResult.getResultMap().put(RESULT_QUERY, "TIMEOUT");
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            testResult.getResultMap().put(RESULT_QUERY, "ERROR");
            e.printStackTrace();
            return null;
        }

        return result;
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
        return QoSTestResultEnum.DNS;
    }

    /*
     * (non-Javadoc)
     * @see at.specure.client.v2.task.QoSTask#needsQoSControlConnection()
     */
    public boolean needsQoSControlConnection() {
        return false;
    }
}
