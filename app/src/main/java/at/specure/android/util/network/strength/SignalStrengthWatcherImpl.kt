/*
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.specure.info.strength

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.READ_PHONE_STATE
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Handler
import android.telephony.CellInfo
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import at.specure.android.util.network.TransportType
import at.specure.android.util.network.cell.ActiveDataCellInfoExtractor
import at.specure.android.util.network.cell.CellInfoWatcher
import at.specure.android.util.network.network.ActiveNetworkWatcher
import at.specure.android.util.network.network.NRConnectionState
import at.specure.android.util.network.network.NetworkInfo
import at.specure.info.wifi.WifiInfoWatcher
import at.specure.util.permission.LocationAccess
import at.specure.util.synchronizedForEach
import timber.log.Timber
import java.lang.IllegalStateException
import java.util.Collections

private const val WIFI_UPDATE_DELAY = 2000L
private const val WIFI_MESSAGE_ID = 1

/**
 * Basic implementation of [SignalStrengthInfo] that using [ActiveNetworkWatcher] and [WifiInfoWatcher] to detect network changes and handle
 * signal strength changes of current network available on the mobile device
 */
class SignalStrengthWatcherImpl(
    private val context: Context,
    private val subscriptionManager: SubscriptionManager,
    private val telephonyManager: TelephonyManager,
    private val activeNetworkWatcher: ActiveNetworkWatcher,
    private val wifiInfoWatcher: WifiInfoWatcher,
    private val cellInfoWatcher: CellInfoWatcher,
    private val activeDataCellInfoExtractor: ActiveDataCellInfoExtractor,
    locationAccess: LocationAccess
) : SignalStrengthWatcher, LocationAccess.LocationAccessChangeListener {

    private val listeners = Collections.synchronizedSet(mutableSetOf<SignalStrengthWatcher.SignalStrengthListener>())

    private var cellListenerRegistered = false
    private var wifiListenerRegistered = false

    private var signalStrengthInfo: SignalStrengthInfo? = null

    private var lastNRConnectionState: NRConnectionState? = null

    override val lastSignalStrength: SignalStrengthInfo?
        get() = signalStrengthInfo

    init {
        locationAccess.addListener(this)
    }

    private val strengthListener = object : PhoneStateListener() {

        // discard signal strength from GT-I9100G (Galaxy S II) - passes wrong info
        private val ignoredDevices = setOf("GT-I9100G", "HUAWEI P2-6011")

        private val isDeviceIgnored: Boolean
            get() = ignoredDevices.contains(Build.MODEL)

        @SuppressLint("MissingPermission")
        override fun onSignalStrengthsChanged(signalStrength: SignalStrength?) {
            if (isDeviceIgnored) {
                Timber.i("Signal Strength is ignored for current device")
                return
            }
            var nrConnectionState = NRConnectionState.NOT_AVAILABLE
            var cellInfo: CellInfo? = null
            val network = activeNetworkWatcher.currentNetworkInfo
            if ((PermissionChecker.checkSelfPermission(context, READ_PHONE_STATE) == PERMISSION_GRANTED) && PermissionChecker.checkSelfPermission(
                    context,
                    ACCESS_COARSE_LOCATION
                ) == PERMISSION_GRANTED
            ) {
                try {
                    val activeDataCellInfo = activeDataCellInfoExtractor.extractActiveCellInfo(telephonyManager.allCellInfo, signalStrength)
                    cellInfo = activeDataCellInfo.activeDataNetworkCellInfo
                    nrConnectionState = activeDataCellInfo.nrConnectionState
                } catch (e: SecurityException) {
                    Timber.e("SecurityException: Not able to read telephonyManager.allCellInfo")
                } catch (e: IllegalStateException) {
                    Timber.e("IllegalStateException: Not able to read telephonyManager.allCellInfo")
                }
            }

            val dualSim = if (PermissionChecker.checkSelfPermission(context, READ_PHONE_STATE) == PERMISSION_GRANTED) {
                subscriptionManager.activeSubscriptionInfoCount > 1
            } else {
                telephonyManager.phoneCount > 1
            }

            Timber.d("Signal changed detected: value: ${signalStrength?.level}\nclass: ${signalStrength?.javaClass}\n ${signalStrength?.toString()}")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                signalStrength?.cellSignalStrengths?.forEach {
                    Timber.d("Cell signal changed detected: \ndbm: ${it.dbm}\nLevel: ${it.level}\nasuLevel: ${it.asuLevel}\nclass: ${it.javaClass}")
                }
            }

            val signal = SignalStrengthInfo.from(signalStrength, network, cellInfo, nrConnectionState, dualSim)

            if (nrConnectionState != lastNRConnectionState) {
                cellInfoWatcher.forceUpdate()
                lastNRConnectionState = nrConnectionState
            }

            if (signal?.value == null || signal.value == 0) {
                signalStrengthInfo = null
                lastNRConnectionState = null
                Timber.d("Signal changed to: NULL")
            } else {
                signalStrengthInfo = signal
                Timber.d("Signal changed to: \ntransport: ${signal.transport} \nvalue: ${signal.value} \nsignalLevel:${signal.signalLevel}")
            }
            notifyInfoChanged()
        }
    }

    private val activeNetworkListener = object : ActiveNetworkWatcher.NetworkChangeListener {

        override fun onActiveNetworkChanged(info: NetworkInfo?) {
            if (info == null) {
                unregisterWifiCallbacks()
                unregisterCellCallbacks()

                Timber.i("Network changed to NULL")
                signalStrengthInfo = null
                notifyInfoChanged()

                return
            }

            if (info.type == TransportType.CELLULAR) {
                registerCellCallbacks()
            }

            if (info.type == TransportType.WIFI) {
                registerWifiCallbacks()
            }
        }
    }

    private val wifiUpdateHandler = Handler {
        handleWifiUpdate()
        return@Handler true
    }

    private fun handleWifiUpdate() {
        val wifiInfo = wifiInfoWatcher.activeWifiInfo
        if (wifiInfo != null) {
            signalStrengthInfo = SignalStrengthInfo.from(wifiInfo)
        }
        notifyInfoChanged()
        scheduleWifiUpdate()
    }

    private fun scheduleWifiUpdate() {
        wifiUpdateHandler.removeMessages(WIFI_MESSAGE_ID)
        if (wifiListenerRegistered) {
            wifiUpdateHandler.sendEmptyMessageDelayed(WIFI_MESSAGE_ID, WIFI_UPDATE_DELAY)
        }
    }

    private fun notifyInfoChanged() {
        listeners.synchronizedForEach { it.onSignalStrengthChanged(signalStrengthInfo) }
    }

    override fun addListener(listener: SignalStrengthWatcher.SignalStrengthListener) {
        listeners.add(listener)
        listener.onSignalStrengthChanged(lastSignalStrength)
        if (listeners.size == 1) {
            registerCallbacks()
        }
    }

    override fun removeListener(listener: SignalStrengthWatcher.SignalStrengthListener) {
        listeners.remove(listener)
        if (listeners.isEmpty()) {
            unregisterCallbacks()
        }
    }

    private fun registerCallbacks() {
        activeNetworkWatcher.addListener(activeNetworkListener)
    }

    private fun unregisterCallbacks() {
        activeNetworkWatcher.removeListener(activeNetworkListener)
        unregisterCellCallbacks()
        unregisterWifiCallbacks()
    }

    private fun registerCellCallbacks() {
        Timber.i("Network changed to CELLULAR")
        if (!cellListenerRegistered) {
            telephonyManager.listen(strengthListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)
            cellListenerRegistered = true
        }
        unregisterWifiCallbacks()
    }

    private fun registerWifiCallbacks() {
        Timber.i("Network changed to WIFI")
        if (!wifiListenerRegistered) {
            wifiListenerRegistered = true
            handleWifiUpdate()
        }
        unregisterCellCallbacks()
    }

    private fun unregisterCellCallbacks() {
        if (cellListenerRegistered) {
            telephonyManager.listen(strengthListener, PhoneStateListener.LISTEN_NONE)
            cellListenerRegistered = false
        }
    }

    private fun unregisterWifiCallbacks() {
        if (wifiListenerRegistered) {
            wifiUpdateHandler.removeMessages(WIFI_MESSAGE_ID)
            wifiListenerRegistered = false
        }
    }

    override fun onLocationAccessChanged(isAllowed: Boolean) {
        if (listeners.isNotEmpty() && isAllowed) {
            unregisterCallbacks()
            registerCallbacks()
        }
    }
}