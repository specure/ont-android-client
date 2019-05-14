package at.specure.android.api.jsons.TestResultDetailOpenData;

import com.google.gson.annotations.SerializedName;

public class GraphSpeedItem {

    @SerializedName("time_elapsed")
    Long timeElapsed;

    @SerializedName("bytes_total")
    Long bytesTotalTransfered;

    public Long getTimeElapsed() {
        return timeElapsed;
    }

    public void setTimeElapsed(Long timeElapsed) {
        this.timeElapsed = timeElapsed;
    }

    public Long getBytesTotalTransfered() {
        return bytesTotalTransfered;
    }

    public void setBytesTotalTransfered(Long bytesTotalTransfered) {
        this.bytesTotalTransfered = bytesTotalTransfered;
    }
}
