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
package at.specure.android.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.specure.opennettest.R;

import androidx.fragment.app.FragmentActivity;
import at.specure.android.base.BaseFragment;
import at.specure.android.configs.ConfigHelper;
import at.specure.android.configs.TermsAndConditionsConfig;
import at.specure.android.constants.AppConstants;
import at.specure.android.screens.main.MainActivity;

public class PrivacyPolicyFragment extends BaseFragment {

    private WebView webview;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        Activity activity = getActivity();
        webview = new WebView(activity);
        /* JavaScript must be enabled if you want it to work, obviously */
        webview.getSettings().setJavaScriptEnabled(true);
        TermsAndConditionsConfig.showPP(webview, activity);
        webview.getSettings().setUserAgentString(AppConstants.getUserAgentString(getActivity()));
        webview.getSettings().setSupportZoom(true);
        webview.getSettings().setBuiltInZoomControls(true);
        return webview;
    }

    @Override
    public void onResume() {
        super.onResume();
        final FragmentActivity activity = getActivity();
        if (activity != null) {
            ((MainActivity) activity).updateTitle(setActionBarTitle());
            webview.setWebViewClient(new WebViewClient() {
                @Override
                public void onReceivedError(final WebView view, final int errorCode, final String description,
                                            final String failingUrl) {
                    super.onReceivedError(view, errorCode, description, failingUrl);
                    if (activity != null) {
                        webview.loadData(ConfigHelper.getErrorString(activity.getApplicationContext()), "text/html; charset=utf-8", "utf-8");
                    }
                }
            });
        }
    }

    @Override
    public String setActionBarTitle() {
        return getString(R.string.about_pp_title);
    }
}
