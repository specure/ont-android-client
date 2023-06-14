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
package at.specure.android.screens.help;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.specure.opennettest.R;

import androidx.fragment.app.FragmentActivity;
import at.specure.android.base.BaseFragment;
import at.specure.android.configs.ConfigHelper;
import at.specure.android.constants.AppConstants;
import timber.log.Timber;

public class HelpFragment extends BaseFragment {

    public static final String ARG_URL = "url";
    public static final String ARG_TITLE = "title";
    private WebView webview;
    private String url;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        final Bundle args = getArguments();
        url = args.getString(ARG_URL);
        final Activity activity = getActivity();

        webview = new WebView(activity) {
            @Override
            public boolean onKeyDown(final int keyCode, final KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && canGoBack()) {
                    goBack();
                    return true;
                }
                return super.onKeyDown(keyCode, event);
            }
        };

        final WebSettings webSettings = webview.getSettings();
        final String userAgent = AppConstants.getUserAgentString(getActivity());
        if (userAgent != null) {
            webSettings.setUserAgentString(userAgent);
        }
        webSettings.setJavaScriptEnabled(true);
        return webview;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (url == null || url.length() == 0)
            url = this.getString(R.string.url_help);

        final FragmentActivity activity = getActivity();

        webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(final WebView view, final int errorCode, final String description,
                                        final String failingUrl) {
                Timber.w( "error code:%s" , errorCode);
                Timber.d( "error desc:%s" , description);
                Timber.d( "error url: %s" , failingUrl);
                if (activity != null) {
                    webview.loadData(ConfigHelper.getErrorString(activity), "text/html; charset=utf-8", "utf-8");
                }
                super.onReceivedError(view, errorCode, description, failingUrl);
            }
        });

        webview.setDownloadListener(new DownloadListener() {
            public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimetype,
                                        long contentLength) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                if (isAdded() && getActivity() != null) {
                    startActivity(i);
                }
            }
        });

        if (!url.matches("^https?://.*")) {
            final String protocol = ConfigHelper.isControlSeverSSL(activity) ? "https" : "http";
            url = protocol + "://" + url;
        }

        webview.loadUrl(url);

    }

    @Override
    public String setActionBarTitle() {
        if (this.isAdded()) {
            int resourceId = getArguments().getInt(ARG_TITLE, R.string.page_title_help);
            return getString(resourceId);
        }
        else return "";
    }
}
