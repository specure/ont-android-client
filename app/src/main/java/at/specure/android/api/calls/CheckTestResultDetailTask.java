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

import android.os.AsyncTask;

import com.google.gson.JsonArray;
import com.google.gson.JsonParseException;

import at.specure.android.api.ControlServerConnection;
import at.specure.android.screens.main.MainActivity;
import at.specure.android.screens.result.ResultDetailType;
import at.specure.android.util.EndTaskListener;

public class CheckTestResultDetailTask extends AsyncTask<String, Void, JsonArray>
{
	private final ResultDetailType resultType;
    
    private final MainActivity activity;
    
    private JsonArray resultList;
    
    private ControlServerConnection serverConn;
    
    private EndTaskListener endTaskListener;
    
    private boolean hasError = false;
    
    public CheckTestResultDetailTask(final MainActivity activity2, final ResultDetailType resultType)
    {
        this.activity = activity2;
        this.resultType = resultType;
    }
    
    @Override
    protected JsonArray doInBackground(final String... uid)
    {
        serverConn = new ControlServerConnection(activity.getApplicationContext());

        try {
            if (uid != null && uid[0] != null)
            {
            	switch(this.resultType) {
            	case SPEEDTEST:
                	resultList = serverConn.requestTestResultDetail(uid[0]);
                	break;
            	case QUALITY_OF_SERVICE_TEST:
            		resultList = serverConn.requestTestResultQoS(uid[0]);
            		break;
            	case OPENDATA:
            		resultList = new JsonArray();
    				resultList.add(serverConn.requestOpenDataTestResult(uid[0], uid[1]));
            		break;
            	}
            }	
        }
        catch (JsonParseException e) {
        	e.printStackTrace();
        }
        
        return resultList;
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
    protected void onPostExecute(final JsonArray resultList)
    {
        if (serverConn.hasError())
            hasError = true;
        if (endTaskListener != null)
            endTaskListener.taskEnded(resultList);
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
