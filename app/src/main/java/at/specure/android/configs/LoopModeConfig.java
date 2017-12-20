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

public class LoopModeConfig {

    public static final String LOOP_MODE_ENABLED = "loop_mode";
    public static final String LOOP_MODE_GPS_ENABLED = "loop_mode_gps";
    public static final String LOOP_MODE_QOS_DISABLED = "loop_mode_disable_qos";
    public static final String LOOP_MODE_CURRENTLY_PERFORMING = "loop_mode_currently_performing";
    public static final String LOOP_MODE_CURRENTLY_PERFORMING_TEST_NUMBER = "loop_mode_currently_performing_test_number";  // from 1 to...

    public static boolean isLoopMode(final Context context) {

        if (FeatureConfig.SHOW_LOOP_MODE_FOR_ALL) {
            return getSharedPreferences(context).getBoolean(LOOP_MODE_ENABLED, false);
        } else {
            return false;
        }
    }

    public static boolean isLoopModeGPS(final Context context) {

        if (FeatureConfig.SHOW_LOOP_MODE_FOR_ALL) {
            return getSharedPreferences(context).getBoolean(LOOP_MODE_GPS_ENABLED, true);
        } else {
            return false;
        }
    }

    public static boolean isLoopModeQosDisabled(final Context context) {

        if (FeatureConfig.SHOW_LOOP_MODE_FOR_ALL) {
            return getSharedPreferences(context).getBoolean(LOOP_MODE_QOS_DISABLED, true);
        } else {
            return false;
        }
    }

//    public static void setLoopModeQosDisabled(final Context context, boolean disabled) {
//
//        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
//        editor.putBoolean(LOOP_MODE_QOS_DISABLED, disabled);
//        editor.apply();
//    }

    public static int getLoopModeMaxTests(final Context context) {

        return getInt(context, "loop_mode_max_tests", R.integer.default_loop_max_tests);
    }

    /**
     * In seconds
     *
     * @param context
     * @return
     */
    public static int getLoopModeMinDelay(final Context context) {

        return getInt(context, "loop_mode_min_delay", R.integer.default_loop_min_delay);
    }

    /**
     * In seconds
     *
     * @param context
     * @return
     */
    public static int getLoopModeMaxDelay(final Context context) {

        return getInt(context, "loop_mode_max_delay", R.integer.default_loop_max_delay);
    }

    public static int getLoopModeMaxMovement(final Context context) {

        return getInt(context, "loop_mode_max_movement", R.integer.default_loop_max_movement);
    }

    private static int getInt(final Context context, String key, int defaultId) {

        final int def = context.getResources().getInteger(defaultId);
        final String string = getSharedPreferences(context).getString(key, Integer.toString(def));
        try {
            return Integer.parseInt(string);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public static boolean isCurrentlyPerformingLoopMode(Context context) {
        if (FeatureConfig.SHOW_LOOP_MODE_FOR_ALL) {
            return getSharedPreferences(context).getBoolean(LOOP_MODE_CURRENTLY_PERFORMING, false);
        } else {
            return false;
        }
    }

    public static void setCurrentlyPerformingLoopMode(final Context context, boolean isPerforming) {

        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(LOOP_MODE_CURRENTLY_PERFORMING, isPerforming);
        editor.apply();
    }

    public static void resetCurrentTestNumber(Context context) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt(LOOP_MODE_CURRENTLY_PERFORMING_TEST_NUMBER, 1);
        editor.apply();
    }

    public static void incrementCurrentTestNumber(Context context) {
        int testNumber = getSharedPreferences(context).getInt(LOOP_MODE_CURRENTLY_PERFORMING_TEST_NUMBER, 1);
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt(LOOP_MODE_CURRENTLY_PERFORMING_TEST_NUMBER, ++testNumber);
        editor.apply();
    }

    public static int getCurrentTestNumber(Context context) {
       return getSharedPreferences(context).getInt(LOOP_MODE_CURRENTLY_PERFORMING_TEST_NUMBER, 1);
    }
}
