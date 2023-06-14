package at.specure.androidX.data.test.testResultRequest;

import android.content.Context;
import android.os.Build;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import com.specure.opennettest.BuildConfig;
import com.specure.opennettest.R;

import java.util.List;

import at.specure.android.api.jsons.CellLocation;
import at.specure.android.api.jsons.Location;
import at.specure.android.api.jsons.Signal;
import at.specure.android.api.jsons.TestResultDetails.CellInfoGet;
import at.specure.android.api.jsons.VoipTestResult;
import at.specure.android.configs.ConfigHelper;
import at.specure.android.configs.LocaleConfig;
import at.specure.android.configs.LoopModeConfig;
import at.specure.android.util.InformationCollector;
import at.specure.android.util.network.network.NRConnectionState;
import at.specure.client.SpeedItem;
import at.specure.client.TotalTestResult;
import at.specure.client.helper.Config;
import at.specure.client.helper.TestStatus;
import at.specure.client.v2.task.service.TestMeasurement;

import static at.specure.android.util.InformationCollector.NETWORK_BLUETOOTH;
import static at.specure.android.util.InformationCollector.NETWORK_ETHERNET;
import static at.specure.android.util.InformationCollector.NETWORK_WIFI;

public class TestResultRequest {

    @SerializedName("client_uuid")
    String clientUUID;

    @SerializedName("loop_uuid")
    String loopUUID;

    /**
     * See {@link NRConnectionState} but {@link NRConnectionState.NOT_AVAILABLE} is not sent as string but as null,
     * other values are transformed into string constants and sent
     */
    @SerializedName("telephony_nr_connection")
    public String nrConnectionState;

    //TODO: same as client uuid - can I remove this?
    @SerializedName("uuid")
    String uuid;

    @SerializedName("plattform")
    String platform;

    @SerializedName("product")
    String product;

    @SerializedName("api_level")
    String apiLevel;  //e.g. 28

    @SerializedName("client_version")
    String clientVersion;  //e.g. 28

    @SerializedName("client_name")
    String clientName; //RMBT

    @SerializedName("client_software_version")
    String clientSoftwareVersion;  //e.g 2.7.30 internal test


    /**
     * WIFI only
     **/
    @SerializedName("wifi_supplicant_state_detail")
    String wifiSupplicantStateDetail;  // e.g. OBTAINING_IPADDR, todo: ???

    @SerializedName("wifi_supplicant_state")
    String wifiSupplicantState;  // e.g. COMPLETED, todo: ???

    @SerializedName("wifi_ssid")
    String wifiSSID; // e.g. Martes-Specure

    @SerializedName("wifi_bssid")
    String wifiBSSID;   //e.g. b8:ec:a3:f8:3f:08

    @SerializedName("wifi_network_id")
    String wifiNetworkId; //e.g. "4"

//removed previously also in info object
//    @SerializedName("wifi_linkspeed")
//    String wifiLinkspeed;
//
//    @SerializedName("wifi_rssi")
//    String wifirssi;

    /** END WIFI only **/

    /**
     * MOBILE NET ONLY
     **/

    @SerializedName("telephony_network_operator")
    String telephonyNetworkOperator; //todo: ??? not filled

    @SerializedName("telephony_network_is_roaming")
    String telephonyNetworkIsRoaming = "false";//todo: ??? not filled

    @SerializedName("telephony_network_country")
    String telephonyNetworkCountry; //todo: ??? not filled

    @SerializedName("telephony_network_operator_name")
    String telephonyNetworkOperatorName; //e.g. "O2 - SK"

    @SerializedName("telephony_network_sim_operator_name")
    String telephonyNetworkSimOperatorName; // todo: ??? not filled

    @SerializedName("telephony_network_sim_operator")
    String telephonyNetworkSimOperator; // todo: ??? not filled

    @SerializedName("telephony_network_sim_country")
    String telephonyNetworkSimCountry; //todo: ??? not filled

    @SerializedName("telephony_phone_type")
    String telephonyPhoneType; //todo: ??? not filled

    @SerializedName("telephony_data_state")
    String telephonyDataState; //todo: ??? not filled

    @SerializedName("cells_info")
    List<CellInfoGet> cellInfos;

    @SerializedName("cellLocations")
    List<CellLocation> cellLocations;

    /**
     * END MOBILE NET ONLY
     **/

    @SerializedName("os_version")
    String osVersion; // e.g. 9(G950FXXS4DSC2)

    @SerializedName("network_type")
    public String networkType; //e.g "99" for wifi //TODO: make constants

    @SerializedName("model")
    String model; //e.g.SM-G950F

    @SerializedName("device")
    String device; //e.g dreamlte

    @SerializedName("client_language")
    String clientLanguage;  //e.g. en

    @SerializedName("time")
    Long timestamp;

    @SerializedName("test_token")
    String testToken;

    @SerializedName("test_port_remote")
    Integer testPortRemote; //5323

    @SerializedName("test_bytes_download")
    Long testBytesDownload;

