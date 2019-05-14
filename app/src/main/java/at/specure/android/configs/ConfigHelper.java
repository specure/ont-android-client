/*******************************************************************************
 * Copyright 2014-2017 Specure GmbH
 * Copyright 2013-2015 alladin-IT GmbH
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
 ******************************************************************************/
package at.specure.android.configs;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.specure.opennettest.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import at.specure.android.screens.result.adapter.result.QoSCategoryPagerAdapter;
import at.specure.client.v2.task.result.QoSTestResultEnum;

import static at.specure.client.helper.Config.RMBT_CONTROL_PORT;
import static at.specure.client.helper.Config.RMBT_CONTROL_SSL;
import static at.specure.client.helper.Config.RMBT_QOS_SSL;

@SuppressWarnings("UnnecessaryLocalVariable")
public final class ConfigHelper {
    public final static String PREF_KEY_SWIPE_INTRO_COUNTER = "swipe_intro_counter";
    public final static int SWIPE_INTRO_COUNTER_MAX = 2;

    public static final int ZERO_MEASUREMENT_DETECT_NEVER = 0; // unused for now (because of global config)
    public static final int ZERO_MEASUREMENT_DETECT_SETTINGS = 1;
    public static final int ZERO_MEASUREMENT_DETECT_ALWAYS = 2;


    /**
     * save log in files locally?
     */
    public final static boolean DEFAULT_REDIRECT_SYSOUT_TO_FILE = false;

    /**
     * send log files to control server?
     */
    public final static boolean DEFAULT_SEND_LOG_TO_CONTROL_SERVER = false;
    private static AtomicReference<String> mapHost = new AtomicReference<String>();
    private static AtomicInteger mapPort = new AtomicInteger();
    private static AtomicBoolean mapSSL = new AtomicBoolean();
    private static ConcurrentMap<String, String> volatileSettings = new ConcurrentHashMap<String, String>();

    public static SharedPreferences getSharedPreferences(final Context context) {
        return PreferenceConfig.getPreferenceSharedPreferences(context);
    }

    public static void setUUID(final Context context, final String uuid) {
        final SharedPreferences pref = getSharedPreferences(context);
        final boolean devMode = isOverrideControlServer(pref);
        pref.edit().putString(devMode ? "uuid_dev" : "uuid", uuid).commit();
    }

    public static String getUUID(final Context context) {
        SharedPreferences sharedPreferences = getSharedPreferences(context);
        final boolean devMode = isOverrideControlServer(sharedPreferences);

        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String oldUuid = defaultSharedPreferences.getString(devMode ? "uuid_dev" : "uuid", "");

        String uuid = sharedPreferences.getString(devMode ? "uuid_dev" : "uuid", "");

        if ((uuid != null) && (!uuid.isEmpty())) {
            return uuid;
        } else {
            if ((!oldUuid.isEmpty())) {
                setUUID(context, oldUuid);
                SharedPreferences.Editor edit = defaultSharedPreferences.edit();
                edit.putString("uuid", "");
                edit.putString("uuid_dev", "");
                edit.commit();
                return uuid;
            }
        }
        return uuid;
    }

    public static void setLastNewsUid(final Context context, final long newsUid) {
        getSharedPreferences(context).edit().putLong("lastNewsUid", newsUid).apply();
    }

    public static void setControlServerVersion(final Context context, final String controlServerVersion) {
        getSharedPreferences(context).edit().putString("controlServerVersion", controlServerVersion).apply();
    }

    public static String getControlServerVersion(final Context context) {
        return getSharedPreferences(context).getString("controlServerVersion", null);
    }

    public static void setLastIp(final Context context, final String lastIp) {
        getSharedPreferences(context).edit().putString("lastIp", lastIp).apply();
    }

    public static String getLastIp(final Context context) {
        return getSharedPreferences(context).getString("lastIp", null);
    }

    public static void setLastTestUuid(final Context context, final String lastUuid) {
        getSharedPreferences(context).edit().putString("lastTestUuid", lastUuid).apply();
    }

