package at.specure.androidX.data.map_filter.data;

import android.content.Context;

import com.google.gson.annotations.SerializedName;
import com.specure.opennettest.R;

import java.util.List;

import at.specure.androidX.data.map_filter.view_data.FilterItem;

public class MapType {

//    public static final int[] subtypesTitles = new int[]{
//        R.string.test_bottom_test_status_down,
//        R.string.test_bottom_test_status_up,
//        R.string.test_ping,
//        R.string.test_signal_strength,
//        R.string.test_signal_strength};
//
//    public static final String[] subtypesFilterValues = new String[]{
//            "download",
//            "upload",
//            "ping",
//            "signal",
//            "signal"};

    @SerializedName("mapListOptions")
    Integer mapListOptionId;

    @SerializedName("mapCellularTypeOptions")
    Boolean isCellularTypesOptions;

    @SerializedName("default")
    Boolean isDefault;

    @SerializedName("mapSubTypeOptions")
    List<Integer> mapSubtypeOptionsIds;

    @SerializedName("id")
    String id;

    @SerializedName("title")
    String title;

    public Integer getMapListOptionId() {
        return mapListOptionId;
    }

    public void setMapListOptionId(Integer mapListOptionId) {
        this.mapListOptionId = mapListOptionId;
    }

    public Boolean getCellularTypesOptions() {
        return isCellularTypesOptions;
    }

    public void setCellularTypesOptions(Boolean cellularTypesOptions) {
        isCellularTypesOptions = cellularTypesOptions;
    }

    public Boolean isDefault() {
        return isDefault;
    }

    public void setDefault(Boolean aDefault) {
        isDefault = aDefault;
    }

    public List<Integer> getMapSubtypeOptionsIds() {
        return mapSubtypeOptionsIds;
    }

    public void setMapSubtypeOptionsIds(List<Integer> mapSubtypeOptionsIds) {
        this.mapSubtypeOptionsIds = mapSubtypeOptionsIds;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
