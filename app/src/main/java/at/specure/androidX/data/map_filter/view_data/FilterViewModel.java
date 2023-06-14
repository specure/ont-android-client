package at.specure.androidX.data.map_filter.view_data;

import android.app.Application;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class FilterViewModel extends AndroidViewModel {

    private FilterLiveData data;

    public FilterViewModel(@NonNull Application application) {
        super(application);
        data = new FilterLiveData(application);
    }

    public LiveData<List<FilterGroup>> getData() {
        return data;
    }

    public FilterGroup getFilterGroupById(String id) {
        if (id != null) {
            if ((data == null) || (data.getValue() == null)) {
                getData();
            } else {
                for (FilterGroup filterGroup : data.getValue()) {
                    if (id.equalsIgnoreCase(filterGroup.id)) {
                        return filterGroup;
                    }
                }
            }
        }
        return null;
    }


}
