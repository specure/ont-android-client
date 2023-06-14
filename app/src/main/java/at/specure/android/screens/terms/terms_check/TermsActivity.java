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

import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.specure.opennettest.R;

import androidx.fragment.app.FragmentTransaction;
import at.specure.android.constants.AppConstants;
import at.specure.android.screens.main.BasicActivity;
import at.specure.android.screens.terms.CheckType;
import at.specure.android.screens.terms.check_fragment.CheckFragment;

public class TermsActivity extends BasicActivity {
    public final static String EXTRA_KEY_CHECK_TYPE = "check_type";
    private CheckType checkType = null;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        if (getIntent().getExtras() != null) {
            final String checkTypeName = getIntent().getExtras().getString(EXTRA_KEY_CHECK_TYPE, null);
            if (checkTypeName != null) {
                checkType = CheckType.valueOf(checkTypeName);
            }
        }

        final Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
        window.addFlags(WindowManager.LayoutParams.FLAG_DITHER);

        showTermsCheck();
    }

    public void showTermsCheck() {
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_content, TermsCheckFragment.getInstance(checkType), "terms_check");
        ft.commit();
    }

    public void showIcCheck() {
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_content, CheckFragment.newInstance(CheckType.INFORMATION_COMMISSIONER), AppConstants.PAGE_TITLE_CHECK_INFORMATION_COMMISSIONER);
        ft.addToBackStack(AppConstants.PAGE_TITLE_CHECK_INFORMATION_COMMISSIONER);
        ft.commit();
    }

    public void showNdtCheck() {
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_content, CheckFragment.newInstance(CheckType.NDT), AppConstants.PAGE_TITLE_NDT_CHECK);
        ft.addToBackStack(AppConstants.PAGE_TITLE_NDT_CHECK);
        ft.commit();
    }
}
