package at.specure.android.screens.badges.newbadges.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.specure.opennettest.R;

import at.specure.util.tools.expandablerecyclerview.listeners.OnGroupClickListener;
import at.specure.util.tools.expandablerecyclerview.viewholders.GroupViewHolder;

public class BadgeGroupViewHolder extends GroupViewHolder {

    private TextView groupType;
    private ImageView groupExpanded;

    public BadgeGroupViewHolder(View itemView) {
        super(itemView);
        groupType = itemView.findViewById(R.id.badges_group_name);
        groupExpanded = itemView.findViewById(R.id.group_expanded);
    }

    public void setGroupTitle(String name) {
        groupType.setText(name);
    }

    @Override
    public void setOnGroupClickListener(OnGroupClickListener listener) {
        super.setOnGroupClickListener(listener);
    }

    @Override
    public void expand() {
        super.expand();
        groupExpanded.setRotation(180f);
    }

    @Override
    public void collapse() {
        super.collapse();
        groupExpanded.setRotation(0f);
    }

}

