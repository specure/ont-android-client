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

import com.specure.opennettest.R;


/**
 * @author lb
 */
//TODO change to build config fields or as boolean values of resources
//	see defaults.xml for other configs (also in respective customer directories)

public class FeatureConfig {

    /**
     * only home, history, map, help, info, settings are shown in menu
     */
    public static boolean SHOW_ONLY_BASIC_MENU = true;

    /**
     * enable/disable traffic warning before test
     */
    public static boolean TEST_SHOW_TRAFFIC_WARNING = false;

    /**
     * enable (true) or disable (false) opendata (=statistics, map dots, ...)
     */
    public static boolean USE_OPENDATA = true;

    /**
     * enable (true) or disable (false) loop mode as a option in settings menu
     */
    public static boolean SHOW_LOOP_MODE_FOR_ALL = true;

    public static boolean showStatisticInMainMenu(Context context) {
        return context.getResources().getBoolean(R.bool.show_statistics_in_main_menu);
    }

    public static boolean showCountrySpecificOperatorsInMapFilters(Context context) {
        return context.getResources().getBoolean(R.bool.country_specific_operators_for_map);
    }
}
