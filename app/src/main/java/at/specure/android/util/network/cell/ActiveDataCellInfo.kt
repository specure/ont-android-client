package at.specure.android.util.network.cell

import android.telephony.CellInfo
import at.specure.android.util.network.network.NRConnectionState

data class ActiveDataCellInfo(
    val dualSimDecision: String = "",
    val activeDataNetwork: CellNetworkInfo? = null,
    val activeDataNetworkCellInfo: CellInfo? = null,
    val nrConnectionState: NRConnectionState = NRConnectionState.NOT_AVAILABLE
)