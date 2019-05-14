package at.specure.androidX.data.map_filter.view_data;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;

import com.specure.opennettest.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import at.specure.android.configs.PreferenceConfig;
import at.specure.androidX.data.map_filter.data.MapFilterTypes;
import at.specure.androidX.data.map_filter.data.MapSubType;
import at.specure.androidX.data.map_filter.data.MapType;

public class FilterGroup implements Parcelable {

    public static final int SELECTION_TYPE_OR = 2;
    public static final int SELECTION_TYPE_EXCLUSIVE = 1;

    List<FilterItemGroup> filterItemGroups;
    String id;
    String title;
    String subtitle;
    String filterParameterName;
    String optionsMergingCharacter;
    boolean addToFilter = true;
    public int selectionType = SELECTION_TYPE_EXCLUSIVE;

    public FilterGroup(List<MapType> mapTypes, HashMap<Integer, MapSubType> mapSubTypes, Context context) {
        this.id = MapFilterTypes.MAP_FILTER_TYPE_TYPE;
        this.title = context.getString(R.string.map_filter_type_title);
        this.subtitle = "";
        this.filterParameterName = "map_options";
        this.optionsMergingCharacter = "/";
        this.filterItemGroups = new ArrayList<>();

        for (MapType mapType : mapTypes) {
            List<Integer> mapSubtypeOptionsIds = mapType.getMapSubtypeOptionsIds();

            ArrayList<FilterItem> filterItems = new ArrayList<>();
            for (Integer mapSubTypeId : mapSubtypeOptionsIds) {
                filterItems.add(new FilterItem(mapSubTypes.get(mapSubTypeId)));
            }

            FilterItemGroup filterItemGroup = new FilterItemGroup(mapType, filterItems);
            this.filterItemGroups.add(filterItemGroup);
        }
        setFilterItemGroups(filterItemGroups);
    }

    public FilterGroup(String id, String title, String subtitle, String filterParameterName, String optionsMergingCharacter, List<FilterItemGroup> itemGroups) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.filterParameterName = filterParameterName;
        this.optionsMergingCharacter = optionsMergingCharacter;
        this.filterItemGroups = itemGroups;
    }

    public FilterGroup(String id, String title, String subtitle, String filterParameterName, String optionsMergingCharacter, List<FilterItemGroup> itemGroups, int selectionType, boolean addToFilter) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.filterParameterName = filterParameterName;
        this.optionsMergingCharacter = optionsMergingCharacter;
        this.filterItemGroups = itemGroups;
        this.selectionType = selectionType;
        this.addToFilter = addToFilter;
    }

    public FilterGroup(String id, String title, String subtitle, String filterParameterName, String optionsMergingCharacter, List<FilterItemGroup> itemGroups, int selectionType) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.filterParameterName = filterParameterName;
        this.optionsMergingCharacter = optionsMergingCharacter;
        this.filterItemGroups = itemGroups;
        this.selectionType = selectionType;
    }

