package at.specure.android.screens.main;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import at.specure.android.base.ContextWrapper;
import at.specure.android.configs.LocaleConfig;

public class BasicActivity extends AppCompatActivity {


    private boolean firstTimeRun = false;

    @Override
    protected void attachBaseContext(Context newBase) {
        Context context = ContextWrapper.wrap(newBase, LocaleConfig.getSetLocale(newBase));
        super.attachBaseContext(context);
    }


    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            firstTimeRun = true;
        } else {
            firstTimeRun = false;
        }

        if (LocaleConfig.isUserAbleToChangeLanguage(this)) {
            if (firstTimeRun) {
                firstTimeRun = false;
                LocaleConfig.initializeApp(this, false);
            }
        }
    }
}
