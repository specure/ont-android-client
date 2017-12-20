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
package at.specure.android.api;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import at.specure.android.configs.ConfigHelper;
import at.specure.android.screens.map.MapProperties;
import at.specure.android.util.InformationCollector;
import at.specure.client.helper.Config;
import at.specure.client.helper.JSONParser;
import at.specure.android.api.jsons.Location;
import at.specure.android.api.jsons.MeasurementServerGet;
import at.specure.android.api.jsons.ZeroMeasurement;
import at.specure.android.api.jsons.ZeroMeasurementPost;
import at.specure.android.api.reqres.measurement_server.MeasurementServerRq;
import at.specure.android.api.reqres.zero_measurements.ZeroMeasurementsPostRq;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.specure.opennettest.R;

public class ControlServerConnection
{
    
	public static enum UriType {
		DEFAULT_HOSTNAME,
		HOSTNAME_IPV4,
		HOSTNAME_IPV6
	}
	
    private static final String DEBUG_TAG = "ControlServerConnection";
    
    private static String hostname;
    
    private static String hostname4;
    
    private static String hostname6;
    
    private static int port;
    
    private boolean encryption;
    
    private JSONParser jParser;
    
    private Context context;
    
    private String errorMsg = "";
    
    private boolean hasError = false;
    
    private boolean useMapServerPath = false;
    
    private String getUUID()
    {
        return ConfigHelper.getUUID(context.getApplicationContext());
    }
    
    private URI getUri(final String path) {
    	return getUri(path, UriType.DEFAULT_HOSTNAME);
    }
    
