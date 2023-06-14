package at.specure.android.screens.badges.newbadges;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.specure.opennettest.R;

import at.specure.android.screens.main.BasicActivity;


/**
 * An activity representing a single Badge detail screen. This
 * activity is only used on narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link BadgeListActivity}.
 */
public class BadgeDetailActivity extends BasicActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_badge_detail);

        // Show the Up button in the action bar.
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar supportActionBar = getDelegate().getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }
        ActionBar supportActionBar1 = getSupportActionBar();
        if (supportActionBar1 != null) {
            supportActionBar1.setTitle(R.string.title_badge_detail);
        }

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(BadgeDetailFragment.ARG_ITEM_ID,
                    getIntent().getStringExtra(BadgeDetailFragment.ARG_ITEM_ID));
            BadgeDetailFragment fragment = new BadgeDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.badge_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            navigateUpTo(new Intent(this, BadgeListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        navigateUpTo(new Intent(this, BadgeListActivity.class));
    }
}
