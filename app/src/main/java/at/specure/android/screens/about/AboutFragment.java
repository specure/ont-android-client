/*
 Copyright 2013-2015 alladin-IT GmbH

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package at.specure.android.screens.about;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.specure.opennettest.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import at.specure.android.base.BaseFragment;
import at.specure.android.configs.PreferenceConfig;


public class AboutFragment extends BaseFragment implements AboutInterface {

    @SuppressWarnings("unused")
    private static final String DEBUG_TAG = "AboutFragment";
    private ListView listView;
    private long counter = 0;


    @Override
    public View onCreateView(final @NonNull LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.about, container, false);
        return createView(view, inflater);
    }

    @SuppressWarnings("UnusedParameters")
    private View createView(View view, LayoutInflater inflater) {
        final AboutController aboutController = new AboutController(this);
        listView = view.findViewById(R.id.aboutList);
        FragmentActivity activity = getActivity();
        if (activity != null) {
            if (aboutController.isLoopModeSecret(activity)) {
                ImageView icon = view.findViewById(R.id.headerImageBg);
                icon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        aboutController.showSecretOnClickAction();
                    }
                });
            }
            ImageView icon = view.findViewById(R.id.headerImageBg);
            icon.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (++counter == 3) {
                        PreferenceConfig.setLoggingEnabled(getContext(), true);
                        aboutController.openSettings(activity);
                    }
                    return false;
                }
            });
            ArrayList<HashMap<String, String>> listItems = aboutController.getListItems(activity);
            ListAdapter sa = new AboutAdapter(activity, listItems, R.layout.about_item, new String[]{"title", "text1", "text2"},
                    new int[]{R.id.title, R.id.text1, R.id.text2});
            listView.setAdapter(sa);
        }
        return view;
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        populateViewForOrientation(inflater, (ViewGroup) getView());
    }

    private void populateViewForOrientation(LayoutInflater inflater, ViewGroup view) {
        view.removeAllViewsInLayout();
        View v = inflater.inflate(R.layout.about, view);
        createView(v, inflater);
    }


    @Override
    public void setOnItemClickListenerForList(final List<AboutItem> aboutItems) {
        if (listView != null) {
            listView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    if ((aboutItems != null) && (aboutItems.size() > position)) {
                        aboutItems.get(position).action();
                    }
                }
            });
        }
    }

    @Override
    public String setActionBarTitle() {
        if (this.isAdded())
            return getString(R.string.page_title_about);
        else return "";
    }
}