//    public FilterGroup(List<MapLayout> mapLayouts, Context context) {
//        this.id = MapFilterTypes.MAP_FILTER_TYPE_MAP_LAYOUT;
//        this.title = context.getString(R.string.map_appearance_header);
//        this.subtitle = "";
//        this.filterParameterName = "";
//        this.optionsMergingCharacter = "";
//        this.filterItemGroups = new ArrayList<>();
//        FilterItemGroup filterItemGroup = new FilterItemGroup(mapLayouts);
//        this.filterItemGroups.add(filterItemGroup);
//    }

   /* public FilterGroup(List<MapPeriodFilter> mapPeriodFilters, Context context) {
        this.id = MapFilterTypes.MAP_FILTER_TYPE_PERIOD;
        this.title = context.getString(R.string.map_filter_period_title);
        this.subtitle = "";
        this.filterParameterName = "period";
        this.optionsMergingCharacter = "";
        this.filterItemGroups = new ArrayList<>();
        FilterItemGroup filterItemGroup = new FilterItemGroup(mapPeriodFilters);
        this.filterItemGroups.add(filterItemGroup);
    }*/

    public String getTitle() {
        return title;
    }

    public List<FilterItemGroup> getFilterItemGroups() {
        return filterItemGroups;
    }

    public void setFilterItemGroups(List<FilterItemGroup> filterItemGroups) {
        this.filterItemGroups = filterItemGroups;
        if (selectionType == SELECTION_TYPE_EXCLUSIVE) {
            for (FilterItemGroup filterItemGroup : filterItemGroups) {
                if (!filterItemGroup.isSelected()) {
                    filterItemGroup.clearSelections();
                }
            }
        }
    }

    public String getSelectedValuesToShow() {
        String valueToShow = "";
        List<FilterItemGroup> selectedGroups = getSelectedOptions();

        StringBuilder stringBuilder = new StringBuilder();

        for (FilterItemGroup selectedItemGroup : selectedGroups) {
            addToBuilder(stringBuilder, selectedItemGroup);
        }

        valueToShow = stringBuilder.toString();
        return valueToShow;
    }

    //TODO: no  support of OR selection type for groups
    public String getStringToFilterURLasParam() {
        String valueToShow = "";
        List<FilterItemGroup> selectedOptions = getSelectedOptions();

        StringBuilder stringBuilder = new StringBuilder();

        ArrayList<String> values = new ArrayList<>();
        for (FilterItemGroup selectedOption : selectedOptions) {
            if (selectedOption.isSelected()) {
                addToURLParamToBuilder(stringBuilder, selectedOption);
            }
        }
        valueToShow = stringBuilder.toString();
        return valueToShow;
    }

    List<FilterItemGroup> getSelectedOptions() {
        List<FilterItemGroup> selectedFilterItemGroups = new ArrayList<>();

        for (FilterItemGroup filterItemGroup : this.filterItemGroups) {

            boolean selected = filterItemGroup.isSelected();
            if (selected) {
                selectedFilterItemGroups.add(filterItemGroup);
                /*List<FilterItem> selected1 = filterItemGroup.getSelected();
                for (FilterItem filterItem : selected1) {
                    selectedFilterItems.add(filterItem);
                }*/
            }
        }
        return selectedFilterItemGroups;
    }

    void addToBuilder(StringBuilder builder, FilterItemGroup selectedItemGroup) {

        if ((selectedItemGroup.title != null) && (!selectedItemGroup.title.isEmpty())) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(selectedItemGroup.title);
        }

        List<FilterItem> selected = selectedItemGroup.getSelected();

        for (FilterItem filterItem : selected) {
            if ((filterItem.title != null) && (!filterItem.title.isEmpty())) {
                if (builder.length() > 0) {
                    builder.append(", ");
                }
                builder.append(filterItem.title);
            }
        }

    }


    public StringBuilder addToURLParamToBuilder(StringBuilder builder, FilterItemGroup filterItemGroup) {

        if (builder.length() > 0) {
            builder.append("&");
        }

        builder.append(filterParameterName);
        builder.append("=");
        if ((filterItemGroup.filterValue != null) && (!filterItemGroup.filterValue.isEmpty())) {
            builder.append(filterItemGroup.filterValue);
            builder.append(optionsMergingCharacter);
        }
        List<FilterItem> selected = filterItemGroup.getSelected();
        if (filterItemGroup.selectionMode == SELECTION_TYPE_EXCLUSIVE) {
            for (FilterItem filterItem : selected) {
                    builder.append(filterItem.filterValue);
                    return builder;
            }
        } else {
            int i = 0;
            for (FilterItem filterItem : selected) {
                    if (i > 0) {
                        builder.append(filterItemGroup.filterMergingCharacter);
                    }
                    builder.append(filterItem.filterValue);
                    i++;
            }
            return builder;
        }
        return builder;

       /* if ((selectedOption.title != null) && (!selectedOption.title.isEmpty())) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(selectedOption.title);
        }

        if ((value != null) && (!value.isEmpty())) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(filterParameterName);
            builder.append("=");
            builder.append(value.get(0));

            for (int i = 1; i<value.size();i++) {
                builder.append(optionsMergingCharacter);
                builder.append(value.get(i));
            }
        }*/
    }

    public void resetFilter() {
        for (FilterItemGroup filterItemGroup : filterItemGroups) {
            filterItemGroup.resetFilter();
        }
    }

    public String getId() {
        return id;
    }

    public FilterGroup(FilterGroup group) {
        this.selectionType = group.selectionType;
        this.filterParameterName = group.filterParameterName;
        this.subtitle = group.subtitle;
        this.title = group.title;
        this.id = group.id;
        this.optionsMergingCharacter = group.optionsMergingCharacter;
        this.filterItemGroups = group.filterItemGroups;
    }

    public void saveAdditionalParameters(Context context) {
        SharedPreferences sp = PreferenceConfig.getPreferenceSharedPreferences(context);
        SharedPreferences.Editor edit = sp.edit();
        List<FilterItemGroup> selectedOptions = getSelectedOptions();
        if (selectedOptions != null) {
            for (FilterItemGroup selectedOption : selectedOptions) {
                List<FilterItem> selected = selectedOption.getSelected();
                if (selected != null) {
                    for (FilterItem filterItem : selected) {
                        if (filterItem != null) {
                            filterItem.saveAdditionalParameters(context);
                        }
                    }
                }
            }
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(this.filterItemGroups);
        dest.writeString(this.id);
        dest.writeString(this.title);
        dest.writeString(this.subtitle);
        dest.writeString(this.filterParameterName);
        dest.writeString(this.optionsMergingCharacter);
        dest.writeByte(this.addToFilter ? (byte) 1 : (byte) 0);
        dest.writeInt(this.selectionType);
    }

    protected FilterGroup(Parcel in) {
        this.filterItemGroups = in.createTypedArrayList(FilterItemGroup.CREATOR);
        this.id = in.readString();
        this.title = in.readString();
        this.subtitle = in.readString();
        this.filterParameterName = in.readString();
        this.optionsMergingCharacter = in.readString();
        this.addToFilter = in.readByte() != 0;
        this.selectionType = in.readInt();
    }

    public static final Creator<FilterGroup> CREATOR = new Creator<FilterGroup>() {
        @Override
        public FilterGroup createFromParcel(Parcel source) {
            return new FilterGroup(source);
        }

        @Override
        public FilterGroup[] newArray(int size) {
            return new FilterGroup[size];
        }
    };

    public String getFilterValue() {
        return filterParameterName;
    }
}
