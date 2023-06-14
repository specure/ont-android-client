package at.specure.android.util.network.cell

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.telephony.*
import androidx.core.app.ActivityCompat
import at.specure.android.util.network.network.MobileNetworkType
import at.specure.android.util.network.network.NRConnectionState
import at.specure.info.strength.extractSignalValue
import timber.log.Timber
import java.util.UUID

private const val INVALID_SUBSCRIPTION_ID = -1
private val DUAL_SIM_METHOD_API = "api_" + Build.VERSION.SDK_INT

/**
 * Detects correct active cell info. This class is important for 5G connections and dual sims mainly. In case of 5G connections there are 4G cells
 * reported as active, but for data transfer is used 5G cell which is not reported as active one.
 *
 * To use this class you need [Manifest.permission.READ_PHONE_STATE] to be granted
 */
class ActiveDataCellInfoExtractorImpl(
    private val context: Context,
    private val telephonyManager: TelephonyManager,
    private val subscriptionManager: SubscriptionManager,
    private val connectivityManager: ConnectivityManager
) : ActiveDataCellInfoExtractor {
    private var _dualSimDecision: String = ""
    private var _activeDataNetwork: CellNetworkInfo? = null
    private var _activeDataNetworkCellInfo: CellInfo? = null
    private var _nrConnectionState: NRConnectionState = NRConnectionState.NOT_AVAILABLE

    override fun extractActiveCellInfo(cellInfo: MutableList<CellInfo>, signalStrength: SignalStrength?): ActiveDataCellInfo {
        _nrConnectionState = NRConnectionState.NOT_AVAILABLE
        val dataSimSubscriptionId = subscriptionManager.getCurrentDataSubscriptionId()
        var dualSimDecisionLog = ""
        var dualSimDecision = ""

        cellInfo.forEach {
            Timber.d("5G ADCI cell: $it")
        }

        _activeDataNetwork = null
        if (dataSimSubscriptionId != INVALID_SUBSCRIPTION_ID) {
            val registeredInfoList = cellInfo.filter { it.isRegistered }

            var subscriptions: List<SubscriptionInfo>? = null
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                subscriptions = subscriptionManager.activeSubscriptionInfoList
                subscriptions?.forEachIndexed { index, it ->
                    // TODO this is not proved solution, need to find another way to connect CellInfo and SubscriptionInfo
                    if (dataSimSubscriptionId == it.subscriptionId && (registeredInfoList.size > index || registeredInfoList.size == 1)) {

                        var networkType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            val manager = telephonyManager.createForSubscriptionId(dataSimSubscriptionId)
                            if (NRConnectionState.getNRConnectionState(manager) != NRConnectionState.NOT_AVAILABLE) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    _nrConnectionState = NRConnectionState.getNRConnectionState(manager)
                                    when (_nrConnectionState) {
                                        NRConnectionState.NOT_AVAILABLE -> manager.dataNetworkType
                                        NRConnectionState.AVAILABLE -> 40// ServerNetworkType.TYPE_5G_NR_AVAILABLE.intValue
                                        NRConnectionState.NSA -> 41 // ServerNetworkType.TYPE_5G_NR_NSA.intValue
                                        NRConnectionState.SA -> TelephonyManager.NETWORK_TYPE_NR
                                    }
                                } else {
                                    manager.dataNetworkType
                                }
                            } else {
                                manager.dataNetworkType
                            }
                        } else {
                            // Todo: problem if operators are the same for both SIM cards (e.g. roaming network), but solving problems with different Networks (if user has no restriction on the usage of the network type for data or voice sim then it should use the same)
                            val networkTypeCheck =
                                connectivityManager.cellNetworkInfoCompat(telephonyManager.networkOperatorName)?.networkType
                                    ?: MobileNetworkType.UNKNOWN
                            if (networkTypeCheck == MobileNetworkType.UNKNOWN) {
                                telephonyManager.networkType
                            } else {
                                networkTypeCheck.ordinal
                            }
                        }

                        val mobileNetworkType = MobileNetworkType.fromValue(networkType)
                        val dataCellTechnology = CellTechnology.fromMobileNetworkType(mobileNetworkType)

                        // single sim
                        if (subscriptions.size == 1) {
                            _activeDataNetworkCellInfo = registeredInfoList[0]
                        } else {
                            // dual sim handling
                            it.displayName
                            dualSimDecision =
                                "$DUAL_SIM_METHOD_API\nDATA_SIM: slotIndex: ${it.simSlotIndex} carrierName: ${it.carrierName} displayName: ${it.displayName}\n"
                            // we need to check which of the registered cells uses same type of the network as data sim
                            var dualSimRegistered = registeredInfoList.filter { cellInfo ->
                                var sameNetworkType = false
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    when (cellInfo) {
                                        // 5G connections
                                        is CellInfoNr ->
                                            sameNetworkType = CellTechnology.CONNECTION_5G == dataCellTechnology
                                        // 3G connections
                                        is CellInfoTdscdma -> {
                                            sameNetworkType = CellTechnology.CONNECTION_3G == dataCellTechnology
                                        }
                                    }
                                }
                                if (sameNetworkType) {
                                    sameNetworkType
                                } else {
                                    when (cellInfo) {
                                        // 4G connections
                                        is CellInfoLte -> {
                                            CellTechnology.CONNECTION_4G == dataCellTechnology
                                        }
                                        // 3G connections
                                        is CellInfoWcdma -> {
                                            CellTechnology.CONNECTION_3G == dataCellTechnology
                                        }
                                        // 2G connections
                                        is CellInfoCdma -> {
                                            CellTechnology.CONNECTION_2G == dataCellTechnology
                                        }
                                        is CellInfoGsm -> {
                                            CellTechnology.CONNECTION_2G == dataCellTechnology
                                        }
                                        else -> false
                                    }
                                }
                            }
                            val countAfterNetworkTypeFilter = dualSimRegistered.size
                            if (registeredInfoList.size > dualSimRegistered.size) {
                                dualSimDecisionLog += "DSD - filtered according to same network type from ${registeredInfoList.size} to $countAfterNetworkTypeFilter\n"
                                dualSimDecision += "CELL_INFO: filtered according to: same network type from ${registeredInfoList.size} to $countAfterNetworkTypeFilter\n"
                            }
                            // if there is still more than one we can try filter it according to network operator name
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                if (dualSimRegistered.size > 1) {
                                    dualSimRegistered = dualSimRegistered.filter { cellInfo ->
                                        val networkOperator = it.carrierName.toString()
                                        when (cellInfo) {
                                            is CellInfoNr -> networkOperator.contentEquals(cellInfo.cellIdentity.operatorAlphaLong.toString()) || networkOperator.contentEquals(
                                                cellInfo.cellIdentity.operatorAlphaShort.toString()
                                            )
                                            is CellInfoLte -> networkOperator.contentEquals(cellInfo.cellIdentity.operatorAlphaLong.toString()) || networkOperator.contentEquals(
                                                cellInfo.cellIdentity.operatorAlphaShort.toString()
                                            )
                                            is CellInfoWcdma -> networkOperator.contentEquals(cellInfo.cellIdentity.operatorAlphaLong.toString()) || networkOperator.contentEquals(
                                                cellInfo.cellIdentity.operatorAlphaShort.toString()
                                            )
                                            is CellInfoCdma -> networkOperator.contentEquals(cellInfo.cellIdentity.operatorAlphaLong.toString()) || networkOperator.contentEquals(
                                                cellInfo.cellIdentity.operatorAlphaShort.toString()
                                            )
                                            is CellInfoGsm -> networkOperator.contentEquals(cellInfo.cellIdentity.operatorAlphaLong.toString()) || networkOperator.contentEquals(
                                                cellInfo.cellIdentity.operatorAlphaShort.toString()
                                            )
                                            else -> false
                                        }
                                    }
                                }
                            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                if (dualSimRegistered.size > 1) {
                                    dualSimRegistered = dualSimRegistered.filter { cellInfo ->
                                        val networkOperator = it.carrierName.toString()
                                        when (cellInfo) {
                                            is CellInfoLte -> networkOperator.contentEquals(cellInfo.cellIdentity.operatorAlphaLong.toString()) || networkOperator.contentEquals(
                                                cellInfo.cellIdentity.operatorAlphaShort.toString()
                                            )
                                            is CellInfoWcdma -> networkOperator.contentEquals(cellInfo.cellIdentity.operatorAlphaLong.toString()) || networkOperator.contentEquals(
                                                cellInfo.cellIdentity.operatorAlphaShort.toString()
                                            )
                                            is CellInfoCdma -> networkOperator.contentEquals(cellInfo.cellIdentity.operatorAlphaLong.toString()) || networkOperator.contentEquals(
                                                cellInfo.cellIdentity.operatorAlphaShort.toString()
                                            )
                                            is CellInfoGsm -> networkOperator.contentEquals(cellInfo.cellIdentity.operatorAlphaLong.toString()) || networkOperator.contentEquals(
                                                cellInfo.cellIdentity.operatorAlphaShort.toString()
                                            )
                                            else -> false
                                        }
                                    }
                                }
                            }

                            if (countAfterNetworkTypeFilter > dualSimRegistered.size) {
                                dualSimDecisionLog += "DSD - filtered according to same network operator name as in data subscription info $countAfterNetworkTypeFilter to ${dualSimRegistered.size}\n"
                                dualSimDecision += "CELL_INFO: filtered according to: same network operator name as in data subscription info $countAfterNetworkTypeFilter to ${dualSimRegistered.size}\n"
                            }

                            if (dualSimRegistered.size == 1) {
                                _activeDataNetworkCellInfo = dualSimRegistered[0]
                                dualSimDecisionLog += "DSD - SUCCESS! \n Filtered this: \n\n$_activeDataNetworkCellInfo\n\n\n"
                                dualSimDecision += "CELL_INFO: SUCCESS! $_activeDataNetworkCellInfo"
                            } else {
                                dualSimDecisionLog += "DSD - FAILED! \n Unable to select one data cell info!"
                                dualSimDecision += "CELL_INFO: FAILED!"
                            }
                            Timber.v(dualSimDecisionLog)
                        }

                        // apply fix for 5G NSA when there is 4G connection as anchor connection and 5G is used as secondary one while 5G cell is signalled as non registered
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            if (_nrConnectionState == NRConnectionState.NSA) {
                                val onlyNRCells = cellInfo.filterIsInstance<CellInfoNr>()
                                val sortedNRCellsBySignalDesc = onlyNRCells.sortedByDescending { it.cellSignalStrength.dbm }
                                _activeDataNetworkCellInfo = if (sortedNRCellsBySignalDesc.isEmpty()) {
                                    _activeDataNetworkCellInfo
                                } else {
                                    sortedNRCellsBySignalDesc.first()
                                }
                            }
                        }

                        // apply fix for 5G NSA when there is 4G connection as anchor connection and 5G is used as secondary one while 5G cell is signalled as non registered for android 12
                        if (Build.VERSION.SDK_INT >= 31) {
                            val onlyNRCells = cellInfo.filterIsInstance<CellInfoNr>()
                            val sortedNRCellsBySignalDesc = onlyNRCells.sortedByDescending { it.cellSignalStrength.dbm }
                            _activeDataNetworkCellInfo = if (sortedNRCellsBySignalDesc.isEmpty()) {
                                _activeDataNetworkCellInfo
                            } else {
                                Timber.d("5G override NR connection state, because NR cell found")
                                _nrConnectionState = NRConnectionState.NSA
                                sortedNRCellsBySignalDesc.first()
                            }
                        }

                        // apply fix for 5G NSA when there is 4G connection as anchor connection and 5G is used as secondary one while 5G cell is not available but 5G signal is returned with 4G signal in onSignalStrengthChange for android 12
                        if (Build.VERSION.SDK_INT >= 31) {
                            val onlyNRCells = cellInfo.filterIsInstance<CellInfoNr>()
                            if (onlyNRCells.isEmpty()) {
                                val signalStrengths = signalStrength?.cellSignalStrengths?.filterIsInstance<CellSignalStrengthNr>()
                                _activeDataNetworkCellInfo = if (signalStrengths?.isNullOrEmpty() != true) {
                                    _nrConnectionState = NRConnectionState.NSA
                                    networkType = 41 // ServerNetworkType.TYPE_5G_NR_NSA.intValue
                                    _activeDataNetworkCellInfo
                                } else {
                                    _activeDataNetworkCellInfo
                                }
                            }
                        }

                        // Timber.v("Cell: $_cellInfo, Network_type: $networkType, MNT: ${MobileNetworkType.fromValue(networkType)}" )
                        _activeDataNetwork = CellNetworkInfo.from(
                            _activeDataNetworkCellInfo,
                            it,
                            MobileNetworkType.fromValue(networkType),
                            true,
                            connectivityManager.activeNetworkInfo?.isRoaming ?: false,
                            connectivityManager.activeNetworkInfo?.extraInfo,
                            if (subscriptions.size > 1) dualSimDecisionLog else null
                        )
                    }
                }
            }
        }
        return ActiveDataCellInfo(
            dualSimDecision = _dualSimDecision,
            nrConnectionState = _nrConnectionState,
            activeDataNetworkCellInfo = _activeDataNetworkCellInfo,
            activeDataNetwork = _activeDataNetwork
        )
    }
}

