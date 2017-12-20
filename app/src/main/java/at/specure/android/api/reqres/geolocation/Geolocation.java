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

package at.specure.android.api.reqres.geolocation;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

import at.specure.android.api.jsons.Address;
import at.specure.android.api.jsons.LocalizedGeopositionGet;

/**
 * Created by michal.cadrik on 8/8/2017.
 * http://wiki.openstreetmap.org/wiki/Nominatim#Reverse_Geocoding_.2F_Address_lookup
 */

public class Geolocation {


    public static String requestDecodeLocation(double lat, double lon, GeolocationDecoderListener listener) {
        final String requestString = "http://nominatim.openstreetmap.org/reverse?format=json&lat=" +
                Double.toString(lat) + "&lon=" + Double.toString(lon) + "&zoom=18&addressdetails=1";
        Log.e("Request to:", requestString);
        String responseBody;
//        final HttpParams params = new BasicHttpParams();
//        HttpConnectionParams.setConnectionTimeout(params, 20000);
//        HttpConnectionParams.setSoTimeout(params, 20000);
//        final HttpClient client = new DefaultHttpClient(params);
        final HttpClient client = new DefaultHttpClient();

        final HttpGet httpget = new HttpGet(requestString);
        httpget.addHeader(new Header() {
            @Override
            public String getName() {
                return "User-agent";
            }

            @Override
            public String getValue() {
                return "Specure Nettest";
            }

            @Override
            public HeaderElement[] getElements() throws ParseException {
                return new HeaderElement[0];
            }
        });


        final ResponseHandler<String> responseHandler = new BasicResponseHandler();
        try {
            responseBody = client.execute(httpget, responseHandler);
            Log.e("Response to:", requestString + "\n" + responseBody);

            String city = "";
            try {
                Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
                LocalizedGeopositionGet jelem = gson.fromJson(responseBody, LocalizedGeopositionGet.class);

                if (jelem != null) {
                    Address address = jelem.getAddress();
                    if (address != null) {
                        city = address.getCityDistrictAlias();
                        if ((city != null) && (address.getCityAlias() != null)) {
                            city += ", " + address.getCityAlias();
                        } else {
                            if ((address.getCityAlias() != null)) {
                                city = address.getCityAlias();
                            }
                        }
                    }
                }

                if (city == null) city = "";
//                String[] split = jelem.getLongDisplayName().split(",");
//
//                city = split[0];
//
//                if (split.length > 1) {
//                    city += ", " + split[1];
//                }
//
//                if (split.length > 3) {
//                    city = split[2] + ", " + split[3];
//                }

                /*if (split.length > 3) {
                    city += ", " + split[3];
                }*/

                /*if ((jelem != null) && (jelem.getAddress() != null)) {
                    if (jelem.getAddress().getCityDistrict() != null) {
                        city = jelem.getAddress().getCityDistrict();
                    } else if (jelem.getAddress().getVillage() != null) {
                        city = jelem.getAddress().getVillage();
                    }
                }*/
                return city;
            } catch (Exception e) {
                return "";
            }

        } catch (IOException e1) {
            e1.printStackTrace();
            return "";
        }
    }
}




