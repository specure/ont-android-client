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

package at.specure.android.api.jsons;

import android.content.Context;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import at.specure.android.configs.ConfigHelper;
import at.specure.android.screens.map.MapListEntry;
import at.specure.android.screens.map.MapListSection;
import at.specure.androidX.data.map_filter.data.MapFilterTypes;

/**
 * Used in map filters for operators only for this moment
 * Created by michal.cadrik on 10/24/2017.
 */

public class FilterGroup {

    @SerializedName("title")
    public String groupTitle;

    @SerializedName("options")
    public ArrayList<FilterOperator> filterOperators;


    public MapListSection convertOperatorsToMapListSection(Context context) {
        boolean oneSelected = false;
        ArrayList<MapListEntry> lists = new ArrayList<>();
        if (filterOperators != null) {
            for (FilterOperator operator : filterOperators) {
                String idToStore = operator.id == null ? "" : operator.id.toString();
                String selectedOperatorInMapFilter = ConfigHelper.getSelectedOperatorInMapFilter(context);
                boolean selected = selectedOperatorInMapFilter.equalsIgnoreCase(idToStore);

                if (!oneSelected) {
                    if (selected) {
                        oneSelected = true;
                    }
                } else {
                    selected = false;
                }

                MapListEntry provider = new MapListEntry(operator.title, operator.detail, selected, "operator", idToStore, operator.isDefault != null);
                provider.setOverlayType(MapFilterTypes.MAP_FILTER_TYPE_OPERATOR);
                lists.add(provider);
            }
        }
        if ((!oneSelected)&& (lists.size() > 0)) {
            lists.get(0).setChecked(true);
        }

        return new MapListSection(groupTitle, MapFilterTypes.MAP_FILTER_TYPE_OPERATOR, lists);
    }
}
