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

package at.specure.android.util.net;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.telephony.CellInfo;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.specure.opennettest.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import at.specure.android.configs.Config;
import at.specure.android.configs.ConfigHelper;
import at.specure.android.configs.RuntimeProperties;
import at.specure.android.configs.ZeroMeasurementsConfig;
import at.specure.android.database.enums.CellLocationType;
import at.specure.android.database.enums.LocationType;
import at.specure.android.database.enums.SignalType;
import at.specure.android.database.enums.ZeroMeasurementState;
import at.specure.android.database.obj.TCellLocation;
import at.specure.android.database.obj.TLocation;
import at.specure.android.database.obj.TSignal;
import at.specure.android.database.obj.TZeroMeasurement;
import at.specure.android.util.BasicInfo;
import at.specure.android.util.Helperfunctions;
import at.specure.android.util.InformationCollector;
import at.specure.android.util.location.GeoLocationX;
import timber.log.Timber;

import static at.specure.android.configs.PermissionHandler.isCoarseLocationPermitted;

/**
 * Class detecting weak signal or no signal at all.
 * <p>
 * Created by michal.cadrik on 10/10/2017.
 */

public class ZeroMeasurementDetector {

    @SuppressLint({"NewApi", "MissingPermission"})
    public static boolean detectZeroMeasurement(@Nullable final Activity activity, final Context context, InformationCollector informationCollector) {


        Timber.e( "ZERO CHECKING START");

        boolean zeroMeasurementDetected = false;
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        List<CellInfo> allCellInfo = null;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = null;
        if (cm != null) {
            activeNetworkInfo = cm.getActiveNetworkInfo();
        }

        boolean apiLevel17andBigger = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;

        boolean accessToLocationGranted = isCoarseLocationPermitted(context);

        if (apiLevel17andBigger && accessToLocationGranted) {
            //it is checked in static method
            if (telephonyManager != null) {
                allCellInfo = telephonyManager.getAllCellInfo();
            }
        }


        boolean noConnection = false;

//        Integer signal = RealTimeInformation.getCurrentSignalStrength(context);
//        boolean isSignalLTE = RealTimeInformation.getCurrentSignalStrengthObject(context) instanceof CellSignalStrengthLte;

        if (informationCollector == null) {
            Timber.e( "UNABLE TO DETECT ZERO BECAUSE information collector is NULL");
            eventLog(context, "0", "X", System.currentTimeMillis(), "ERROR, IC NULL");

            return false;
        }

        informationCollector.reInit();

        Integer signal = informationCollector.getSignal();
        InformationCollector.SignalItem lastSignal = informationCollector.getLastSignalItem();
        boolean networkLTE = informationCollector.isNetworkLTE();
        boolean isSignalLTE = informationCollector.getSignalType() == InformationCollector.SINGAL_TYPE_RSRP && networkLTE;
        int lastNetworkType = informationCollector.getNetwork();
        Integer signalRsrq = informationCollector.getSignalRsrq();
        //Integer currentSignalStrength = RealTimeInformation.getCurrentSignalStrength(context);
        //CellSignalStrength currentSignalStrengthObject = RealTimeInformation.getCurrentSignalStrengthObject(context);

        String info = "";
        if ((activeNetworkInfo == null) || (!activeNetworkInfo.isConnected())) {
            info = info + "\nactiveNetworkInfo is null";
        } else {
            info = info + "\nactiveNetworkInfo is: " + activeNetworkInfo;
        }

        if (allCellInfo != null) {
            info = info + "\nallCellInfo is: " + allCellInfo;
        } else {
            info = info + "\nallCellInfo is: null";
        }

        if (signal != null) {
            info = info + "\nsignal is: " + signal;
        } else {
            info = info + "\nsignal is: null";
        }

        if ((activeNetworkInfo == null) || (!activeNetworkInfo.isConnected())) {//&& ((allCellInfo == null) || (allCellInfo.isEmpty())) && (apiLevel17andBigger)) {
            noConnection = true;
            info = info + "\nRESULT: true";
        } else {
            info = info + "\nRESULT: false";
            info = info + "\n DETAIL: " + activeNetworkInfo.getDetailedState();
        }

        Timber.e("ZERO SUB %s", info);

        NetworkInfoCollector networkInfoCollectorInstance = NetworkInfoCollector.getInstance();
        if (networkInfoCollectorInstance != null) {
            Timber.e("ACTUAL STATUS: %s", networkInfoCollectorInstance.getActiveNetworkInfo());
        }
        // Zero measurement identification

        boolean airplaneModeOn = RuntimeProperties.isAirplaneModeOn(context);
        boolean mobileDataEnabled = RuntimeProperties.isMobileDataEnabled(context);


        Timber.e( "ZERO? am: %s \n mobileDataEnabled: %s \n noConnection: %s \n signal: %s \n + signal type 4G: %s \n\n LAST SIGNAL: %s"// + " \n CurrentSignal: " + currentSignalStrength + " \n currentSignalStrengthObject: " + (currentSignalStrengthObject == null ? "NULL" : currentSignalStrengthObject.toString()));
        ,airplaneModeOn, mobileDataEnabled, noConnection, signal,isSignalLTE,((lastSignal == null) ? "null" : lastSignal.toJson()));
        boolean noConnectionDetected = !airplaneModeOn && mobileDataEnabled && noConnection;
        /*
         * Zero measurement by no signal at all
         */
        if (noConnectionDetected) {
            Timber.e( "ZERO DETECTED AT NO SIGNAL AT ALL");
            eventLog(context, "0", "X", System.currentTimeMillis(), "OK, NO SIGNAL AT ALL");
            zeroMeasurementDetected = true;
        }
        /*
         * Zero measurement by low signal
         */
        else if (activeNetworkInfo != null) {// && (networkInfoCollector.getActiveNetworkInfo().getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
            int networkType = activeNetworkInfo.getType();
            if (networkType == ConnectivityManager.TYPE_MOBILE) {
                int subtype = activeNetworkInfo.getSubtype();
                if (networkInfoCollectorInstance != null) {
                    Timber.e("ACTUAL STATUS: %s \n Subtype: %s", networkInfoCollectorInstance.getActiveNetworkInfo(), subtype);
                }

               /* NetworkInfo.DetailedState detailedState = NetworkInfoCollector.getInstance().getActiveNetworkInfo().getDetailedState();

                boolean stateOfSignal = (detailedState == NetworkInfo.DetailedState.DISCONNECTED
                        || detailedState == NetworkInfo.DetailedState.DISCONNECTING
                        || detailedState == NetworkInfo.DetailedState.FAILED
                        || detailedState == NetworkInfo.DetailedState.IDLE
                        || detailedState == NetworkInfo.DetailedState.SCANNING
                        || detailedState == NetworkInfo.DetailedState.VERIFYING_POOR_LINK);
                Timber.e( "DETAILED STATE OF NETWORK: " + detailedState.name());*/


                // if signal is low or mobile client is not connected we are detecting
                if (signal != null) {
                    if (isSignalLTE && (signal < ZeroMeasurementsConfig.get4gThreshold(context))) {
                        eventLog(context, signal + "", "4G", System.currentTimeMillis(), "OK, LOW 4G SIGNAL");
                        zeroMeasurementDetected = true;
                    } else {
                        if (RealTimeInformation.getNetworkGeneration(subtype, context) == RealTimeInformation.NETWORK_GENERAITON_2G
                                && signal < ZeroMeasurementsConfig.get2gThreshold(context)) {
                            zeroMeasurementDetected = true;
                            Timber.e( "ZERO DETECTED AT LOW SIGNAL");
                            eventLog(context, signal + "", "2G", System.currentTimeMillis(), "OK, LOW 2G SIGNAL");
                        } else {
                            if (RealTimeInformation.getNetworkGeneration(subtype, context) == RealTimeInformation.NETWORK_GENERAITON_3G
                                    && signal < ZeroMeasurementsConfig.get3gThreshold(context)) {
                                zeroMeasurementDetected = true;
                                Timber.e( "ZERO DETECTED AT LOW SIGNAL");
                                eventLog(context, signal + "", "3G", System.currentTimeMillis(), "OK, LOW 3G SIGNAL");
                            }
                        }
                    }
                }
            }
        }

        if (zeroMeasurementDetected) {
            Timber.e( "Zero measurement detected");
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(activity.getBaseContext(), R.string.zero_measurement_detected, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            if (informationCollector == null) {
                Timber.e( "UNABLE TO SAVE ZERO BECAUSE information collector is NULL");
                eventLog(context, signal + "", isSignalLTE ? "4G" : "3G/2G", System.currentTimeMillis(), "ERROR, DETECTED - UNABLE TO SAVE - IC NULL");
                return false;
            }

//            InformationCollector fullInfo = new InformationCollector(context, true, true);
            final String uuid = informationCollector.getUUID();

            Locale locale;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                locale = context.getResources().getConfiguration().getLocales().get(0);
            } else {
                locale = context.getResources().getConfiguration().locale;
            }
            String iso3Language = locale.getLanguage();

            BasicInfo basicInfo = InformationCollector.getBasicInfo(context);

            List<InformationCollector.CellLocationItem> cellLocations = informationCollector.getCellLocations();
            List<TCellLocation> tCellLocations = TCellLocation.convertToTs(cellLocations, CellLocationType.ZERO_MEASUREMENT_CELL_LOCATION);


            ArrayList<InformationCollector.SignalItem> signals = new ArrayList<>();
            signals.add(lastSignal);
            InformationCollector.SignalItem signalItem = null;
            if (!signals.isEmpty()) {
                signalItem = signals.get(signals.size() - 1);
            }
//            InformationCollector.SignalItem signalItem = informationCollector.getLastSignalItem();

            ArrayList<TSignal> tSignals = new ArrayList<>();
            if ((signalItem != null) && (!noConnectionDetected)) {
                TSignal tSignal = new TSignal(signalItem, InformationCollector.UNKNOWN, SignalType.ZERO_MEASUREMENT_SIGNAL);

                String lastNetworkTypeString = Helperfunctions.getNetworkTypeName(lastNetworkType);
                if (!"BLUETOOTH".equals(lastNetworkTypeString) && !"ETHERNET".equals(lastNetworkTypeString)) {
                    if (signal != null && signal > Integer.MIN_VALUE) {
                        if (isSignalLTE) {
                            tSignal.lteRSRP = signal;
                            if (signalRsrq != null) {
                                tSignal.lteRSRQ = signalRsrq;
                            }
                        } else {
                            tSignal.signalStrength = signal;
                            tSignal.lteRSRP = null;
                            tSignal.lteRSRQ = null;
                            tSignal.lteCQI = null;
                            tSignal.lteRSSNR = null;
                        }
                    }
                }

                // maybe unnecessary, because database save 0 when Integer is null
                if (signal != null && signal == 0) {
                    tSignal.signalStrength = null;
                    tSignal.lteRSRP = null;
                    tSignal.lteRSRQ = null;
                    tSignal.lteCQI = null;
                    tSignal.lteRSSNR = null;
                    tSignal.gsmBitErrorRate = null;
                }

                // If connection is lost signals are removed
                if (!(noConnectionDetected)) {
                    tSignals.add(tSignal);
                }
            }

            ArrayList<TLocation> tLocations = new ArrayList<>();
            Location loc = GeoLocationX.getInstance(context).getLastKnownLocation(context, null);

            /*if ((loc == null) || ((loc.getAltitude() == 0.0) && (loc.getLongitude() == 0.0))) {
                Timber.e( "Not saved due to lack of position");
                if (activity != null) {
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(activity.getBaseContext(), R.string.zero_measurement_not_saved, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                return true;
            } else */

            if (loc == null) {
                eventLog(context, signal + "", isSignalLTE ? "4G" : "3G/2G", System.currentTimeMillis(), "ERROR, DETECTED - UNABLE TO SAVE - GPS NULL");
            }

            if (loc != null) {
                TLocation tLocation;
                if (apiLevel17andBigger) {
                    tLocation = new TLocation(loc.getTime(), loc.getElapsedRealtimeNanos(), loc.getLatitude(), loc.getLongitude(), (double) loc.getAccuracy(), loc.getAltitude(), (double) loc.getBearing(), (double) loc.getSpeed(), loc.getProvider(), LocationType.ZERO_MEASUREMENT_LOCATION);
                } else {
                    tLocation = new TLocation(loc.getTime(), -1L, loc.getLatitude(), loc.getLongitude(), (double) loc.getAccuracy(), loc.getAltitude(), (double) loc.getBearing(), (double) loc.getSpeed(), loc.getProvider(), LocationType.ZERO_MEASUREMENT_LOCATION);

                }
                tLocation.type = LocationType.ZERO_MEASUREMENT_LOCATION;
                tLocations.add(tLocation);
            }


            TZeroMeasurement tZeroMeasurement = new TZeroMeasurement(null,
                    uuid,
                    Config.RMBT_CLIENT_NAME,
                    at.specure.client.helper.Config.RMBT_VERSION_NUMBER,
                    iso3Language,
                    System.currentTimeMillis(),
                    uuid,
                    basicInfo.platform,
                    basicInfo.product,
                    String.valueOf(basicInfo.apiLevel),
                    informationCollector.getInfo("TELEPHONY_NETWORK_OPERATOR"),//getTelManager().getNetworkOperator(), TELEPHONY_NETWORK_OPERATOR
                    String.valueOf(basicInfo.getCodeVersion()),
                    informationCollector.getInfo("TELEPHONY_NETWORK_IS_ROAMING"),
                    basicInfo.osVersion,
                    informationCollector.getTelManager().getNetworkCountryIso(),
                    String.valueOf(lastNetworkType),
                    informationCollector.getTelManager().getNetworkOperatorName(),
                    informationCollector.getTelManager().getSimOperatorName(),
                    basicInfo.model,
                    informationCollector.getInfo("TELEPHONY_NETWORK_SIM_OPERATOR"), //getTelManager().getSimOperator(),
                    basicInfo.device,
                    String.valueOf(informationCollector.getTelManager().getPhoneType()),
                    String.valueOf(informationCollector.getTelManager().getDataState()),
                    informationCollector.getTelManager().getSimCountryIso(),
                    tCellLocations,
                    tLocations,
                    tSignals,
                    ZeroMeasurementState.NOT_SENT
            );


            eventLog(context, signal + "", isSignalLTE ? "4G" : "3G/2G", System.currentTimeMillis(), "OK, SAVING ZERO");
            tZeroMeasurement.save(context);
            return true;
        }

        return false;
    }

    private static void eventLog(Context context, String signal, String networkType, long date, String error) {

        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
        if (mFirebaseAnalytics != null) {
            mFirebaseAnalytics.setUserId(ConfigHelper.getUUID(context));
            mFirebaseAnalytics.setUserProperty("UUID", ConfigHelper.getUUID(context));
            Bundle bundle = new Bundle();
            bundle.putString("Signal", signal);
            bundle.putString("Signal_type", networkType);
            bundle.putString("Date", date + "");
            bundle.putString("Date_readable", new Date(date).toString());
            bundle.putString("Error", error);
            mFirebaseAnalytics.logEvent("ZERO", bundle);
        }
    }
}
