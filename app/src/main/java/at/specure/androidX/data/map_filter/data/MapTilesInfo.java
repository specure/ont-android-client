package at.specure.androidX.data.map_filter.data;

import android.content.Context;

import com.google.gson.annotations.SerializedName;
import com.specure.opennettest.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import at.specure.androidX.data.map_filter.view_data.FilterGroup;
import at.specure.androidX.data.map_filter.view_data.FilterItem;
import at.specure.androidX.data.map_filter.view_data.FilterItemGroup;

public class MapTilesInfo {

    @SerializedName("mapCellularTypes")
    public List<MapCellularType> cellularTypes;

    @SerializedName("mapSubTypes")
    public List<MapSubType> mapSubTypes;

    @SerializedName("mapOverlays")
    public List<MapOverlay> mapOverlays;

    @SerializedName("mapStatistics")
    public List<MapStatistic> mapStatistics;

    @SerializedName("mapTypes")
    public List<MapType> mapTypes;

    @SerializedName("mapLayouts")
    public List<MapLayout> mapLayouts;

    @SerializedName("mapPeriodFilters")
    public List<MapPeriodFilter> mapPeriodFilters;


    private HashMap<Integer, MapSubType> mapSubTypesHash;


    public HashMap<Integer, MapSubType> getMapSubTypes() {
        if (mapSubTypesHash != null) {
            return mapSubTypesHash;
        } else {
            mapSubTypesHash = new HashMap<Integer, MapSubType>();
            for (MapSubType mapSubType : mapSubTypes) {
                mapSubTypesHash.put(mapSubType.index, mapSubType);
            }
            return mapSubTypesHash;
        }
    }

    public FilterGroup getFilterMapPeriods(Context context) {

        ArrayList<FilterItem> filterItems = new ArrayList<>();
        for (MapPeriodFilter mapPeriodFilter : mapPeriodFilters) {
            filterItems.add(new FilterItem(mapPeriodFilter));
        }
        List<FilterItemGroup> filterItemGroups = new ArrayList<>();
        FilterItemGroup filterItemGroup = new FilterItemGroup("", filterItems);
        filterItemGroups.add(filterItemGroup);

        FilterGroup filterGroup = new FilterGroup(MapFilterTypes.MAP_FILTER_TYPE_PERIOD, context.getString(R.string.map_filter_period_title), "", "period", "", filterItemGroups);
        return filterGroup;
    }

    public FilterGroup getFilterMapLayouts(Context context) {

        ArrayList<FilterItem> filterItems = new ArrayList<>();
        for (MapLayout mapLayout : mapLayouts) {
            filterItems.add(new FilterItem(mapLayout));
        }
        List<FilterItemGroup> filterItemGroups = new ArrayList<>();
        FilterItemGroup filterItemGroup = new FilterItemGroup("", filterItems);
        filterItemGroups.add(filterItemGroup);

        FilterGroup filterGroup = new FilterGroup(MapFilterTypes.MAP_FILTER_TYPE_MAP_LAYOUT, context.getString(R.string.map_appearance_header), "", MapFilterTypes.MAP_FILTER_TYPE_MAP_LAYOUT, "", filterItemGroups, FilterGroup.SELECTION_TYPE_EXCLUSIVE,false);
        return filterGroup;
    }

    public FilterGroup getFilterMapStatistics(Context context) {


        ArrayList<FilterItem> filterItems = new ArrayList<>();
        for (MapStatistic mapStatistic : mapStatistics) {
            filterItems.add(new FilterItem(mapStatistic));
        }
        List<FilterItemGroup> filterItemGroups = new ArrayList<>();
        FilterItemGroup filterItemGroup = new FilterItemGroup("", filterItems);
        filterItemGroups.add(filterItemGroup);


        FilterGroup filterGroup = new FilterGroup(MapFilterTypes.MAP_FILTER_TYPE_STATISTIC_TYPE, context.getString(R.string.page_title_statistics), "", "statistical_method", "", filterItemGroups);
        return filterGroup;
    }

    public FilterGroup getFilterMapTechnology(Context context) {

        ArrayList<FilterItem> filterItems = new ArrayList<>();
        for (MapCellularType mapCellularType : cellularTypes) {
            filterItems.add(new FilterItem(mapCellularType));
        }
        List<FilterItemGroup> filterItemGroups = new ArrayList<>();
        FilterItemGroup filterItemGroup = new FilterItemGroup("", FilterItemGroup.SELECTION_TYPE_OR, filterItems);
        filterItemGroups.add(filterItemGroup);

        FilterGroup filterGroup = new FilterGroup(MapFilterTypes.MAP_FILTER_TYPE_TECHNOLOGY, context.getString(R.string.map_filter_technology_title), "", "technology", "", filterItemGroups);
        return filterGroup;
    }

    public FilterGroup getFilterMapOverlays(Context context) {

        ArrayList<FilterItem> filterItems = new ArrayList<>();
        for (MapOverlay mapOverlay : mapOverlays) {
            filterItems.add(new FilterItem(mapOverlay));
        }

        List<FilterItemGroup> filterItemGroups = new ArrayList<>();
        FilterItemGroup filterItemGroup = new FilterItemGroup("", filterItems);
        filterItemGroups.add(filterItemGroup);


        FilterGroup filterGroup = new FilterGroup(MapFilterTypes.MAP_FILTER_TYPE_MAP_OVERLAY, context.getString(R.string.map_overlay_header), "", "overlay_type", "", filterItemGroups);
        return filterGroup;
    }
}
