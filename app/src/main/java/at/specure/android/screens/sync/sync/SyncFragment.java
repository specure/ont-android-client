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
package at.specure.android.screens.sync.sync;

import android.content.res.Configuration;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.specure.opennettest.R;

import at.specure.android.base.BaseFragment;
import at.specure.android.screens.main.MainActivity;

public class SyncFragment extends BaseFragment implements SyncInterface {

    private SyncController syncController;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        FrameLayout frameLayout = new FrameLayout(getActivity());
        populateViewForOrientation(inflater, frameLayout);
        syncController = new SyncController(this);
        return frameLayout;
    }

    public void populateViewForOrientation(final LayoutInflater inflater, final ViewGroup container) {
        container.removeAllViewsInLayout();
        final View view = inflater.inflate(R.layout.sync, container);
        final Button buttonRequestCode = view.findViewById(R.id.requestCodeButton);
        final Button buttonEnterCode = view.findViewById(R.id.enterCodeButton);

        buttonRequestCode.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (syncController == null) {
                    syncController = new SyncController(SyncFragment.this);
                }
                syncController.requestCodeAction();
            }
        });


        buttonEnterCode.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (syncController == null) {
                    syncController = new SyncController(SyncFragment.this);
                }
                syncController.enterCodeAction();
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        populateViewForOrientation(inflater, (ViewGroup) getView());
    }

    public boolean onBackPressed() {
        if (syncController == null) {
            syncController = new SyncController(SyncFragment.this);
        }
        this.syncController.onBackPress();
        return false;
    }

    @Override
    public MainActivity getRMBTActivity() {
        return ((MainActivity) getActivity());
    }


    @Override
    public String setActionBarTitle() {
        if (this.isAdded())
            return getString(R.string.page_title_sync);
        else return "";
    }
}
