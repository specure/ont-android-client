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
package at.specure.android.screens.terms;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.specure.opennettest.R;

import java.util.Locale;

import at.specure.android.screens.main.MainActivity;
import at.specure.android.configs.ConfigHelper;

public class TermsCheckFragment extends Fragment
{
//    private static final String DEBUG_TAG = "TermsCheckFragment";
    
    private boolean firstTime = true;
    
    private View view;
    
    private CheckFragment.CheckType followedByType;

    private String deviceLanguage = Locale.getDefault().getLanguage();

    public static TermsCheckFragment getInstance(final CheckFragment.CheckType followedBy) {
    	return new TermsCheckFragment(followedBy);
    }

    public TermsCheckFragment() {

    }

    @SuppressLint("ValidFragment")
    private TermsCheckFragment(final CheckFragment.CheckType followedBy) {
    	this.followedByType = followedBy;
	}
    
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
    {
        view = inflater.inflate(R.layout.terms_check, container, false);
        

        final Activity activity = getActivity();
        if (! (activity instanceof MainActivity))
            firstTime = false;

        final WebView tcWvl = (WebView) view.findViewById(R.id.termsCheckWebViewLong);
        tcWvl.loadData(ConfigHelper.getTaCString(activity.getApplicationContext()),"text/html","utf-8");
        //tcWvl.loadUrl("https://www.meracinternetu.sk/sk/tc");

        if (! firstTime)
            view.findViewById(R.id.termsButtonDecline).setVisibility(View.GONE);
        
        final Button buttonTermsAccept = (Button) view.findViewById(R.id.termsAcceptButton);
        buttonTermsAccept.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(final View v)
            {
                ConfigHelper.setTCAccepted(getActivity(), true);
                if (firstTime)
                {
                    ((MainActivity)getActivity()).checkSettings(true, null);
                    final boolean wasNDTTermsNecessary = ((MainActivity)getActivity()).showChecksIfNecessary();
                    if (! wasNDTTermsNecessary)
                        ((MainActivity) activity).initApp(false);
                }
                else if (followedByType != null) {
                	switch (followedByType) {
                	case INFORMATION_COMMISSIONER:
                        ((TermsActivity)getActivity()).showIcCheck();
                		break;
                	case NDT:
                        ((TermsActivity)getActivity()).showNdtCheck();
                		break;
                	}
                }
            }
        });
        
        final Button buttonTermsDecline = (Button) view.findViewById(R.id.termsDeclineButton);
        buttonTermsDecline.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(final View v)
            {
                onBackPressed();
            }
        });
        
        return view;
    }
    
    @Override
    public void onResume()
    {
        super.onResume();
        final Activity activity = getActivity();
        final boolean tcAccepted = ConfigHelper.isTCAccepted(activity);
        if (tcAccepted)
        {
            final TextView buttonTermsAccept = (TextView) view.findViewById(R.id.termsAcceptButton);
            buttonTermsAccept.setText(R.string.terms_accept_button_continue);
            view.findViewById(R.id.termsAcceptText).setVisibility(View.GONE);
        }
    }

    public boolean onBackPressed()
    {
        // user has declined t+c!
        
        ConfigHelper.setTCAccepted(getActivity(), false);
        ConfigHelper.setUUID(getActivity(), "");
        getActivity().finish();
        return true;
    }
}
