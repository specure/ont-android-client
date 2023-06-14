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
import android.telephony.CellIdentity;
import android.telephony.CellIdentityNr;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoNr;
import android.telephony.CellInfoTdscdma;
import android.telephony.CellInfoWcdma;
import android.telephony.CellSignalStrength;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;

import java.util.List;

import at.specure.android.api.jsons.TestResultDetails.CellInfoGet;
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
                                } else if (arfcnType.toLowerCase().contains("nrarfcn")) {
                                    return BandCalculationUtil.getBandFromNrarfcn(Integer.parseInt(arfcnValue));
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

    public static CellInfoGet getARFCNCellInfo(Context context) {
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
                                    return new CellInfoGet(System.currentTimeMillis(), "UARFCN", Integer.parseInt(arfcnValue));
                                } else if (arfcnType.toLowerCase().contains("earfcn")) {
                                    return new CellInfoGet(System.currentTimeMillis(), "EARFCN", Integer.parseInt(arfcnValue));
                                } else if (arfcnType.toLowerCase().contains("arfcn")) {
                                    return new CellInfoGet(System.currentTimeMillis(), "ARFCN", Integer.parseInt(arfcnValue));
                                } else if (arfcnType.toLowerCase().contains("nrarfcn")) {
                                    return new CellInfoGet(System.currentTimeMillis(), "NRARFCN", Integer.parseInt(arfcnValue));
                                }
                            }

                        }
                    }
                }

                Integer arfcn = null;
                if (cellInfo1 instanceof CellInfoLte) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        arfcn = ((CellInfoLte) cellInfo1).getCellIdentity().getEarfcn();
                        if (isArfcnAvailable(arfcn)) {
                            return new CellInfoGet(System.currentTimeMillis(), "EARFCN", arfcn);
                        }
                    }
                } else if (cellInfo1 instanceof CellInfoCdma) {
                    // not able to get CDMA band information
                }


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    if (cellInfo1 instanceof CellInfoWcdma) {
                        arfcn = ((CellInfoWcdma) cellInfo1).getCellIdentity().getUarfcn();
                        if (isArfcnAvailable(arfcn)) {
                            return new CellInfoGet(System.currentTimeMillis(), "UARFCN", arfcn);
                        }
                    } else if (cellInfo1 instanceof CellInfoGsm) {
                        arfcn = ((CellInfoGsm) cellInfo1).getCellIdentity().getArfcn();
                        if (isArfcnAvailable(arfcn)) {
                            return new CellInfoGet(System.currentTimeMillis(), "ARFCN", arfcn);
                        }
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (cellInfo1 instanceof CellInfoNr) {
                        CellIdentity cellIdentity = ((CellInfoNr) cellInfo1).getCellIdentity();
                        if (cellIdentity != null) {
                            arfcn = ((CellIdentityNr) cellIdentity).getNrarfcn();
                            return new CellInfoGet(System.currentTimeMillis(), "NRARFCN", arfcn);
                        }
                    } else if (cellInfo1 instanceof CellInfoTdscdma) {
                        arfcn = ((CellInfoTdscdma) cellInfo1).getCellIdentity().getUarfcn();
                        if (isArfcnAvailable(arfcn)) {
                            return new CellInfoGet(System.currentTimeMillis(), "UARFCN", arfcn);
                        }
                    }
                }

            }
        } catch (NumberFormatException e) {
            return null;
        }
        return null;
    }

    public static CellInfoGet parseFromCellInfo(CellInfo cellInfo) {
        try {
            if (cellInfo != null) {
                String s = cellInfo.toString();
                String[] parsedString = s.split(" ");
                for (String s1 : parsedString) {
                    if (s1.toLowerCase().contains("arfcn")) {
                        String[] split = s1.split("=");
                        if (split.length > 1) {
                            String arfcnType = split[0];
                            String arfcnValue = split[1].replace("{", "").replace("}", "");

                            if ((arfcnType != null) && (arfcnValue != null) && !arfcnValue.isEmpty()) {
                                if (arfcnType.toLowerCase().contains("uarfcn")) {
                                    return new CellInfoGet(System.currentTimeMillis(), "UARFCN", Integer.parseInt(arfcnValue));
                                } else if (arfcnType.toLowerCase().contains("earfcn")) {
                                    return new CellInfoGet(System.currentTimeMillis(), "EARFCN", Integer.parseInt(arfcnValue));
                                } else if (arfcnType.toLowerCase().contains("arfcn")) {
                                    return new CellInfoGet(System.currentTimeMillis(), "ARFCN", Integer.parseInt(arfcnValue));
                                } else if (arfcnType.toLowerCase().contains("nrarfcn")) {
                                    return new CellInfoGet(System.currentTimeMillis(), "NRARFCN", Integer.parseInt(arfcnValue));
                                }
                            }
                        }
                    }
                }
                Integer arfcn = null;
                if (cellInfo instanceof CellInfoLte) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        arfcn = ((CellInfoLte) cellInfo).getCellIdentity().getEarfcn();
                        if (isArfcnAvailable(arfcn)) {
                            return new CellInfoGet(System.currentTimeMillis(), "EARFCN", arfcn);
                        }
                    }
                } else if (cellInfo instanceof CellInfoCdma) {
                    // not able to get CDMA band information
                }


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    if (cellInfo instanceof CellInfoWcdma) {
                        arfcn = ((CellInfoWcdma) cellInfo).getCellIdentity().getUarfcn();
                        if (isArfcnAvailable(arfcn)) {
                            return new CellInfoGet(System.currentTimeMillis(), "UARFCN", arfcn);
                        }
                    } else if (cellInfo instanceof CellInfoGsm) {
                        arfcn = ((CellInfoGsm) cellInfo).getCellIdentity().getArfcn();
                        if (isArfcnAvailable(arfcn)) {
                            return new CellInfoGet(System.currentTimeMillis(), "ARFCN", arfcn);
                        }
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (cellInfo instanceof CellInfoNr) {
                        CellIdentity cellIdentity = ((CellInfoNr) cellInfo).getCellIdentity();
                        if (cellIdentity != null) {
                            arfcn = ((CellIdentityNr) cellIdentity).getNrarfcn();
                            return new CellInfoGet(System.currentTimeMillis(), "NRARFCN", arfcn);
                        }
                    } else if (cellInfo instanceof CellInfoTdscdma) {
                        arfcn = ((CellInfoTdscdma) cellInfo).getCellIdentity().getUarfcn();
                        if (isArfcnAvailable(arfcn)) {
                            return new CellInfoGet(System.currentTimeMillis(), "UARFCN", arfcn);
                        }
                    }
                }

            }
        } catch (NumberFormatException e) {
            return null;
        }
        return null;
    }

    public static boolean isArfcnAvailable(Integer arfcn) {
        return arfcn != null && arfcn != Integer.MAX_VALUE;
    }

    public static String getCellId(Context context) {
        String result = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && context != null) {
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
}
