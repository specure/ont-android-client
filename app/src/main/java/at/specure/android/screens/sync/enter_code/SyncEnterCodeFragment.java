/*******************************************************************************
 * Copyright 2014-2017 Specure GmbH
 * Copyright 2013-2015 alladin-IT GmbH
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
 ******************************************************************************/
package at.specure.android.screens.sync.enter_code;

import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.specure.opennettest.R;

import at.specure.android.base.BaseFragment;
import at.specure.android.screens.main.MainActivity;


public class SyncEnterCodeFragment extends BaseFragment implements EnterCodeInterface {

    @SuppressWarnings("unused")
    private static final String DEBUG_TAG = "SyncEnterCodeFragment";
    private EditText codeField;
    private Button syncButton;
    private LinearLayout overlay;
    private OnClickListener listener;
    private EnterCodeController controller;

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.sync_enter_code, container, false);
        syncButton = view.findViewById(R.id.button);
        overlay = view.findViewById(R.id.overlay);
        codeField = view.findViewById(R.id.code);
        controller = new EnterCodeController(this);
        listener = new OnClickListener() {
            @Override
            public void onClick(final View v) {
                controller.sendCode(codeField.getText().toString(), overlay, syncButton, codeField);
            }
        };
        codeField.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
                listener.onClick(v);
                return true;
            }
        });
        syncButton.setOnClickListener(listener);
        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        controller.stopTask();
    }

    public MainActivity getRMBTMainActivity() {
        return ((MainActivity) this.getActivity());
    }


    @Override
    public void onCheckSyncEnded(boolean success) {
        if (overlay != null) {
            overlay.setVisibility(View.GONE);
            overlay.setClickable(false);
        }
        if (codeField != null) {
            codeField.setClickable(true);
            if (success) {
                codeField.clearFocus();
            }
        }
        if (syncButton != null) {
            syncButton.setOnClickListener(listener);
            if (success) {
                syncButton.requestFocus();
            }

        }
    }

    @Override
    public String setActionBarTitle() {
        if (this.isAdded())
            return getString(R.string.page_title_sync);
        else return "";
    }
}