    @SerializedName("test_bytes_upload")
    Long testBytesUpload;

    @SerializedName("test_total_bytes_download")
    Long testTotalBytesDownload;

    @SerializedName("test_total_bytes_upload")
    Long testTotalBytesUpload;

    @SerializedName("test_encryption")
    String testEncryption; //e.g. TLSv1.2 (TLS_RSA_WITH_AES_128_GCM_SHA256)

    @SerializedName("test_ip_local")
    String testIPLocal;

    @SerializedName("test_ip_server")
    String testIPServer;

    @SerializedName("test_nsec_download")
    Long testDownloadDurationNSec;

    @SerializedName("test_nsec_upload")
    Long testUploadDurationNSec;

    @SerializedName("test_num_threads")
    Integer testThreadNumber;

    @SerializedName("num_threads_ul")
    Integer testUploadThreadNumber;

    @SerializedName("test_speed_download")
    Long testDownloadSpeed;

    @SerializedName("test_speed_upload")
    Long testUploadSpeed;

    @SerializedName("test_ping_shortest")
    Long testPingShortest;

    @SerializedName("test_if_bytes_download")
    Long testIFBytesDownload;

    @SerializedName("test_if_bytes_upload")
    Long testIFBytesUpload;

    @SerializedName("testdl_if_bytes_download")
    Long testDownloadIFBytesDownload;

    @SerializedName("testdl_if_bytes_upload")
    Long testDownloadIFBytesUpload;

    @SerializedName("testul_if_bytes_download")
    Long testUploadIFBytesDownload;

    @SerializedName("testul_if_bytes_upload")
    Long testUploadIFBytesUpload;

    @SerializedName("time_dl_ns")
    Long testDownloadNSecTimestamp;

    @SerializedName("time_ul_ns")
    Long testUploadNSecTimestamp;

    @SerializedName("jpl")
    VoipTestResult jitterPacketLossResult;

    @SerializedName("pings")
    List<Ping> pings;

    @SerializedName("speed_detail")
    List<SpeedItem> speedDetails;

    @SerializedName("geoLocations")
    List<Location> geoLocations;

    @SerializedName("signals")
    List<Signal> signals;

    /**
     * No more provided because of problem to obtain from various devices, only passed as JsonElement
     */
    @SerializedName("extended_test_stats")
    JsonElement extendedTestStats;

    @SerializedName("publish_public_data")
    Boolean publishPublicData = false;

    @SerializedName("tag")
    String tag;


