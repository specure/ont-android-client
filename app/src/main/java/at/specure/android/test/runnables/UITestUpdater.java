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

package at.specure.android.test.runnables;

import android.os.Handler;

import at.specure.android.configs.ConfigHelper;
import at.specure.android.test.TestService;
import at.specure.android.test.UIUpdateInterface;

/**
 * Created by michal.cadrik on 5/18/2017.
 */

public class UITestUpdater implements Runnable {

    private static final long UI_UPDATE_DELAY = 100;

    private Boolean qosMode, stopLoop;
    private UIUpdateInterface uiUpdater;
    private TestService testService;
    private Handler handler;

    public UITestUpdater(Boolean qosMode, Boolean stopLoop, UIUpdateInterface uiUpdater, TestService testService, Handler handler) {
        this.qosMode = qosMode;
        this.stopLoop = stopLoop;
        this.uiUpdater = uiUpdater;
        this.testService = testService;
        this.handler = handler;
    }

    @Override
    public void run() {
        if (uiUpdater == null)
            return;

        if (qosMode && ConfigHelper.isQosEnabled(uiUpdater.getContext())) {
            this.uiUpdater.updateQoSUI();
        } else {
            this.uiUpdater.updateTestUI();
        }

        if (testService != null) {
            if (testService.isCompleted() && testService.getTestUuid() != null) {
                uiUpdater.showResultDelayed();
//                handler.postDelayed(resultSwitcherRunnable, 300);
            }
        }

        if (!stopLoop)
            handler.postDelayed(this, UI_UPDATE_DELAY);
    }

    public void setQoSModeEnabled(boolean qoSModeEnabled) {
        this.qosMode = qoSModeEnabled;
    }

    public void setStopLoop(boolean stopLoop) {
        this.stopLoop = stopLoop;
    }

    public void setRMBTService(TestService testService) {
        this.testService = testService;
    }
}
