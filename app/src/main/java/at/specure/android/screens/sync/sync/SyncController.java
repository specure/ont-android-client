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

package at.specure.android.screens.sync.sync;

import com.specure.opennettest.R;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import at.specure.android.configs.ConfigHelper;
import at.specure.android.screens.main.MainActivity;
import at.specure.android.screens.sync.enter_code.SyncEnterCodeFragment;
import at.specure.android.screens.sync.request_code.SyncRequestCodeFragment;


/**
 * Sync controller for sync
 * Created by michal.cadrik on 10/31/2017.
 */

class SyncController {

    private SyncInterface syncInterface;

    SyncController(SyncInterface syncInterface) {
        this.syncInterface = syncInterface;
    }

    void onBackPress() {
        if (this.syncInterface != null) {
            MainActivity rmbtActivity = syncInterface.getRMBTActivity();
            if (rmbtActivity != null) {
                ConfigHelper.setHistoryIsDirty(rmbtActivity, true);
                rmbtActivity.checkSettings(true, null);
                rmbtActivity.setSettings(null, null);
            }
        }
    }

    void requestCodeAction() {
        if (syncInterface != null) {
            MainActivity rmbtActivity = syncInterface.getRMBTActivity();
            if (rmbtActivity != null) {
                final FragmentManager fm = rmbtActivity.getSupportFragmentManager();
                FragmentTransaction ft;

                ft = fm.beginTransaction();
                ft.replace(R.id.fragment_content, new SyncRequestCodeFragment(), "sync_request_code");
                ft.addToBackStack("sync_request_code");
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.commit();
            }
        }
    }

    void enterCodeAction() {
        if (syncInterface != null) {
            MainActivity rmbtActivity = syncInterface.getRMBTActivity();
            if (rmbtActivity != null) {
                final FragmentManager fm = rmbtActivity.getSupportFragmentManager();
                FragmentTransaction ft;

                ft = fm.beginTransaction();
                ft.replace(R.id.fragment_content, new SyncEnterCodeFragment(), "sync_enter_code");
                ft.addToBackStack("sync_enter_code");
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                ft.commit();
            }
        }
    }
}
