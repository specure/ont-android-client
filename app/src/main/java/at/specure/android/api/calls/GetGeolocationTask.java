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

import at.specure.android.screens.main.MainActivity;
import at.specure.android.util.EndStringTaskListener;
import at.specure.android.api.reqres.geolocation.Geolocation;

public class GetGeolocationTask extends AsyncTask<Void, Void, String> {
    /**
     *
     */
    private static final String DEBUG_TAG = "GetMapOptionsInfoTask";
    private double MINIMAL_DISTANCE_TO_UPDATE_LOCATION_NAME_IN_KILOMETRES = 0.5;

    /**
     *
     */
    private final MainActivity activity;

    /**
     *
     */
    private EndStringTaskListener endTaskListener;

    private double lat;
    private double lng;
    private String actualLocation = null;
    /**
     *
     */
    private boolean hasError = false;

    /**
     * @param activity
     */
    public GetGeolocationTask(final MainActivity activity, double lat, double lng) {
        this.activity = activity;
        this.lat = lat;
        this.lng = lng;
    }

    /**
     *
     */
    @Override
    protected String doInBackground(final Void... params) {
        String result = null;

        if (actualLocation != null) {
            return actualLocation;
        }

        try {
            actualLocation = Geolocation.requestDecodeLocation(lat, lng, null);
            return actualLocation;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     *
     */
    @Override
    protected void onCancelled() {

    }

    /**
     *
     */
    @Override
    protected void onPostExecute(final String result) {
        if ((result != null) && (endTaskListener != null)) {
            endTaskListener.taskEnded(result);
        }
    }

    /**
     * @param endTaskListener
     */
    public void setEndTaskListener(final EndStringTaskListener endTaskListener) {
        this.endTaskListener = endTaskListener;
    }

    /**
     * @return
     */
    public boolean hasError() {
        return hasError;
    }

    public boolean shouldUpdate(double lat, double lng, boolean forceUpdate) {
        if (forceUpdate) {
            this.lat = lat;
            this.lng = lng;
            return true;
        }


        double distanceInKm = getDistanceInKm(this.lng, this.lat, lng, lat);
        if (distanceInKm > MINIMAL_DISTANCE_TO_UPDATE_LOCATION_NAME_IN_KILOMETRES) {
            this.lat = lat;
            this.lng = lng;
            actualLocation = null;
            return true;
        }
        return false;
    }

    public double getDistanceInKm(double lon1, double lat1, double lon2, double lat2) {
        double R = 6371; // Radius of the earth in km
        double dLat = Math.toRadians(lat2 - lat1);  // Javascript functions in radians
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = R * c; // Distance in km
        return d;
    }

    public String getLocationString() {
        return actualLocation;
    }
}
