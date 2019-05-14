package at.specure.android.api.jsons.TestResultDetailOpenData;

import com.google.gson.annotations.SerializedName;

import at.specure.android.api.jsons.VoipTestResult;

public class OpenDataJson {

    @SerializedName("upload_classification")
    Integer classificationUpload;

    /**
     * test duration in s
     */
    @SerializedName("test_duration")
    Integer testDuration;

    @SerializedName("municipality")
    String municipality;

    @SerializedName("testdl_if_bytes_upload")
    Long bytesUploadedDuringDownloadPhaseOnInterface;

    @SerializedName("testdl_if_bytes_download")
    Long bytesDownloadedDuringDownloadPhaseOnInterface;

    @SerializedName("testul_if_bytes_upload")
    Long bytesUploadedDuringUploadPhaseOnInterface;

    @SerializedName("testul_if_bytes_download")
    Long bytesDownloadedDuringUploadPhaseOnInterface;

    @SerializedName("test_if_bytes_upload")
    Long bytesUploadedDuringTestOnInterface;

    @SerializedName("test_if_bytes_download")
    Long bytesDownloadedDuringTestOnInterface;

    @SerializedName("sim_country")
    String simCountry;

    @SerializedName("zip_code")
    String zipCode;

    @SerializedName("ndt_download_kbit") // float ???
            Float ndtDownloadKBit;

    @SerializedName("time_dl_ms")
    Float durationDownloadPhase;

    @SerializedName("time_ul_ms")
    Float durationUploadPhase;

    @SerializedName("public_ip_as_name")
    String providerNameFromIpAddress;

    @SerializedName("country_geoip") // 2 letter country code
            String countryCodeFromGEOIP;

    @SerializedName("ping_classification")
    Integer classificationPing;

    @SerializedName("signal_classification")
    Integer classificationSignal;

    @SerializedName("download_classification")
    Integer classificationDownload;

    @SerializedName("roaming_type")
    String roumingType;

    @SerializedName("implausible")
    Boolean isInvalid;

    @SerializedName("model")
    String deviceName;

    @SerializedName("connection")
    String connectionDescription;

    @SerializedName("signal_strength")
    Integer signalStrength;

    @SerializedName("lat")
    Float latitude;

    @SerializedName("long")
    Float longitude;

    @SerializedName("cell_id")
    Integer cellID;

    @SerializedName("duration_download_ms")
    Float downloadDurationInMS;

    @SerializedName("duration_upload_ms")
    Float uploadDurationInMS;

    @SerializedName("network_name")
    String networkName;

    @SerializedName("bytes_upload")
    Long bytesUpload;

    @SerializedName("bytes_download")
    Long bytesDownload;

    @SerializedName("ip_anonym")
    String ipAddressAnonymized; // ip without last segment

    @SerializedName("upload_kbit")
    Long uploadKbit;

    @SerializedName("download_kbit")
    Long downloadKbit;

    @SerializedName("ping_ms")
    Float pingInMS;

    @SerializedName("wifi_link_speed")
    Float wifiLinkSpeed; // float ???

    @SerializedName("num_threads")
    Integer threadsNumber;

    @SerializedName("num_threads_ul")
    Integer threadsUploadNumber;

    @SerializedName("num_threads_requested")
    Integer threadsNumberRequested;

    @SerializedName("cat_technology")
    String technologyCategory;

    @SerializedName("speed_test_duration")
    Float durationOfSpeedTest; // float/int ???

    @SerializedName("region")
    String region;

    @SerializedName("server_name")
    String serverName;

    @SerializedName("model_native")
    String deviceNameNative; // Lenovo K10a40

    @SerializedName("product")
    String productName; // K10a40

    /**
     * Distance in meters with unit added
     */
    @SerializedName("distance")
    String distanceTravelledDuringTest; // XXXXm?

    @SerializedName("country_location")
    String countryLocation; // "SK"

    @SerializedName("platform")
    String platform; // "Android"

    @SerializedName("country_asn")
    String asnCountry; // "SK"

    @SerializedName("asn")
    Integer asnNumber; // "SK"

    @SerializedName("loc_accuracy")
    Float locationAccuracy;

    @SerializedName("loc_src")
    String locationSource;

    @SerializedName("time")
    String timeFormatted;

    @SerializedName("client_version")
    String clientVersion;

    @SerializedName("provider_name")
    String providerNameWithCountry;

    @SerializedName("network_type")
    String networkType;

    @SerializedName("open_test_uuid")
    String openTestUUID;

    @SerializedName("cell_id_multiple")
    Boolean cellIdMultiple = false;

