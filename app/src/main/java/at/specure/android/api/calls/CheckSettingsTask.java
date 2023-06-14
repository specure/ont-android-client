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
package at.specure.android.api.calls;

import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import at.specure.android.api.ControlServerConnection;
import at.specure.android.api.reqres.check_survey.SurveySettings;
import at.specure.android.configs.ConfigHelper;
import at.specure.android.configs.SurveyConfig;
import at.specure.android.screens.main.MainActivity;
import at.specure.android.util.EndTaskListener;
import timber.log.Timber;


/**
 * 
 * @author
 * 
 */
public class CheckSettingsTask extends AsyncTask<Void, Void, JsonArray>
{
    
    /**
	 * 
	 */
    private static final String DEBUG_TAG = "CheckSettingsTask";
    
    /**
	 * 
	 */
    private final MainActivity activity;
    
    /**
	 * 
	 */
    private ControlServerConnection serverConn;
    
    /**
	 * 
	 */
    private EndTaskListener endTaskListener;
    
    /**
	 * 
	 */
    private boolean hasError = false;
    
    /**
     * 
     * @param activity
     */
    public CheckSettingsTask(final MainActivity activity)
    {
        this.activity = activity;
        
    }
    
    /**
	 * 
	 */
    @Override
    protected JsonArray doInBackground(final Void... params)
    {
        JsonArray resultList = null;
        
        serverConn = new ControlServerConnection(activity.getApplicationContext());
        
        resultList = serverConn.requestSettings();
        
        return resultList;
    }
    
    /**
	 * 
	 */
    @Override
    protected void onCancelled()
    {
        if (serverConn != null)
        {
            serverConn.unload();
            serverConn = null;
        }
    }
    
