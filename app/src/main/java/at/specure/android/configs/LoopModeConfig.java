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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static at.specure.android.configs.ConfigHelper.getSharedPreferences;

/**
 * Created by michal.cadrik on 8/24/2017.
 */

@SuppressWarnings("UnnecessaryLocalVariable")
public class LoopModeConfig {

    private static final String SPLIT_CHAR = "_";

    public static final String LOOP_MODE_ENABLED = "loop_mode";
    public static final String LOOP_MODE_GPS_ENABLED = "loop_mode_gps";
    public static final String LOOP_MODE_QOS_DISABLED = "loop_mode_disable_qos";
    public static final String LOOP_MODE_CURRENTLY_PERFORMING = "loop_mode_currently_performing";
    public static final String LOOP_MODE_CURRENTLY_PERFORMING_TEST_NUMBER = "loop_mode_currently_performing_test_number";  // from 1 to...
    public static final String LOOP_MODE_CURRENTLY_PERFORMING_LOOP_UUID = "loop_mode_currently_performing_loop_uuid";
    public static final String LOOP_MODE_LAST_ENDED_TEST_TIME = "loop_mode_last_ended_test_time";

    public static final String LOOP_MODE_DOWNLOADS = "loop_mode_downloads";
    public static final String LOOP_MODE_UPLOADS = "loop_mode_uploads";
    public static final String LOOP_MODE_PINGS = "loop_mode_pings";
    public static final String LOOP_MODE_JITTERS = "loop_mode_jitters";
    public static final String LOOP_MODE_PACKET_LOSSES = "loop_mode_packet_loses";
    public static final String LOOP_MODE_QOSES = "loop_mode_qoses";
    public static final String LOOP_MODE_QOSES_MAX = "loop_mode_qoses_max"; //max number of successful qos tests
    private static String currentLoopId;

    /**
     *
     * @param context
     * @return true if loop mode is enabled in the settings by the user and therefore it could be executed by the start button
     */
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

