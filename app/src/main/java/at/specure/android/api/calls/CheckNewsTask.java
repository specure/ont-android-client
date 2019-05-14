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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.specure.opennettest.R;

import at.specure.android.api.ControlServerConnection;
import at.specure.android.configs.ConfigHelper;
import at.specure.android.screens.main.MainActivity;
import at.specure.android.util.CustomAlertDialogFragment;

public class CheckNewsTask extends AsyncTask<Void, Void, JsonArray> {

    private final MainActivity activity;

    private JsonArray newsList;

    long lastNewsUid;

    ControlServerConnection serverConn;

    public CheckNewsTask(final MainActivity activity) {
        this.activity = activity;

    }

    @Override
    protected JsonArray doInBackground(final Void... params) {
        serverConn = new ControlServerConnection(activity);

        lastNewsUid = ConfigHelper.getLastNewsUid(activity);

        newsList = serverConn.requestNews(lastNewsUid);

        return newsList;
    }

    @Override
    protected void onCancelled() {
        if (serverConn != null) {
            serverConn.unload();
            serverConn = null;
        }
    }

    @Override
    protected void onPostExecute(final JsonArray newsList) {

        if (newsList != null && newsList.size() > 0 && !serverConn.hasError())
            for (int i = 0; i < newsList.size(); i++)
                if (!isCancelled() && !Thread.interrupted())
                    try {

                        final JsonObject newsItem = newsList.get(i).getAsJsonObject();
                        String title = activity.getString(R.string.news_title);
                        if (newsItem.has("title")) {
                            title = newsItem.get("title").getAsString();
                        }

                        String text = activity.getString(R.string.news_no_message);
                        if (newsItem.has("text")) {
                            text = newsItem.get("text").getAsString();
                        }


                        final CustomAlertDialogFragment newFragment = CustomAlertDialogFragment.newInstance(title, text, null);

                        newFragment.show(activity.getSupportFragmentManager(), "dialog");

                        if (newsItem.has("uid")) {
                            if (lastNewsUid < newsItem.get("uid").getAsLong())
                                lastNewsUid = newsItem.get("uid").getAsLong();
                        }
                    } catch (final JsonParseException e) {
                        e.printStackTrace();
                    }

        ConfigHelper.setLastNewsUid(activity.getApplicationContext(), lastNewsUid);
    }

}
