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
        return MAP_TYPE_MAPBOX;
    }

    private static int getUserChosenMap(Context context) {
        return MAP_TYPE_MAPBOX;
    }

    public static void saveUserChosenMap(int mapType, Context context) {
        SharedPreferences.Editor edit = getSharedPreferences(context).edit();
        edit.putInt(USER_CHOSEN_MAP, mapType);
        edit.apply();
    }


    public static int getInitialPointWhenGPSOff(Context context) {
        return 0;
    }

    public static boolean pointsOverlayEnabled(Context context) {
        return false;
    }

}
