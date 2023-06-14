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

package at.specure.android.screens.sync.enter_code;

import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.specure.opennettest.R;

import java.util.Locale;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import at.specure.android.api.calls.CheckSyncTask;
import at.specure.android.configs.ConfigHelper;
import at.specure.android.screens.main.MainActivity;
import at.specure.android.util.CustomAlertDialogFragment;
import at.specure.android.util.EndTaskListener;

/**
 * Controller for enter code fragment
 * Created by michal.cadrik on 10/30/2017.
 */

class EnterCodeController implements EndTaskListener {

    private EnterCodeInterface enterCodeInterface;
    private CheckSyncTask checkSyncTask;

    EnterCodeController(EnterCodeInterface enterCodeInterface) {
        this.enterCodeInterface = enterCodeInterface;
    }

    void sendCode(String s, View overlayView, View syncButton, EditText viewToShowError) {
        final String syncCode = s.toUpperCase(Locale.US);

        FragmentActivity activity = enterCodeInterface.getRMBTMainActivity();

        if (activity != null) {
            if (syncCode.length() == 12) {
                if (overlayView != null) {
                    overlayView.setVisibility(View.VISIBLE);
                    overlayView.setClickable(true);
                    overlayView.bringToFront();
                }
                syncButton.setOnClickListener(null);
                checkSyncTask = new CheckSyncTask(activity);
                checkSyncTask.setEndTaskListener(this);
                checkSyncTask.execute(syncCode);
            } else {
                viewToShowError.setError(activity.getString(R.string.sync_enter_code_length));
            }
        }
    }

    void stopTask() {
        if (checkSyncTask != null) {
            checkSyncTask.cancel(true);
        }
    }

    @Override
    public void taskEnded(final JsonArray resultList) {


        if (resultList != null && resultList.size() > 0 && (checkSyncTask != null) && !checkSyncTask.hasError()) {
            for (int i = 0; i < resultList.size(); i++) {

                if (enterCodeInterface != null) {
                    enterCodeInterface.onCheckSyncEnded(true);
                }

                JsonObject resultListItem;
                try {
                    resultListItem = resultList.get(i).getAsJsonObject();

                    String title = "";
                    if (resultListItem.has("msg_title")) {
                        title = resultListItem.get("msg_title").getAsString();
                    }

                    String text = "";
                    if (resultListItem.has("msg_text")) {
                        text = resultListItem.get("msg_text").getAsString();
                    }

                    Boolean success = false;
                    if (resultListItem.has("success")) {
                        success = resultListItem.get("success").getAsBoolean();
                    }

                    if (enterCodeInterface != null) {
                        MainActivity activity = enterCodeInterface.getRMBTMainActivity();
                        if (activity != null) {
                            if (!TextUtils.isEmpty(text)) {
                                String popBackStackIncluding = null;
                                if (success) {
                                    popBackStackIncluding = "sync";
                                    ConfigHelper.setHistoryIsDirty(activity, true);
                                    activity.setSettings(null, null);
                                    activity.checkSettings(true, null);
                                }
                                final DialogFragment newFragment = CustomAlertDialogFragment.newInstance(title, text, popBackStackIncluding);
                                newFragment.show(activity.getSupportFragmentManager(), "sync_msg");
                            }
                        }
                    }
                } catch (final JsonParseException e) {
                    e.printStackTrace();
                }

            }
        } else {
            if (enterCodeInterface != null) {
                enterCodeInterface.onCheckSyncEnded(false);
            }
        }
    }
}
