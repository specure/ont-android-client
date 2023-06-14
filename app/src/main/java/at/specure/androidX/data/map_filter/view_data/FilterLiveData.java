package at.specure.androidX.data.map_filter.view_data;

import android.content.Context;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.logging.Filter;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import at.specure.android.api.ControlServerConnection;
import at.specure.android.configs.FeatureConfig;
import at.specure.android.screens.main.MainActivity;
import at.specure.androidX.data.map_filter.data.MapTilesInfo;
import at.specure.androidX.data.map_filter.mappers.MapFilterDataMapper;
import at.specure.androidX.data.map_filter.mappers.MapFilterSaver;
import at.specure.androidX.data.operators.MapFilterOperators;
import timber.log.Timber;

public class FilterLiveData extends LiveData<List<FilterGroup>> {

    private final Context context;
    private String operatorCountry = "";
    private JsonObject operatorList;
    private LoadFiltersAsyncTask asyncTask;

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
            asyncTask = new LoadFiltersAsyncTask(this);
            asyncTask.execute(this);
        }
    }

    public String getCountryCode() {
        return operatorCountry;
    }

    public void setData(List<FilterGroup> result) {
        setValue(result);
    }

    public static class LoadFiltersAsyncTask extends AsyncTask<FilterLiveData, Void, List<FilterGroup>> {

        private FilterLiveData livedata;

        public LoadFiltersAsyncTask(FilterLiveData livedata) {
            this.livedata = livedata;
        }

        @Override
        protected List<FilterGroup> doInBackground(FilterLiveData... voids) {
            List<FilterGroup> filterGroups = null;
            try {
                FilterLiveData data = voids[0];
                ControlServerConnection serverConn = new ControlServerConnection(data.context.getApplicationContext(), true);
                JsonObject tilesInfoJson = serverConn.requestMapOptionsInfoV2();

                if (tilesInfoJson != null) {
                    Gson gson = new Gson();
                    MapTilesInfo mapTilesInfo = gson.fromJson(tilesInfoJson, MapTilesInfo.class);

                    FilterGroup filterGroup = null;
                    if (data.operatorCountry != null) {
                        try {
                            serverConn = new ControlServerConnection(data.context.getApplicationContext(), true);
                            if ((data.operatorCountry != null) && (!data.operatorCountry.isEmpty())) {
                                //TODO:change to provider type according to map filters are set
//                                        if ((operatorType != null) && (!operatorType.isEmpty())) {
                                data.operatorList = serverConn.requestMapOperatorsFilterV2(data.operatorCountry, MapFilterSaver.getFilterOperatorType(data.context));
//                                        }
                            }

                            if (data.operatorList != null) {
                                MapFilterOperators operators = gson.fromJson(data.operatorList, MapFilterOperators.class);
                                if (operators != null) {
                                    filterGroup = operators.toFilterGroupX(data.context);

                                }
                            }
                        } catch (Exception e) {
                            Timber.e("ERROR GETTING MAP FILTER OPERATORS V2 ITEMS");
                        }
                    }

                    filterGroups = MapFilterDataMapper.mapToAdapter(mapTilesInfo, filterGroup, data.context);
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
            livedata.setValue(data);
        }

    }
}
