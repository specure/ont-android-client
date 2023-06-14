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
package at.specure.android.util;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Looper;
import android.telephony.CellInfo;
import android.telephony.CellSignalStrength;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;

import androidx.core.app.ActivityCompat;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;

import at.specure.android.api.jsons.CellLocation;
import at.specure.android.api.jsons.Signal;
import at.specure.android.api.jsons.TestResultDetails.CellInfoGet;
import at.specure.android.configs.Config;
import at.specure.android.configs.ConfigHelper;
import at.specure.android.configs.LocaleConfig;
import at.specure.android.support.telephony.CellInfoPreV18;
import at.specure.android.support.telephony.CellInfoSupport;
import at.specure.android.support.telephony.TelephonyManagerPreV18;
import at.specure.android.support.telephony.TelephonyManagerSupport;
import at.specure.android.support.telephony.TelephonyManagerV18;
import at.specure.android.util.location.GeoLocationX;
import at.specure.android.util.net.RealTimeInformation;
import at.specure.android.util.network.TransportType;
import at.specure.android.util.network.cell.ActiveDataCellInfo;
import at.specure.android.util.network.cell.ActiveDataCellInfoExtractor;
import at.specure.android.util.network.cell.ActiveDataCellInfoExtractorImpl;
import at.specure.android.util.network.cell.CellNetworkInfo;
import at.specure.android.util.network.network.NRConnectionState;
import at.specure.androidX.data.test.testResultRequest.TestResultProperties;
import at.specure.client.helper.RevisionHelper;
import at.specure.client.v2.task.result.QoSResultCollector;
import at.specure.info.strength.SignalStrengthInfo;
import at.specure.info.strength.SignalStrengthInfoCommon;
import at.specure.info.strength.SignalStrengthInfoGsm;
import at.specure.info.strength.SignalStrengthInfoLte;
import at.specure.info.strength.SignalStrengthInfoNr;
import timber.log.Timber;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.telephony.TelephonyManager.NETWORK_TYPE_GSM;
import static android.telephony.TelephonyManager.NETWORK_TYPE_LTE;
import static android.telephony.TelephonyManager.NETWORK_TYPE_NR;
import static android.telephony.TelephonyManager.NETWORK_TYPE_UNKNOWN;
import static androidx.core.content.PermissionChecker.PERMISSION_GRANTED;
import static androidx.core.content.PermissionChecker.checkSelfPermission;
import static at.specure.android.configs.PermissionHandler.isCoarseLocationPermitted;
import static com.google.gson.stream.JsonToken.NULL;

public class InformationCollector {
    /**
     * set to true if location information should be included to server request
     */
    public final static boolean BASIC_INFORMATION_INCLUDE_LOCATION = true;

    /**
     * set to true if last signal information should be included to server request
     */
    public final static boolean BASIC_INFORMATION_INCLUDE_LAST_SIGNAL_ITEM = true;

    public static final int UNKNOWN = Integer.MIN_VALUE;

    public static final String PLATTFORM_NAME = "Android";
    public static final Integer SIGNAL_TYPE_NO_SIGNAL = 0;
    public static final int SIGNAL_TYPE_MOBILE = 1;
    public static final Integer SINGAL_TYPE_WLAN = 3;
    public static final Integer SINGAL_TYPE_RSRP = 2;

    public static final int SIGNAL_TYPE_WLAN = 3;
    public static final int SIGNAL_TYPE_RSRP = 2;

    private static final String DEBUG_TAG = "InformationCollector";

    private static final int ACCEPT_WIFI_RSSI_MIN = -113;

    /**
     * Returned by getNetwork() if Wifi
     */
    public static final int NETWORK_WIFI = 99;

    /**
     * Returned by getNetwork() if Ethernet
     */
    public static final int NETWORK_ETHERNET = 106;

    /**
     * Returned by getNetwork() if Bluetooth
     */
    public static final int NETWORK_BLUETOOTH = 107;
    private static final int NETWORK_TYPE_LTE_CA = 19;

    private static InformationCollector instance;

    private boolean testInProgress;

    private ConnectivityManager connManager = null;

    private TelephonyManager telManager = null;

    private TelephonyManagerSupport telManagerSupport = null;

    private PhoneStateListener telListener = null;

    private WifiManager wifiManager = null;

    // Handlers and Receivers for phone and network state
    private NetworkStateBroadcastReceiver networkReceiver;

//    private InfoGeoLocation locationManager = null;

    private String testServerName;

//    private Properties fullInfo = null;

    private Context context = null;
    private boolean collectInformation;
    private boolean registerNetworkReiceiver;

    private static final List<at.specure.android.api.jsons.CellLocation> cellLocations = new ArrayList<at.specure.android.api.jsons.CellLocation>();
    public static List<Signal> signals = new ArrayList<Signal>();
    public static Set<NRConnectionState> signalsNRConnectionStates = new HashSet<NRConnectionState>();
    public static List<CellInfoGet> cellsInfos = new ArrayList<>();

    //used when test is not running
    private static AtomicInteger signal = new AtomicInteger(Integer.MIN_VALUE);
    private static AtomicInteger signalType = new AtomicInteger(SIGNAL_TYPE_NO_SIGNAL);
    private static AtomicInteger signalRsrq = new AtomicInteger(UNKNOWN);

    private static final AtomicReference<Signal> lastSignalItem = new AtomicReference<Signal>();
    private final AtomicInteger lastNetworkType = new AtomicInteger(TelephonyManager.NETWORK_TYPE_UNKNOWN);
    private static final AtomicBoolean illegalNetworkTypeChangeDetected = new AtomicBoolean(false);

    public static QoSResultCollector qoSResult;

    public static QoSResultCollector voipResult;
    private TestResultProperties testResultProperties;
    private SubscriptionManager subscriptionManager;
    private ActiveDataCellInfoExtractor activeDataCellInfoExtractor;
    private NRConnectionState lastNRConnectionState = NRConnectionState.NOT_AVAILABLE;
    private SignalStrengthInfo lastSignalStrengthInfo = null;
    private Integer mobileNetworkType = NETWORK_TYPE_UNKNOWN;

   /* public static InformationCollector getInstance(final Context context, final boolean collectInformation, final boolean registerNetworkReceiver) {
        if (instance == null) {
            Timber.d( "new Instance");
            instance = new InformationCollector(context, collectInformation, registerNetworkReceiver);
        } else {
            instance.context = context;
            instance.collectInformation = collectInformation;
            instance.registerNetworkReiceiver = registerNetworkReceiver;
            instance.enableGeoLocation = true;
            instance.getLocationInfo();
            instance.reInit();
        }
        return instance;
    }

    public static InformationCollector getInstance(final Context context, final boolean collectInformation, final boolean registerNetworkReceiver, final boolean enableGeoLocation) {
        if (instance == null) {
            Timber.d( "new Instance");
            instance = new InformationCollector(context, collectInformation, registerNetworkReceiver, enableGeoLocation);
        } else {
            instance.context = context;
            instance.collectInformation = collectInformation;
            instance.registerNetworkReiceiver = registerNetworkReceiver;
            instance.enableGeoLocation = enableGeoLocation;
            instance.getLocationInfo();
//            instance.reInit();
        }

        return instance;
    }

    public static InformationCollector getInstance(final Context context, final boolean collectInformation, final boolean registerNetworkReceiver, final boolean enableGeoLocation, boolean forceInit) {
        if (instance == null) {
            Timber.d( "new Instance");
            instance = new InformationCollector(context, collectInformation, registerNetworkReceiver, enableGeoLocation);
        } else {
            instance.context = context;
            instance.collectInformation = collectInformation;
            instance.registerNetworkReiceiver = registerNetworkReceiver;
            instance.enableGeoLocation = enableGeoLocation;
            instance.getLocationInfo();
            if (forceInit) {
                instance.reInit();
            }
        }

        return instance;
    }*/

