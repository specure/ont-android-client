package at.specure.androidX.data.map_filter.view_data;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import at.specure.android.configs.PreferenceConfig;
import at.specure.androidX.data.map_filter.data.MapCellularType;
import at.specure.androidX.data.map_filter.data.MapFilterCountry;
import at.specure.androidX.data.map_filter.data.MapLayout;
import at.specure.androidX.data.map_filter.data.MapOverlay;
import at.specure.androidX.data.map_filter.data.MapPeriodFilter;
import at.specure.androidX.data.map_filter.data.MapStatistic;
import at.specure.androidX.data.map_filter.data.MapSubType;
import at.specure.androidX.data.operators.MapFilterOperator;

public class FilterItem implements Parcelable {

    boolean isDefault;
    String title;
    String subtitle;
    String filterValue;
    Bundle additionalParameters;
//    HashMap<String, String> additionalParameters;
//    FilterItemGroup filterItemGroup;

    public FilterItem(MapSubType mapSubType) {
        this.title = mapSubType.getTitle();
        this.filterValue = mapSubType.getFilterValue();
//        this.filterItemGroup = group;
        this.isDefault = mapSubType.isDefault();
        // TODO: get from shared preferencies
        // this.selected = mapSubType;
    }

    public FilterItem(MapLayout mapLayout) {
//        this.filterItemGroup = group;
        this.isDefault = mapLayout.getDefault();
        this.title = mapLayout.getTitle();
        this.filterValue = mapLayout.getFilterValue();
        this.additionalParameters = mapLayout.getAdditionalParameters();
    }

    public FilterItem(MapPeriodFilter mapPeriodFilter) {
        this.title = mapPeriodFilter.getTitle();
        this.filterValue = mapPeriodFilter.getFilterValue();
        this.isDefault = mapPeriodFilter.isDefault();
//        this.filterItemGroup = group;
        // TODO: get from shared preferencies
        // this.selected = mapSubType;
    }

    public FilterItem(MapStatistic mapStatistic) {
        this.title = mapStatistic.getTitle();
        this.filterValue = mapStatistic.getFilterValue();
        this.isDefault = mapStatistic.isDefault();
//        this.filterItemGroup = group;
    }

    public FilterItem(MapCellularType mapCellularType) {
        this.title = mapCellularType.getTitle();
        this.filterValue = mapCellularType.getFilterValue();
        this.isDefault = mapCellularType.isDefault();
//        this.filterItemGroup = group;
    }

    public FilterItem(MapOverlay mapOverlay) {
        this.title = mapOverlay.getTitle();
        this.filterValue = mapOverlay.getFilterValue();
        this.isDefault = mapOverlay.isDefault();
        this.additionalParameters = mapOverlay.getAdditionalParameters();
//        this.filterItemGroup = group;
    }

    public FilterItem(MapFilterCountry mapFilterCountry) {
        this.title = mapFilterCountry.getTitle();
        this.filterValue = mapFilterCountry.getFilterValue();
        this.isDefault = mapFilterCountry.isDefault();
    }

    public FilterItem(MapFilterOperator mapFilterOperator) {
        this.title = mapFilterOperator.title == null ? mapFilterOperator.providerName : mapFilterOperator.title;
        this.filterValue = mapFilterOperator.providerName == null ? "" : mapFilterOperator.providerName;
        this.isDefault = mapFilterOperator.isDefault == null ? false : mapFilterOperator.isDefault;
    }

    public String getTitle() {
        return title;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public String getFilterValue() {
        return filterValue;
    }

//    public void saveAdditionalParameters(Context context) {
//        if (additionalParameters != null) {
//            SharedPreferences sp = PreferenceConfig.getPreferenceSharedPreferences(context);
//            SharedPreferences.Editor edit = sp.edit();
//
//            Set<String> strings = additionalParameters.keySet();
//            for (String string : strings) {
//                String s = additionalParameters.get(string);
//                edit.putString(string, s);
//            }
//            edit.commit();
//        }
//    }

    public void saveAdditionalParameters(Context context) {
        if (additionalParameters != null) {
            SharedPreferences sp = PreferenceConfig.getPreferenceSharedPreferences(context);
            SharedPreferences.Editor edit = sp.edit();

            Set<String> strings = additionalParameters.keySet();
            for (String string : strings) {
                String s = additionalParameters.getString(string);
                edit.putString(string, s);
            }
            edit.commit();
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.isDefault ? (byte) 1 : (byte) 0);
        dest.writeString(this.title);
        dest.writeString(this.subtitle);
        dest.writeString(this.filterValue);
        dest.writeBundle(this.additionalParameters);
    }

    protected FilterItem(Parcel in) {
        this.isDefault = in.readByte() != 0;
        this.title = in.readString();
        this.subtitle = in.readString();
        this.filterValue = in.readString();
        this.additionalParameters = in.readBundle(getClass().getClassLoader());
    }

    public static final Creator<FilterItem> CREATOR = new Creator<FilterItem>() {
        @Override
        public FilterItem createFromParcel(Parcel source) {
            return new FilterItem(source);
        }

        @Override
        public FilterItem[] newArray(int size) {
            return new FilterItem[size];
        }
    };
}