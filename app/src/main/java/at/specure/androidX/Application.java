package at.specure.androidX;

import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.hypertrack.hyperlog.HyperLog;
import com.specure.opennettest.BuildConfig;

import androidx.multidex.MultiDexApplication;
import at.specure.android.util.location.GeoLocationX;
import at.specure.androidX.logging.timber.LoggingTree;
import at.specure.androidX.logging.timber.NotLoggingTree;
import timber.log.Timber;

public class Application extends MultiDexApplication {

    @Override
    public void onCreate() {
        FirebaseApp.initializeApp(this);
        super.onCreate();
        Timber.e( "CREATE");
        if (!BuildConfig.DEBUG) {
            Timber.plant(new LoggingTree());
        } else {
            Timber.plant(new LoggingTree());
        }
        HyperLog.initialize(this);
        HyperLog.setLogLevel(Log.VERBOSE);
        GeoLocationX instance = GeoLocationX.getInstance(this);
        instance.getLastKnownLocation(this, null);
    }
}