    @SerializedName("lte_rsrq")
    Long lteRSRQ;

    @SerializedName("lte_rsrp")
    Long lteRSRP;

    @SerializedName("network_mcc_mnc")
    String networkMccMnc;

    @SerializedName("sim_mcc_mnc")
    String simMccMnc;

    @SerializedName("whitespot")
    String whitespot;

    @SerializedName("settlement")
    String settlement;

    @SerializedName("cell_name")
    String cellName;

    @SerializedName("network_country")
    String networkCountry;

    @SerializedName("ndt_upload_kbit")
    Float ndtUploadKbit;

    @SerializedName("speed_curve")
    SpeedCurve speedCurve;

    @SerializedName("jpl")
    VoipTestResult voipResults;


    public Integer getClassificationUpload() {
        return classificationUpload;
    }

    public void setClassificationUpload(Integer classificationUpload) {
        this.classificationUpload = classificationUpload;
    }

    public Integer getTestDuration() {
        return testDuration;
    }

    public void setTestDuration(Integer testDuration) {
        this.testDuration = testDuration;
    }

    public String getMunicipality() {
        return municipality;
    }

    public void setMunicipality(String municipality) {
        this.municipality = municipality;
    }

    public Long getBytesUploadedDuringDownloadPhaseOnInterface() {
        return bytesUploadedDuringDownloadPhaseOnInterface;
    }

    public void setBytesUploadedDuringDownloadPhaseOnInterface(Long bytesUploadedDuringDownloadPhaseOnInterface) {
        this.bytesUploadedDuringDownloadPhaseOnInterface = bytesUploadedDuringDownloadPhaseOnInterface;
    }

    public Long getBytesDownloadedDuringDownloadPhaseOnInterface() {
        return bytesDownloadedDuringDownloadPhaseOnInterface;
    }

    public void setBytesDownloadedDuringDownloadPhaseOnInterface(Long bytesDownloadedDuringDownloadPhaseOnInterface) {
        this.bytesDownloadedDuringDownloadPhaseOnInterface = bytesDownloadedDuringDownloadPhaseOnInterface;
    }

    public Long getBytesUploadedDuringUploadPhaseOnInterface() {
        return bytesUploadedDuringUploadPhaseOnInterface;
    }

    public void setBytesUploadedDuringUploadPhaseOnInterface(Long bytesUploadedDuringUploadPhaseOnInterface) {
        this.bytesUploadedDuringUploadPhaseOnInterface = bytesUploadedDuringUploadPhaseOnInterface;
    }

    public Long getBytesDownloadedDuringUploadPhaseOnInterface() {
        return bytesDownloadedDuringUploadPhaseOnInterface;
    }

    public void setBytesDownloadedDuringUploadPhaseOnInterface(Long bytesDownloadedDuringUploadPhaseOnInterface) {
        this.bytesDownloadedDuringUploadPhaseOnInterface = bytesDownloadedDuringUploadPhaseOnInterface;
    }

    public Long getBytesUploadedDuringTestOnInterface() {
        return bytesUploadedDuringTestOnInterface;
    }

    public void setBytesUploadedDuringTestOnInterface(Long bytesUploadedDuringTestOnInterface) {
        this.bytesUploadedDuringTestOnInterface = bytesUploadedDuringTestOnInterface;
    }

    public Long getBytesDownloadedDuringTestOnInterface() {
        return bytesDownloadedDuringTestOnInterface;
    }

    public void setBytesDownloadedDuringTestOnInterface(Long bytesDownloadedDuringTestOnInterface) {
        this.bytesDownloadedDuringTestOnInterface = bytesDownloadedDuringTestOnInterface;
    }

    public String getSimCountry() {
        return simCountry;
    }