fun SubscriptionManager.getCurrentDataSubscriptionId(): Int {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        SubscriptionManager.getDefaultDataSubscriptionId()
    } else {
        val clazz = this::class.java
        try {
            val method = clazz.getMethod("getDefaultDataSubId")
            method.invoke(this) as Int
        } catch (ex: Throwable) {
            Timber.e(ex)
            -1
        }
    }
}

fun ConnectivityManager.cellNetworkInfoCompat(operatorName: String?): CellNetworkInfo? {
    val info = activeNetworkInfo
    Timber.i("type: ${info?.type}")
    Timber.i("typeName: ${info?.typeName}")
    Timber.i("subtype: ${info?.subtype}")
    Timber.i("subtypeName: ${info?.subtypeName}")
    Timber.i("detailed state ${info?.detailedState?.name}")

    return if (info == null || !info.isConnected || info.type != ConnectivityManager.TYPE_MOBILE) {
        null
    } else {
        CellNetworkInfo(
            providerName = operatorName ?: "",
            band = null,
            networkType = MobileNetworkType.fromValue(info.subtype),
            cellUUID = UUID.nameUUIDFromBytes(info.detailedState.name.toByteArray()).toString(),
            mnc = null,
            mcc = null,
            locationId = null,
            areaCode = null,
            scramblingCode = null,
            isActive = true,
            isRegistered = true,
            isRoaming = info.isRoaming,
            apn = info.extraInfo,
            signalStrength = null,
            dualSimDetectionMethod = null
        )
    }
}
