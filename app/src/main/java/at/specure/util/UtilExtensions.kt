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

package at.specure.util

import android.net.Network

/**
 * Returns network id from netId variable that is hidden in Android API but returns in toString method
 */
fun Network.id(): Int = toString().toInt()

/**
 * Runs for each sequence in synchronized block
 */
inline fun <T> Iterable<T>.synchronizedForEach(action: (T) -> Unit) {
    synchronized(this) {
        for (element in this) action(element)
    }
}