package at.specure.android.configs;

import android.content.Context;
import android.content.SharedPreferences;

import at.specure.android.screens.preferences.PreferenceFragment;

public class PreferenceConfig {

    public static final String LOGGING_ENABLED = "loggingEnable";

    public static SharedPreferences getPreferenceSharedPreferences(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(
                PreferenceFragment.SETTINGS_SHARED_PREFERENCES_FILE_NAME,
                Context.MODE_PRIVATE);
        return preferences;
    }

    public static boolean isLoggingEnabled(Context context) {
        SharedPreferences preferenceSharedPreferences = getPreferenceSharedPreferences(context);
        return preferenceSharedPreferences.getBoolean(LOGGING_ENABLED, false);
    }

    public static boolean setLoggingEnabled(Context context, boolean enabled) {
        SharedPreferences preferenceSharedPreferences = getPreferenceSharedPreferences(context);
        SharedPreferences.Editor edit = preferenceSharedPreferences.edit();
        edit.putBoolean(LOGGING_ENABLED, enabled);
        return edit.commit();
    }
}
