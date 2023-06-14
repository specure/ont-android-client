package at.specure.androidX.data.map_filter.adapter.filter_detail;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import com.specure.opennettest.R;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import at.specure.android.screens.map.map_filter_x.FilterDetailActivity;
import at.specure.android.screens.map.map_filter_x.FilterDetailFragment;
import at.specure.androidX.data.map_filter.mappers.MapFilterSaver;
import at.specure.androidX.data.map_filter.view_data.FilterGroup;
import at.specure.androidX.data.map_filter.view_data.FilterItem;
import at.specure.androidX.data.map_filter.view_data.FilterItemGroup;

import static at.specure.androidX.data.map_filter.view_data.FilterItemGroup.SELECTION_TYPE_EXCLUSIVE;

public class FilterGroupItemAdapter extends RecyclerView.Adapter<FilterGroupItemAdapter.ViewHolder> {

    private final List<FilterItemGroup> mValues;
    private final boolean mTwoPane;

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
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
    };
    private final FilterGroup group;

    public FilterGroupItemAdapter(FilterGroup group,
                                  boolean twoPane) {
        this.group = group;
        mValues = group.getFilterItemGroups();
        mTwoPane = twoPane;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.map_filter_group_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
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
//                    filterItemGroup.setSelected(item, filterItemGroup, group, !checked);
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

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView mIdView;
        final LinearLayout mContentView;

        ViewHolder(View view) {
            super(view);
            mIdView = (TextView) view.findViewById(R.id.item_title);
            mContentView = (LinearLayout) view.findViewById(R.id.item_container);
        }
    }
}