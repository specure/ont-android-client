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
package at.specure.android.util;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.Locale;

import at.specure.android.JsonMapConfig;
import at.specure.android.configs.ConfigHelper;
import at.specure.android.constants.AppConstants;

public class TermsFragment extends Fragment
{
    
    // private static final String DEBUG_TAG = "TermsFragment";
    
    private WebView webview;
    
    private Activity activity;

    private JsonMapConfig urlTAC = null;// = new JsonMapConfig("file:///android_asset/tac_links.json");

    private String deviceLanguage = Locale.getDefault().getLanguage();

// Deactivated after discussion with Jozef, new logic is to always display embedded TAC file
//    public TermsFragment() {
//        //Uri filePAth = new Uri("file:///android_asset/tac_links.json");
//        try {
//
//            InputStream is = this.getClass().getClassLoader().getResourceAsStream("assets/tac_links.json");
//            // JSonMapConfigFile closes the io stream
//            urlTAC = new JsonMapConfig(is);
//        } catch (IOException e) {
//            //webview.loadUrl("file:///android_asset/internal_error.html");
//            Log.d("Debug:", e.getLocalizedMessage());
//        } catch (JSONException e) {
//            //webview.loadUrl("file:///android_asset/internal_error.html");
//            //Log.d("Debug:", e.getLocalizedMessage());
//        }
//    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
    {
        activity = getActivity();
        
        webview = new WebView(activity);

        /* JavaScript must be enabled if you want it to work, obviously */
        webview.getSettings().setJavaScriptEnabled(true);

        webview.loadData(ConfigHelper.getTaCString(activity.getApplicationContext()),"text/html","utf-8");

        webview.getSettings().setUserAgentString(AppConstants.getUserAgentString(getActivity()));
        webview.getSettings().setSupportZoom(true);
        webview.getSettings().setBuiltInZoomControls(true);
        
        webview.setWebViewClient(new WebViewClient()
        {
            @Override
            public void onReceivedError(final WebView view, final int errorCode, final String description,
                    final String failingUrl)
            {
                super.onReceivedError(view, errorCode, description, failingUrl);
                webview.loadData(ConfigHelper.getErrorString(activity.getApplicationContext()),"text/html","utf-8");
            }
        });

//        if (urlTAC != null) {
//            if (urlTAC.getProperty(deviceLanguage) != null)
//                webview.loadUrl(urlTAC.getProperty(deviceLanguage));
//            // TODO - find default language and show it instead of error
//            else
//                webview.loadUrl("file:///android_asset/tc.html       else
//            webview.loadUrl("file:///android_asset/internal_error.html");

        return webview;
    }
    
}