    public static String getLastTestUuid(final Context context, boolean isDeleteOldValue) {
        String lastTestUuid = getSharedPreferences(context).getString("lastTestUuid", null);
        if (isDeleteOldValue) {
            setLastTestUuid(context, null);
        }

        return lastTestUuid;
    }

    public static long getLastNewsUid(final Context context) {
        return getSharedPreferences(context).getLong("lastNewsUid", 0);
    }

    public static boolean isSystemOutputRedirectedToFile(final Context context) {
        return getSharedPreferences(context).getBoolean("dev_debug_output", DEFAULT_REDIRECT_SYSOUT_TO_FILE);
    }

    public static boolean isDontShowMainMenuOnClose(final Context context) {
        return getSharedPreferences(context).getBoolean("dont_show_menu_before_exit", false);
    }

    public static boolean isGPS(final Context context) {
        return !getSharedPreferences(context).getBoolean("no_gps", false);
    }

    /////////////////////////////////////////////////////////////////
    // Information Commissioner feature (requested by AKOS)
    /////////////////////////////////////////////////////////////////

    public static boolean isNDT(final Context context) {
        return getSharedPreferences(context).getBoolean("ndt", false);
    }

    public static void setNDT(final Context context, final boolean value) {
        getSharedPreferences(context).edit().putBoolean("ndt", value).apply();
    }

    public static boolean isNDTDecisionMade(final Context context) {
        return getSharedPreferences(context).getBoolean("ndt_decision", false);
    }

    public static void setNDTDecisionMade(final Context context, final boolean value) {
        getSharedPreferences(context).edit().putBoolean("ndt_decision", value).apply();
    }

    /////////////////////////////////////////////////////////////////   

    public static boolean isInformationCommissioner(final Context context) {
        return getSharedPreferences(context).getBoolean("information_commissioner", false);
    }

    public static void setInformationCommissioner(final Context context, final boolean value) {
        getSharedPreferences(context).edit().putBoolean("information_commissioner", value).apply();
    }

    public static boolean isICDecisionMade(final Context context) {
        return getSharedPreferences(context).getBoolean("ic_decision", false);
    }

    public static void setICDecisionMade(final Context context, final boolean value) {
        getSharedPreferences(context).edit().putBoolean("ic_decision", value).apply();
    }

    public static String getPreviousTestStatus(final Context context) {
        return getSharedPreferences(context).getString("previous_test_status", null);
    }

    public static void setPreviousTestStatus(final Context context, final String status) {
        getSharedPreferences(context).edit().putString("previous_test_status", status).apply();
    }

    public static int getTestCounter(final Context context) {
        int counter = getSharedPreferences(context).getInt("test_counter", 0);
        return counter;
    }

    public static int incAndGetNextTestCounter(final Context context) {
        int lastValue = getSharedPreferences(context).getInt("test_counter", 0);
        lastValue++;
        getSharedPreferences(context).edit().putInt("test_counter", lastValue).apply();
        return lastValue;
    }

    public static boolean setHistoryIsDirty(final Context context, boolean isDirty) {
        if (context != null) {
            SharedPreferences.Editor edit = getSharedPreferences(context).edit();
            edit.putBoolean("history_dirty", isDirty);
            edit.commit();
        }
        return false;
    }

    public static boolean getHistoryIsDirty(final Context context) {
        boolean isHistoryDirty = true;
        if (context != null) {
            isHistoryDirty = getSharedPreferences(context).getBoolean("history_dirty", true);
        }
        return isHistoryDirty;
    }

    public static boolean useNetworkInterfaceIpMethod(final Context context) {
        return getSharedPreferences(context).getBoolean("dev_debug_ip_method", false);
    }

    public static boolean isRetryRequiredOnIpv6SocketTimeout(final Context context) {
        return getSharedPreferences(context).getBoolean("dev_debug_ip_retry_on_socket_timeout", false);
    }

