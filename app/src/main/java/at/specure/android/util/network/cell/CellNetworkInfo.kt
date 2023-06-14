/*
 * Licensed under the Apache License, Version 2.0 (the "License");
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

package at.specure.android.util.network.cell

import android.os.Build
import android.telephony.CellIdentityCdma
import android.telephony.CellIdentityGsm
import android.telephony.CellIdentityLte
import android.telephony.CellIdentityNr
import android.telephony.CellIdentityWcdma
import android.telephony.CellInfo
import android.telephony.CellInfoCdma
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.CellInfoNr
import android.telephony.CellInfoWcdma
import android.telephony.CellSignalStrengthNr
import android.telephony.SubscriptionInfo
import androidx.annotation.RequiresApi
import at.specure.android.util.network.TransportType
import at.specure.info.band.CellBand
import at.specure.android.util.network.network.MobileNetworkType
import at.specure.android.util.network.network.NetworkInfo
import at.specure.info.strength.SignalStrengthInfo
import java.util.UUID

/**
 * Cellular Network information
 */
class CellNetworkInfo(

    /**
     * Provider or sim operator name
     */
    val providerName: String,

    /**
     * Cell band information of current network
     */
    val band: CellBand?,

    /**
     * Detailed Cellular Network type
     */
    val networkType: MobileNetworkType,

    val mnc: Int?,

    val mcc: Int?,

    val locationId: Int?,

    val areaCode: Int?,

    val scramblingCode: Int?,

    val isRegistered: Boolean,

    val isActive: Boolean,

    val isRoaming: Boolean,

    val apn: String?,

    val signalStrength: SignalStrengthInfo?,

    val dualSimDetectionMethod: String?,

    /**
     * Random generated cell UUID
     */
    cellUUID: String
) :
    NetworkInfo(TransportType.CELLULAR, cellUUID) {

    override val name: String?
        get() = providerName

    companion object {

        fun from(
            info: CellInfo,
            subscriptionInfo: SubscriptionInfo?,
            isActive: Boolean,
            isRoaming: Boolean,
            apn: String?,
            dualSimDetectionMethod: String?
        ): CellNetworkInfo {
            val networkType: MobileNetworkType = when {
                info is CellInfoLte -> MobileNetworkType.LTE
                info is CellInfoWcdma -> MobileNetworkType.HSPAP
                info is CellInfoCdma -> MobileNetworkType.CDMA
                info is CellInfoGsm -> MobileNetworkType.GSM
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && info is CellInfoNr -> MobileNetworkType.NR
                else -> throw IllegalArgumentException("Unknown cell info cannot be extracted ${info::class.java.name}")
            }
            return from(info, subscriptionInfo, networkType, isActive, isRoaming, apn, dualSimDetectionMethod)
        }

        /**
         * Creates [CellNetworkInfo] from initial data objects
         */
        fun from(
            info: CellInfo?,
            subscriptionInfo: SubscriptionInfo?,
            networkType: MobileNetworkType,
            isActive: Boolean,
            isRoaming: Boolean,
            apn: String?,
            dualSimDetectionMethod: String?
        ): CellNetworkInfo {
            val providerName = subscriptionInfo?.carrierName?.toString() ?: ""

            return when (networkType) {
                MobileNetworkType.NR,
                MobileNetworkType.NR_NSA ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && info is CellInfoNr) {
                        fromNr(
                            info,
                            providerName,
                            networkType,
                            isActive,
                            isRoaming,
                            apn,
                            dualSimDetectionMethod
                        )
                    } else {
                        fromUnknown(providerName, networkType, isActive, isRoaming, apn, dualSimDetectionMethod)
                    }
                MobileNetworkType.NR_AVAILABLE ->
                    when (info) {
                        is CellInfoLte -> fromLte(info, providerName, networkType, isActive, isRoaming, apn, dualSimDetectionMethod)
                        else -> fromUnknown(providerName, networkType, isActive, isRoaming, apn, dualSimDetectionMethod)
                    }
                else ->
                    when {
                        info is CellInfoLte -> fromLte(info, providerName, networkType, isActive, isRoaming, apn, dualSimDetectionMethod)
                        info is CellInfoWcdma -> fromWcdma(info, providerName, networkType, isActive, isRoaming, apn, dualSimDetectionMethod)
                        info is CellInfoGsm -> fromGsm(info, providerName, networkType, isActive, isRoaming, apn, dualSimDetectionMethod)
                        info is CellInfoCdma -> fromCdma(info, providerName, networkType, isActive, isRoaming, apn, dualSimDetectionMethod)
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && info is CellInfoNr -> fromNr(
                            info,
                            providerName,
                            networkType,
                            isActive,
                            isRoaming,
                            apn,
                            dualSimDetectionMethod
                        )
                        else -> fromUnknown(providerName, networkType, isActive, isRoaming, apn, dualSimDetectionMethod)
                    }
            }
        }

        private fun fromUnknown(
            providerName: String,
            networkType: MobileNetworkType,
            isActive: Boolean,
            isRoaming: Boolean,
            apn: String?,
            dualSimDetectionMethod: String?
        ): CellNetworkInfo {

            return CellNetworkInfo(
                providerName = providerName,
                band = null,
                networkType = networkType,
                mcc = null,
                mnc = null,
                locationId = null,
                areaCode = null,
                scramblingCode = null,
                cellUUID = "",
                isRegistered = false,
                isActive = isActive,
                isRoaming = isRoaming,
                apn = apn,
                signalStrength = null,
                dualSimDetectionMethod = dualSimDetectionMethod
            )
        }

        @RequiresApi(Build.VERSION_CODES.Q)
        private fun fromNr(
            info: CellInfoNr,
            providerName: String,
            networkType: MobileNetworkType,
            isActive: Boolean,
            isRoaming: Boolean,
            apn: String?,
            dualSimDetectionMethod: String?
        ): CellNetworkInfo {

            val identity = info.cellIdentity as CellIdentityNr

            val band = CellBand.fromChannelNumber(identity.nrarfcn, CellChannelAttribution.NRARFCN)

            return CellNetworkInfo(
                providerName = providerName,
                band = band,
                networkType = networkType,
                mcc = identity.mccCompat(),
                mnc = identity.mncCompat(),
                locationId = null,
                areaCode = identity.tac.fixValue(),
                scramblingCode = identity.pci,
                cellUUID = info.uuid(),
                isActive = isActive,
                isRegistered = info.isRegistered,
                isRoaming = isRoaming,
                apn = apn,
                signalStrength = SignalStrengthInfo.from(info.cellSignalStrength as CellSignalStrengthNr),
                dualSimDetectionMethod = dualSimDetectionMethod
            )
        }

        private fun fromLte(
            info: CellInfoLte,
            providerName: String,
            networkType: MobileNetworkType,
            isActive: Boolean,
            isRoaming: Boolean,
            apn: String?,
            dualSimDetectionMethod: String?
        ): CellNetworkInfo {

            val band: CellBand? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                CellBand.fromChannelNumber(info.cellIdentity.earfcn, CellChannelAttribution.EARFCN)
            } else {
                null
            }

            return CellNetworkInfo(
                providerName = providerName,
                band = band,
                networkType = networkType,
                mcc = info.cellIdentity.mccCompat(),
                mnc = info.cellIdentity.mncCompat(),
                locationId = info.cellIdentity.ci.fixValue(),
                areaCode = info.cellIdentity.tac.fixValue(),
                scramblingCode = info.cellIdentity.pci,
                cellUUID = info.uuid(),
                isRegistered = info.isRegistered,
                isActive = isActive,
                isRoaming = isRoaming,
                apn = apn,
                signalStrength = SignalStrengthInfo.from(info.cellSignalStrength),
                dualSimDetectionMethod = dualSimDetectionMethod
            )
        }

        private fun fromWcdma(
            info: CellInfoWcdma,
            providerName: String,
            networkType: MobileNetworkType,
            isActive: Boolean,
            isRoaming: Boolean,
            apn: String?,
            dualSimDetectionMethod: String?
        ): CellNetworkInfo {
            val band: CellBand? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                CellBand.fromChannelNumber(info.cellIdentity.uarfcn, CellChannelAttribution.UARFCN)
            } else {
                null
            }

            return CellNetworkInfo(
                providerName = providerName,
                band = band,
                networkType = networkType,
                mcc = info.cellIdentity.mccCompat(),
                mnc = info.cellIdentity.mncCompat(),
                locationId = info.cellIdentity.cid.fixValue(),
                areaCode = info.cellIdentity.lac.fixValue(),
                scramblingCode = info.cellIdentity.psc,
                cellUUID = info.uuid(),
                isActive = isActive,
                isRegistered = info.isRegistered,
                isRoaming = isRoaming,
                apn = apn,
                signalStrength = SignalStrengthInfo.from(info.cellSignalStrength),
                dualSimDetectionMethod = dualSimDetectionMethod
            )
        }

        private fun fromGsm(
            info: CellInfoGsm,
            providerName: String,
            networkType: MobileNetworkType,
            isActive: Boolean,
            isRoaming: Boolean,
            apn: String?,
            dualSimDetectionMethod: String?
        ): CellNetworkInfo {
            val band: CellBand? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                CellBand.fromChannelNumber(info.cellIdentity.arfcn, CellChannelAttribution.ARFCN)
            } else {
                null
            }

            val scramblingCode: Int? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) info.cellIdentity.bsic else null

            return CellNetworkInfo(
                providerName = providerName,
                band = band,
                networkType = networkType,
                mcc = info.cellIdentity.mccCompat(),
                mnc = info.cellIdentity.mncCompat(),
                locationId = info.cellIdentity.cid.fixValue(),
                areaCode = info.cellIdentity.lac.fixValue(),
                scramblingCode = scramblingCode,
                cellUUID = info.uuid(),
                isActive = isActive,
                isRegistered = info.isRegistered,
                isRoaming = isRoaming,
                apn = apn,
                signalStrength = SignalStrengthInfo.from(info.cellSignalStrength),
                dualSimDetectionMethod = dualSimDetectionMethod
            )
        }

        private fun fromCdma(
            info: CellInfoCdma,
            providerName: String,
            networkType: MobileNetworkType,
            isActive: Boolean,
            isRoaming: Boolean,
            apn: String?,
            dualSimDetectionMethod: String?
        ): CellNetworkInfo {

            return CellNetworkInfo(
                providerName = providerName,
                band = null,
                networkType = networkType,
                mcc = null,
                mnc = null,
                locationId = null,
                areaCode = info.cellIdentity.basestationId.fixValue(),
                scramblingCode = null,
                cellUUID = info.uuid(),
                isActive = isActive,
                isRegistered = info.isRegistered,
                isRoaming = isRoaming,
                apn = apn,
                signalStrength = SignalStrengthInfo.from(info.cellSignalStrength),
                dualSimDetectionMethod = dualSimDetectionMethod
            )
        }
    }
}

