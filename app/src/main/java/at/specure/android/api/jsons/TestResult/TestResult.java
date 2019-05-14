package at.specure.android.api.jsons.TestResult;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import at.specure.android.api.jsons.TestResultDetails.CellInfoGet;
import at.specure.android.api.jsons.VoipTestResult;

@SuppressWarnings({"unused", "WeakerAccess"})
public class TestResult {

    @SerializedName("loop_uuid")
    String loopID;

    @SerializedName("share_text")
    String shareText;

    @SerializedName("share_subject")
    String shareSubject;

    @SerializedName("open_test_uuid")
    String testOpenUUID;

    @SerializedName("open_uuid")
    String openUUID;

    @SerializedName("timezone")
    String timezone;

    @SerializedName("time_string")
    String timeFormatted;

    @SerializedName("time")
    Long timestamp;

    @SerializedName("location")
    String humanReadableLocation;

    @SerializedName("jpl")
    VoipTestResult voipResult;

    /**
     * Download, upload, ping, signal
     */
    @SerializedName("measurement")
    List<MeasuredValue> measuredValues;

    /**
     * Connection, Operator, SSID/Roaming/...
     */
    @SerializedName("net")
    List<NetworkInfo> networkInfo;

    @SerializedName("network_type")
    Integer networkType;

    @SerializedName("geo_long")
    Float geoLong;

    @SerializedName("geo_lat")
    Float geoLat;

    @SerializedName("cells_info")
    List<CellInfoGet> cellsInfo;

    public TestResult(String shareText, String shareSubject, String testOpenUUID, String openUUID, String timezone, String timeFormatted, Long timestamp, String humanReadableLocation, VoipTestResult voipResult, List<MeasuredValue> measuredValues, List<NetworkInfo> networkInfo, Integer networkType, Float geoLong, Float geoLat, List<CellInfoGet> cellsInfo) {
        this.shareText = shareText;
        this.shareSubject = shareSubject;
        this.testOpenUUID = testOpenUUID;
        this.openUUID = openUUID;
        this.timezone = timezone;
        this.timeFormatted = timeFormatted;
        this.timestamp = timestamp;
        this.humanReadableLocation = humanReadableLocation;
        this.voipResult = voipResult;
        this.measuredValues = measuredValues;
        this.networkInfo = networkInfo;
        this.networkType = networkType;
        this.geoLong = geoLong;
        this.geoLat = geoLat;
        this.cellsInfo = cellsInfo;
    }

    public List<CellInfoGet> getCellsInfo() {
        return cellsInfo;
    }

    public void setCellsInfo(List<CellInfoGet> cellsInfo) {
        this.cellsInfo = cellsInfo;
    }

    public String getShareText() {
        return shareText;
    }

    public void setShareText(String shareText) {
        this.shareText = shareText;
    }

    public String getShareSubject() {
        return shareSubject;
    }

    public void setShareSubject(String shareSubject) {
        this.shareSubject = shareSubject;
    }

    public String getTestOpenUUID() {
        return testOpenUUID;
    }

    public void setTestOpenUUID(String testOpenUUID) {
        this.testOpenUUID = testOpenUUID;
    }

    public String getOpenUUID() {
        return openUUID;
    }

    public void setOpenUUID(String openUUID) {
        this.openUUID = openUUID;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getTimeFormatted() {
        return timeFormatted;
    }

    public void setTimeFormatted(String timeFormatted) {
        this.timeFormatted = timeFormatted;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getHumanReadableLocation() {
        return humanReadableLocation;
    }

    public void setHumanReadableLocation(String humanReadableLocation) {
        this.humanReadableLocation = humanReadableLocation;
    }

    public VoipTestResult getVoipResult() {
        return voipResult;
    }

    public void setVoipResult(VoipTestResult voipResult) {
        this.voipResult = voipResult;
    }

    public List<MeasuredValue> getMeasuredValues() {
        return measuredValues;
    }

    public void setMeasuredValues(List<MeasuredValue> measuredValues) {
        this.measuredValues = measuredValues;
    }

    public List<NetworkInfo> getNetworkInfo() {
        return networkInfo;
    }

    public void setNetworkInfo(List<NetworkInfo> networkInfo) {
        this.networkInfo = networkInfo;
    }

    public Integer getNetworkType() {
        return networkType;
    }

    public void setNetworkType(Integer networkType) {
        this.networkType = networkType;
    }

    public Float getGeoLong() {
        return geoLong;
    }

    public void setGeoLong(Float geoLong) {
        this.geoLong = geoLong;
    }

    public Float getGeoLat() {
        return geoLat;
    }

    public void setGeoLat(Float geoLat) {
        this.geoLat = geoLat;
    }
}
