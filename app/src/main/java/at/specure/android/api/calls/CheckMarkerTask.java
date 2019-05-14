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

import android.app.Activity;
import android.os.AsyncTask;

import com.google.gson.JsonArray;

import at.specure.android.api.ControlServerConnection;
import at.specure.android.util.EndTaskListener;
import at.specure.androidX.data.map_filter.mappers.MapFilterSaver;


public class CheckMarkerTask extends AsyncTask<Void, Void, JsonArray>
{
    private final Activity activity;
    
    private JsonArray resultList;
    
    private ControlServerConnection serverConn;
    
    private EndTaskListener endTaskListener;
    
    private final double lat, lon;
    private final int zoom, size, maptype;
    
    private boolean hasError = false;

    public CheckMarkerTask(final Activity activity, final double lat, final double lon, final int zoom, final int size, int maptype)
    {
        this.activity = activity;
        this.lat = lat;
        this.lon = lon;
        this.zoom = zoom;
        this.size = size;
        this.maptype = maptype;
    }
    
    @Override
    protected JsonArray doInBackground(final Void... params)
    {
        serverConn = new ControlServerConnection(activity.getApplicationContext(), true);
        
        resultList = serverConn.requestMapMarker(lat, lon, zoom, size, maptype, MapFilterSaver.getActiveMapFilterParams(activity.getApplicationContext()));
        
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
