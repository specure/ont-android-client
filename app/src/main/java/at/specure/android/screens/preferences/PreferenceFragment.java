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

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.specure.opennettest.R;

import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import at.specure.android.configs.ConfigHelper;
import at.specure.android.configs.LocaleConfig;
import at.specure.android.configs.PreferenceConfig;
import at.specure.android.screens.preferences.preferences.ExtendedDialogPreferenceCompat;
import at.specure.android.screens.terms.CheckFragment;
import at.specure.android.screens.terms.TermsActivity;
import at.specure.android.util.location.GeoLocationX;
import timber.log.Timber;

import static at.specure.android.screens.preferences.PreferenceActivity.REQUEST_IC_CHECK;
import static at.specure.android.screens.preferences.PreferenceActivity.REQUEST_NDT_CHECK;

/**
 * Settings fragment
 * Created by michal.cadrik on 11/2/2017.
 */

public class PreferenceFragment extends PreferenceFragmentCompat {


    private final static String TAG = PreferenceFragment.class.getName();
    public final static String SETTINGS_SHARED_PREFERENCES_FILE_NAME = TAG + ".preferences_file";

    private CheckBoxPreference adsPersonalisation;
    private Loader<Boolean> optAddLoader;
    private boolean callFromLoader = false;
    private ListView v;
    private PreferenceManager preferenceManager;
    private int counter = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        if (preference instanceof ExtendedDialogPreferenceCompat) {
            ((ExtendedDialogPreferenceCompat) preference).showDialog(null);
        } else
            super.onDisplayPreferenceDialog(preference);
    }


//    @Override
//    public void onDisplayPreferenceDialog(Preference preference) {
//
//        PreferenceDialogFragmentCompat fragment;
//        if (preference instanceof LoopMovementPickerPreference) {
//            fragment = LoopMovementDialogFragmentCompat.newInstance((LoopMovementPickerPreference) preference);
//            fragment.setTargetFragment(this, 0);
//            fragment.show(getFragmentManager(),
//                    "android.support.v7.preference.PreferenceFragment.DIALOG");
//        }
//        super.onDisplayPreferenceDialog(preference);
//    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        // Define the settings file to use by this settings fragment
        preferenceManager = getPreferenceManager();
        preferenceManager.setSharedPreferencesName(SETTINGS_SHARED_PREFERENCES_FILE_NAME);

        if (ConfigHelper.isSecretEntered(this.getActivity())) {
            addPreferencesFromResource(R.xml.preferences_after_secret);
        } else {
            addPreferencesFromResource(R.xml.preferences);
            if (ConfigHelper.isDevEnabled(this.getActivity()))
                addPreferencesFromResource(R.xml.preferences_dev);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
                            getActivity().startActivityForResult(intent, REQUEST_NDT_CHECK);
                        }
                    }
                    return true;
                }
            });
        }

        final Preference languagePref = findPreference("chosen_language_id");
        if (languagePref != null) {
            if (!LocaleConfig.isUserAbleToChangeLanguage(getActivity())) {
                languagePref.setEnabled(false);
            }
            languagePref.setSummary(LocaleConfig.getSelectedLanguage(getActivity()).getLanguageName());
            languagePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    LanguagesHandler.showSupportedLanguages(getActivity());
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
                                getActivity().startActivityForResult(intent, REQUEST_IC_CHECK);
                            }
                        }
                        return true;
                    }
                });
            } else {
                final PreferenceCategory cat2 = (PreferenceCategory) findPreference("preference_category_test");
                cat2.removePreference(icPref);
            }
        }

        final Preference gpsPref = findPreference("location_settings");
        if (gpsPref != null) {
            gpsPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    try {
                        GeoLocationX.openGeolocationSettings(getActivity(), false);
                        return true;
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(PreferenceFragment.this.getActivity(), R.string.not_able_to_open_gps_settings, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }
            });
        }

        final Preference connectionDetailsPref = findPreference("connection_details");
        if (!Build.MANUFACTURER.contentEquals("Amazon")) {
            if (connectionDetailsPref != null) {
                connectionDetailsPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setClassName("com.android.settings", "com.android.settings.RadioInfo");
                            startActivity(intent);
                            return true;
                        } catch (ActivityNotFoundException | SecurityException e) {
                            Toast.makeText(PreferenceFragment.this.getActivity(), R.string.preferences_connection_details_not_supported, Toast.LENGTH_SHORT).show();
                            return false;
                        }
                    }
                });
            }
        }


        final Preference zeroMeasurementPref = findPreference("enabled_zero_measurements_in_preferences");
        if (zeroMeasurementPref != null) {
            if (!ConfigHelper.showZeroMeasurementsPreference(PreferenceFragment.this.getActivity())) {
                final PreferenceCategory cat2 = (PreferenceCategory) findPreference("preference_category_test");
                cat2.removePreference(zeroMeasurementPref);
            }
        }

        adsPersonalisation = (CheckBoxPreference) findPreference("ads_personalisation");

        adsPersonalisation.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Boolean newValue1 = (Boolean) newValue;
                Timber.e("Loader Preference: %s", newValue1);
                if (callFromLoader) {
                    ((CheckBoxPreference) preference).setChecked(newValue1);
                    Timber.e("Loader Preference: %s", newValue1);
                    callFromLoader = false;
                }
                return true;
            }
        });

        adsPersonalisation.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                try {
                    String action = "com.google.android.gms.settings.ADS_PRIVACY";
                    Intent settings = new Intent(action);
                    startActivity(settings);
//                        startActivity(new Intent(Settings.ACTION_PRIVACY_SETTINGS));
                    return true;
                } catch (ActivityNotFoundException e) {
//                        Toast.makeText(PreferenceFragment.this.getActivity(), R.string.not_able_to_open_gps_settings, Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        });

        optAddLoader = LoaderManager.getInstance(this).initLoader(0, null, new PrivacyLoaderCallbacks(this));
        optAddLoader.forceLoad();
    }

    public void setChecked(final String preferenceString, final boolean checked) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                ((CheckBoxPreference) findPreference(preferenceString)).setChecked(checked);
            }
        });

    }


    @Override
    public void onResume() {
        super.onResume();
        if (optAddLoader != null) {
            getLoaderManager().restartLoader(0, null, new PrivacyLoaderCallbacks(this)).forceLoad();
        }
    }

    public class PrivacyLoaderCallbacks implements LoaderManager.LoaderCallbacks<Boolean> {

        PreferenceFragment fragment;

        public PrivacyLoaderCallbacks(PreferenceFragment fragment) {
            this.fragment = fragment;
        }

        @Override
        public Loader<Boolean> onCreateLoader(int id, Bundle args) {
            return new AdvertisingIdClientLoader(PreferenceFragment.this.getActivity());
        }

        @Override
        public void onLoadFinished(@NonNull Loader<Boolean> loader, Boolean enabledAdvertisingInfo) {
            Timber.e("Loader, finished: %s", enabledAdvertisingInfo);
            if (adsPersonalisation != null) {
                callFromLoader = true;
                adsPersonalisation.setDefaultValue(enabledAdvertisingInfo);
                adsPersonalisation.setChecked(enabledAdvertisingInfo);
                /*adsPersonalisation.getEditor().commit();
                ListAdapter adapter = v.getAdapter();
                if (adapter instanceof BaseAdapter) {
                    ((BaseAdapter)adapter).notifyDataSetChanged();
                }*/
                Timber.e("Loader finished: %s", enabledAdvertisingInfo);
            }
        }

        @Override
        public void onLoaderReset(@NonNull Loader<Boolean> loader) {

        }
    }

}
