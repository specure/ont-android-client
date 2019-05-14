package at.specure.androidX.data.map_filter.data;

import com.google.gson.annotations.SerializedName;

public class MapStatistic {

    @SerializedName("title")
    String title;

    @SerializedName("value")
    Float value;

    @SerializedName("default")
    Boolean isDefault;

    public static final String filterParamName = "statistical_method";

    public boolean isDefault() {
        return isDefault != null && isDefault;
    }

    public String getTitle() {
        return title;
    }

    public String getFilterValue() {
        return value.toString();
    }

}
