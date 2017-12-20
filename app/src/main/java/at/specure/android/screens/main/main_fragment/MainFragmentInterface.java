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

import android.content.Context;

import at.specure.android.screens.main.MainActivity;
import at.specure.android.test.TestService;

/**
 * Interface for main activity
 * Created by michal.cadrik on 10/23/2017.
 */

public interface MainFragmentInterface {

    Context getContext();

    MainActivity getMainActivity();

    void changeScreenState(MainScreenState screenState, String logInfoMessage, Boolean forceUIRefresh);


    // test performing methods

    boolean isTestVisible();

    void setPacketLossText(String packetLossText);

    void setJitterText(String jitter);

    void setSignalValue(Integer signal);

    void showQosResults(int testsCount, int testsFailed, int successPercentage);

    void setPingText(String pingStr);

    void setQosTestProgress(int i, boolean showArrow);

    void setTestProgress(int i);

    void setSpeedGaugeProgress(int i);

    void setTestTextProgress(String i);

    void updateDownloadGraph(String value, int downloadStatusStringID, int unitStringID);

    void updateUploadGraph(String value, int uploadStatusStringID, int unitStringID);

    void showQoSProgress(TestService testTestService);

    void setTestUUID(String testUUID);
}
