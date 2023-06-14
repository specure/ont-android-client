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
package at.specure.client.helper;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import at.specure.androidX.data.test.testResultRequest.TestResultRequest;
import at.specure.client.TestParameter;
import at.specure.client.TotalTestResult;
import at.specure.client.ndt.UiServicesAdapter;
import at.specure.client.v2.task.TaskDesc;
import timber.log.Timber;

public class ControlServerConnection
{

    // url to make request
    private URI hostUri;

    private boolean testEncryption;

    private final JsonParser jParser;

    private static String testToken = "";
    private String testId = "";
    private String testUuid = "";

    private long testTime = 0;

    private String testHost = "";
    private int testPort = 0;
    private String remoteIp = "";
    private String serverName;
    private String provider;

    private int testDuration = 0;
    private int testNumThreads = 0;
    private Integer testNumPings = null;

    private String clientUUID = "";

    private URI resultURI;
    private URI resultQoSURI;

    private String errorMsg = null;

    private boolean hasError = false;

    public TaskDesc udpTaskDesc;
    public TaskDesc dnsTaskDesc;
    public TaskDesc ntpTaskDesc;
    public TaskDesc httpTaskDesc;
    public TaskDesc tcpTaskDesc;

    public List<TaskDesc> v2TaskDesc;
    private long startTimeNs = 0;
    public int qosServerId = -1;

    public ControlServerConnection()
    {

        // Creating JSON Parser instance
        jParser = new JsonParser();

    }

    private static URI getUri(final boolean encryption, final String host, final String pathPrefix, final int port,
                              final String path)
    {
        try
        {
            final String protocol = encryption ? "https" : "http";
            final int defaultPort = encryption ? 443 : 80;
            final String totalPath = (pathPrefix != null ? pathPrefix : "") + Config.RMBT_CONTROL_PATH + path;

            if (defaultPort == port)
                return new URL(protocol, host, totalPath).toURI();
            else
                return new URL(protocol, host, port, totalPath).toURI();
        }
        catch (final MalformedURLException e)
        {
            return null;
        }
        catch (final URISyntaxException e)
        {
            return null;
        }
    }

    public static String getTestToken() {
        return testToken;
    }

    /**
     * requests the parameters for the v2 tests
     * @param host
     * @param pathPrefix
     * @param port
     * @param encryption
     * @param geoInfo
     * @param uuid
     * @param clientType
     * @param clientName
     * @param clientVersion
     * @param additionalValues
     * @return
     */
    public String requestQoSTestParameters(final String host, final String pathPrefix, final int port,
                                           final boolean encryption, final ArrayList<String> geoInfo, final String uuid, final String clientType,
                                           final String clientName, final String clientVersion, final JsonObject additionalValues, String language)
    {
        clientUUID = uuid;

        hostUri = getUri(encryption, host, pathPrefix, port, Config.RMBT_CONTROL_V2_TESTS);

        System.out.println("Connection to " + hostUri);

        Gson gson = new Gson();
        final JsonObject regData = new JsonObject();

        try
        {
            regData.add("uuid", new JsonPrimitive(uuid));
            regData.add("client", new JsonPrimitive(clientName));
            regData.add("version", new JsonPrimitive(Config.RMBT_VERSION_NUMBER));
            regData.add("type", new JsonPrimitive(clientType));
            regData.add("softwareVersion", new JsonPrimitive(clientVersion));
            regData.add("softwareRevision", new JsonPrimitive(RevisionHelper.getVerboseRevision()));
            regData.add("language", new JsonPrimitive(language));
            regData.add("timezone", new JsonPrimitive(TimeZone.getDefault().getID()));
            regData.add("time", new JsonPrimitive(System.currentTimeMillis()));

            if (geoInfo != null)
            {
                final JsonObject locData = new JsonObject();
                locData.add("time", new JsonPrimitive(geoInfo.get(0)));
                locData.add("lat", new JsonPrimitive(geoInfo.get(1)));
                locData.add("long", new JsonPrimitive(geoInfo.get(2)));
                locData.add("accuracy", new JsonPrimitive(geoInfo.get(3)));
                locData.add("altitude", new JsonPrimitive(geoInfo.get(4)));
                locData.add("bearing", new JsonPrimitive(geoInfo.get(5)));
                locData.add("speed", new JsonPrimitive(geoInfo.get(6)));
                locData.add("provider", new JsonPrimitive(geoInfo.get(7)));

                if (regData.has("location")) {
                    JsonElement element = regData.get("location");
                    if (element.isJsonArray()) {
                        JsonArray array = element.getAsJsonArray();
                        regData.remove("location");
                        array.add(locData);
                        regData.add("location", array);
                    } else {
                        JsonObject object = element.getAsJsonObject();
                        JsonArray array = new JsonArray();
                        regData.remove("location");
                        array.add(object);
                        regData.add("location", array);
                    }
                } else {
                    regData.add("location", locData);
                }


            }

            addToJSONObject(regData, additionalValues);

        }
        catch (final JsonParseException e1)
        {
            hasError = true;
            errorMsg = "Error gernerating request";
            // e1.printStackTrace();
        }

        // getting JSON string from URL
        final JsonObject response = jParser.sendJsonToUrl(hostUri, regData);

        if (response != null)
            try
            {
                final JsonArray errorList = response.getAsJsonArray("error");

                // System.out.println(response.toString(4));

                if (errorList.size() == 0)
                {

                    int testPort = 5233;

                    Map<String, Object> testParams = null;

//                    if (response.has("qos_server_id")) {
//                        qosServerId = response.get("qos_server_id").getAsInt();
//                    } else {
//                        qosServerId = -1;
//                    }

                    Iterator<Entry<String, JsonElement>> objectives = response.getAsJsonObject("objectives").entrySet().iterator();
                    v2TaskDesc = new ArrayList<TaskDesc>();

                    while (objectives.hasNext()) {
                        Entry<String, JsonElement> next = objectives.next();
                        JsonElement value = next.getValue();
                        if (value.isJsonArray()) {
                            JsonArray asJsonArray = value.getAsJsonArray();
                            int size = asJsonArray.size();
                            for (JsonElement element: asJsonArray) {
                                Iterator<Entry<String, JsonElement>> iterator = element.getAsJsonObject().entrySet().iterator();
                                HashMap<String, Object> stringObjectHashMap = new HashMap<>();
                                while (iterator.hasNext()) {

                                    Entry<String, JsonElement> next1 = iterator.next();
                                    stringObjectHashMap.put(next1.getKey(), next1.getValue());

                                }
                                TaskDesc taskDesc = new TaskDesc(testHost, testPort, encryption, testToken, 0, 1, 0, System.nanoTime(), stringObjectHashMap, next.getKey());
                                v2TaskDesc.add(taskDesc);
                            }
                        }
                    }

                }
                else
                {
                    hasError = true;
                    for (int i = 0; i < errorList.size(); i++)
                    {
                        if (i > 0)
                            errorMsg += "\n";
                        errorMsg += errorList.get(i).getAsString();
                    }
                }

                // }
            }
            catch (final JsonParseException e)
            {
                hasError = true;
                errorMsg = "Error parsing server response";
                e.printStackTrace();
            }
        else
        {
            hasError = true;
            errorMsg = "No response";
        }

        return errorMsg;
    }

