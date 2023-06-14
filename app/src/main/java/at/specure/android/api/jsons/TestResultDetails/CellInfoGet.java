package at.specure.android.api.jsons.TestResultDetails;

import com.google.gson.annotations.SerializedName;

public class CellInfoGet {

    /**
     * Download E/U/ARFCN
     */
    @SerializedName("arfcn_number")
    public Integer arfcnNumber;

    @SerializedName("tstamp")
    Long timestamp;

    /**
     * EARFCN / UARFCN / ARFCN / NRARFCN
     */
    @SerializedName("type")
    public String arfcnType;

    @SerializedName("band")
    public Integer band;

    @SerializedName("band_name")
    public String bandName;

    @SerializedName("frequency_download")
    public Float frequencyDownload;

    @SerializedName("frequency_upload")
    public Float frequencyUpload;

    @SerializedName("bandwidth")
    public Float bandwidth;

    public CellInfoGet(long timestamp, String type, Integer number) {
        this.timestamp = timestamp;
        this.arfcnType = type;
        this.arfcnNumber = number;
    }

}
