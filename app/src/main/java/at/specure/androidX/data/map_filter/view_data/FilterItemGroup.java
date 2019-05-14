package at.specure.androidX.data.map_filter.view_data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import at.specure.androidX.data.map_filter.data.MapType;
import at.specure.util.tools.expandablecheckrecyclerview.models.MultiCheckExpandableGroup;

public class FilterItemGroup extends MultiCheckExpandableGroup implements Parcelable {

    public static final int SELECTION_TYPE_OR = 2;
    public static final int SELECTION_TYPE_EXCLUSIVE = 1;

    String title;
    Boolean isDefault;
    Boolean isSelected;
    List<FilterItem> items;
    public String filterValue;
    public String filterMergingCharacter = "";
    public int selectionMode = SELECTION_TYPE_EXCLUSIVE;
    private List<FilterItem> selectedItems;
    private List<FilterItem> defaultItems;

    public FilterItemGroup(MapType mapType, List<FilterItem> items) {
        super(mapType.getTitle(), items);
        this.isDefault = mapType.isDefault() != null && mapType.isDefault();
        this.isSelected = isDefault;
        this.title = mapType.getTitle();
        this.filterValue = mapType.getId();
        this.setItems(items);
    }

    public FilterItemGroup(String filterValue, List<FilterItem> items) {
        super("", items);
        this.title = "";
        this.filterValue = filterValue;
        this.isDefault = true;
        this.isSelected = true;
        this.setItems(items);
    }

    public FilterItemGroup(String filterValue, int selectionMode, List<FilterItem> items) {
        super("", items);
        this.title = "";
        this.filterValue = filterValue;
        this.isDefault = true;
        this.isSelected = true;
        this.selectionMode = selectionMode;
        this.setItems(items);
    }


//    public FilterItemGroup(List<MapLayout> mapLayouts) {
//        this.isDefault = true;
//        this.isSelected = true;
//        this.title = "";
//        this.filterValue = "";
//
//        ArrayList<FilterItem> filterItems = new ArrayList<>();
//        for (MapLayout mapLayout : mapLayouts) {
//            filterItems.add(new FilterItem(mapLayout, this));
//        }
//        this.items = filterItems;
//        resetFilter();
//    }

    public void setItems(List<FilterItem> items) {
        this.items = items;
        defaultItems = new ArrayList<>();
        selectedItems = new ArrayList<>();
        int index = 0;
        for (FilterItem item : items) {
            if (item.isDefault()) {
                defaultItems.add(item);
                selectedItems.add(item);
                checkChild(index);
            }
            index++;
        }
    }


    /*public FilterItemGroup(List<MapPeriodFilter> mapPeriodFilters) {
        this.isDefault = true;
        this.isSelected = true;
        this.title = "";
        this.filterValue = "";

        ArrayList<FilterItem> filterItems = new ArrayList<>();
        for (MapPeriodFilter mapPeriodFilter : mapPeriodFilters) {
            filterItems.add(new FilterItem(mapPeriodFilter, this));
        }
        this.items = filterItems;
        resetFilter();
    }*/


    public List<FilterItem> getItems() {
        return items;
    }

    public String getTitle() {
        return title;
    }

    public boolean isSelected() {

        if (this.isSelected) {
            return selectedItems != null && !selectedItems.isEmpty();
        } else {
            return false;
        }
    }

    public List<FilterItem> getSelected() {
        return selectedItems;
    }

    public void resetFilter() {
        this.isSelected = false;
        if (this.isDefault) {
            this.isSelected = true;
        }

        if (selectedItems != null) {
            selectedItems.clear();
        } else {
            selectedItems = new ArrayList<>();
            selectedItems.addAll(defaultItems);
        }
    }

    public void setSelected(FilterItem selectedItem, FilterItemGroup itemGroup, FilterGroup group, boolean selected, int index) {

        if (group.selectionType == SELECTION_TYPE_EXCLUSIVE) {
            for (FilterItemGroup filterItemGroup : group.filterItemGroups) {
                if (filterItemGroup != itemGroup) {
                    filterItemGroup.isSelected = false;
                    filterItemGroup.selectedItems = null;
                    filterItemGroup.clearSelections();
                }
            }
        }

        itemGroup.isSelected = true;
        if (selectedItems == null) {
            selectedItems = new ArrayList<>();
        }

        if (selectionMode == SELECTION_TYPE_EXCLUSIVE) {
            selectedItems.clear();
            selectedItems.add(selectedItem);
            clearSelections();
            checkChild(index);
        } else {
            //selection mode OR
            FilterItem itemToRemove = null;
            for (FilterItem item : selectedItems) {
                if (item.filterValue.equalsIgnoreCase(selectedItem.filterValue)) {
                    itemToRemove = item;
                }
            }
            if (itemToRemove != null) {
                selectedItems.remove(itemToRemove);
                unCheckChild(index);
            } else {
                selectedItems.add(selectedItem);
                checkChild(index);
            }
        }
    }


    public void unselectAll() {
        selectedItems = null;
        clearSelections();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void onChildClicked(int childIndex, boolean checked) {

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.title);
        dest.writeValue(this.isDefault);
        dest.writeValue(this.isSelected);
        dest.writeTypedList(this.items);
        dest.writeString(this.filterValue);
        dest.writeString(this.filterMergingCharacter);
        dest.writeInt(this.selectionMode);
        dest.writeTypedList(this.selectedItems);
        dest.writeTypedList(this.defaultItems);
    }

    protected FilterItemGroup(Parcel in) {
        super(in);
        this.title = in.readString();
        this.isDefault = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.isSelected = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.items = in.createTypedArrayList(FilterItem.CREATOR);
        this.filterValue = in.readString();
        this.filterMergingCharacter = in.readString();
        this.selectionMode = in.readInt();
        this.selectedItems = in.createTypedArrayList(FilterItem.CREATOR);
        this.defaultItems = in.createTypedArrayList(FilterItem.CREATOR);
    }

    public static final Creator<FilterItemGroup> CREATOR = new Creator<FilterItemGroup>() {
        @Override
        public FilterItemGroup createFromParcel(Parcel source) {
            return new FilterItemGroup(source);
        }

        @Override
        public FilterItemGroup[] newArray(int size) {
            return new FilterItemGroup[size];
        }
    };
}