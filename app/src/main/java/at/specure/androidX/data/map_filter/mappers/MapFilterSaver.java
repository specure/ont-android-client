package at.specure.androidX.data.map_filter.mappers;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import java.util.List;

import at.specure.android.configs.ConfigHelper;
import at.specure.android.configs.PreferenceConfig;
import at.specure.androidX.data.map_filter.data.MapFilterTypes;
import at.specure.androidX.data.map_filter.data.MapLayout;
import at.specure.androidX.data.map_filter.data.MapOverlay;
import at.specure.androidX.data.map_filter.view_data.FilterGroup;
import at.specure.androidX.data.map_filter.view_data.FilterItem;
import at.specure.androidX.data.map_filter.view_data.FilterItemGroup;
import timber.log.Timber;

public class MapFilterSaver {

    private static final String MAP_FILTER_URL_PARAMS = "MAP_FILTER_URL_PARAMS";

    public static void loadMapFilter(List<FilterGroup> groups, Context context) {

        SharedPreferences sp = PreferenceConfig.getPreferenceSharedPreferences(context);

        for (FilterGroup group : groups) {
            String id = group.getId();

            List<FilterItemGroup> filterItemGroups = group.getFilterItemGroups();
            String string = sp.getString(id, null);
            if (string != null)
                for (FilterItemGroup filterItemGroup : filterItemGroups) {
                    if (filterItemGroup.selectionMode == FilterItemGroup.SELECTION_TYPE_EXCLUSIVE) {
                        if (filterItemGroup.filterValue != null && !filterItemGroup.filterValue.isEmpty()) {
                            String[] split = string.split("=");
                            if (split.length > 1) {
                                if (split[1].startsWith(filterItemGroup.filterValue)) {
                                    int index = 0;
                                    for (FilterItem item : filterItemGroup.getItems()) {
                                        if (split[1].contains(item.getFilterValue())) {
                                            filterItemGroup.setSelected(item, filterItemGroup, group, true, index);
                                        }
                                        index++;
                                    }
                                }
                            }
                        } else {
                            int index = 0;
                            String[] split = string.split("=");
                            if (split.length > 1) {
                                for (FilterItem item : filterItemGroup.getItems()) {
                                    if (split[1].equalsIgnoreCase(item.getFilterValue())) {
                                        filterItemGroup.setSelected(item, filterItemGroup, group, true, index);
                                    }
                                    index++;
                                }
                            }
                        }
                    } else {
                        filterItemGroup.unselectAll();
                        if (filterItemGroup.filterValue != null && !filterItemGroup.filterValue.isEmpty()) {
                            String[] split = string.split("=");
                            if (split.length > 1) {
                                if (split[1].startsWith(filterItemGroup.filterValue)) {
                                    int index = 0;
                                    for (FilterItem item : filterItemGroup.getItems()) {
                                        if (split[1].contains(item.getFilterValue())) {
                                            filterItemGroup.setSelected(item, filterItemGroup, group, true, index);
                                        }
                                        index++;
                                    }
                                }
                            }
                        } else {
                            int index = 0;
                            for (FilterItem item : filterItemGroup.getItems()) {
                                if (string.contains(item.getFilterValue())) {
                                    filterItemGroup.setSelected(item, filterItemGroup, group, true, index);
                                }
                                index++;
                            }
                        }
                    }
                }

        }
    }

    public static boolean saveGroupSettings(FilterGroup group, Context context) {
        SharedPreferences sp = PreferenceConfig.getPreferenceSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        String id = group.getId();
        String stringToFilterURLasParam = group.getStringToFilterURLasParam();

        editor.remove(id).putString(id, stringToFilterURLasParam);

        // remove operator filter because country has changed probably
        if (group.getId().equalsIgnoreCase(MapFilterTypes.MAP_FILTER_TYPE_COUNTRY)) {
            editor.remove(MapFilterTypes.MAP_FILTER_TYPE_OPERATOR);
        }

        // save additional parameters from map_overlay and map_layout
        group.saveAdditionalParameters(context);

        editor.commit();

        SharedPreferences.Editor edit = sp.edit();
        // create link from selected filters
        if (!(id.equalsIgnoreCase(MapFilterTypes.MAP_FILTER_TYPE_MAP_LAYOUT)
                || id.equalsIgnoreCase(MapFilterTypes.MAP_FILTER_TYPE_MAP_OVERLAY))) {
            String stringFilter = sp.getString(MAP_FILTER_URL_PARAMS, "");
            edit.remove(MAP_FILTER_URL_PARAMS);
            String[] split = stringFilter.split("&");
            boolean found = false;

            StringBuilder builder = new StringBuilder();
            for (String s : split) {
                if (s.contains(group.getFilterValue())) {
                    s = stringToFilterURLasParam;
                    found = true;
                }

                if (builder.length() != 0) {
                    builder.append("&");
                }
                builder.append(s);
            }

            if (!found) {
                if (builder.length() != 0) {
                    builder.append("&");
                }
                builder.append(stringToFilterURLasParam);
            }

            edit.putString(MAP_FILTER_URL_PARAMS, builder.toString());
        }

        return edit.commit();
    }

//    public static String getActiveMapFilter(Context context) {
//        SharedPreferences sp = PreferenceConfig.getPreferenceSharedPreferences(context);
//        String stringFilter = sp.getString(MAP_FILTER_URL, "");
//        return stringFilter;
//    }

