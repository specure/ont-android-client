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

package at.specure.android.screens.main;

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

import at.specure.android.api.jsons.MeasurementServer;

/**
 * Created by michal.cadrik on 7/31/2017.
 */

public class MeasurementServersAdapter extends ArrayAdapter<MeasurementServer> {

    Activity activity;
    int resource;

    public MeasurementServersAdapter(@NonNull Context context, @LayoutRes int resource, @IdRes int textViewResourceId, @NonNull List<MeasurementServer> objects, Activity activity) {
        super(context, resource, textViewResourceId, objects);
        this.resource = resource;
        this.activity = activity;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
//        View view = super.getView(position, convertView, parent);
        View view;
        TextView text;
        if (convertView != null) {
            view = convertView;
            text = (TextView) convertView.findViewById(R.id.text);

        } else {
            LayoutInflater layoutInflater = activity.getLayoutInflater();
            view = layoutInflater.inflate(resource, null);
            text = (TextView) view.findViewById(R.id.text);
        }
        text.setText(this.getItem(position).getName());
        return view;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        LayoutInflater inflater = activity.getLayoutInflater();
        View row = inflater.inflate(R.layout.test_server_dropdown_item, parent, false);
        TextView text = (TextView) row.findViewById(R.id.text);
        text.setText(this.getItem(position).getName());
        return row;
    }

}
