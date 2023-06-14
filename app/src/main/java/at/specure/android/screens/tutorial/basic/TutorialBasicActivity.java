package at.specure.android.screens.tutorial.basic;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.specure.opennettest.R;

import java.util.Formatter;

import androidx.viewpager.widget.ViewPager;
import at.specure.android.configs.LocaleConfig;
import at.specure.android.configs.PrivacyConfig;
import at.specure.android.configs.StartConfig;
import at.specure.android.screens.main.BasicActivity;
import at.specure.android.screens.main.MainActivity;
import at.specure.android.screens.preferences.PreferenceActivity;
import timber.log.Timber;

/**
 * Tutorial Activity displays multiple fragments in defined order just to show new user how to
 * properly use the application.
 * <p/>
 * Created by Misiak on 11-Feb-16.
 */
public class TutorialBasicActivity extends BasicActivity {

    public static final int PERM_REQ_LOC_COARSE_START = 1;
    private ViewPager viewPager;
    private boolean currentPage;
    private TextView title;
    private TextView description;
    private Button agreeButton;
    private Button skipButton;
    private TextView details;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, TutorialBasicActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tutorial__activity_basic__layout);
//        if (ConfigHelper.isSecretEntered(this)) {
//            PreferenceManager.setDefaultValues(this, SETTINGS_SHARED_PREFERENCES_FILE_NAME, MODE_PRIVATE, R.xml.preferences_after_secret, false);
//        } else {
//            PreferenceManager.setDefaultValues(this, SETTINGS_SHARED_PREFERENCES_FILE_NAME, MODE_PRIVATE,  R.xml.preferences, false);
//        }
        startPreferenceActivity();

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//        }
    }

    private void startPreferenceActivity() {
//        Intent intent = new Intent(TutorialBasicActivity.this, PreferenceActivity.class);
//        intent.putExtra("finish", true);
//        startActivity(intent);
    }


    @Override
    protected void onStart() {
        super.onStart();

        StringBuilder sb = new StringBuilder();
        // Send all output to the Appendable object sb
        Formatter formatter = new Formatter(sb, LocaleConfig.getSetLocale(this));

        Timber.i("SDK_INT %s", String.valueOf(Build.VERSION.SDK_INT));
        title = findViewById(R.id.title);
        title.setText(getString(R.string.wizard_one_page_title) + " " + getString(R.string.app_name));

        description = findViewById(R.id.description);
        description.setText(formatter.format(getString(R.string.wizard_one_page_privacy_description), getString(R.string.about_pp_title), getString(R.string.about_email_email)).toString());

        details = findViewById(R.id.details);
        details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartConfig.setTutorialDisplayed(TutorialBasicActivity.this);
                Intent intent = new Intent(TutorialBasicActivity.this, MainActivity.class);
                startActivity(intent);
                Intent intent2 = new Intent(TutorialBasicActivity.this, PreferenceActivity.class);
                TutorialBasicActivity.this.finish();
                startActivity(intent2);
            }
        });


        skipButton = findViewById(R.id.skip_button);
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PrivacyConfig.setClientUUIDPersistent(TutorialBasicActivity.this.getApplicationContext(), false);
                PrivacyConfig.setAnalyticsPermitted(TutorialBasicActivity.this.getApplicationContext(), false);
                StartConfig.setTutorialDisplayed(TutorialBasicActivity.this);
                if (PrivacyConfig.showPublishPersonalDataInSettings(TutorialBasicActivity.this.getApplicationContext())) {
                    PrivacyConfig.setPublishPersonalData(TutorialBasicActivity.this.getApplicationContext(), false);
                }
                startApp();
            }
        });

        agreeButton = findViewById(R.id.agree_button);
        agreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PrivacyConfig.setClientUUIDPersistent(TutorialBasicActivity.this.getApplicationContext(), true);
                PrivacyConfig.setAnalyticsPermitted(TutorialBasicActivity.this.getApplicationContext(), true);
                StartConfig.setTutorialDisplayed(TutorialBasicActivity.this);
                if (PrivacyConfig.showPublishPersonalDataInSettings(TutorialBasicActivity.this.getApplicationContext())) {
                    PrivacyConfig.setPublishPersonalData(TutorialBasicActivity.this.getApplicationContext(), true);
                }
                startApp();
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void startApp() {
        Intent intent = new Intent(TutorialBasicActivity.this, MainActivity.class);
        TutorialBasicActivity.this.finish();
        startActivity(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
