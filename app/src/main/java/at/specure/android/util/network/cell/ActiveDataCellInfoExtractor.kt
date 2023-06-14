package at.specure.android.util.network.cell

import android.telephony.CellInfo
import android.telephony.SignalStrength

interface ActiveDataCellInfoExtractor {

    fun extractActiveCellInfo(cellInfo: MutableList<CellInfo>, signalStrength: SignalStrength?): ActiveDataCellInfo
}