    public static boolean isIpPolling(final Context context) {
        return getSharedPreferences(context).getBoolean("dev_debug_ip_poll", true);
    }

    public static boolean isTCAccepted(final Context context) {
        if (!context.getResources().getBoolean(R.bool.show_terms_and_conditions_on_start)) {
            //TODO: check with Jozef
            setTCAccepted(context, true);
        }
        final int tcNeedVersion = context.getResources().getInteger(R.integer.rmbt_terms_version);
        final int tcAcceptedVersion = getTCAcceptedVersion(context);
        return tcAcceptedVersion == tcNeedVersion;
    }

    public static int getTCAcceptedVersion(final Context context) {
        return getSharedPreferences(context).getInt("terms_and_conditions_accepted_version", 0);
    }

    public static void setTCAccepted(final Context context, final boolean accepted) {
        final int tcVersion = context.getResources().getInteger(R.integer.rmbt_terms_version);
        if (accepted)
            getSharedPreferences(context).edit().putInt("terms_and_conditions_accepted_version", tcVersion).apply();
        else
            getSharedPreferences(context).edit().remove("terms_and_conditions_accepted_version").apply();
    }

    private static boolean isOverrideControlServer(final SharedPreferences pref) {
        return pref.getBoolean("dev_control_override", false);
    }

    private static boolean isOverrideMapServer(final SharedPreferences pref) {
        return pref.getBoolean("dev_map_override", false);
    }

    private static String getDefaultControlServerName(final Context context, final SharedPreferences pref) {
        final boolean ipv4Only = pref.getBoolean("ipv4_only", false);
        if (ipv4Only)
            return getCachedControlServerNameIpv4(context);
        else
            return context.getResources().getString(R.string.default_control_host);
    }

    public static Boolean shouldUpdateUUID(final Context context) {
        String mainDefaultControlServer = getSharedPreferences(context).getString("main_default_control_server", "");
//        return !mainDefaultControlServer.equalsIgnoreCase(context.getString(R.string.default_control_host));
        return false;
    }

    public static void setSecretEntered(Boolean secretEntered, Context context) {
        getSharedPreferences(context).edit().putBoolean("secret_entered", secretEntered).apply();
    }

    public static Boolean isSecretEntered(Context context) {
        return getSharedPreferences(context).getBoolean("secret_entered", false);
    }

    public static void setCachedIpv4CheckUrl(String url, Context context) {
        getSharedPreferences(context).edit().putString("url_ipv4_check", url).apply();
    }

    public static String getCachedIpv4CheckUrl(Context context) {
        if (context.getResources().getBoolean(R.bool.url_override)) {
            return context.getString(R.string.default_control_check_ipv4_url);
        }
        return getSharedPreferences(context).getString("url_ipv4_check", context.getString(R.string.default_control_check_ipv4_url));
    }

    public static void setCachedIpv6CheckUrl(String url, Context context) {
        getSharedPreferences(context).edit().putString("url_ipv6_check", url).apply();
    }

    public static String getCachedIpv6CheckUrl(Context context) {
        if (context.getResources().getBoolean(R.bool.url_override)) {
            return context.getString(R.string.default_control_check_ipv6_url);
        }
        return getSharedPreferences(context).getString("url_ipv6_check", context.getString(R.string.default_control_check_ipv6_url));
    }

    public static void setCachedControlServerNameIpv4(String url, Context context) {
        getSharedPreferences(context).edit().putString("cache_control_ipv4", url).apply();
    }

    public static String getCachedControlServerNameIpv4(Context context) {
        if (context.getResources().getBoolean(R.bool.url_override)) {
            return context.getString(R.string.default_control_host_ipv4_only);
        }
        return getSharedPreferences(context).getString("cache_control_ipv4", context.getString(R.string.default_control_host_ipv4_only));
    }


    public static void setCachedControlServerNameIpv6(String url, Context context) {
        getSharedPreferences(context).edit().putString("cache_control_ipv6", url).apply();
    }

