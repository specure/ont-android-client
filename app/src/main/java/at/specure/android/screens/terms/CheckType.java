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

package at.specure.android.screens.terms;

import com.specure.opennettest.R;

import at.specure.android.constants.AppConstants;

public enum CheckType {
    NDT(R.raw.ndt_info, AppConstants.PAGE_TITLE_NDT_CHECK,
            R.string.terms_ndt_header, R.string.terms_ndt_accept_text, false),
    INFORMATION_COMMISSIONER(R.raw.ic_info, AppConstants.PAGE_TITLE_CHECK_INFORMATION_COMMISSIONER,
            R.string.terms_ic_header, R.string.terms_ic_accept_text, true),
    TERMS_AND_PRIVACY(R.raw.tc, "", R.string.terms_ic_header, R.string.terms_accept_text, false);

    private final int templateFile;
    private final String fragmentTag;
    private final boolean defaultIsChecked;
    private final int titleId;
    private final int textId;

    CheckType(final int templateFile, final String fragmentTag, final int titleId, final int textId, final boolean defaultIsChecked) {
        this.templateFile = templateFile;
        this.fragmentTag = fragmentTag;
        this.titleId = titleId;
        this.textId = textId;
        this.defaultIsChecked = defaultIsChecked;
    }

    public int getTemplateFile() {
        return templateFile;
    }

    public String getFragmentTag() {
        return fragmentTag;
    }

    public int getTitleId() {
        return titleId;
    }

    public int getTextId() {
        return textId;
    }

    public boolean isDefaultIsChecked() {
        return defaultIsChecked;
    }
}
