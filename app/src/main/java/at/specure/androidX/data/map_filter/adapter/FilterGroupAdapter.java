package at.specure.androidX.data.map_filter.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.specure.opennettest.R;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import at.specure.android.screens.main.BasicActivity;
import at.specure.android.screens.map.map_filter_x.FilterDetailActivity;
import at.specure.android.screens.map.map_filter_x.FilterDetailFragment;
import at.specure.android.screens.map.map_filter_x.FilterListActivity;
import at.specure.androidX.data.map_filter.view_data.FilterGroup;

public class FilterGroupAdapter extends RecyclerView.Adapter<FilterGroupAdapter.ViewHolder> {

    private final List<FilterGroup> mValues;
    private final boolean mTwoPane;
    private BasicActivity mParentActivity;

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            FilterGroup item = (FilterGroup) view.getTag();
            if (mTwoPane) {
                Bundle arguments = new Bundle();
                arguments.putParcelable(FilterDetailFragment.ARG_ITEM_ID, item);
                FilterDetailFragment fragment = new FilterDetailFragment();
                fragment.setArguments(arguments);
                mParentActivity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.filter_detail_container, fragment)
                        .commit();
            } else {
                Context context = view.getContext();
                Intent intent = new Intent(context, FilterDetailActivity.class);

                intent.setExtrasClassLoader(FilterGroup.class.getClassLoader());
                intent.putExtra(FilterDetailFragment.ARG_ITEM_ID, item);

                context.startActivity(intent);
            }
        }
    };

    public FilterGroupAdapter(FilterListActivity parent,
                              List<FilterGroup> items,
                              boolean twoPane) {
        mValues = items;
        mParentActivity = parent;
        mTwoPane = twoPane;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.filter_list_content, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mIdView.setText(mValues.get(position).getTitle());
        holder.mContentView.setText(mValues.get(position).getSelectedValuesToShow());

        holder.itemView.setTag(mValues.get(position));
        holder.itemView.setOnClickListener(mOnClickListener);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final TextView mIdView;
        final TextView mContentView;

        ViewHolder(View view) {
            super(view);
            mIdView = (TextView) view.findViewById(R.id.title);
            mContentView = (TextView) view.findViewById(R.id.content);
        }
    }
}