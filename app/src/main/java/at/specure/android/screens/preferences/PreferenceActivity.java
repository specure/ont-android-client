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
package at.specure.android.screens.preferences;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.specure.opennettest.R;

import at.specure.android.configs.ConfigHelper;
import at.specure.android.configs.LocaleConfig;
import at.specure.android.screens.main.BasicActivity;


public class PreferenceActivity extends BasicActivity {

    static final int REQUEST_NDT_CHECK = 1;
    static final int REQUEST_IC_CHECK = 2;

    private PreferenceFragment mPrefsFragment;
    private boolean shouldFinish = false;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        shouldFinish = getIntent().getBooleanExtra("finish", false);


        if (LocaleConfig.isUserAbleToChangeLanguage(this)) {
            if (savedInstanceState == null) {
                LocaleConfig.initializeApp(this, false);
            }
        }

        setContentView(R.layout.settings_activity);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar supportActionBar = getDelegate().getSupportActionBar();
        supportActionBar.setTitle(R.string.page_title_settings);
        if (supportActionBar != null) {
            supportActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        FragmentManager supportFragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = supportFragmentManager
                .beginTransaction();
        mPrefsFragment = new PreferenceFragment();
        fragmentTransaction.replace(R.id.preference_activity__content, mPrefsFragment);
        fragmentTransaction.commit();

    }


    @Override
    protected void onStart() {
        super.onStart();
        if (shouldFinish) {
            this.finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mPrefsFragment != null) {
            if (requestCode == REQUEST_NDT_CHECK) {
                mPrefsFragment.setChecked("ndt", ConfigHelper.isNDT(this));
            } else if (requestCode == REQUEST_IC_CHECK) {
                mPrefsFragment.setChecked("information_commissioner", ConfigHelper.isInformationCommissioner(this));
            }
        }
    }

}
