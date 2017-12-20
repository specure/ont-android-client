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

package at.specure.android.configs;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;


/**
 * Created by michal.cadrik on 8/24/2017.
 */

@SuppressWarnings("ConstantConditions")
public class GPSConfig {

    public static Location getLastKnownLocation(Context context, LocationListener listener) {

        LocationManager location_manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Location location = null;

        boolean networkLocationEnabled = location_manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean gpsLocationEnabled = location_manager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (networkLocationEnabled || gpsLocationEnabled) {

            if (location_manager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER) && networkLocationEnabled) {
                location_manager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER, 2000, 2000, listener);
                location = location_manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            } else if (location_manager.getAllProviders().contains(LocationManager.GPS_PROVIDER) && gpsLocationEnabled) {
                location_manager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, 2000, 2000, listener);
                location = location_manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
        } else {
            return location;
        }

        return location;
    }

    public static boolean isEnabledGPS(Context context) {

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static boolean isEnabledAnyGeopositioning(Context context) {

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER);
    }
}

