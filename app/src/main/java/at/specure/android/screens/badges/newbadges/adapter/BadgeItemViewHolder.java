package at.specure.android.screens.badges.newbadges.adapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.specure.opennettest.R;

import at.specure.android.configs.BadgesConfig;
import at.specure.androidX.data.badges.Badge;
import at.specure.util.tools.expandablerecyclerview.viewholders.ChildViewHolder;

public class BadgeItemViewHolder extends ChildViewHolder implements View.OnClickListener {

    private TextView badgeName;
    private ImageView badgeState;
    private TextView badgeConditionValue;
    private OnBadgeClickListener clickListener;
    private Badge badge;

    public BadgeItemViewHolder(View itemView) {
        super(itemView);

        badgeConditionValue = itemView.findViewById(R.id.badge_condition_value);
        badgeState = itemView.findViewById(R.id.badge_gained_image_view);
        badgeName = itemView.findViewById(R.id.badge_name);
        itemView.setOnClickListener(this);
    }

    public void setValues(Badge badge, Context context) {
        badgeName.setText(badge.title);
        this.badge = badge;
        boolean badgeReceived = BadgesConfig.isBadgeReceived(badge, context);

        Drawable drawable = ContextCompat.getDrawable(context, badgeReceived ? R.drawable.tick : R.drawable.prize);
        drawable.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(context, R.color.app_tint_antenna_image_color),PorterDuff.Mode.SRC_ATOP));
        badgeState.setImageDrawable(drawable);
        badgeConditionValue.setText(badge.getCriteriaForDisplay());
    }

    public void setOnClickListener(OnBadgeClickListener listener) {
        clickListener = listener;
    }

    @Override
    public void onClick(View view) {
        if (clickListener != null)
            clickListener.onBadgeClicked(badge);
    }
}