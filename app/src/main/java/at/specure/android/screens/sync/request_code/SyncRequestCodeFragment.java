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
package at.specure.android.screens.sync.request_code;


import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.specure.opennettest.R;

import at.specure.android.base.BaseFragment;
import at.specure.android.screens.main.MainActivity;


public class SyncRequestCodeFragment extends BaseFragment implements RequestCodeInterface {

    @SuppressWarnings("unused")
    private static final String DEBUG_TAG = "SyncRequestCodeFragment";
    private TextView codeText;
    private RequestCodeController requestCodeController;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestCodeController = new RequestCodeController(this);
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sync_request_code, container, false);
        codeText = view.findViewById(R.id.code);
        requestCodeController.requestCode();
        return view;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo contextMenuInfo) {
        if (view instanceof TextView && view == codeText)
            requestCodeController.addCopyCodeToContextMenu(codeText, menu);
        else
            super.onCreateContextMenu(menu, view, contextMenuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem mi) {
        if (mi.getItemId() == codeText.getId()) {
            requestCodeController.copyCodeToClipboard(codeText);
            return true;
        }
        return super.onContextItemSelected(mi);
    }

    @Override
    public MainActivity getRMBTActivity() {
        return (MainActivity) getActivity();
    }

    @Override
    public void onCodeAcquired(String code) {
        codeText.setText(code);
        registerForContextMenu(codeText);
    }

    @Override
    public String setActionBarTitle() {
        if (this.isAdded())
            return getString(R.string.page_title_sync);
        else return "";
    }
}