    /**
	 * 
	 */
    @Override
    protected void onPostExecute(final JsonArray resultList)
    {
        try
        {
        if (serverConn.hasError())
            hasError = true;
        else if (resultList != null && resultList.size() > 0)
        {
            
                JsonObject resultListItem;
                
                try
                {
                    resultListItem = resultList.get(0).getAsJsonObject();

                    /* UUID */
                    String uuid = "";
                    if (resultListItem.has("uuid")) {
                        uuid = resultListItem.get("uuid").getAsString();
                    }
                    if (uuid != null && uuid.length() != 0)
                        ConfigHelper.setUUID(activity.getApplicationContext(), uuid);
                    
                    /* urls */
                    
                    final ConcurrentMap<String, String> volatileSettings = ConfigHelper.getVolatileSettings();

                    JsonObject urls = null;
                    if (resultListItem.has("urls")) {
                        urls = resultListItem.get("urls").getAsJsonObject();
                    }

                    if (urls != null)
                    {
                        final Iterator<Map.Entry<String, JsonElement>> keys = urls.entrySet().iterator();
                        
                        while (keys.hasNext())
                        {

                            final Map.Entry<String, JsonElement> key = keys.next();
//                            key.getValue().getAsString();
                            final String value = key.getValue().getAsString();
                            String key1 = key.getKey();
                            if (value != null) {
                                volatileSettings.put("url_" + key1, value);
                                if ("statistics".equals(key1)) {
                                	ConfigHelper.setCachedStatisticsUrl(value, activity);
                                }
                                else if ("control_ipv4_only".equals(key1)) {
                                	ConfigHelper.setCachedControlServerNameIpv4(value, activity);
                                }
                                else if ("control_ipv6_only".equals(key1)) {
                                	ConfigHelper.setCachedControlServerNameIpv6(value, activity);
                                }
                                else if ("url_ipv4_check".equals(key1)) {
                                	ConfigHelper.setCachedIpv4CheckUrl(value, activity);
                                }
                                else if ("url_ipv6_check".equals(key1)) {
                                	ConfigHelper.setCachedIpv6CheckUrl(value, activity);
                                }
                            }
                        }
                    }
                    
                    /* qos names */
                    JsonArray qosNames = null;
                    if (resultListItem.has("qostesttype_desc")) {
                        qosNames = resultListItem.getAsJsonArray("qostesttype_desc");
                    }

                    if (qosNames != null) {
                    	final Map<String, String> qosNamesMap = new HashMap<String, String>();
                    	for (int i = 0; i < qosNames.size(); i++) {
                    		JsonObject json = qosNames.get(i).getAsJsonObject();
                            String testType = null;
                            if (json.has("test_type")) {
                                testType = json.get("test_type").getAsString();
                            }
                            String name = null;
                            if (json.has("name")) {
                                name = json.get("name").getAsString();
                            }
                    		qosNamesMap.put(testType, name);
                    	}
                    	ConfigHelper.setCachedQoSNames(qosNamesMap, activity);
                    }
                    
                    /* map server */
                    JsonObject mapServer = null;
                    if (resultListItem.has("map_server")) {
                        mapServer = resultListItem.getAsJsonObject("map_server");
                    }

                    if (mapServer != null)
                    {
                        String host = null;
                        if (mapServer.has("host")) {
                            host = mapServer.get("host").getAsString();
                        }
                        Integer port = null;
                        if (mapServer.has("port")) {
                            port = mapServer.get("port").getAsInt();
                        }
                        Boolean ssl = null;
                        if (mapServer.has("ssl")) {
                            ssl = mapServer.get("ssl").getAsBoolean();
                        }

                        if (host != null && port > 0)
                            ConfigHelper.setMapServer(host, port, ssl);
                    }
                    
                    /* control server version */
                    JsonObject versions = null;
                    if (resultListItem.has("versions")) {
                        versions = resultListItem.getAsJsonObject("versions");
                    }
                    if (versions != null)
                    {
                    	if (versions.has("control_server_version")) {
                            String controlServerVersion = "";
                            if (versions.has("control_server_version")) {
                                controlServerVersion = versions.get("control_server_version").getAsString();
                            }
                    		ConfigHelper.setControlServerVersion(activity, controlServerVersion);
                    	}
                    }


                    // SURVEY
                    try {
                        if (SurveyConfig.isSurveyEnabledInApp(activity)) {
                            final JsonObject surveyObject = resultListItem.getAsJsonObject("survey_settings");
                            Gson gson = new Gson();
                            SurveySettings surveySettings = gson.fromJson(surveyObject, SurveySettings.class);
                            if (surveySettings.isActive) {
                                SurveyConfig.saveCurrentSurveySettings(activity, surveySettings.surveyUrl, surveySettings.timestampStarted);
                            } else {
                                SurveyConfig.saveCurrentSurveySettings(activity, null, 0);
                            }
                        }
                    } catch (Exception e) {
                        //do nothing
                    }

                    // ///////////////////////////////////////////////////////
                    // HISTORY / FILTER
                    
                    final JsonObject historyObject = resultListItem.getAsJsonObject("history");
                    
                    final JsonArray deviceArray = historyObject.getAsJsonArray("devices");
                    final JsonArray networkArray = historyObject.getAsJsonArray("networks");
                    
                    final String historyDevices[] = new String[deviceArray.size()];
                    
                    for (int i = 0; i < deviceArray.size(); i++)
                        historyDevices[i] = deviceArray.get(i).getAsString();
                    
                    final String historyNetworks[] = new String[networkArray.size()];
                    
                    for (int i = 0; i < networkArray.size(); i++)
                        historyNetworks[i] = networkArray.get(i).getAsString();
                    
                    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    
                    activity.setSettings(historyDevices, historyNetworks);

                    ConfigHelper.setHistoryIsDirty(activity, true);

                }
                catch (final JsonParseException e)
                {
                    e.printStackTrace();
                }
                
            }
            else
                Timber.i("LEERE LISTE");
        }
        finally
        {
            if (endTaskListener != null)
                endTaskListener.taskEnded(resultList);
        }
    }
    
    /**
     * 
     * @param endTaskListener
     */
    public void setEndTaskListener(final EndTaskListener endTaskListener)
    {
        this.endTaskListener = endTaskListener;
    }
    
    /**
     * 
     * @return
     */
    public boolean hasError()
    {
        return hasError;
    }
}
