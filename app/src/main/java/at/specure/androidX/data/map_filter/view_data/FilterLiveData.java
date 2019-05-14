package at.specure.androidX.data.map_filter.view_data;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.List;

import at.specure.android.api.ControlServerConnection;
import at.specure.android.configs.FeatureConfig;
import at.specure.androidX.data.map_filter.data.MapTilesInfo;
import at.specure.androidX.data.map_filter.mappers.MapFilterDataMapper;
import at.specure.androidX.data.map_filter.mappers.MapFilterSaver;
import at.specure.androidX.data.operators.MapFilterOperators;
import timber.log.Timber;

public class FilterLiveData extends LiveData<List<FilterGroup>> {

    private final Context context;
    private String operatorCountry = "";
    private JsonObject operatorList;

    @Nullable
    @Override
    public List<FilterGroup> getValue() {
        return super.getValue();
    }

    public FilterLiveData(Context context) {
        this.context = context;
        loadData();
    }

    public FilterGroup getFilterGroupById(String id, String operatorCountry) {
        List<FilterGroup> value = getValue();
        if (id != null) {
            if ((value == null) || (value.isEmpty())) {
                loadData();
            } else {
                for (FilterGroup badge : value) {
                    if (id.equalsIgnoreCase(badge.id)) {
                        return badge;
                    }
                }
            }
        }
        return null;
    }

    private void loadData() {
        List<FilterGroup> value = getValue();
        String countryCode = FeatureConfig.countrySpecificOperatorsCountryCode(context.getApplicationContext());

        if ((value == null) || (!this.operatorCountry.equalsIgnoreCase(countryCode))) {
            this.operatorCountry = countryCode;
            new AsyncTask<Void, Void, List<FilterGroup>>() {
                @Override
                protected List<FilterGroup> doInBackground(Void... voids) {
                    List<FilterGroup> filterGroups = null;
                    try {
                        ControlServerConnection serverConn = new ControlServerConnection(context.getApplicationContext(), true);
                        JsonObject tilesInfoJson = serverConn.requestMapOptionsInfoV2();

                        if (tilesInfoJson != null) {
                            Gson gson = new Gson();
                            MapTilesInfo mapTilesInfo = gson.fromJson(tilesInfoJson, MapTilesInfo.class);

                            FilterGroup filterGroup = null;
                            if (operatorCountry != null) {
                                try {
                                    serverConn = new ControlServerConnection(context.getApplicationContext(), true);
                                    if ((operatorCountry != null) && (!operatorCountry.isEmpty())) {
                                        //TODO:change to provider type according to map filters are set
//                                        if ((operatorType != null) && (!operatorType.isEmpty())) {
                                            operatorList = serverConn.requestMapOperatorsFilterV2(operatorCountry, MapFilterSaver.getFilterOperatorType(context));
//                                        }
                                    }

                                    if (operatorList != null) {
                                        MapFilterOperators operators = gson.fromJson(operatorList, MapFilterOperators.class);
                                        if (operators != null) {
                                        filterGroup = operators.toFilterGroupX(context);

                                        }
                                    }
                                } catch (Exception e) {
                                    Timber.e("ERROR GETTING MAP FILTER OPERATORS V2 ITEMS");
                                }
                            }

                            filterGroups = MapFilterDataMapper.mapToAdapter(mapTilesInfo, filterGroup, context);
//                            if (filterGroup != null) {
//                                filterGroups.add(filterGroup);
//                            }

                        }
                        return filterGroups;
                    } catch (Exception e) {
                        Timber.e("ERROR GETTING MAP TILES INFO V2 ITEMS");
                        return filterGroups;
                    }
                }

                @Override
                protected void onPostExecute(List<FilterGroup> data) {
                    setValue(data);
//                    BadgesConfig.setAllBadgesReceived(context, data);
                }
            }.execute();
        }
    }

    public String getCountryCode() {
        return operatorCountry;
    }
}