    public String requestNewTestConnection(final String host, final String pathPrefix, final int port, int measurementServerId,
                                           final boolean encryption, final ArrayList<String> geoInfo, final String uuid, final String clientType,
                                           final String clientName, final String clientVersion, final JsonObject additionalValues, String language)
    {

        String errorMsg = null;
        // url to make request to

        clientUUID = uuid;

        hostUri = getUri(encryption, host, pathPrefix, port, Config.RMBT_CONTROL_MAIN_URL);

        System.out.println("Connection to " + hostUri);

        Gson gson = new Gson();

        final JsonObject regData = new JsonObject();

        try
        {
            regData.add("uuid", new JsonPrimitive(uuid));
            regData.add("client", new JsonPrimitive(clientName));
            regData.add("version", new JsonPrimitive(Config.RMBT_VERSION_NUMBER));
            regData.add("type", new JsonPrimitive(clientType));
            regData.add("softwareVersion", new JsonPrimitive(clientVersion));
            regData.add("softwareRevision", new JsonPrimitive(RevisionHelper.getVerboseRevision()));
            regData.add("language", new JsonPrimitive(language));
            regData.add("timezone", new JsonPrimitive(TimeZone.getDefault().getID()));
            regData.add("time", new JsonPrimitive(System.currentTimeMillis()));
            regData.add("measurement_server_id", new JsonPrimitive(measurementServerId));
            startTimeNs = System.nanoTime();

            // TODO: remake as object filled once (then no need of accumulate method)
            if (geoInfo != null)
            {
                final JsonObject locData = new JsonObject();
                locData.add("time", new JsonPrimitive(geoInfo.get(0)));
                locData.add("lat", new JsonPrimitive(geoInfo.get(1)));
                locData.add("long", new JsonPrimitive(geoInfo.get(2)));
                locData.add("accuracy", new JsonPrimitive(geoInfo.get(3)));
                locData.add("altitude", new JsonPrimitive(geoInfo.get(4)));
                locData.add("bearing", new JsonPrimitive(geoInfo.get(5)));
                locData.add("speed", new JsonPrimitive(geoInfo.get(6)));
                locData.add("provider", new JsonPrimitive(geoInfo.get(7)));

                if (regData.has("location")) {
                    JsonElement element = regData.get("location");
                    if (element.isJsonArray()) {
                        JsonArray array = element.getAsJsonArray();
                        regData.remove("location");
                        array.add(locData);
                        regData.add("location", array);
                    } else {
                        JsonObject object = element.getAsJsonObject();
                        JsonArray array = new JsonArray();
                        regData.remove("location");
                        array.add(object);
                        regData.add("location", array);
                    }
                } else {
                    regData.add("location", locData);
                }
            }

            addToJSONObject(regData, additionalValues);

        }
        catch (final JsonParseException e1)
        {
            errorMsg = "Error gernerating request";
            // e1.printStackTrace();
        }

        // getting JSON string from URL
        final JsonObject response = jParser.sendJsonToUrl(hostUri, regData);

        if (response != null)
            try
            {
                final JsonArray errorList = response.getAsJsonArray("error");

                // System.out.println(response.toString(4));

                if (errorList.size() == 0)
                {
                    clientUUID = "";
                    if (response.has("uuid")) {
                        clientUUID = response.get("uuid").getAsString();
                    }

                    testToken = response.get("test_token").getAsString();

                    testId = response.get("test_id").getAsString();
                    testUuid = response.get("test_uuid").getAsString();

                    testTime = System.currentTimeMillis() + 1000 * response.get("test_wait").getAsLong();

                    testHost = response.get("test_server_address").getAsString();
                    testPort = response.get("test_server_port").getAsInt();
                    testEncryption = response.get("test_server_encryption").getAsBoolean();

                    serverName = null;
                    provider = null;

                    if (response.has("test_server_name"))
                        serverName = response.get("test_server_name").getAsString();
                    if (response.has("provider"))
                        provider = response.get("provider").getAsString();

                    testDuration = response.get("test_duration").getAsInt();
                    testNumThreads = response.get("test_numthreads").getAsInt();

                    testNumPings = 10;
                    if (response.has("test_numpings")) {
                        testNumPings = response.get("test_numpings").getAsInt();
                    }

                    remoteIp = response.get("client_remote_ip").getAsString();

                    resultURI = new URI(response.get("result_url").getAsString());
                    resultQoSURI = new URI(response.get("result_qos_url").getAsString());
                }
                else
                {
                    errorMsg = "";
                    for (int i = 0; i < errorList.size(); i++)
                    {
                        if (i > 0)
                            errorMsg += "\n";
                        errorMsg += errorList.get(i).getAsString();
                    }
                }

                // }
            }
            catch (final JsonParseException e)
            {
                errorMsg = "Error parsing server response";
                e.printStackTrace();
            }
            catch (final URISyntaxException e)
            {
                errorMsg = "Error parsing server response";
                e.printStackTrace();
            }
        else
            errorMsg = "No response";
        return errorMsg;
    }

