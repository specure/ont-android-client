package at.specure.androidX.data.map_filter.adapter.filter_detail;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.specure.opennettest.R;

import java.util.List;

import at.specure.androidX.data.map_filter.mappers.MapFilterSaver;
import at.specure.androidX.data.map_filter.view_data.FilterGroup;
import at.specure.androidX.data.map_filter.view_data.FilterItem;
import at.specure.androidX.data.map_filter.view_data.FilterItemGroup;
import at.specure.util.tools.expandablecheckrecyclerview.CheckableChildRecyclerViewAdapter;
import at.specure.util.tools.expandablecheckrecyclerview.listeners.OnCheckChildClickListener;
import at.specure.util.tools.expandablecheckrecyclerview.models.CheckedExpandableGroup;
import at.specure.util.tools.expandablerecyclerview.models.ExpandableGroup;

import static at.specure.androidX.data.map_filter.view_data.FilterGroup.SELECTION_TYPE_EXCLUSIVE;

public class FilterGroupItemExpandableAdapter extends CheckableChildRecyclerViewAdapter<FilterGroupViewHolder, FilterItemViewHolder> {

    private final boolean mTwoPane;

    /*private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            FilterGroup item = (FilterGroup) view.getTag();
            if (mTwoPane) {
                Bundle arguments = new Bundle();
                arguments.putString(FilterDetailFragment.ARG_ITEM_ID, item.getId());
                FilterDetailFragment fragment = new FilterDetailFragment();
                fragment.setArguments(arguments);
            } else {
                Context context = view.getContext();
                Intent intent = new Intent(context, FilterDetailActivity.class);
                intent.putExtra(FilterDetailFragment.ARG_ITEM_ID, item.getId());

                context.startActivity(intent);
            }
        }
    };*/
    private final FilterGroup group;

//    @Override
//    public void onChildCheckChanged(View view, boolean checked, int flatPos) {
//        super.onChildCheckChanged(view, checked, flatPos);
//        ExpandableListPosition listPos = expandableList.getUnflattenedPosition(flatPos);
//        FilterItemGroup filterItemGroup = (FilterItemGroup) expandab leList.getExpandableGroup(listPos);
//        FilterItem item = (FilterItem) filterItemGroup.getItems().get(listPos.childPos);
//        filterItemGroup.setSelected(item, filterItemGroup, group, !checked);
//        MapFilterSaver.saveGroupSettings(group, view.getContext().getApplicationContext());
//    }

    public FilterGroupItemExpandableAdapter(List<? extends CheckedExpandableGroup> groups, FilterGroup group, boolean twoPane) {
        super(groups);
        this.group = group;
        this.mTwoPane = twoPane;
        setForceExpandedGroups(true);
        setChildClickListener(new OnCheckChildClickListener() {
            @Override
            public void onCheckChildCLick(View v, boolean checked, CheckedExpandableGroup itemGroup, int childIndex) {
                FilterItemGroup filterItemGroup = (FilterItemGroup) itemGroup;
                FilterItem item = (FilterItem) itemGroup.getItems().get(childIndex);
                filterItemGroup.setSelected(item, filterItemGroup, group, !checked, childIndex);
                MapFilterSaver.saveGroupSettings(group, v.getContext().getApplicationContext());
                if (group.selectionType == SELECTION_TYPE_EXCLUSIVE) {
                    notifyChange();
                }
            }
        });
    }

    /*public FilterGroupItemExpandableAdapter(FilterGroup group,
                                            boolean twoPane) {
        this.group = group;
        mValues = group.getFilterItemGroups();
        mTwoPane = twoPane;
    }*/

//    @Override
//    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.map_filter_group_item, parent, false);
//        return new ViewHolder(view);
//    }

    /*@Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final FilterItemGroup filterItemGroup = mValues.get(position);
        String title1 = filterItemGroup.getTitle();
        if (title1 == null || title1.isEmpty()) {
            holder.mIdView.setVisibility(View.GONE);
        } else {
            holder.mIdView.setVisibility(View.VISIBLE);
            holder.mIdView.setText(title1);
        }

        List<FilterItem> selected = filterItemGroup.getSelected();
        List<FilterItem> items = filterItemGroup.getItems();
        holder.mContentView.removeAllViews();

        for (FilterItem item : items) {
            View view = LayoutInflater.from(holder.mIdView.getContext())
                    .inflate(R.layout.map_filter_item, null, false);
            TextView title = view.findViewById(R.id.title);
            TextView summary = view.findViewById(R.id.summary);
            RadioButton radioButton = view.findViewById(R.id.radiobutton);
            CheckBox checkButton = view.findViewById(R.id.checkbox);
            title.setText(item.getTitle());
            summary.setText("");
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean checked = checkButton.isChecked();
                    checkButton.setChecked(!checked);
                    filterItemGroup.setSelected(item, filterItemGroup, group, !checked);
                    MapFilterSaver.saveGroupSettings(group, holder.mIdView.getContext().getApplicationContext());
                    notifyItemChanged(position);
                }
            });
            if (selected != null && selected.contains(item)) {
                if (filterItemGroup.selectionMode == SELECTION_TYPE_EXCLUSIVE) {
                    radioButton.setVisibility(View.VISIBLE);
                    checkButton.setVisibility(View.GONE);
                    radioButton.setChecked(true);
                } else {
                    radioButton.setVisibility(View.GONE);
                    checkButton.setVisibility(View.VISIBLE);
                    checkButton.setChecked(true);
                }
            } else {
                if (filterItemGroup.selectionMode == SELECTION_TYPE_EXCLUSIVE) {
                    radioButton.setVisibility(View.VISIBLE);
                    checkButton.setVisibility(View.GONE);
                    radioButton.setChecked(false);
                } else {
                    radioButton.setVisibility(View.GONE);
                    checkButton.setVisibility(View.VISIBLE);
                    checkButton.setChecked(false);
                }
            }
            holder.mContentView.addView(view);
        }

//        holder.itemView.setTag(mValues.get(position));
//        holder.itemView.setOnClickListener(mOnClickListener);
    }
*/

    @Override
    public FilterGroupViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.map_filter_group_item, parent, false);
        return new FilterGroupViewHolder(view);
    }

    @Override
    public void onBindGroupViewHolder(FilterGroupViewHolder holder, int flatPosition, ExpandableGroup group) {
        holder.setTitle(group.getTitle());
    }

    @Override
    public FilterItemViewHolder onCreateCheckChildViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.map_filter_item, parent, false);
        return new FilterItemViewHolder(view);
    }

    @Override
    public void onBindCheckChildViewHolder(FilterItemViewHolder holder, int flatPosition, CheckedExpandableGroup group, int childIndex) {
        final FilterItem item = (FilterItem) group.getItems().get(childIndex);
        holder.onBind(item);
    }

//    class ViewHolder extends RecyclerView.ViewHolder {
//        final TextView mIdView;
//        final LinearLayout mContentView;
//
//        ViewHolder(View view) {
//            super(view);
//            mIdView = (TextView) view.findViewById(R.id.item_title);
//            mContentView = (LinearLayout) view.findViewById(R.id.item_container);
//        }
//    }
}