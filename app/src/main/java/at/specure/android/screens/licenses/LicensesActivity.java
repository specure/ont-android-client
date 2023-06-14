/*******************************************************************************
 * Copyright 2014-2017 Specure GmbH
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
 *******************************************************************************/

package at.specure.android.screens.licenses;

import android.os.Bundle;

import android.util.Base64;
import android.view.MenuItem;
import android.webkit.WebView;

import com.specure.opennettest.R;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import at.specure.android.configs.ConfigHelper;
import at.specure.android.configs.LocaleConfig;
import at.specure.android.screens.main.BasicActivity;


public class LicensesActivity extends BasicActivity {

    private WebView webView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (LocaleConfig.isUserAbleToChangeLanguage(this)) {
            if (savedInstanceState == null) {
                LocaleConfig.initializeApp(this, false);
            }
        }

        setContentView(R.layout.activity_licenses);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.title_activity_licenses);

//        toolbar.setNavigationIcon(R.drawable.backbtn_back);


        setSupportActionBar(toolbar);
        ActionBar supportActionBar = getSupportActionBar();
//
//        supportActionBar.setDisplayShowTitleEnabled(true);
        supportActionBar.setHomeButtonEnabled(true);
        supportActionBar.setDisplayHomeAsUpEnabled(true);
//        supportActionBar.setTitle(R.string.title_activity_licenses);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == android.R.id.home) {
                    finish();
                }
                return true;
            }
        });

        webView = findViewById(R.id.licenses__webview);
        String htmlContent = ConfigHelper.getLicensesString(this);
        String encodedHtml = Base64.encodeToString(htmlContent.getBytes(), Base64.NO_PADDING);
        webView.loadData(encodedHtml, "text/html", "base64");

//        webView.loadDataWithBaseURL(null, ConfigHelper.getLicensesString(this), "text/html; charset=utf-8", "utf-8", null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
