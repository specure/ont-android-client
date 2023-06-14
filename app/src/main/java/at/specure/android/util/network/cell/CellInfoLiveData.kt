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

import androidx.lifecycle.LiveData
import at.specure.android.util.network.cell.CellInfoWatcher
import at.specure.android.util.network.cell.CellNetworkInfo
import javax.inject.Inject

/**
 * LiveData that observes changes of cellular network from [CellInfoWatcher]
 * If no active or cellular connection is available null well be posted
 */
class CellInfoLiveData @Inject constructor(private val cellInfoWatcher: CellInfoWatcher) : LiveData<CellNetworkInfo?>(),
    CellInfoWatcher.CellInfoChangeListener {

    override fun onCellInfoChanged(activeNetwork: CellNetworkInfo?) {
        postValue(activeNetwork)
    }

    override fun onActive() {
        super.onActive()
        cellInfoWatcher.addListener(this)
    }

    override fun onInactive() {
        super.onInactive()
        cellInfoWatcher.removeListener(this)
    }
}