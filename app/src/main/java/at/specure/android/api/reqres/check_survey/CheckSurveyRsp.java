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

import com.google.gson.annotations.SerializedName;

/**
 * Created by michal.cadrik on 7/27/2017.
 */

public class CheckSurveyRsp {

    @SerializedName("survey_url")
    public String surveyUrl;

    @SerializedName("is_filled_up")
    public Boolean filledUp;
}
