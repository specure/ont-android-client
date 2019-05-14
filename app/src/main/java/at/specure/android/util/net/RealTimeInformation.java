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
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrength;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;

import java.util.List;

import at.specure.android.configs.PermissionHandler;
import at.specure.android.util.InformationCollector;
import at.specure.util.BandCalculationUtil;

/**
 * Created by michal.cadrik on 10/9/2017.
 */

@SuppressWarnings("UnnecessaryLocalVariable")
@Deprecated
public class RealTimeInformation {

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


    public static final int NETWORK_GENERAITON_UNKNOWN = 0;
    public static final int NETWORK_GENERAITON_2G = 2;
    public static final int NETWORK_GENERAITON_3G = 3;
    public static final int NETWORK_GENERAITON_4G = 4;

    /**
     * @param networkType if null then current network will be used
     * @param context
     * @return 0 if unknown, number mobile of network generation (2 - 2G, 3 - 3G, ...)
     */
    public static int getNetworkGeneration(Integer networkType, Context context) {

        if (networkType == null) {
            TelephonyManager mTelephonyManager = (TelephonyManager)
                    context.getSystemService(Context.TELEPHONY_SERVICE);
            networkType = mTelephonyManager.getNetworkType();
        }
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return NETWORK_GENERAITON_2G;
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return NETWORK_GENERAITON_3G;
            case TelephonyManager.NETWORK_TYPE_LTE:
            case 19: //19 is for import android.telephony.TelephonyManager.NETWORK_TYPE_LTE_CA;
                return NETWORK_GENERAITON_4G;
            default:
                return NETWORK_GENERAITON_UNKNOWN;
        }
    }

    /**
     * Needs to be tested
     *
     * @param context
     * @return
     */
    public static CellSignalStrength getCurrentSignalStrengthObject(Context context) {
        CellSignalStrength result = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            // for example value of first element

            boolean coarseLocationPermitted = PermissionHandler.isCoarseLocationPermitted(context);
            if (coarseLocationPermitted) {
                // permission checked in static method above
                @SuppressLint("MissingPermission") List<CellInfo> allCellInfo = getTelephonyManager(context).getAllCellInfo();
                if (allCellInfo != null && allCellInfo.size() > 0) {
                    CellInfo cellInfo = allCellInfo.get(0);
                    if (cellInfo != null) {
                        if (cellInfo instanceof CellInfoGsm) {
                            CellInfoGsm cellinfogsm = (CellInfoGsm) cellInfo;
                            CellSignalStrengthGsm cellSignalStrength = cellinfogsm.getCellSignalStrength();
                            result = cellSignalStrength;

                        } else if (cellInfo instanceof CellInfoLte) {
                            CellInfoLte cellinfolte = (CellInfoLte) cellInfo;
                            CellSignalStrengthLte cellSignalStrength = cellinfolte.getCellSignalStrength();
                            result = cellSignalStrength;
                        } else if (cellInfo instanceof CellInfoCdma) {
                            CellInfoCdma cellinfoCdma = (CellInfoCdma) cellInfo;
                            CellSignalStrengthCdma cellSignalStrength = cellinfoCdma.getCellSignalStrength();
                            result = cellSignalStrength;
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                            if (cellInfo instanceof CellInfoWcdma) {
                                CellInfoWcdma cellinfowcdma = (CellInfoWcdma) cellInfo;
                                CellSignalStrengthWcdma cellSignalStrength = cellinfowcdma.getCellSignalStrength();
                                result = cellSignalStrength;
                            }
                        }
                        return result;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Needs to be tested
     *
     * @param context
     * @return
     */
    public static Integer getCurrentSignalStrength(Context context) {
        Integer result = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            // for example value of first element
            boolean coarseLocationPermitted = PermissionHandler.isCoarseLocationPermitted(context);
            if (coarseLocationPermitted) {
                // permission checked in static method above
                @SuppressLint("MissingPermission") List<CellInfo> allCellInfo = getTelephonyManager(context).getAllCellInfo();
                if (allCellInfo != null && allCellInfo.size() > 0) {
                    CellInfo cellInfo = allCellInfo.get(0);
                    if (cellInfo != null) {
                        if (cellInfo instanceof CellInfoGsm) {
                            CellInfoGsm cellinfogsm = (CellInfoGsm) cellInfo;
                            CellSignalStrengthGsm cellSignalStrength = cellinfogsm.getCellSignalStrength();
                            result = cellSignalStrength.getDbm();
                        } else if (cellInfo instanceof CellInfoLte) {
                            CellInfoLte cellinfolte = (CellInfoLte) cellInfo;
                            CellSignalStrengthLte cellSignalStrength = cellinfolte.getCellSignalStrength();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                result = cellSignalStrength.getRsrp();
                            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                result = cellSignalStrength.getDbm();
                            }
                        } else if (cellInfo instanceof CellInfoCdma) {
                            CellInfoCdma cellinfoCdma = (CellInfoCdma) cellInfo;
                            CellSignalStrengthCdma cellSignalStrength = cellinfoCdma.getCellSignalStrength();
                            result = cellSignalStrength.getCdmaDbm();
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                            if (cellInfo instanceof CellInfoWcdma) {
                                CellInfoWcdma cellinfowcdma = (CellInfoWcdma) cellInfo;
                                CellSignalStrengthWcdma cellSignalStrength = cellinfowcdma.getCellSignalStrength();
                                result = cellSignalStrength.getDbm();
                            }
                        }
                        return result;
                    }
                }
            }
        }
        return result;
    }

    private static ConnectivityManager getConnectivityManager(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager;
    }

    private static TelephonyManager getTelephonyManager(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager;
    }

    @SuppressLint("MissingPermission")
    private static List<CellInfo> getCellInfo(Context context) {
        TelephonyManager telephonyManager = getTelephonyManager(context);
        List<CellInfo> allCellInfo = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            boolean coarseLocationPermitted = PermissionHandler.isCoarseLocationPermitted(context);
            if (coarseLocationPermitted) {
                // permission checked in static method above
                allCellInfo = telephonyManager.getAllCellInfo();
            }
        }
        return allCellInfo;
    }

    @SuppressLint("MissingPermission")
    public static BandCalculationUtil.FrequencyInformation getCellBandAndFrequency(Context context) {
        List<CellInfo> cellInfo = getCellInfo(context);
        try {
            if ((cellInfo != null) && (!cellInfo.isEmpty())) {
                CellInfo cellInfo1 = cellInfo.get(0);
                String s = cellInfo1.toString();
                String[] parsedString = s.split(" ");
                for (String s1 : parsedString) {
                    if (s1.toLowerCase().contains("arfcn")) {
                        String[] split = s1.split("=");
                        if (split.length > 1) {
                            String arfcnType = split[0];
                            String arfcnValue = split[1].replace("{", "").replace("}", "");

                            if ((arfcnType != null) && (arfcnValue != null) && !arfcnValue.isEmpty()) {
                                if (arfcnType.toLowerCase().contains("uarfcn")) {
                                    return BandCalculationUtil.getBandFromUarfcn(Integer.parseInt(arfcnValue));
                                } else if (arfcnType.toLowerCase().contains("earfcn")) {
                                    return BandCalculationUtil.getBandFromEarfcn(Integer.parseInt(arfcnValue));
                                } else if (arfcnType.toLowerCase().contains("arfcn")) {
                                    return BandCalculationUtil.getBandFromArfcn(Integer.parseInt(arfcnValue));
                                }
                            }

                        }
                    }
                }

            }
        } catch (NumberFormatException e) {
            return null;
        }
        return null;
    }

    public static InformationCollector.CellInfoItem getARFCNCellInfo(Context context) {
        List<CellInfo> cellInfo = getCellInfo(context);
        try {
            if ((cellInfo != null) && (!cellInfo.isEmpty())) {
                CellInfo cellInfo1 = cellInfo.get(0);
                String s = cellInfo1.toString();
                String[] parsedString = s.split(" ");
                for (String s1 : parsedString) {
                    if (s1.toLowerCase().contains("arfcn")) {
                        String[] split = s1.split("=");
                        if (split.length > 1) {
                            String arfcnType = split[0];
                            String arfcnValue = split[1].replace("{", "").replace("}", "");

                            if ((arfcnType != null) && (arfcnValue != null) && !arfcnValue.isEmpty()) {
                                if (arfcnType.toLowerCase().contains("uarfcn")) {
                                    return new InformationCollector.CellInfoItem(System.currentTimeMillis(), "UARFCN", Integer.parseInt(arfcnValue));
                                } else if (arfcnType.toLowerCase().contains("earfcn")) {
                                    return new InformationCollector.CellInfoItem(System.currentTimeMillis(), "EARFCN", Integer.parseInt(arfcnValue));
                                } else if (arfcnType.toLowerCase().contains("arfcn")) {
                                    return new InformationCollector.CellInfoItem(System.currentTimeMillis(), "ARFCN", Integer.parseInt(arfcnValue));
                                }
                            }

                        }
                    }
                }

            }
        } catch (NumberFormatException e) {
            return null;
        }
        return null;
    }



    public static String getCellId(Context context) {
        String result = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            List<CellInfo> cellInfos = getCellInfo(context);
            if ((cellInfos != null) && (!cellInfos.isEmpty())) {
                CellInfo cellInfo = cellInfos.get(0);
                if (cellInfo != null) {
                    if (cellInfo instanceof CellInfoGsm) {
                        CellInfoGsm cellinfogsm = null;
                        cellinfogsm = (CellInfoGsm) cellInfo;
                        CellSignalStrengthGsm cellSignalStrength = cellinfogsm.getCellSignalStrength();
                        result = String.valueOf(cellinfogsm.getCellIdentity().getCid());

                    } else if (cellInfo instanceof CellInfoLte) {
                        CellInfoLte cellinfolte = (CellInfoLte) cellInfo;
                        CellSignalStrengthLte cellSignalStrength = cellinfolte.getCellSignalStrength();
                        result = String.valueOf(cellinfolte.getCellIdentity().getCi() + "\n" + cellinfolte.getCellIdentity().getPci());
                    } else if (cellInfo instanceof CellInfoCdma) {
                        CellInfoCdma cellinfoCdma = (CellInfoCdma) cellInfo;
                        CellSignalStrengthCdma cellSignalStrength = cellinfoCdma.getCellSignalStrength();
                        result = String.valueOf(cellinfoCdma.getCellIdentity().getBasestationId());
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        if (cellInfo instanceof CellInfoWcdma) {
                            CellInfoWcdma cellinfowcdma = (CellInfoWcdma) cellInfo;
                            CellSignalStrengthWcdma cellSignalStrength = cellinfowcdma.getCellSignalStrength();
                            result = String.valueOf(cellinfowcdma.getCellIdentity().getCid());
                        }
                    }
                    return result;
                } else return result;

            }
        } else {
            return result;
        }
        return result;
    }

    /**
     * Returns the network that the phone is on (e.g. Wifi, Edge, GPRS, etc).
     */
    public static int getNetwork(Context context) {
        int result = TelephonyManager.NETWORK_TYPE_UNKNOWN;

        ConnectivityManager connManager = getConnectivityManager(context);
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
                        TelephonyManager telephonyManager = getTelephonyManager(context);
                        result = telephonyManager.getNetworkType();
                        break;
                }
            }
        }
        return result;
    }

//    public static Location getLocationInfo(Context context) {
//
//        if (locationManager == null) {
//            // init location Manager
//            locationManager = new InformationCollector.InfoGeoLocation(context);
//            locationManager.start();
//        }
//        final Location curLocation = locationManager.getLastKnownLocation();
//
//        if (curLocation != null && this.collectInformation) {
//            geoLocations.add(new InformationCollector.GeoLocationItem(curLocation.getTime(), curLocation.getLatitude(), curLocation
//                    .getLongitude(), curLocation.getAccuracy(), curLocation.getAltitude(), curLocation.getBearing(),
//                    curLocation.getSpeed(), curLocation.getProvider()));
//            Timber.i(DEBUG_TAG, "Location: " + curLocation.toString());
//        }
//
//        return curLocation;
//
//    }

}
