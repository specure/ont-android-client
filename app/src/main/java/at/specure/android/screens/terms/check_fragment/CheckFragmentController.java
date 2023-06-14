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

package at.specure.android.screens.terms.check_fragment;

import android.app.Activity;
import android.content.Context;
import java.io.IOException;
import java.io.InputStream;

import androidx.fragment.app.FragmentActivity;
import at.specure.android.configs.ConfigHelper;
import at.specure.android.screens.terms.CheckType;

class CheckFragmentController {

    private CheckFragmentInterface checkFragmentInterface;

    CheckFragmentController(CheckFragmentInterface checkFragmentInterface) {
        this.checkFragmentInterface = checkFragmentInterface;
    }

    void onAcceptAction(CheckType checkType, Activity activity, boolean accepted) {
        switch (checkType) {
            case INFORMATION_COMMISSIONER:
                ConfigHelper.setInformationCommissioner(activity, accepted);
                ConfigHelper.setICDecisionMade(activity, true);
                break;
            case NDT:
                ConfigHelper.setNDT(activity, accepted);
                ConfigHelper.setNDTDecisionMade(activity, true);
                break;
        }
        if (checkFragmentInterface != null) {
            checkFragmentInterface.closeFragment(accepted);
        }
    }

    void onBackAction(FragmentActivity activity) {
        activity.getSupportFragmentManager().popBackStack();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    String getCheckHTMLFileContent(CheckType checkType, Context context) {
        String content = "";
        if ((context != null) && checkType != null) {
            try {
                InputStream inputStream = context.getResources().openRawResource(checkType.getTemplateFile());
                byte[] buffer = new byte[inputStream.available()];
                inputStream.read(buffer);
                content = new String(buffer);
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return content;
    }

}