    /**
     * Returns all params needed for map filter url (todo: add uuid of client to highlight)
     *
     * @param context
     * @return
     */
    public static String getActiveMapFilterParams(Context context) {
        SharedPreferences sp = PreferenceConfig.getPreferenceSharedPreferences(context);
        StringBuilder stringFilter = new StringBuilder(sp.getString(MAP_FILTER_URL_PARAMS, ""));
        String activeMapFilterOverlayParams = getActiveMapFilterOverlayParams(context);
        String clientUUID = ConfigHelper.getUUID(context);

        if (stringFilter.length() > 0) {
            stringFilter.append("&");
        }
        if (!activeMapFilterOverlayParams.isEmpty()) {
            stringFilter.append(activeMapFilterOverlayParams);
        }
        if (stringFilter.length() > 0) {
            stringFilter.append("&");
        }
        if (!clientUUID.isEmpty()) {
            stringFilter.append("highlight=").append(clientUUID);
        }

        if (!stringFilter.toString().contains("mobile/")) {
            String[] split = stringFilter.toString().split("&");
            boolean first = true;
            stringFilter = new StringBuilder();
            for (String s : split) {
                if (!s.startsWith("technology")) {
                    if (!first) {
                        stringFilter.append("&");
                    }
                    stringFilter.append(s);
                    first = false;
                }
            }

        }

        return stringFilter.toString();
    }

    public static String getActiveMapFilterOverlayUrl(Context context) {
        SharedPreferences sp = PreferenceConfig.getPreferenceSharedPreferences(context);
        String stringFilter = sp.getString(MapOverlay.MAP_FILTER_MAP_OVERLAY_URL, "");
        return stringFilter;
    }

    public static String getActiveMapFilterOverlayParams(Context context) {
        SharedPreferences sp = PreferenceConfig.getPreferenceSharedPreferences(context);
        String stringFilter = sp.getString(MapOverlay.MAP_FILTER_MAP_OVERLAY_ADDITIONAL_PARAMS, "");
        return stringFilter;
    }

    public static String getActiveMapFilterOverlayType(Context context) {
        SharedPreferences sp = PreferenceConfig.getPreferenceSharedPreferences(context);
        String stringFilter = sp.getString(MapOverlay.MAP_FILTER_MAP_OVERLAY_TYPE, "");
        return stringFilter;
    }

    @NonNull
    /**
     * Used for getting tiles
     */
    public static String getFilterOperatorType(Context context) {
        SharedPreferences sp = PreferenceConfig.getPreferenceSharedPreferences(context);
        String stringFilter = sp.getString(MAP_FILTER_URL_PARAMS, "");

        String[] split = stringFilter.split("&");
        String map_options = null;
        StringBuilder builder = new StringBuilder();
        for (String s : split) {
            if (s.contains("map_options")) {
                String[] split1 = s.split("=");
                if (split1.length > 1) {
                    map_options = split1[1];
                }
            }
        }

        String operatorType = MapFilterTypes.MAP_FILTER_TYPE_ALL;
        if (map_options != null) {
            if (map_options.contains("mobile")) {
                operatorType = MapFilterTypes.MAP_FILTER_TYPE_MOBILE;
            } else if (map_options.contains("wifi")) {
                operatorType = MapFilterTypes.MAP_FILTER_TYPE_WLAN;
            } else if (map_options.contains("browser")) {
                operatorType = MapFilterTypes.MAP_FILTER_TYPE_BROWSER;
            } else if (map_options.contains("all")) {
                operatorType = MapFilterTypes.MAP_FILTER_TYPE_ALL;
            }
        }
        return operatorType;
    }

    public static String getChosenMapLayout(Context context) {
        SharedPreferences sp = PreferenceConfig.getPreferenceSharedPreferences(context);
        String string = sp.getString(MapLayout.MAP_FILTER_LAYOUT_API_LINK, "");
        Timber.e("Loading style string %s", string);
        return string;
    }

    public static String getChosenMapLayoutAccessToken(Context context) {
        SharedPreferences sp = PreferenceConfig.getPreferenceSharedPreferences(context);
        String string = sp.getString(MapLayout.MAP_FILTER_LAYOUT_ACCESS_TOKEN, "");
        return string;
    }

    public static String getChosenMapLayoutLayer(Context context) {
        SharedPreferences sp = PreferenceConfig.getPreferenceSharedPreferences(context);
        String string = sp.getString(MapLayout.MAP_FILTER_LAYOUT_LAYER, "");
        return string;
    }

}
