package at.specure.android.screens.map.map_filter_x;

import android.os.Bundle;
import android.view.MenuItem;

import com.specure.opennettest.R;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;
import at.specure.android.configs.FeatureConfig;
import at.specure.android.screens.main.BasicActivity;
import at.specure.androidX.data.map_filter.adapter.FilterGroupAdapter;
import at.specure.androidX.data.map_filter.data.MapTilesInfo;
import at.specure.androidX.data.map_filter.view_data.FilterGroup;
import at.specure.androidX.data.map_filter.view_data.FilterViewModel;
import timber.log.Timber;

/**
 * An activity representing a list of Filters. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link FilterDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class FilterListActivity extends BasicActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private RecyclerView recyclerView;

    private List<FilterGroup> filterGroups;
    Observer<List<FilterGroup>> observer = new Observer<List<FilterGroup>>() {
        @Override
        public void onChanged(@Nullable List<FilterGroup> badges) {
            if (badges != null) {
                Timber.i("Observing map filter change: %s", badges);
                filterGroups = badges;
                setupRecyclerView();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_list);
        // Show the Up button in the action bar.

        // Show the Up button in the action bar.
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar supportActionBar = getDelegate().getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
        androidx.appcompat.app.ActionBar supportActionBar1 = getSupportActionBar();
        if (supportActionBar1 != null) {
            supportActionBar1.setTitle(R.string.title_filter_list);
        }

        if (findViewById(R.id.filter_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        recyclerView = findViewById(R.id.filter_list);
        assert recyclerView != null;

        FilterViewModel badgesModel = ViewModelProviders.of(this).get(FilterViewModel.class);
        LiveData<List<FilterGroup>> data  = badgesModel.getData();
        data.observe(this, observer);
        filterGroups = data.getValue();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (filterGroups == null) {
            FilterViewModel badgesModel = ViewModelProviders.of(this).get(FilterViewModel.class);
            LiveData<List<FilterGroup>> data  = badgesModel.getData();
            data.observe(this, observer);
            filterGroups = data.getValue();
            if (filterGroups != null) {
                setupRecyclerView();
            }
        } else {
            setupRecyclerView();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupRecyclerView() {
        recyclerView.setAdapter(new FilterGroupAdapter(this, filterGroups, mTwoPane));
    }

}
