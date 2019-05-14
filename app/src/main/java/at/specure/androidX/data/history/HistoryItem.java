package at.specure.androidX.data.history;

import com.google.gson.annotations.SerializedName;

import java.text.DateFormat;
import java.util.Date;

import at.specure.android.util.Helperfunctions;
import at.specure.androidX.data.jitter.JitterPacketLossHistory;

import static at.specure.androidX.data.DataUtil.parseNullValue;

public class HistoryItem {

    @SerializedName("test_uuid")
    String testUUID;

    @SerializedName("timezone")
    String timezoneHumanReadable; // e.g.Europe/Bratislava

    @SerializedName("ping")
    String ping;

    /**
     * speed as string without units
     */
    @SerializedName("speed_upload")
    String speedUpload;

    /**
     * speed as string without units
     */
    @SerializedName("speed_download")
    String speedDownload;


    /**
     * number
     */
    @SerializedName("speed_download_classification")
    Integer speedDownClass;

    /**
     * number
     */
    @SerializedName("speed_upload_classification")
    Integer speedUpClass;

    /**
     * number
     */
    @SerializedName("ping_shortest_classification")
    Integer pingShortestClass;

    /**
     * number
     */
    @SerializedName("ping_classification")
    Integer pingClass;


    @SerializedName("jpl")
    JitterPacketLossHistory jitterAndPacketLoss;

    /**
     * SSID of network or in mobile it is operator name
     */
    @SerializedName("network_name")
    String networkName;

    /**
     * in ms, without unit
     */
    @SerializedName("ping_shortest")
    String pingShortest;

    /**
     * operator (service provider)
     */
    @SerializedName("operator")
    String operator;

    @SerializedName("model")
    String model;

    @SerializedName("time")
    Long timestamp;

    @SerializedName("timeString")
    String timeString;

    public HistoryItem() {
    }

    public HistoryItem(String testUUID, String timezoneHumanReadable, String ping, String speedUpload, String speedDownload, Integer speedDownClass, Integer speedUpClass, Integer pingShortestClass, Integer pingClass, JitterPacketLossHistory jitterAndPacketLoss, String networkName, String pingShortest, String operator, String model, Long timestamp, String timeString, String networkType, String qosResultPercentage) {
        this.testUUID = testUUID;
        this.timezoneHumanReadable = timezoneHumanReadable;
        this.ping = ping;
        this.speedUpload = speedUpload;
        this.speedDownload = speedDownload;
        this.speedDownClass = speedDownClass;
        this.speedUpClass = speedUpClass;
        this.pingShortestClass = pingShortestClass;
        this.pingClass = pingClass;
        this.jitterAndPacketLoss = jitterAndPacketLoss;
        this.networkName = networkName;
        this.pingShortest = pingShortest;
        this.operator = operator;
        this.model = model;
        this.timestamp = timestamp;
        this.timeString = timeString;
        this.networkType = networkType;
        this.qosResultPercentage = qosResultPercentage;
    }

    /**
     * "WLAN", "4G", ...
     */
    @SerializedName("network_type")
    String networkType;

    /**
     * QoS result percentage without units, e.g. "99", In case of no value -> "-"
     */
    @SerializedName("qos_result")
    String qosResultPercentage;

    public String getTestUUID() {
        return testUUID;
    }

    public String getTimezoneHumanReadable() {
        return parseNullValue(timezoneHumanReadable);
    }

    public String getPing() {
        return parseNullValue(ping);
    }

    public String getSpeedUpload() {
        return parseNullValue(speedUpload);
    }

    public String getSpeedDownload() {
        return parseNullValue(speedDownload);
    }

    public Integer getSpeedDownClass() {
        return speedDownClass;
    }

    public Integer getSpeedUpClass() {
        return speedUpClass;
    }

    public Integer getPingShortestClass() {
        return pingShortestClass;
    }

    public Integer getPingClass() {
        return pingClass;
    }

    public JitterPacketLossHistory getJitterAndPacketLoss() {
        return jitterAndPacketLoss;
    }

    public String getNetworkName() {
        return parseNullValue(networkName);
    }

    public String getPingShortest() {
        return parseNullValue(pingShortest);
    }

    public String getOperator() {
        return parseNullValue(operator);
    }

    public String getModel() {
        return parseNullValue(model);
    }

    public Long getTimestamp() {
        return timestamp == null ? 0L : timestamp;
    }

    public String getDate() {
        final Date tmpDate = new Date();
        final DateFormat dateFormat = Helperfunctions.getDateFormat(false);

        final String timeString = Helperfunctions.formatTimestampWithTimezone(tmpDate,
                dateFormat, getTimestamp(), getTimezoneHumanReadable());
        return timeString;
    }

    public String getTimeString() {
        return parseNullValue(timeString);
    }

    public String getNetworkType() {
        return parseNullValue(networkType);
    }

    public String getQosResultPercentage() {
        return parseNullValue(qosResultPercentage);
    }
}
