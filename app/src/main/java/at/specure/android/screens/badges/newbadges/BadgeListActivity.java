package at.specure.android.screens.badges.newbadges;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.specure.opennettest.R;

import java.util.List;

import at.specure.android.screens.badges.newbadges.adapter.BadgesGroup;
import at.specure.android.screens.badges.newbadges.adapter.BadgesGroupsAdapter;
import at.specure.android.screens.badges.newbadges.adapter.OnBadgeClickListener;
import at.specure.android.screens.main.BasicActivity;
import at.specure.androidX.data.badges.Badge;
import at.specure.androidX.data.badges.BadgesViewModel;
import timber.log.Timber;


/**
 * An activity representing a list of Badges. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link BadgeDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class BadgeListActivity extends BasicActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    public static final String BADGE_BUNDLE_KEY = "BADGE_BUNDLE_KEY";
    private Badge badge;
    private BadgesGroupsAdapter badgesAdapter;
    private String badgeOrdinal;
    private LiveData<List<Badge>> data1;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_badge_list);
        // Show the Up button in the action bar.

        progressBar = findViewById(R.id.progress_bar);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar supportActionBar = getDelegate().getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
        ActionBar supportActionBar1 = getSupportActionBar();
        if (supportActionBar1 != null) {
            supportActionBar1.setTitle(R.string.title_badges);
        }

        if (findViewById(R.id.badge_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        Bundle bundleExtra = getIntent().getExtras();
        if (bundleExtra != null) {
            badgeOrdinal = bundleExtra.getString(BADGE_BUNDLE_KEY, null);
            if (badgeOrdinal != null) {
                openBadgeDetail(badgeOrdinal);
            }
        }

        recyclerView = findViewById(R.id.badge_list);
        assert recyclerView != null;
        BadgesViewModel badgesModel = ViewModelProviders.of(this).get(BadgesViewModel.class);
        data1 = badgesModel.getData();
                data1.observe(this, data -> {
                    if (data != null) {
                        Timber.i("Observing badges change: %s", data);
                        List<BadgesGroup> badgesGroups = BadgesGroup.convertFromBadge(data, this);
                        setupRecyclerView(recyclerView, badgesGroups);
                        progressBar.setVisibility(View.GONE);
                    }
                }
        );


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

    private void setupRecyclerView(@NonNull RecyclerView recyclerView, List<BadgesGroup> adapters) {
//        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(this, DummyContent.ITEMS, mTwoPane));
        badgesAdapter = new BadgesGroupsAdapter(adapters, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(badgesAdapter);

        badgesAdapter.setOnItemClickListener(new OnBadgeClickListener() {
            @Override
            public void onBadgeClicked(Badge badge) {
                if (badge != null) {
                    openBadge(badge);
                }
            }
        });
    }

//    @Override
//    public void onItemClick(View view, BadgesConfig.BADGES badge) {
//        openBadge(badge);
//    }

    private void openBadge(Badge badge) {
        if (badge != null) {
            this.badge = badge;
            String id = badge.id;
            openBadgeDetail(id);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (data1 != null && data1.getValue() != null) {
            List<BadgesGroup> badgesGroups = BadgesGroup.convertFromBadge(data1.getValue(), this);
            setupRecyclerView((RecyclerView) recyclerView, badgesGroups);
            progressBar.setVisibility(View.GONE);
        }
    }

    private void openBadgeDetail(String id) {
        if (mTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putString(BadgeDetailFragment.ARG_ITEM_ID, id);
            BadgeDetailFragment fragment = new BadgeDetailFragment();
            fragment.setArguments(arguments);
            this.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.badge_detail_container, fragment)
                    .commit();
        } else {
            Intent intent = new Intent(this, BadgeDetailActivity.class);
            intent.putExtra(BadgeDetailFragment.ARG_ITEM_ID, id);
            this.startActivity(intent);
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        if (badgesAdapter != null) {
            badgesAdapter.onSaveInstanceState(outState);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (badgesAdapter != null) {
            badgesAdapter.onRestoreInstanceState(savedInstanceState);
        }
    }
}
