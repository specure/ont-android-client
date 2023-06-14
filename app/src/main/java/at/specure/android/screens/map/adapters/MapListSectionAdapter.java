/*******************************************************************************
 * Copyright 2014-2017 Specure GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package at.specure.android.screens.map.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import com.specure.opennettest.R;

import at.specure.android.screens.map.MapListEntry;
import at.specure.android.screens.map.MapListSection;

public class MapListSectionAdapter extends BaseAdapter {

    private final MapListSection mapListSection;
    private final Context context;

    public MapListSectionAdapter(final Context context, final MapListSection mapListSection) {
        this.context = context;
        this.mapListSection = mapListSection;
    }

    public MapListSection getMapListSection() {
        return mapListSection;
    }

    @Override
    public int getCount() {
        return mapListSection.getMapListEntryList().size();
    }

    @Override
    public Object getItem(final int position) {
        return mapListSection.getMapListEntryList().get(position);
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }


    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        if (convertView == null) {
            final LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            convertView = inflater.inflate(R.layout.map_filter_item, parent, false);
        }

        final TextView titleTextView = convertView.findViewById(R.id.title);
        final TextView summaryTextView = convertView.findViewById(R.id.summary);
        final RadioButton checkedTextView = convertView.findViewById(R.id.radiobutton);
        final MapListEntry entry = (MapListEntry) getItem(position);

        titleTextView.setText(entry.getTitle());
        summaryTextView.setText(entry.getSummary());
        checkedTextView.setChecked(entry.isChecked());

        return convertView;
    }
}
