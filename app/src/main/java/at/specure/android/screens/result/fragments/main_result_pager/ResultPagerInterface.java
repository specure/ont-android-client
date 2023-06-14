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

package at.specure.android.screens.result.fragments.main_result_pager;


import at.specure.android.screens.main.main_activity_interfaces.MapInterface;
import at.specure.android.views.ExtendedViewPager;

public interface ResultPagerInterface {

    MapInterface getMapInterface();

    ExtendedViewPager getViewPager();

    void scrollToTabTab(int tabIndex);
}