    public static String getCachedControlServerNameIpv6(Context context) {
        if (context.getResources().getBoolean(R.bool.url_override)) {
            return context.getString(R.string.default_control_host_ipv6_only);
        }
        return getSharedPreferences(context).getString("cache_control_ipv6", context.getString(R.string.default_control_host_ipv6_only));


    }

    public static void setSelectedCountryInMapFilter(String countryCode, Context context) {
        getSharedPreferences(context).edit().putString("selected_country_in_map_filter", countryCode).apply();
    }

    public static String getSelectedCountryInMapFilter(Context context) {
        String s = FeatureConfig.countrySpecificOperatorsCountryCode(context);
        return getSharedPreferences(context).getString("selected_country_in_map_filter", (s.isEmpty()) ? "all" : s);
    }

    public static void setSelectedOperatorInMapFilter(String operatorID, Context context) {
        getSharedPreferences(context).edit().putString("selected_operator_in_map_filter", operatorID).apply();
    }

    public static String getSelectedOperatorInMapFilter(Context context) {
        return getSharedPreferences(context).getString("selected_operator_in_map_filter", "");
    }

    public static int getSelectedMeasurementServerId(final Context context) {
        final SharedPreferences pref = getSharedPreferences(context);
        int selected_measurement_server_id = pref.getInt("selected_measurement_server_id", -1);
        return selected_measurement_server_id;
    }

    public static boolean setSelectedMeasurementServerId(final Context context, int id) {
        final SharedPreferences pref = getSharedPreferences(context);
        SharedPreferences.Editor edit = pref.edit();
        edit.putInt("selected_measurement_server_id", id);
        boolean commit = edit.commit();
        return commit;
    }

    public static String getControlServerName(final Context context) {
        final SharedPreferences pref = getSharedPreferences(context);
        final boolean devMode = isOverrideControlServer(pref);
        if (devMode) {
            final boolean noControlServer = pref.getBoolean("dev_no_control_server", false);
            if (noControlServer)
                return null;
            return pref.getString("dev_control_hostname", getDefaultControlServerName(context, pref));
        } else
            return getDefaultControlServerName(context, pref);
    }

    public static int getControlServerPort(final Context context) {
        final SharedPreferences pref = getSharedPreferences(context);
        final boolean devMode = isOverrideControlServer(pref);
        if (devMode) {
            final boolean noControlServer = pref.getBoolean("dev_no_control_server", false);
            if (noControlServer)
                return -1;

            try {
                return Integer.parseInt(pref.getString("dev_control_port", "443"));
            } catch (final NumberFormatException e) {
                return RMBT_CONTROL_PORT;
            }
        } else
            return RMBT_CONTROL_PORT;
    }

    public static boolean isControlSeverSSL(final Context context) {
        final SharedPreferences pref = getSharedPreferences(context);
        final boolean devMode = isOverrideControlServer(pref);
        if (devMode) {
            final boolean noControlServer = pref.getBoolean("dev_no_control_server", false);
            if (noControlServer)
                return false;
            if (pref.contains("dev_control_port"))
                return pref.getBoolean("dev_control_ssl", RMBT_CONTROL_SSL);
            return RMBT_CONTROL_SSL;
        } else
            return RMBT_CONTROL_SSL;
    }

    public static boolean isQoSSeverSSL(final Context context) {
        final SharedPreferences pref = getSharedPreferences(context);
        return pref.getBoolean("dev_debug_qos_ssl", RMBT_QOS_SSL);
    }

    public static String getMapServerName(final Context context) {
        final SharedPreferences pref = getSharedPreferences(context);
        final boolean devMode = isOverrideMapServer(pref);
        if (devMode)
            return pref.getString("dev_map_hostname", "develop");
        else {
            if (context.getResources().getBoolean(R.bool.url_override)) {
                return context.getResources().getString(R.string.default_map_server_url);
            } else {
                final String host = mapHost.get();
                if (host != null)
                    return host;
                else
                    return getControlServerName(context);
            }
        }
    }

