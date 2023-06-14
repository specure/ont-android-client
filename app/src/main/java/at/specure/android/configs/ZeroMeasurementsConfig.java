package at.specure.android.configs;

import android.content.Context;
import android.content.SharedPreferences;

import com.specure.opennettest.R;

public class ZeroMeasurementsConfig {

    private static final String THRESHOLD_KEY_2G = "zero_measurement_threshold_2g";
    private static final String THRESHOLD_KEY_3G = "zero_measurement_threshold_3g";
    private static final String THRESHOLD_KEY_4G = "zero_measurement_threshold_4g";


    public static Integer get2gThreshold(Context context) {
        Integer threshold = Integer.MIN_VALUE;
        if (context != null) {
            SharedPreferences sharedPreferences = PreferenceConfig.getPreferenceSharedPreferences(context);
            threshold = sharedPreferences.getInt(THRESHOLD_KEY_2G, Integer.MIN_VALUE);
            if (threshold == Integer.MIN_VALUE) {
                threshold = context.getResources().getInteger(R.integer.zero_measurement_2g_threshold);
            }
        }
        return threshold;
    }

    public static Integer get3gThreshold(Context context) {
        Integer threshold = Integer.MIN_VALUE;
        if (context != null) {
            SharedPreferences sharedPreferences = PreferenceConfig.getPreferenceSharedPreferences(context);
            threshold = sharedPreferences.getInt(THRESHOLD_KEY_3G, Integer.MIN_VALUE);
            if (threshold == Integer.MIN_VALUE) {
                threshold = context.getResources().getInteger(R.integer.zero_measurement_3g_threshold);
            }
        }
        return threshold;
    }

    public static Integer get4gThreshold(Context context) {
        Integer threshold = Integer.MIN_VALUE;
        if (context != null) {
            SharedPreferences sharedPreferences = PreferenceConfig.getPreferenceSharedPreferences(context);
            threshold = sharedPreferences.getInt(THRESHOLD_KEY_4G, Integer.MIN_VALUE);
            if (threshold == Integer.MIN_VALUE) {
                threshold = context.getResources().getInteger(R.integer.zero_measurement_4g_threshold);
            }
        }
        return threshold;
    }

    public static boolean set2gThreshold(Context context, Integer threshold) {
        if ((context != null) && (threshold != null)) {
            SharedPreferences sharedPreferences = PreferenceConfig.getPreferenceSharedPreferences(context);
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putInt(THRESHOLD_KEY_2G, threshold);
            return edit.commit();
        }
        return false;
    }

    public static boolean set3gThreshold(Context context, Integer threshold) {
        if ((context != null) && (threshold != null)) {
            SharedPreferences sharedPreferences = PreferenceConfig.getPreferenceSharedPreferences(context);
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putInt(THRESHOLD_KEY_3G, threshold);
            return edit.commit();
        }
        return false;
    }

    public static boolean set4gThreshold(Context context, Integer threshold) {
        if ((context != null) && (threshold != null)) {
            SharedPreferences sharedPreferences = PreferenceConfig.getPreferenceSharedPreferences(context);
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putInt(THRESHOLD_KEY_4G, threshold);
            return edit.commit();
        }
        return false;
    }
}
