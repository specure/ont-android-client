package at.specure.androidX.data.history;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;
import at.specure.android.api.ControlServerConnection;
import at.specure.android.api.jsons.MeasurementServer;
import at.specure.android.configs.ConfigHelper;
import at.specure.android.screens.main.MainActivity;
import at.specure.android.util.EndTaskListener;
import timber.log.Timber;

public class HistoryLoader extends AsyncTaskLoader<List<HistoryItem>> {

    List<HistoryItem> historyItems;
    private MainActivity activity;
    private ArrayList<String> devicesToShow;
    private ArrayList<String> networksToShow;
    private JsonArray historyList;
    private String uuid;
    private ControlServerConnection serverConn;
    private EndTaskListener endTaskListener;
    private boolean hasError = false;
    private ArrayList<HistoryItem> historyItemsFilteredFromBad;

    public HistoryLoader(final MainActivity mainActivity, final ArrayList<String> devicesToShow,
                         final ArrayList<String> networksToShow) {
        super(mainActivity);
        this.activity = mainActivity;
        this.devicesToShow = devicesToShow;
        this.networksToShow = networksToShow;
    }

    public HistoryLoader(@NonNull Context context) {
        super(context);
    }

    @Nullable
    @Override
    public List<HistoryItem> loadInBackground() {

        try {
            serverConn = new ControlServerConnection(activity.getApplicationContext());
            uuid = ConfigHelper.getUUID(activity.getApplicationContext());
            if (uuid.length() > 0) {
                historyList = serverConn.requestHistory(uuid, devicesToShow, networksToShow, activity.getHistoryResultLimit());
                Gson gson = new Gson();
                HistoryItem[] historyItemsArray = gson.fromJson(historyList, HistoryItem[].class);
                historyItems = Arrays.asList(historyItemsArray);

                //could be removed when information collector will be fixed
                historyItemsFilteredFromBad = new ArrayList<HistoryItem>();
                if (historyItems != null) {
                    for (HistoryItem historyItem : historyItems) {
                        if (historyItem != null) {
                            String ping = historyItem.getPing();
                            try {
                                Long aLong = Long.valueOf(ping);
                                if (aLong != Long.MAX_VALUE) {
                                    historyItemsFilteredFromBad.add(historyItem);
                                }
                            } catch (Exception ignored) {
                                historyItemsFilteredFromBad.add(historyItem);
                            }
                        }
                    }
                }


                return historyItemsFilteredFromBad;
            } else {
                return historyItems = new ArrayList<>();
            }
        } catch (Exception e) {
            Timber.e("ERROR GETTING HISTORY ITEMS");
            return historyItems = new ArrayList<>();
        }
    }
}
