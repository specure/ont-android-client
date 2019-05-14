package at.specure.android.configs;

import android.content.Context;
import android.content.SharedPreferences;

import com.specure.opennettest.R;

public class PrivacyConfig {

    public static final String PERSISTEN_CLIENT_UUID_KEY = "persistent_client_uuid_enabled";
    public static final String PERSISTEN_CLIENT_UUID_KEY_2 = "persistent_client_uuid_enabled_2";
    public static final String CRASH_ANALYTICS_PERMITTED_KEY = "crash_analytics_enabled";
    public static final String CRASH_ANALYTICS_PERMITTED_KEY_2 = "crash_analytics_enabled_2";


    private static Boolean uuidPersistent = null;

    /**
     * Value is set by preference fragment
     *
     * @param context
     * @return
     */
    public static boolean isClientUUIDPersistent(Context context) {
        boolean persistent = false;
        if (context != null) {
            if (uuidPersistent == null) {
                SharedPreferences sharedPreferences = PreferenceConfig.getPreferenceSharedPreferences(context);
                persistent = sharedPreferences.getBoolean(PERSISTEN_CLIENT_UUID_KEY, false);
            }
        }
        return persistent;
    }

    /**
     * Value is set by preference fragment
     *
     * @param context
     * @return
     */
    public static boolean isAnalyticsPermitted(Context context) {
        boolean permitted = false;
        if (context != null) {
            SharedPreferences sharedPreferences = PreferenceConfig.getPreferenceSharedPreferences(context);
            permitted = sharedPreferences.getBoolean(CRASH_ANALYTICS_PERMITTED_KEY, false);
            return true;
        }
        return permitted;
    }

    public static void setAnalyticsPermitted(Context context, boolean permitted) {
        if (context != null) {
            SharedPreferences sharedPreferences = PreferenceConfig.getPreferenceSharedPreferences(context);
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putBoolean(CRASH_ANALYTICS_PERMITTED_KEY, permitted);
            edit.commit();
        }
    }

    public static void setClientUUIDPersistent(Context context, boolean persistent) {
        if (context != null) {
            SharedPreferences sharedPreferences = PreferenceConfig.getPreferenceSharedPreferences(context);
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putBoolean(PERSISTEN_CLIENT_UUID_KEY, persistent);
            edit.commit();
        }
    }

    public static void setPublishPersonalData(Context context, boolean persistent) {
        ConfigHelper.setInformationCommissioner(context, persistent);
    }

    public static boolean isPublishPersonalDataEnabled(Context context) {
        return ConfigHelper.isInformationCommissioner(context);
    }

    public static void updateSettings(Context context) {
        if (context != null) {

            SharedPreferences sharedPreferences = ConfigHelper.getSharedPreferences(context);
            boolean uuid = sharedPreferences.getBoolean(PERSISTEN_CLIENT_UUID_KEY_2, false);
            boolean analytics = sharedPreferences.getBoolean(CRASH_ANALYTICS_PERMITTED_KEY_2, false);
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putBoolean(PERSISTEN_CLIENT_UUID_KEY, uuid);
            edit.putBoolean(CRASH_ANALYTICS_PERMITTED_KEY, analytics);
            edit.commit();
        }
    }

    public static boolean showPublishPersonalDataInSettings(Context context) {
        return false;
    }


}
