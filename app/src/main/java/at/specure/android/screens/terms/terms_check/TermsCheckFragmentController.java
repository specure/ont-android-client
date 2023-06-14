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

package at.specure.android.screens.terms.terms_check;

import android.app.Activity;
import android.content.Context;

import at.specure.android.configs.ConfigHelper;
import at.specure.android.screens.terms.CheckType;

class TermsCheckFragmentController {

    private TermsCheckFragmentInterface termsCheckFragmentInterface;
    private CheckType type;

    TermsCheckFragmentController(TermsCheckFragmentInterface termsCheckFragmentInterface) {
        this.termsCheckFragmentInterface = termsCheckFragmentInterface;
    }

    void onAcceptTermsAction(Activity activity, boolean firstTime, CheckType followedByCheckType) {
        ConfigHelper.setTCAccepted(activity, true);
        if (firstTime) {
            termsCheckFragmentInterface.checkSettings(true, null);
            final boolean wasNDTTermsNecessary = termsCheckFragmentInterface.showChecksIfNecessary();
            if (!wasNDTTermsNecessary)
                termsCheckFragmentInterface.initApp(false);
        } else if (followedByCheckType != null) {
            switch (followedByCheckType) {
                case INFORMATION_COMMISSIONER:
                    termsCheckFragmentInterface.showIcCheck();
                    break;
                case NDT:
                    termsCheckFragmentInterface.showNdtCheck();
                    break;
            }
        }
    }

    void onRejectPressed(Context context) {
        ConfigHelper.setTCAccepted(context, false);
        ConfigHelper.setUUID(context, "");
    }

    boolean isAcceptedToC(Context context) {
        return ConfigHelper.isTCAccepted(context);
    }

    public CheckType getType() {
        return type;
    }
}
