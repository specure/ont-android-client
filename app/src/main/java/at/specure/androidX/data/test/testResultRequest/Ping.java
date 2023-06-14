package at.specure.androidX.data.test.testResultRequest;

import com.google.gson.annotations.SerializedName;

public class Ping {

    @SerializedName("value")
    public Long timeDiffClient;

    @SerializedName("value_server")
    public Long timeDiffServer;

    @SerializedName("time_ns")
    public Long pingTimeNs;

    public Ping(long diffClient, long diffServer, long pingTimeNs) {
        this.timeDiffClient = diffClient;
        this.timeDiffServer = diffServer;
        this.pingTimeNs = pingTimeNs;
    }
}
