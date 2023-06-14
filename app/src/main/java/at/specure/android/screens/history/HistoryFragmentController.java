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

package at.specure.android.screens.history;

import android.content.res.Configuration;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;


import at.specure.android.screens.main.MainActivity;
import at.specure.android.screens.preferences.OnItemClick;
import timber.log.Timber;

public class HistoryFragmentController implements MainActivity.HistoryUpdatedCallback {

    private static final String DEBUG_TAG = "HistFragContr";
    private HistoryFragmentInterface fragmentInterface;

    public HistoryFragmentController(HistoryFragmentInterface fragmentInterface) {
        this.fragmentInterface = fragmentInterface;
    }


    @Override
    public void historyUpdated(int status) {
        if (fragmentInterface != null) {
            if (fragmentInterface.isVisible()) {
                MainActivity mainActivity = fragmentInterface.getMainActivity();
                int orientation = fragmentInterface.getOrientation();
                switch (status) {
                    case SUCCESSFUL:
                        HistoryAdapter historyAdapter = new HistoryAdapter(mainActivity, orientation, mainActivity.getHistoryItemList(), new OnItemClick() {
                            @Override
                            public void onClick(View v, int position) {
                                showHistoryPager(position);
                            }
                        });
                        /*ListAdapter historyList = new SimpleAdapter(mainActivity, mainActivity.getHistoryItemList(), orientation == Configuration.ORIENTATION_LANDSCAPE ? R.layout.history_item_land : R.layout.history_item,
                                new String[]{"device", "type", "date", "down", "up", "ping", "packet_loss", "jitter", "quality", "network_name"}, new int[]{R.id.device,
                                R.id.type, R.id.date, R.id.down, R.id.up, R.id.ping, R.id.packet_loss, R.id.jitter, R.id.quality, R.id.history_network_name});*/

                        fragmentInterface.dataSuccessfullyLoaded(historyAdapter);
                        break;

                    case LIST_EMPTY:
                        Timber.i("LIST_EMPTY");
                        fragmentInterface.dataSuccessfullyLoadedEmpty();
                        break;

                    case ERROR:
                        Timber.i("ERROR");
                        fragmentInterface.dataError();
                        break;
                }
            }
        }
    }

    public void updateHistory() {
        if (fragmentInterface != null) {
            MainActivity mainActivity = fragmentInterface.getMainActivity();
            if (mainActivity != null) {
                mainActivity.updateHistory(this);
            }
        }
    }

    public void showHistoryPager(int position) {
        if (fragmentInterface != null) {
            MainActivity mainActivity = fragmentInterface.getMainActivity();
            if (mainActivity != null) {
                mainActivity.showHistoryTestDetailPager(position);
            }
        }
    }
}
