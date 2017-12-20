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

package at.specure.android.screens.preferences;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ListView;


import com.specure.opennettest.R;

import at.specure.android.screens.terms.CheckFragment;
import at.specure.android.screens.terms.TermsActivity;
import at.specure.android.configs.ConfigHelper;

import static at.specure.android.screens.preferences.PreferenceActivity.REQUEST_IC_CHECK;
import static at.specure.android.screens.preferences.PreferenceActivity.REQUEST_NDT_CHECK;

/**
 * Settings fragment
 * Created by michal.cadrik on 11/2/2017.
 */

public class PreferenceFragment extends android.preference.PreferenceFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (ConfigHelper.isSecretEntered(this.getActivity())) {
            addPreferencesFromResource(R.xml.preferences_after_secret);
        } else {
            addPreferencesFromResource(R.xml.preferences);
            if (ConfigHelper.isDevEnabled(this.getActivity()))
                addPreferencesFromResource(R.xml.preferences_dev);
        }

        final ListView v = view.findViewById(android.R.id.list);
        v.setCacheColorHint(0);

        final Preference ndtPref = findPreference("ndt");
        if (ndtPref != null) {
            ndtPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (preference instanceof CheckBoxPreference) {
                        final CheckBoxPreference cbp = (CheckBoxPreference) preference;

                        if (cbp.isChecked()) {
                            cbp.setChecked(false);
                            final Intent intent = new Intent(PreferenceFragment.this.getActivity(), TermsActivity.class);
                            intent.putExtra(TermsActivity.EXTRA_KEY_CHECK_TYPE, CheckFragment.CheckType.NDT.name());
                            startActivityForResult(intent, REQUEST_NDT_CHECK);
                        }
                    }
                    return true;
                }
            });
        }

        final Preference icPref = findPreference("information_commissioner");
        if (icPref != null) {
            if (getResources().getBoolean(R.bool.test_use_personal_data_fuzzing)) {
                icPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        if (preference instanceof CheckBoxPreference) {
                            final CheckBoxPreference cbp = (CheckBoxPreference) preference;

                            if (cbp.isChecked()) {
                                cbp.setChecked(false);
                                final Intent intent = new Intent(PreferenceFragment.this.getActivity(), TermsActivity.class);
                                intent.putExtra(TermsActivity.EXTRA_KEY_CHECK_TYPE, CheckFragment.CheckType.INFORMATION_COMMISSIONER.name());
                                startActivityForResult(intent, REQUEST_IC_CHECK);
                            }
                        }
                        return true;
                    }
                });
            } else {
                final PreferenceCategory cat = (PreferenceCategory) findPreference("preference_category_test");
                cat.removePreference(icPref);
            }
        }

        final Preference gpsPref = findPreference("location_settings");
        if (gpsPref != null) {
            gpsPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    return true;
                }
            });
        }
    }

    public void setChecked(String preferenceString, boolean checked) {
        ((CheckBoxPreference) findPreference(preferenceString)).setChecked(checked);
    }
}