    public static InformationCollector getInstance(Context context, final boolean collectInformation, final boolean registerNetworkReceiver, boolean testStarted) {
        if (instance == null) {
            Timber.d("new Instance");
            instance = new InformationCollector(context, collectInformation, registerNetworkReceiver);
        } else {
            instance.context = context;
            instance.collectInformation = collectInformation;
            instance.registerNetworkReiceiver = registerNetworkReceiver;
            Timber.d("SIGNAL CHANGED new Instance CLEARED!");
            instance.init();
        }
        if (testStarted) {
            instance.clearNSAStates();
            illegalNetworkTypeChangeDetected.set(false);
        }
        return instance;
    }

    //todo: change to private and use getInstance
    public InformationCollector(Context context, final boolean collectInformation, final boolean registerNetworkReceiver) {
        // create and load default properties

        this.context = context;
        this.collectInformation = collectInformation;
        this.registerNetworkReiceiver = registerNetworkReceiver;
        Timber.d("SIGNAL CHANGED INIT constructor CLEARED!");
        init();

    }

    //todo: change to private and use start test
    public void init() {

        // this.unload();

        reset(); // ok

        initNetwork(); // ok

        getClientInfo();

        getTelephonyInfo();

        getWiFiInfo();

        registerListeners();

        registerNetworkReceiver();

    }

    public void reInit() {
        reset();

        initNetwork();

        getClientInfo();

        getTelephonyInfo();

        getWiFiInfo();

        registerListeners();

        registerNetworkReceiver();

    }


    public Context getContext() {
        return context;
    }

    public void clearLists() {
        // Reset all Lists but store Last Item for next test.
        if (cellsInfos.size() > 0) {
            final CellInfoGet lastCellInfo = cellsInfos.get(cellsInfos.size() - 1);
            cellsInfos.clear();
            cellsInfos.add(lastCellInfo);
        } else {
            cellsInfos.clear();
        }

        if (cellLocations.size() > 0) {
            final at.specure.android.api.jsons.CellLocation lastCell = cellLocations.get(cellLocations.size() - 1);
            cellLocations.clear();
            cellLocations.add(lastCell);
        } else
            cellLocations.clear();

        if (signals.size() > 0) {
            final Signal lastSignal = signals.get(signals.size() - 1);
            signals.clear();
            Timber.d("SIGNAL CHANGED CLEARED! 1");
            signals.add(lastSignal);
        } else {
            signals.clear();
            Timber.d("SIGNAL CHANGED CLEARED! 2");
        }
    }

    /**
     * This method needs to be called when tests begins
     */
    public void clearNSAStates() {
        if (signalsNRConnectionStates != null) {
            signalsNRConnectionStates.clear();
            Timber.d("5G Signal NR state records cleaned");
        }
    }

    public void reset() {

        testServerName = "";
//        lastLocation = null;

//        lastNetworkType.set(NETWORK_TYPE_UNKNOWN);
//        illegalNetworkTypeChangeDetcted.set(false);
//        Timber.e("RESETING NETWORK TYPE");

        testResultProperties = new TestResultProperties();

        clearLists();
    }

    // removes the listener
    public void unload() {

//        GeoLocationX.getInstance(getContext().getApplicationContext()).removeListener(this);

        unregisterListeners();

        if (connManager != null)
            connManager = null;

        // stop network/wifi listener
        unregisterNetworkReceiver();

        if (wifiManager != null)
            wifiManager = null;

        testResultProperties = null;
    }

    public void stopTest(Context applicationContext) {
        testInProgress = false;
    }

    public void startTest(Context applicationContext) {

        if (applicationContext != null) {
            this.context = applicationContext;
            testInProgress = true;
            illegalNetworkTypeChangeDetected.set(false);

            //clear all lists
            cellLocations.clear();
            signals.clear();
            signalsNRConnectionStates.clear();
            Timber.d("SIGNAL CHANGED CLEARED! 3");
            cellsInfos.clear();
            signal.set(Integer.MIN_VALUE);
            signalType.set(SIGNAL_TYPE_NO_SIGNAL);
            signalRsrq.set(UNKNOWN);
            lastSignalItem.set(null);
            lastNetworkType.set(NETWORK_TYPE_UNKNOWN);
            illegalNetworkTypeChangeDetected.set(false);

            //load managers
            initNetwork();
            //load basic onfo for results as UUID, plattform, model, ...
            getClientInfo();

//            if (connManager != null) {
//                int network = getNetwork();
//                fullInfo.setProperty("NETWORK_TYPE", String.valueOf(network));
//
//                NetworkInfo activeNetworkInfo = connManager.getActiveNetworkInfo();
//                if (activeNetworkInfo != null)
//                    fullInfo.setProperty("TELEPHONY_NETWORK_IS_ROAMING", String.valueOf(activeNetworkInfo.isRoaming()));
//            }

            telManager.getDataState(); // check during test if DATA_SUSPENDED then error during test - in 2g networks data are suspended when call arrives


            telManager.getNetworkCountryIso();
            telManager.getNetworkOperator();
            telManager.getNetworkOperatorName();
            telManager.getSimOperatorName();
            telManager.getSimOperator();
            telManager.getSimState();
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                SignalStrength signalStrength = telManager.getSignalStrength();
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (applicationContext.checkSelfPermission(READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    Activity#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for Activity#requestPermissions for more details.
                    telManager.getDataNetworkType(); //todo: uncomment
                    return;
                }
            }

            if (connManager == null || telManager == null || wifiManager == null) {
                FirebaseCrashlytics.getInstance().recordException(new Exception("One of ConnectionManagers is null: Connectivity: " + (connManager == null) + "    Telephony: " + (telManager == null) + " Wifi: " + (wifiManager == null)));
            }

        } else {
            FirebaseCrashlytics.getInstance().recordException(new Exception("InformationCollector bad start test initialization -> null context"));
        }


    }


    private void getClientInfo() {


        if (connManager != null) {
            final NetworkInfo activeNetworkInfo = connManager.getActiveNetworkInfo();
            if (activeNetworkInfo != null)
                testResultProperties.telephonyNetworkIsRoaming = activeNetworkInfo.isRoaming();
        }
    }

    @Deprecated
    public static PackageInfo getPackageInfo(Context ctx) {
        PackageInfo pInfo = null;
        try {
            pInfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
        } catch (final NameNotFoundException e) {
            // e1.printStackTrace();
            Timber.e(e, "version of the application cannot be found");
        }
        return pInfo;
    }

    @Deprecated // use Data object instead to fill this info
    public static BasicInfo getBasicInfo(Context ctx) {
        BasicInfo basicInfo = new BasicInfo(PLATTFORM_NAME, Build.VERSION.RELEASE + "(" + Build.VERSION.INCREMENTAL + ")",
                Build.VERSION.SDK_INT, Build.DEVICE, Build.MODEL,
                Build.PRODUCT, LocaleConfig.getLocaleForServerRequest(ctx),
                TimeZone.getDefault().getID(), RevisionHelper.getVerboseRevision());

        PackageInfo pInfo = getPackageInfo(ctx);
        if (pInfo != null) {
            basicInfo.setCodeVersion(pInfo.versionCode);
            basicInfo.setSoftwareVersionName(pInfo.versionName);
        }

        if (BASIC_INFORMATION_INCLUDE_LOCATION) {
//            Location loc = GeoLocation.getLastKnownLocation(ctx);
            Location loc = null;
//            if (Build.MANUFACTURER.contentEquals("Amazon")) {
//                loc = GPSConfig.getLastKnownLocation(ctx, null);
//            } else {
            loc = GeoLocationX.getInstance(ctx.getApplicationContext()).getLastKnownLocation(ctx, null);
//            }

            if (loc != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    new at.specure.android.api.jsons.Location(loc.getTime(), loc.getElapsedRealtimeNanos(), loc.getLatitude(), loc.getLongitude(), (double) loc.getAccuracy(), loc.getAltitude(), (double) loc.getBearing(), (double) loc.getSpeed(), loc.getProvider());
                } else {
                    new at.specure.android.api.jsons.Location(loc.getTime(), -1L, loc.getLatitude(), loc.getLongitude(), (double) loc.getAccuracy(), loc.getAltitude(), (double) loc.getBearing(), (double) loc.getSpeed(), loc.getProvider());
                }
            }
        }

