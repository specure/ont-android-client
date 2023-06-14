package at.specure.android.screens.history;

import android.app.Activity;
import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.specure.opennettest.R;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import at.specure.android.SupportedLocales;
import at.specure.android.configs.LocaleConfig;
import at.specure.android.screens.preferences.OnItemClick;
import at.specure.androidX.data.history.HistoryItem;
import at.specure.androidX.data.jitter.JitterPacketLossHistory;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private List<HistoryItem> mDataset;
    Activity activity;
    int orientation = Configuration.ORIENTATION_PORTRAIT;
    OnItemClick  itemClickListener;


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // each data item is just a string in this case
        public TextView deviceTV;
        public TextView typeTV;
        public TextView dateTV;
        public TextView downTV;
        public TextView upTV;
        public TextView pingTV;
        public TextView packetLossTV;
        public TextView jitterTV;
        public TextView qosTV;
        public TextView networkNameTV;

        OnItemClick itemClickListener;

        public OnItemClick getItemClickListener() {
            return itemClickListener;
        }

        public void setItemClickListener(OnItemClick itemClickListener) {
            this.itemClickListener = itemClickListener;
        }

        public ViewHolder(View itemView) {
            super(itemView);
            deviceTV = itemView.findViewById(R.id.device);
            typeTV = itemView.findViewById(R.id.type);
            dateTV = itemView.findViewById(R.id.date);
            downTV = itemView.findViewById(R.id.down);
            upTV = itemView.findViewById(R.id.up);
            pingTV = itemView.findViewById(R.id.ping);
            packetLossTV = itemView.findViewById(R.id.packet_loss);
            jitterTV = itemView.findViewById(R.id.jitter);
            qosTV = itemView.findViewById(R.id.quality);
            networkNameTV = itemView.findViewById(R.id.history_network_name);

            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            itemClickListener.onClick(v, getAdapterPosition());
        }
    }

    public HistoryAdapter(Activity myContext, int orientation, List<HistoryItem> data, OnItemClick onItemClickListener) {
        mDataset = data;
        activity = myContext;
        this.orientation = orientation;
        this.itemClickListener = onItemClickListener;
    }

    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(orientation == Configuration.ORIENTATION_LANDSCAPE ? R.layout.history_item_land : R.layout.history_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        HistoryItem item = mDataset.get(position);

        holder.deviceTV.setText(item.getModel());
        holder.dateTV.setText(item.getDate());
        holder.typeTV.setText(item.getNetworkType());
        holder.downTV.setText(item.getSpeedDownload());
        holder.upTV.setText(item.getSpeedUpload());
        holder.pingTV.setText(item.getPing());
        JitterPacketLossHistory jitterAndPacketLoss = item.getJitterAndPacketLoss();
        if (jitterAndPacketLoss != null) {
            holder.packetLossTV.setText(jitterAndPacketLoss.getPacketLossResult());
            holder.jitterTV.setText(jitterAndPacketLoss.getJitterResult());
        } else {
            holder.packetLossTV.setText(" - ");
            holder.jitterTV.setText(" - ");
        }

        holder.qosTV.setText(item.getQosResultPercentage());
        holder.networkNameTV.setText(item.getNetworkName());
        holder.setItemClickListener(itemClickListener);
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
