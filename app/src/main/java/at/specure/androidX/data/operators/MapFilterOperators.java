package at.specure.androidX.data.operators;

import android.content.Context;

import com.google.gson.annotations.SerializedName;
import com.specure.opennettest.R;

import java.util.ArrayList;
import java.util.List;

import at.specure.android.api.jsons.FilterGroup;
import at.specure.android.api.jsons.FilterOperator;
import at.specure.androidX.data.map_filter.data.MapFilterTypes;
import at.specure.androidX.data.map_filter.view_data.FilterItem;
import at.specure.androidX.data.map_filter.view_data.FilterItemGroup;

public class MapFilterOperators {

    @SerializedName("options")
    List<MapFilterOperator> operators;

    @SerializedName("title")
    String title;

    public MapFilterOperators(List<MapFilterOperator> operators, String title) {
        this.operators = operators;
        this.title = title;
    }

    public FilterGroup toFilterGroup() {
        FilterGroup filterGroup = new FilterGroup();
        filterGroup.groupTitle = title;
        filterGroup.filterOperators = new ArrayList<>();

        if (operators != null) {
            for (MapFilterOperator operator : operators) {
                filterGroup.filterOperators.add(new FilterOperator(operator));
            }
        }
        return filterGroup;
    }

    public at.specure.androidX.data.map_filter.view_data.FilterGroup toFilterGroupX(Context context) {
        if ((operators != null) && !operators.isEmpty()) {
            ArrayList<FilterItem> filterItems = new ArrayList<>();
            for (MapFilterOperator operator : operators) {
                FilterItem filterItem = new FilterItem(operator);
                filterItems.add(filterItem);
            }

            ArrayList<FilterItemGroup> filterItemGroups = new ArrayList<>();
            filterItemGroups.add(new FilterItemGroup("", filterItems));

            at.specure.androidX.data.map_filter.view_data.FilterGroup filterGroup = new at.specure.androidX.data.map_filter.view_data.FilterGroup(MapFilterTypes.MAP_FILTER_TYPE_OPERATOR, context.getString(R.string.map_filter_operator_title), "", "mobile_provider_name", "", filterItemGroups);
            return filterGroup;
        }
        return null;
    }
}

