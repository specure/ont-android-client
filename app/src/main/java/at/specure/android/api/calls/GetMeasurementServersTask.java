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
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import at.specure.android.api.ControlServerConnection;
import at.specure.android.api.jsons.Location;
import at.specure.android.api.jsons.MeasurementServer;
import at.specure.android.screens.main.MainActivity;
import at.specure.android.util.MeasurementTaskEndedListener;
import timber.log.Timber;

public class GetMeasurementServersTask extends AsyncTask<Location, Void, List<MeasurementServer>> {

    private static final long REFRESH_MILLISECONDS_INTERVAL = 60000 * 5; // 5 minutes interval
    private final MainActivity activity;
    private List<MeasurementServer> servers;
    private ControlServerConnection serverConn;
    private MeasurementTaskEndedListener endTaskListener;
    private long timestamp = new Date().getTime();

    private static final String DEBUG_TAG = "GetMeasurementServTask";
    private Location location;

    public GetMeasurementServersTask(final MainActivity activity) {
        this.activity = activity;
        this.timestamp = new Date().getTime();
        this.servers = new ArrayList<>();
        this.location = null;
    }

    public void setOnCompleteListener(MeasurementTaskEndedListener listener) {
        this.endTaskListener = listener;
    }

    @Override
    protected List<MeasurementServer> doInBackground(final Location... params) {

        try {
            serverConn = new ControlServerConnection(activity);

            location = params[0];
            JsonArray response = serverConn.requestGetMeasurementServers(params[0], activity);
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<MeasurementServer>>() {
            }.getType();

            MeasurementServer[] measurementServers = gson.fromJson(response, MeasurementServer[].class);

            List<MeasurementServer> list = Arrays.asList(measurementServers);

            return servers = list;
        } catch (Exception e) {
            Timber.e( "ERROR GETTING MEASUREMENT SERVER");
            return servers = new ArrayList<>();
        }
    }

    @Override
    protected void onCancelled() {
        if (serverConn != null) {
            serverConn.unload();
            serverConn = null;
        }
    }

    @Override
    protected void onPostExecute(final List<MeasurementServer> newsList) {

        try {
            Timber.d("%s", newsList);

            if (newsList != null && newsList.size() > 0 && !serverConn.hasError()) {
                servers = newsList;
                if (endTaskListener != null) {
                    endTaskListener.taskEnded(servers);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean shouldRun(Location newlocation) {
        long newTimestamp = new Date().getTime();
        if ((location == null && newlocation != null) || newTimestamp - REFRESH_MILLISECONDS_INTERVAL > this.timestamp) {
            return true;
        } else {
            return false;
        }

    }

    public List<MeasurementServer> getServers() {
        return servers;
    }
}