        return basicInfo;

    }

    @Deprecated
    public static JsonObject fillBasicInfo(JsonObject object, Context ctx) throws JsonParseException {
        Gson gson = new Gson();
        object.add("plattform", gson.toJsonTree(PLATTFORM_NAME));
        object.add("os_version", gson.toJsonTree(Build.VERSION.RELEASE + "(" + Build.VERSION.INCREMENTAL
                + ")"));
        object.add("api_level", gson.toJsonTree(String.valueOf(Build.VERSION.SDK_INT)));
        object.add("device", gson.toJsonTree(Build.DEVICE));
        object.add("model", gson.toJsonTree(Build.MODEL));
        object.add("product", gson.toJsonTree(Build.PRODUCT));
        object.add("language", gson.toJsonTree(LocaleConfig.getLocaleForServerRequest(ctx)));
        object.add("timezone", gson.toJsonTree(TimeZone.getDefault().getID()));
        object.add("softwareRevision", gson.toJsonTree(RevisionHelper.getVerboseRevision()));

        PackageInfo pInfo = getPackageInfo(ctx);
        if (pInfo != null) {
            object.add("softwareVersionCode", gson.toJsonTree(String.valueOf(pInfo.versionCode)));
            object.add("softwareVersionName", gson.toJsonTree(pInfo.versionName));
        }
        object.add("type", gson.toJsonTree(Config.RMBT_CLIENT_TYPE));

        if (BASIC_INFORMATION_INCLUDE_LOCATION) {
//            Location loc = GeoLocation.getLastKnownLocation(ctx);
            Location loc = null;
//            if (Build.MANUFACTURER.contentEquals("Amazon")) {
//                loc = GPSConfig.getLastKnownLocation(ctx, null);
//            } else {
            loc = GeoLocationX.getInstance(ctx.getApplicationContext()).getLastKnownLocation(ctx, null);
//            }
            if (loc != null) {
                JsonObject locationJson = new JsonObject();
                locationJson.add("lat", gson.toJsonTree(loc.getLatitude()));
                locationJson.add("long", gson.toJsonTree(loc.getLongitude()));
                locationJson.add("provider", gson.toJsonTree(loc.getProvider()));
                if (loc.hasSpeed())
                    locationJson.add("speed", gson.toJsonTree(loc.getSpeed()));
                if (loc.hasAltitude())
                    locationJson.add("altitude", gson.toJsonTree(loc.getAltitude()));
                locationJson.add("age", gson.toJsonTree(System.currentTimeMillis() - loc.getTime())); //getElapsedRealtimeNanos() would be better, but require higher API-level
                if (loc.hasAccuracy())
                    locationJson.add("accuracy", gson.toJsonTree(loc.getAccuracy()));
                if (loc.hasSpeed())
                    locationJson.add("speed", gson.toJsonTree(loc.getSpeed()));
                /*
                 *  would require API level 18
		        if (loc.isFromMockProvider())
		        	locationJson.put("mock",loc.isFromMockProvider());
		        */
                object.add("location", locationJson);
            }
        }

        InformationCollector infoCollector = InformationCollector.getInstance(ctx, true, true, false);


        if (BASIC_INFORMATION_INCLUDE_LAST_SIGNAL_ITEM && (infoCollector != null)) {
            Signal signalItem = infoCollector.getLastSignalItem();
            if (signalItem != null) {
                object.add("last_signal_item", gson.toJsonTree(signalItem));
            } else {
                object.add("last_signal_item", gson.toJsonTree(NULL));
            }
        }

        return object;
    }

    @Deprecated
    public JsonObject getInitialInfo() {
        try {

            Gson gson = new Gson();
            final JsonObject result = new JsonObject();
            fillBasicInfo(result, context);

            result.add("ndt", gson.toJsonTree(ConfigHelper.isNDT(context)));

            result.add("testCounter", gson.toJsonTree(ConfigHelper.incAndGetNextTestCounter(context)));
            result.add("previousTestStatus", gson.toJsonTree(ConfigHelper.getPreviousTestStatus(context)));
            ConfigHelper.setPreviousTestStatus(context, null);
            return result;
        } catch (final JsonParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void getWiFiInfo() {
        initNetwork();
        if (wifiManager != null) {
            final WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            testResultProperties.wifiSSID = Helperfunctions.removeQuotationsInCurrentSSIDForJellyBean(wifiInfo.getSSID());
            testResultProperties.wifiBSSID = wifiInfo.getBSSID();
            testResultProperties.wifiNetworkId = String.valueOf(wifiInfo.getNetworkId());
            final SupplicantState wifiState = wifiInfo.getSupplicantState();
            testResultProperties.wifiSupplicantState = wifiState.name();
            final DetailedState wifiDetail = WifiInfo.getDetailedStateOf(wifiState);
            testResultProperties.wifiSupplicantStateDetail = wifiDetail.name();
            /*
             * fullInfo.setProperty("WIFI_LINKSPEED",
             * String.valueOf(wifiInfo.getLinkSpeed()));
             */
            /*
             * fullInfo.setProperty("WIFI_RSSI",
             * String.valueOf(wifiInfo.getRssi()));
             */
            if (getNetwork() == NETWORK_WIFI) {

                final int rssi = wifiInfo.getRssi();
                if (rssi != -1 && rssi >= ACCEPT_WIFI_RSSI_MIN) {
                    int linkSpeed = wifiInfo.getLinkSpeed();
                    if (linkSpeed < 0) {
                        linkSpeed = 0;
                    }

                    final Signal signalItem = new Signal(linkSpeed, rssi);
                    if (this.collectInformation) {
                        signals.add(signalItem);
                    }
                    lastSignalItem.set(signalItem);
                    signal.set(rssi);
                    signalType.set(SIGNAL_TYPE_WLAN);
//                    Timber.i( "Signals1: " + signals.toString());
                }
            }
        }
    }

    private void getTelephonyInfo() {
        initNetwork();
        if (telManager != null) {
            try {
                // Get Cell Location
                android.telephony.CellLocation.requestLocationUpdate();
            } catch (Exception e) {
                // some devices with Android 5.1 seem to throw a NPE is some cases
                e.printStackTrace();
            }

            boolean accessToLocationGranted = isCoarseLocationPermitted(context);


            if (accessToLocationGranted) {
                //it is checked in static method above
                if (context != null)
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(context, ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED) {
                        final android.telephony.CellLocation cellLocation = telManager.getCellLocation();
                        if (cellLocation != null && (cellLocation instanceof GsmCellLocation)) {
                            final GsmCellLocation gcl = (GsmCellLocation) cellLocation;
                            if (gcl.getCid() > 0 && this.collectInformation) {
                                cellLocations.add(new at.specure.android.api.jsons.CellLocation(new CellInfoPreV18(gcl)));
                            }
                        }
                    }

            }
            testResultProperties.telephonyNetworkOperatorName = telManager.getNetworkOperatorName();
            String networkOperator = telManager.getNetworkOperator();
            if (networkOperator != null && networkOperator.length() >= 5)
                networkOperator = String.format("%s-%s", networkOperator.substring(0, 3), networkOperator.substring(3));
            testResultProperties.telephonyNetworkOperator = networkOperator;
            testResultProperties.telephonyNetworkCountry = telManager.getNetworkCountryIso();
            testResultProperties.telephonyNetworkSimCountry = telManager.getSimCountryIso();
            String simOperator = telManager.getSimOperator();
            if (simOperator != null && simOperator.length() >= 5)
                simOperator = String.format("%s-%s", simOperator.substring(0, 3), simOperator.substring(3));
            testResultProperties.telephonyNetworkSimOperator = simOperator;

            try // hack for Motorola Defy (#594)
            {
                testResultProperties.telephonyNetworkSimOperatorName = telManager.getSimOperatorName();
            } catch (SecurityException e) {
                e.printStackTrace();
                testResultProperties.telephonyNetworkSimOperatorName = "s.exception";
            }

            testResultProperties.telephonyPhoneType = String.valueOf(telManager.getPhoneType());

            try // some devices won't allow this w/o READ_PHONE_STATE. conflicts with Android API doc
            {
                testResultProperties.telephonyDataState = String.valueOf(telManager.getDataState());
            } catch (SecurityException e) {
                e.printStackTrace();
                testResultProperties.telephonyDataState = "s.exception";
            }
//             telManager.listen(telListener,
//             PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        }
    }


    // public boolean setInfo(String key, String value) {
    // if (fullInfo.containsKey(key)) {
    // fullInfo.setProperty(key, value);
    // return true;
    // } else
    // return false;
    // }

    public TestResultProperties getInfo(final String key) {
        if (testResultProperties == null) {
            Timber.d("SIGNAL CHANGED IC TRP 682 reinit CLEARED!");
            reInit();
        }
        return testResultProperties;
    }

    public NRConnectionState getLastNRConnectionState() {
        if (lastNRConnectionState == null || lastNRConnectionState == NRConnectionState.NOT_AVAILABLE) {
            return null;
        } else {
            return lastNRConnectionState;
        }
    }

    public void setUUID(final String uuid) {
        if (uuid != null && uuid.length() != 0) {
            ConfigHelper.setUUID(context, uuid);
        }
    }

    public String getOperatorName() {
        int network = getNetwork();

        if (network == NETWORK_WIFI)
            if (testResultProperties != null) {
                return testResultProperties.wifiSSID;
            } else return "-";
        else if (network == NETWORK_ETHERNET)
            return "Ethernet";
        else if (network == NETWORK_BLUETOOTH)
            return "Bluetooth";
        else {
            if (testResultProperties != null) {
                String TelephonyNetworkOperator = testResultProperties.telephonyNetworkOperator;
                String TelephonyNetworkOperatorName = testResultProperties.telephonyNetworkOperatorName;
                if ((TelephonyNetworkOperator == null || TelephonyNetworkOperator.length() == 0) && (TelephonyNetworkOperatorName == null || TelephonyNetworkOperatorName.length() == 0))
                    return "-";
                else if (TelephonyNetworkOperator == null || TelephonyNetworkOperator.length() == 0)
                    return TelephonyNetworkOperatorName;
                else if (TelephonyNetworkOperatorName == null || TelephonyNetworkOperatorName.length() == 0)
                    return TelephonyNetworkOperator;
                else
                    return String.format("%s (%s)", TelephonyNetworkOperatorName, TelephonyNetworkOperator);
            } else {
                return "-";
            }
        }

    }

    public boolean isNetworkLTE() {
        int network = getNetwork();
        //19 is for import android.telephony.TelephonyManager.NETWORK_TYPE_LTE_CA;
        if (network == TelephonyManager.NETWORK_TYPE_LTE || network == 19) {
            return true;
        } else {
            return false;
        }
    }


    public List<at.specure.android.api.jsons.CellLocation> getCellLocations(long startTimestampNs) {
        final int network = getNetwork();
        if (cellLocations.size() > 0 && isMobileNetwork(network)) {
            if (startTimestampNs > 0) {
                for (at.specure.android.api.jsons.CellLocation cellLocation : cellLocations) {
                    cellLocation.setTimeNs(cellLocation.getTimeNs() - startTimestampNs);
                }
            }
            return cellLocations;
        }
        return null;
    }

    public List<at.specure.android.api.jsons.Location> getGeoLocations() {
        return GeoLocationX.getInstance(context).getTestResultLocations();
    }

    public List<at.specure.android.api.jsons.Signal> getSignals() {
        return signals;
    }

    public NRConnectionState getResultNRConnectionState() {
        if (signalsNRConnectionStates.contains(NRConnectionState.SA)) {
            return NRConnectionState.SA;
        } else if (signalsNRConnectionStates.contains(NRConnectionState.NSA)) {
            return NRConnectionState.NSA;
        } else if (signalsNRConnectionStates.contains(NRConnectionState.AVAILABLE)) {
            return NRConnectionState.AVAILABLE;
        } else {
            return null;
        }
    }

    public List<CellInfoGet> getResultCellInfos() {
        final int network = getNetwork();
        if (network != NETWORK_WIFI && network != NETWORK_BLUETOOTH && network != NETWORK_ETHERNET) {
            if (cellsInfos != null && cellsInfos.size() > 0) {
                return cellsInfos;
            }
        }
        return null;
    }

//    @Deprecated // use partial get methods to get all info
//    public JsonObject getResultValues(long startTimestampNs) throws JsonParseException {
//        Gson gson = new Gson();
//        final JsonObject result = new JsonObject();
//
//        final Enumeration<?> pList = fullInfo.propertyNames();
//
//        final int network = getNetwork();
//        while (pList.hasMoreElements()) {
//            final String key = (String) pList.nextElement();
//            boolean add = true;
//            if (network == NETWORK_WIFI) {
//                if (key.startsWith("TELEPHONY_")) // no mobile data if wifi
//                    add = false;
//            } else if (key.startsWith("WIFI_")) // no wifi data if mobile
//                add = false;
//            if ((network == NETWORK_ETHERNET || network == NETWORK_BLUETOOTH) &&
//                    (key.startsWith("TELEPHONY_") || key.startsWith("WIFI_"))) // add neither mobile nor wifi data
//                add = false;
//            if (add)
//                result.add(key.toLowerCase(Locale.US), gson.toJsonTree(fullInfo.getProperty(key)));
//        }
//
//        return result;
//    }

    /**
     * Lazily initializes the network managers.
     * <p>
     * As a side effect, assigns connectivityManager and telephonyManager.
     */
    private synchronized void initNetwork() {

        if (connManager == null) {
            final ConnectivityManager tryConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            final TelephonyManager tryTelephonyManager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);

            final WifiManager tryWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

            // Assign to member vars only after all the get calls succeeded,

            mobileNetworkType = NETWORK_TYPE_UNKNOWN;

            connManager = tryConnectivityManager;
            telManager = tryTelephonyManager;
            wifiManager = tryWifiManager;
            subscriptionManager = (SubscriptionManager) context.getApplicationContext().getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
            activeDataCellInfoExtractor = new ActiveDataCellInfoExtractorImpl(context, telManager, subscriptionManager, connManager);

            if (Build.VERSION.SDK_INT >= 18) {
                telManagerSupport = new TelephonyManagerV18(telManager, context);
            } else {
                telManagerSupport = new TelephonyManagerPreV18(telManager, context);
            }
            // Some interesting info to look at in the logs
            //final NetworkInfo[] infos = connManager.getAllNetworkInfo();
            //for (final NetworkInfo networkInfo : infos)
            //    Timber.i( "Network: " + networkInfo);
        }
        assert connManager != null;
        assert telManager != null;
        assert wifiManager != null;
    }

    /**
     * Returns the network that the phone is on (e.g. Wifi, Edge, GPRS, etc).
     */
    public int getNetwork() {
        int result = NETWORK_TYPE_UNKNOWN;

        if (connManager != null) {
            final NetworkInfo activeNetworkInfo = connManager.getActiveNetworkInfo();
            if (activeNetworkInfo != null) {
                final int type = activeNetworkInfo.getType();
                switch (type) {
                    case ConnectivityManager.TYPE_WIFI:
                        result = NETWORK_WIFI;
                        break;

                    case ConnectivityManager.TYPE_BLUETOOTH:
                        result = NETWORK_BLUETOOTH;
                        break;

                    case ConnectivityManager.TYPE_ETHERNET:
                        result = NETWORK_ETHERNET;
                        break;

                    case ConnectivityManager.TYPE_MOBILE:
                    case ConnectivityManager.TYPE_MOBILE_DUN:
                    case ConnectivityManager.TYPE_MOBILE_HIPRI:
                    case ConnectivityManager.TYPE_MOBILE_MMS:
                    case ConnectivityManager.TYPE_MOBILE_SUPL:
                        try {
                            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                                result = telManager.getNetworkType();
                                if (result == 0) {
                                    result = activeNetworkInfo.getSubtype();
                                }
                            }
                        } catch (Exception e) {
                            Timber.e(e);
                        }
                        break;
                }
            }
        }

        /* detect change from wifi to mobile or reverse */
        final int lastNetworkType = this.lastNetworkType.get();
        if (result != NETWORK_TYPE_UNKNOWN && lastNetworkType != NETWORK_TYPE_UNKNOWN) {
            if (
                    (result == ConnectivityManager.TYPE_WIFI && lastNetworkType != ConnectivityManager.TYPE_WIFI)
                            ||
                            (result != ConnectivityManager.TYPE_WIFI && lastNetworkType == ConnectivityManager.TYPE_WIFI)
            ) {
                illegalNetworkTypeChangeDetected.set(true);
                Timber.e("ILLEGAL NETWORK CHANGE DETECTED");
            }
        }
        if (result == NETWORK_TYPE_UNKNOWN) {
            result = mobileNetworkType;
        }

        if (result != lastNetworkType) {
            this.lastNetworkType.set(result);
            if (telListener != null)
                telListener.onSignalStrengthsChanged(null);
            //initNetwork();
            registerListeners();
            registerNetworkReceiver();
        }

        return result;
    }

    public boolean getIllegalNetworkTypeChangeDetected() {
        return illegalNetworkTypeChangeDetected.get();
    }

    /*
     * private static final String[] NETWORK_TYPES = {
     *
     * "UNKNOWN", // 0 - NETWORK_TYPE_UNKNOWN OR NONE "GSM", // 1 -
     * NETWORK_TYPE_GPRS "EDGE", // 2 - NETWORK_TYPE_EDGE "UMTS", // 3 -
     * NETWORK_TYPE_UMTS "CDMA", // 4 - NETWORK_TYPE_CDMA "EVDO_0", // 5 -
     * NETWORK_TYPE_EVDO_0 "EVDO_A", // 6 - NETWORK_TYPE_EVDO_A "1xRTT", // 7 -
     * NETWORK_TYPE_1xRTT "HSDPA", // 8 - NETWORK_TYPE_HSDPA "HSUPA", // 9 -
     * NETWORK_TYPE_HSUPA "HSPA", // 10 - NETWORK_TYPE_HSPA "IDEN", // 11 -
     * NETWORK_TYPE_IDEN "EVDO_B", // 12 - NETWORK_TYPE_EVDO_B "LTE", // 13 -
     * NETWORK_TYPE_LTE "EHRPD", // 14 - NETWORK_TYPE_EHRPD "HSPA+", //15 -
     * NETWORK_TYPE_HSPAP };
     */

    /**
     * Returns mobile data network connection type.
     */
    /*
     * private int getTelephonyNetworkType() { //assert
     * NETWORK_TYPES[14].compareTo("EHRPD") == 0;
     *
     * int networkType = telManager.getNetworkType(); if (networkType <
     * NETWORK_TYPES.length) {
     *
     * } else { return 0; } }
     */

    // Listeners
    private void registerListeners() {
        initNetwork();

        boolean accessToLocationGranted = isCoarseLocationPermitted(context);

        if (telListener == null) {
            Timber.e("SIGNAL LISTENER CREATED: %s", new Date().getTime());
            try {
                try {
                    if (Looper.myLooper() == null) {
                        Looper.prepareMainLooper();
                    }
                } catch (Exception e) {
                    Timber.e(e, "INFOCOLLECTOR");
                }
                telListener = new TelephonyStateListener();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (accessToLocationGranted) {
                telManager.listen(telListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
                        | PhoneStateListener.LISTEN_CELL_LOCATION);
            } else {
                telManager.listen(telListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
            }
            SignalStrength getSignalStrength = null;
            try {
                getSignalStrength = (SignalStrength) TelephonyManager.class.getMethod("getSignalStrength").invoke(telManager);
                telListener.onSignalStrengthsChanged(getSignalStrength);
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    private void unregisterListeners() {
        Timber.d("unregistering listener");

        if (telManager != null) {
            telManager.listen(telListener, PhoneStateListener.LISTEN_NONE);
            telListener = null;
            telManager = null;
        }
    }

    private void registerNetworkReceiver() {
        if (networkReceiver == null && registerNetworkReiceiver) {
            networkReceiver = new NetworkStateBroadcastReceiver();
            IntentFilter intentFilter;
            intentFilter = new IntentFilter();
            // intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
            intentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
            Timber.d("registering receiver");
            context.registerReceiver(networkReceiver, intentFilter);
        }
    }

    private void unregisterNetworkReceiver() {
        Timber.d("unregistering receiver");
        try {
            if (networkReceiver != null)
                context.unregisterReceiver(networkReceiver);
        } catch (Exception e) {
            //do nothing
        } finally {
            networkReceiver = null;
        }
    }

    public Integer getSignal() {
        final int _signal = signal.get();
        if ((_signal == Integer.MIN_VALUE) || (_signal == SignalStrength.INVALID))
            return null;
        return _signal;
    }

    public Integer getSignalRsrq() {
        final int _signal = signalRsrq.get();
        if (_signal == Integer.MIN_VALUE)
            return null;
        return _signal;
    }

    public int getSignalType() {
        return signalType.get();
    }

    public Signal getLastSignalItem() {
        return lastSignalItem.get();
    }

    public void setTestServerName(final String serverName) {
        testServerName = serverName;
    }

    public String getTestServerName() {
        return testServerName;
    }

    /**
     * Listener + recorder for mobile or wifi updates
     */
    private class NetworkStateBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();

            if (action != null && action.equals(WifiManager.RSSI_CHANGED_ACTION)) {
                Timber.d("Wifi RSSI changed");

                if (getNetwork() == NETWORK_WIFI) {
                    if (wifiManager != null) {
                        final WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                        final int rssi = wifiInfo.getRssi();
                        if (rssi != -1 && rssi >= ACCEPT_WIFI_RSSI_MIN) {
                            final Signal signalItem = new Signal(wifiInfo.getLinkSpeed(), rssi);
                            if (InformationCollector.this.collectInformation) {
                                signals.add(signalItem);
                            }
                            lastSignalItem.set(signalItem);
                            signal.set(rssi);
                            Timber.e("ZERO SIGNAL IC RSSI: %s", rssi);
                            signalType.set(SIGNAL_TYPE_WLAN);
                        }
                    }
                }

            }
        }
    }

    public class TelephonyStateListener extends PhoneStateListener {

        public TelephonyStateListener() {

        }

        /**
         * in ASU UNITS
         *
         * @param signalStrength
         */
        @Override
        public void onSignalStrengthsChanged(final SignalStrength signalStrength) {
            //Timber.d( "SignalStrength changed");

            final int network = getNetwork();
            int strength = UNKNOWN;
            int lteRsrp = UNKNOWN;
            int lteRsrq = UNKNOWN;
            int lteRsssnr = UNKNOWN;
            int lteCqi = UNKNOWN;
            int errorRate = UNKNOWN;
            NRConnectionState currentNRConnectionState = NRConnectionState.NOT_AVAILABLE;
            CellInfo currentCellInfo = null;

            at.specure.android.util.network.network.NetworkInfo currentNetworkInfo = null; //activeNetworkWatcher.currentNetworkInfo;

            if (signalStrength != null) {
                Timber.e("5G SIGNAL CHANGED: %s", signalStrength.toString());
            } else {
                lastNRConnectionState = NRConnectionState.NOT_AVAILABLE;
                mobileNetworkType = NETWORK_TYPE_UNKNOWN;
                return;
            }

            // discard signal strength from GT-I9100G (Galaxy S II) - passes wrong info
            if(Build.MODEL != null) {
                if (Build.MODEL.equals("GT-I9100G") || Build.MODEL.equals("HUAWEI P2-6011")) {
                    lastNRConnectionState = NRConnectionState.NOT_AVAILABLE;
                    mobileNetworkType = NETWORK_TYPE_UNKNOWN;
                    return;
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if(network !=NETWORK_WIFI &&network !=NETWORK_BLUETOOTH &&network !=NETWORK_ETHERNET) {
                    // new way of processing signalStrengthInfo changes
//                    if ((checkSelfPermission(context, READ_PHONE_STATE) == PERMISSION_GRANTED) && checkSelfPermission(context, ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED) {
                        try {
                            Timber.d("5G, checking ADCI 5G");
                            ActiveDataCellInfo activeDataCellInfo = activeDataCellInfoExtractor.extractActiveCellInfo(telManager.getAllCellInfo(), signalStrength);

                            currentCellInfo = activeDataCellInfo.getActiveDataNetworkCellInfo();
                            if (currentCellInfo != null) {
                                Timber.d("5G, currentCellInfo: " + currentCellInfo.toString());
                            } else {
                                Timber.d("5G, currentCellInfo: NULL");
                            }

                            currentNetworkInfo = activeDataCellInfo.getActiveDataNetwork();
                            if (currentNetworkInfo != null) {
                                Timber.d("5G, currentNetworkInfo: " + currentNetworkInfo.getType() + "  " + currentNetworkInfo.getName());
                            } else {
                                Timber.d("5G, currentNetworkInfo: NULL");
                            }
                            currentNRConnectionState = activeDataCellInfo.getNrConnectionState();
                            if (currentNRConnectionState != null) {
                                Timber.d("5G, currentNRConnectionState: " + currentNRConnectionState.name());
                            } else {
                                Timber.d("5G, currentNRConnectionState: NULL");
                            }

                            CellInfoGet currentCellInfoItem = RealTimeInformation.parseFromCellInfo(currentCellInfo);

                            if (cellsInfos == null) {
                                cellsInfos = new ArrayList<>();
                            }

                            if (currentCellInfoItem != null) {
                                if ((!cellsInfos.isEmpty())) {
                                    CellInfoGet cellInfoItem = cellsInfos.get(cellsInfos.size() - 1);
                                    if (cellInfoItem.arfcnNumber != null && currentCellInfoItem.arfcnNumber != null && cellInfoItem.arfcnNumber.intValue() != currentCellInfoItem.arfcnNumber.intValue()) {
                                        cellsInfos.add(currentCellInfoItem);
                                    }
                                } else {
                                    cellsInfos.add(currentCellInfoItem);
                                }
                            }
                        } catch (SecurityException e) {
                            Timber.e("5G SecurityException: Not able to read telephonyManager.allCellInfo");
                        } catch (IllegalStateException e) {
                            Timber.e("5G IllegalStateException: Not able to read telephonyManager.allCellInfo");
                        } catch (Exception e) {
                            Timber.e("5G Another exception regarding detecting network type");
                        }
//                    } else {
//                        Timber.e("5G Not enough permissions to detect 5G");
//                    }

                    boolean dualSim = false;
                    if ((checkSelfPermission(context, READ_PHONE_STATE) == PERMISSION_GRANTED)) {
                        dualSim = subscriptionManager.getActiveSubscriptionInfoCount() > 1;
                    } else {
                        if (telManager == null) {
                            telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                            if (telManager != null) {
                                dualSim = telManager.getPhoneCount() > 1;
                            }
                        }
                    }

                    Timber.d("5G Signal changed detected: value: " + signalStrength.getLevel() + "\nclass: " + signalStrength.getClass() + "\n " + signalStrength.toString());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        if (signalStrength != null) {
                            if (signalStrength.getCellSignalStrengths() != null) {
                                for (CellSignalStrength cellSignalStrength : signalStrength.getCellSignalStrengths()) {
                                    Timber.d("5G Cell signalStrengthInfo changed detected: \ndbm: " + cellSignalStrength.getDbm() + "\nLevel: " + cellSignalStrength.getLevel() + "\nasuLevel: " + cellSignalStrength.getAsuLevel() + " \nclass: " + cellSignalStrength.getClass());
                                }
                            }
                        }
                    }

                    try {
                        mobileNetworkType = ((CellNetworkInfo) currentNetworkInfo).getNetworkType().getIntValue();
                        Timber.d("5G NETWORK TYPE ID: " + mobileNetworkType);
                    } catch (Exception e) {
                        Timber.e("5G NETWORK TYPE ID: " + e.getLocalizedMessage());
                        try {
                            mobileNetworkType = connManager.getActiveNetworkInfo().getSubtype();
                        } catch (Exception ex) {
                            Timber.e("5G NETWORK TYPE ID 2: " + e.getLocalizedMessage());
                        }
                    }


//                NetworkInfo activeNetworkInfo = connManager.getActiveNetworkInfo();
//                if (activeNetworkInfo != null) {
//                    TransportType networkInfoTransportType = getNetworkInfoTransportType(activeNetworkInfo);
//                    if (networkInfoTransportType != null) {
//                        switch (networkInfoTransportType) {
//                            case CELLULAR:
//                                CellNetworkInfo.Companion.from()
//
//
//                                ;
//                            case WIFI:
//
//
//                        }
//                        currentNetworkInfo = new ConnectivityInfo(Integer.parseInt(activeNetworkInfo.toString()), networkInfoTransportType, new ArrayList<>(), 0,0)
//                    }
//                }

                    SignalStrengthInfo signalStrengthInfo = SignalStrengthInfo.Companion.from(signalStrength, currentNetworkInfo, currentCellInfo, currentNRConnectionState, dualSim);

                    if (currentNRConnectionState != lastNRConnectionState) {
                        lastNRConnectionState = currentNRConnectionState;
                    }
                    if (lastNRConnectionState != null) {
                        Timber.d("5G Signal NR state recorded 2: " + lastNRConnectionState);
                        signalsNRConnectionStates.add(lastNRConnectionState);
                    }

                    if (signalStrengthInfo == null || signalStrengthInfo.getValue() == null || signalStrengthInfo.getValue() == 0) {
                        // this is case when signal strength info is NULL - in case of 5G NSA it is when there is no info about 5G available
                        lastSignalStrengthInfo = null;
                        lastSignalItem.set(null);
                        if (lastNRConnectionState == NRConnectionState.NOT_AVAILABLE) {
                            lastNRConnectionState = null;
                        }
                        Timber.d("5G Signal changed to: NULL");
                    } else {
                        lastSignalStrengthInfo = signalStrengthInfo;
                        Signal signalItem = null;

                        if (mobileNetworkType == NETWORK_TYPE_UNKNOWN) {
                            if (lastSignalStrengthInfo instanceof SignalStrengthInfoNr) {
                                mobileNetworkType = NETWORK_TYPE_NR;
                            } else if (lastSignalStrengthInfo instanceof SignalStrengthInfoLte) {
                                mobileNetworkType = NETWORK_TYPE_LTE;
                            } else if (lastSignalStrengthInfo instanceof SignalStrengthInfoGsm) {
                                mobileNetworkType = NETWORK_TYPE_GSM;
                            }
                        }

                        if (lastSignalStrengthInfo instanceof SignalStrengthInfoNr) {
                            signalItem = new Signal(
                                    mobileNetworkType,
                                    UNKNOWN,
                                    UNKNOWN,
                                    UNKNOWN,
                                    UNKNOWN,
                                    lastSignalStrengthInfo.getValue(),
                                    0);
                            if (lastSignalStrengthInfo.getValue() != null) {
                                signal.set(lastSignalStrengthInfo.getValue());
                            }
                            signalRsrq.set(UNKNOWN);
                            signalType.set(SIGNAL_TYPE_MOBILE);
                        } else if (lastSignalStrengthInfo instanceof SignalStrengthInfoLte) {
//                            Timber.d("PPP object" + lastSignalStrengthInfo);
//                            Timber.d("PPP transport" + lastSignalStrengthInfo.getTransport().getValue());
//                            Timber.d("PPP rsrp" + ((SignalStrengthInfoLte) lastSignalStrengthInfo).getRsrp());
//                            Timber.d("PPP rsrq" + ((SignalStrengthInfoLte) lastSignalStrengthInfo).getRsrq());
//                            Timber.d("PPP rssnr" + ((SignalStrengthInfoLte) lastSignalStrengthInfo).getRssnr());
//                            Timber.d("PPP cqi" + ((SignalStrengthInfoLte) lastSignalStrengthInfo).getCqi());
                            Integer rssnr = UNKNOWN;
                            Integer cqi = UNKNOWN;
                            if (((SignalStrengthInfoLte) lastSignalStrengthInfo).getRssnr() != null) {
                                rssnr = ((SignalStrengthInfoLte) lastSignalStrengthInfo).getRssnr();
                            }
                            if (((SignalStrengthInfoLte) lastSignalStrengthInfo).getCqi() != null) {
                                cqi = ((SignalStrengthInfoLte) lastSignalStrengthInfo).getCqi();
                            }
                            signalItem = new Signal(
                                    mobileNetworkType,
                                    ((SignalStrengthInfoLte) lastSignalStrengthInfo).getRsrp(),
                                    ((SignalStrengthInfoLte) lastSignalStrengthInfo).getRsrq(),
                                    rssnr,
                                    cqi,
                                    UNKNOWN,
                                    UNKNOWN);
                            Integer rsrp = ((SignalStrengthInfoLte) lastSignalStrengthInfo).getRsrp();
                            if (rsrp != null) {
                                signal.set(rsrp);
                            }
                            Integer rsrq = ((SignalStrengthInfoLte) lastSignalStrengthInfo).getRsrq();
                            if (rsrq != null) {
                                signalRsrq.set(rsrq);
                            }
                            signalType.set(SIGNAL_TYPE_RSRP);
                        } else if (lastSignalStrengthInfo instanceof SignalStrengthInfoGsm) {
                            signalItem = new Signal(
                                    mobileNetworkType,
                                    UNKNOWN,
                                    UNKNOWN,
                                    UNKNOWN,
                                    UNKNOWN,
                                    lastSignalStrengthInfo.getValue(),
                                    ((SignalStrengthInfoGsm) lastSignalStrengthInfo).getBitErrorRate());
                            if (lastSignalStrengthInfo.getValue() != null) {
                                signal.set(lastSignalStrengthInfo.getValue());
                            }
                            signalRsrq.set(UNKNOWN);
                            signalType.set(SIGNAL_TYPE_MOBILE);
                        } else if (lastSignalStrengthInfo instanceof SignalStrengthInfoCommon) {
                            signalItem = new Signal(
                                    mobileNetworkType,
                                    UNKNOWN,
                                    UNKNOWN,
                                    UNKNOWN,
                                    UNKNOWN,
                                    lastSignalStrengthInfo.getValue(),
                                    UNKNOWN);
                            if (lastSignalStrengthInfo.getValue() != null) {
                                signal.set(lastSignalStrengthInfo.getValue());
                            }
                            signalRsrq.set(UNKNOWN);
                            signalType.set(SIGNAL_TYPE_MOBILE);
                        }

                        Timber.e("ZERO SIGNAL ITEM: %s", signalItem);
                        if (InformationCollector.this.collectInformation) {
                            Timber.d("SIGNAL CHANGED SAVED");
                            signals.add(signalItem);
                            Timber.d("5G Signal NR state recorded: " + lastNRConnectionState);
                            signalsNRConnectionStates.add(lastNRConnectionState);
                            // testing only
//                            signalsNRConnectionStates.add(NRConnectionState.SA);
//                            signalsNRConnectionStates.add(NRConnectionState.NSA);
//                            signalsNRConnectionStates.add(NRConnectionState.AVAILABLE);
//                            signalsNRConnectionStates.add(NRConnectionState.NOT_AVAILABLE);
                        }
                        lastSignalItem.set(signalItem);
                        Timber.d("5G Signal changed to: \ntransport: " + signalStrengthInfo.getTransport() + "\nvalue: " + signalStrengthInfo.getValue() + " \nsignalLevel: " + signalStrengthInfo.getSignalLevel());
                    }


                }
            } else {
                if(network !=NETWORK_WIFI &&network !=NETWORK_BLUETOOTH &&network !=NETWORK_ETHERNET) {
                    if (signalStrength != null) {

                        CellInfoGet arfcnCellInfo = RealTimeInformation.getARFCNCellInfo(context);

                        if ((arfcnCellInfo != null) && (arfcnCellInfo.arfcnNumber != null)) {
                            if (cellsInfos == null) {
                                cellsInfos = new ArrayList<>();
                            }

                            if ((!cellsInfos.isEmpty())) {
                                CellInfoGet cellInfoItem = cellsInfos.get(cellsInfos.size() - 1);
                                if (cellInfoItem.arfcnNumber.intValue() != arfcnCellInfo.arfcnNumber.intValue()) {
                                    cellsInfos.add(arfcnCellInfo);
                                }
                            } else {
                                cellsInfos.add(arfcnCellInfo);
                            }
                        }

                        if (network == TelephonyManager.NETWORK_TYPE_CDMA) {
                            strength = signalStrength.getCdmaDbm();
                            Timber.e("SIGNAL CHANGED: %s TelephonyManager.NETWORK_TYPE_CDMA", strength);
                        } else if (network == TelephonyManager.NETWORK_TYPE_EVDO_0
                                || network == TelephonyManager.NETWORK_TYPE_EVDO_A
                            /* || network == TelephonyManager.NETWORK_TYPE_EVDO_B */) {
                            strength = signalStrength.getEvdoDbm();
                            Timber.e("SIGNAL CHANGED: %s TelephonyManager.NETWORK_TYPE_EVDO", strength);
                        } else if (network == NETWORK_TYPE_LTE || network == NETWORK_TYPE_LTE_CA)/* TelephonyManager.NETWORK_TYPE_LTE ; not avail in api 8 */ {
                            try {
                                lteRsrp = (Integer) SignalStrength.class.getMethod("getLteRsrp").invoke(signalStrength);
                                lteRsrq = (Integer) SignalStrength.class.getMethod("getLteRsrq").invoke(signalStrength);
                                lteRsssnr = (Integer) SignalStrength.class.getMethod("getLteRssnr").invoke(signalStrength);
                                lteCqi = (Integer) SignalStrength.class.getMethod("getLteCqi").invoke(signalStrength);

                                if (lteRsrp == Integer.MAX_VALUE)
                                    lteRsrp = UNKNOWN;
                                if (lteRsrq == Integer.MAX_VALUE)
                                    lteRsrq = UNKNOWN;
                                if (lteRsrq > 0)
                                    lteRsrq = -lteRsrq; // fix invalid rsrq values for some devices (see #996)
                                if (lteRsssnr == Integer.MAX_VALUE)
                                    lteRsssnr = UNKNOWN;
                                if (lteCqi == Integer.MAX_VALUE)
                                    lteCqi = UNKNOWN;
                            } catch (Throwable t) {
                                t.printStackTrace();
                            }
                        } else if (signalStrength.isGsm()) {
                            try {
                                final Method getGsmDbm = SignalStrength.class.getMethod("getGsmDbm");
                                final Integer result = (Integer) getGsmDbm.invoke(signalStrength);
                                if (result != -1)
                                    strength = result;
                                Timber.e("SIGNAL CHANGED: %s TelephonyManager.GSM", strength);
                            } catch (Throwable t) {
                            }
                            if (strength == UNKNOWN) {   // fallback if not implemented
                                int dBm;
                                int gsmSignalStrength = signalStrength.getGsmSignalStrength();
                                int asu = (gsmSignalStrength == 99 ? -1 : gsmSignalStrength);
                                if (asu != -1)
                                    dBm = -113 + (2 * asu);
                                else
                                    dBm = UNKNOWN;
                                strength = dBm;
                                Timber.e("SIGNAL CHANGED: %s UNKNOWN", strength);
                            }
                            errorRate = signalStrength.getGsmBitErrorRate();
                        }
                        if (lteRsrp != UNKNOWN) {
                            signal.set(lteRsrp);
                            Timber.e("ZERO SIGNAL IC RSRP: %s", lteRsrp);
                            signalType.set(SIGNAL_TYPE_RSRP);
                        } else {
                            signal.set(strength);
                            Timber.e("ZERO SIGNAL IC 2G/3G: %s", strength);
                            signalType.set(SIGNAL_TYPE_MOBILE);
                        }

                        signalRsrq.set(lteRsrq);
                    }

                    final Signal signalItem = new Signal(network, lteRsrp, lteRsrq, lteRsssnr, lteCqi, strength, errorRate);
                    Timber.e("ZERO SIGNAL ITEM: %s", signalItem);
                    if (InformationCollector.this.collectInformation) {
                        Timber.d("SIGNAL CHANGED SAVED");
                        signals.add(signalItem);
                    }
                    lastSignalItem.set(signalItem);
                }
            }

            /*NRConnectionState nrConnectionState = NRConnectionState.NOT_AVAILABLE;
            CellInfo cellInfo = null;
            network = activeNetworkWatcher.currentNetworkInfo;
            if ((PermissionChecker.checkSelfPermission(context, READ_PHONE_STATE) == PERMISSION_GRANTED) && PermissionChecker.checkSelfPermission(
                    context,
                    ACCESS_COARSE_LOCATION
            ) == PERMISSION_GRANTED
            ) {
                try {
                    val activeDataCellInfo = activeDataCellInfoExtractor.extractActiveCellInfo(telephonyManager.allCellInfo)
                    cellInfo = activeDataCellInfo.activeDataNetworkCellInfo
                    nrConnectionState = activeDataCellInfo.nrConnectionState
                } catch (e:SecurityException){
                    Timber.e("SecurityException: Not able to read telephonyManager.allCellInfo")
                } catch(e:IllegalStateException){
                    Timber.e("IllegalStateException: Not able to read telephonyManager.allCellInfo")
                }
            }

            val dualSim = if (PermissionChecker.checkSelfPermission(context, READ_PHONE_STATE) == PERMISSION_GRANTED) {
                subscriptionManager.activeSubscriptionInfoCount > 1
            } else {
                telephonyManager.phoneCount > 1
            }

            Timber.d("Signal changed detected: value: ${signalStrength?.level}\nclass: ${signalStrength?.javaClass}\n ${signalStrength?.toString()}")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                signalStrength ?.getCellSignalStrengths() ?.forEach {
                    Timber.d("Cell signal changed detected: \ndbm: ${it.dbm}\nLevel: ${it.level}\nasuLevel: ${it.asuLevel}\nclass: ${it.javaClass}")
                }
            }

            val signal = SignalStrengthInfo.from(signalStrength, network, cellInfo, nrConnectionState, dualSim)

            if (nrConnectionState != lastNRConnectionState) {
                cellInfoWatcher.forceUpdate()
                lastNRConnectionState = nrConnectionState
            }

            if (signal ?.value == null || signal.value == 0){
                signalStrengthInfo = null
                lastNRConnectionState = null
                Timber.d("Signal changed to: NULL")
            } else{

                signal

                Timber.d("Signal changed to: \ntransport: ${signal.transport} \nvalue: ${signal.value} \nsignalLevel:${signal.signalLevel}")
            }
            notifyInfoChanged()
        }*/


        //                Timber.d( signalStrength.toString());
//            Timber.e("SIGNAL", "CHANGED");

    }

        public TransportType getNetworkInfoTransportType(NetworkInfo networkInfo) {
            switch (networkInfo.getType()) {
                case ConnectivityManager.TYPE_MOBILE: return TransportType.CELLULAR;
                case ConnectivityManager.TYPE_WIFI: return TransportType.WIFI;
                case ConnectivityManager.TYPE_BLUETOOTH: return TransportType.BLUETOOTH;
                case ConnectivityManager.TYPE_ETHERNET: return TransportType.ETHERNET;
                case ConnectivityManager.TYPE_VPN: return TransportType.VPN;
                default: return null;
            }
        }

    @Override
    public void onCellLocationChanged(android.telephony.CellLocation location) {
        try {
            final List<CellInfoSupport> cellInfoList = getTelManagerSupport().getAllCellInfo();
            if (cellInfoList != null && cellInfoList.size() > 0) {
                final CellInfoSupport cellInfo = cellInfoList.get(0);
                if (isCollectInformation()) {
                    getCellLocations().add(new CellLocation(cellInfo));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}


    public boolean isCollectInformation() {
        return collectInformation;
    }

    public boolean isRegisterNetworkReiceiver() {
        return registerNetworkReiceiver;
    }

    public TelephonyManager getTelManager() {
        return telManager;
    }

    public TelephonyManagerSupport getTelManagerSupport() {
        return telManagerSupport;
    }

    public List<at.specure.android.api.jsons.CellLocation> getCellLocations() {
        return cellLocations;
    }

    public List<CellInfoGet> getCellInfos() {
        return cellsInfos;
    }

    public static boolean isMobileNetwork(final int network) {
        return network != NETWORK_BLUETOOTH && network != NETWORK_ETHERNET && network != NETWORK_WIFI;
    }

}
