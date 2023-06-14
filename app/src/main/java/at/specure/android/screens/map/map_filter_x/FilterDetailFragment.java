package at.specure.android.screens.map.map_filter_x;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import at.specure.androidX.data.map_filter.adapter.filter_detail.FilterGroupItemAdapter;
import at.specure.androidX.data.map_filter.adapter.filter_detail.FilterGroupItemExpandableAdapter;
import at.specure.androidX.data.map_filter.view_data.FilterGroup;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.specure.opennettest.R;

import java.util.ArrayList;


/**
 * A fragment representing a single Filter detail screen.
 * This fragment is either contained in a {@link FilterListActivity}
 * in two-pane mode (on tablets) or a {@link FilterDetailActivity}
 * on handsets.
 */
public class FilterDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    /**
     * The dummy content this fragment is presenting.
     */
    private FilterGroup mItem;
    private FilterGroup filterGroup;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FilterDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = getArguments().getParcelable(ARG_ITEM_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.filter_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (mItem != null) {

        View recyclerView = rootView.findViewById(R.id.filter_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);
        }

        return rootView;
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        ArrayList<FilterGroup> filterGroups = new ArrayList<>();
//        filterGroup = new FilterGroup(mItem);

//        recyclerView.setAdapter(new FilterGroupItemAdapter(mItem, false));
        recyclerView.setAdapter(new FilterGroupItemExpandableAdapter(mItem.getFilterItemGroups(), mItem, false));
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