fun CellInfo.uuid(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        when (this) {
            is CellInfoLte -> cellIdentity.uuid()
            is CellInfoWcdma -> cellIdentity.uuid()
            is CellInfoGsm -> cellIdentity.uuid()
            is CellInfoCdma -> cellIdentity.uuid()
            is CellInfoNr -> (cellIdentity as CellIdentityNr).uuid()
            else -> throw IllegalArgumentException("Unknown cell info cannot be extracted ${javaClass.name}")
        }
    } else {
        when (this) {
            is CellInfoLte -> cellIdentity.uuid()
            is CellInfoWcdma -> cellIdentity.uuid()
            is CellInfoGsm -> cellIdentity.uuid()
            is CellInfoCdma -> cellIdentity.uuid()
            else -> throw IllegalArgumentException("Unknown cell info cannot be extracted ${javaClass.name}")
        }
    }
}

fun SubscriptionInfo.mccCompat(): Int? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        mccString?.toInt().fixValue()
    } else {
        mcc.fixValue()
    }

fun SubscriptionInfo.mncCompat(): Int? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        mncString?.toInt().fixValue()
    } else {
        mnc.fixValue()
    }

@RequiresApi(Build.VERSION_CODES.Q)
private fun CellIdentityNr.uuid(): String {
    val id = buildString {
        append("nr")
        append(nci)
        append(pci)
    }.toByteArray()
    return UUID.nameUUIDFromBytes(id).toString()
}

