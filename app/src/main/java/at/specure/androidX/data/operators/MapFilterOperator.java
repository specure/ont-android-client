package at.specure.androidX.data.operators;

import com.google.gson.annotations.SerializedName;


public class MapFilterOperator {

    @SerializedName("default")
    public Boolean isDefault;

    @SerializedName("title")
    public String title;

    @SerializedName("detail")
    public String detail;

    @SerializedName("provider")
    public String providerNumber;

    @SerializedName("id_provider")
    public String providerName;

    public MapFilterOperator(Boolean isDefault, String title, String detail, String providerNumber) {
        this.isDefault = isDefault == null ? false : isDefault;
        this.title = title;
        this.detail = detail;
        this.providerNumber = providerNumber;
    }

}