    public String sendTestResult(TestResultRequest result)
    {
        String errorMsg = null;
        if (resultURI != null)
        {
            Gson gson = new Gson();

            final JsonObject testData = gson.toJsonTree(result).getAsJsonObject();


            Timber.e("TEST RESULTS sendTestResult: %s", testData);
            Timber.e("5G TEST RESULTS nr_connection_state: %s", result.nrConnectionState);
            Timber.e("5G TEST RESULTS network_type: %s", result.networkType);
            // getting JSON string from URL
            final JsonObject response = jParser.sendJsonToUrl(resultURI, testData);

            if (response != null)
                try
                {
                    final JsonArray errorList = response.getAsJsonArray("error");

                    // System.out.println(response.toString(4));

                    if (errorList.size() == 0)
                    {

                        // System.out.println("All is fine");

                    }
                    else
                    {
                        for (int i = 0; i < errorList.size(); i++)
                        {
                            if (i > 0)
                                errorMsg += "\n";
                            errorMsg += errorList.get(i).getAsString();
                        }
                    }

                    // }
                }
                catch (final JsonParseException e)
                {
                    errorMsg = "Error parsing server response";
                    e.printStackTrace();
                }
        }
        else
            errorMsg = "No URL to send the Data to.";

        return errorMsg;
    }


