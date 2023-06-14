package at.specure.androidX.data.test.testResultRequest;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class MemUsage {

    @SerializedName("values")
    List<MemUsageValue> values;
}
