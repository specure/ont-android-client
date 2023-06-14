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

package at.specure.android.screens.about;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.specure.opennettest.BuildConfig;
import com.specure.opennettest.R;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import at.specure.android.configs.ConfigHelper;
import at.specure.android.configs.TermsAndConditionsConfig;
import at.specure.android.screens.licenses.LicensesActivity;
import at.specure.android.screens.preferences.PreferenceActivity;
import at.specure.android.util.PrivacyPolicyFragment;
import at.specure.android.util.TermsFragment;
import timber.log.Timber;


/**
 * Controller for About fragment handling all the fragment stuff
 * Created by michal.cadrik on 10/24/2017.
 */

@SuppressWarnings("WeakerAccess")
public class AboutController {

    private static final String DEBUG_TAG = "AboutController";

    private String clientVersion;
    private String clientName;
    private int clickCounter;
    private AboutInterface aboutInterface;
    private String clientUUID;
    private String controlServerVersion;

    public AboutController(@NonNull AboutInterface aboutInterface) {
        this.aboutInterface = aboutInterface;
        clickCounter = 0;
        getAppInfo();
    }


    private String getAppInfo() {
        PackageInfo pInfo;

        if ((aboutInterface != null) && (aboutInterface.getContext() != null)) {
            Context context = aboutInterface.getContext();
            try {
                Date buildTime = BuildConfig.buildTime;
                String date = DateFormat.getDateTimeInstance().format(buildTime);
                pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                clientVersion = pInfo.versionName + " (" + pInfo.versionCode + ")\n(" + date + ")\n";
                clientName = context.getResources().getString(R.string.app_name);
            } catch (final Exception e) {
                Timber.e(e,"version of the application cannot be found ");
            }
        } else {
            return "-";
        }
        return clientVersion;
    }

    public String getClientName() {
        return clientName;
    }

    public String getClientVersion() {
        return clientVersion;
    }

    public String getClientUUID() {
        if (clientUUID == null) {
            clientUUID = String.format("U%s", ConfigHelper.getUUID(aboutInterface.getContext()));
        }
        return clientUUID;
    }

    public String getControlServerVersion() {
        if (controlServerVersion == null) {
            controlServerVersion = ConfigHelper.getControlServerVersion(aboutInterface.getContext());
        }
        return controlServerVersion;
    }

    public boolean isLoopModeSecret(@NonNull Context context) {
        return (context.getResources().getBoolean(R.bool.show_loop_mode_after_secret) && !ConfigHelper.isSecretEntered(context));
    }

    public void showSecretOnClickAction() {
        clickCounter++;
        if (clickCounter == 10) {
            showDialogToEnterSecret();
            clickCounter = 0;
        }
    }

    private void showDialogToEnterSecret() {

        final Activity activity = aboutInterface.getActivity();
        if (activity != null) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(activity);

            LayoutInflater layoutInflater = activity.getLayoutInflater();
            @SuppressLint("InflateParams") View inflatedView = layoutInflater.inflate(R.layout.enter_secret_layout, null);

            final EditText secretEditText = inflatedView.findViewById(R.id.secret_edit_text);

            builder.setView(inflatedView);

            builder.setPositiveButton(R.string._ok,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface d, int id) {
                            String enteredSecret = secretEditText.getText().toString();
                            String secret = activity.getResources().getString(R.string.secret);
                            if (secret.equalsIgnoreCase(enteredSecret)) {
                                ConfigHelper.setSecretEntered(true, activity);
                                Toast.makeText(activity, R.string.enter_secret_code_success, Toast.LENGTH_SHORT).show();
                                openSettings(activity);
                                d.dismiss();
                            } if ("00000000".equalsIgnoreCase(enteredSecret)) {
                                ConfigHelper.setSecretEntered(false, activity);
                            } else {
                                Toast.makeText(activity, R.string.enter_secret_code_failed, Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .setNegativeButton(R.string._cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface d, int id) {
                                    d.cancel();
                                }
                            });
            builder.create().show();
        }
    }

    public void openSettings(Activity activity) {
        activity.startActivity(new Intent(activity, PreferenceActivity.class));
    }