    /**
     *
     * @param result
     * @param qosTestResult
     * @return
     */
    public String sendQoSResult(final TotalTestResult result, final JsonArray qosTestResult, String language)
    {
        String errorMsg = null;
        System.out.println("sending qos results to " + resultQoSURI);
        if (resultQoSURI != null)
        {

            Gson gson = new Gson();
            final JsonObject testData = new JsonObject();

            try
            {
                testData.add("client_uuid", new JsonPrimitive(clientUUID));
                testData.add("client_name", new JsonPrimitive(Config.RMBT_CLIENT_NAME));
                testData.add("client_version", new JsonPrimitive(Config.RMBT_VERSION_NUMBER));
                testData.add("client_language", new JsonPrimitive(language));

                testData.add("time", new JsonPrimitive(System.currentTimeMillis()));

                testData.add("test_token", new JsonPrimitive(testToken));

//                testData.add("qos_server_id", new JsonPrimitive(qosServerId));

                testData.add("qos_result", qosTestResult);
            }
            catch (final JsonParseException e1)
            {
                errorMsg = "Error gernerating request";
                e1.printStackTrace();
            }

            // getting JSON string from URL
            final JsonObject response = jParser.sendJsonToUrl(resultQoSURI, testData);

            if (response != null)
                try
                {
                    final JsonArray errorList = response.getAsJsonArray("error");

                    // System.out.println(response.toString(4));

                    if (errorList.size() == 0)
                    {

                        // System.out.println("All is fine");

                    }
                    else
                    {
                        for (int i = 0; i < errorList.size(); i++)
                        {
                            if (i > 0)
                                errorMsg += "\n";
                            errorMsg += errorList.get(i).getAsString();
                        }
                    }

                    // }
                }
                catch (final JsonParseException e)
                {
                    errorMsg = "Error parsing server response";
                    e.printStackTrace();
                }
        }
        else
            errorMsg = "No URL to send the Data to.";

        return errorMsg;
    }

    public void sendNDTResult(final String host, final String pathPrefix, final int port, final boolean encryption,
                              final String clientUUID, final UiServicesAdapter data, final String testUuide, String language)
    {
        hostUri = getUri(encryption, host, pathPrefix, port, Config.RMBT_CONTROL_MAIN_URL);
        this.clientUUID = clientUUID;
        sendNDTResult(data, testUuid, language);
    }

    public void sendNDTResult(final UiServicesAdapter data, final String testUuid, String language)
    {
        final JsonObject testData = new JsonObject();
        Gson gson = new Gson();
        try
        {
            testData.add("client_uuid", new JsonPrimitive(clientUUID));
            testData.add("client_language", new JsonPrimitive(language));
            if (testUuid != null)
                testData.add("test_uuid", new JsonPrimitive(testUuid));
            else
                testData.add("test_uuid", new JsonPrimitive(this.testUuid));
            testData.add("s2cspd", new JsonPrimitive(data.s2cspd));
            testData.add("c2sspd", new JsonPrimitive(data.c2sspd));
            testData.add("avgrtt", new JsonPrimitive(data.avgrtt));
            testData.add("main", new JsonPrimitive(data.sbMain.toString()));
            testData.add("stat", new JsonPrimitive(data.sbStat.toString()));
            testData.add("diag", new JsonPrimitive(data.sbDiag.toString()));
            testData.add("time_ns", new JsonPrimitive(data.getStartTimeNs() - startTimeNs));
            testData.add("time_end_ns", new JsonPrimitive(data.getStopTimeNs() - startTimeNs));

            jParser.sendJsonToUrl(hostUri.resolve(Config.RMBT_CONTROL_NDT_RESULT_URL), testData);

            System.out.println(testData);
        }
        catch (final JsonParseException e)
        {
            e.printStackTrace();
        }
    }

    public static void addToJSONObject(final JsonObject data, final JsonObject additionalValues) throws JsonParseException
    {
        if (additionalValues != null && additionalValues.entrySet().size() > 0)
        {
            Iterator<Entry<String, JsonElement>> iterator = additionalValues.entrySet().iterator();

            while (iterator.hasNext()) {
                Entry<String, JsonElement> next = iterator.next();
                data.add(next.getKey(), next.getValue());
            }

        }
    }

    public String getRemoteIp()
    {
        return remoteIp;
    }

    public String getClientUUID()
    {
        return clientUUID;
    }

    public String getServerName()
    {
        return serverName;
    }

    public String getProvider()
    {
        return provider;
    }

    public long getTestTime()
    {
        return testTime;
    }

    public long getStartTimeNs() {
        return startTimeNs;
    }

    public String getTestId()
    {
        return testId;
    }

    public String getTestUuid()
    {
        return testUuid;
    }

    public TestParameter getTestParameter(TestParameter overrideParams)
    {
        String host = testHost;
        int port = testPort;
        boolean encryption = testEncryption;
        int duration = testDuration;
        int numThreads = testNumThreads;
        int numPings = testNumPings;

        if (overrideParams != null)
        {
            if (overrideParams.getHost() != null && overrideParams.getPort() > 0)
            {
                host = overrideParams.getHost();
                encryption = overrideParams.isEncryption();
                port = overrideParams.getPort();
            }
            if (overrideParams.getDuration() > 0)
                duration = overrideParams.getDuration();
            if (overrideParams.getNumThreads() > 0)
                numThreads = overrideParams.getNumThreads();
        }
        return new TestParameter(host, port, encryption, testToken, duration, numThreads, numPings, testTime);
    }

}
