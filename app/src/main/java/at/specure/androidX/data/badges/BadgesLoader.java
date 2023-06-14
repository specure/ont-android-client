package at.specure.androidX.data.badges;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.loader.content.AsyncTaskLoader;
import at.specure.android.api.ControlServerConnection;
import at.specure.android.api.jsons.MeasurementServer;
import timber.log.Timber;

public class BadgesLoader extends AsyncTaskLoader<List<Badge>> {

    List<Badge> badges;


    public BadgesLoader(@NonNull Context context) {
        super(context);
    }

    @Nullable
    @Override
    public List<Badge> loadInBackground() {
        try {
            Gson gson = new Gson();
            ControlServerConnection serverConn = new ControlServerConnection(getContext());

            JsonArray response = serverConn.requestBadges();

            Type listType = new TypeToken<ArrayList<MeasurementServer>>() {
            }.getType();

            Badge[] badgesArray = gson.fromJson(response, Badge[].class);

            List<Badge> list = Arrays.asList(badgesArray);
            badges = list;
            return badges;
        } catch (Exception e) {
            Timber.e( "ERROR GETTING BADGES");
            return badges = new ArrayList<>();
        }
    }
}
