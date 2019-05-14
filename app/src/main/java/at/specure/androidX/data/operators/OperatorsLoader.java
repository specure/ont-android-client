package at.specure.androidX.data.operators;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import at.specure.android.api.ControlServerConnection;
import at.specure.android.screens.main.MainActivity;
import timber.log.Timber;

public class OperatorsLoader extends AsyncTaskLoader<MapFilterOperators> {

    MapFilterOperators mapFilterOperators;
    private MainActivity activity;
    private JsonObject operatorList;
    private String operatorType;
    private String countryCode;
    private ControlServerConnection serverConn;

    public OperatorsLoader(final MainActivity mainActivity, String countryCode, String operatorType) {
        super(mainActivity);
        this.countryCode = countryCode;
        this.operatorType = operatorType;
        this.activity = mainActivity;
    }

    public OperatorsLoader(@NonNull Context context) {
        super(context);
    }

    @Nullable
    @Override
    public MapFilterOperators loadInBackground() {

        try {
            serverConn = new ControlServerConnection(activity.getApplicationContext(), true);
            if ((countryCode != null) && (!countryCode.isEmpty())) {
                if ((operatorType != null) && (!operatorType.isEmpty())) {
                    operatorList = serverConn.requestMapOperatorsFilterV2(countryCode, operatorType);
                }
            }

            if (operatorList != null) {
                Gson gson = new Gson();
                MapFilterOperators operators = gson.fromJson(operatorList, MapFilterOperators.class);
                if (operators != null) {
                    return operators;
                }
            }
            return mapFilterOperators;
        } catch (Exception e) {
            Timber.e("ERROR GETTING MAP FILTER OPERATORS V2 ITEMS");
            return mapFilterOperators;
        }
    }
}
