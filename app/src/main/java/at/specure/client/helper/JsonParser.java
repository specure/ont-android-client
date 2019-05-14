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
package at.specure.client.helper;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

import static com.google.gson.stream.JsonToken.NULL;

public class JsonParser {
    // Start filing Errors
    JsonArray errorList = null;

    // constructor
    public JsonParser() {
        // Start filing Errors
        errorList = new JsonArray();
    }

    public JsonObject getURL(final URI uri) {
        Gson gson = new Gson();
        JsonObject jObj = null;
        String responseBody;

        try {
            final HttpParams params = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(params, 20000);
            HttpConnectionParams.setSoTimeout(params, 20000);
            final HttpClient client = new DefaultHttpClient(params);

            final HttpGet httpget = new HttpGet(uri);

            final ResponseHandler<String> responseHandler = new BasicResponseHandler();
            responseBody = client.execute(httpget, responseHandler);

            // try parse the string to a Json object
            try {
                jObj = gson.fromJson(responseBody, JsonElement.class).getAsJsonObject();
            } catch (final JsonParseException e) {
                writeErrorList("Error parsing Json " + e.toString());
            }

        } catch (final UnsupportedEncodingException e) {
            writeErrorList("Wrong encoding");
            // e.printStackTrace();
        } catch (final HttpResponseException e) {
            writeErrorList("Server responded with Code " + e.getStatusCode() + " and message '" + e.getMessage() + "'");
        } catch (final ClientProtocolException e) {
            writeErrorList("Wrong Protocol");
            // e.printStackTrace();
        } catch (final IOException e) {
            writeErrorList("IO Exception");
            e.printStackTrace();
        }

        return jObj;
    }

    public JsonObject sendJsonToUrl(final URI uri, final JsonObject data) {
        JsonObject jObj = null;
        String responseBody;

        try {
            final HttpParams params = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(params, 20000);
            HttpConnectionParams.setSoTimeout(params, 20000);
            final HttpClient client = new DefaultHttpClient(params);

            final HttpPost httppost = new HttpPost(uri);

            final StringEntity se = new StringEntity(data.toString(), "UTF-8");

            httppost.setEntity(se);
            httppost.setHeader(new BasicHeader(HTTP.CONTENT_TYPE, "application/json;charset=UTF-8"));

            Timber.e("Request to: %s \n \nwith data: \n%s", uri.toString(),  data.toString());

            final ResponseHandler<String> responseHandler = new BasicResponseHandler();
            try {
                responseBody = client.execute(httppost, responseHandler);
                try {
                    Timber.e("Response to: %s \n\n with data: \n %s", uri.toString(), responseBody);
//                JsonObject json = new JsonObject();
                    Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
                    JsonElement jelem = gson.fromJson(responseBody, JsonElement.class);
//                JsonObject jobj = jelem.getAsJsonObject();
                    jObj = jelem.getAsJsonObject();
//                jObj = (JsonObject) gson.toJsonTree(responseBody);
//                jObj = new JsonObject(responseBody);
                } catch (final JsonParseException | IllegalStateException e) {
                    writeErrorList("Error parsing Json " + e.toString());
                    Timber.e("ReqError %s", e.getMessage());
                }
            } catch (OutOfMemoryError memoryError) {
                //do nothing
            }

            // try parse the string to a Json object


        } catch (final UnsupportedEncodingException e) {
            writeErrorList("Wrong encoding");
            Timber.e("ReqError %s", e.getMessage());
            // e.printStackTrace();
        } catch (final HttpResponseException e) {
            writeErrorList("Server responded with Code " + e.getStatusCode() + " and message '" + e.getMessage() + "'");
            Timber.e("ReqError %s", e.getMessage());
        } catch (final ClientProtocolException e) {
            writeErrorList("Wrong Protocol");
            Timber.e("ReqError %s", e.getMessage());
            // e.printStackTrace();
        } catch (final ConnectTimeoutException e) {
            writeErrorList("ConnectionTimeoutException");
            e.printStackTrace();
            Timber.e("ReqError %s", e.getMessage());
        } catch (final IOException e) {
            writeErrorList("IO Exception");
            e.printStackTrace();
            Timber.e("ReqError %s", e.getMessage());
        }

        if (jObj == null)
            jObj = createErrorJson();

        return jObj;
    }

    private void writeErrorList(final String errorText) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(String.valueOf(errorList.size()), errorText);
        errorList.add(jsonObject);
        if (errorText != null) {
            System.out.println(errorText);
        }
    }

    private JsonObject createErrorJson() {
        final JsonObject errorAnswer = new JsonObject();
        Gson gson = new Gson();
        try {
            errorAnswer.add("error", gson.toJsonTree(errorList));
        } catch (final JsonParseException e) {
            System.out.println("Error saving ErrorList: " + e.toString());
        }
        return errorAnswer;
    }

    /**
     * @param object
     * @return
     */
    public static Map<String, Object> toMap(JsonObject object) {
        Map<String, Object> map = new HashMap<String, Object>();
        Iterator<Map.Entry<String, JsonElement>> iterator = object.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, JsonElement> next = iterator.next();
            String key = next.getKey();
            map.put(key, next.getValue());
        }
        return map;
    }

    /**
     * @param array
     * @return
     */
    public static List<Object> toList(JsonArray array) {
        List<Object> list = new ArrayList<Object>();
        for (int i = 0; i < array.size(); i++) {
            list.add(fromJson(array.get(i)));
        }
        return list;
    }

    /**
     * @param json
     * @return
     */
    private static Object fromJson(Object json) {
        if (json == NULL) {
            return null;
        } else if (json instanceof JsonObject) {
            return toMap((JsonObject) json);
        } else if (json instanceof JsonArray) {
            return toList((JsonArray) json);
        } else {
            return json;
        }
    }
}
