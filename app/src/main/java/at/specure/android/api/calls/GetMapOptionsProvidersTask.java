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
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import at.specure.android.api.ControlServerConnection;
import at.specure.android.api.jsons.FilterGroup;
import at.specure.android.screens.main.MainActivity;

public class GetMapOptionsProvidersTask extends AsyncTask<String, Void, JsonObject> {
    /**
     *
     */
    private static final String DEBUG_TAG = "GetMapOptionsProvidersTask";

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
     * @param activity
     */
    public GetMapOptionsProvidersTask(final MainActivity activity) {
        this.activity = activity;
    }

    /**
     *
     */
    @Override
    protected JsonObject doInBackground(final String... params) {
        JsonObject result = null;

        serverConn = new ControlServerConnection(activity.getApplicationContext(), true);

        try {
            result = serverConn.requestMapOperatorsFilter(params[0], params[1]);
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     *
     */
    @Override
    protected void onCancelled() {
        if (serverConn != null) {
            serverConn.unload();
            serverConn = null;
        }
    }

    /**
     *
     */
    @Override
    protected void onPostExecute(final JsonObject result) {
        FilterGroup filterGroup = null;
        if (serverConn.hasError())
            hasError = true;
        else if (result != null) {
            try {
                filterGroup = new Gson().fromJson(result, FilterGroup.class);
            } catch (final JsonParseException | NullPointerException e) {
                e.printStackTrace();
            }
        }
        if (endTaskListener != null) {
            final JsonArray array = new JsonArray();
            array.add(result);
            endTaskListener.taskEnded(filterGroup);
        }
    }

    public void setEndTaskListener(final EndTaskListener endTaskListener) {
        this.endTaskListener = endTaskListener;
    }

    public boolean hasError() {
        return hasError;
    }


    public interface EndTaskListener
    {
        public void taskEnded(FilterGroup result);
    }
}
