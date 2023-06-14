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

package at.specure.android.api.reqres.zero_measurements;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import at.specure.android.api.jsons.ZeroMeasurement;
import at.specure.android.api.jsons.ZeroMeasurementPost;

/**
 * Created by michal.cadrik on 7/27/2017.
 */

@SuppressWarnings("UnnecessaryLocalVariable")
public class ZeroMeasurementsPostRq {

    private ZeroMeasurementPost requestObject;

    public ZeroMeasurementsPostRq(@NonNull ZeroMeasurementPost requestObject) {
        List<ZeroMeasurement> zeroMeasurements = requestObject.getZeroMeasurements();
        //modification because user can run app which could not be registered on server and detect zero measurements
        ArrayList<ZeroMeasurement> zeroMeasurementsWithUUID = new ArrayList<>();
        for (ZeroMeasurement zeroM : zeroMeasurements) {
            if ((zeroM.getClientUuid() != null) && (!zeroM.getClientUuid().isEmpty())) {
                zeroMeasurementsWithUUID.add(zeroM);
            }
        }
        requestObject.setZeroMeasurements(zeroMeasurementsWithUUID);
        this.requestObject = requestObject;

    }

    public JsonObject createRequest() {
        if (requestObject == null) throw new RuntimeException("Zero measurements request is null");

        Gson gson = new Gson();
        JsonObject requestJson = gson.toJsonTree(this.requestObject).getAsJsonObject();
        return requestJson;
    }

}
