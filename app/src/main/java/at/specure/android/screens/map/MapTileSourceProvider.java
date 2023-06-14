/*******************************************************************************
 * Copyright 2014-2017 Specure GmbH
 * Copyright 2013-2015 alladin-IT GmbH
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

import android.content.Context;

import com.google.android.gms.maps.model.UrlTileProvider;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;
import java.util.Map;

import at.specure.androidX.data.map_filter.mappers.MapFilterSaver;
import timber.log.Timber;

/**
 * 
 * @author bp
 * 
 */
public class MapTileSourceProvider extends UrlTileProvider
{
    private final String protocol;
    private final String host;
    private final int port;
    
    private final int tileSize;
    
    private String path;
    
    public MapTileSourceProvider(final String protocol, final String host, final int port, final int tileSize)
    {
        super(tileSize, tileSize);
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        
        this.tileSize = tileSize;
        
    }

    public String getMapBoxTileUrl(Context context) {
        URI uri = null;
        try {
            path = MapFilterSaver.getActiveMapFilterOverlayUrl(context);
            String options = MapFilterSaver.getActiveMapFilterParams(context);
            uri = new URI(protocol, null, host, port, path,
//            uri = new URI(protocol, null, host, port, path + "{z}/{x}/{y}.png",
                    String.format(Locale.US, "%s&point_diameter=%d&size=%d",
                            options, MapProperties.POINT_DIAMETER, tileSize),
                    null);

//            System.out.println(uri.toASCIIString());
            String s = uri.toString();

            Timber.e("Map_overlay_source %s", s);
            String[] split = s.split("\\?");
            String url = split[0] + "/{z}/{x}/{y}.png?" + split[1];
            Timber.e("Map_overlay_source %s", url);
            return url;
        } catch (final URISyntaxException e) {
            e.printStackTrace();
        }
        return null;

    }

    @Override
    public URL getTileUrl(int x, int y, int zoom)
    {
        URI uri = null;
        try
        {
            //this is dummy - historical reason - google maps artifact
            String options = "";
            uri = new URI(protocol, null, host, port,path,
                    String.format(Locale.US, "%spath=%d/%d/%d&point_diameter=%d&size=%d",
                    options, zoom, x, y, MapProperties.POINT_DIAMETER, tileSize),
                    null);
            
//            System.out.println(uri.toASCIIString());
            Timber.e("MAP_URI %s", uri.toString());
            return uri.toURL();
        }
        catch (final URISyntaxException e)
        {
            e.printStackTrace();
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        return null;
    }

}
