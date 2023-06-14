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

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import at.specure.android.api.ControlServerConnection;
import at.specure.android.api.reqres.check_survey.CheckSurveyRsp;
import timber.log.Timber;

public class CheckSurveyTask extends AsyncTask<String, Void, CheckSurveyRsp> {

    private static final String DEBUG_TAG = "GetMeasurementServTask";
    private final Activity activity;
    private ControlServerConnection serverConn;
    private CheckSurveyEndTaskListener endTaskListener;

    public CheckSurveyTask(final Activity activity) {
        this.activity = activity;
    }

    public void setOnCompleteListener(CheckSurveyEndTaskListener listener) {
        this.endTaskListener = listener;
    }

    @Override
    protected CheckSurveyRsp doInBackground(final String... params) {

        try {
            serverConn = new ControlServerConnection(activity);

            JsonArray response = serverConn.requestCheckSurvey(params[0]);
            JsonElement jsonElement = response.get(0);
            Gson gson = new Gson();
            CheckSurveyRsp checkSurvey = gson.fromJson(jsonElement, CheckSurveyRsp.class);

            return checkSurvey;
        } catch (Exception e) {
            Timber.e( "ERROR GETTING SURVEY CHECK");
            return null;
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
    protected void onPostExecute(final CheckSurveyRsp checkedSurvey) {

        try {
            Timber.d( "%s", checkedSurvey);

            if (checkedSurvey != null && !serverConn.hasError()) {
                if (endTaskListener != null) {
                    endTaskListener.onSurveyCheckEnded(checkedSurvey);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface CheckSurveyEndTaskListener {

        void onSurveyCheckEnded(CheckSurveyRsp checkSurveyRsp);
    }
}
