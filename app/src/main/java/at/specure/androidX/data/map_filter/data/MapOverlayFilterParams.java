package at.specure.androidX.data.map_filter.data;

import at.specure.android.screens.map.MapProperties;

public class MapOverlayFilterParams {

    String urlEnding;
    String parametersToAdd;
    String overlayType;

    public MapOverlayFilterParams(String urlEnding, String parametersToAdd, String overlayType) {
        this.urlEnding = urlEnding;
        this.parametersToAdd = parametersToAdd;
        this.overlayType = overlayType;
    }

    public String getMapFilterUrl() {
        if ((parametersToAdd != null) && (!parametersToAdd.isEmpty())) {
            return MapProperties.MapServerUrl + "/tiles/"+ urlEnding /*+ "&" + parametersToAdd*/;
        } else {
            return MapProperties.MapServerUrl + "/tiles/"+ urlEnding;
        }
    }
}
