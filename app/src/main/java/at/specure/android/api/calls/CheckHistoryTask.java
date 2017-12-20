/*******************************************************************************
 * Copyright 2014-2017 Specure GmbH
 * Copyright 2013-2014 alladin-IT GmbH
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

import java.util.ArrayList;

import android.os.AsyncTask;

import com.google.gson.JsonArray;

import at.specure.android.api.ControlServerConnection;
import at.specure.android.configs.ConfigHelper;
import at.specure.android.screens.main.MainActivity;
import at.specure.android.util.EndTaskListener;


public class CheckHistoryTask extends AsyncTask<Void, Void, JsonArray>
{
    
    private final MainActivity activity;
    
    private JsonArray historyList;
    
    private String uuid;
    
    private ControlServerConnection serverConn;
    
    private EndTaskListener endTaskListener;
    
    private final ArrayList<String> devicesToShow;
    
    private final ArrayList<String> networksToShow;
    
    private boolean hasError = false;
    
    public CheckHistoryTask(final MainActivity mainActivity, final ArrayList<String> devicesToShow,
                            final ArrayList<String> networksToShow)
    {
        this.activity = mainActivity;
        
        this.devicesToShow = devicesToShow;
        
        this.networksToShow = networksToShow;
    }
    
    @Override
    protected JsonArray doInBackground(final Void... params)
    {
        serverConn = new ControlServerConnection(activity.getApplicationContext());
        
        uuid = ConfigHelper.getUUID(activity.getApplicationContext());
        
        if (uuid.length() > 0)
            historyList = serverConn.requestHistory(uuid, devicesToShow, networksToShow,
                    ((MainActivity) activity).getHistoryResultLimit());
        
        return historyList;
    }
    
    @Override
    protected void onCancelled()
    {
        if (serverConn != null)
        {
            serverConn.unload();
            serverConn = null;
        }
    }
    
    @Override
    protected void onPostExecute(final JsonArray historyList)
    {
        // fixed issue: before first measurement was performed, History of measurements view showed "No internet connection" message
        // Now it is showing "No data available"
        String errmsg = serverConn.getErrorMessage();
        hasError = serverConn.hasError() && errmsg.contains("No response");
//        if () {
//              hasError = true;
//        }

        if (endTaskListener != null)
            endTaskListener.taskEnded(historyList);
    }
    
    public void setEndTaskListener(final EndTaskListener endTaskListener)
    {
        this.endTaskListener = endTaskListener;
    }
    
    public boolean hasError()
    {
        return hasError;
    }
}
