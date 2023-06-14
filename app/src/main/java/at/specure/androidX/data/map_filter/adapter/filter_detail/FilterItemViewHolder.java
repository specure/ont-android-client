package at.specure.androidX.data.map_filter.adapter.filter_detail;

import android.view.View;
import android.widget.Checkable;
import android.widget.RadioButton;
import android.widget.TextView;

import com.specure.opennettest.R;

import at.specure.androidX.data.map_filter.view_data.FilterItem;
import at.specure.util.tools.expandablecheckrecyclerview.viewholders.CheckableChildViewHolder;

public class FilterItemViewHolder extends CheckableChildViewHolder {

    private TextView optionName;
    private RadioButton radioButton;

    public FilterItemViewHolder(View itemView) {
        super(itemView);
        optionName = itemView.findViewById(R.id.title);
        radioButton = itemView.findViewById(R.id.radiobutton);
    }

    @Override
    public Checkable getCheckable() {
        return radioButton;
    }

    public void onBind(FilterItem item) {
        optionName.setText(item.getTitle());
    }
}
