package at.specure.android.api.jsons.TestResultDetails;

import com.google.gson.annotations.SerializedName;

public class CellInfoGet {

    @SerializedName("arfcn_number")
    Integer arfcnNumber;

    @SerializedName("tstamp")
    Long timestamp;

    /**
     * EARFCN / UARFCN / ARFCN
     */
    @SerializedName("type")
    String arfcnType;

    @SerializedName("band")
    Integer band;

    @SerializedName("band_name")
    String bandName;

    @SerializedName("frequency_download")
    Float frequencyDownload;

    @SerializedName("frequency_upload")
    Float frequencyUpload;

    @SerializedName("bandwidth")
    Float bandwidth;

}
