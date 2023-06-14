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
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import at.specure.android.api.ControlServerConnection;
import at.specure.android.configs.ConfigHelper;
import at.specure.android.screens.main.MainActivity;
import at.specure.android.util.EndTaskListener;
import timber.log.Timber;


/**
 * @author
 */
public class RegistrationTask extends AsyncTask<Void, Void, JsonArray> {

    /**
     *
     */
    private static final String DEBUG_TAG = "RegistrationTask";

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
    public RegistrationTask(final MainActivity activity) {
        this.activity = activity;

    }

    /**
     *
     */
    @Override
    protected JsonArray doInBackground(final Void... params) {
        JsonArray resultList = null;

        serverConn = new ControlServerConnection(activity.getApplicationContext());

        resultList = serverConn.requestRegistration();

        return resultList;
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
    protected void onPostExecute(final JsonArray resultList) {
        try {
            if (serverConn.hasError())
                hasError = true;
            else if (resultList != null && resultList.size() > 0) {

                JsonObject resultListItem;

                try {
                    resultListItem = resultList.get(0).getAsJsonObject();

                    /* UUID */
                    String uuid = "";
                    if (resultListItem.has("uuid")) {
                        uuid = resultListItem.get("uuid").getAsString();
                    }
                    if (uuid != null && uuid.length() != 0)
                        ConfigHelper.setUUID(activity.getApplicationContext(), uuid);

                } catch (final JsonParseException e) {
                    e.printStackTrace();
                }

            } else
                Timber.i( "LEERE LISTE");
        } finally {
            if (endTaskListener != null)
                endTaskListener.taskEnded(resultList);
        }
    }

    /**
     * @param endTaskListener
     */
    public void setEndTaskListener(final EndTaskListener endTaskListener) {
        this.endTaskListener = endTaskListener;
    }

    /**
     * @return
     */
    public boolean hasError() {
        return hasError;
    }
}
