/*******************************************************************************
 * Copyright 2014-2017 Specure GmbH
 * Copyright 2013-2014 alladin-IT GmbH
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
package at.specure.android.util;

import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import android.view.inputmethod.InputMethodManager;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import at.specure.android.screens.main.InitialSetupInterface;

public class CustomAlertDialogFragment extends DialogFragment implements InitialSetupInterface {

    private String popBackStackIncluding;

    public static CustomAlertDialogFragment newInstance(final String title, final String message, final String popBackStackIncluding) {
        final CustomAlertDialogFragment frag = new CustomAlertDialogFragment();
        final Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("message", message);
        args.putString("popBackStackIncluding", popBackStackIncluding);
        frag.setArguments(args);
        return frag;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final @NonNull Bundle savedInstanceState) {
        final String title = getArguments().getString("title");
        final String message = getArguments().getString("message");
        popBackStackIncluding = getArguments().getString("popBackStackIncluding");

        final AlertDialog alert = new AlertDialog.Builder(getActivity())
                // .setIcon(R.drawable.alert_dialog_icon)
                .setTitle(title).setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .create();

        return alert;
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
//         close keyboard if open
        FragmentActivity activity = getActivity();
        try {
            final InputMethodManager imm = (InputMethodManager) activity
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
            if (popBackStackIncluding != null)
                activity.getSupportFragmentManager().popBackStack(popBackStackIncluding, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } catch (Exception e) {
        }
        super.onDismiss(dialog);
    }

    @Override
    public String setActionBarTitle() {
        return "";
    }

    @Override
    public void setActionBarItems(Context context) {

    }
}
