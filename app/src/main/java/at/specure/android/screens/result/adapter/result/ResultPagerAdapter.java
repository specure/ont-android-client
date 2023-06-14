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
package at.specure.android.screens.result.adapter.result;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.markerview.MarkerViewManager;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.RasterLayer;
import com.mapbox.mapboxsdk.style.sources.RasterSource;
import com.mapbox.mapboxsdk.style.sources.Source;
import com.specure.opennettest.R;

import androidx.viewpager.widget.PagerAdapter;
import at.specure.android.api.calls.CheckTestResultDetailTask;
import at.specure.android.api.calls.CheckTestResultTask;
import at.specure.android.api.jsons.VoipTestResult;
import at.specure.android.configs.FeatureConfig;
import at.specure.android.configs.MapConfig;
import at.specure.android.screens.main.main_activity_interfaces.ExpandedResultInterface;
import at.specure.android.screens.main.main_activity_interfaces.HelpInterface;
import at.specure.android.screens.main.main_activity_interfaces.MapInterface;
import at.specure.android.screens.result.fragments.main_result_pager.ResultPagerController;
import at.specure.android.screens.test_results.VoipTestResultHandler;
import at.specure.android.util.EndTaskListener;
import at.specure.android.util.Helperfunctions;
import at.specure.android.util.SemaphoreColorHelper;
import at.specure.android.views.ResultDetailsView;
import at.specure.android.views.ResultGraphView;
import at.specure.android.views.ResultQoSDetailView;
import at.specure.client.v2.task.result.QoSServerResultCollection;
import timber.log.Timber;

/**
 * @author lb
 */
public class ResultPagerAdapter extends PagerAdapter implements OnMapReadyCallback, com.mapbox.mapboxsdk.maps.OnMapReadyCallback {
    public final static int RESULT_PAGE_MAIN_MENU = 0;
    public final static int RESULT_PAGE_TEST = 1;
    public final static int RESULT_PAGE_QOS = 2;
    public final static int RESULT_PAGE_GRAPH = 3;
    public final static int RESULT_PAGE_MAP = 4;

//    public final static int RESULT_PAGE_TEST = 0;
//    public final static int RESULT_PAGE_QOS = 1;
//    public final static int RESULT_PAGE_GRAPH = 2;
//    public final static int RESULT_PAGE_MAP = 3;

    private static SparseIntArray RESULT_PAGE_TAB_TITLE_MAP;
    private static final String DEBUG_TAG = "ResultPagerAdapter";
    private final Bundle savedState;
    private RasterSource satelliteSource;
    private MapboxMap mapboxMap;
    private com.mapbox.mapboxsdk.maps.MapView miniMapBoxView;
    private Marker marker;
    private MarkerViewManager markerViewManager;

    /**
     * This is mapping of titles in titles array
     */
//    static {
//        RESULT_PAGE_TAB_TITLE_MAP = new SparseIntArray();
//        RESULT_PAGE_TAB_TITLE_MAP.put(RESULT_PAGE_QOS, 1);
//        RESULT_PAGE_TAB_TITLE_MAP.put(RESULT_PAGE_TEST, 2);
//        RESULT_PAGE_TAB_TITLE_MAP.put(RESULT_PAGE_GRAPH, 3);
//        RESULT_PAGE_TAB_TITLE_MAP.put(RESULT_PAGE_MAP, 4);
//    }
    public static SparseIntArray getTitleMapping(Context context) {
        if (FeatureConfig.showBasicResultInPager(context)) {
            RESULT_PAGE_TAB_TITLE_MAP = new SparseIntArray();
            RESULT_PAGE_TAB_TITLE_MAP.put(RESULT_PAGE_MAIN_MENU, 0);
            RESULT_PAGE_TAB_TITLE_MAP.put(RESULT_PAGE_QOS, 1);
            RESULT_PAGE_TAB_TITLE_MAP.put(RESULT_PAGE_TEST, 2);
            RESULT_PAGE_TAB_TITLE_MAP.put(RESULT_PAGE_GRAPH, 3);
            RESULT_PAGE_TAB_TITLE_MAP.put(RESULT_PAGE_MAP, 4);
        } else {
            RESULT_PAGE_TAB_TITLE_MAP = new SparseIntArray();
            RESULT_PAGE_TAB_TITLE_MAP.put(RESULT_PAGE_QOS - 1, 1);
            RESULT_PAGE_TAB_TITLE_MAP.put(RESULT_PAGE_TEST - 1, 2);
            RESULT_PAGE_TAB_TITLE_MAP.put(RESULT_PAGE_GRAPH - 1, 3);
            RESULT_PAGE_TAB_TITLE_MAP.put(RESULT_PAGE_MAP - 1, 4);
        }
        return RESULT_PAGE_TAB_TITLE_MAP;
    }

    private final Activity activity;

