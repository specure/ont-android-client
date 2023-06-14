package at.specure.androidX.data.test.testResultRequest;

import com.google.gson.annotations.SerializedName;

import at.specure.util.tools.CpuStat;

public class ExtendedTestStats {

    @SerializedName("cpu_usage")
    CpuStat.CpuUsage cpuUsage;

    @SerializedName("mem_usage")
    MemUsage memUsage;
}
