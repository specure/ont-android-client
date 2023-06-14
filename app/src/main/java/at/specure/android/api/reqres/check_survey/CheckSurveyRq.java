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

package at.specure.android.api.reqres.check_survey;


import com.google.gson.Gson;
import com.google.gson.JsonObject;

import androidx.annotation.NonNull;
import at.specure.android.api.jsons.CheckSurveyPost;

/**
 * Created by michal.cadrik on 7/27/2017.
 */

@SuppressWarnings("UnnecessaryLocalVariable")
public class CheckSurveyRq {

    private CheckSurveyPost requestObject;

    public CheckSurveyRq(@NonNull CheckSurveyPost requestObject) {
        this.requestObject = requestObject;
    }

    public JsonObject createRequest() {
        if (requestObject == null) throw new RuntimeException("CheckSurvey is null");

        Gson gson = new Gson();
        JsonObject requestJson = gson.toJsonTree(this.requestObject).getAsJsonObject();
        return requestJson;
    }

}
