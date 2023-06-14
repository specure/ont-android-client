package at.specure.android.screens.badges.newbadges.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.specure.opennettest.R;

import java.util.List;

import at.specure.androidX.data.badges.Badge;
import at.specure.util.tools.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import at.specure.util.tools.expandablerecyclerview.models.ExpandableGroup;

public class BadgesGroupsAdapter  extends ExpandableRecyclerViewAdapter<BadgeGroupViewHolder, BadgeItemViewHolder> {

    private final LayoutInflater mInflater;
    private final Context context;
    private OnBadgeClickListener listener;

    public BadgesGroupsAdapter(List<? extends ExpandableGroup> groups, Context context) {
        super(groups);
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
    }

    public void setOnItemClickListener(OnBadgeClickListener listener) {
        this.listener = listener;
    }

    @Override
    public BadgeGroupViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.badge_group, parent, false);
        return new BadgeGroupViewHolder(view);
    }

    @Override
    public BadgeItemViewHolder onCreateChildViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.badge_new_item, parent, false);
        return new BadgeItemViewHolder(view);
    }

    @Override
    public void onBindChildViewHolder(BadgeItemViewHolder holder, int flatPosition, ExpandableGroup group, int childIndex) {
        final Badge badge = ((BadgesGroup) group).getItems().get(childIndex);
        holder.setValues(badge, context);
        if (listener != null) {
            holder.setOnClickListener(listener);
        }
    }

    @Override
    public void onBindGroupViewHolder(BadgeGroupViewHolder holder, int flatPosition, ExpandableGroup group) {
        holder.setGroupTitle(group.getTitle());
    }
}