    public static int getMapServerPort(final Context context) {
        final SharedPreferences pref = getSharedPreferences(context);
        final boolean devMode = isOverrideMapServer(pref);

        if (devMode) {
            try {
                return Integer.parseInt(pref.getString("dev_map_port", "443"));
            } catch (final NumberFormatException e) {
                return -1;
            }
        } else {

            if (context.getResources().getBoolean(R.bool.url_override)) {
                return 443;
            } else {
                if (mapHost.get() != null)
                    return mapPort.get();
                else
                    return getControlServerPort(context);
            }
        }
    }

    public static boolean isMapSeverSSL(final Context context) {
        final SharedPreferences pref = getSharedPreferences(context);
        final boolean devMode = isOverrideMapServer(pref);
        if (devMode)
            return pref.getBoolean("dev_map_ssl", true);
        else {
            if (context.getResources().getBoolean(R.bool.url_override)) {
                return true;
            } else {
                if (mapHost.get() != null)
                    return mapSSL.get();
                else
                    return isControlSeverSSL(context);
            }
        }
    }

    public static String getTag(final Context context) {
        return getSharedPreferences(context).getString("tag", null);
    }

    public static void setMapServer(final String host, final int port, final boolean ssl) {
        mapHost.set(host);
        mapPort.set(port);
        mapSSL.set(ssl);
    }

    public static ConcurrentMap<String, String> getVolatileSettings() {
        return volatileSettings;
    }

    public static String getVolatileSetting(String key) {
        return volatileSettings.get(key);
    }

    public static void setCachedStatisticsUrl(String url, Context context) {
        getSharedPreferences(context).edit().putString("cache_statistics_url", url).apply();
    }

    public static String getCachedStatisticsUrl(Context context) {
        return context.getString(R.string.url_statistics);
    }

    public static void setCachedHelpUrl(String url, Context context) {
        getSharedPreferences(context).edit().putString("cache_help_url", url).apply();
    }

    public static String getCachedHelpUrl(Context context) {
        return context.getString(R.string.url_help);
    }

    public static boolean isNerdModeEnabled(final Context context) {
        return getSharedPreferences(context).getBoolean("nerd_mode", false);
    }

    public static boolean isQosEnabled(final Context context) {
        return getSharedPreferences(context).getBoolean("enable_qos", true);
    }

    /**
     * @param context
     * @param qosNamesMap
     */
    public static void setCachedQoSNames(Map<String, String> qosNamesMap, Context
            context) {
        final SharedPreferences prefs = context.getSharedPreferences("cache_qos_names", Context.MODE_PRIVATE);
        final SharedPreferences.Editor edit = prefs.edit();
        for (Entry<String, String> e : qosNamesMap.entrySet()) {
            edit.putString(e.getKey(), e.getValue());
        }
        edit.apply();
    }

    /**
     * @param context
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Map<QoSTestResultEnum, String> getCachedQoSNames(Context context) {
        final Map<QoSTestResultEnum, String> namesMap = new HashMap<QoSTestResultEnum, String>();
        final SharedPreferences prefs = context.getSharedPreferences("cache_qos_names", Context.MODE_PRIVATE);
        final Map<String, ?> cacheMap = prefs.getAll();

        if (cacheMap != null && cacheMap.size() > 0) {
            Iterator<?> namesIterator = cacheMap.entrySet().iterator();
            while (namesIterator.hasNext()) {
                Entry<String, String> name = (Entry<String, String>) namesIterator.next();
                try {
                    namesMap.put(QoSTestResultEnum.valueOf(name.getKey().toUpperCase(Locale.US)), name.getValue());
                } catch (IllegalArgumentException e) {
                    //in case the qos type doesn't exist
                    e.printStackTrace();
                }
            }
        } else {
            for (Entry<QoSTestResultEnum, Integer> e : QoSCategoryPagerAdapter.TITLE_MAP.entrySet()) {
                String name = context.getString(e.getValue());
                namesMap.put(e.getKey(), name);
            }
        }

        return namesMap;
    }

    /**
     * @param testType
     * @param context
     * @return
     */
    public static String getCachedQoSNameByTestType(QoSTestResultEnum testType, Context
            context) {
        final SharedPreferences prefs = context.getSharedPreferences("cache_qos_names", Context.MODE_PRIVATE);
        String name = prefs.getString(testType.name(), null);
        if (name == null) {
            if (QoSCategoryPagerAdapter.TITLE_MAP.containsKey(testType)) {
                name = context.getString(QoSCategoryPagerAdapter.TITLE_MAP.get(testType));
            } else {
                name = testType.name();
            }
        }

        return name;
    }

