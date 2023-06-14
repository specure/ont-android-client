package at.specure.di

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import at.specure.android.util.network.cell.ActiveDataCellInfoExtractor
import at.specure.android.util.network.cell.ActiveDataCellInfoExtractorImpl
import at.specure.android.util.network.cell.CellInfoWatcher
import at.specure.android.util.network.cell.CellInfoWatcherImpl
import at.specure.info.connectivity.ConnectivityWatcher
import at.specure.android.util.network.network.ActiveNetworkWatcher
import at.specure.android.util.network.ip.CaptivePortal
import at.specure.info.wifi.WifiInfoWatcher
import at.specure.info.wifi.WifiInfoWatcherImpl
import at.specure.location.LocationWatcher
import at.specure.util.permission.LocationAccess
import at.specure.util.permission.PhoneStateAccess
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule {

    @Provides
    @Singleton
    fun provideActiveDataCellInfoExtractor(
        context: Context,
        telephonyManager: TelephonyManager,
        subscriptionManager: SubscriptionManager,
        connectivityManager: ConnectivityManager
    ): ActiveDataCellInfoExtractor = ActiveDataCellInfoExtractorImpl(context, telephonyManager, subscriptionManager, connectivityManager)

    @Provides
    @Singleton
    fun provideCellInfoWatcher(
        context: Context,
        telephonyManager: TelephonyManager,
        locationAccess: LocationAccess,
        phoneStateAccess: PhoneStateAccess,
        connectivityManager: ConnectivityManager,
        activeDataCellInfoExtractor: ActiveDataCellInfoExtractor
    ): CellInfoWatcher =
        CellInfoWatcherImpl(context, telephonyManager, locationAccess, phoneStateAccess, connectivityManager, activeDataCellInfoExtractor)

    @Provides
    @Singleton
    fun provideWifiInfoWatcher(wifiManager: WifiManager): WifiInfoWatcher = WifiInfoWatcherImpl(wifiManager)

    @Provides
    @Singleton
    fun provideActiveNetworkWatcher(
        connectivityWatcher: ConnectivityWatcher,
        wifiInfoWatcher: WifiInfoWatcher,
        cellInfoWatcher: CellInfoWatcher,
        locationWatcher: LocationWatcher,
        captivePortal: CaptivePortal
    ): ActiveNetworkWatcher =
        ActiveNetworkWatcher(connectivityWatcher, wifiInfoWatcher, cellInfoWatcher, locationWatcher.stateWatcher, captivePortal)

}
