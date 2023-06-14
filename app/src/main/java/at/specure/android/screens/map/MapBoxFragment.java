/*
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
 */
package at.specure.android.screens.map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;

import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.markerview.MarkerViewManager;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.RasterLayer;
import com.mapbox.mapboxsdk.style.sources.RasterSource;
import com.mapbox.mapboxsdk.style.sources.Source;
import com.mapbox.mapboxsdk.style.sources.TileSet;
import com.specure.opennettest.R;

import java.util.ArrayList;
import java.util.List;

import at.specure.android.api.calls.CheckMarkerTask;
import at.specure.android.configs.ConfigHelper;
import at.specure.android.configs.FeatureConfig;
import at.specure.android.configs.MapConfig;
import at.specure.android.configs.PermissionHandler;
import at.specure.android.constants.AppConstants;
import at.specure.android.screens.main.MainActivity;
import at.specure.android.screens.map.map_filter_x.FilterListActivity;
import at.specure.android.screens.map.overlay.BalloonOverlayItem;
import at.specure.android.screens.map.overlay.BalloonOverlayView;
import at.specure.android.util.EndTaskListener;
import at.specure.android.util.location.GeoLocationX;
import at.specure.android.util.location.LocationChangeListener;
import at.specure.android.util.location.RequestGPSPermissionInterface;
import at.specure.androidX.data.map_filter.data.MapOverlay;
import at.specure.androidX.data.map_filter.mappers.MapFilterSaver;
import at.specure.androidX.data.map_filter.view_data.FilterGroup;
import timber.log.Timber;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MapBoxFragment extends BaseMapBoxSupportFragment implements MapInterface, MapboxMap.OnMapLongClickListener, OnClickListener, RequestGPSPermissionInterface, LocationChangeListener, OnMapReadyCallback, MapboxMap.OnMapClickListener, MapboxMap.OnCameraMoveListener, MapboxMap.OnCameraIdleListener, MapboxMap.OnCameraMoveStartedListener, LocationListener {

    public final static String OPTION_SHOW_INFO_TOAST = "show_info_toast";
    public final static String OPTION_ENABLE_ALL_GESTURES = "enable_all_gestures";
    public final static String OPTION_ENABLE_CONTROL_BUTTONS = "enable_control_buttons";
    public final static String OPTION_ENABLE_OVERLAY = "enable_overlay";
    public final static int PERM_REQ_LOC_FINE = 88;

    private final List<com.mapbox.mapboxsdk.maps.OnMapReadyCallback> mapReadyCallbackList = new ArrayList<>();

    private MapView mapboxMapView;
    private TileOverlay heatmapOverlay;
    private TileOverlay pointsOverlay;
    private TileOverlay shapesOverlay;
    private Location myLocation;

    private boolean firstStart = true;
    private RMBTMapFragmentOptions options = new RMBTMapFragmentOptions();
    private int mapType;
    private int orientation;
    private RelativeLayout mapViewContainer;
    private MainActivity mainActivity;
    private MapboxMap mapboxMap;
    private RasterSource heatmapsource;
    private MapTileSourceProvider heatmapProvider;
    private MapTileSourceProvider pointsProvider;
    private RasterSource pointsource;
    private MapTileSourceProvider shapesProvider;
    private com.mapbox.mapboxsdk.camera.CameraPosition cameraPosition;
    private boolean run = false;
    private boolean needPointsOverlay;
    private boolean needShapesOverlay;
    private boolean needHeatmapOverlay;
    private boolean cameraMoving = false;
    private LocationComponent locationComponent;

    private List<FilterGroup> filterGroups;
    Observer<List<FilterGroup>> observer = new Observer<List<FilterGroup>>() {
        @Override
        public void onChanged(@Nullable List<FilterGroup> badges) {
            if (badges != null) {
                Timber.i("Observing map filter change: %s", badges);
                filterGroups = badges;
                restartMap();
            }
        }
    };
    private MarkerViewManager markerViewManager;
    private FrameLayout balloonContainer;

    private void mapReady() {
        Timber.e("map ready start");
        MainActivity activity = (MainActivity) getActivity();
        if (((mapboxMap != null) && mapType == MapConfig.MAP_TYPE_MAPBOX)) {
            if (firstStart) {
                final Bundle bundle = getArguments();

                TypedValue latitudeVal = new TypedValue();
                getResources().getValue(R.dimen.map_center_latitude, latitudeVal, true);
                float latitude = latitudeVal.getFloat();

                TypedValue longitudeVal = new TypedValue();
                getResources().getValue(R.dimen.map_center_longitude, longitudeVal, true);
                float longitude = longitudeVal.getFloat();
                LatLng latLng = new LatLng(latitude, longitude);

                TypedValue zoomVal = new TypedValue();
                getResources().getValue(R.dimen.map_zoom_level, zoomVal, true);
                float zoom = zoomVal.getFloat();

                LatLng initialCenter = null;
                if (bundle != null) {
                    initialCenter = bundle.getParcelable("initialCenter");

                    if (initialCenter != null) {
                        latLng = initialCenter;
                        zoom = MapProperties.POINT_MAP_ZOOM;
                        Timber.e("marker added");
                    }
                }


                Timber.e("setting location on map");
                if (initialCenter == null) {
                    boolean enabledGPS = GeoLocationX.getInstance(activity.getApplication()).isGeolocationEnabled(activity);
                    if ((MapConfig.getInitialPointWhenGPSOff(mainActivity) == MapConfig.MAP_GPS_OFF_INITIAL_POINT_RESOURCE)) {
                        if (!enabledGPS) {
                            latLng = new LatLng(latitude, longitude);
                        } else {
//                            if (Build.MANUFACTURER.contentEquals("Amazon")) {
//                                myLocation = GPSConfig.getLastKnownLocation(mainActivity, this);
//                            } else {
                            myLocation = GeoLocationX.getInstance(mainActivity.getApplication()).getLastKnownLocation(mainActivity, this);
//                            }
                            //TODO: GET LAST KNOWN LOCATION HERE

                            if (myLocation != null) {
                                latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                            } else {
                                latLng = new LatLng(latitude, longitude);
                            }
                        }
                    } else if (MapConfig.getInitialPointWhenGPSOff(mainActivity) == MapConfig.MAP_GPS_OFF_INITIAL_POINT_LAST_KNOWN_LOCATION) {

//                        if (Build.MANUFACTURER.contentEquals("Amazon")) {
//                            myLocation = GPSConfig.getLastKnownLocation(mainActivity, this);
//                        } else {
                        myLocation = GeoLocationX.getInstance(mainActivity.getApplication()).getLastKnownLocation(mainActivity, this);
//                        }
                        //TODO: GET LAST KNOWN LOCATION HERE

                        if (myLocation != null) {
                            latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                        } else {
                            latLng = new LatLng(latitude, longitude);
                        }
                    }
                }
                String selectedCountryInMapFilter = ConfigHelper.getSelectedCountryInMapFilter(activity);

                if (selectedCountryInMapFilter.isEmpty() || selectedCountryInMapFilter.equals("all")) {
                    final com.mapbox.mapboxsdk.camera.CameraPosition position = new com.mapbox.mapboxsdk.camera.CameraPosition.Builder()
                            .target(new com.mapbox.mapboxsdk.geometry.LatLng(latLng.latitude, latLng.longitude)) // Sets the new camera position
                            .zoom(zoom) // Sets the zoom
                            .build();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mapboxMap.removeOnCameraMoveListener(MapBoxFragment.this);
                            mapboxMap.animateCamera(com.mapbox.mapboxsdk.camera.CameraUpdateFactory.newCameraPosition(position));
                            mapboxMap.addOnCameraMoveListener(MapBoxFragment.this);
                        }
                    }, 300);
                }
                Timber.e("marker centered to location");

                if (activity != null) {
                    if (!(ActivityCompat.checkSelfPermission(activity, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                        setUpLocationListener(activity, false);
                    }
                }
            }

            com.mapbox.mapboxsdk.maps.UiSettings uiSettings = uiSettings = mapboxMap.getUiSettings();
            uiSettings.setCompassEnabled(false);
            uiSettings.setTiltGesturesEnabled(false);
            uiSettings.setRotateGesturesEnabled(false);
            uiSettings.setScrollGesturesEnabled(options.isEnableAllGestures());
            Timber.e("setting up listeners");
            mapboxMap.addOnMapClickListener(this);
            mapboxMap.addOnMapLongClickListener(this);
            Timber.e("setting up listeners done");

            if (activity != null) {
                if (!(ActivityCompat.checkSelfPermission(activity, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                    Timber.e("adding location listener");
                }
            }

            if (activity != null) {
                mapboxMap.setStyle(MapFilterSaver.getChosenMapLayout(activity), new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        initMapLayers(activity);
                        setUpLocation(style);
                    }
                });
            }
        }

        if ((mapboxMap != null) && (cameraPosition != null)) {
            mapboxMap.setCameraPosition(cameraPosition);
        }
        if (mapboxMap != null) {
            mapboxMap.addOnCameraIdleListener(this);
        }

        if (activity != null) {
            setActionBarItems(activity);
        }
        firstStart = false;
    }

    private void setUpLocation(Style style) {
        LocationComponentActivationOptions locationComponentActivationOptions = LocationComponentActivationOptions
                .builder(mainActivity, style)
                .build();

        locationComponent = mapboxMap.getLocationComponent();
        locationComponent.activateLocationComponent(locationComponentActivationOptions);
        if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationComponent.setLocationComponentEnabled(true);
        }
    }

    private void initMapLayers(MainActivity activity) {
        needHeatmapOverlay = false;
        needPointsOverlay = false;
        needShapesOverlay = false;

        String mapOverlayType = MapOverlay.MAP_FILTER_HEATMAP;
        if (activity != null) {
            mapOverlayType = MapFilterSaver.getActiveMapFilterOverlayType(activity.getApplicationContext());
        }
        if (mapOverlayType != null) {

            if (mapOverlayType.equalsIgnoreCase(MapOverlay.MAP_FILTER_AUTOMATIC)) {
                needPointsOverlay = false;
                needShapesOverlay = false;
                needHeatmapOverlay = true;
            } else if (mapOverlayType.equalsIgnoreCase(MapOverlay.MAP_FILTER_HEATMAP)) {
                needPointsOverlay = false;
                needShapesOverlay = false;
                needHeatmapOverlay = true;
            } else if (mapOverlayType.equalsIgnoreCase(MapOverlay.MAP_FILTER_POINTS)) {
                needPointsOverlay = true;
                needShapesOverlay = false;
                needHeatmapOverlay = false;
            } else if ((mapOverlayType.equalsIgnoreCase(MapOverlay.MAP_FILTER_REGIONS))
                    || (mapOverlayType.equalsIgnoreCase(MapOverlay.MAP_FILTER_SETTLEMENTS))
                    || (mapOverlayType.equalsIgnoreCase(MapOverlay.MAP_FILTER_MUNICIPALITIES))
                    || (mapOverlayType.equalsIgnoreCase(MapOverlay.MAP_FILTER_WHITESPOTS))) {

                //TODO: this was set to true, why???
                needPointsOverlay = true;

                needShapesOverlay = true;
                needHeatmapOverlay = false;
            }

        }

        run = false;
        if (!run) {
            run = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    initializeOverlays(needHeatmapOverlay, needPointsOverlay, needShapesOverlay);
                }
            }, 500);
        }
    }

    private void removeOverlays() {
        Timber.e("removeing overlays");
        run = false;
        if ((mapboxMap != null) && (mapboxMap.getStyle() != null)) {
            Layer layer = mapboxMap.getStyle().getLayer("heatmap");
            Source source = mapboxMap.getStyle().getSource("heatmapsource-id");
            if (layer != null) {
                if (source != null) {
                    mapboxMap.getStyle().removeLayer(layer);
                    mapboxMap.getStyle().removeSource(source);
                }
            }
            layer = mapboxMap.getStyle().getLayer("shapetiles");
            source = mapboxMap.getStyle().getSource("shapesource-id");
            if (layer != null) {
                if (source != null) {
                    mapboxMap.getStyle().removeLayer(layer);
                    mapboxMap.getStyle().removeSource(source);
                }
            }
            layer = mapboxMap.getStyle().getLayer("pointtiles");
            source = mapboxMap.getStyle().getSource("pointssource-id");
            if (layer != null) {
                if (source != null) {
                    mapboxMap.getStyle().removeLayer(layer);
                    mapboxMap.getStyle().removeSource(source);
                }
            }
        }
    }

    private void addLayerToTheMap(RasterLayer rasterLayer) {
        try {
            mapboxMap.getStyle().addLayerBelow(rasterLayer, MapFilterSaver.getChosenMapLayoutLayer(getContext().getApplicationContext()));
        } catch (Exception ignored) {
            mapboxMap.getStyle().addLayer(rasterLayer);
        }
    }


    private void initializeOverlays(boolean needHeatmapOverlay, boolean needPointsOverlay, boolean needShapesOverlay) {
//        try {
        Timber.e("adding overlays");
        Context context = getContext();
        if (!options.isEnableOverlay()) {
            needHeatmapOverlay = false;
            needShapesOverlay = false;
            needPointsOverlay = false;
        }
        if ((context != null) && (mapboxMap.getStyle() != null)) {
            if (!MapConfig.pointsOverlayEnabled(context)) {
                needPointsOverlay = false;
            }

            final String protocol = ConfigHelper.isMapSeverSSL(getActivity()) ? "https" : "http";
            final String host = ConfigHelper.getMapServerName(getActivity());
            final int port = ConfigHelper.getMapServerPort(getActivity());

            pointsProvider = new MapTileSourceProvider(protocol, host, port, MapProperties.TILE_SIZE * 2);
            heatmapProvider = new MapTileSourceProvider(protocol, host, port, MapProperties.TILE_SIZE);
            shapesProvider = new MapTileSourceProvider(protocol, host, port, MapProperties.TILE_SIZE);

            if (needHeatmapOverlay) {

                Layer layer = mapboxMap.getStyle().getLayer("heatmap");
                Source source = mapboxMap.getStyle().getSource("heatmapsource-id");
                if (layer == null) {
                    if (source == null) {
                        heatmapsource = new RasterSource("heatmapsource-id", new TileSet("2.1.0", heatmapProvider.getMapBoxTileUrl(context.getApplicationContext())), MapProperties.TILE_SIZE);
                        mapboxMap.getStyle().addSource(heatmapsource);
                    }
                    Timber.e("add heatmap source");
                    if (!needPointsOverlay) {
                        mapboxMap.getStyle().removeLayer("pointtiles");
                    }
                    if (!needShapesOverlay) {
                        mapboxMap.getStyle().removeLayer("shapetiles");
                    }
                    RasterLayer rasterLayer = new RasterLayer("heatmap", "heatmapsource-id");
//                        mapboxMap.addLayerAbove(rasterLayer, "admin-country-disputed");
                    addLayerToTheMap(rasterLayer);
                    Timber.e("add heatmap layer");
                }

            }

            if (needShapesOverlay) {
                Layer layer = mapboxMap.getStyle().getLayer("shapetiles");
                Source source = mapboxMap.getStyle().getSource("shapesource-id");
                if (layer == null) {
                    if (source == null) {
                        pointsource = new RasterSource("shapesource-id", new TileSet("2.1.0", pointsProvider.getMapBoxTileUrl(context.getApplicationContext())), MapProperties.TILE_SIZE);
                        mapboxMap.getStyle().addSource(pointsource);
                    }
                    Timber.e("add heatmap source");
                    if (!needPointsOverlay) {
                        mapboxMap.getStyle().removeLayer("pointtiles");
                    }
                    if (!needHeatmapOverlay) {
                        mapboxMap.getStyle().removeLayer("maptiles");
                    }
                    RasterLayer rasterLayer = new RasterLayer("shapetiles", "shapesource-id");
//                        mapboxMap.addLayerAbove(rasterLayer, "admin-country-disputed");
                    addLayerToTheMap(rasterLayer);
                    Timber.e("add shapes layer");
                }

            }

            if (needPointsOverlay) {
                Layer layer = mapboxMap.getStyle().getLayer("pointtiles");
                Source source = mapboxMap.getStyle().getSource("pointssource-id");
                if (layer == null) {
                    if (source == null) {
                        pointsource = new RasterSource("pointssource-id", new TileSet("2.1.0", pointsProvider.getMapBoxTileUrl(context.getApplicationContext())), MapProperties.TILE_SIZE);
                        mapboxMap.getStyle().addSource(pointsource);
                    }
                    Timber.e("add heatmap source");
                    if (!needHeatmapOverlay) {
                        mapboxMap.getStyle().removeLayer("maptiles");
                    }
                    if (!needShapesOverlay) {
                        mapboxMap.getStyle().removeLayer("shapetiles");
                    }
                    RasterLayer rasterLayer = new RasterLayer("pointtiles", "pointssource-id");
//                        mapboxMap.addLayerAbove(rasterLayer, "admin-country-disputed");
                    addLayerToTheMap(rasterLayer);
                    Timber.e("add points layer");
                }
            }
        }
    }


    public void removeMapListeners() {
        if (mapboxMap != null) {
            mapboxMap.removeOnCameraIdleListener(this);
            mapboxMap.removeOnCameraMoveListener(this);
            mapboxMap.removeOnCameraMoveStartedListener(this);
        }
    }

    public void addMapListeners() {
        if (mapboxMap != null) {
            mapboxMap.addOnCameraIdleListener(this);
            mapboxMap.addOnCameraMoveStartedListener(this);
            mapboxMap.addOnCameraMoveListener(this);
        }
    }


    @NonNull
    private Icon getMapboxMarkerIcon(MainActivity activity) {
        return IconFactory.getInstance(activity).fromResource(R.drawable.map_marker);
    }

    /**
     * !!! check permission for location before usage of this method
     */
    @SuppressLint("MissingPermission")
    @RequiresPermission(anyOf = {ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION})
    private void setUpLocationListener(MainActivity activity, boolean force) {

//        if (Build.MANUFACTURER.contentEquals("Amazon")) {
//           GPSConfig.getLastKnownLocation(mainActivity, this);
//        } else {
//            if (force) {
//                GeoLocationFused.getInstance().startForce(activity);
//            } else {
//                GeoLocationFused.getInstance().start(activity);
//            }
//            GeoLocationFused.getInstance().addListener(this);
        GeoLocationX.getInstance(mainActivity.getApplication()).getLastKnownLocation(mainActivity, this);
//        }


    }