    private URI getUri(final String path, final UriType uriType)
    {
        try
        {
            String protocol = encryption ? "https" : "http";
            final int defaultPort = encryption ? 443 : 80;
            final String totalPath;
            if (useMapServerPath)
                totalPath = path;
            else
                totalPath = Config.RMBT_CONTROL_PATH + path;
            
            String host = hostname;
            
            switch(uriType) {
            case HOSTNAME_IPV4:
            	host = hostname4;
            	break;
            case HOSTNAME_IPV6:
            	host = hostname6;
            	break;
            case DEFAULT_HOSTNAME:
            default:
            	host = hostname;
            }
            
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
    
    public ControlServerConnection(final Context context)
    {
        setupServer(context, false);
    }
    
    public ControlServerConnection(final Context context, final boolean useMapServer)
    {
        setupServer(context, useMapServer);
    }
    
    private void setupServer(final Context context, final boolean useMapServerPath)
    {    	 
        jParser = new JSONParser();
        hasError = false;
        
        this.context = context;
        
        this.useMapServerPath = useMapServerPath;
        
        hostname4 = ConfigHelper.getCachedControlServerNameIpv4(context);
        hostname6 = ConfigHelper.getCachedControlServerNameIpv6(context);

        if (useMapServerPath)
        {
            encryption = ConfigHelper.isMapSeverSSL(context);
            hostname = ConfigHelper.getMapServerName(context);
            port = ConfigHelper.getMapServerPort(context);
        }
        else
        {
            encryption = ConfigHelper.isControlSeverSSL(context);
            hostname = ConfigHelper.getControlServerName(context);
            port = ConfigHelper.getControlServerPort(context);
        }
    }
    
    public boolean unload()
    {
        jParser = null;
        
        return true;
    }

    private JsonElement sendRequestElem(final URI hostUrl, final JsonObject requestData, final String fieldName)
    {
        // getting JSON string from URL
        Log.e(DEBUG_TAG, "request to "+ hostUrl + " " + requestData);
        final JsonObject response = jParser.sendJSONToUrl(hostUrl, requestData);

        if (response != null)
            try
            {
                Log.e(DEBUG_TAG, "response to "+ hostUrl + " " + response);
                JsonArray errorList = null;
                if (response.has("error")) {
                    errorList = response.getAsJsonArray("error");
                }

//                if (errorList == null || errorList.size() == 0)

                    if (fieldName != null) {
                        return response.get(fieldName);
                    }
                    else {
                        return response;
                    }


                // }
            }
            catch (final JsonParseException e)
            {
                hasError = true;
                errorMsg = "Error parsing server response";
                e.printStackTrace();
            }
            catch (ClassCastException e) {
                hasError = true;
                errorMsg = "Error parsing server response";
                e.printStackTrace();
            }
        else
        {
            hasError = true;
            errorMsg = "No response";
        }

        return null;

    }

    // HERE UUID KGB
    private JsonArray sendRequest(final URI hostUrl, final JsonObject requestData, final String fieldName)
    {
        // getting JSON string from URL
        Log.e(DEBUG_TAG, "request to "+ hostUrl + " " + requestData);
        final JsonObject response = jParser.sendJSONToUrl(hostUrl, requestData);
        
        if (response != null)
            try
            {
                Log.e(DEBUG_TAG, "response to "+ hostUrl + " " + response);
                JsonArray errorList = null;
                if (response.has("error")) {
                    errorList = response.getAsJsonArray("error");
                }

                if (errorList == null || errorList.size() == 0)
                {
                	return getResponseField(response, fieldName);
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
                  
                    System.out.println(errorMsg);
                    
                    //return getResponseField(response, fieldName);
                }
                
                // }
            }
            catch (final JsonParseException e)
            {
                hasError = true;
                errorMsg = "Error parsing server response";
                e.printStackTrace();
            }
            catch (ClassCastException e) {
                hasError = true;
                errorMsg = "Error parsing server response";
                e.printStackTrace();
            }
        else
        {
            hasError = true;
            errorMsg = "No response";
        }
        
        return null;
        
    }
    
    private static JsonArray getResponseField(JsonObject response, String fieldName) throws JsonParseException {
       	if (fieldName != null) {
            return response.getAsJsonArray(fieldName);
    	}
    	else {
    		JsonArray array = new JsonArray();
    		array.add(response);
    		return array;
    	}
    }
    
    public JsonArray requestNews(final long lastNewsUid)
    {
        
        hasError = false;
        
        final URI hostUrl = getUri(Config.RMBT_NEWS_HOST_URL);
        
        Log.e(DEBUG_TAG,"Newsrequest to " + hostUrl);
        
        final JsonObject requestData = new JsonObject();
        Gson gson = new Gson();

        try
        {
            InformationCollector.fillBasicInfo(requestData, context);
            
            requestData.add("uuid", new JsonPrimitive(getUUID()));
            requestData.add("lastNewsUid", new JsonPrimitive(lastNewsUid));
        }
        catch (final JsonParseException e)
        {
            hasError = true;
            errorMsg = "Error gernerating request";
        }

        JsonArray array = sendRequest(hostUrl, requestData, "news");
        Log.e(DEBUG_TAG,"LOG response to " + hostUrl + "\n" + array);
        return array;
        
    }
    
    public JsonArray sendLogReport(final JsonObject requestData)  {
        final URI hostUrl = getUri(Config.RMBT_LOG_HOST_URL);
        Gson gson = new Gson();
        try
        {
            Log.e(DEBUG_TAG,"LOG request to " + hostUrl);

            InformationCollector.fillBasicInfo(requestData, context);
            
            requestData.add("uuid", new JsonPrimitive(getUUID()));
        }
        catch (final Exception e)
        {
            hasError = true;
            errorMsg = "Error gernerating request";
		}

        JsonArray array = sendRequest(hostUrl, requestData, null);
        Log.e(DEBUG_TAG,"LOG response to " + hostUrl + "\n" + array);
        return array;
    }
    
    public JsonArray requestIp(boolean isIpv6)
    {
        Gson gson = new Gson();
        hasError = false;
                
        URI hostUrl = null;
        
        final JsonObject requestData = new JsonObject();
        
        try
        {
            hostUrl = isIpv6 ? new URL(ConfigHelper.getCachedIpv6CheckUrl(context)).toURI() : new URL(ConfigHelper.getCachedIpv4CheckUrl(context)).toURI();
            
            Log.e(DEBUG_TAG,"IP request to " + hostUrl);

            InformationCollector.fillBasicInfo(requestData, context);
            
            requestData.add("uuid", new JsonPrimitive(getUUID()));
        }
        catch (final Exception e)
        {
            hasError = true;
            errorMsg = "Error gernerating request";
		}
        Log.e("Debug-Request:", requestData.toString());
        JsonArray array = sendRequest(hostUrl, requestData, null);
        Log.e(DEBUG_TAG,"IP response to " + hostUrl + "\n" + array);
        return hostUrl != null ?  array : null;
        
    }
    
    public JsonArray requestHistory(final String uuid, final ArrayList<String> devicesToShow,
            final ArrayList<String> networksToShow, final int resultLimit)
    {
        
        hasError = false;
        Gson gson = new Gson();
        final URI hostUrl = getUri(Config.RMBT_HISTORY_HOST_URL);
        
        Log.e(DEBUG_TAG,"Historyrequest to " + hostUrl);
        
        final JsonObject requestData = new JsonObject();
        
        try
        {
            InformationCollector.fillBasicInfo(requestData, context);
            requestData.add("uuid", new JsonPrimitive(uuid));
            requestData.add("result_limit", new JsonPrimitive(resultLimit));
            
            if (devicesToShow != null && devicesToShow.size() > 0)
            {
                
                final JsonArray filterList = new JsonArray();
                
                for (final String s : devicesToShow)
                    filterList.add(s);
                
                requestData.add("devices",filterList);
            }
            
            if (networksToShow != null && networksToShow.size() > 0)
            {
                
                final JsonArray filterList = new JsonArray();
                
                for (final String s : networksToShow)
                {
                    Log.i(DEBUG_TAG, s);
                    filterList.add(s);
                }
                
                requestData.add("networks", filterList);
            }
            
        }
        catch (final JsonParseException e1)
        {
            hasError = true;
            errorMsg = "Error gernerating request";
            //e1.printStackTrace();
        }

        JsonArray array = sendRequest(hostUrl, requestData, "history");
        Log.e(DEBUG_TAG,"RMBTTest Result response to " + hostUrl + "\n" + array);
        return array;
        
    }
    
    public JsonArray requestTestResult(final String testUuid)
    {
        
        hasError = false;
        Gson gson = new Gson();
        final URI hostUrl = getUri(Config.RMBT_TESTRESULT_HOST_URL);
        
        Log.e(DEBUG_TAG,"RMBTTest Result request to " + hostUrl);
        
        final JsonObject requestData = new JsonObject();
        
        try
        {
            InformationCollector.fillBasicInfo(requestData, context);
            requestData.add("test_uuid", new JsonPrimitive(testUuid));
            requestData.add("uuid", new JsonPrimitive(getUUID()));
        }
        catch (final JsonParseException e1)
        {
            hasError = true;
            errorMsg = "Error gernerating request";
            // e1.printStackTrace();
        }
        JsonArray array = sendRequest(hostUrl, requestData, "testresult");
        Log.e(DEBUG_TAG,"RMBTTest Result response to " + hostUrl + "\n" + array);
        return array;
    }
    
    public JsonObject requestOpenDataTestResult(final String testUuid, final String openTestUuid) {
        hasError = false;
        Gson gson = new Gson();
        final URI hostUrl = getUri(Config.RMBT_TESTRESULT_OPENDATA_HOST_URL + openTestUuid);
        
        Log.e(DEBUG_TAG,"RMBTTest OpenData Result request to " + hostUrl);
        
        final JsonObject requestData = new JsonObject();
        
        try
        {
            InformationCollector.fillBasicInfo(requestData, context);
            requestData.add("test_uuid", new JsonPrimitive(testUuid));
            requestData.add("open_test_uuid", new JsonPrimitive(openTestUuid));
            requestData.add("uuid", new JsonPrimitive(getUUID()));
        }
        catch (final JsonParseException e1)
        {
            hasError = true;
            errorMsg = "Error gernerating request";
            // e1.printStackTrace();
        }
        JsonObject url = jParser.getURL(hostUrl);
        Log.e(DEBUG_TAG,"RMBTTest OpenData Result response to " + hostUrl + "\n " + url);
        return url;
//        JsonArray array = sendRequest(hostUrl, requestData, null);
//        return array;
    }
    
    public JsonArray requestTestResultQoS(final String testUuid)
    {
        
        hasError = false;
        Gson gson = new Gson();
        final URI hostUrl = getUri(Config.RMBT_TESTRESULT_QOS_HOST_URL);
        
        Log.e(DEBUG_TAG,"RMBTTest QoS Result request to " + hostUrl);
        
        final JsonObject requestData = new JsonObject();
        
        try
        {
            InformationCollector.fillBasicInfo(requestData, context);
            requestData.add("test_uuid", new JsonPrimitive(testUuid));
            requestData.add("uuid", new JsonPrimitive(getUUID()));
        }
        catch (final JsonParseException e1)
        {
            hasError = true;
            errorMsg = "Error gernerating request";
            // e1.printStackTrace();
        }

        JsonArray array = sendRequest(hostUrl, requestData, null);
        Log.e(DEBUG_TAG,"RMBTTest QoS Result response to " + hostUrl + "\n" + array);
        return array;
    }
    
    public JsonArray requestTestResultDetail(final String testUuid)
    {
        
        hasError = false;
        Gson gson = new Gson();
        final URI hostUrl = getUri(Config.RMBT_TESTRESULT_DETAIL_HOST_URL);
        
        Log.e(DEBUG_TAG,"RMBTTest ResultDetail request to " + hostUrl);
        
        final JsonObject requestData = new JsonObject();
        
        try
        {
            InformationCollector.fillBasicInfo(requestData, context);
            requestData.add("test_uuid", new JsonPrimitive(testUuid));
            requestData.add("uuid", new JsonPrimitive(getUUID()));
        }
        catch (final JsonParseException e1)
        {
            hasError = true;
            errorMsg = "Error gernerating request";
            // e1.printStackTrace();
        }
        JsonArray array = sendRequest(hostUrl, requestData, "testresultdetail");
        Log.e(DEBUG_TAG,"RMBTTest ResultDetail response to " + hostUrl + "\n"+ array);
        return array;
    }

    public JsonArray requestGetMeasurementServers(Location location)
    {
        hasError = false;
        Gson gson = new Gson();
        final URI hostUrl = getUri(Config.RMBT_GET_MEASUREMENT_SERVERS_HOST_URL);

        Log.e(DEBUG_TAG,"Measurement servers get request to " + hostUrl);

        String clientName = at.specure.android.configs.Config.RMBT_CLIENT_NAME;
        MeasurementServerGet measurementServerGet = new MeasurementServerGet(location, clientName);

        JsonObject request = new MeasurementServerRq(measurementServerGet).createRequest();

        JsonArray array = sendRequest(hostUrl, request, "servers");
        Log.e(DEBUG_TAG,"Measurement servers get response to " + hostUrl + "\n"+ array);
        return array;
    }

    public JsonElement requestSendZeroMeasurements(List<ZeroMeasurement> zeroMeasurements)
    {
        hasError = false;
        Gson gson = new Gson();
        final URI hostUrl = getUri(Config.RMBT_PUT_ZERO_MEASUREMENTS_HOST_URL);

        Log.e(DEBUG_TAG,"Zero Measurements post request to " + hostUrl);

        String clientName = at.specure.android.configs.Config.RMBT_CLIENT_NAME;
        ZeroMeasurementPost zeroMeasurementPost = new ZeroMeasurementPost(zeroMeasurements);

        JsonObject request = new ZeroMeasurementsPostRq(zeroMeasurementPost).createRequest();

        JsonElement array = sendRequestElem(hostUrl, request, "success");
        Log.e(DEBUG_TAG,"Zero Measurements post response to " + hostUrl + "\n"+ array);
        return array;
    }
    
    public JsonArray requestSyncCode(final String uuid, final String syncCode)
    {
        
        hasError = false;
        Gson gson = new Gson();
        final URI hostUrl = getUri(Config.RMBT_SYNC_HOST_URL);
        
        Log.e(DEBUG_TAG,"Sync request to " + hostUrl);
        
        final JsonObject requestData = new JsonObject();
        
        try
        {
            InformationCollector.fillBasicInfo(requestData, context);
            requestData.add("uuid", new JsonPrimitive(uuid));
            
            if (syncCode.length() > 0)
                requestData.add("sync_code", new JsonPrimitive(syncCode));
            
        }
        catch (final JsonParseException e1)
        {
            hasError = true;
            errorMsg = "Error gernerating request";
            // e1.printStackTrace();
        }

        JsonArray array = sendRequest(hostUrl, requestData, "sync");
        Log.e(DEBUG_TAG,"Settings response to " + hostUrl + "\n" + array);
        return array;
    }
    
    public JsonArray requestSettings()
    {
        
        hasError = false;
        Gson gson = new Gson();
        final URI hostUrl = getUri(Config.RMBT_SETTINGS_HOST_URL);
        
        PackageInfo pInfo;
        String clientVersionName = "";
        int clientVersionCode = 0;
        String clientName = "";
        try
        {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            clientVersionName = pInfo.versionName;
            clientVersionCode = pInfo.versionCode;
            clientName = context.getResources().getString(R.string.app_name_api);
        }
        catch (final NameNotFoundException e)
        {
            // e1.printStackTrace();
            Log.e(DEBUG_TAG, "version of the application cannot be found", e);
        }
        
        Log.e(DEBUG_TAG,"Settings request to " + hostUrl);
        
        final JsonObject requestData = new JsonObject();
        
        try
        {
            InformationCollector.fillBasicInfo(requestData, context);
            requestData.add("uuid", new JsonPrimitive(getUUID()));
            requestData.add("name", new JsonPrimitive(clientName));
            requestData.add("version_name", new JsonPrimitive(clientVersionName));
            requestData.add("version_code", new JsonPrimitive(clientVersionCode));
            
            final int tcAcceptedVersion = ConfigHelper.getTCAcceptedVersion(context);
            requestData.add("terms_and_conditions_accepted_version", new JsonPrimitive(tcAcceptedVersion));
            if (tcAcceptedVersion > 0) // for server backward compatibility
                requestData.add("terms_and_conditions_accepted", new JsonPrimitive(true));
        }
        catch (final JsonParseException e1)
        {
            hasError = true;
            errorMsg = "Error gernerating request";
            // e1.printStackTrace();
        }

        JsonArray array = sendRequest(hostUrl, requestData, "settings");
        Log.e(DEBUG_TAG,"Settings response to " + hostUrl + "\n" + array);
        return array;
    }
    
    public JsonObject requestMapOptionsInfo()
    {
        
        hasError = false;
        Gson gson = new Gson();
        final URI hostUrl = getUri(MapProperties.MAP_OPTIONS_PATH);
        
        final JsonObject requestData = new JsonObject();
        
        try
        {
            requestData.add("language", new JsonPrimitive(Locale.getDefault().getLanguage()));
        }
        catch (final JsonParseException e1)
        {
            hasError = true;
            errorMsg = "Error gernerating request";
        }
        
        Log.e(DEBUG_TAG, "request to " + hostUrl);
        final JsonObject response = jParser.sendJSONToUrl(hostUrl, requestData);
        Log.e(DEBUG_TAG, "response to " + hostUrl + "\n" + response);
        return response;
    }

    public JsonObject requestMapOperatorsFilter(String countryCode, String providerType)
    {

        hasError = false;
        Gson gson = new Gson();
        final URI hostUrl = getUri(MapProperties.MAP_OPERATORS_FILTER_PATH);

        final JsonObject requestData = new JsonObject();

        try
        {
            requestData.add("language", new JsonPrimitive(Locale.getDefault().getLanguage()));
            requestData.add("country_code", new JsonPrimitive(countryCode));
            requestData.add("provider_type", new JsonPrimitive(providerType));
        }
        catch (final JsonParseException e1)
        {
            hasError = true;
            errorMsg = "Error gernerating request";
        }

        Log.e(DEBUG_TAG, "request to " + hostUrl);
        final JsonObject response = jParser.sendJSONToUrl(hostUrl, requestData);
        Log.e(DEBUG_TAG, "response to " + hostUrl + "\n" + response);
        return response;
    }
    
    public JsonArray requestMapMarker(final double lat, final double lon, final int zoom, final int size,
            final Map<String, String> optionMap)
    {
        hasError = false;
        Gson gson = new Gson();
        final URI hostUrl = getUri(MapProperties.MARKER_PATH);
        
        Log.e(DEBUG_TAG,"MapMarker request to " + hostUrl);
        
        final JsonObject requestData = new JsonObject();
        
        try
        {
            requestData.add("language", gson.toJsonTree(Locale.getDefault().getLanguage()));
            
            final JsonObject coords = new JsonObject();
            coords.add("lat", new JsonPrimitive(lat));
            coords.add("lon", new JsonPrimitive(lon));
            coords.add("z", new JsonPrimitive(zoom));
            coords.add("size", new JsonPrimitive(size));
            requestData.add("coords", coords);
            
            final JsonObject filter = new JsonObject();
            final JsonObject options = new JsonObject();
            
            for (final String key : optionMap.keySet())
            {
                
                if (MapProperties.MAP_OVERLAY_KEY.equals(key))
                    // skip map_overlay_key
                    continue;
                
                final String value = optionMap.get(key);
                
                if (value != null && value.length() > 0)
                    if (key.equals("map_options"))
                        options.add(key, new JsonPrimitive(value));
                    else
                        filter.add(key, gson.toJsonTree(value));
            }
            
            requestData.add("filter", filter);
            requestData.add("options", options);
            
        }
        catch (final JsonParseException e1)
        {
            hasError = true;
            errorMsg = "Error gernerating request";
            // e1.printStackTrace();
        }

        JsonArray array = sendRequest(hostUrl, requestData, "measurements");
        Log.e(DEBUG_TAG,"MapMarker response to " + hostUrl + "\n" + array);
        return array;
    }
    
    public boolean hasError()
    {
        return hasError;
    }

    public String getErrorMessage() { return errorMsg; }
    
}
