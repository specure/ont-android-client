package at.specure.androidX.data.map_filter.data;

import com.google.gson.annotations.SerializedName;

public class MapCellularType {

    @SerializedName("default")
    Boolean isDefault;

    @SerializedName("id")
    Long id;

    @SerializedName("title")
    String title;

    public Boolean isDefault() {
        return isDefault != null && isDefault;
    }

    public String getTitle() {
        return title;
    }

    public String getFilterValue() {
        return id.toString();
    }
}
