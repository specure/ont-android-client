package at.specure.android.configs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.util.DisplayMetrics;

import androidx.annotation.NonNull;
import androidx.core.os.ConfigurationCompat;

import com.specure.opennettest.R;

import java.util.Locale;

import at.specure.android.SupportedLocales;
import at.specure.android.screens.main.MainActivity;
import timber.log.Timber;

public class LocaleConfig {

    public static String getLocaleForServerRequest(Context context) {
        Locale locale = getSetLocale(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (locale != null) {
                String language = locale.getLanguage();
                if ((language != null) && (!language.isEmpty())) {
                    if ("sr".equalsIgnoreCase(language)) {
                        String script = locale.getScript();
                        String country = locale.getCountry();
                        if ("latn".equalsIgnoreCase(script) || "XX".equalsIgnoreCase(country)) {
                            Timber.e("LOCALE SERVER 1 %s", language + "-Latn");
                            return language + "-Latn";
                        } else if ("ZZ".equalsIgnoreCase(country)) {
                            Timber.e("LOCALE SERVER 8 %s", language + "_ME-Latn");
                            return language + "_ME-Latn";
                        }
                    }
                    Timber.e("LOCALE SERVER 2 %s", language);
                    return language;
                }
            }
        } else {
            String language = locale.getLanguage();
            if ((language != null) && (!language.isEmpty())) {
                if ("sr".equalsIgnoreCase(language)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        String script = locale.getScript();
                        String country = locale.getCountry();
                        if ("latn".equalsIgnoreCase(script) || "XX".equalsIgnoreCase(country) || "ZZ".equalsIgnoreCase(country)) {
                            Timber.e("LOCALE SERVER 3 %s", language + "-Latn");
                            return language + "-Latn";
                        }
                    } else {
                        String country = locale.getCountry();
                        if ("XX".equalsIgnoreCase(country)) {
                            Timber.e("LOCALE SERVER 4 %s", language + "-Latn");
                            return language + "-Latn";
                        } else if ("ZZ".equalsIgnoreCase(country)) {
                            Timber.e("LOCALE SERVER 7 %s", language + "_ME-Latn");
                            return language + "_ME-Latn";
                        }
                    }
                }
                Timber.e("LOCALE SERVER 5 %s", language);
                return language;
            }
        }
        Timber.e("LOCALE SERVER 6 %s", "en");
        return "en";
    }

    @SuppressLint("NewApi")
    public static Locale getSetLocale(Context context) {
        Locale myLocale = new Locale("en");
        SharedPreferences sharedPreferences = ConfigHelper.getSharedPreferences(context);
        try {
            int selectedLanguage = sharedPreferences.getInt("selected_language", 0);
            if (selectedLanguage != 0) {
                SupportedLocales locale = getLocaleAccordingId(selectedLanguage);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    if (("Latn".equalsIgnoreCase(locale.script)) && ("sr".equalsIgnoreCase(locale.languageCode))) {
                        if ("ME".equalsIgnoreCase(locale.countryCode)) {
                            myLocale = new Locale("sr", "ZZ",locale.script);
                        } else {
                            myLocale = new Locale("sr", "XX", locale.script);
                        }
                        Timber.e("LOCALE %s", myLocale.toString());
                    } else {
                        myLocale = new Locale(locale.languageCode, locale.countryCode.toUpperCase(), locale.script);
                        Timber.e("LOCALE %s", myLocale.toString());
                    }

                } else {
                    myLocale = new Locale.Builder().setLanguage(locale.languageCode).setRegion(locale.countryCode.toUpperCase()).setScript(locale.script).build();
//                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    if (("Latn".equalsIgnoreCase(locale.script)) && ("sr".equalsIgnoreCase(locale.languageCode))) {
                        if ("ME".equalsIgnoreCase(locale.countryCode)) {
                            myLocale = new Locale.Builder().setLanguage(locale.languageCode).setRegion("ZZ").build();
                        } else {
                            myLocale = new Locale.Builder().setLanguage(locale.languageCode).setRegion("XX").build();
                        }
                        Timber.e("LOCALE %s", myLocale.toString());
                    }
                    if (("Cyrl".equalsIgnoreCase(locale.script)) && ("sr".equalsIgnoreCase(locale.languageCode))) {
                        myLocale = new Locale.Builder().setLanguage(locale.languageCode).setRegion("YY").build();
                        Timber.e("LOCALE %s", myLocale.toString());
                    }
//                }

                }
            } else {
                myLocale = ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration()).get(0);

            /*if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.N)) {
                myLocale = Locale.getDefault();
            } else {
                myLocale = Resources.getSystem().getConfiguration().getLocales().get(0);
            }*/
            }
        } catch (Exception ignored) {
            myLocale = new Locale("en");
        }
        return myLocale;
    }

    @SuppressLint("NewApi")
    public static void initializeApp(Activity activity, boolean shouldrefresh) {
        Locale myLocale;

        myLocale = getSetLocale(activity);

        Resources resources = activity.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration configuration = resources.getConfiguration();

        Resources res1 = activity.getApplicationContext().getResources();
        DisplayMetrics dm1 = res1.getDisplayMetrics();
        Configuration configuration1 = res1.getConfiguration();

        if (myLocale != null) {
            if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) && (!"ME".equalsIgnoreCase(myLocale.getCountry()))) {

                LocaleList localeList = null;
                localeList = new LocaleList(myLocale);
                LocaleList.setDefault(localeList);
                configuration.setLocales(localeList);
                configuration.setLocale(myLocale);

                configuration1.setLocales(localeList);
                configuration1.setLocale(myLocale);

            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                configuration.setLocale(myLocale);
            } else {
                configuration.locale = myLocale;
            }

            res1.updateConfiguration(configuration, dm);
        }
        if (shouldrefresh) {
            Intent refresh = new Intent(activity, MainActivity.class);
            activity.finish();
            activity.startActivity(refresh);
        }
    }

    public static SupportedLocales getSelectedLanguage(Context activity) {
        SharedPreferences sharedPreferences = ConfigHelper.getSharedPreferences(activity);
        int selectedLanguage = sharedPreferences.getInt("selected_language", 0);
        SupportedLocales locale = getLocaleAccordingId(selectedLanguage);
        return locale;
    }

    @NonNull
    private static SupportedLocales getLocaleAccordingId(int selectedLanguage) {
        SupportedLocales[] values = SupportedLocales.values();
        SupportedLocales locale = SupportedLocales.DEFAULT;
        for (SupportedLocales value : values) {
            if (value.id == selectedLanguage) {
                locale = value;
            }
        }
        return locale;
    }

    public static void setSelectedLanguage(Activity activity, int id) {

        SharedPreferences sharedPreferences = ConfigHelper.getSharedPreferences(activity);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putInt("selected_language", id);
        edit.putBoolean("language_changed", true);
        edit.commit();

        initializeApp(activity, true);
    }

    public static void setLanguageChangedDone(Context context) {
        SharedPreferences sharedPreferences = ConfigHelper.getSharedPreferences(context);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putBoolean("language_changed", false);
        edit.commit();
    }

    public static boolean isLanguageChanged(Context context) {
        SharedPreferences sharedPreferences = ConfigHelper.getSharedPreferences(context);
        boolean language_changed = sharedPreferences.getBoolean("language_changed", false);
        return language_changed;
    }

    public static boolean isUserAbleToChangeLanguage(Context context) {
        return false;
    }
}