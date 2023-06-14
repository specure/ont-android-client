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

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TabHost;

import at.specure.android.screens.main.main_activity_interfaces.ExpandedResultInterface;
import at.specure.android.screens.main.main_activity_interfaces.HelpInterface;
import at.specure.android.screens.main.main_activity_interfaces.MapInterface;
import at.specure.android.screens.result.adapter.result.ResultPagerAdapter;
import at.specure.android.views.ExtendedViewPager;

public class ResultPagerController implements TabHost.OnTabChangeListener {

    public final static boolean MAP_INDICATOR_DYNAMIC_VISIBILITY = false;
    public static final String ARG_TEST_UUID = "test_uuid";
    private Handler handler = new Handler();


    private ResultPagerInterface resultPagerInterface;

    ResultPagerController(ResultPagerInterface resultPagerInterface) {
        this.resultPagerInterface = resultPagerInterface;
    }

    ResultPagerAdapter getPagerAdapter(Activity activity, String uuid, MapInterface mapInterface, HelpInterface helpInterface, ExpandedResultInterface expandedResultInterface, Bundle savedState) {
        if ((activity != null) && (mapInterface != null) && (helpInterface != null) && (expandedResultInterface != null)) {
            return new ResultPagerAdapter(activity, handler, uuid, mapInterface, helpInterface, expandedResultInterface, savedState);
        } else
            return null;
    }

    @Override
    public void onTabChanged(String tabId) {
        int tabIndex = Integer.valueOf(tabId);
        if (resultPagerInterface != null) {
            ExtendedViewPager viewPager = resultPagerInterface.getViewPager();
            if (viewPager != null && (tabIndex != viewPager.getCurrentItem())) {
                resultPagerInterface.scrollToTabTab(tabIndex);
                viewPager.setCurrentItem(tabIndex);
            }
        }
    }
}
