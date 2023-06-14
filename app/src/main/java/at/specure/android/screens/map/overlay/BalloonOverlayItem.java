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
package at.specure.android.screens.map.overlay;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonArray;

public class BalloonOverlayItem
{
    
    protected JsonArray resultItems;
    private String title;
    
    public BalloonOverlayItem(final LatLng point, final String title, final JsonArray resultItems)
    {
//        super(point, title, "");
        this.resultItems = resultItems;
        this.title = title;
    }
    
    public JsonArray getResultItems()
    {
        return resultItems;
    }
    
    public void setResultItems(final JsonArray resultItems)
    {
        this.resultItems = resultItems;
    }

    public CharSequence getTitle()
    {
        return title;
    }
    
}
