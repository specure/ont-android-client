package at.specure.androidX.data.map_filter.data;

import com.google.gson.annotations.SerializedName;


public class MapPeriodFilter {

    @SerializedName("default")
    Boolean isDefault;

    @SerializedName("title")
    String title;

    @SerializedName("period")
    Long value;

    public static final String filterParamName = "period";

    public String getTitle() {
        return title;
    }

    public String getFilterValue() {
        return value.toString();
    }

    public Boolean isDefault() {
        return isDefault != null && isDefault;
    }
}
