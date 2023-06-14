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
package at.specure.android.screens.main;

import android.content.Context;
import android.content.res.Resources;

import com.specure.opennettest.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import at.specure.android.configs.BadgesConfig;
import at.specure.android.configs.FeatureConfig;
import at.specure.android.configs.SurveyConfig;

public class MainMenuUtil {

    /**
     *
     */
    public static final int MENU_STATISTICS_INDEX = 3;

    public static List<String> getMenuTitles(final Resources res, Context context) {
        final List<String> menuList = new ArrayList<String>();
        Collections.addAll(menuList, res.getStringArray(R.array.navigation_main_titles));
        if (!FeatureConfig.showStatisticInMainMenu(context)) {
            if ((!FeatureConfig.USE_OPENDATA) || (FeatureConfig.SHOW_ONLY_BASIC_MENU)) {
                menuList.remove(context.getResources().getString(R.string.menu_button_statistics));
            }
        }
//        if (Build.MANUFACTURER.contentEquals("Amazon")) {
//            if ("lo" == "lo") {
//            menuList.remove(context.getResources().getString(R.string.page_title_map));
//        }
        if (BadgesConfig.isBadgesFeatureEnabled(context)) {
            menuList.add(context.getResources().getString(R.string.title_badges));
        }
        if (SurveyConfig.isSurveyEnabledInApp(context) && SurveyConfig.isSurveyActive(context)) {
            menuList.add(context.getResources().getString(R.string.menu_button_survey));
        }

        return menuList;
    }

    public static List<Integer> getMenuIds(Context context) {
        final List<Integer> menuIds = new ArrayList<Integer>();
        final Integer[] ids;
        if (FeatureConfig.SHOW_ONLY_BASIC_MENU) {
            ids = new Integer[]{
                    R.drawable.ic_action_home,
                    R.drawable.ic_action_history,
                    R.drawable.ic_action_map,
                    R.drawable.ic_action_stat,
                    R.drawable.ic_action_help,
                    R.drawable.ic_action_about,
                    R.drawable.ic_action_settings};
            Collections.addAll(menuIds, ids);
//            if (Build.MANUFACTURER.contentEquals("Amazon")) {
//            if ("lo" == "lo") {
//                menuIds.remove((Integer) R.drawable.ic_action_map);
//            }
        } else {
            ids = new Integer[]{
                    R.drawable.ic_action_home,
                    R.drawable.ic_action_history,
                    R.drawable.ic_action_map,
                    R.drawable.ic_action_stat,
                    R.drawable.ic_action_help,
                    R.drawable.ic_action_about,
                    R.drawable.ic_action_settings,
                    R.drawable.ic_action_about};
            Collections.addAll(menuIds, ids);
//            if (Build.MANUFACTURER.contentEquals("Amazon")) {
//            if ("lo" == "lo") {
//                menuIds.remove((Integer) R.drawable.ic_action_map);
//            }
        }
        if ((!FeatureConfig.USE_OPENDATA) || (!FeatureConfig.showStatisticInMainMenu(context))){
            menuIds.remove((Object) R.drawable.ic_action_stat);
        }
        if (BadgesConfig.isBadgesFeatureEnabled(context)) {
            menuIds.add(R.drawable.ic_action_cup);
        }
        if (SurveyConfig.isSurveyEnabledInApp(context) && SurveyConfig.isSurveyActive(context)) {
            menuIds.add(R.drawable.ic_action_survey);
        }
        return menuIds;
    }


    public static List<Integer> getMenuActionIds(Context context) {
        final List<Integer> menuIds = new ArrayList<Integer>();

        final Integer[] ids = new Integer[]{
                R.id.action_title_page,
                R.id.action_history,
                R.id.action_map,
                R.id.action_stats,
                R.id.action_help,
                R.id.action_info,
                R.id.action_settings};
//                R.id.action_netstat,
//                R.id.action_log};

        Collections.addAll(menuIds, ids);


        if (((!FeatureConfig.USE_OPENDATA) || (!FeatureConfig.showStatisticInMainMenu(context))) || ((FeatureConfig.SHOW_ONLY_BASIC_MENU) && (!FeatureConfig.showStatisticInMainMenu(context)))) {
            menuIds.remove((Integer) R.id.action_stats);
        }

//        if (Build.MANUFACTURER.contentEquals("Amazon")) {
//            menuIds.remove((Integer) R.id.action_map);
//        }
        if (BadgesConfig.isBadgesFeatureEnabled(context)) {
            menuIds.add(R.id.action_badges);
        }
        if (SurveyConfig.isSurveyEnabledInApp(context) && SurveyConfig.isSurveyActive(context)) {
            menuIds.add(R.id.action_survey);
        }
        return menuIds;
    }
}