    public TestResultRequest(Context ctx, String testToken, TotalTestResult totalTestResult, Long startTimeNs, TestResultProperties testProperties, int networkType, List<CellLocation> cellLocations, List<Signal> signals, List<Location> geoLocations, List<CellInfoGet> cellInfos, NRConnectionState nrConnectionState, JsonElement extendedStats) {
        this.platform = InformationCollector.PLATTFORM_NAME;
        this.osVersion = Build.VERSION.RELEASE + "(" + Build.VERSION.INCREMENTAL + ")";
        this.apiLevel = String.valueOf(Build.VERSION.SDK_INT);
        this.device = String.valueOf(Build.DEVICE);
        this.model = String.valueOf(Build.MODEL);
        this.product = String.valueOf(Build.PRODUCT);
        this.clientLanguage = LocaleConfig.getLocaleForServerRequest(ctx);
        this.timestamp = System.currentTimeMillis();
        this.testToken = testToken;
//        PackageInfo pInfo = getPackageInfo(ctx);
//        if (pInfo != null) {
//            this.softwareVersionCode = String.valueOf(pInfo.versionCode);
//            this.softwareVersionName = pInfo.versionName;
//            this.versionName = pInfo.versionName;
//            this.versionCode = String.valueOf(pInfo.versionCode);
//        }
//        this.clitype = Config.RMBT_CLIENT_TYPE;
        this.clientUUID = uuid = ConfigHelper.getUUID(ctx);
        this.clientName = ctx.getResources().getString(R.string.app_name_api); //RMBT
        this.clientVersion = totalTestResult.client_version;
        this.clientSoftwareVersion = BuildConfig.VERSION_NAME;
        this.loopUUID = LoopModeConfig.getCurrentLoopId(ctx);
//        this.nrConnectionState = NRConnectionState.NSA.name();
        if (nrConnectionState != null) {
            this.nrConnectionState = nrConnectionState.name();
        } else {
            this.nrConnectionState = null;
        }

        this.networkType = String.valueOf(networkType);

        if (testProperties != null) {
            if (networkType == NETWORK_WIFI) {
                this.wifiSupplicantStateDetail = testProperties.wifiSupplicantStateDetail;
                this.wifiSupplicantState = testProperties.wifiSupplicantState;
                this.wifiSSID = testProperties.wifiSSID;
                this.wifiBSSID = testProperties.wifiBSSID;
                this.wifiNetworkId = testProperties.wifiNetworkId;
            } else {
                if ((networkType != NETWORK_BLUETOOTH) && (networkType != NETWORK_ETHERNET)) {
                    this.telephonyNetworkOperator = testProperties.telephonyNetworkOperator;
                    this.telephonyNetworkIsRoaming = String.valueOf(testProperties.telephonyNetworkIsRoaming);
                    this.telephonyNetworkCountry = testProperties.telephonyNetworkCountry;
                    this.telephonyNetworkOperatorName = testProperties.telephonyNetworkOperatorName;
                    this.telephonyNetworkSimOperatorName = testProperties.telephonyNetworkSimOperatorName;
                    this.telephonyNetworkSimOperator = testProperties.telephonyNetworkSimOperator;
                    this.telephonyNetworkSimCountry = testProperties.telephonyNetworkSimCountry;
                    this.telephonyPhoneType = testProperties.telephonyPhoneType;
                    this.telephonyDataState = testProperties.telephonyDataState;

                    if (cellInfos != null) {
                        this.cellInfos = cellInfos;
                    }

                    this.cellLocations = cellLocations;
                }
            }
        }

        this.testPortRemote = totalTestResult.port_remote;
        this.testBytesDownload = totalTestResult.bytes_download;
        this.testBytesUpload = totalTestResult.bytes_upload;
        this.testTotalBytesDownload = totalTestResult.totalDownBytes;
        this.testTotalBytesUpload = totalTestResult.totalUpBytes;
        this.testEncryption = totalTestResult.encryption;
        this.testIPLocal = totalTestResult.ip_local.getHostAddress();
        this.testIPServer = totalTestResult.ip_server.getHostAddress();
        this.testDownloadDurationNSec = totalTestResult.nsec_download;
        this.testUploadDurationNSec = totalTestResult.nsec_upload;
        this.testThreadNumber = totalTestResult.num_threads;
        this.testUploadThreadNumber = totalTestResult.num_threads_ul;
        this.testUploadSpeed = (long) Math.floor(totalTestResult.speed_upload + 0.5d);
        this.testDownloadSpeed = (long) Math.floor(totalTestResult.speed_download + 0.5d);
        this.testPingShortest = totalTestResult.ping_shortest;
        this.pings = totalTestResult.pings;
        this.speedDetails = totalTestResult.speedItems;
        //interface data
        this.testIFBytesDownload = totalTestResult.getTotalTrafficMeasurement(TestMeasurement.TrafficDirection.RX);
        this.testIFBytesUpload = totalTestResult.getTotalTrafficMeasurement(TestMeasurement.TrafficDirection.TX);
        //bytes during download phase
        this.testDownloadIFBytesDownload = totalTestResult.getTrafficByTestPart(TestStatus.DOWN, TestMeasurement.TrafficDirection.RX);
        this.testDownloadIFBytesUpload = totalTestResult.getTrafficByTestPart(TestStatus.DOWN, TestMeasurement.TrafficDirection.TX);
        //bytes during upload phase
        this.testUploadIFBytesDownload = totalTestResult.getTrafficByTestPart(TestStatus.UP, TestMeasurement.TrafficDirection.RX);
        this.testUploadIFBytesUpload = totalTestResult.getTrafficByTestPart(TestStatus.UP, TestMeasurement.TrafficDirection.TX);
        //relative timestamps
        TestMeasurement dlMeasurement = totalTestResult.getTestMeasurementByTestPart(TestStatus.DOWN);
        if (dlMeasurement != null) {
            this.testDownloadNSecTimestamp = dlMeasurement.getTimeStampStart() - startTimeNs;
        }
        TestMeasurement ulMeasurement = totalTestResult.getTestMeasurementByTestPart(TestStatus.UP);
        if (ulMeasurement != null) {
            this.testUploadNSecTimestamp = ulMeasurement.getTimeStampStart() - startTimeNs;
        }
        //jitter and packet loss
        this.jitterPacketLossResult = totalTestResult.voipTestResult;

        // do not send signal values if it is 5G NSA mode because backend is not able to handle it correctly then
        if (nrConnectionState == null || nrConnectionState == NRConnectionState.AVAILABLE || nrConnectionState == NRConnectionState.NOT_AVAILABLE || nrConnectionState == NRConnectionState.SA) {
            if (signals != null) {
                for (Signal signal : signals) {
                    signal.setTimeNs(signal.getTimeNs() - startTimeNs);
                }
                this.signals = signals;
            }
        }



        if (extendedStats != null) {
            extendedTestStats = extendedStats;
        }

        if (ctx != null) {
            boolean dataFuzzing = ctx.getResources().getBoolean(R.bool.test_use_personal_data_fuzzing);
            if (dataFuzzing) {
                publishPublicData = ConfigHelper.isInformationCommissioner(ctx);
            }
            final String tag = ConfigHelper.getTag(ctx);
            if (tag != null && !tag.isEmpty())
                this.tag = tag;
        }

        if (geoLocations != null) {
            for (Location location : geoLocations) {
                location.setTime(location.getTime() + (location.getTimestamp() != null ? location.getTimestamp() : 0) - startTimeNs);
            }
            this.geoLocations = geoLocations;
        }


    }

}
