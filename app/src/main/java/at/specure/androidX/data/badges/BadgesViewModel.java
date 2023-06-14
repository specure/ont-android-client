package at.specure.androidX.data.badges;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class BadgesViewModel extends AndroidViewModel {

    private final BadgesLiveData data;

    public BadgesViewModel(@NonNull Application application) {
        super(application);
        data = new BadgesLiveData(application);
    }

    public LiveData<List<Badge>> getData() {
        return data;
    }

    public Badge getBadgeById(String id) {
        if (id != null) {
            if ((data == null) || (data.getValue() == null)) {
                getData();
            } else {
                for (Badge badge : data.getValue()) {
                    if (id.equalsIgnoreCase(badge.id)) {
                        return badge;
                    }
                }
            }
        }
        return null;
    }
}