//    private void changeMapScrollToSelectedCountry(String selectedCountryInMapFilter) {
//        List<Address> ru;
//        Geocoder geocoder = new Geocoder(this.getActivity());
//        try {
//            ru = geocoder.getFromLocationName(selectedCountryInMapFilter, 1);
//            if (ru != null && ru.size() > 0) {
//                double latitude1 = ru.get(0).getLatitude();
//                double longitude1 = ru.get(0).getLongitude();
//
//                LatLng latLng1 = new LatLng(latitude1, longitude1);
//
//                if (mapboxMap != null) {
//                    mapboxMap.moveCamera(com.mapbox.mapboxsdk.camera.CameraUpdateFactory.newLatLngZoom(new com.mapbox.mapboxsdk.geometry.LatLng(latitude1, longitude1), 5f));
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }


    @Override
    public String setActionBarTitle() {
        if (this.isAdded())
            return getString(R.string.page_title_map);
        else return "";
    }


    @Override
    public void setActionBarItems(Context context) {
        if (context != null && context instanceof MainActivity) {
            Timber.e("MENU setvisibleMenuItems MapBoxFragment");
//            ((MainActivity) context).setVisibleMenuItems(R.id.action_menu_map_info, R.id.action_menu_map_filter, R.id.action_menu_map_settings, R.id.action_menu_my_position);
        }
    }


    @Override
    public void onCameraMove() {

        cameraMoving = true;
        if (pointsOverlay != null) {
            boolean automaticShowPoints = false;
            if (mapboxMap != null) {
                com.mapbox.mapboxsdk.camera.CameraPosition cameraPositionBuf = mapboxMap.getCameraPosition();
                if (cameraPositionBuf.target != null) {
                    if ((cameraPosition.target.getLatitude() != 0d) && (cameraPosition.target.getLongitude() != 0d)) {
                        this.cameraPosition = mapboxMap.getCameraPosition();
                    }
                }
                automaticShowPoints = this.cameraPosition.zoom >= MapProperties.MAP_AUTO_SWITCH_VALUE;
            }
            if (automaticShowPoints && !pointsOverlay.isVisible())
                pointsOverlay.setVisible(true);
            else if (!automaticShowPoints && pointsOverlay.isVisible())
                pointsOverlay.setVisible(false);
        }
    }

    @Override
    public void requestPermission(int requestCodeFine) {
        checkForGPSPermissions(requestCodeFine, false);
    }

    @Override
    public void onLocationChange(Location location, String decodedLocation, boolean enabledGPS) {
        onMyLocationChange(location);
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        markerViewManager = new MarkerViewManager(mapboxMapView, mapboxMap);
        for (com.mapbox.mapboxsdk.maps.OnMapReadyCallback onMapReadyCallback : mapReadyCallbackList) {
            onMapReadyCallback.onMapReady(mapboxMap);
        }
        this.mapboxMap.setStyle(new Style.Builder().fromUri(getString(R.string.mapbox_style_url)));
        mapReady();
    }

    @Override
    public boolean onMapClick(@NonNull com.mapbox.mapboxsdk.geometry.LatLng point) {
        if (balloonContainer != null) {
            balloonContainer.setVisibility(View.GONE);
        }
        if (FeatureConfig.USE_OPENDATA) {

            if (mapboxMap != null)
                if (options.isEnableAllGestures() || options.isEnableControlButtons()) {
                    mapboxMap.removeOnCameraMoveStartedListener(MapBoxFragment.this);
                    mapboxMap.removeOnCameraMoveListener(MapBoxFragment.this);
                    mapboxMap.removeOnCameraIdleListener(MapBoxFragment.this);
                    cancelCheckMarker();
                    checkMarkerTask = new CheckMarkerTask(getActivity(), point.getLatitude(), point.getLongitude(), (int) mapboxMap.getCameraPosition().zoom, 20, MapConfig.MAP_TYPE_MAPBOX); // TODO correct params (zoom, size)
                    checkMarkerTask.setEndTaskListener(checkMarkerEndTaskListener);
                    checkMarkerTask.execute();
                }
        }
        return false;
    }

    public boolean onInfoWindowClick() {
        if (openTestUUIDURL != null) {
            Timber.d("go to url: %s", openTestUUIDURL);
            final MainActivity activity = getMainActivity();
            if (activity != null) {
                activity.showHelp(openTestUUIDURL, false, AppConstants.PAGE_TITLE_MAP, R.string.page_title_help);
            }

        }
        return true;
    }

    @Override
    public void centerToMyPosition() {

        if (myLocation != null) {
            final LatLng latlng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

            if (mapboxMap != null) {
                com.mapbox.mapboxsdk.camera.CameraPosition position = new com.mapbox.mapboxsdk.camera.CameraPosition.Builder()
                        .target(new com.mapbox.mapboxsdk.geometry.LatLng(myLocation.getLatitude(), myLocation.getLongitude())) // Sets the new camera position
                        .build();
                mapboxMap.animateCamera(com.mapbox.mapboxsdk.camera.CameraUpdateFactory.newCameraPosition(position));
            }
        }
    }

    @Override
    public void openMapSettings() {

    }

    @Override
    public void openMapFilter() {
        final FragmentManager fm;
        FragmentActivity activity = getActivity();
        if (activity != null) {
            Intent intent = new Intent(activity, FilterListActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void openInfo() {
        showInfoToast();
    }

    @Override
    public boolean onMapLongClick(@NonNull com.mapbox.mapboxsdk.geometry.LatLng point) {
        onMapClick(point);
        return false;
    }

    @Override
    public void onCameraMoveStarted(int reason) {
        if (mapType == MapConfig.MAP_TYPE_MAPBOX) {
            if (balloonContainer != null) {
                balloonContainer.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onCameraIdle() {
        if (mapType == MapConfig.MAP_TYPE_MAPBOX) {
            cameraMoving = false;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!cameraMoving) {
                        boolean finalNeedHeatmapOverlay = needHeatmapOverlay;
                        boolean finalNeedPointsOverlay = needPointsOverlay;
                        boolean finalNeedShapesOverlay = needShapesOverlay;
                        cameraMoving = true;
                    }
                }
            }, 500);

            if (mapboxMap != null) {
                cameraPosition = mapboxMap.getCameraPosition();
                Timber.e("CAMERA MOVED");
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        onMyLocationChange(location);
//        if (locationEngineListener != null) {
//            locationEngineListener.onLocationChanged(location);
//        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @SuppressWarnings("unused")
    public class RMBTMapFragmentOptions {
        private boolean showInfoToast = true;
        private boolean enableAllGestures = true;
        private boolean enableControlButtons = true;
        private boolean enableOverlay = true;

        public boolean isShowInfoToast() {
            return showInfoToast;
        }

        void setShowInfoToast(boolean showInfoToast) {
            this.showInfoToast = showInfoToast;
        }

        boolean isEnableAllGestures() {
            return enableAllGestures;
        }

        void setEnableAllGestures(boolean enableAllGestures) {
            this.enableAllGestures = enableAllGestures;
        }

        boolean isEnableControlButtons() {
            return enableControlButtons;
        }

        void setEnableControlButtons(boolean enableControlButtons) {
            this.enableControlButtons = enableControlButtons;
        }

        boolean isEnableOverlay() {
            return enableOverlay;
        }

        void setEnableOverlay(boolean enableOverlay) {
            this.enableOverlay = enableOverlay;
        }
    }

    private LocationSource.OnLocationChangedListener onLocationChangedListener;


    @Override
    public void setRetainInstance(boolean retain) {
        super.setRetainInstance(false);
    }

//    @SuppressLint("InflateParams")
//    private void setLayout() {
//
//        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
//        View view = null;
//        int orientation = getResources().getConfiguration().orientation;
//        if (orientation == Configuration.ORIENTATION_LANDSCAPE)
//            view = layoutInflater.inflate(R.layout.mapbox_fragment_land, null);
//        else if (orientation == Configuration.ORIENTATION_PORTRAIT)
//            view = layoutInflater.inflate(R.layout.mapbox_fragment, null);
//    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        orientation = newConfig.orientation;
        if (balloonContainer != null) {
            balloonContainer.setVisibility(View.GONE);
        }
//        this.setLayout();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.findItem(R.id.action_menu_map_filter);
        item.setVisible(true);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                openMapFilter();
                return true;
            }
        });

        item = menu.findItem(R.id.action_menu_my_position);
        item.setVisible(true);
        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                centerToMyPosition();
                return true;
            }
        });

