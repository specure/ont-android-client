/*******************************************************************************
 * Copyright 2014-2017 Specure GmbH
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
 *******************************************************************************/

package at.specure.android;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

/**
 * Created by grondz on 18.11.16.
 */

public class JsonMapConfig {
    private Map<String, String> data;
    private InputStream propertiesStream;

    public JsonMapConfig(InputStream propertiesStream) throws IOException {
        this.propertiesStream = propertiesStream;
        parseJson();
    }

    private void parseJson() throws IOException {
        try {
            data = new HashMap<String, String>();
            JsonObject obj = new Gson().fromJson(loadJsonFromAsset(), JsonObject.class);
            JsonArray m_jArry = obj.getAsJsonArray("urls");
            String language_data;
            String url_data;

            for (int i = 0; i < m_jArry.size(); i++) {
                JsonObject jo_inside = m_jArry.get(i).getAsJsonObject();
                language_data = jo_inside.getAsJsonPrimitive("language").getAsString();
                url_data = jo_inside.getAsJsonPrimitive("url").getAsString();
                Timber.d("Details--> Language: %s. Url: %s",   language_data, url_data);

                data.put(language_data, url_data);
            }
        }
          catch (IOException e) {
              Timber.d("JSON Parser error: %s", e.getLocalizedMessage());
              throw e;
          }
        }

    private String loadJsonFromAsset() throws IOException {
        String json = "";
        try {
            int size = propertiesStream.available();
            byte[] buffer = new byte[size];
            propertiesStream.read(buffer);
            propertiesStream.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException e) {
            Timber.d("JSON Load error: %s", e.getLocalizedMessage());
            throw e;
        }
        return json;
    }

    public String getProperty(String key) {
        return (data != null) ? data.get(key) : null;
    }
}
