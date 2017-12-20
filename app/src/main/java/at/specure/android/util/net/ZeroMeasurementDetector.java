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
import android.support.annotation.Nullable;
import android.telephony.CellInfo;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.specure.opennettest.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import at.specure.android.database.enums.CellLocationType;
import at.specure.android.database.enums.LocationType;
import at.specure.android.database.enums.SignalType;
import at.specure.android.database.enums.ZeroMeasurementState;
import at.specure.android.database.obj.TCellLocation;
import at.specure.android.database.obj.TLocation;
import at.specure.android.database.obj.TSignal;
import at.specure.android.database.obj.TZeroMeasurement;
import at.specure.android.util.BasicInfo;
import at.specure.android.configs.Config;
import at.specure.android.configs.ConfigHelper;
import at.specure.android.util.Helperfunctions;
import at.specure.android.util.InformationCollector;
import at.specure.android.configs.RuntimeProperties;

/**
 * Created by michal.cadrik on 10/10/2017.
 */

public class ZeroMeasurementDetector {

    @SuppressLint("NewApi")
    public static boolean detectZeroMeasurement(@Nullable final Activity activity, Context context, InformationCollector informationCollector) {

        boolean zeroMeasurementDetected = false;
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        List<CellInfo> allCellInfo = null;
        boolean apiLevel17andBigger = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
        if (apiLevel17andBigger) {
                allCellInfo = telephonyManager.getAllCellInfo();
        }
        boolean noConnection = false;
        Integer signal = informationCollector.getSignal();
        boolean isSignalRsrp = informationCollector.getSignalType() == InformationCollector.SINGAL_TYPE_RSRP;
        NetworkInfoCollector networkInfoCollector = NetworkInfoCollector.getInstance();
        if ((networkInfoCollector != null) && (networkInfoCollector.getActiveNetworkInfo() == null) && ((allCellInfo == null) || (allCellInfo.isEmpty())) && (apiLevel17andBigger)) {
            noConnection = true;
        }

        Log.e("ACTUAL STATUS: ", NetworkInfoCollector.getInstance().getActiveNetworkInfo() + "");
        // Zero measurement identification

        boolean airplaneModeOn = RuntimeProperties.isAirplaneModeOn(context);
        boolean mobileDataEnabled = RuntimeProperties.isMobileDataEnabled(context);


        /**
         * Zero measurement by no signal at all
         */
        if (!airplaneModeOn && mobileDataEnabled && noConnection) {
            zeroMeasurementDetected = true;
        }
        /**
         * Zero measurement by low signal
         */
        else if ((networkInfoCollector != null) && (networkInfoCollector.getActiveNetworkInfo() != null)) {// && (networkInfoCollector.getActiveNetworkInfo().getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
            int networkType = networkInfoCollector.getActiveNetworkInfo().getType();
            if (networkType == ConnectivityManager.TYPE_MOBILE) {
                int subtype = networkInfoCollector.getActiveNetworkInfo().getSubtype();
                Log.e("ACTUAL STATUS: ", NetworkInfoCollector.getInstance().getActiveNetworkInfo() + " \n " + subtype);

                NetworkInfo.DetailedState detailedState = NetworkInfoCollector.getInstance().getActiveNetworkInfo().getDetailedState();

                // if signal is low or mobile client is not connected we are detecting
                if (((isSignalRsrp && (signal != null) && (signal < context.getResources().getInteger(R.integer.zero_measurement_lte_threshold)))
                        || ((signal != null) && (signal < context.getResources().getInteger(R.integer.zero_measurement_gsm_threshold))))
                        || (detailedState == NetworkInfo.DetailedState.DISCONNECTED
                        || detailedState == NetworkInfo.DetailedState.DISCONNECTING
                        || detailedState == NetworkInfo.DetailedState.FAILED
                        || detailedState == NetworkInfo.DetailedState.IDLE
                        || detailedState == NetworkInfo.DetailedState.SCANNING
                        || detailedState == NetworkInfo.DetailedState.VERIFYING_POOR_LINK)) {

                    zeroMeasurementDetected = true;
                }
            }
        }

        if (zeroMeasurementDetected) {
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(activity.getBaseContext(), R.string.zero_measurement_detected, Toast.LENGTH_SHORT).show();
                    }
                });
            }

//            InformationCollector fullInfo = new InformationCollector(context, true, true);
            final String uuid = informationCollector.getUUID();

            final String controlServer = ConfigHelper.getControlServerName(context);
            final int controlPort = ConfigHelper.getControlServerPort(context);
            final boolean controlSSL = ConfigHelper.isControlSeverSSL(context);
            Locale locale = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                locale = context.getResources().getConfiguration().getLocales().get(0);
            } else {
                locale = context.getResources().getConfiguration().locale;
            }
            String iso3Language = locale.getLanguage();
            File cacheDir = context.getCacheDir();

            final ArrayList<String> geoInfo = informationCollector.getCurLocation();

            BasicInfo basicInfo = InformationCollector.getBasicInfo(context);

            List<InformationCollector.CellLocationItem> cellLocations = informationCollector.getCellLocations();
            List<TCellLocation> tCellLocations = TCellLocation.convertToTs(cellLocations, CellLocationType.ZERO_MEASUREMENT_CELL_LOCATION);

            informationCollector.reInit();

            ArrayList<InformationCollector.SignalItem> signals = new ArrayList<>(informationCollector.signals);
            InformationCollector.SignalItem signalItem = null;
            if ((signals != null) && (!signals.isEmpty())) {
                signalItem = signals.get(signals.size() - 1);
            }
//            InformationCollector.SignalItem signalItem = informationCollector.getLastSignalItem();

            ArrayList<TSignal> tSignals = new ArrayList<>();
            if (signalItem != null) {
                TSignal tSignal = new TSignal(signalItem, InformationCollector.UNKNOWN, SignalType.ZERO_MEASUREMENT_SIGNAL);

                signal = informationCollector.getSignal();

                int lastNetworkType = informationCollector.getNetwork();
                String lastNetworkTypeString = Helperfunctions.getNetworkTypeName(lastNetworkType);
                if (!"BLUETOOTH".equals(lastNetworkTypeString) && !"ETHERNET".equals(lastNetworkTypeString)) {
                    if (signal != null && signal > Integer.MIN_VALUE) {
                        int signalType = informationCollector.getSignalType();

                        if (signalType == InformationCollector.SINGAL_TYPE_RSRP) {
                            tSignal.lteRSRP = signal;
                            Integer signalRsrq = informationCollector.getSignalRsrq();
                            if (signalRsrq != null) {
                                tSignal.lteRSRQ = signalRsrq;
                            }
                        } else {
                            tSignal.signalStrength = signal;
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
                if (!(!airplaneModeOn && mobileDataEnabled && noConnection)) {
                    tSignals.add(tSignal);
                }
            }

            ArrayList<TLocation> tLocations = new ArrayList<>();
            Location loc = informationCollector.getLocationInfo();

            if ((loc == null) || ((loc.getAltitude() == 0.0) && (loc.getLongitude() == 0.0))) {
                if (activity != null) {
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(activity.getBaseContext(), R.string.zero_measurement_not_saved, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                return zeroMeasurementDetected;
            } else {
                TLocation tLocation = null;
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
                    String.valueOf(informationCollector.getTelManager().getNetworkType()),
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

            tZeroMeasurement.save(context);
            return zeroMeasurementDetected;
        }

        return zeroMeasurementDetected;
    }
}