    public void setSimCountry(String simCountry) {
        this.simCountry = simCountry;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public Float getNdtDownloadKBit() {
        return ndtDownloadKBit;
    }

    public void setNdtDownloadKBit(Float ndtDownloadKBit) {
        this.ndtDownloadKBit = ndtDownloadKBit;
    }

    public Float getDurationDownloadPhase() {
        return durationDownloadPhase;
    }

    public void setDurationDownloadPhase(Float durationDownloadPhase) {
        this.durationDownloadPhase = durationDownloadPhase;
    }

    public Float getDurationUploadPhase() {
        return durationUploadPhase;
    }

    public void setDurationUploadPhase(Float durationUploadPhase) {
        this.durationUploadPhase = durationUploadPhase;
    }

    public String getProviderNameFromIpAddress() {
        return providerNameFromIpAddress;
    }

    public void setProviderNameFromIpAddress(String providerNameFromIpAddress) {
        this.providerNameFromIpAddress = providerNameFromIpAddress;
    }

    public String getCountryCodeFromGEOIP() {
        return countryCodeFromGEOIP;
    }

    public void setCountryCodeFromGEOIP(String countryCodeFromGEOIP) {
        this.countryCodeFromGEOIP = countryCodeFromGEOIP;
    }

    public Integer getClassificationPing() {
        return classificationPing;
    }

    public void setClassificationPing(Integer classificationPing) {
        this.classificationPing = classificationPing;
    }

    public Integer getClassificationSignal() {
        return classificationSignal;
    }

    public void setClassificationSignal(Integer classificationSignal) {
        this.classificationSignal = classificationSignal;
    }

    public Integer getClassificationDownload() {
        return classificationDownload;
    }

    public void setClassificationDownload(Integer classificationDownload) {
        this.classificationDownload = classificationDownload;
    }

    public String getRoumingType() {
        return roumingType;
    }

    public void setRoumingType(String roumingType) {
        this.roumingType = roumingType;
    }

    public Boolean getInvalid() {
        return isInvalid;
    }

    public void setInvalid(Boolean invalid) {
        isInvalid = invalid;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getConnectionDescription() {
        return connectionDescription;
    }

    public void setConnectionDescription(String connectionDescription) {
        this.connectionDescription = connectionDescription;
    }

    public Integer getSignalStrength() {
        return signalStrength;
    }

    public void setSignalStrength(Integer signalStrength) {
        this.signalStrength = signalStrength;
    }

    public Float getLatitude() {
        return latitude;
    }

    public void setLatitude(Float latitude) {
        this.latitude = latitude;
    }

    public Float getLongitude() {
        return longitude;
    }

    public void setLongitude(Float longitude) {
        this.longitude = longitude;
    }

    public Integer getCellID() {
        return cellID;
    }

    public void setCellID(Integer cellID) {
        this.cellID = cellID;
    }

    public Float getDownloadDurationInMS() {
        return downloadDurationInMS;
    }

    public void setDownloadDurationInMS(Float downloadDurationInMS) {
        this.downloadDurationInMS = downloadDurationInMS;
    }

    public Float getUploadDurationInMS() {
        return uploadDurationInMS;
    }

    public void setUploadDurationInMS(Float uploadDurationInMS) {
        this.uploadDurationInMS = uploadDurationInMS;
    }

    public String getNetworkName() {
        return networkName;
    }

    public void setNetworkName(String networkName) {
        this.networkName = networkName;
    }

    public Long getBytesUpload() {
        return bytesUpload;
    }

    public void setBytesUpload(Long bytesUpload) {
        this.bytesUpload = bytesUpload;
    }

    public Long getBytesDownload() {
        return bytesDownload;
    }

    public void setBytesDownload(Long bytesDownload) {
        this.bytesDownload = bytesDownload;
    }

    public String getIpAddressAnonymized() {
        return ipAddressAnonymized;
    }

    public void setIpAddressAnonymized(String ipAddressAnonymized) {
        this.ipAddressAnonymized = ipAddressAnonymized;
    }

    public Long getUploadKbit() {
        return uploadKbit;
    }

    public void setUploadKbit(Long uploadKbit) {
        this.uploadKbit = uploadKbit;
    }

    public Long getDownloadKbit() {
        return downloadKbit;
    }

    public void setDownloadKbit(Long downloadKbit) {
        this.downloadKbit = downloadKbit;
    }

    public Float getPingInMS() {
        return pingInMS;
    }

    public void setPingInMS(Float pingInMS) {
        this.pingInMS = pingInMS;
    }

    public Float getWifiLinkSpeed() {
        return wifiLinkSpeed;
    }

    public void setWifiLinkSpeed(Float wifiLinkSpeed) {
        this.wifiLinkSpeed = wifiLinkSpeed;
    }

    public Integer getThreadsNumber() {
        return threadsNumber;
    }

    public void setThreadsNumber(Integer threadsNumber) {
        this.threadsNumber = threadsNumber;
    }

    public Integer getThreadsUploadNumber() {
        return threadsUploadNumber;
    }

    public void setThreadsUploadNumber(Integer threadsUploadNumber) {
        this.threadsUploadNumber = threadsUploadNumber;
    }

    public Integer getThreadsNumberRequested() {
        return threadsNumberRequested;
    }

    public void setThreadsNumberRequested(Integer threadsNumberRequested) {
        this.threadsNumberRequested = threadsNumberRequested;
    }

    public String getTechnologyCategory() {
        return technologyCategory;
    }

    public void setTechnologyCategory(String technologyCategory) {
        this.technologyCategory = technologyCategory;
    }

    public Float getDurationOfSpeedTest() {
        return durationOfSpeedTest;
    }

    public void setDurationOfSpeedTest(Float durationOfSpeedTest) {
        this.durationOfSpeedTest = durationOfSpeedTest;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getDeviceNameNative() {
        return deviceNameNative;
    }

    public void setDeviceNameNative(String deviceNameNative) {
        this.deviceNameNative = deviceNameNative;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getDistanceTravelledDuringTest() {
        return distanceTravelledDuringTest;
    }

    public void setDistanceTravelledDuringTest(String distanceTravelledDuringTest) {
        this.distanceTravelledDuringTest = distanceTravelledDuringTest;
    }

    public String getCountryLocation() {
        return countryLocation;
    }

    public void setCountryLocation(String countryLocation) {
        this.countryLocation = countryLocation;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getAsnCountry() {
        return asnCountry;
    }

    public void setAsnCountry(String asnCountry) {
        this.asnCountry = asnCountry;
    }

    public Integer getAsnNumber() {
        return asnNumber;
    }

    public void setAsnNumber(Integer asnNumber) {
        this.asnNumber = asnNumber;
    }

    public Float getLocationAccuracy() {
        return locationAccuracy;
    }

    public void setLocationAccuracy(Float locationAccuracy) {
        this.locationAccuracy = locationAccuracy;
    }

    public String getLocationSource() {
        return locationSource;
    }

    public void setLocationSource(String locationSource) {
        this.locationSource = locationSource;
    }

    public String getTimeFormatted() {
        return timeFormatted;
    }

    public void setTimeFormatted(String timeFormatted) {
        this.timeFormatted = timeFormatted;
    }

    public String getClientVersion() {
        return clientVersion;
    }

    public void setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
    }

    public String getProviderNameWithCountry() {
        return providerNameWithCountry;
    }

    public void setProviderNameWithCountry(String providerNameWithCountry) {
        this.providerNameWithCountry = providerNameWithCountry;
    }

    public String getNetworkType() {
        return networkType;
    }

    public void setNetworkType(String networkType) {
        this.networkType = networkType;
    }

    public String getOpenTestUUID() {
        return openTestUUID;
    }

    public void setOpenTestUUID(String openTestUUID) {
        this.openTestUUID = openTestUUID;
    }

    public Boolean getCellIdMultiple() {
        return cellIdMultiple;
    }

    public void setCellIdMultiple(Boolean cellIdMultiple) {
        this.cellIdMultiple = cellIdMultiple;
    }

    public Long getLteRSRQ() {
        return lteRSRQ;
    }

    public void setLteRSRQ(Long lteRSRQ) {
        this.lteRSRQ = lteRSRQ;
    }

    public Long getLteRSRP() {
        return lteRSRP;
    }

    public void setLteRSRP(Long lteRSRP) {
        this.lteRSRP = lteRSRP;
    }

    public String getNetworkMccMnc() {
        return networkMccMnc;
    }

    public void setNetworkMccMnc(String networkMccMnc) {
        this.networkMccMnc = networkMccMnc;
    }

    public String getSimMccMnc() {
        return simMccMnc;
    }

    public void setSimMccMnc(String simMccMnc) {
        this.simMccMnc = simMccMnc;
    }

    public String getWhitespot() {
        return whitespot;
    }

    public void setWhitespot(String whitespot) {
        this.whitespot = whitespot;
    }

    public String getSettlement() {
        return settlement;
    }

    public void setSettlement(String settlement) {
        this.settlement = settlement;
    }

    public String getCellName() {
        return cellName;
    }

    public void setCellName(String cellName) {
        this.cellName = cellName;
    }

    public String getNetworkCountry() {
        return networkCountry;
    }

    public void setNetworkCountry(String networkCountry) {
        this.networkCountry = networkCountry;
    }

    public Float getNdtUploadKbit() {
        return ndtUploadKbit;
    }

    public void setNdtUploadKbit(Float ndtUploadKbit) {
        this.ndtUploadKbit = ndtUploadKbit;
    }

    public SpeedCurve getSpeedCurve() {
        return speedCurve;
    }

    public void setSpeedCurve(SpeedCurve speedCurve) {
        this.speedCurve = speedCurve;
    }

    public VoipTestResult getVoipResults() {
        return voipResults;
    }

    public void setVoipResults(VoipTestResult voipResults) {
        this.voipResults = voipResults;
    }
}
