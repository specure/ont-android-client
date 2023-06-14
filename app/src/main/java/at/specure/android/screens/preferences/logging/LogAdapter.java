package at.specure.android.screens.preferences.logging;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

import com.specure.opennettest.R;

import at.specure.android.screens.preferences.OnItemClick;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.ViewHolder> {
    private List<String> mDataset;
    Activity activity;

    public void setData(List<String> logsList) {
        mDataset = logsList;
        this.notifyDataSetChanged();
    }


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // each data item is just a string in this case
        public TextView nameView;

        OnItemClick itemClickListener;

        public OnItemClick getItemClickListener() {
            return itemClickListener;
        }

        public void setItemClickListener(OnItemClick itemClickListener) {
            this.itemClickListener = itemClickListener;
        }

        public ViewHolder(View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.text_name);
        }


        @Override
        public void onClick(View v) {
            itemClickListener.onClick(v, getAdapterPosition());
        }
    }

    public LogAdapter(Activity myContext, List<String> values) {
        mDataset = values;
        activity = myContext;
    }

    @Override
    public LogAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.log_list_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String value = mDataset.get(position);
        holder.nameView.setText(value);
    }

    @Override
    public int getItemCount() {
        if (mDataset == null) {
            return 0;
        } else
            return mDataset.size();
    }
}