    ArrayList<HashMap<String, String>> getListItems(@NonNull final FragmentActivity activity) {
        final ArrayList<HashMap<String, String>> list = new ArrayList<>();
        final ArrayList<AboutItem> actions = new ArrayList<>();
        HashMap<String, String> item;

        item = new HashMap<>();
        item.put("title", getClientName());
        item.put("text1", activity.getString(R.string.about_web_line1));
        list.add(item);
        actions.add(new AboutItem() {
            @Override
            public void action() {

            }
        });
        item = new HashMap<>();
        item.put("title", activity.getString(R.string.about_version_title));
        item.put("text1", getClientVersion());
        item.put("text2", "");
        list.add(item);
        actions.add(new AboutItem() {
            @Override
            public void action() {

            }
        });
        item = new HashMap<>();
        item.put("title", activity.getString(R.string.about_clientid_title));
        item.put("text1", getClientUUID());
        item.put("text2", "");
        list.add(item);
        actions.add(new AboutItem() {
            @Override
            public void action() {
                final android.content.ClipboardManager clipBoard = (android.content.ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("client_uuid", getClientUUID());
                clipBoard.setPrimaryClip(clip);
                final Toast toast = Toast.makeText(activity, R.string.about_clientid_toast, Toast.LENGTH_LONG);
                toast.show();
            }
        });
        item = new HashMap<>();
        item.put("title", activity.getString(R.string.about_web_title));
        item.put("text1", activity.getString(R.string.about_web_line1));
        item.put("text2", "");
        list.add(item);
        actions.add(new AboutItem() {
            @Override
            public void action() {
                try {
                    if (ConfigHelper.shouldShowWebsite(activity)) {
                        activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(activity.getString(R.string.about_web_link))));
                    }
                } catch (ActivityNotFoundException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            }
        });
        item = new HashMap<>();
        item.put("title", activity.getString(R.string.about_email_title));
        item.put("text1", activity.getString(R.string.about_email_line1));
        item.put("text2", "");
        list.add(item);
        actions.add(new AboutItem() {
            @Override
            public void action() {
                /* Create the Intent */
                final Intent emailIntent = new Intent(Intent.ACTION_SEND);

                /* Fill it with Data */
                emailIntent.setType("plain/text");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{activity.getString(R.string.about_email_email)});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, activity.getString(R.string.about_email_subject));
                emailIntent.putExtra(Intent.EXTRA_TEXT, "");

                /* Send it off to the Activity-Chooser */
                try {
                    activity.startActivity(Intent.createChooser(emailIntent, activity.getString(R.string.about_email_sending)));
                } catch (ActivityNotFoundException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            }
        });
        item = new HashMap<>();
        item.put("title", activity.getString(R.string.about_terms_title));
        item.put("text1", "");
        item.put("text2", "");
        list.add(item);
        actions.add(new AboutItem() {
            @Override
            public void action() {
                final FragmentManager fm = activity.getSupportFragmentManager();
                FragmentTransaction ft;
                ft = fm.beginTransaction();
                ft.replace(R.id.fragment_content, new TermsFragment(), "terms");
                ft.addToBackStack("terms");
                ft.commit();
            }
        });

        if (TermsAndConditionsConfig.showPrivacyPolicyInAboutPage(activity)) {
            item = new HashMap<>();
            item.put("title", activity.getString(R.string.about_pp_title));
            item.put("text1", "");
            item.put("text2", "");
            list.add(item);
            actions.add(new AboutItem() {
                @Override
                public void action() {
                    final FragmentManager fm = activity.getSupportFragmentManager();
                    FragmentTransaction ft;
                    ft = fm.beginTransaction();
                    ft.replace(R.id.fragment_content, new PrivacyPolicyFragment(), "pp");
                    ft.addToBackStack("pp");
                    ft.commit();
                }
            });
        }
        item = new HashMap<>();
        item.put("title", activity.getString(R.string.about_git_title));
        item.put("text1", activity.getString(R.string.about_git_line1));
        item.put("text2", "");
        list.add(item);
        actions.add(new AboutItem() {
            @Override
            public void action() {
                try {
                    activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(activity.getString(R.string.about_git_link))));
                } catch (ActivityNotFoundException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            }
        });
        item = new HashMap<>();
        item.put("title", activity.getString(R.string.about_dev_title));
        item.put("text1", activity.getString(R.string.about_dev_line1));
        item.put("text2", activity.getString(R.string.about_dev_line2));
        list.add(item);
        actions.add(new AboutItem() {
            @Override
            public void action() {
                try {
                    activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(activity.getString(R.string.about_dev_link))));
                } catch (ActivityNotFoundException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            }
        });
        item = new HashMap<>();
        item.put("title", activity.getString(R.string.about_gms_legal_title));
        item.put("text1", ""); //activity.getString(R.string.about_gms_legal_line1));
        item.put("text2", "");
        list.add(item);
        actions.add(new AboutItem() {
            @Override
            public void action() {
                try {
                    activity.startActivity(new Intent(activity, OssLicensesMenuActivity.class));
                } catch (ActivityNotFoundException e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            }
        });

        if (activity.getResources().getBoolean(R.bool.show_licenses)) {
            item = new HashMap<>();
            item.put("title", activity.getString(R.string.title_activity_licenses));
            item.put("text1", "");
            item.put("text2", "");
            list.add(item);
            actions.add(new AboutItem() {
                @Override
                public void action() {
                    if (activity.getResources().getBoolean(R.bool.show_licenses)) {
                        try {
                            activity.startActivity(new Intent(activity, LicensesActivity.class));
                        } catch (ActivityNotFoundException e) {
                            FirebaseCrashlytics.getInstance().recordException(e);
                        }
                    }
                }
            });
        }

        if (ConfigHelper.isDevEnabled(activity)) {
            item = new HashMap<>();
            item.put("title", activity.getString(R.string.about_test_counter_title));
            item.put("text1", Integer.toString(ConfigHelper.getTestCounter(activity)));
            item.put("text2", "");
            list.add(item);
            actions.add(new AboutItem() {
                @Override
                public void action() {

                }
            });
        }

//        item = new HashMap<>();
//        item.put("title", activity.getString(R.string.about_control_server_version));
//        item.put("text1", getControlServerVersion() != null ? getControlServerVersion() : "---");
//        item.put("text2", "");
//        list.add(item);
//        actions.add(new AboutItem() {
//            @Override
//            public void action() {
//
//            }
//        });
        aboutInterface.setOnItemClickListenerForList(actions);
        return list;
    }

}