    public static void setLoopModeMaxTests(final Context context, int maxtests) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt("loop_mode_max_tests_picker", maxtests);
        editor.apply();
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
        editor.commit();
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
        editor.commit();
        resetMedianValues(context);
    }

    public static void resetMedianValues(Context context) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.remove(LOOP_MODE_DOWNLOADS);
        editor.remove(LOOP_MODE_UPLOADS);
        editor.remove(LOOP_MODE_PINGS);
        editor.remove(LOOP_MODE_PACKET_LOSSES);
        editor.remove(LOOP_MODE_JITTERS);
        editor.remove(LOOP_MODE_QOSES);
        editor.commit();
    }

    public static void setQosTestMax(Integer qos, Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(LOOP_MODE_QOSES_MAX, qos);
        boolean commit = editor.commit();
    }

    public static Integer getQosTestMax(Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        int anInt = sharedPreferences.getInt(LOOP_MODE_QOSES_MAX, -1);
        return anInt;
    }

    public static void setQosResultValues(float qos, Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        Set<String> currentLoopQOSes = getCurrentLoopQOSes(context);
        int currentTestNumber = getCurrentTestNumber(context);
        currentLoopQOSes.add(currentTestNumber + SPLIT_CHAR + String.valueOf(qos));
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(LOOP_MODE_QOSES, currentLoopQOSes);
        boolean commit = editor.commit();
    }

    public static void setCurrentTestValues(Float downloadSpeed, Float uploadSpeed, Float ping, Float jitter, Float packetLoss, Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);

        Set<String> currentLoopDownloads = getCurrentLoopDownloads(context);
        Set<String> currentLoopUploads = getCurrentLoopUploads(context);
        Set<String> currentLoopPings = getCurrentLoopPings(context);
        Set<String> currentLoopJitters = getCurrentLoopJitters(context);
        Set<String> currentLoopPacketLosses = getCurrentLoopPacketLosses(context);

        int currentTestNumber = getCurrentTestNumber(context);

        if (downloadSpeed != null) {
            currentLoopDownloads.add(currentTestNumber + SPLIT_CHAR + String.valueOf(downloadSpeed));
        }
        if (uploadSpeed != null) {
            currentLoopUploads.add(currentTestNumber + SPLIT_CHAR + String.valueOf(uploadSpeed));
        }
        if (ping != null) {
            currentLoopPings.add(currentTestNumber + SPLIT_CHAR + String.valueOf(ping));
        }
        if (jitter != null) {
            currentLoopJitters.add(currentTestNumber + SPLIT_CHAR + String.valueOf(jitter));
        }
        if (packetLoss != null) {
            currentLoopPacketLosses.add(currentTestNumber + SPLIT_CHAR + String.valueOf(packetLoss));
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putStringSet(LOOP_MODE_DOWNLOADS, currentLoopDownloads);
        editor.putStringSet(LOOP_MODE_UPLOADS, currentLoopUploads);
        editor.putStringSet(LOOP_MODE_PINGS, currentLoopPings);
        editor.putStringSet(LOOP_MODE_JITTERS, currentLoopJitters);
        editor.putStringSet(LOOP_MODE_PACKET_LOSSES, currentLoopPacketLosses);
        editor.apply();
    }

    public static Set<String> getCurrentLoopDownloads(Context context) {
        Set<String> stringSet = getSharedPreferences(context).getStringSet(LOOP_MODE_DOWNLOADS, new HashSet<String>());
        return stringSet;
    }

    public static Set<String> getCurrentLoopUploads(Context context) {
        Set<String> stringSet = getSharedPreferences(context).getStringSet(LOOP_MODE_UPLOADS, new HashSet<String>());
        return stringSet;
    }

    public static Set<String> getCurrentLoopPings(Context context) {
        Set<String> stringSet = getSharedPreferences(context).getStringSet(LOOP_MODE_PINGS, new HashSet<String>());
        return stringSet;
    }

    public static Set<String> getCurrentLoopJitters(Context context) {
        Set<String> stringSet = getSharedPreferences(context).getStringSet(LOOP_MODE_JITTERS, new HashSet<String>());
        return stringSet;
    }

    public static Set<String> getCurrentLoopPacketLosses(Context context) {
        Set<String> stringSet = getSharedPreferences(context).getStringSet(LOOP_MODE_PACKET_LOSSES, new HashSet<String>());
        return stringSet;
    }

    public static Set<String> getCurrentLoopQOSes(Context context) {
        Set<String> stringSet = getSharedPreferences(context).getStringSet(LOOP_MODE_QOSES, new HashSet<String>());
        return stringSet;
    }

    public static String getCurrentDownloadsMedian(Context context) {
        Set<String> stringSet = getCurrentLoopDownloads(context);
        return getMedianFormatted(stringSet);
    }

    public static String getCurrentUploadsMedian(Context context) {
        Set<String> stringSet = getCurrentLoopUploads(context);
        return getMedianFormatted(stringSet);
    }

    public static String getCurrentLoopPingMedian(Context context) {
        Set<String> stringSet = getCurrentLoopPings(context);
        return getMedianFormatted(stringSet);
    }

    public static String getCurrentLoopJitterMedian(Context context) {
        Set<String> stringSet = getCurrentLoopJitters(context);
        return getMedianFormatted(stringSet);
    }

    public static String getCurrentLoopPacketLossesMedian(Context context) {
        Set<String> stringSet = getCurrentLoopPacketLosses(context);
        return getMedianFormatted(stringSet);
    }

    public static String getCurrentLoopQOSesMedian(Context context) {
        Set<String> stringSet = getCurrentLoopQOSes(context);
        return getMedianFormatted(stringSet);
    }

    private static String getMedianFormatted(Set<String> stringSet) {
        String result = "-";
        if (stringSet.isEmpty()) {
            return result;
        } else {
            ArrayList<Float> values = new ArrayList<>();
            for (String s : stringSet) {
                try {
                    String[] split = s.split(SPLIT_CHAR);
                    values.add(Float.parseFloat(split[1]));
                } catch (Exception e) {
                    //ignored
                }
            }
            Float aFloat = calculateMedian(values);
            result = formatResult(aFloat);

        }
        return result;
    }


    public static String formatResult(Float result) {
        DecimalFormat decimalFormat;
        if (result < 1f) {
            decimalFormat = new DecimalFormat("#.##");
        } else if (result < 10f) {
            decimalFormat = new DecimalFormat("#.#");
        } else {
            decimalFormat = new DecimalFormat("#");
        }

        //rounding to tens
        if (result > 100) {
            result = result / 10;
            int round = Math.round(result);
            round = round * 10;
            return String.valueOf(round);
        } else {
            return decimalFormat.format(result);
        }

    }


    public static Float calculateMedian(ArrayList<Float> values) {
        float median = 0;

        if (values.size() > 0) {
            Collections.sort(values);
            System.out.print("Sorted Scores: ");
            for (float x : values) {
                System.out.print(x + " ");
            }
            double pos1 = Math.floor((values.size() - 1.0) / 2.0);
            double pos2 = Math.ceil((values.size() - 1.0) / 2.0);
            if (pos1 == pos2) {
                median = values.get((int) pos1);
            } else {
                median = (values.get((int) pos1) + values.get((int) pos2)) / 2.0f;
            }
        }
        return median;

    }
}