//        item = menu.findItem(R.id.action_menu_map_info);
//        item.setVisible(true);
//        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                showInfoToast();
//                return true;
//            }
//        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mainActivity = getMainActivity();
        Timber.e(" ON CREATE INIT");
        mainActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);

        final Bundle bundle = getArguments();

        mapType = MapConfig.getMapType(this.getMainActivity());
        Timber.e("Type: %s", mapType);

        if (bundle != null) {
            if (bundle.containsKey(OPTION_ENABLE_ALL_GESTURES)) {
                options.setEnableAllGestures(bundle.getBoolean(OPTION_ENABLE_ALL_GESTURES));
            }
            if (bundle.containsKey(OPTION_SHOW_INFO_TOAST)) {
                options.setShowInfoToast(bundle.getBoolean(OPTION_SHOW_INFO_TOAST));
            }
            if (bundle.containsKey(OPTION_ENABLE_CONTROL_BUTTONS)) {
                options.setEnableControlButtons(bundle.getBoolean(OPTION_ENABLE_CONTROL_BUTTONS));
            }
            if (bundle.containsKey(OPTION_ENABLE_OVERLAY)) {
                options.setEnableOverlay(bundle.getBoolean(OPTION_ENABLE_OVERLAY));
            }
        }

        /*FilterViewModel badgesModel = ViewModelProviders.of(this).get(FilterViewModel.class);
        LiveData<List<FilterGroup>> data = badgesModel.getData();
        data.observe(this, observer);
        filterGroups = data.getValue();
        if (filterGroups != null) {
            restartMap();
        }*/
    }

   /* @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add(R.string.map_settings);
//        menu.add(R.id.action_menu_map_filter);

    }*/

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mapType = MapConfig.getMapType(this.getMainActivity());
        Timber.e("Attach Type: " + mapType);
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                Timber.e("MENU FRAGMENT setActionBarItems RESUME");
                setActionBarItems(mainActivity);
            }
        });
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mapType = MapConfig.getMapType(this.getMainActivity());
        Timber.e("Attach Type: " + mapType);
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                Timber.e("MENU FRAGMENT setActionBarItems RESUME");
                setActionBarItems(mainActivity);
            }
        });
    }

    private MainActivity getMainActivity() {
        FragmentActivity activity = getActivity();
        if ((activity != null) && (activity instanceof MainActivity)) {
            return ((MainActivity) activity);
        }
        return null;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERM_REQ_LOC_FINE:
                if ((grantResults.length > 0) && (grantResults[0] == PermissionChecker.PERMISSION_GRANTED)) {
                    MainActivity activity = getMainActivity();
                    if (activity != null)
                        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            setUpLocationListener(activity, true);
                        }
                }
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        MainActivity activity = getMainActivity();
        if (activity != null) {
            if (!(ActivityCompat.checkSelfPermission(activity, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                setUpLocationListener(activity, false);
            }
        }

        Timber.e(" ON START");
        checkForGPSPermissions(PERM_REQ_LOC_FINE, true);
        if (mapType == MapConfig.MAP_TYPE_MAPBOX) {
            mapboxMapView.onStart();
        } else {
            getMapAsync(this);
        }
    }

    public void checkForGPSPermissions(int requestCodeFine,
                                       boolean showRationaleDialog) {

        FragmentActivity activity = this.getActivity();
        if (activity != null) {
            if (ContextCompat.checkSelfPermission(activity, ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (showRationaleDialog && shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
                        PermissionHandler.showLocationExplanationDialog((MainActivity) activity, requestCodeFine, this);
                    } else {
                        requestPermissions(new String[]{ACCESS_FINE_LOCATION}, requestCodeFine);
                    }
                }
            }
        }
    }

    public void onMyLocationChange(Location location) {
        myLocation = location;
        if (onLocationChangedListener != null)
            onLocationChangedListener.onLocationChanged(location);
    }

    public int getOrientation() {
        return orientation;
    }

    @Override
    public void onStop() {
        super.onStop();

//        mainActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

//        if ((locationEngine != null) && (locationEngineListener != null)) {
//            locationEngine.deactivate();
//            locationEngine.removeLocationEngineListener(locationEngineListener);
//            locationEngine.removeLocationUpdates();
//        }
        cancelCheckMarker();
//        if (Build.MANUFACTURER.contentEquals("Amazon")) {
//               // do not unregister method available (not found)
//        } else {
        GeoLocationX.getInstance(mainActivity.getApplication()).removeListener(this);
//        }

//        geoLocation.stop();
        if (heatmapOverlay != null) {
            heatmapOverlay.clearTileCache();
            heatmapOverlay.remove();
            heatmapOverlay = null;
        }
        if (pointsOverlay != null) {
            pointsOverlay.clearTileCache();
            pointsOverlay.remove();
            pointsOverlay = null;
        }
        if (shapesOverlay != null) {
            shapesOverlay.clearTileCache();
            shapesOverlay.remove();
            shapesOverlay = null;
        }
        mapboxMapView.onStop();
        Timber.e(" ON STOP");
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapboxMapView.onLowMemory();
        Timber.e(" ON LOW MEMORY");
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        markerViewManager.onDestroy();
        mapboxMapView.onDestroy();
        mapReadyCallbackList.clear();
        Timber.e(" ON DESTROY");
    }

    @Override
    public void onResume() {
        super.onResume();
        mainActivity = this.getMainActivity();
        mainActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);


        restartMap();


//        String selectedCountryInMapFilter = ConfigHelper.getSelectedCountryInMapFilter(getContext());
//
//        if (!(selectedCountryInMapFilter.isEmpty() || selectedCountryInMapFilter.equals("all"))) {
//            changeMapScrollToSelectedCountry(selectedCountryInMapFilter);
//        }

        MainActivity activity = getMainActivity();
        if (activity != null) {
            activity.updateTitle(setActionBarTitle());
            setActionBarItems(activity);
            if (!(ActivityCompat.checkSelfPermission(activity, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                setUpLocationListener(activity, false);
            }
        }
        Timber.e(" ON RESUME");

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                Timber.e("MENU FRAGMENT setActionBarItems RESUME");
                setActionBarItems(mainActivity);
            }
        });
    }

    private void restartMap() {
        if (options.showInfoToast) {
            showInfoToast();

        }
        if (mapType == MapConfig.MAP_TYPE_MAPBOX) {
            mapboxMapView.setVisibility(View.VISIBLE);
            mapViewContainer.setVisibility(View.GONE);
            mapboxMapView.onResume();
            mapboxMapView.getMapAsync(this);
            Timber.e(" ON RESUME GET ASYNC");
        } else {
            mapboxMapView.setVisibility(View.GONE);
            mapViewContainer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
//        if ((locationEngine != null) && (locationEngineListener != null)) {
//            locationEngine.deactivate();
//            locationEngine.removeLocationEngineListener(locationEngineListener);
//            locationEngine.removeLocationUpdates();
//        }
//        if (locationComponent != null) {
//            locationComponent.setLocationComponentEnabled(true);
//        }

        if ((mapboxMap != null) && (mapboxMap.getStyle() != null)) {
            mapboxMap.getStyle().removeLayer("heatmap");
            mapboxMap.getStyle().removeSource("heatmapsource-id");
            mapboxMap.removeOnCameraMoveStartedListener(MapBoxFragment.this);
            mapboxMap.removeOnCameraMoveListener(MapBoxFragment.this);
            mapboxMap.removeOnCameraIdleListener(MapBoxFragment.this);
        }
        if (mapboxMapView != null) {
            mapboxMapView.onPause();
        }
        if (balloonContainer != null) {
            balloonContainer.setVisibility(View.GONE);
        }
        Timber.e(" ON PAUSE");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapboxMapView.onSaveInstanceState(outState);
        Timber.e(" ON SAVE INSTANCE STATE");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        final ConstraintLayout view = (ConstraintLayout) inflater.inflate(R.layout.mapbox_fragment_land, container, false);
        registerListeners(view);

        FragmentActivity activity = getActivity();
        initComponent(inflater, container, savedInstanceState, view, activity);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle
            savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Timber.e(" ON CREATE");
        if (mapType == MapConfig.MAP_TYPE_MAPBOX) {
            mapboxMapView.onCreate(savedInstanceState);
            mapboxMapView.getMapAsync(this);
        }

    }

    private void initComponent(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState, ConstraintLayout view, FragmentActivity activity) {
        View mapView = super.onCreateView(inflater, container, savedInstanceState);

        mapViewContainer = view.findViewById(R.id.mapViewContainer);

        mapViewContainer.addView(mapView);
        mapViewContainer.setVisibility(View.GONE);

        mapboxMapView = view.findViewById(R.id.mapbox_mapview);
        mapboxMapView.setVisibility(View.GONE);

        balloonContainer = view.findViewById(R.id.balloon_container);
        if (balloonContainer != null) {
            balloonContainer.setVisibility(View.GONE);
        }

        ProgressBar progressBar = new ProgressBar(activity);
        final RelativeLayout.LayoutParams layoutParams =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, 1);
        progressBar.setLayoutParams(layoutParams);
        progressBar.setVisibility(View.GONE);
        view.addView(progressBar);
    }

    private void registerListeners(View view) {
        final Button mapChooseButton = view.findViewById(R.id.mapChooseButton);
        final Button mapFilterButton = view.findViewById(R.id.mapFilterButton);
        final Button mapLocateButton = view.findViewById(R.id.mapLocateButton);
        final Button mapHelpButton = view.findViewById(R.id.mapHelpButton);
        final Button mapInfoButton = view.findViewById(R.id.mapInfoButton);
        final Button mapZoomInButton = view.findViewById(R.id.mapZoomInButton);
        final Button mapZoomOutButton = view.findViewById(R.id.mapZoomOutButton);

        if (options.isEnableControlButtons()) {
            mapChooseButton.setOnClickListener(this);
            mapFilterButton.setOnClickListener(this);
            mapLocateButton.setOnClickListener(this);
            mapHelpButton.setOnClickListener(this);
            mapInfoButton.setOnClickListener(this);
            mapZoomInButton.setOnClickListener(this);
            mapZoomOutButton.setOnClickListener(this);
            mapHelpButton.setVisibility(View.GONE);
        } else {
            mapChooseButton.setVisibility(View.GONE);
            mapFilterButton.setVisibility(View.GONE);
            mapLocateButton.setVisibility(View.GONE);
            mapHelpButton.setVisibility(View.GONE);
            mapInfoButton.setVisibility(View.GONE);
            mapZoomInButton.setVisibility(View.GONE);
            mapZoomOutButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        FragmentActivity activity = getActivity();
        if (activity != null) {

            switch (v.getId()) {
                case R.id.mapChooseButton:

                    openMapSettings();


                    break;

                case R.id.mapFilterButton:

                    openMapFilter();


                    break;

                case R.id.mapLocateButton:
                    centerToMyPosition();

                    break;

                case R.id.mapInfoButton:
                    showInfoToast();
                    break;

                case R.id.mapZoomInButton:

                    if (mapboxMap != null) {
                        mapboxMap.animateCamera(com.mapbox.mapboxsdk.camera.CameraUpdateFactory.zoomIn());
                    }

                    break;

                case R.id.mapZoomOutButton:
                    if (mapboxMap != null) {
                        mapboxMap.animateCamera(com.mapbox.mapboxsdk.camera.CameraUpdateFactory.zoomOut());
                    }
                    break;

                default:
                    break;
            }
        }
    }

    private void showInfoToast() {

        MainActivity mainActivity = getMainActivity();
        if (mainActivity != null) {

            if (getResources().getBoolean(R.bool.show_licenses)) {
                StringBuilder infoString = new StringBuilder();
                String title = getResources().getString(R.string.map_license_line1);
                String by = getResources().getString(R.string.map_licence_by);
                String[] products = getResources().getStringArray(R.array.map_licence_products);
                String[] companies = getResources().getStringArray(R.array.map_licence_companies);
                String[] licenses = getResources().getStringArray(R.array.map_licence_licenses);
                String[] countries = getResources().getStringArray(R.array.map_licence_country);
                infoString.append(title);
                infoString.append("\n\n");


                for (int i = 0; i < products.length; i++) {
                    infoString.append("- ");
                    infoString.append(products[i]);
                    infoString.append(" ");
                    infoString.append(by);
                    infoString.append(" ");
                    infoString.append(companies[i]);
                    infoString.append(", ");
                    infoString.append(countries[i]);
                    infoString.append(", ");
                    infoString.append(licenses[i]);
                    if (i != (products.length - 1)) {
                        infoString.append("\n");
                    }
                }

                final Toast toast = Toast.makeText(getActivity(), infoString, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.BOTTOM, 0, 0);
                toast.show();
            } else {
                StringBuilder infoString = new StringBuilder();
                if (filterGroups != null) {
                    for (final FilterGroup s : filterGroups) {
                        String selectedValuesToShow = s.getSelectedValuesToShow();
                        if (infoString.length() > 0)
                            infoString.append("\n");

                        infoString.append(selectedValuesToShow);
                    }
                    if (infoString.length() > 0) {
                        final Toast toast = Toast.makeText(getActivity(), infoString.toString(), Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.BOTTOM, 0, 0);
                        toast.show();
                    }
                }
            }
        }

    }

    private CheckMarkerTask checkMarkerTask;
    private BalloonOverlayItem balloon;
    private String openTestUUIDURL;
    private final EndTaskListener checkMarkerEndTaskListener = new EndTaskListener() {
        @Override
        public void taskEnded(JsonArray result) {
            if (!isVisible())
                return;

            if ((mapType == MapConfig.MAP_TYPE_MAPBOX && mapboxMap == null || result == null)) {
                return;
            }

            try {
                if (result.size() == 0)
                    return;

                final JsonObject resultListItem = result.get(0).getAsJsonObject();

                final LatLng latLng = new LatLng(resultListItem.get("lat").getAsDouble(), resultListItem.get("lon").getAsDouble());

                openTestUUIDURL = null;
                final String openDataPrefix = ConfigHelper.getVolatileSetting("url_open_data_prefix");
                if (openDataPrefix != null && openDataPrefix.length() > 0) {
                    String openUUID = null;
                    if (resultListItem.has("open_test_uuid")) {
                        openUUID = resultListItem.get("open_test_uuid").getAsString();
                    }
                    if (openUUID != null && openUUID.length() > 0)
                        openTestUUIDURL = openDataPrefix + openUUID + "#noMMenu";
                }

                balloon = new BalloonOverlayItem(latLng, getResources().getString(R.string.map_balloon_overlay_header), result);

                com.mapbox.mapboxsdk.geometry.LatLng point = new com.mapbox.mapboxsdk.geometry.LatLng(resultListItem.get("lat").getAsDouble(), resultListItem.get("lon").getAsDouble());
                ;

                final BalloonOverlayView bv = new BalloonOverlayView(getActivity());
                LinearLayout linearLayout = new LinearLayout(getActivity());
                linearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                final View view = bv.setupView(getActivity(), linearLayout);
                bv.setBalloonData(balloon, null);
                balloonContainer.removeAllViews();
                balloonContainer.addView(view);
                balloonContainer.setVisibility(View.VISIBLE);
                balloonContainer.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onInfoWindowClick();
                    }
                });

                mapboxMap.removeOnCameraMoveStartedListener(MapBoxFragment.this);
                mapboxMap.removeOnCameraMoveListener(MapBoxFragment.this);
                mapboxMap.removeOnCameraIdleListener(MapBoxFragment.this);

                // to move selected point to the middle (horizontal) and to the bottom quarter (vertical), in view we have guideline set to 75% from the top to show balloon on the correct place
                int paddingTop = mapboxMapView.getHeight() / 2;

                mapboxMap.setCameraPosition(new com.mapbox.mapboxsdk.camera.CameraPosition.Builder().target(point).padding(0, paddingTop, 0, 0).build());

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mapboxMap != null) {
                            mapboxMap.addOnCameraMoveStartedListener(MapBoxFragment.this);
                            mapboxMap.addOnCameraMoveListener(MapBoxFragment.this);
                            mapboxMap.addOnCameraIdleListener(MapBoxFragment.this);
                        }
                    }
                }, 500);


            } catch (final JsonParseException e) {
                e.printStackTrace();
            }
        }
    };

    private void cancelCheckMarker() {
        if (checkMarkerTask != null)
            checkMarkerTask.cancel(true);
    }

}
