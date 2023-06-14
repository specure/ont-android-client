package at.specure.androidX.data.test.testResultRequest;

import com.google.gson.annotations.SerializedName;

public class SpeedDetails {



    @SerializedName("thread")
    Integer threadNumber;

    @SerializedName("time")
    Long timeNsec;

    @SerializedName("bytes")
    Long totalBytes;

}
