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
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.specure.opennettest.R;

import java.util.List;

import at.specure.android.api.jsons.MapFilterCountry;

/**
 * Adapter for dropdown (spinner in map filters)
 * Created by michal.cadrik on 10/26/2017.
 */

public class CountriesAdapter extends ArrayAdapter<MapFilterCountry> {

    private Activity activity;
    private int resource;

    public CountriesAdapter(@NonNull Context context, @LayoutRes int resource, @IdRes int textViewResourceId, @NonNull List<MapFilterCountry> objects, Activity activity) {
        super(context, resource, textViewResourceId, objects);
        this.resource = resource;
        this.activity = activity;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;
        TextView text;
        if (convertView != null) {
            view = convertView;
            text = convertView.findViewById(android.R.id.title);

        } else {
            LayoutInflater layoutInflater = activity.getLayoutInflater();
            view = layoutInflater.inflate(resource, null);
            text = view.findViewById(android.R.id.title);
        }

        MapFilterCountry item = this.getItem(position);
        if (item != null) {
            text.setText(item.getCountryName());
        }

        return view;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        super.getView(position, convertView, parent);
        LayoutInflater inflater = activity.getLayoutInflater();
        View row = inflater.inflate(R.layout.preferences_item, parent, false);
        TextView text = row.findViewById(android.R.id.title);
        MapFilterCountry item = this.getItem(position);
        if (item != null) {
            text.setText(item.getCountryName());
        }
        return row;
    }

}
