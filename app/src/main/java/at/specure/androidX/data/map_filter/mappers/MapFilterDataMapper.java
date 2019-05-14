package at.specure.androidX.data.map_filter.mappers;

import android.content.Context;

import androidx.annotation.NonNull;

import com.specure.opennettest.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import at.specure.androidX.data.map_filter.data.MapFilterCountry;
import at.specure.androidX.data.map_filter.data.MapFilterTypes;
import at.specure.androidX.data.map_filter.data.MapSubType;
import at.specure.androidX.data.map_filter.data.MapTilesInfo;
import at.specure.androidX.data.map_filter.data.MapType;
import at.specure.androidX.data.map_filter.view_data.FilterGroup;
import at.specure.androidX.data.map_filter.view_data.FilterItem;
import at.specure.androidX.data.map_filter.view_data.FilterItemGroup;

import static at.specure.androidX.data.map_filter.data.CountryList.getCountryList;

public class MapFilterDataMapper {


    public static List<FilterGroup> mapToAdapter(MapTilesInfo tilesInfo, FilterGroup additionalGroup, Context context) {
        ArrayList<FilterGroup> filterGroups = new ArrayList<>();

        if (tilesInfo != null) {

            FilterGroup filterGroup = mapToFilterGroup(tilesInfo.mapTypes, tilesInfo.getMapSubTypes(), context);
            filterGroups.add(filterGroup);

            filterGroups.add(tilesInfo.getFilterMapLayouts(context));

            filterGroups.add(tilesInfo.getFilterMapPeriods(context));

            filterGroups.add(tilesInfo.getFilterMapStatistics(context));

            filterGroups.add(tilesInfo.getFilterMapTechnology(context));

            filterGroups.add(tilesInfo.getFilterMapOverlays(context));

            ArrayList<FilterItem> filterItems = new ArrayList<>();
            ArrayList<MapFilterCountry> countryList = getCountryList(context);
            for (MapFilterCountry mapFilterCountry : countryList) {
                filterItems.add(new FilterItem(mapFilterCountry));
            }
            ArrayList<FilterItemGroup> filterItemGroups = new ArrayList<>();
            FilterItemGroup filterItemGroup = new FilterItemGroup("", filterItems);
            filterItemGroups.add(filterItemGroup);

            filterGroups.add(new FilterGroup(MapFilterTypes.MAP_FILTER_TYPE_COUNTRY, context.getString(R.string.operators_country), "", "country", "", filterItemGroups));

            if (additionalGroup != null) {
                filterGroups.add(additionalGroup);
            }

            MapFilterSaver.loadMapFilter(filterGroups, context.getApplicationContext());
            for (FilterGroup group : filterGroups) {
                MapFilterSaver.saveGroupSettings(group, context.getApplicationContext());
            }
        }

        return filterGroups;
    }

//    public static FilterGroup mapToFilterGroup(List<MapLayout> mapLayouts, Context context) {
//        FilterGroup filterGroup = new FilterGroup((List<Object>) mapLayouts, context);
//        return filterGroup;
//    }
//
//    public static FilterGroup mapToFilterGroup(List<MapPeriodFilter> mapPeriodFilters, Context context) {
//        FilterGroup filterGroup = new FilterGroup(mapPeriodFilters, context);
//        return filterGroup;
//    }

    public static FilterGroup mapToFilterGroup(@NonNull List<MapType> mapTypes, HashMap<Integer, MapSubType> subTypes, Context context) {


        FilterGroup filterGroup = new FilterGroup(mapTypes, subTypes, context);
        return filterGroup;
    }

}
