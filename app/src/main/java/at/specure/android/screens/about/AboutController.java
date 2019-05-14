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

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.specure.opennettest.BuildConfig;
import com.specure.opennettest.R;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import at.specure.android.screens.licenses.LicensesActivity;
import at.specure.android.configs.ConfigHelper;
import at.specure.android.util.TermsFragment;


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
                Log.e(DEBUG_TAG, "version of the application cannot be found", e);
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


    ArrayList<HashMap<String, String>> getListItems(@NonNull final FragmentActivity activity) {
        final ArrayList<HashMap<String, String>> list = new ArrayList<>();
        final ArrayList<AboutItem> actions = new ArrayList<>();
        HashMap<String, String> item;

        item = new HashMap<>();
        item.put("title", getClientName());
        item.put("text1", activity.getString(R.string.about_rtr_line1));
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
                            activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(activity.getString(R.string.about_web_link))));
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
                            activity.startActivity(Intent.createChooser(emailIntent, activity.getString(R.string.about_email_sending)));
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
        item = new HashMap<>();
        item.put("title", activity.getString(R.string.about_git_title));
        item.put("text1", activity.getString(R.string.about_git_line1));
        item.put("text2", "");
        list.add(item);
        actions.add(new AboutItem() {
            @Override
            public void action() {
                activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(activity.getString(R.string.about_git_link))));
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
                activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(activity.getString(R.string.about_dev_link))));

            }
        });
        item = new HashMap<>();
        item.put("title", activity.getString(R.string.about_gms_legal_title));
        item.put("text1", activity.getString(R.string.about_gms_legal_line1));
        item.put("text2", "");
        list.add(item);
        actions.add(new AboutItem() {
            @Override
            public void action() {
                activity.startActivity(new Intent(activity, OssLicensesMenuActivity.class));

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
                        activity.startActivity(new Intent(activity, LicensesActivity.class));
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

        item = new HashMap<>();
        item.put("title", activity.getString(R.string.about_control_server_version));
        item.put("text1", getControlServerVersion() != null ? getControlServerVersion() : "---");
        item.put("text2", "");
        list.add(item);
        actions.add(new AboutItem() {
            @Override
            public void action() {

            }
        });
        aboutInterface.setOnItemClickListenerForList(actions);
        return list;
    }

}
