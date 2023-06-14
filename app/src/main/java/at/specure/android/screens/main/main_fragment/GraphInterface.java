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

package at.specure.android.screens.main.main_fragment;

/**
 * Created by michal.cadrik on 12/6/2017.
 */

public interface GraphInterface {

    void updateDownloadGraph(String value, int downloadStatusStringID, int unitStringID);

    void updateUploadGraph(String value, int uploadStatusStringID, int unitStringID);

    int getUnitStringId();

    int getUploadStatusStringId();

    int getDownloadStatusStringId();


}
