package at.specure.androidX.data.jitter;

import com.google.gson.annotations.SerializedName;

import static at.specure.androidX.data.DataUtil.parseNullValue;

public class JitterPacketLossHistory {

    /**
     * number
     */
    @SerializedName("classification_jitter")
    Integer jitterClass;

    /**
     * number
     */
    @SerializedName("classification_packet_loss")
    Integer packetLossClass;

    /**
     * string without unit in ms
     */
    @SerializedName("voip_result_jitter")
    String jitterResult;

    /**
     * string without unit in %
     */
    @SerializedName("voip_result_packet_loss")
    String packetLossResult;

    public Integer getJitterClass() {
        return jitterClass;
    }

    public Integer getPacketLossClass() {
        return packetLossClass;
    }

    public String getJitterResult() {
        return parseNullValue(jitterResult);
    }

    public String getPacketLossResult() {
        return parseNullValue(packetLossResult);
    }
}
