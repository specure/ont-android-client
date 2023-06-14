package at.specure.android.util.connectivity;

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

import at.specure.android.api.jsons.TestResultDetails.CellInfoGet;
import at.specure.android.configs.PermissionHandler;
import at.specure.android.util.InformationCollector;
import at.specure.util.BandCalculationUtil;

public class ConnectionX {

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

    public static ConnectionX instance;
    Context context;

    /**
     * get with Application object
     * @param context
     * @return
     */
    public static ConnectionX getInstance(Context context) {
        if (instance == null) {
            instance = new ConnectionX(context);
        }
        return instance;
    }

    public ConnectionX(Context context) {
        this.context = context;
    }

    /**
     * @param networkType if null then current network will be used
     * @return 0 if unknown, number mobile of network generation (2 - 2G, 3 - 3G, ...)
     */
    public int getMobileNetworkGeneration(Integer networkType) {

        if (networkType == null) {
            TelephonyManager mTelephonyManager = (TelephonyManager)
                    context.getSystemService(Context.TELEPHONY_SERVICE);
            if (mTelephonyManager != null) {
                networkType = mTelephonyManager.getNetworkType();
            } else {
                networkType = -1;
            }
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
    public CellSignalStrength getCurrentSignalStrengthObject(Context context) {
        CellSignalStrength result = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            // for example value of first element

            boolean coarseLocationPermitted = PermissionHandler.isCoarseLocationPermitted(context);
            if (coarseLocationPermitted) {
                // permission checked in static method above
                @SuppressLint("MissingPermission") List<CellInfo> allCellInfo = getTelephonyManager().getAllCellInfo();
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
     * @return
     */
    public Integer getCurrentSignalStrength() {
        Integer result = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            // for example value of first element
            boolean coarseLocationPermitted = PermissionHandler.isCoarseLocationPermitted(context);
            if (coarseLocationPermitted) {
                // permission checked in static method above
                @SuppressLint("MissingPermission") List<CellInfo> allCellInfo = getTelephonyManager().getAllCellInfo();
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

    private ConnectivityManager getConnectivityManager() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager;
    }

    private TelephonyManager getTelephonyManager() {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager;
    }

    @SuppressLint("MissingPermission")
    private List<CellInfo> getCellInfo() {
        TelephonyManager telephonyManager = getTelephonyManager();
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
    public BandCalculationUtil.FrequencyInformation getCellBandAndFrequency() {
        List<CellInfo> cellInfo = getCellInfo();
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

    public CellInfoGet getARFCNCellInfo() {
        List<CellInfo> cellInfo = getCellInfo();
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



    public String getCellId() {
        String result = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            List<CellInfo> cellInfos = getCellInfo();
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
    public int getNetwork() {
        int result = TelephonyManager.NETWORK_TYPE_UNKNOWN;

        ConnectivityManager connManager = getConnectivityManager();
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
                        TelephonyManager telephonyManager = getTelephonyManager();
                        result = telephonyManager.getNetworkType();
                        break;
                }
            }
        }
        return result;
    }

}
