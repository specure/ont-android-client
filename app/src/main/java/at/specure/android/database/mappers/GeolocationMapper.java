/*******************************************************************************
 * Copyright 2014-2017 Specure GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package at.specure.android.database.mappers;

import java.util.ArrayList;
import java.util.List;

import at.specure.android.api.jsons.Location;
import at.specure.android.database.obj.TLocation;

/**
 * Created by michal.cadrik on 8/17/2017.
 */

public class GeolocationMapper {

    /**
     *
     * @param locations
     * @return
     */
    public List<Location> map(List<TLocation> locations) {
        ArrayList<Location> result = null;
        if (locations != null) {
            result = new ArrayList<>();
            for (TLocation location: locations) {
                result.add(new Location(location));
            }

        }
        return result;
    }
}
