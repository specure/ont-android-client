/*******************************************************************************
 * Copyright 2014-2017 Specure GmbH
 * Copyright 2013-2015 alladin-IT GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package at.specure.android.api.calls;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.specure.opennettest.R;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import at.specure.android.api.ControlServerConnection;
import at.specure.android.screens.main.MainActivity;
import at.specure.android.screens.map.MapFilterTypes;
import at.specure.android.screens.map.MapListEntry;
import at.specure.android.screens.map.MapListSection;
import at.specure.android.screens.map.MapProperties;
import at.specure.android.util.EndTaskListener;
import at.specure.android.configs.FeatureConfig;
import at.specure.android.api.jsons.MapFilterCountries;

public class GetMapOptionsInfoTask extends AsyncTask<Void, Void, JsonObject> {
    /**
     *
     */
    private static final String DEBUG_TAG = "GetMapOptionsInfoTask";

    /**
     *
     */
    private final MainActivity activity;

    /**
     *
     */
    private ControlServerConnection serverConn;

    /**
     *
     */
    private EndTaskListener endTaskListener;

    /**
     *
     */
    private boolean hasError = false;

    /**
     * @param activity
     */
    public GetMapOptionsInfoTask(final MainActivity activity) {
        this.activity = activity;
    }

    /**
     *
     */
    @Override
    protected JsonObject doInBackground(final Void... params) {
        JsonObject result = null;

        serverConn = new ControlServerConnection(activity.getApplicationContext(), true);

        try {
            result = serverConn.requestMapOptionsInfo();
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     *
     */
    @Override
    protected void onCancelled() {
        if (serverConn != null) {
            serverConn.unload();
            serverConn = null;
        }
    }

    /**
     *
     */
    @Override
    protected void onPostExecute(final JsonObject result) {
        if (serverConn.hasError())
            hasError = true;
        else if (result != null) {
            try {
                MapFilterCountries mapFilterCountries = null;
                if (FeatureConfig.showCountrySpecificOperatorsInMapFilters(this.activity)) {
                    mapFilterCountries = new Gson().fromJson(result, MapFilterCountries.class);
                }


                final JsonObject mapSettingsObject = result.getAsJsonObject("mapfilter");

                // ////////////////////////////////////////////////////
                // MAP / CHOOSE

                final JsonArray mapTypeArray = mapSettingsObject.getAsJsonArray("mapTypes");

//                Log.d(DEBUG_TAG, mapTypeArray.toString(4));

                // /

                final ArrayList<MapListSection> mapListSectionList = new ArrayList<MapListSection>();

                for (int cnt = 0; cnt < mapTypeArray.size(); cnt++) {

                    final JsonObject t = mapTypeArray.get(cnt).getAsJsonObject();

                    MapListSection mapListSection = new Gson().fromJson(t, MapListSection.class);
                    mapListSection.setListKey("map_options");


                    if (FeatureConfig.showCountrySpecificOperatorsInMapFilters(this.activity)) {
                        if (!MapFilterTypes.MAP_FILTER_TYPE_OPERATOR.equalsIgnoreCase(mapListSection.getType())) {
                            mapListSectionList.add(mapListSection);
                        }
                    } else {
                        mapListSectionList.add(mapListSection);
                    }
                }

                // ////////////////////////////////////////////////////
                // MAP / FILTER

                final JsonObject mapFiltersObject = mapSettingsObject.getAsJsonObject("mapFilters");

                final HashMap<String, List<MapListSection>> mapFilterListSectionListHash = new HashMap<String, List<MapListSection>>();

//                Log.d(DEBUG_TAG, mapFilterArray.toString(4));

                for (final String typeKey : new String[]{"mobile", "wifi", "browser", "all"}) {
                    final JsonArray mapFilterArray = mapFiltersObject.getAsJsonArray(typeKey);
                    final List<MapListSection> mapFilterListSectionList = new ArrayList<MapListSection>();
                    mapFilterListSectionListHash.put(typeKey, mapFilterListSectionList);


                    // add map appearance option (satellite, no satellite)
                    final MapListSection appearanceSection = new MapListSection(
                            activity.getString(R.string.map_appearance_header), Arrays.asList(
                            new MapListEntry(activity.getString(R.string.map_appearance_nosat_title), activity
                                    .getString(R.string.map_appearance_nosat_summary), true,
                                    MapProperties.MAP_SAT_KEY, MapProperties.MAP_NOSAT_VALUE, true),
                            new MapListEntry(activity.getString(R.string.map_appearance_sat_title), activity
                                    .getString(R.string.map_appearance_sat_summary), MapProperties.MAP_SAT_KEY,
                                    MapProperties.MAP_SAT_VALUE)));

                    mapFilterListSectionList.add(appearanceSection);

                    // add overlay option (heatmap, points)
                    MapListSection overlaySection = new MapListSection(
                            activity.getString(R.string.map_overlay_header), Arrays.asList(
                            new MapListEntry(activity.getString(R.string.map_overlay_auto_title), activity
                                    .getString(R.string.map_overlay_auto_summary), true,
                                    MapProperties.MAP_OVERLAY_KEY, MapProperties.MapOverlay.AUTO.name(), true),
                            new MapListEntry(activity.getString(R.string.map_overlay_heatmap_title), activity
                                    .getString(R.string.map_overlay_heatmap_summary),
                                    MapProperties.MAP_OVERLAY_KEY, MapProperties.MapOverlay.HEATMAP.name()),
                            new MapListEntry(activity.getString(R.string.map_overlay_points_title), activity
                                    .getString(R.string.map_overlay_points_summary), MapProperties.MAP_OVERLAY_KEY,
                                    MapProperties.MapOverlay.POINTS.name())

                    ));

                    List<MapListEntry> mapListEntryList1 = overlaySection.getMapListEntryList();
                    ArrayList<MapListEntry> mapListEntries = new ArrayList<>(mapListEntryList1);
                    if (MapProperties.MapOverlay.REGIONS.showInFilters(this.activity)) {
                        mapListEntries.add(new MapListEntry(activity.getString(R.string.map_overlay_regions_title), activity
                                .getString(R.string.map_overlay_regions_summary), MapProperties.MAP_OVERLAY_KEY,
                                MapProperties.MapOverlay.REGIONS.name()));
                    }

                    if (MapProperties.MapOverlay.MUNICIPALITY.showInFilters(this.activity)) {
                        mapListEntries.add(new MapListEntry(activity.getString(R.string.map_overlay_municipality_title), activity
                                .getString(R.string.map_overlay_municipality_summary), MapProperties.MAP_OVERLAY_KEY,
                                MapProperties.MapOverlay.MUNICIPALITY.name()));
                    }

                    if (MapProperties.MapOverlay.SETTLEMENTS.showInFilters(this.activity)) {
                        mapListEntries.add(new MapListEntry(activity.getString(R.string.map_overlay_settlements_title), activity
                                .getString(R.string.map_overlay_settlements_summary), MapProperties.MAP_OVERLAY_KEY,
                                MapProperties.MapOverlay.SETTLEMENTS.name()));
                    }

                    if (MapProperties.MapOverlay.WHITESPOTS.showInFilters(this.activity)) {
                        mapListEntries.add(new MapListEntry(activity.getString(R.string.map_overlay_whitespots_title), activity
                                .getString(R.string.map_overlay_whitespots_summary), MapProperties.MAP_OVERLAY_KEY,
                                MapProperties.MapOverlay.WHITESPOTS.name()));
                    }
                    overlaySection.setMapListEntryList(mapListEntries);

                    mapFilterListSectionList.add(overlaySection);

                    // add other filter options

                    for (int cnt = 0; cnt < mapFilterArray.size(); cnt++) {

                        final JsonObject t = mapFilterArray.get(cnt).getAsJsonObject();

                        final String sectionTitle = t.get("title").getAsString();

                        String entryType = null;
                        if (t.has("type")) {
                            entryType = t.get("type").getAsString();
                        }


                        final JsonArray objectOptionsArray = t.getAsJsonArray("options");

                        // /

                        final List<MapListEntry> mapListEntryList = new ArrayList<MapListEntry>();

                        boolean haveDefault = false;

                        for (int cnt2 = 0; cnt2 < objectOptionsArray.size(); cnt2++) {

                            final JsonObject s = objectOptionsArray.get(cnt2).getAsJsonObject();

                            final String entryTitle = s.get("title").getAsString();
                            final String entrySummary = s.get("summary").getAsString();
                            boolean entryDefault = false;
                            if (s.has("default")) {
                                entryDefault = s.get("default").getAsBoolean();
                            }

                            s.remove("title");
                            s.remove("summary");
                            s.remove("default");

                            //

                            final MapListEntry mapListEntry = new MapListEntry(entryTitle, entrySummary);

                            //

                            JSONObject object;
                            Set<Map.Entry<String, JsonElement>> entries = s.entrySet();
                            Iterator<Map.Entry<String, JsonElement>> iterator = entries.iterator();

                            for (Map.Entry<String, JsonElement> entry : entries) {

                                mapListEntry.setKey(entry.getKey());
                                mapListEntry.setValue(entry.getValue().getAsString());

                            }

                            mapListEntry.setChecked(entryDefault && !haveDefault);
                            mapListEntry.setDefault(entryDefault);
                            if (entryDefault)
                                haveDefault = true;

                            // /

                            mapListEntryList.add(mapListEntry);
                        }

                        if (!haveDefault && mapListEntryList.size() > 0) {
                            final MapListEntry first = mapListEntryList.get(0);
                            first.setChecked(true); // set first if we had no default
                            first.setDefault(true);
                        }

                        final MapListSection mapListSection = new MapListSection(sectionTitle, entryType, mapListEntryList);

                        if (FeatureConfig.showCountrySpecificOperatorsInMapFilters(this.activity)) {
                            if (!MapFilterTypes.MAP_FILTER_TYPE_OPERATOR.equalsIgnoreCase(mapListSection.getType())) {
                                mapFilterListSectionList.add(mapListSection);
                            }
                        } else {
                            mapFilterListSectionList.add(mapListSection);
                        }
                    }
                }


                // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

                // map type

                final MapListEntry entry = mapListSectionList.get(0).getMapListEntryList().get(0);

                if (FeatureConfig.showCountrySpecificOperatorsInMapFilters(activity)) {
                    activity.setMapFilterCountries(mapFilterCountries);
                }

                if (activity.getCurrentMapType() == null) {
                    activity.setCurrentMapType(entry, mapListSectionList.get(0));
                }

                activity.setMapTypeListSectionList(mapListSectionList);
                activity.setMapFilterListSectionListMap(mapFilterListSectionListHash);


            } catch (final JsonParseException | NullPointerException e) {
                e.printStackTrace();
            }

        } else
            Log.i(DEBUG_TAG, "LEERE LISTE");

        if (endTaskListener != null) {
            final JsonArray array = new JsonArray();
            array.add(result);
            endTaskListener.taskEnded(array);
        }
    }

    /**
     * @param endTaskListener
     */
    public void setEndTaskListener(final EndTaskListener endTaskListener) {
        this.endTaskListener = endTaskListener;
    }

    /**
     * @return
     */
    public boolean hasError() {
        return hasError;
    }
}
