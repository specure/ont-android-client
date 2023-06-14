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
package at.specure.android.screens.terms.terms_check;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.specure.opennettest.R;

import androidx.fragment.app.FragmentActivity;
import at.specure.android.base.BaseFragment;
import at.specure.android.configs.TermsAndConditionsConfig;
import at.specure.android.screens.main.MainActivity;
import at.specure.android.screens.terms.CheckType;
import at.specure.android.util.EndTaskListener;

public class TermsCheckFragment extends BaseFragment implements TermsCheckFragmentInterface {

    private boolean firstTime = true;
    private boolean checkedTermsFirstPart = false;

    private View view;

    private CheckType followedByType;
    private TermsCheckFragmentController termsCheckFragmentController;
    private WebView tcWvl;

    private TextView headerTextView;
    private TextView acceptanceTextView;

    public TermsCheckFragment() {

    }

    public static TermsCheckFragment getInstance(final CheckType followedBy) {
        TermsCheckFragment termsCheckFragment = new TermsCheckFragment();
        if (followedBy != null) {
            Bundle bundle = new Bundle();
            bundle.putInt("check_type", followedBy.ordinal());
            termsCheckFragment.setArguments(bundle);
        }
        return termsCheckFragment;
    }

//    private TermsCheckFragment(final CheckType followedBy) {
//        this.followedByType = followedBy;
//    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.terms_check, container, false);

        Bundle arguments = getArguments();
        if (arguments != null) {
            int checkType = arguments.getInt("check_type");
            followedByType = CheckType.values()[checkType];
        }

        termsCheckFragmentController = new TermsCheckFragmentController(this);

        final Activity activity = getActivity();
        if (!(activity instanceof MainActivity))
            firstTime = false;

        headerTextView = (TextView) view.findViewById(R.id.header);
        acceptanceTextView = (TextView) view.findViewById(R.id.termsAcceptText);
        tcWvl = view.findViewById(R.id.termsCheckWebViewLong);
        refreshUI();



        if (!firstTime)
            view.findViewById(R.id.termsButtonDecline).setVisibility(View.GONE);

        final Button buttonTermsAccept = view.findViewById(R.id.termsAcceptButton);
        buttonTermsAccept.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                CheckType type = followedByType;//termsCheckFragmentController.getType();

                if (type != null) {
                    switch (type) {
                        case TERMS_AND_PRIVACY:
                            if (checkedTermsFirstPart) {
                                termsCheckFragmentController.onAcceptTermsAction(activity, firstTime, followedByType);
                            } else {
                                checkedTermsFirstPart = true;
                                refreshUI();
                            }
                            break;
                        default:
                            termsCheckFragmentController.onAcceptTermsAction(activity, firstTime, followedByType);
                    }
                } else {
                    termsCheckFragmentController.onAcceptTermsAction(activity, firstTime, followedByType);
                }


            }
        });


        final Button buttonTermsDecline = view.findViewById(R.id.termsDeclineButton);
        buttonTermsDecline.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                onBackPressed();
            }
        });

        return view;
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void refreshUI() {
        WebSettings webSettings = tcWvl.getSettings();
        webSettings.setJavaScriptEnabled(true);
//            webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            webSettings.setAllowFileAccessFromFileURLs(true);
            webSettings.setAllowUniversalAccessFromFileURLs(true);
        }
        if (checkedTermsFirstPart) {
            TermsAndConditionsConfig.showPP(tcWvl, getActivity());
            headerTextView.setText(R.string.about_pp_title);
            acceptanceTextView.setText(R.string.pp_accept_text);
        } else {
            //tcWvl.loadUrl("file:///android_asset/terms_conditions_long.html");
            TermsAndConditionsConfig.showTaC(tcWvl, getActivity());
            headerTextView.setText(R.string.about_terms_title);
            acceptanceTextView.setText(R.string.terms_accept_text);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        FragmentActivity activity = getActivity();

        final boolean tcAccepted = termsCheckFragmentController.isAcceptedToC(activity);
        if (tcAccepted) {
            final TextView buttonTermsAccept = view.findViewById(R.id.termsAcceptButton);
            buttonTermsAccept.setText(R.string.terms_accept_button_continue);
            view.findViewById(R.id.termsAcceptText).setVisibility(View.GONE);
        }
    }

    public boolean onBackPressed() {
        // user has declined t+c!
        FragmentActivity activity = getActivity();
        termsCheckFragmentController.onRejectPressed(activity);
        activity.finish();
        return true;
    }

    @Override
    public void setActionBarItems(Context context) {
        super.setActionBarItems(context);
    }

    @Override
    public String setActionBarTitle() {
        if (this.isAdded())
            return getString(R.string.terms);
        else return "";
    }

    @Override
    public void checkSettings(boolean force, EndTaskListener endTaskListener) {
        FragmentActivity activity = getActivity();
        if (activity != null) {
            if (activity instanceof MainActivity) {
                ((MainActivity) activity).checkSettings(true, null);
            }
        }
    }

    @Override
    public boolean showChecksIfNecessary() {
        FragmentActivity activity = getActivity();
        if (activity != null) {
            if (activity instanceof MainActivity) {
                return ((MainActivity) activity).showChecksIfNecessary();
            }
        }
        return false;
    }

    @Override
    public void initApp(boolean duringCreate) {
        FragmentActivity activity = getActivity();
        if (activity != null) {
            if (activity instanceof MainActivity) {
                ((MainActivity) activity).initApp(duringCreate);
            }
        }
    }

    @Override
    public void showIcCheck() {
        FragmentActivity activity = getActivity();
        if (activity != null) {
            if (activity instanceof TermsActivity) {
                ((TermsActivity) activity).showIcCheck();
            }
        }
    }

    @Override
    public void showNdtCheck() {
        FragmentActivity activity = getActivity();
        if (activity != null) {
            if (activity instanceof TermsActivity) {
                ((TermsActivity) activity).showNdtCheck();
            }
        }
    }
}
