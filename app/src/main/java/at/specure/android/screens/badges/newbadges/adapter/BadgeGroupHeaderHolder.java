package at.specure.android.screens.badges.newbadges.adapter;

import android.view.View;
import android.widget.TextView;

import com.specure.opennettest.R;

import at.specure.androidX.data.badges.BadgeCategoryItem;
import at.specure.util.tools.expandablerecyclerview.viewholders.ChildViewHolder;

public class BadgeGroupHeaderHolder extends ChildViewHolder {

    private TextView badge;
    private TextView badgeConditionType;

    public BadgeGroupHeaderHolder(View itemView) {
        super(itemView);
        badge = itemView.findViewById(R.id.badge);
        badgeConditionType = itemView.findViewById(R.id.badge_condition_type);
    }

    public void setValues(BadgeCategoryItem group) {
        badgeConditionType.setText(group.gainConditionTitle);
        badge.setText(R.string.badge);
    }
}
