package at.specure.androidX.data.map_filter.data;

import com.google.gson.annotations.SerializedName;

public class MapSubType {

    @SerializedName("default")
    Integer defaultValue;

    @SerializedName("index")
    Integer index;

    @SerializedName("id")
    String filterValue;

    @SerializedName("title")
    String title;

    public boolean isDefault() {
        if ((defaultValue!= null) && (defaultValue == 1)) {
            return true;
        }
        return false;
    }

    public Integer getIndex() {
        return index;
    }

    public String getFilterValue() {
        return filterValue;
    }

    public String getTitle() {
        return title;
    }
}
