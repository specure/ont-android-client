package at.specure.androidX.data.badges;

import android.content.Context;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import androidx.lifecycle.LiveData;
import at.specure.android.api.ControlServerConnection;
import at.specure.android.api.jsons.MeasurementServer;
import timber.log.Timber;

public class BadgesLiveData extends LiveData<List<Badge>> {
    private final Context context;

    public BadgesLiveData(Context context) {
        this.context = context;
        loadData();
    }

    public Badge getBadgeById(String id) {
        List<Badge> value = getValue();
        if (id != null) {
            if ((value == null) || (value.isEmpty())) {
                loadData();
            } else {
                for (Badge badge : value) {
                    if (id.equalsIgnoreCase(badge.id)) {
                        return badge;
                    }
                }
            }
        }
        return null;
    }


    private void loadData() {
        List<Badge> value = getValue();

        if (value == null) {
            new AsyncTask<Void, Void, List<Badge>>() {
                @Override
                protected List<Badge> doInBackground(Void... voids) {
                    try {
                        Gson gson = new Gson();
                        ControlServerConnection serverConn = new ControlServerConnection(context);

                        JsonArray response = serverConn.requestBadges();
                        JsonObject badgesJson = response.get(0).getAsJsonObject();

                        Type listType = new TypeToken<ArrayList<MeasurementServer>>() {
                        }.getType();

                        BadgesResponse badgesArray = gson.fromJson(badgesJson, BadgesResponse.class);

                        return badgesArray.badges;
                    } catch (Exception e) {
                        Timber.e("ERROR GETTING BADGES");
                        return new ArrayList<>();
                    }
                }

                @Override
                protected void onPostExecute(List<Badge> data) {
                    setValue(data);
//                    BadgesConfig.setAllBadgesReceived(context, data);
                }
            }.execute();
        }
    }
}