    public static boolean isDevEnabled(final Context ctx) {
        return PackageManager.SIGNATURE_MATCH == ctx.getPackageManager().checkSignatures(ctx.getPackageName(), Config.RMBT_DEV_UNLOCK_PACKAGE_NAME);
    }


    public static String getLicensesString(final Context context) {
        String prompt = "";
        try {
            InputStream inputStream = context.getResources().openRawResource(R.raw.licenses);
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            prompt = new String(buffer);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prompt;
    }

    public static String getErrorString(final Context context) {
        String prompt = "";
        try {
            if (context != null) {
                InputStream inputStream = context.getResources().openRawResource(R.raw.error);
                byte[] buffer = new byte[inputStream.available()];
                inputStream.read(buffer);
                prompt = new String(buffer);
                inputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return prompt;
    }


    public static boolean showZeroMeasurementsPreference(Context context) {
        if (context != null) {
            boolean detectZeroMeasurementGlobal = detectZeroMeasurementGlobal(context);
            int detectZeroMeasurementManner = detectZeroMeasurementManner(context);
            return (ZERO_MEASUREMENT_DETECT_SETTINGS == detectZeroMeasurementManner) && detectZeroMeasurementGlobal;
        }
        return false;
    }

    /**
     * Global for build of customer
     *
     * @param context
     * @return
     */
    public static boolean detectZeroMeasurementGlobal(Context context) {
        if (context != null) {
            return context.getResources().getBoolean(R.bool.detect_zero_measurements);
        }
        return false;
    }


    private static boolean enabledZeroMeasurementsInPreferences(Context context) {
        return false;
    }

    private static int detectZeroMeasurementManner(Context context) {

        return ZERO_MEASUREMENT_DETECT_NEVER;
    }

    /**
     * @param context
     * @return
     */
    public static boolean detectZeroMeasurementEnabled(Context context) {
        if (context != null) {
            int detectZeroMeasurementManner = detectZeroMeasurementManner(context);

            switch (detectZeroMeasurementManner) {
                case ZERO_MEASUREMENT_DETECT_NEVER:
                    return false;
                case ZERO_MEASUREMENT_DETECT_SETTINGS: {
                    boolean enabledZeroMeasurementsInPreferences = enabledZeroMeasurementsInPreferences(context);
                    return detectZeroMeasurementGlobal(context) && enabledZeroMeasurementsInPreferences;
                }
                case ZERO_MEASUREMENT_DETECT_ALWAYS: {
                    return detectZeroMeasurementGlobal(context);
                }
            }
        }
        return false;
    }

    public static boolean shouldBeAppReviewed(Context context) {

        return false;

    }

    public static int measurementCountToDisplayReviewDialog(Context context) {

        return 5;
    }

    public static int daysCountToDisplayReviewDialog(Context context) {

        return 5;

    }

    public static String getMeasurementServerAPIVersion(@NonNull Context context) {
        int apiVersion = 1;
        if (context != null) {
            try {
                apiVersion = 1;
            } catch (Exception ignored) {
            }
        }

        String apiVersionRoute = "";
        switch (apiVersion) {
            case 1:
                break;
            case 2:
                apiVersionRoute = "/V2";
                break;
        }

        return apiVersionRoute;
    }

    public static boolean shouldShowWebsite(Context context) {

        return false;
    }
}