private fun CellIdentityLte.uuid(): String {
    val id = buildString {
        append("lte")
        append(ci)
        append(pci)
    }.toByteArray()
    return UUID.nameUUIDFromBytes(id).toString()
}

private fun CellIdentityWcdma.uuid(): String {
    val id = buildString {
        append("wcdma")
        append(cid)
    }.toByteArray()
    return UUID.nameUUIDFromBytes(id).toString()
}

private fun CellIdentityGsm.uuid(): String {
    val id = buildString {
        append("gsm")
        append(cid)
    }.toByteArray()
    return UUID.nameUUIDFromBytes(id).toString()
}

private fun CellIdentityCdma.uuid(): String {
    val id = buildString {
        append("cdma")
        append(networkId)
        append(systemId)
    }.toByteArray()
    return UUID.nameUUIDFromBytes(id).toString()
}

@RequiresApi(Build.VERSION_CODES.Q)
private fun CellIdentityNr.mccCompat(): Int? = mccString?.toInt().fixValue()

@RequiresApi(Build.VERSION_CODES.Q)
private fun CellIdentityNr.mncCompat(): Int? = mncString?.toInt().fixValue()

private fun CellIdentityLte.mccCompat(): Int? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        mccString?.toInt().fixValue()
    } else {
        mcc.fixValue()
    }

private fun CellIdentityLte.mncCompat(): Int? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        mncString?.toInt().fixValue()
    } else {
        mnc.fixValue()
    }

private fun CellIdentityWcdma.mccCompat(): Int? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        mccString?.toInt().fixValue()
    } else {
        mcc.fixValue()
    }

private fun CellIdentityWcdma.mncCompat(): Int? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        mncString?.toInt().fixValue()
    } else {
        mnc.fixValue()
    }

private fun CellIdentityGsm.mccCompat(): Int? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        mccString?.toInt().fixValue()
    } else {
        mcc.fixValue()
    }

private fun CellIdentityGsm.mncCompat(): Int? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        mncString?.toInt().fixValue()
    } else {
        mnc.fixValue()
    }

fun Int?.fixValue(): Int? {
    return if (this == null || this == Int.MIN_VALUE || this == Int.MAX_VALUE || this < 0) {
        null
    } else {
        this
    }
}