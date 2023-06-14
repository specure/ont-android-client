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
import android.content.SharedPreferences;

import com.specure.opennettest.R;

import static at.specure.android.configs.ConfigHelper.getSharedPreferences;

/**
 * Created by michal.cadrik on 8/24/2017.
 */

@SuppressWarnings("UnnecessaryLocalVariable")
public class MapConfig {

    //    <!--MAPS-->
//    <!--0 = user choose  - unused because of global config --> in user choice it is app_default
//    <!--1 = google maps -->
//    <!--2 = mapbox maps -->
    public static final String DEFAULT_MAP_MODE = "default_map_mode";
    public static final String DEFAULT_MAP = "default_map"; // only if user choose mode and selected 0
    public static final String USER_CHOSEN_MAP = "user_map_selected";


    public static int MAP_TYPE_GOOGLE = 1;
    public static int MAP_TYPE_MAPBOX = 2;

    public static int MAP_GPS_OFF_INITIAL_POINT_LAST_KNOWN_LOCATION = 0;
    public static int MAP_GPS_OFF_INITIAL_POINT_RESOURCE = 1;


    public static int getMapType(Context context) {
        if (context != null) {
            int integer = context.getResources().getInteger(R.integer.maps_provider);
            if (integer == 0) {
                return getUserChosenMap(context);
            }
            return integer;
        }
        return MAP_TYPE_GOOGLE;
    }

    private static int getUserChosenMap(Context context) {
        int anInt = getSharedPreferences(context).getInt(USER_CHOSEN_MAP, context.getResources().getInteger(R.integer.default_maps_provider_if_user_choose));
        if (anInt == 0) {
            return context.getResources().getInteger(R.integer.default_maps_provider_if_user_choose);
        }
        return anInt;
    }

    public static void saveUserChosenMap(int mapType, Context context) {
        SharedPreferences.Editor edit = getSharedPreferences(context).edit();
        edit.putInt(USER_CHOSEN_MAP, mapType);
        edit.apply();
    }


    public static int getInitialPointWhenGPSOff(Context context) {
        return context.getResources().getInteger(R.integer.initial_point_not_enabled_gps);
    }

    public static boolean pointsOverlayEnabled(Context context) {
        return context.getResources().getBoolean(R.bool.points_overlay_enabled);
    }

    public static boolean sendCountryInMapMarkerRequest(Context context) {
        return context.getResources().getBoolean(R.bool.send_country_in_map_marker);
    }
}
