package at.specure.androidX

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.util.Log
import androidx.multidex.MultiDexApplication
import at.specure.android.util.location.GeoLocationX
import at.specure.android.util.net.NetworkInfoCollector
import at.specure.androidX.logging.timber.LoggingTree
import at.specure.di.AppComponent
import at.specure.di.DaggerAppComponent
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.facebook.stetho.Stetho
import com.google.firebase.FirebaseApp
import com.hypertrack.hyperlog.HyperLog
import com.specure.opennettest.BuildConfig
import timber.log.Timber

class Application : MultiDexApplication() {
    private var mNetworkStateIntentReceiver: BroadcastReceiver? = null
    private var mNetworkStateChangedFilter: IntentFilter? = null
    private var networkInfoCollector: NetworkInfoCollector? = null

    val coreComponent: AppComponent
        get() = Injector.component

    override fun onCreate() {
        FirebaseApp.initializeApp(this)
        super.onCreate()

        FacebookSdk.sdkInitialize(applicationContext)

        Stetho.initializeWithDefaults(this)
        AppEventsLogger.activateApp(this)
        Timber.e("CREATE")

        if (!BuildConfig.DEBUG) {
            Timber.plant(LoggingTree())
        } else {
            Timber.plant(LoggingTree())
        }

        HyperLog.initialize(this)
        HyperLog.setLogLevel(Log.VERBOSE)

        Injector.component = DaggerAppComponent.builder()
            .context(this)
            .build()

        Injector.component = Injector.component

        Injector.inject(this)

        val instance = GeoLocationX.getInstance(this)
        instance.getLastKnownLocation(this, null)

        mNetworkStateChangedFilter = IntentFilter()
        mNetworkStateChangedFilter!!.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
        networkInfoCollector = NetworkInfoCollector.getInstance(applicationContext)

        mNetworkStateIntentReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action != null && intent.action == ConnectivityManager.CONNECTIVITY_ACTION) {
                    val connected = !intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)
                    val isFailOver = intent.getBooleanExtra(ConnectivityManager.EXTRA_IS_FAILOVER, false)
                    if (connected) {
                        if (networkInfoCollector != null) {
                            networkInfoCollector!!.setHasConnectionFromAndroidApi(true)
                        }
                    } else {
                        if (networkInfoCollector != null) {
                            networkInfoCollector!!.setHasConnectionFromAndroidApi(false)
                        }
                    }
                    Timber.i(" %s  CONNECTED:  %s  FAILOVER:  %s", "application", connected, isFailOver)
                }
            }
        }
    }
}