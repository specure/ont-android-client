/*******************************************************************************
 * Copyright 2014-2017 Specure GmbH
 * Copyright 2013-2014 alladin-IT GmbH
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
 ******************************************************************************/
package at.specure.android.screens.map;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

import at.specure.androidX.data.map_filter.data.MapFilterTypes;

import static at.specure.androidX.data.map_filter.data.MapFilterTypes.MAP_FILTER_TYPE_UNKNOWN;

/**
 * @author bp
 */
public class MapListSection implements Serializable {
    private static final long serialVersionUID = 1L;

    @SerializedName("title")
    private String title;

    /**
     * @see MapFilterTypes
     */
    @SerializedName("type")
    private String type = MAP_FILTER_TYPE_UNKNOWN;

    @SerializedName("options")
    private List<MapListEntry> mapListEntryList;

    public MapListSection(final String title, final String type, final List<MapListEntry> mapListEntryList) {

        setTitle(title);
        setMapListEntryList(mapListEntryList);
        if (type != null) {
            setType(type);
        }
    }

    public MapListSection(final String title, final List<MapListEntry> mapListEntryList) {

        setTitle(title);
        setMapListEntryList(mapListEntryList);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {

        return title;
    }

    public void setTitle(final String title) {

        this.title = title;
    }

    public List<MapListEntry> getMapListEntryList() {

        return mapListEntryList;
    }

    public void setMapListEntryList(final List<MapListEntry> mapListEntryList) {

        this.mapListEntryList = mapListEntryList;
//        for (final MapListEntry entry : mapListEntryList)
//            entry.setSection(this);
    }

    public MapListEntry getCheckedMapListEntry() {

        for (final MapListEntry entry : mapListEntryList)
            if (entry.isChecked())
                return entry;

        return null;
    }

    /**
     * This is only for purposes of adding key after it its parsed from server because it is not coming from server
     * @param map_options
     */
    public void setListKey(String map_options) {
        for (final MapListEntry entry : mapListEntryList)
            entry.setKey(map_options);
    }
}
