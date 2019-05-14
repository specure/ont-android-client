package at.specure.android.screens.preferences;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.specure.opennettest.R;

import at.specure.android.SupportedLocales;
import at.specure.android.configs.LocaleConfig;

public class LanguageAdapter extends RecyclerView.Adapter<LanguageAdapter.ViewHolder> {
    private SupportedLocales[] mDataset;
    Activity activity;


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // each data item is just a string in this case
        public TextView nameView;
        public ImageView selectedView;
        public ImageView flagView;

        OnItemClick itemClickListener;

        public OnItemClick getItemClickListener() {
            return itemClickListener;
        }

        public void setItemClickListener(OnItemClick itemClickListener) {
            this.itemClickListener = itemClickListener;
        }

        public ViewHolder(View itemView) {
            super(itemView);
            flagView = itemView.findViewById(R.id.flag);
            nameView = itemView.findViewById(R.id.text_name);
            selectedView = itemView.findViewById(R.id.image_selected);
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View v) {
            itemClickListener.onClick(v, getAdapterPosition());
        }
    }

    public LanguageAdapter(Activity myContext) {
        mDataset = SupportedLocales.values();
        activity = myContext;
    }

    @Override
    public LanguageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.language_list_item, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SupportedLocales locale = mDataset[position];

        holder.nameView.setText(locale.getLanguageName());
        if (locale.countryIcon != 0) {
            holder.flagView.setVisibility(View.VISIBLE);
            Glide.with(activity).load(locale.countryIcon).into(holder.flagView);
        } else {
            holder.flagView.setVisibility(View.INVISIBLE);
        }


        if (locale.id == LocaleConfig.getSelectedLanguage(holder.itemView.getContext()).id) {
            holder.selectedView.setVisibility(View.VISIBLE);
        } else {
            holder.selectedView.setVisibility(View.INVISIBLE);
        }

        holder.setItemClickListener(new OnItemClick() {
            @Override
            public void onClick(View v, int position) {
                LocaleConfig.setSelectedLanguage(activity,
                        mDataset[position].id);
                notifyDataSetChanged();
//                    if (controller != null) {
//                        controller.onServerSelectedClickAction(mDataset.get(position));
//                    }

            }
        });

    }

    @Override
    public int getItemCount() {
        return mDataset.length;
    }
}
