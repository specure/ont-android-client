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

package at.specure.android.screens.sync.request_code;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.ContextMenu;
import android.view.Menu;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.specure.opennettest.R;

import at.specure.android.api.calls.CheckSyncTask;
import at.specure.android.screens.main.MainActivity;
import at.specure.android.util.EndTaskListener;

/**
 * Controller for requesting code
 * Created by michal.cadrik on 10/31/2017.
 */

class RequestCodeController implements EndTaskListener {

    private RequestCodeInterface requestCodeInterface;
    private CheckSyncTask checkSyncTask;

    RequestCodeController(RequestCodeInterface requestCodeInterface) {
        this.requestCodeInterface = requestCodeInterface;
    }

    @Override
    public void taskEnded(JsonArray resultList) {
        if (resultList != null && resultList.size() > 0 && checkSyncTask != null && !checkSyncTask.hasError()) {
            for (int i = 0; i < resultList.size(); i++) {
                JsonObject resultListItem;
                try {
                    resultListItem = resultList.get(i).getAsJsonObject();
                    if (requestCodeInterface != null) {
                        requestCodeInterface.onCodeAcquired(resultListItem.get("sync_code").getAsString());
                    }
                } catch (final JsonParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    void copyCodeToClipboard(TextView codeText) {
        if (requestCodeInterface != null) {
            MainActivity rmbtActivity = requestCodeInterface.getRMBTActivity();
            if (rmbtActivity != null) {
                ClipboardManager clipboard = (ClipboardManager) rmbtActivity.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("sync_code", codeText.getText());
                clipboard.setPrimaryClip(clip);
            }
        }

    }

    void addCopyCodeToContextMenu(TextView view, ContextMenu menu) {
        menu.add(Menu.NONE, view.getId(), Menu.NONE, R.string.sync_request_code_context_copy);
    }

    void requestCode() {
        if (requestCodeInterface != null) {
            MainActivity rmbtActivity = requestCodeInterface.getRMBTActivity();
            if (rmbtActivity != null) {
                checkSyncTask = new CheckSyncTask(rmbtActivity);
                checkSyncTask.setEndTaskListener(this);
                checkSyncTask.execute("");
            }
        }
    }
}
