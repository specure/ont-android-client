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

import java.util.Date;

import static at.specure.android.configs.ConfigHelper.getSharedPreferences;

/**
 * Created by michal.cadrik on 8/24/2017.
 */

@SuppressWarnings("UnnecessaryLocalVariable")
public class LoopModeConfig {

    public static final String LOOP_MODE_ENABLED = "loop_mode";
    public static final String LOOP_MODE_GPS_ENABLED = "loop_mode_gps";
    public static final String LOOP_MODE_QOS_DISABLED = "loop_mode_disable_qos";
    public static final String LOOP_MODE_CURRENTLY_PERFORMING = "loop_mode_currently_performing";
    public static final String LOOP_MODE_CURRENTLY_PERFORMING_TEST_NUMBER = "loop_mode_currently_performing_test_number";  // from 1 to...
    public static final String LOOP_MODE_CURRENTLY_PERFORMING_LOOP_UUID = "loop_mode_currently_performing_loop_uuid";
    public static final String LOOP_MODE_LAST_ENDED_TEST_TIME = "loop_mode_last_ended_test_time";
    private static String currentLoopId;

    public static boolean isLoopMode(final Context context) {

        if (FeatureConfig.SHOW_LOOP_MODE_FOR_ALL) {
            return PreferenceConfig.getPreferenceSharedPreferences(context).getBoolean(LOOP_MODE_ENABLED, false);
        } else {
            return false;
        }
    }

    public static boolean isLoopModeGPS(final Context context) {

        if (FeatureConfig.SHOW_LOOP_MODE_FOR_ALL) {
            return PreferenceConfig.getPreferenceSharedPreferences(context).getBoolean(LOOP_MODE_GPS_ENABLED, true);
        } else {
            return false;
        }
    }

    public static boolean isLoopModeQosDisabled(final Context context) {

        if (FeatureConfig.SHOW_LOOP_MODE_FOR_ALL) {
            return PreferenceConfig.getPreferenceSharedPreferences(context).getBoolean(LOOP_MODE_QOS_DISABLED, true);
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

        return getInt(context, "loop_mode_max_tests_picker", R.integer.default_loop_max_tests);
    }

    /**
     * In seconds
     *
     * @param context
     * @return
     */
    public static int getLoopModeMinDelay(final Context context) {
        int loopModeMinDelay = getInt(context, "loop_mode_min_delay_picker", R.integer.default_loop_min_delay);
        int minLoopDelay = context.getResources().getInteger(R.integer.loop_min_delay);
        if (loopModeMinDelay < minLoopDelay) {
            loopModeMinDelay = minLoopDelay;
        }
        return loopModeMinDelay;
    }

//    /**
//     * In seconds
//     *
//     * @param context
//     * @return
//     */
//    public static int getLoopModeMaxDelay(final Context context) {
//        int loopModeMaxDelay = getInt(context, "loop_mode_max_delay", R.integer.default_loop_max_delay);
//        if (loopModeMaxDelay < 30) {
//            loopModeMaxDelay = 30;
//        }
//        return loopModeMaxDelay;
//    }

    public static int getLoopModeMaxMovement(final Context context) {
        int maxMovementInMetres = getInt(context, "loop_mode_max_movement_picker", R.integer.default_loop_max_movement);
        int minLoopDistance = context.getResources().getInteger(R.integer.loop_min_distance);
        if (maxMovementInMetres < minLoopDistance) {
            maxMovementInMetres = minLoopDistance;
        }
        return maxMovementInMetres;

    }

    private static int getInt(final Context context, String key, int defaultId) {

        final int def = context.getResources().getInteger(defaultId);
        final Integer string = PreferenceConfig.getPreferenceSharedPreferences(context).getInt(key, def);
        return string;
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
        if (!isPerforming) {
            setLastTimeEndOfLoopTest(context, new Date().getTime());
        }

    }

    public static void setLastTimeEndOfLoopTest(Context context, Long timestamp) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putLong(LOOP_MODE_LAST_ENDED_TEST_TIME, timestamp);
        editor.apply();
    }

    public static long getLastTimeEndOfLoopTest(Context context) {
        long timestamp = getSharedPreferences(context).getLong(LOOP_MODE_LAST_ENDED_TEST_TIME, -1);
        return timestamp;
    }

    public static long getRemainingTimeToNextLoopTest(Context context) {
        long timestamp = getSharedPreferences(context).getLong(LOOP_MODE_LAST_ENDED_TEST_TIME, -1);
        long time = new Date().getTime();
        if (getCurrentTestNumber(context) == getLoopModeMaxTests(context)) {
            return -1;
        }
        if (timestamp != -1) {
            return time - timestamp;
        } else {
            return -1;
        }
    }

    public static void resetCurrentTestNumber(Context context) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt(LOOP_MODE_CURRENTLY_PERFORMING_TEST_NUMBER, 0);
        editor.putLong(LOOP_MODE_LAST_ENDED_TEST_TIME, -1);
        editor.apply();
    }

    public static void incrementCurrentTestNumber(Context context) {
        int testNumber = getSharedPreferences(context).getInt(LOOP_MODE_CURRENTLY_PERFORMING_TEST_NUMBER, 0);
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt(LOOP_MODE_CURRENTLY_PERFORMING_TEST_NUMBER, ++testNumber);
        editor.apply();
    }

    public static int getCurrentTestNumber(Context context) {
        int anInt = getSharedPreferences(context).getInt(LOOP_MODE_CURRENTLY_PERFORMING_TEST_NUMBER, 1);
        if (anInt == 0) {
            anInt = 1;
        }
        return anInt;
    }

    public static String getCurrentLoopId(Context context) {
        String loopUUID = getSharedPreferences(context).getString(LOOP_MODE_CURRENTLY_PERFORMING_LOOP_UUID, null);
        return loopUUID;
    }

    public static void resetCurrentLoopId(Context context) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.remove(LOOP_MODE_CURRENTLY_PERFORMING_LOOP_UUID);
        editor.apply();
    }

    public static void setCurrentLoopId(String currentLoopId, Context context) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(LOOP_MODE_CURRENTLY_PERFORMING_LOOP_UUID, currentLoopId);
        editor.apply();
    }
}
