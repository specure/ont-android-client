package at.specure.androidX.data.map_filter.data;

import android.os.Bundle;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

import at.specure.androidX.data.map_filter.view_data.FilterGroup;
import at.specure.androidX.data.map_filter.view_data.FilterItem;

public class MapOverlay {

    public static final String MAP_FILTER_AUTOMATIC = "MAP_FILTER_AUTOMATIC";
    public static final String MAP_FILTER_HEATMAP = "MAP_FILTER_HEATMAP";
    public static final String MAP_FILTER_POINTS = "MAP_FILTER_POINTS";
    public static final String MAP_FILTER_REGIONS = "MAP_FILTER_REGIONS";
    public static final String MAP_FILTER_MUNICIPALITIES = "MAP_FILTER_MUNICIPALITIES";
    public static final String MAP_FILTER_SETTLEMENTS = "MAP_FILTER_SETTLEMENTS";
    public static final String MAP_FILTER_WHITESPOTS = "MAP_FILTER_WHITE_SPOTS";

    public static final HashMap<String, MapOverlayFilterParams> values = new HashMap<String, MapOverlayFilterParams>() {{
        put(MAP_FILTER_AUTOMATIC, new MapOverlayFilterParams("heatmap", "", "overlay_type=heatmap")); // url changes according to zoom level
        put(MAP_FILTER_HEATMAP, new MapOverlayFilterParams("heatmap", "", "overlay_type=heatmap"));
        put(MAP_FILTER_POINTS, new MapOverlayFilterParams("points", "", ""));
        put(MAP_FILTER_REGIONS, new MapOverlayFilterParams("shapes", "shapetype=regions", ""));
        put(MAP_FILTER_MUNICIPALITIES, new MapOverlayFilterParams("shapes", "shapetype=municipalities", ""));
        put(MAP_FILTER_SETTLEMENTS, new MapOverlayFilterParams("shapes", "shapetype=settlements", ""));
        put(MAP_FILTER_WHITESPOTS, new MapOverlayFilterParams("shapes", "shapetype=whitespots", ""));
    }};

    public static final String MAP_FILTER_MAP_OVERLAY_URL = "MAP_FILTER_MAP_OVERLAY_URL";
    public static final String MAP_FILTER_MAP_OVERLAY_ADDITIONAL_PARAMS = "MAP_FILTER_MAP_OVERLAY_ADDITIONAL_PARAMS";
    public static final String MAP_FILTER_MAP_OVERLAY_TYPE = "MAP_FILTER_MAP_OVERLAY_TYPE";


    @SerializedName("default")
    Boolean isDefault;

    @SerializedName("title")
    String title;

    @SerializedName("value")
    String value;

    public boolean isDefault() {
        return isDefault != null && isDefault;
    }

    public String getTitle() {
        return title;
    }

    public String getFilterValue() {
        return value;
    }

//    public HashMap<String, String> getAdditionalParameters() {
//
//        MapOverlayFilterParams mapOverlayFilterParams = values.get(value);
//
//        return new HashMap<String, String>() {
//            {
//                put(MAP_FILTER_MAP_OVERLAY_URL, mapOverlayFilterParams.getMapFilterUrl());
//                put(MAP_FILTER_MAP_OVERLAY_ADDITIONAL_PARAMS, mapOverlayFilterParams.parametersToAdd);
//                put(MAP_FILTER_MAP_OVERLAY_TYPE, value);
//            }
//        };
//    }

    public Bundle getAdditionalParameters() {

        MapOverlayFilterParams mapOverlayFilterParams = values.get(value);
        Bundle bundle = new Bundle();
        bundle.putString(MAP_FILTER_MAP_OVERLAY_URL, mapOverlayFilterParams.getMapFilterUrl());
        bundle.putString(MAP_FILTER_MAP_OVERLAY_ADDITIONAL_PARAMS, mapOverlayFilterParams.parametersToAdd);
        bundle.putString(MAP_FILTER_MAP_OVERLAY_TYPE, value);
        return bundle;
    }


}