    private final String testUuid;
    private final MapInterface mapInterface;
    private final HelpInterface helpInterface;
    private final Handler handler;
    private String openTestUuid = null;
    private boolean hasMap = true;
    private Runnable progressUpdater;
    private JsonArray testResult;
    private JsonArray testResultDetails;
    private JsonArray testGraphResult;
    private QoSServerResultCollection testResultQoSDetails;

    private LatLng testPoint = null;
    private String mapType = null;

    private OnCompleteListener completeListener;
    private OnDataChangedListener dataChangedListener;

    //private ResultGraphView graphView = null;
    private LinearLayout measurementLayout;
    private ExpandedResultInterface expandedResultInterface;
    private ResultGraphView graphView;
    private EndTaskListener graphViewEndTaskListener = new EndTaskListener() {

        @Override
        public void taskEnded(JsonArray result) {
            ResultPagerAdapter.this.testGraphResult = result;
            System.out.println("REFRESHING GRAPHVIEW");
            graphView.refresh(result);
        }
    };

    public ResultPagerAdapter(final Activity _activity, final Handler _handler, final String testUuid, MapInterface mapInterface, HelpInterface helpInterface, ExpandedResultInterface expandedResultInterface, Bundle savedState) {
        this.activity = _activity;
        this.handler = _handler;
        this.testUuid = testUuid;
        this.mapInterface = mapInterface;
        this.helpInterface = helpInterface;
        this.expandedResultInterface = expandedResultInterface;
        this.savedState = savedState;
    }

    public void mapboxOnStop() {
        /*if (miniMapBoxView != null) {
            miniMapBoxView.onStop();
        }*/
    }

    /**
     * @param listener
     */
    public void setOnDataChangedListener(OnDataChangedListener listener) {
        this.dataChangedListener = listener;
    }

    @Override
    public Parcelable saveState() {
        Timber.d("RMBT SAVE STATE Saving state in ResultPagerAdapter");
        Bundle bundle = new Bundle();
        bundle.putParcelable("instanceState", super.saveState());
        if (testResult != null) {
            bundle.putString("test_result", testResult.toString());
        }
        if (testResultDetails != null) {
            bundle.putString("test_result_details", testResultDetails.toString());
        }
        if (testGraphResult != null) {
            bundle.putString("test_result_graph", testGraphResult.toString());
        }
        if (testResultQoSDetails != null) {
            bundle.putString("test_result_qos", testResultQoSDetails.getTestResultArray().toString());
        }
        return bundle;
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader cl) {
        Gson gson = new Gson();
        if (state instanceof Bundle) {
            Timber.d("RMBT RESTORE STATE Restoring state in ResultPagerAdapter");
            Bundle bundle = (Bundle) state;
            super.restoreState(bundle.getParcelable("instanceState"), cl);
            try {
                String testResultJson = bundle.getString("test_result");
                if (testResultJson != null) {
                    testResult = gson.fromJson(testResultJson, JsonElement.class).getAsJsonArray();
                }

                String testDetailsJson = bundle.getString("test_result_details");
                if (testDetailsJson != null) {
                    testResultDetails = gson.fromJson(testDetailsJson, JsonElement.class).getAsJsonArray();
                }

                String testGraphJson = bundle.getString("test_result_graph");
                if (testGraphJson != null) {
                    testGraphResult = gson.fromJson(testGraphJson, JsonElement.class).getAsJsonArray();
                    //graphViewEndTaskListener.taskEnded(testGraphResult);
                }

                String testDetailsQos = bundle.getString("test_result_qos");
                if (testDetailsQos != null) {
                    setTestResultQoSDetails(new QoSServerResultCollection(gson.fromJson(testDetailsQos, JsonElement.class).getAsJsonArray()));
                }
            } catch (JsonParseException e) {
                // TODO Auto-generated catch block
                //e.printStackTrace();
            }
            return;
        }
    }

