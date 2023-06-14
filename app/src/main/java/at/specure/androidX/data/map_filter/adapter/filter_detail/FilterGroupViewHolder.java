package at.specure.androidX.data.map_filter.adapter.filter_detail;

import android.view.View;
import android.widget.TextView;

import com.specure.opennettest.R;

import at.specure.util.tools.expandablerecyclerview.viewholders.GroupViewHolder;

public class FilterGroupViewHolder extends GroupViewHolder {

    private TextView title;


    public FilterGroupViewHolder(View itemView) {
        super(itemView);
        title = itemView.findViewById(R.id.item_title);
    }

    public void setTitle(String titleText) {
        title.setText(titleText);
    }
}
