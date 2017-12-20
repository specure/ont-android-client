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

/**
 * @author bp
 */
public class MapListEntry implements Serializable {
    private static final long serialVersionUID = 1L;

    @SerializedName("title")
    private String title;

    @SerializedName("summary")
    private String summary;

    @SerializedName("map_options")
    private String value;

    @SerializedName("overlay_type")
    private String overlayType;

    @SerializedName("default")
    private boolean _default = false;

    //this is not received from server, must be set after json is parsed
    @SerializedName("map_options_key")
    private String key = "map_options";
//    private MapListSection section;
    private boolean checked;


    public MapListEntry(final String title, final String summary) {
        this(title, summary, false, null, null, false);
    }

    public MapListEntry(final String title, final String summary, final String key, final String value) {
        this(title, summary, false, key, value, false);
    }

    public MapListEntry(final String title, final String summary, final boolean checked, final String key,
                        final String value, final boolean _default) {
        setTitle(title);
        setSummary(summary);
        setKey(key);
        setValue(value);
        setChecked(checked);
        setDefault(_default);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(final String summary) {
        this.summary = summary;
    }

    public String getKey() {
        return key;
    }

    public void setKey(final String key) {
        if (key == null || key.length() == 0)
            return;

        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

//    public MapListSection getSection() {
//        return section;
//    }
//
//    public void setSection(final MapListSection section) {
//        this.section = section;
//    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(final boolean checked) {
        this.checked = checked;
    }

    public void setDefault(boolean _default) {
        this._default = _default;
    }

    public boolean isDefault() {
        return _default;
    }

    @Override
    public String toString() {
        return title + ", " + summary;
    }

    public void setOverlayType(String overlayType) {
        this.overlayType = overlayType;
    }

    public String getOverlayType() {
        return overlayType;
    }

}
