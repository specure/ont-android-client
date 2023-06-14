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
package at.specure.android.screens.terms.check_fragment;

import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.specure.opennettest.R;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import at.specure.android.base.BaseFragment;
import at.specure.android.screens.main.MainActivity;
import at.specure.android.screens.terms.CheckType;

public class CheckFragment extends BaseFragment implements CheckFragmentInterface {

    boolean firstTime = true;
    private CheckType checkType;
    private CheckBox checkBox;
    private CheckFragmentController checkFragmentController;

    public CheckFragment() {
    }

    public static CheckFragment newInstance(final CheckType checkType) {
        CheckFragment checkFragment = new CheckFragment();
        if (checkType != null) {
            Bundle bundle = new Bundle();
            bundle.putInt("check_type", checkType.ordinal());
            checkFragment.setArguments(bundle);
        }
        return checkFragment;
    }

    @Override
    public void onSaveInstanceState(final @NonNull Bundle b) {
        b.putBoolean("isChecked", checkBox.isChecked());
        super.onSaveInstanceState(b);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public View onCreateView(final @NonNull LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        if (!(getActivity() instanceof MainActivity))
            firstTime = false;

        Bundle arguments = this.getArguments();

        if (arguments != null) {
            int checkTypeInt = arguments.getInt("check_type");
            checkType = CheckType.values()[checkTypeInt];
        } /*else {
            checkType = CheckType.INFORMATION_COMMISSIONER;
        }*/


        checkFragmentController = new CheckFragmentController(this);

        View v = inflater.inflate(R.layout.ndt_check, container, false);
        TextView textTitle = v.findViewById(R.id.check_fragment_title);
        final Button buttonAccept = v.findViewById(R.id.termsNdtAcceptButton);
        final WebView wv = v.findViewById(R.id.ndtInfoWebView);

        textTitle.setText(checkType.getTitleId());

        checkBox = v.findViewById(R.id.ndtCheckBox);
        checkBox.setText(checkType.getTextId());

        if (savedInstanceState != null) {
            checkBox.setChecked(savedInstanceState.getBoolean("isChecked"));
        } else {
            checkBox.setChecked(checkType.isDefaultIsChecked());
        }

        if (!firstTime) {
            checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    buttonAccept.setEnabled(isChecked);
                }
            });
            v.findViewById(R.id.termsNdtButtonBack).setVisibility(View.GONE);
        }

        String htmlFileContent = checkFragmentController.getCheckHTMLFileContent(checkType, getContext());
        wv.loadData(htmlFileContent, "text/html; charset=utf-8", "utf-8");

        buttonAccept.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                FragmentActivity activity = getActivity();
                checkFragmentController.onAcceptAction(checkType, activity, checkBox.isChecked());
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                buttonAccept.setEnabled(firstTime || checkBox.isChecked());
            }
        }, 500);

        final Button buttonBack = v.findViewById(R.id.termsNdtBackButton);
        buttonBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                checkFragmentController.onBackAction(getActivity());
            }
        });

        return v;
    }

    @Override
    public String setActionBarTitle() {
        return "";
    }

    @Override
    public void closeFragment(boolean accepted) {
        FragmentActivity activity = getActivity();
        if (activity != null) {
            getActivity().getSupportFragmentManager().popBackStack(checkType.getFragmentTag(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
            if (firstTime && CheckType.INFORMATION_COMMISSIONER.equals(checkType))
                ((MainActivity) getActivity()).initApp(false);
            else {
                getActivity().setResult(checkBox.isChecked() ? FragmentActivity.RESULT_OK : FragmentActivity.RESULT_CANCELED);
                activity.finish();
            }
        }
    }
}