    private void addDividerToMeasurement(int leftRightDiv, int topBottomDiv, int heightDiv) {
        final View divider = new View(activity);
        divider.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, heightDiv, 1));
        divider.setPadding(leftRightDiv, topBottomDiv, leftRightDiv, topBottomDiv);

        divider.setBackgroundResource(R.drawable.bg_trans_light);

        measurementLayout.addView(divider);
    }

    /**
     *
     */
    public synchronized void addQoSResultItem() {
        if (testResultQoSDetails != null && measurementLayout != null) {
            QoSServerResultCollection.QoSResultStats stats = testResultQoSDetails.getQoSStatistics();
            addResultListItem(activity.getString(R.string.result_qos_stats), stats.getPercentageForTests() + "% (" + (stats.getTestCounter() - stats.getFailedTestsCounter()) + "/" + stats.getTestCounter() + ")", measurementLayout);
        }
    }

    public void addResultListItem(String title, String value, LinearLayout netLayout) {
        final float scale = activity.getResources().getDisplayMetrics().density;
        final int leftRightDiv = Helperfunctions.dpToPx(0, scale);
        final int topBottomDiv = Helperfunctions.dpToPx(0, scale);
        final int heightDiv = Helperfunctions.dpToPx(1, scale);

        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View measurementItemView = inflater.inflate(R.layout.classification_list_item, netLayout, false);

        final TextView itemTitle = measurementItemView.findViewById(R.id.classification_item_title);
        itemTitle.setText(title);

        final ImageView itemClassification = measurementItemView.findViewById(R.id.classification_item_color);
        itemClassification.setImageResource(SemaphoreColorHelper.resolveSemaphoreColor(-1));

        itemClassification.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (helpInterface != null) {
                    helpInterface.showHelp(R.string.url_help_result, false);
                }
            }
        });

        final TextView itemValue = measurementItemView.findViewById(R.id.classification_item_value);
        itemValue.setText(value);

        netLayout.addView(measurementItemView);

        final View divider = new View(activity);
        divider.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, heightDiv, 1));
        divider.setPadding(leftRightDiv, topBottomDiv, leftRightDiv, topBottomDiv);

        divider.setBackgroundResource(R.drawable.bg_trans_light_10);

        netLayout.addView(divider);

        netLayout.invalidate();
    }

    /**
     * @param vg
     * @param inflater
     * @return
     */
    public View instantiateDetailView(final ViewGroup vg, final LayoutInflater inflater) {
        Timber.e("MAPBOX DETAILS");
        final ResultDetailsView view = new ResultDetailsView(activity.getApplicationContext(), activity, testUuid, testResultDetails);
        view.initialize(new EndTaskListener() {
            @Override
            public void taskEnded(JsonArray result) {
                ResultPagerAdapter.this.testResultDetails = result;
            }
        });
        return view;
    }

    /**
     * @param vg
     * @param inflater
     * @return
     */
    public View instantiateQoSDetailView(final ViewGroup vg, final LayoutInflater inflater) {
        Timber.e("MAPBOX QOS DETAILS");
        final ResultQoSDetailView view = new ResultQoSDetailView(activity.getApplicationContext(), activity, testUuid,
                (testResultQoSDetails != null ? testResultQoSDetails.getTestResultArray() : null), this.expandedResultInterface);

        //if (!isCheckingQoSResult.getAndSet(true)) {
        view.initialize(new EndTaskListener() {
            @Override
            public void taskEnded(JsonArray result) {
                //isCheckingQoSResult.set(false);
                try {
                    ResultPagerAdapter.this.setTestResultQoSDetails(new QoSServerResultCollection(result));
                } catch (JsonParseException e) {
                    //e.printStackTrace();
                }
            }
        });
        //}
        return view;
    }

    /**
     * @param uid
     */
    public void initializeQoSResults(String uid) {
        Timber.e("MAPBOX QOS");
        CheckTestResultDetailTask testResultDetailTask = new CheckTestResultDetailTask(activity, ResultDetailType.QUALITY_OF_SERVICE_TEST);

        testResultDetailTask.setEndTaskListener(new EndTaskListener() {
            @Override
            public void taskEnded(JsonArray result) {
                //isCheckingQoSResult.set(false);
                try {
                    ResultPagerAdapter.this.setTestResultQoSDetails(new QoSServerResultCollection(result));
                } catch (JsonParseException e) {
                    //e.printStackTrace();
                }
            }
        });

        testResultDetailTask.execute(uid);
    }

    /**
     * @param vg
     * @param inflater
     * @return
     */
    public View instantiateMapView(final ViewGroup vg, final LayoutInflater inflater) {
        try {
            Timber.e("MAPBOX INIT START");


        } catch (Exception e) {
            //do nothing
            Timber.e("MAPBOX EXCEPTION INIT");
        }

        //final ResultMapView view = new ResultMapView(activity.getApplicationContext(), activity, testUuid, testResult, inflater);
        //final View view = inflater.inflate(R.layout.result_map, this);

        Timber.e("MAPBOX MAP");
        final View view = inflater.inflate(R.layout.result_map, vg, false);
        //final View view = inflater.inflate(R.layout.result_map, null);

        TextView locationView = view.findViewById(R.id.result_map_location);
        TextView locationProviderView = view.findViewById(R.id.result_map_location_provider);
        TextView motionView = view.findViewById(R.id.result_map_motion);

        TextView notAvailableView = view.findViewById(R.id.result_mini_map_not_available);
        MapView miniMapView = view.findViewById(R.id.result_mini_map_view);
        miniMapBoxView = view.findViewById(R.id.result_mini_mapbox_map_view);
        Button overlayButton = view.findViewById(R.id.result_mini_map_view_button);

        try {
            System.out.println(testResult.toString());
            JsonObject testResultItem = testResult.get(0).getAsJsonObject();

            if (testResultItem.has("geo_lat") && testResultItem.has("geo_long")) {
                int mapType = MapConfig.getMapType(activity);

                if (MapConfig.MAP_TYPE_MAPBOX == mapType) {
                    miniMapBoxView.setVisibility(View.VISIBLE);
                } else {
                    miniMapView.setVisibility(View.VISIBLE);
                }
                notAvailableView.setVisibility(View.GONE);
                overlayButton.setVisibility(View.VISIBLE);

                if (dataChangedListener != null) {
                    dataChangedListener.onChange(false, true, "HAS_MAP");
                }


                final double geoLat = testResultItem.get("geo_lat").getAsDouble();
                final double geoLong = testResultItem.get("geo_long").getAsDouble();
                final int networkType = testResultItem.get("network_type").getAsInt();
                this.mapType = Helperfunctions.getMapType(networkType) + "/download";

                if (testResultItem.has("motion")) {
                    motionView.setText(testResultItem.get("motion").getAsString());
                    motionView.setVisibility(View.VISIBLE);
                }
                if (testResultItem.has("location")) {
                    String loc = testResultItem.get("location").getAsString();
                    int i = -1;
                    if (loc != null) {
                        if ((i = loc.indexOf("(")) >= 0) {
                            locationView.setText(loc.substring(0, i - 1).trim());
                            locationProviderView.setText(loc.substring(i).trim());
                            locationProviderView.setVisibility(View.VISIBLE);
                        } else {
                            locationView.setText(loc);
                        }
                        locationView.setVisibility(View.VISIBLE);
                    }
                }


                testPoint = new LatLng(geoLat, geoLong);

                if (MapConfig.MAP_TYPE_MAPBOX == mapType) {
                    if (miniMapBoxView != null) {
                        Timber.e("MAPBOX map create");
                        miniMapBoxView.onCreate(savedState);
                        Timber.e("MAPBOX map start");
                        miniMapBoxView.onStart();
                        Timber.e("MAPBOX map resume");
                        miniMapBoxView.onResume();
                        Timber.e("MAPBOX map async");
                        miniMapBoxView.getMapAsync(this);
                        Timber.e("MAPBOX map done");
                    }
                    if (overlayButton != null) {
                        overlayButton.setOnClickListener(new OnClickListener() {


                            @Override
                            public void onClick(View v) {
                                if (miniMapBoxView != null) {
                                    miniMapBoxView.onPause();
                                    miniMapBoxView.onStop();
                                    miniMapBoxView.onDestroy();
                                    miniMapBoxView = null;
                                }
                                if (mapInterface != null) {
                                    mapInterface.showMap(ResultPagerAdapter.this.mapType, testPoint, true, false);
                                }
                            }
                        });

                        overlayButton.bringToFront();
                    }
                } else {
                    if (miniMapView != null) {

                        try {
                            MapsInitializer.initialize(activity);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        miniMapView.onCreate(null);
                        miniMapView.onResume();
                        miniMapView.getMapAsync(this);

                    }
                    if (overlayButton != null) {
                        overlayButton.setOnClickListener(new OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                if (mapInterface != null) {
                                    mapInterface.showMap(ResultPagerAdapter.this.mapType, testPoint, true, false);
                                }
                            }
                        });

                        overlayButton.bringToFront();
                    }
                }



                Timber.d("ResultMapView TESTRESULT OK. Drawing MapView");
            } else {
                notAvailableView.setVisibility(View.VISIBLE);
                miniMapView.setVisibility(View.GONE);
                overlayButton.setVisibility(View.GONE);

                if (dataChangedListener != null) {
                    dataChangedListener.onChange(true, false, "HAS_MAP");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return view;
    }

    /**
     * @param vg
     * @param inflater
     * @return
     */
    public View instantiateGraphView(final ViewGroup vg, final LayoutInflater inflater) {
        System.out.println("instantiateGraphView");
        Timber.e("MAPBOX GRAPHS");
        if (graphView != null) {
            graphView.recycle();
            graphView = null;
        }
        graphView = new ResultGraphView(activity.getApplicationContext(), activity, testUuid, openTestUuid, testGraphResult, vg);

        if (openTestUuid != null) {
            graphView.initialize(graphViewEndTaskListener);
        }

        return graphView.getView();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull final ViewGroup vg, int i) {
        final Context context = vg.getContext();
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        System.out.println("instantiateItem: " + i);

        View view = null;

        if (!FeatureConfig.showBasicResultInPager(context)) {
            i++;
        }


        switch (i) {
            case RESULT_PAGE_QOS:
            case RESULT_PAGE_TEST:
            case RESULT_PAGE_MAIN_MENU:
                if (miniMapBoxView != null) {
                    miniMapBoxView.onPause();
                    miniMapBoxView.onStop();
                    miniMapBoxView.onDestroy();
                    miniMapBoxView = null;
                }
        }

        switch (i) {
            case RESULT_PAGE_QOS:
                view = instantiateQoSDetailView(vg, inflater);
                break;
            case RESULT_PAGE_TEST:
                instantiateResultPage(vg, inflater, false);
                view = instantiateDetailView(vg, inflater);
                break;
            case RESULT_PAGE_MAIN_MENU:
                view = instantiateResultPage(vg, inflater, true);
                break;
            case RESULT_PAGE_MAP:
                view = instantiateMapView(vg, inflater);
                break;
            case RESULT_PAGE_GRAPH:
                view = instantiateGraphView(vg, inflater);
                break;
        }


        if (view != null)
            vg.addView(view);
        return view;
    }

    /**
     *
     */
    @Override
    public void destroyItem(@NonNull final ViewGroup vg, final int i, @NonNull final Object obj) {
        final View view = (View) obj;
        vg.removeView(view);
    }

    @Override
    public boolean isViewFromObject(@NonNull final View view, @NonNull final Object object) {
        return view == object;
    }

    @Override
    public int getCount() {
        int i = ResultPagerController.MAP_INDICATOR_DYNAMIC_VISIBILITY ? (hasMap ? 5 : 4) : 5;
        if (FeatureConfig.showBasicResultInPager(activity)) {
            return i;
        } else {
            return i - 1; // because we removed main main result screen
        }

    }

    @Override
    public CharSequence getPageTitle(final int position) {
        if (FeatureConfig.showBasicResultInPager(activity)) {
            return activity.getResources().getStringArray(R.array.result_page_title)[position];
        } else {
            return activity.getResources().getStringArray(R.array.result_page_title)[position + 1]; //because we removed first screen in results
        }

    }

    public void destroy() {
        if (handler != null) {
            if (progressUpdater != null) {
                handler.removeCallbacks(progressUpdater);
            }
        }
        if (miniMapBoxView != null) {
            miniMapBoxView.onDestroy();
            Timber.e("MAPBOX map onDestroy");
        }
    }

    public void onPause() {
        if (handler != null) {
            if (progressUpdater != null) {
                handler.removeCallbacks(progressUpdater);
            }
        }

        if (marker != null) {
            marker.remove();
        }

        if (miniMapBoxView != null) {
            miniMapBoxView.onPause();
            miniMapBoxView.onStop();
            miniMapBoxView.onDestroy();
            miniMapBoxView = null;
            Timber.e("MAPBOX map onPause");
        }

    }


    /**
     * @param listener
     */
    public void setOnCompleteListener(OnCompleteListener listener) {
        this.completeListener = listener;
    }

    /**
     * @param qosResults
     */
    public synchronized void setTestResultQoSDetails(QoSServerResultCollection qosResults) {
        boolean firstSet = false;

        if (testResultQoSDetails == null) {
            firstSet = true;
        }

        testResultQoSDetails = qosResults;

        if (firstSet) {
            addQoSResultItem();
        }
    }

    /**
     *
     */
    public void showMap() {
        if (mapInterface != null) {
            if (mapType != null && testPoint != null) {
                mapInterface.showMap(mapType, testPoint, true, false);
            } else {
                mapInterface.showMap(false);
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(testPoint, 16));
        googleMap.addMarker(new MarkerOptions().position(testPoint));

        final UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(false); // options.isEnableAllGestures());
        uiSettings.setMyLocationButtonEnabled(false);
        uiSettings.setCompassEnabled(false);
        uiSettings.setRotateGesturesEnabled(false);
        uiSettings.setScrollGesturesEnabled(false);
        uiSettings.setZoomGesturesEnabled(false);
        uiSettings.setAllGesturesEnabled(false);

        googleMap.setTrafficEnabled(false);
        googleMap.setIndoorEnabled(false);

        googleMap.addMarker(new MarkerOptions().position(testPoint).draggable(false).
                icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        if (mapInterface != null) {
            googleMap.setMapType(mapInterface.getMapTypeSatellite() ? GoogleMap.MAP_TYPE_SATELLITE : GoogleMap.MAP_TYPE_NORMAL);
        }

        googleMap.setOnMapClickListener(new OnMapClickListener() {
            @Override
            public void onMapClick(LatLng arg0) {
                final Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        if (mapInterface != null) {
                            mapInterface.showMap(mapType, testPoint, true, false);
                        }
                    }
                };

                runnable.run();
            }
        });
    }

    public void startShareResultsIntent() {
        try {
            JsonObject resultListItem = testResult.get(0).getAsJsonObject();
            final String shareText = resultListItem.get("share_text").getAsString();
            final String shareSubject = resultListItem.get("share_subject").getAsString();

            final Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, shareSubject);
            sendIntent.setType("text/plain");
            activity.startActivity(Intent.createChooser(sendIntent, null));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param vg
     * @param inflater
     * @return
     */
    public View instantiateResultPage(final ViewGroup vg, final LayoutInflater inflater, final boolean displayResults) {
        final View view = inflater.inflate(R.layout.result_menu, vg, false);
        Timber.e("MAPBOX RESULTS");

        if (this.testResult == null) {
            final CheckTestResultTask testResultTask = new CheckTestResultTask(activity);
            testResultTask.setEndTaskListener(new EndTaskListener() {
                @Override
                public void taskEnded(final JsonArray testResult) {
                    if (testResult != null) {
                        System.out.println("testResultTask.hasError() = " + testResultTask.hasError() + ", testResult.length() = " + testResult.size());

                        ResultPagerAdapter.this.testResult = testResult;

                        if (testResult != null && testResult.size() > 0) {

                            JsonObject resultListItem;

                            try {
                                resultListItem = testResult.get(0).getAsJsonObject();


                                if (resultListItem.has("open_test_uuid")) {
                                    openTestUuid = resultListItem.get("open_test_uuid").getAsString();
                                }
                                if (graphView != null) {
                                    graphView.setOpenTestUuid(openTestUuid);
                                    graphView.initialize(graphViewEndTaskListener);
                                }
                            } catch (JsonParseException e) {
                                hasMap = false;
                                e.printStackTrace();
                            }
                        }

                        if (displayResults) {
                            displayResult(view, inflater, vg);
                        }

                        JsonObject testResultItem;
                        try {
                            testResultItem = testResult.get(0).getAsJsonObject();
                            if (testResultItem.has("geo_lat") && testResultItem.has("geo_long") && !hasMap) {
                                hasMap = true;
                                if (dataChangedListener != null) {
                                    dataChangedListener.onChange(false, true, "HAS_MAP");
                                }
                                notifyDataSetChanged();
                            } else if (!testResultItem.has("geo_lat") && !testResultItem.has("geo_long") && hasMap) {
                                hasMap = false;
                                if (dataChangedListener != null) {
                                    dataChangedListener.onChange(true, false, "HAS_MAP");
                                }
                                notifyDataSetChanged();
                            }
                        } catch (JsonParseException e) {
                            hasMap = false;
                            e.printStackTrace();
                        }

                        if (completeListener != null) {
                            completeListener.onComplete(OnCompleteListener.DATA_LOADED, this);
                        }
                    }
                }
            });

            testResultTask.execute(testUuid);
        } else {
            displayResult(view, inflater, vg);
        }


        if (this.testResultQoSDetails == null) {
            if (RESULT_PAGE_QOS > 1) {
                initializeQoSResults(testUuid);
            }
        }

        return view;
    }

    /**
     * @param view
     */
    private void displayResult(View view, LayoutInflater inflater, ViewGroup vg) {
    	/*
        final Button shareButton = (Button) view.findViewById(R.id.resultButtonShare);
        if (shareButton != null)
            shareButton.setEnabled(false);
            */

        //final LinearLayout measurementLayout = (LinearLayout) view.findViewById(R.id.resultMeasurementList);
        measurementLayout = view.findViewById(R.id.resultMeasurementList);
        measurementLayout.setVisibility(View.GONE);

        final LinearLayout resultLayout = view.findViewById(R.id.result_layout);
        resultLayout.setVisibility(View.INVISIBLE);

        final LinearLayout netLayout = view.findViewById(R.id.resultNetList);
        netLayout.setVisibility(View.GONE);

        final TextView measurementHeader = view.findViewById(R.id.resultMeasurement);
        measurementHeader.setVisibility(View.GONE);

        final TextView netHeader = view.findViewById(R.id.resultNet);
        netHeader.setVisibility(View.GONE);

        final TextView emptyView = view.findViewById(R.id.infoText);
        emptyView.setVisibility(View.GONE);
        final float scale = activity.getResources().getDisplayMetrics().density;

        final ProgressBar progessBar = view.findViewById(R.id.progressBar);

        if (testResult != null && testResult.size() > 0) {

            JsonObject resultListItem;

            try {
                resultListItem = testResult.get(0).getAsJsonObject();


                if (resultListItem.has("open_test_uuid")) {
                    openTestUuid = resultListItem.get("open_test_uuid").getAsString();
                }
                if (graphView != null) {
                    graphView.setOpenTestUuid(openTestUuid);
                    graphView.initialize(graphViewEndTaskListener);
                }

                JsonObject testResultItem;
                try {
                    testResultItem = testResult.get(0).getAsJsonObject();
                    if (testResultItem.has("geo_lat") && testResultItem.has("geo_long") && !hasMap) {
                        hasMap = true;
                        if (dataChangedListener != null) {
                            dataChangedListener.onChange(false, true, "HAS_MAP");
                        }
                        notifyDataSetChanged();
                    } else if (!testResultItem.has("geo_lat") && !testResultItem.has("geo_long") && hasMap) {
                        System.out.println("hasMap = " + hasMap);
                        hasMap = false;
                        if (dataChangedListener != null) {
                            dataChangedListener.onChange(true, false, "HAS_MAP");
                        }
                        notifyDataSetChanged();
                    }
                } catch (JsonParseException e) {
                    hasMap = false;
                    e.printStackTrace();
                }

                if (completeListener != null) {
                    completeListener.onComplete(OnCompleteListener.DATA_LOADED, this);
                }

                final JsonArray measurementArray = resultListItem.getAsJsonArray("measurement");

                final JsonArray netArray = resultListItem.getAsJsonArray("net");

                final int leftRightDiv = Helperfunctions.dpToPx(0, scale);
                final int topBottomDiv = Helperfunctions.dpToPx(0, scale);
                final int heightDiv = Helperfunctions.dpToPx(1, scale);


                for (int i = 0; i < measurementArray.size(); i++) {

                    final View measurementItemView = inflater.inflate(R.layout.classification_list_item, vg, false);

                    final JsonObject singleItem = measurementArray.get(i).getAsJsonObject();

                    final TextView itemTitle = measurementItemView.findViewById(R.id.classification_item_title);
                    itemTitle.setText(singleItem.get("title").getAsString());

                    final ImageView itemClassification = measurementItemView.findViewById(R.id.classification_item_color);
                    itemClassification.setImageResource(SemaphoreColorHelper.resolveSemaphoreColor(singleItem.get("classification").getAsInt()));

                    itemClassification.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (helpInterface != null) {
                                helpInterface.showHelp(R.string.url_help_result, false);
                            }
                        }
                    });

                    final TextView itemValue = measurementItemView.findViewById(R.id.classification_item_value);
                    itemValue.setText(singleItem.get("value").getAsString());

                    measurementLayout.addView(measurementItemView);

                    addDividerToMeasurement(leftRightDiv, topBottomDiv, heightDiv);

                    measurementLayout.invalidate();
                }

                // JITTER AND PACKET LOSS

                String meanPacketLoss = " - ";
                String meanJitter = " - ";
                int jitterClasification = -1;
                int packetLossClasification = -1;

                VoipTestResultHandler voipTestResultHandler = new VoipTestResultHandler();
                String meanPacketLossTitle = voipTestResultHandler.getMeanPacketLossString(this.activity);
                String meanJitterTitle = voipTestResultHandler.getMeanJitterTitleString(this.activity);


                if (resultListItem.has(VoipTestResult.JSON_OBJECT_IDENTIFIER)) {
                    JsonObject jsonObject = resultListItem.getAsJsonObject(VoipTestResult.JSON_OBJECT_IDENTIFIER);
                    Gson gson = new Gson();
                    try {
                        VoipTestResult voipTestResult = gson.fromJson(jsonObject, VoipTestResult.class);
                        meanJitter = voipTestResult.getVoipResultJitter() + " " + activity.getResources().getString(R.string.test_ms);
                        meanPacketLoss = voipTestResult.getVoipResultPacketLoss() + " %";
                        jitterClasification = voipTestResult.getClassificationJitter();
                        packetLossClasification = voipTestResult.getClassificationPacketLoss();

                    } catch (JsonParseException e) {
                        Timber.e(e,"MAIN_VOIP_RESULT_ERR");
                    }

                }

                final View measurementItemViewPL = inflater.inflate(R.layout.classification_list_item, vg, false);

                final TextView itemTitlePL = measurementItemViewPL.findViewById(R.id.classification_item_title);
                itemTitlePL.setText(meanPacketLossTitle);

                final ImageView itemClassificationPL = measurementItemViewPL.findViewById(R.id.classification_item_color);
                itemClassificationPL.setImageResource(SemaphoreColorHelper.resolveSemaphoreColor(packetLossClasification));

                final TextView itemValuePL = measurementItemViewPL.findViewById(R.id.classification_item_value);
                itemValuePL.setText(meanPacketLoss);

                measurementLayout.addView(measurementItemViewPL);

                addDividerToMeasurement(leftRightDiv, topBottomDiv, heightDiv);


                final View measurementItemViewJ = inflater.inflate(R.layout.classification_list_item, vg, false);
                final TextView itemTitleJ = measurementItemViewJ.findViewById(R.id.classification_item_title);
                itemTitleJ.setText(meanJitterTitle);

                final ImageView itemClassificationJ = measurementItemViewJ.findViewById(R.id.classification_item_color);
                itemClassificationJ.setImageResource(SemaphoreColorHelper.resolveSemaphoreColor(jitterClasification));

                final TextView itemValueJ = measurementItemViewJ.findViewById(R.id.classification_item_value);
                itemValueJ.setText(meanJitter);

                measurementLayout.addView(measurementItemViewJ);

                addDividerToMeasurement(leftRightDiv, topBottomDiv, heightDiv);

                // END OF JITTER AND PACKET LOSS

                for (int i = 0; i < netArray.size(); i++) {

                    final JsonObject singleItem = netArray.get(i).getAsJsonObject();

                    String value = null;
                    if (singleItem.has("value")) {
                        value = singleItem.get("value").getAsString();
                    }

                    addResultListItem(singleItem.get("title").getAsString(), value, netLayout);
                }

                addQoSResultItem();

            } catch (final JsonParseException e) {
                e.printStackTrace();
            }

            progessBar.setVisibility(View.GONE);
            emptyView.setVisibility(View.GONE);

            resultLayout.setVisibility(View.VISIBLE);
            measurementHeader.setVisibility(View.VISIBLE);
            netHeader.setVisibility(View.VISIBLE);

            measurementLayout.setVisibility(View.VISIBLE);
            netLayout.setVisibility(View.VISIBLE);

        } else {
            Timber.i( "LEERE LISTE");
            progessBar.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText(activity.getString(R.string.error_no_data));
            emptyView.invalidate();
        }
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {

        this.mapboxMap = mapboxMap;
        markerViewManager = new MarkerViewManager(miniMapBoxView, mapboxMap);
        this.mapboxMap.setStyle(new Style.Builder().fromUri(activity.getString(R.string.mapbox_style_url)));
        Timber.e("MAPBOX map done done");
        final com.mapbox.mapboxsdk.geometry.LatLng latLng = new com.mapbox.mapboxsdk.geometry.LatLng(testPoint.latitude, testPoint.longitude);

        mapboxMap.moveCamera(com.mapbox.mapboxsdk.camera.CameraUpdateFactory.newLatLngZoom(latLng, 16));

        com.mapbox.mapboxsdk.maps.UiSettings uiSettings = mapboxMap.getUiSettings();
        uiSettings.setCompassEnabled(false);
        uiSettings.setRotateGesturesEnabled(false);
        uiSettings.setScrollGesturesEnabled(false);
        uiSettings.setTiltGesturesEnabled(false);
        uiSettings.setZoomGesturesEnabled(false);
        uiSettings.setAllGesturesEnabled(false);


        if ((mapInterface != null) && (mapboxMap.getStyle() != null)) {
            if (mapInterface.getMapTypeSatellite()) {
                Layer layer = mapboxMap.getStyle().getLayer("satellite_layer");
                Source source = mapboxMap.getStyle().getSource("satellite_source");
                if (layer == null) {
                    if (source == null) {
                        satelliteSource = new RasterSource("satellite_source", "mapbox://mapbox.satellite");
                        mapboxMap.getStyle().addSource(satelliteSource);
                    }
                    RasterLayer rasterLayer = new RasterLayer("satellite_layer", "satellite_source");
                    mapboxMap.getStyle().addLayerBelow(rasterLayer, "bridge");

                    Timber.e("MAPBOX satellite map satellite layer added");

                } else {
                    Layer layer2 = mapboxMap.getStyle().getLayer("satellite_layer");
                    Source source2 = mapboxMap.getStyle().getSource("satellite_source");
                    if (layer2 == null) {
                        mapboxMap.getStyle().removeLayer(layer2);
                    }
                    if (source2 != null) {
                        mapboxMap.getStyle().removeSource(source2);
                    }
                }
            }
        }

        marker = mapboxMap.addMarker(new com.mapbox.mapboxsdk.annotations.MarkerOptions().position(latLng));


        mapboxMap.addOnMapClickListener(new MapboxMap.OnMapClickListener() {
            @Override
            public boolean onMapClick(@NonNull com.mapbox.mapboxsdk.geometry.LatLng point) {
                LatLng latLng1 = new LatLng(latLng.getLatitude(), latLng.getLongitude());
                if (miniMapBoxView != null) {
                    miniMapBoxView.onPause();
                    miniMapBoxView.onStop();
                    miniMapBoxView.onDestroy();
                    miniMapBoxView = null;
                }
                if (mapInterface != null) {
                    mapInterface.showMap(mapType, latLng1, true, false);
                }
                return false;
            }
        });
    }

    public void mapboxOnSaveInstanceState(Bundle outState) {
        if (miniMapBoxView != null) {
            miniMapBoxView.onSaveInstanceState(outState);
            Timber.e("MAPBOX map save state");
        }
    }


    public void mapboxOnLowMemory() {
        if (miniMapBoxView != null) {
            miniMapBoxView.onLowMemory();
            Timber.e("MAPBOX map low memory");
        }
    }
}