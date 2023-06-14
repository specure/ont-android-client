/**
 * Copyright 2014-2017 Specure GmbH
 * Copyright 2013-2015 alladin-IT GmbH
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package at.specure.android.screens.main.main_fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.specure.opennettest.R;

import java.text.DecimalFormat;
import java.text.Format;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.specure.android.api.calls.GetGeolocationTask;
import at.specure.android.api.jsons.MeasurementServer;
import at.specure.android.base.BaseFragment;
import at.specure.android.configs.BadgesConfig;
import at.specure.android.configs.ConfigHelper;
import at.specure.android.configs.FeatureConfig;
import at.specure.android.configs.LoopModeConfig;
import at.specure.android.configs.TestConfig;
import at.specure.android.impl.CpuStatAndroidImpl.CpuMemClassificationEnum;
import at.specure.android.screens.main.InfoCollector;
import at.specure.android.screens.main.LoopModeActivityCheckListener;
import at.specure.android.screens.main.MainActivity;
import at.specure.android.screens.main.MeasurementServersAdapter;
import at.specure.android.screens.main.OnMeasurementServersLoaded;
import at.specure.android.screens.main.main_fragment.adapters.InfoArrayAdapter;
import at.specure.android.screens.main.main_fragment.enums.InfoOverlayEnum;
import at.specure.android.screens.main.main_fragment.graphs_handlers.GraphHandler;
import at.specure.android.screens.main.main_fragment.runnables.MainInfoRunnable;
import at.specure.android.screens.main.main_fragment.view_handlers.DefaultViewsHandler;
import at.specure.android.screens.main.main_fragment.view_handlers.LoopModeViewsHandler;
import at.specure.android.screens.main.main_fragment.view_handlers.TestQosResultsViewsHandler;
import at.specure.android.screens.main.main_fragment.view_handlers.TestQosViewsHandler;
import at.specure.android.screens.main.main_fragment.view_handlers.TestResultsViewsHandler;
import at.specure.android.screens.main.main_fragment.view_handlers.TestViewsHandler;
import at.specure.android.screens.main.main_fragment.view_handlers.ViewsHandler;
import at.specure.android.test.SpeedTestStatViewController;
import at.specure.android.test.TestService;
import at.specure.android.test.views.graph.SmoothGraph;
import at.specure.android.util.AppRater;
import at.specure.android.util.Helperfunctions;
import at.specure.android.util.InformationCollector;
import at.specure.android.util.SemaphoreColorHelper;
import at.specure.android.util.location.GeoLocationX;
import at.specure.android.util.location.LocationChangeListener;
import at.specure.android.util.net.InterfaceTrafficGatherer;
import at.specure.android.util.net.InterfaceTrafficGatherer.TrafficClassificationEnum;
import at.specure.android.util.net.NetworkInfoCollector;
import at.specure.android.util.net.NetworkInfoCollector.CaptivePortalStatusEnum;
import at.specure.android.util.net.NetworkInfoCollector.IpStatus;
import at.specure.android.util.net.NetworkInfoCollector.OnNetworkInfoChangedListener;
import at.specure.android.util.net.NetworkUtil;
import at.specure.android.util.net.NetworkUtil.MinMax;
import at.specure.android.util.net.RealTimeInformation;
import at.specure.android.util.net.ZeroMeasurementDetector;
import at.specure.android.views.CustomGauge;
import at.specure.android.views.GroupCountView;
import at.specure.androidX.data.badges.Badge;
import at.specure.androidX.data.badges.BadgesViewModel;
import at.specure.util.BandCalculationUtil;
import timber.log.Timber;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

public class MainMenuFragment extends BaseFragment implements MainFragmentInterface, GraphInterface, LocationChangeListener {

    public final static String BUNDLE_INFO_LAST_ANTENNA_IMAGE = "last_antenna_image";
    public final static int BACKGROUND_TRAFFIC_MEASUREMENT_TIME = 10000;
    public final static int INFORMATION_COLLECTOR_TIME = 1000;
    public static final int PROGRESS_SEGMENTS_PROGRESS_RING = 143;
    public static final int PROGRESS_SEGMENTS_QOS = 143;
    public static final long NANO_MULTIPLIER = 1000000000;
    public static final long STARTING_PERCENTAGE = 40L * NANO_MULTIPLIER; // segments for init and ping together from SpeedTEstStatViewController
    @SuppressWarnings("PointlessArithmeticExpression")
    public static final long RIGHT_GRAPH_SHIFT = 0L * NANO_MULTIPLIER;
    public static final Format PERCENT_FORMAT = new DecimalFormat("00%");
    public static final long MAX_COUNTER_WITHOUT_RESULT = 100;
    /**
     * used for smoothing the speed graph: amount of data needed for smoothing function
     */
    public static final int SMOOTHING_DATA_AMOUNT = 5;
    /**
     * smoothing function used for speed graph.
     * BEWARE: different functions could require different data amounts
     */
    public static final SmoothGraph.SmoothingFunction SMOOTHING_FUNCTION = SmoothGraph.SmoothingFunction.CENTERED_MOVING_AVARAGE;
    private static final String DEBUG_TAG = "MainMenuFragment";
    private static final String TAG = "RMBTTestFragment";
    private static final String BUNDLE_TEST_UUID = "BUNDLE_TEST_UUID";
    private static final long GRAPH_MAX_NSECS = 12000000000L; //5sec for download 5 sec for upload and some reserve between the upload and download part (2s)
    @SuppressWarnings("unused")
    private static final long MEASUREMENT_PERCENTAGE = 66L * NANO_MULTIPLIER;
    private static final int SLOW_UPDATE_COUNT = 20;
    public static int PROGRESS_SEGMENTS_TOTAL = PROGRESS_SEGMENTS_PROGRESS_RING + PROGRESS_SEGMENTS_QOS;
    @SuppressWarnings("FieldCanBeLocal")
    private final String OPTION_ON_CREATE_VIEW_CREATE_SPEED_GRAPH = "create_speed_graph";
    @SuppressWarnings("FieldCanBeLocal")
    private final String OPTION_ON_CREATE_VIEW_CREATE_SIGNAL_GRAPH = "create_signal_graph";
    private final DecimalFormat cpuPercentFormat = new DecimalFormat("##0.0");
    private final OnClickListener openLocationSettingsOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            showDialogToOpenGPSSettings(getString(R.string.open_gps_settings));
        }
    };
    public Handler infoHandler = new Handler();
    private Object locationInfoObject;
    private TextView locationText;
    private boolean buttonsDisabled;
    private AppCompatSpinner testServerName;
    private View testServer;
    private List<MeasurementServer> testServers;
    private GetGeolocationTask getGeolocationTask;
    private TextView cellIdText;
    private View cellIdContainer;
    private InformationCollector informationCollector;
    private TextView startButtonText;
    private TextView infoNetwork;
    private TextView infoNetworkType;
    private TextView infoSignalStrength;
    private TextView infoSignalStrengthExtra;
    private TextView infoCpuStat;
    private TextView infoMemStat;
    private View ipButton;
    private View locationButton;
    private View trafficButton;
    private View cpuMemStatsButton;
    private View startButton;
    private ImageView ipv4View;
    private ProgressBar ipv4ProgressView;
    private ImageView ipv6View;
    private ProgressBar ipv6ProgressView;
    private ImageView locationView;
    private ImageView antennaView;
    private ImageView ulSpeedView1;
    private ImageView ulSpeedView2;
    private ImageView dlSpeedView1;
    private ImageView dlSpeedView2;
    private ImageView captivePortalWarning;
    private InfoCollector infoCollector = InfoCollector.getInstance();
    private InterfaceTrafficGatherer interfaceTrafficGatherer;
    private ListView infoOverlayList;
    private CardView infoOverlay;
    Observer<List<Badge>> observer = new Observer<List<Badge>>() {
        @Override
        public void onChanged(@Nullable List<Badge> badges) {
            if (badges != null) {
                Timber.i("Observing badges change: %s", badges);
            }
        }
    };

    public Runnable interfaceTrafficRunnable = new Runnable() {

        @Override
        public void run() {
            if (interfaceTrafficGatherer != null) {
                interfaceTrafficGatherer.run();
                final long rxRate = interfaceTrafficGatherer.getRxRate();
                final long txRate = interfaceTrafficGatherer.getTxRate();
                TrafficClassificationEnum rxTrafficClass = TrafficClassificationEnum.classify(rxRate);
                TrafficClassificationEnum txTrafficClass = TrafficClassificationEnum.classify(txRate);

                infoCollector.setUlTraffic(txTrafficClass);
                infoCollector.setDlTraffic(rxTrafficClass);
            }

            if (infoOverlay != null && infoOverlay.getVisibility() == View.VISIBLE) {
                BaseAdapter adapter = (BaseAdapter) infoOverlayList.getAdapter();
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            }

            infoHandler.postDelayed(interfaceTrafficRunnable, BACKGROUND_TRAFFIC_MEASUREMENT_TIME);
        }
    };
    private TextView infoOverlayTitle;
    private Map<OverlayType, InfoArrayAdapter> infoValueListAdapterMap = new HashMap<OverlayType, InfoArrayAdapter>();
    private MainScreenState screenState;
    private CustomGauge testViewUpper;
    private CustomGauge testViewLower;
    private TextView testTextViewLower;
    private TextView testTextViewUpper;
    private ViewGroup testQosProgressView;
    private ViewGroup testGroupCountContainerView;
    private ViewGroup testDownloadGraphContainer;
    private ViewGroup testUploadGraphContainer;
    private TextView testDownloadGraphTitle;
    private TextView testDownloadGraphUnits;
    private TextView testDownloadGraphValue;
    private TextView testUploadGraphTitle;
    private TextView testUploadGraphUnits;
    private TextView testUploadGraphValue;
    private TextView testJitterProgressTitle;
    private TextView testJitterProgressValue;
    private TextView testSignalProgressTitle;
    private TextView testSignalProgressValue;
    private TextView testPacketLossProgressTitle;
    private TextView testPacketLossProgressValue;
    private TextView testPingProgressTitle;
    private TextView testPingProgressValue;
    private Context context;
    private String testUuid;
    private TextView testViewQoSResultFailed;
    private TextView testViewQoSResultPassed;
    private TextView testViewQoSResultPerformed;
    private boolean startScreenServices = true;
    private TextView testViewQoSResultPercentage;
    private ViewsHandler defaultViewsHandler;
    private OnClickListener startButtonLoopModeOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View view) {
            final Intent stopIntent = new Intent(TestService.ACTION_STOP_LOOP, null, getContext(), TestService.class);
            getMainActivity().startService(stopIntent);
            changeScreenState(MainScreenState.DEFAULT, "stop_loop", false);
        }
    };
    private ArrayList<Float> downloadFifo = new ArrayList<>();
    private ArrayList<Float> uploadFifo = new ArrayList<>();
    private TestViewsHandler testViewsHandler;
    private TestResultsViewsHandler testResultViewsHandler;
    private TestQosResultsViewsHandler testQosResultsViewsHandler;
    private LoopModeViewsHandler loopModeViewsHandler;
    private TestQosViewsHandler testQosViewsHandler;
    private View rootView;
    private GraphHandler graphHandler;
    private MainInfoRunnable infoRunnable;
    private MainFragmentController mainFragmentController;
    private View testServerIcon;
    private View testServerNameTitle;
    private View locationTitle;
    private View increasedConsumptionText;
    /**
     *
     */
    private final OnClickListener detailShowOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (hideConsumptionWarning()) return;
            if (infoOverlay != null) {
                switch (v.getId()) {
                    case R.id.title_page_cpu_stats_button:
                        infoOverlayTitle.setText(getResources().getText(OverlayType.CPU_MEM.getResourceId()));
                        infoOverlayList.setAdapter(infoValueListAdapterMap.get(OverlayType.CPU_MEM));
                        break;
                    case R.id.title_page_ip_button:
                        infoOverlayTitle.setText(getResources().getText(OverlayType.IP.getResourceId()));
                        infoOverlayList.setAdapter(infoValueListAdapterMap.get(OverlayType.IP));
                        break;
                    case R.id.title_page_location_button:
                    case R.id.main_fragment__location_title:
                    case R.id.main_fragment__location_enabled_text:
                    case R.id.location_image:
                        if (locationInfoObject != null) {
                            infoOverlayTitle.setText(getResources().getText(R.string.title_screen_info_overlay_location));
                            infoOverlayList.setAdapter(infoValueListAdapterMap.get(OverlayType.LOCATION));
                        } /*else {
                            showDialogToOpenGPSSettings(getString(R.string.open_gps_settings));
                            return;
                        }*/
                        break;
                    case R.id.title_page_traffic_button:
                    default:
                        infoOverlayTitle.setText(getResources().getText(OverlayType.TRAFFIC.getResourceId()));
                        infoOverlayList.setAdapter(infoValueListAdapterMap.get(OverlayType.TRAFFIC));
                        break;
                }
                //System.out.println("SHOWING INFO OVERLAY");
                if (ipButton != null) {
                    ipButton.setOnClickListener(detailHideOnClickListener);
                }
                //antennaView.setOnClickListener(detailHideOnClickListener);
                if (cpuMemStatsButton != null) {
                    cpuMemStatsButton.setOnClickListener(detailHideOnClickListener);
                }

                trafficButton.setOnClickListener(detailHideOnClickListener);
                locationButton.setOnClickListener(detailHideOnClickListener);
                locationView.setOnClickListener(detailHideOnClickListener);
                locationText.setOnClickListener(detailHideOnClickListener);
                locationTitle.setOnClickListener(detailHideOnClickListener);
                infoOverlay.setVisibility(View.VISIBLE);
                infoOverlay.bringToFront();
                infoOverlayList.invalidate();
            }
        }
    };
    private final OnClickListener detailHideOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (infoOverlay != null) {
                InfoArrayAdapter adapter = (InfoArrayAdapter) infoOverlayList.getAdapter();
                if (adapter != null) {
                    OverlayType overlayType = adapter.getOverlayType();
                    if (overlayType != null) {
                        if ((overlayType.getButtonId() == v.getId()) ||
                                v.getId() == R.id.info_overlay) {
                            hideOverlayAndReenableOnClickListeners();
                        } else {
                            if (((v.getId() == R.id.location_image)
                                    || (v.getId() == R.id.main_fragment__location_enabled_text)
                                    || (v.getId() == R.id.main_fragment__location_title)
                                    || (v.getId() == R.id.title_page_location_button))
                                    && ((overlayType.getButtonId() == R.id.location_image)
                                    || (overlayType.getButtonId() == R.id.main_fragment__location_enabled_text)
                                    || (overlayType.getButtonId() == R.id.main_fragment__location_title)
                                    || (overlayType.getButtonId() == R.id.title_page_location_button)
                            )) {
                                hideOverlayAndReenableOnClickListeners();
                            } else {
                                detailShowOnClickListener.onClick(v);
                            }
                        }
                    }
                }
            }
        }
    };
    private boolean enableMeasurementServersClick;
    private OnMeasurementServersLoaded onMeasurementServersLoaded = new OnMeasurementServersLoaded() {
        @Override
        public void onServersLoaded(List<MeasurementServer> servers) {
            updateServerList(servers);
        }
    };
    private OnNetworkInfoChangedListener onNetworkChangedListener = new OnNetworkInfoChangedListener() {

        @Override
        public void onChange(InfoFlagEnum infoFlag, final Object newValue) {

            Context applicationContext = null;
            try {
                applicationContext = MainMenuFragment.this.context.getApplicationContext();
            } catch (Exception e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }

            if (applicationContext != null) {
                Timber.e("INFO NETWORK %s \n %s \n %s", InfoCollector.getInstance().getNetworkTypeString(), NetworkInfoCollector.getInstance(applicationContext).hasConnectionFromAndroidApi(), NetworkInfoCollector.getInstance(applicationContext).getActiveNetworkInfo());
                switch (infoFlag) {
                    case NETWORK_CONNECTION_CHANGED:
                        if ((Boolean) newValue) {
                            if (startButton != null && startButtonText != null) {
                                startButton.setAlpha(1f);
                                startButton.setEnabled(true);
                                startButtonText.setAlpha(1f);
                            }
                        }
                        break;
                    default:
                        if (infoCollector != null) {
                            if (infoFlag == InfoFlagEnum.PRIVATE_IPV4_CHANGED || infoFlag == InfoFlagEnum.PRIVATE_IPV6_CHANGED) {
                                if (NetworkInfoCollector.getInstance(applicationContext).hasConnectionFromAndroidApi()) {
                                    infoCollector.dispatchInfoChangedEvent(InfoCollector.InfoCollectorType.SIGNAL, 0, infoCollector.getSignal());
                                    infoCollector.dispatchInfoChangedEvent(InfoCollector.InfoCollectorType.SIGNAL_RSRQ, 0, infoCollector.getSignalRsrq());
                                } else {
                                    infoCollector.setSignal(Integer.MIN_VALUE);
                                    infoCollector.setSignalRsrq(null);
                                }
                            }
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    infoCollector.dispatchInfoChangedEvent(InfoCollector.InfoCollectorType.IPV4, infoCollector.getIpv4(), newValue);
                                    infoCollector.dispatchInfoChangedEvent(InfoCollector.InfoCollectorType.IPV6, infoCollector.getIpv6(), newValue);
                                }
                            }, 300);

                        }
                }
            }
        }

    };
    private OnClickListener startButtonOnClickListener = new OnClickListener() {
        public void onClick(View v) {
            TestConfig.setShouldShowResults(false);
            if (hideConsumptionWarning()) {
                return;
            }

            if (!NotificationManagerCompat.from(getActivity()).areNotificationsEnabled()) {
                // toast is also blocked when there are blocked notifications
                Toast toast = Toast.makeText(getActivity(), R.string.notifications_disabled, Toast.LENGTH_LONG);
                toast.show();
            }
            boolean loopMode = LoopModeConfig.isLoopMode(getContext());
            if (loopMode) {
                if (!GeoLocationX.getInstance(getMainActivity().getApplication()).isGeolocationEnabled(getContext())) {
                    showDialogToOpenGPSSettings(getString(R.string.loop_mode_open_gps_settings));
                }
            }

            if (!buttonsDisabled) {
                buttonsDisabled = true;
                boolean isZeroMeasurement = false;
                if (ConfigHelper.detectZeroMeasurementEnabled(context)) {

                    //TODO: REMOVE
                   /* TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                    List<CellInfo> allCellInfo = null;
                    boolean apiLevel17andBigger = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;

                    boolean accessToLocationGranted = isCoarseLocationPermitted(context);

                    if (apiLevel17andBigger && accessToLocationGranted) {
                        //it is checked in static method
                        if (telephonyManager != null) {
                            allCellInfo = telephonyManager.getAllCellInfo();
                        }
                    }

                    if (apiLevel17andBigger && accessToLocationGranted) {
                        //it is checked in static method
                        if (telephonyManager != null) {
                            allCellInfo = telephonyManager.getAllCellInfo();
                        }
                    }
                    boolean noConnection = false;
                    Integer signal = informationCollector.getSignal();
                    boolean isSignalRsrp = informationCollector.getSignalType() == InformationCollector.SINGAL_TYPE_RSRP;
                    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo activeNetworkInfo = null;
                    if (cm != null) {
                        activeNetworkInfo = cm.getActiveNetworkInfo();
                    }

                    String info = "";
                    if ((activeNetworkInfo == null)) {
                            info = info + "\nactiveNetworkInfo is null";
                        } else {
                        info = info + "\nactiveNetworkInfo is: " + activeNetworkInfo;
                        }
                    if (allCellInfo != null) {
                        info = info + "\nallCellInfo is: " + allCellInfo;
                    } else {
                        info = info + "\nallCellInfo is: null";
                    }

                    if (signal != null) {
                        info = info + "\nsignal is: " + signal;
                    } else {
                        info = info + "\nsignal is: null";
                    }

                    if (activeNetworkInfo == null) {// && ((allCellInfo == null) || (allCellInfo.isEmpty())) && (apiLevel17andBigger)) {
                        noConnection = true;
                        info = info + "\nRESULT: true";
                    } else {
                        info = info + "\nRESULT: false";
                    }

//                    Toast.makeText(context, info, Toast.LENGTH_LONG).show();
                    //
*/
                    if (context != null && ConfigHelper.detectZeroMeasurementEnabled(context)) {
                        isZeroMeasurement = ZeroMeasurementDetector.detectZeroMeasurement(getMainActivity(), context, informationCollector);
                    }
                }


                //this was problem while user wants to detect zero measurements so that is why it was moved here, removed because of ethernet
//                if (!isNetworkLoaded()) {
//                    buttonsDisabled = false;
//                    return;
//                }

                //enable to start and run loop mode when there is no signal
                if (isZeroMeasurement && LoopModeConfig.isLoopMode(getActivity())) {
                    {
                        MainScreenState mainScreenState = ((MainActivity) getActivity()).startTest(screenState);
                        buttonsDisabled = mainScreenState != screenState;
                        if (buttonsDisabled) {
                            changeScreenState(mainScreenState, "StartButton - startTest", true);
                        }
                    }
                }

                if (!isZeroMeasurement) {


                    if (FeatureConfig.TEST_SHOW_TRAFFIC_WARNING) {
                        AlertDialog alert = new AlertDialog.Builder(getActivity()).
                                setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        ((MainActivity) getActivity()).startTest(screenState);


                                    }
                                }).
                                setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        System.out.println("Decline");
                                        buttonsDisabled = false;
                                    }
                                }).
                                setMessage(R.string.start_data_consumption_warning).
                                setCancelable(false).
                                create();

                        alert.show();
                    } else {
                        MainActivity activity = (MainActivity) getActivity();
                        NetworkInfoCollector networkInfoCollector2 = activity.getNetworkInfoCollector();
//                        if ((networkInfoCollector2 != null) && (!networkInfoCollector2.hasConnectionFromAndroidApi())) {
//                            activity.showNoNetworkConnectionToast();
//                            buttonsDisabled = false;
//                        } else
                        {
                            MainScreenState mainScreenState = activity.startTest(screenState);
                            buttonsDisabled = mainScreenState != screenState;
                            if (buttonsDisabled) {
                                changeScreenState(mainScreenState, "StartButton - startTest", true);
                            }
                        }

                    }

                } else {
                    buttonsDisabled = false;
                }
            }

        }
    };
    private OnClickListener showDetailedResultsOnClick = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if (FeatureConfig.showLayoutTheme(getMainActivity()) == FeatureConfig.LAYOUT_SQUARE) {
                getMainActivity().showResultsAfterTest(testUuid);
            } else {
                getMainActivity().showDetailedResultsAfterTest(testUuid);
            }

        }
    };
    private OnClickListener mapButtonOnClickListener = new OnClickListener() {
        public void onClick(View v) {
            if (hideConsumptionWarning()) {
                return;
            }
            if (!buttonsDisabled) {
                buttonsDisabled = true;
//                startActivity(new Intent(getActivity(), FilterListActivity.class));
                ((MainActivity) getActivity()).showMap(true);
            }
        }
    };
    private final InfoCollector.OnInformationChangedListener onInfoChangedListener = new InfoCollector.OnInformationChangedListener() {
        @Override
        public void onInformationChanged(InfoCollector.InfoCollectorType type, Object oldValue, Object newValue) {
            if (isAdded()) {
                PorterDuff.Mode mode = PorterDuff.Mode.SRC_ATOP;
                Drawable drawable = null;
                NetworkInfoCollector netInfo;
                switch (type) {
                    case CPU:
                        setViewText(infoCpuStat, cpuPercentFormat.format(newValue) + "%");
                        Timber.e("cpuValue %s %%", cpuPercentFormat.format(newValue));
                        setViewTextColorResource(infoCpuStat, CpuMemClassificationEnum.classify((Float) newValue).getResId());
                        MainActivity mainActivity = getMainActivity();
                        if ((mainActivity != null) && (screenState == MainScreenState.LOOP_MODE_ACTIVE)) {

                            mainActivity.checkLoopModeRunning(new LoopModeActivityCheckListener() {
                                @Override
                                public void onLoopModeRunning(boolean isRunning) {
                                    if (isRunning) {
                                        changeScreenState(MainScreenState.LOOP_MODE_ACTIVE, "infoRunnable", false);
                                    } else {
                                        changeScreenState(MainScreenState.DEFAULT, "infoRunnable", false);
                                    }
                                }
                            });
                        }
                        break;
                    case MEMORY:
                        setViewText(infoMemStat, cpuPercentFormat.format(newValue) + "%");
                        //setViewTextColorResource(infoMemStat, CpuMemClassificationEnum.classify((Float) newValue).getTextResId());
                        break;

                    case LOCATION:
                        try {
                            if (newValue == null) {
                                infoValueListAdapterMap.get(OverlayType.LOCATION).removeElement(InfoOverlayEnum.LOCATION_ACCURACY);
                                infoValueListAdapterMap.get(OverlayType.LOCATION).removeElement(InfoOverlayEnum.LOCATION_AGE);
                                infoValueListAdapterMap.get(OverlayType.LOCATION).removeElement(InfoOverlayEnum.LOCATION_SOURCE);
                                infoValueListAdapterMap.get(OverlayType.LOCATION).removeElement(InfoOverlayEnum.LOCATION_ALTITUDE);
                            } else {
                                infoValueListAdapterMap.get(OverlayType.LOCATION).addElement(InfoOverlayEnum.LOCATION_ACCURACY);
                                infoValueListAdapterMap.get(OverlayType.LOCATION).addElement(InfoOverlayEnum.LOCATION_AGE);
                                infoValueListAdapterMap.get(OverlayType.LOCATION).addElement(InfoOverlayEnum.LOCATION_SOURCE);
                                infoValueListAdapterMap.get(OverlayType.LOCATION).addElement(InfoOverlayEnum.LOCATION_ALTITUDE);

                                at.specure.android.api.jsons.Location location = null;
                                Location loc = (Location) newValue;
                                if (loc != null) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                                        location = new at.specure.android.api.jsons.Location(loc.getTime(), loc.getElapsedRealtimeNanos(), loc.getLatitude(), loc.getLongitude(), (double) loc.getAccuracy(), loc.getAltitude(), (double) loc.getBearing(), (double) loc.getSpeed(), loc.getProvider());
                                    } else {
                                        location = new at.specure.android.api.jsons.Location(loc.getTime(), -1L, loc.getLatitude(), loc.getLongitude(), (double) loc.getAccuracy(), loc.getAltitude(), (double) loc.getBearing(), (double) loc.getSpeed(), loc.getProvider());

                                    }
                                }

                                if (!LoopModeConfig.isCurrentlyPerformingLoopMode(getContext())) {
                                    ((MainActivity) getActivity()).getMeasurementServers(onMeasurementServersLoaded, location, false);
                                }
                            }
                        } catch (NullPointerException e) {
                            //TODO: fix - do nothing if map is empty and you wanna remove something
                        }
                        break;
                    case NETWORK_TYPE:
                        if (antennaView != null) {
                            //System.out.println("NETWORK_TYPE changed to: " + newValue);
                            Integer signal = informationCollector.getSignal();
                            refreshAntennaImage(signal != null ? signal : Integer.MIN_VALUE);
                        }

                        //no break; here!!
                        //if the network type or the network family changes, the same label TextView is used
                    case NETWORK_FAMILY:
                        showNetworkNameAndType(InfoCollector.getInstance().getNetworkTypeString());

                        break;
                    case NETWORK_NAME:
                        //reset all IPs on network change name:
                        resetAllIPandGetNewOne();
                        break;
                    case SIGNAL_RSRQ:
                        showSignal(infoCollector.getSignalType());
                        break;
                    case SIGNAL:

                        if (antennaView != null && newValue != null) {
                            antennaView.setVisibility(View.VISIBLE);
                            refreshAntennaImage((Integer) newValue);
                        }

                        BandCalculationUtil.FrequencyInformation cellBandAndFrequency = RealTimeInformation.getCellBandAndFrequency(getMainActivity());
                        if (cellBandAndFrequency != null) {
                            Timber.e("FREQUENCY BAND %s", cellBandAndFrequency.getBand());
                            Timber.e("FREQUENCY DL %s", cellBandAndFrequency.getFrequencyDL());
                            Timber.e("FREQUENCY UL %s", cellBandAndFrequency.getFrequencyULfromDL());
                        }


                        showSignal(infoCollector.getSignalType());
                        break;
                    case IPV4:
                    case IPV6:
                        Timber.e("IP CHANGE onInformationChanged()");
                        if (getActivity() != null) {
                            netInfo = ((MainActivity) getActivity()).getNetworkInfoCollector();
                            if (netInfo != null) {
                                if (infoValueListAdapterMap.get(OverlayType.IP) != null) {


                                    if (netInfo.getPublicIpv4() != null) {
                                        netInfo.setCaptivePortalStatus(CaptivePortalStatusEnum.NOT_FOUND);
                                        infoValueListAdapterMap.get(OverlayType.IP).addElement(InfoOverlayEnum.IPV4_PUB, 1);
                                    } else {
                                        if (infoValueListAdapterMap.get(OverlayType.IP) != null) {
                                            infoValueListAdapterMap.get(OverlayType.IP).removeElement(InfoOverlayEnum.IPV4_PUB);
                                        }
                                    }

                                    if (netInfo.getPublicIpv6() != null) {
                                        netInfo.setCaptivePortalStatus(CaptivePortalStatusEnum.NOT_FOUND);
                                        infoValueListAdapterMap.get(OverlayType.IP).addElement(InfoOverlayEnum.IPV6_PUB, 2);
                                    } else {
                                        if (infoValueListAdapterMap.get(OverlayType.IP) != null) {
                                            infoValueListAdapterMap.get(OverlayType.IP).removeElement(InfoOverlayEnum.IPV6_PUB);
                                        }
                                    }
                                }

                                //Timber.d(DEDBUG_TAG, "IPv4: " + netInfo.getIpv4Status() + ", IPv6: " + netInfo.getIpv6Status());
                                if (ipv4View != null && ipv6ProgressView != null) {
                                    if (netInfo.getIpv4Status() == IpStatus.STATUS_NOT_AVAILABLE) {
                                        ipv4ProgressView.setVisibility(View.VISIBLE);
                                        ipv4View.setVisibility(View.GONE);
                                    } else {
                                        ipv4ProgressView.setVisibility(View.GONE);
                                        ipv4View.setVisibility(View.VISIBLE);
                                        ipv4View.setImageResource(netInfo.getIpv4Status().getResourceId());
                                    }
                                }
                                if (ipv6View != null && ipv6ProgressView != null) {
                                    if (netInfo.getIpv6Status() == IpStatus.STATUS_NOT_AVAILABLE) {
                                        ipv6ProgressView.setVisibility(View.VISIBLE);
                                        ipv6View.setVisibility(View.GONE);
                                    } else {
                                        ipv6ProgressView.setVisibility(View.GONE);
                                        ipv6View.setVisibility(View.VISIBLE);
                                        ipv6View.setImageResource(netInfo.getIpv6Status().getResourceId());
                                    }
                                }

                                if (netInfo.getIpv4Status().equals(IpStatus.CONNECTED_NAT)
                                        || netInfo.getIpv4Status().equals(IpStatus.CONNECTED_NO_NAT)
                                        || netInfo.getIpv6Status().equals(IpStatus.CONNECTED_NAT)
                                        || netInfo.getIpv6Status().equals(IpStatus.CONNECTED_NO_NAT)) {
                                }
                            }
                        }
                        break;
                    case UL_TRAFFIC:
                        if (ulSpeedView1 != null) {

                            TrafficClassificationEnum trafficEnum = (TrafficClassificationEnum) newValue;
                            switch (trafficEnum) {
                                case NONE:
                                case UNKNOWN:
                                    ulSpeedView1.setImageResource(R.drawable.arrow_grey);
                                    ulSpeedView2.setImageResource(R.drawable.arrow_grey);
                                    break;
                                case LOW:
                                case MID:
                                    drawable = getResources().getDrawable(R.drawable.arrow_green);
                                    drawable.setColorFilter(getResources().getColor(R.color.titlepage_stats_foreground), mode);
                                    ulSpeedView1.setImageDrawable(drawable);
                                    ulSpeedView2.setImageResource(R.drawable.arrow_grey);
                                    break;
                                case HIGH:
                                    drawable = getResources().getDrawable(R.drawable.arrow_green);
                                    drawable.setColorFilter(getResources().getColor(R.color.titlepage_stats_foreground), mode);
                                    ulSpeedView1.setImageDrawable(drawable);
                                    ulSpeedView2.setImageDrawable(drawable);
                                    break;
                                default:
                                    ulSpeedView1.setImageResource(R.drawable.arrow_grey);
                                    ulSpeedView2.setImageResource(R.drawable.arrow_grey);
                            }
                        }
                        break;
                    case DL_TRAFFIC:
                        if (dlSpeedView1 != null) {
                            TrafficClassificationEnum trafficEnum = (TrafficClassificationEnum) newValue;
                            switch (trafficEnum) {
                                case NONE:
                                case UNKNOWN:
                                    dlSpeedView1.setImageResource(R.drawable.arrow_grey);
                                    dlSpeedView2.setImageResource(R.drawable.arrow_grey);
                                    break;
                                case LOW:
                                case MID:
                                    drawable = getResources().getDrawable(R.drawable.arrow_green);
                                    drawable.setColorFilter(getResources().getColor(R.color.titlepage_stats_foreground), mode);
                                    dlSpeedView1.setImageDrawable(drawable);
                                    dlSpeedView2.setImageResource(R.drawable.arrow_grey);
                                    break;
                                case HIGH:
                                    drawable = getResources().getDrawable(R.drawable.arrow_green);
                                    drawable.setColorFilter(getResources().getColor(R.color.titlepage_stats_foreground), mode);
                                    dlSpeedView1.setImageDrawable(drawable);
                                    dlSpeedView2.setImageDrawable(drawable);
                                    break;
                                default:
                                    dlSpeedView1.setImageResource(R.drawable.arrow_grey);
                                    dlSpeedView2.setImageResource(R.drawable.arrow_grey);
                            }
                        }
                        break;
                    case CAPTIVE_PORTAL_STATUS:

                        setCaptivePortalStatus((Boolean) newValue);
                        break;
                    case CELL_ID:
                        showCellId(InfoCollector.getInstance().getNetworkTypeString());
                        break;
                    case LOOP_MODE_FINISHED:
                        if (screenState != MainScreenState.DEFAULT) {
                            changeScreenState(MainScreenState.DEFAULT, "loop mode finished", true);
                        }
                        break;
                    case LOOP_MODE:
                        Timber.e("LOOP MODE onInformationChanged()");
                        if (screenState == MainScreenState.LOOP_MODE_ACTIVE) {
                            testTextViewLower.setVisibility(View.VISIBLE);
                            Integer loopModeMax = InfoCollector.getInstance().getLoopModeMax();
                            Integer loopModeCurrent = InfoCollector.getInstance().getLoopModeCurrent();
                            if (loopModeMax == 0) {
                                testTextViewLower.setText(loopModeCurrent + "");
                            } else {
                                testTextViewLower.setText(loopModeCurrent + "/" + loopModeMax);
                                if (testViewLower != null) {
                                    int i = loopModeCurrent * PROGRESS_SEGMENTS_QOS / loopModeMax;
                                    testViewLower.setValue(i);
                                }
                            }


                            if (testViewUpper != null) {
                                Long loopModeRemainingTimeToNextTest = InfoCollector.getInstance().getLoopModeRemainingTimeToNextTest();
                                if (loopModeRemainingTimeToNextTest == null) {
                                    loopModeRemainingTimeToNextTest = 0L;
                                }
                                Integer remainingTime = (int) (loopModeRemainingTimeToNextTest / 1000);
                                int loopModeMinDelay = LoopModeConfig.getLoopModeMinDelay(getContext());
                                if (remainingTime != null) {
                                    if (remainingTime < 0) {
                                        remainingTime = loopModeMinDelay;
                                    }

                                    int i = loopModeMinDelay - remainingTime;
                                    if (i < 0) {
                                        i = 0;
                                    }
                                    if (loopModeMinDelay < 1) {
                                        loopModeMinDelay = 1;
                                    }
                                    i = (PROGRESS_SEGMENTS_PROGRESS_RING * i) / loopModeMinDelay;
                                    testViewUpper.setValue(i);
                                    Timber.e("testViewUpper I: %s", i);
                                } else {

                                    testViewUpper.setValue(PROGRESS_SEGMENTS_PROGRESS_RING);
                                    Timber.e("testViewUpper LoopMinDelay: %s", PROGRESS_SEGMENTS_PROGRESS_RING);
                                }
                                Timber.e("testViewUpper Value: %s     EndValue: %s", testViewUpper.getValue(), testViewUpper.getEndValue());
                                testViewUpper.invalidate();
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    };
    private BadgesViewModel badgesModel;
    private LiveData<List<Badge>> data1;
    private boolean forceUpdate = false;

    private boolean isNetworkLoaded() {
        if (antennaView != null) {
            Object tag = antennaView.getTag();
            if (tag == null) {
                return false;
            }
        }
        return !(((infoNetwork != null) && (infoNetwork.getText() != null) && (infoNetwork.getText().toString().isEmpty()))
                || ((infoNetwork != null) && (infoNetwork.getText() == null)));
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.i("onCreate");
        getActivity().setRequestedOrientation(SCREEN_ORIENTATION_PORTRAIT);
        String waitText = getActivity().getResources().getString(R.string.test_progress_text_wait);
        mainFragmentController = new MainFragmentController(this, this, waitText);
    }

    @Override
    public void onStop() {
        super.onStop();
        mainFragmentController.unbindTestingService();
    }

    @Override
    public void onStart() {
        super.onStart();

        if ((screenState != MainScreenState.LOOP_MODE_ACTIVE) && (screenState == MainScreenState.TESTING || screenState == MainScreenState.QOS_TESTING || screenState == MainScreenState.DEFAULT)) {
            mainFragmentController.bindTestingService();
        }
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.main_fragment, container, false);
        return createView(view, inflater, savedInstanceState);
    }

    public MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }

    private View createView(View view, LayoutInflater inflater, Bundle savedInstanceState) {

        rootView = view;
        screenState = MainScreenState.DEFAULT;

        context = getActivity().getApplicationContext();

        startButton = view.findViewById(R.id.title_page_start_button);
        testServerName = view.findViewById(R.id.main_fragment__test_server_name);
        testServerNameTitle = view.findViewById(R.id.main_fragment__test_server_title);
        testServer = view.findViewById(R.id.main_fragment__test_server_container);
        testServerIcon = view.findViewById(R.id.main_fragment__test_server_icon);
        infoNetwork = view.findViewById(R.id.main_fragment__network_name);
        infoNetworkType = view.findViewById(R.id.main_fragment__network_type);
        infoSignalStrength = view.findViewById(R.id.main_fragment__signal_strength);
        infoSignalStrengthExtra = view.findViewById(R.id.info_signal_strength_extra);
        infoCpuStat = view.findViewById(R.id.cpu_status);
        infoMemStat = view.findViewById(R.id.ram_status);
        locationView = view.findViewById(R.id.location_image);
        locationText = view.findViewById(R.id.main_fragment__location_enabled_text);
        locationTitle = view.findViewById(R.id.main_fragment__location_title);
        locationButton = view.findViewById(R.id.title_page_location_button);
        ipv4View = view.findViewById(R.id.ipv4_status);
        ipv4ProgressView = view.findViewById(R.id.ipv4_status_progress_bar);
        ipv6View = view.findViewById(R.id.ipv6_status);
        ipv6ProgressView = view.findViewById(R.id.ipv6_status_progress_bar);
        ipButton = view.findViewById(R.id.title_page_ip_button);
        trafficButton = view.findViewById(R.id.title_page_traffic_button);
        cpuMemStatsButton = view.findViewById(R.id.title_page_cpu_stats_button);
        infoOverlayList = view.findViewById(R.id.info_overlay_list);
        cellIdText = view.findViewById(R.id.main_fragment__cell_id);
        cellIdContainer = view.findViewById(R.id.main_fragment__cell_id_container);
        infoOverlayTitle = view.findViewById(R.id.info_overlay_title);
        infoOverlay = view.findViewById(R.id.info_overlay);
        captivePortalWarning = view.findViewById(R.id.captive_portal_image);
        antennaView = view.findViewById(R.id.antenne_image);
        ulSpeedView1 = view.findViewById(R.id.traffic_ul_1_image);
        ulSpeedView2 = view.findViewById(R.id.traffic_ul_2_image);
        dlSpeedView1 = view.findViewById(R.id.traffic_dl_1_image);
        dlSpeedView2 = view.findViewById(R.id.traffic_dl_2_image);

        startButtonText = view.findViewById(R.id.start_button_text);

        {
            increasedConsumptionText = rootView.findViewById(R.id.increased_consumption_button_text);

            testViewUpper = view.findViewById(R.id.gauge_upper);
            testViewLower = view.findViewById(R.id.gauge_lower);
            testTextViewLower = view.findViewById(R.id.text_view_lower_test);
            testTextViewUpper = view.findViewById(R.id.text_view_upper_test);
            testQosProgressView = view.findViewById(R.id.test_view_qos_container);
            testGroupCountContainerView = view.findViewById(R.id.test_view_group_count_container);

            testDownloadGraphContainer = view.findViewById(R.id.test_progress__download_graph);
            testUploadGraphContainer = view.findViewById(R.id.test_progress__upload_graph);

            testDownloadGraphTitle = testDownloadGraphContainer.findViewById(R.id.test_progress__small_graph_title);
            testDownloadGraphUnits = testDownloadGraphContainer.findViewById(R.id.test_progress__small_graph_units);
            testDownloadGraphValue = testDownloadGraphContainer.findViewById(R.id.test_progress__small_graph_value);

            testUploadGraphTitle = testUploadGraphContainer.findViewById(R.id.test_progress__small_graph_title);
            testUploadGraphUnits = testUploadGraphContainer.findViewById(R.id.test_progress__small_graph_units);
            testUploadGraphValue = testUploadGraphContainer.findViewById(R.id.test_progress__small_graph_value);

            ViewGroup progressJitterGroup = view.findViewById(R.id.test_progress_jitter);
            ViewGroup progressSignalGroup = view.findViewById(R.id.test_progress_signal_strength);
            ViewGroup progressPacketLossGroup = view.findViewById(R.id.test_progress_packet_loss);
            ViewGroup progressPingGroup = view.findViewById(R.id.test_progress_ping);

            testJitterProgressTitle = progressJitterGroup.findViewById(R.id.test_progress_info_item_title);
            testJitterProgressValue = progressJitterGroup.findViewById(R.id.test_progress_info_item_value);

            testSignalProgressTitle = progressSignalGroup.findViewById(R.id.test_progress_info_item_title);
            testSignalProgressValue = progressSignalGroup.findViewById(R.id.test_progress_info_item_value);

            testPacketLossProgressTitle = progressPacketLossGroup.findViewById(R.id.test_progress_info_item_title);
            testPacketLossProgressValue = progressPacketLossGroup.findViewById(R.id.test_progress_info_item_value);

            testPingProgressTitle = progressPingGroup.findViewById(R.id.test_progress_info_item_title);
            testPingProgressValue = progressPingGroup.findViewById(R.id.test_progress_info_item_value);

            testViewQoSResultFailed = view.findViewById(R.id.qos_result_failed);
            testViewQoSResultPassed = view.findViewById(R.id.qos_result_passed);
            testViewQoSResultPerformed = view.findViewById(R.id.qos_result_performed);
            testViewQoSResultPercentage = view.findViewById(R.id.qos_result_percentage);

            testSignalProgressTitle.setText(R.string.test_signal_strength);
            testJitterProgressTitle.setText(R.string.test_bottom_test_status_jitter);
            testPacketLossProgressTitle.setText(R.string.test_packet_loss);
            testPingProgressTitle.setText(R.string.test_ping);
        }


        if (antennaView != null) {
            if (savedInstanceState != null && savedInstanceState.containsKey(BUNDLE_INFO_LAST_ANTENNA_IMAGE)) {
                int antennaImageId = savedInstanceState.getInt(BUNDLE_INFO_LAST_ANTENNA_IMAGE);
                antennaView.setImageResource(antennaImageId);
                antennaView.setTag(antennaImageId);
            }
        }

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(BUNDLE_TEST_UUID)) {
                testUuid = savedInstanceState.getString(BUNDLE_TEST_UUID);
                if (testUuid != null) {
                    Timber.d("Current test uuid: %s", testUuid);
                }
             }
        }

        getMainActivity().checkLoopModeRunning(new LoopModeActivityCheckListener() {
            @Override
            public void onLoopModeRunning(boolean isRunning) {
                if (isRunning) {
                    screenState = MainScreenState.LOOP_MODE_ACTIVE;
                    changeScreenState(screenState, "OnCreate", true);
                } else {
                    changeScreenState(screenState, "OnCreate", true);
                }
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        badgesModel = ViewModelProviders.of(this).get(BadgesViewModel.class);
        data1 = badgesModel.getData();
        data1.observe(this.getViewLifecycleOwner(), observer);
    }

    public void changeScreenState(MainScreenState state, String placeFromCalled, Boolean forceUpdate) {
        Timber.e("State changing to: %s from: %s", state.toString(), placeFromCalled);
        if ((state == screenState) && (!forceUpdate)) {
            return;
        }
        if (state == MainScreenState.TEST_RESULT || state == MainScreenState.QOS_TEST_RESULT) {
            if (isAdded()) {
                MainActivity activity = (MainActivity) getActivity();
                AppRater.testPerformed(activity);
            }
        }
        screenState = state;
        hideOverlayAndReenableOnClickListeners();
        enableMeasurementServersClick = ((screenState != MainScreenState.QOS_TESTING)
                && (screenState != MainScreenState.LOOP_MODE_ACTIVE)
                && (screenState != MainScreenState.TESTING));
        switch (screenState) {
            case DEFAULT:
                HashMap<Integer, OnClickListener> defaultOnClickListeners = new HashMap<>();
                defaultOnClickListeners.put(R.id.title_page_start_button, startButtonOnClickListener);
                defaultOnClickListeners.put(R.id.title_page_map_button, mapButtonOnClickListener);
                defaultOnClickListeners.put(R.id.title_page_traffic_button, detailShowOnClickListener);
                defaultOnClickListeners.put(R.id.title_page_ip_button, detailShowOnClickListener);
                defaultOnClickListeners.put(R.id.title_page_location_button, detailShowOnClickListener);
                defaultOnClickListeners.put(R.id.location_image, detailShowOnClickListener);
                defaultOnClickListeners.put(R.id.main_fragment__location_title, detailShowOnClickListener);
                defaultOnClickListeners.put(R.id.main_fragment__location_enabled_text, detailShowOnClickListener);
                defaultOnClickListeners.put(R.id.title_page_cpu_stats_button, detailShowOnClickListener);
                defaultOnClickListeners.put(R.id.info_overlay, detailHideOnClickListener);

                defaultViewsHandler = new DefaultViewsHandler(rootView, defaultOnClickListeners);
                defaultViewsHandler.initializeViews(rootView, getContext());
                if (interfaceTrafficGatherer == null) {
                    interfaceTrafficGatherer = new InterfaceTrafficGatherer();
                }
                if (informationCollector == null) {
                    informationCollector = InformationCollector.getInstance(getActivity(), true, true, false);
                    mainFragmentController.setInformationCollector(informationCollector);
                }

//                stopScreenServices();
                startScreenServices();
                initializeMainScreenDefaultInfo(interfaceTrafficGatherer);

                buttonsDisabled = false;
                mainFragmentController.unbindTestingService();
//                getMeasurementServersInfo();

//                infoCollector.dispatchInfoChangedEvent(InfoCollectorType.IPV4, infoCollector.getIpv4(), infoCollector.getIpv4());
//                infoCollector.dispatchInfoChangedEvent(InfoCollectorType.IPV6, infoCollector.getIpv6(), infoCollector.getIpv6());
//                refreshIpAddresses();
//                resetAllIPandGetNewOne();
                getMeasurementServersInfo();
                break;

            case TESTING:
                if (downloadFifo != null) {
                    downloadFifo.clear();
                } else {
                    downloadFifo = new ArrayList<>();
                }
                if (uploadFifo != null) {
                    uploadFifo.clear();
                } else {
                    uploadFifo = new ArrayList<>();
                }
//                stopScreenServices();
//                if (graphHandler == null) {
                graphHandler = new GraphHandler(rootView, testDownloadGraphContainer, testUploadGraphContainer);
                graphHandler.initializeGraphs(getContext());
//                }
                mainFragmentController.initializeTesting(graphHandler);

                HashMap<Integer, OnClickListener> testOnClickListeners = new HashMap<>();
                testViewsHandler = new TestViewsHandler(rootView, testOnClickListeners);
                testViewsHandler.initializeViews(rootView, getContext());
                break;

            case QOS_TESTING: //unused now, used after qos will be separated from normal test
                HashMap<Integer, OnClickListener> testQosOnClickListeners = new HashMap<>();
                testQosViewsHandler = new TestQosViewsHandler(rootView, testQosOnClickListeners);
                testQosViewsHandler.initializeViews(rootView, getContext());
                break;

            case TEST_RESULT:
                if (context != null) {
                    TestConfig.setShouldShowResults(false);
                }
                mainFragmentController.unbindTestingService();
                HashMap<Integer, OnClickListener> testResultOnClickListeners = new HashMap<>();
                testResultOnClickListeners.put(R.id.show_detailed_result_button, showDetailedResultsOnClick);
                testResultViewsHandler = new TestResultsViewsHandler(rootView, testResultOnClickListeners);
                testResultViewsHandler.initializeViews(rootView, getContext());
                buttonsDisabled = false;
                getMainActivity().showSurveyRequest();
                boolean badgesFeatureEnabled = BadgesConfig.isBadgesFeatureEnabled(getMainActivity());
                if (badgesFeatureEnabled) {
                    if (BadgesConfig.checkForGettingBadge(getMainActivity(), data1.getValue())) {
                        MainActivity mainActivity = getMainActivity();
                        mainActivity.invalidateOptionsMenu();
                    }
                }
                break;

            case QOS_TEST_RESULT:
                mainFragmentController.unbindTestingService();
                HashMap<Integer, OnClickListener> testQosResultOnClickListeners = new HashMap<>();
                testQosResultOnClickListeners.put(R.id.show_detailed_qos_result_button, showDetailedResultsOnClick);
                testQosResultsViewsHandler = new TestQosResultsViewsHandler(rootView, testQosResultOnClickListeners);
                testQosResultsViewsHandler.initializeViews(rootView, getContext());
                mainFragmentController.initializeQoSResults(testUuid);
                buttonsDisabled = false;
                getMainActivity().showSurveyRequest();
                badgesFeatureEnabled = BadgesConfig.isBadgesFeatureEnabled(getMainActivity());
                if (badgesFeatureEnabled) {
                    if (BadgesConfig.checkForGettingBadge(getMainActivity(), data1.getValue())) {
                        MainActivity mainActivity = getMainActivity();
                        mainActivity.invalidateOptionsMenu();
                    }
                }
                break;


            case LOOP_MODE_ACTIVE:
                Timber.e("LOOP_MODE ACTIVE");
                HashMap<Integer, OnClickListener> loopModeOnClickListeners = new HashMap<>();
                loopModeOnClickListeners.put(R.id.title_page_map_button, mapButtonOnClickListener);
                loopModeOnClickListeners.put(R.id.title_page_start_button, startButtonLoopModeOnClickListener);
                loopModeViewsHandler = new LoopModeViewsHandler(rootView, loopModeOnClickListeners);
                loopModeViewsHandler.initializeViews(rootView, getContext());
                stopScreenServices();
                startScreenServices();
                if (interfaceTrafficGatherer == null) {
                    interfaceTrafficGatherer = new InterfaceTrafficGatherer();
                }
                initializeMainScreenDefaultInfo(interfaceTrafficGatherer);
                getMeasurementServersInfo();
                InfoCollector.getInstance().dispatchInfoChangedEvent(InfoCollector.InfoCollectorType.LOOP_MODE, null, InfoCollector.getInstance().getLoopModeCurrent());
                break;

            default:
                break;
        }
    }

    @Override
    public MainScreenState getScreenState() {
        return screenState;
    }

    private void getMeasurementServersInfo() {
        FragmentActivity activity = getActivity();
        if (activity != null) {
            Location loc = GeoLocationX.getInstance(activity.getApplicationContext()).getLastKnownLocation(activity, this);
            at.specure.android.api.jsons.Location location = null;
            if (loc != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    location = new at.specure.android.api.jsons.Location(loc.getTime(), loc.getElapsedRealtimeNanos(), loc.getLatitude(), loc.getLongitude(), (double) loc.getAccuracy(), loc.getAltitude(), (double) loc.getBearing(), (double) loc.getSpeed(), loc.getProvider());
                } else {
                    location = new at.specure.android.api.jsons.Location(loc.getTime(), -1L, loc.getLatitude(), loc.getLongitude(), (double) loc.getAccuracy(), loc.getAltitude(), (double) loc.getBearing(), (double) loc.getSpeed(), loc.getProvider());
                }
            }
            if (activity != null) {
                ((MainActivity) activity).getMeasurementServers(onMeasurementServersLoaded, location, testServer.getVisibility() != View.VISIBLE);
            }
        }
    }

    private void initializeMainScreenDefaultInfo(InterfaceTrafficGatherer interfaceTrafficGatherer) {
        FragmentActivity activity = getActivity();
        if ((infoOverlayList != null) && (activity != null)) {
            infoValueListAdapterMap.put(OverlayType.TRAFFIC, new InfoArrayAdapter(activity, OverlayType.TRAFFIC, interfaceTrafficGatherer,
                    InfoOverlayEnum.UL_TRAFFIC, InfoOverlayEnum.DL_TRAFFIC));

            infoValueListAdapterMap.put(OverlayType.IP, new InfoArrayAdapter(activity, OverlayType.IP, interfaceTrafficGatherer, InfoOverlayEnum.IPV4,
                    InfoOverlayEnum.IPV4_PUB, InfoOverlayEnum.IPV6, InfoOverlayEnum.IPV6_PUB));

            infoValueListAdapterMap.put(OverlayType.LOCATION, new InfoArrayAdapter(activity, OverlayType.LOCATION, interfaceTrafficGatherer,
                    InfoOverlayEnum.LOCATION));

            infoValueListAdapterMap.put(OverlayType.CPU_MEM, new InfoArrayAdapter(activity, OverlayType.CPU_MEM, interfaceTrafficGatherer,
                    InfoOverlayEnum.CPU_USAGE, InfoOverlayEnum.MEM_USAGE,
                    InfoOverlayEnum.MEM_FREE, InfoOverlayEnum.MEM_TOTAL));
        }
    }

    @Override
    public void onPause() {
        Timber.i("onPause");
        super.onPause();
        stopScreenServices();
    }

    private void stopScreenServices() {
        infoCollector.removeAllListeners();
        try {
            NetworkInfoCollector.getInstance(context.getApplicationContext()).removeOnNetworkInfoChangedListener(onNetworkChangedListener);
            if (infoRunnable != null) {
                infoRunnable.setStop();
            }
            try {
                if ((informationCollector != null) && (!LoopModeConfig.isCurrentlyPerformingLoopMode(context.getApplicationContext()))) {
                    informationCollector.unload();
                }
            } catch (Exception e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }
            infoHandler.removeCallbacks(infoRunnable);
            infoHandler.removeCallbacks(interfaceTrafficRunnable);
            startScreenServices = true;
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    @Override
    public void onResume() {
        Timber.i("onResume");
        super.onResume();
        forceUpdate = true;
        buttonsDisabled = false;

        startScreenServices();

        ((MainActivity) getActivity()).setLockNavigationDrawer(false);
        ((MainActivity) getActivity()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ((MainActivity) getActivity()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        getMainActivity().checkLoopModeRunning(new LoopModeActivityCheckListener() {
            @Override
            public void onLoopModeRunning(boolean isRunning) {
                if (isRunning) {
                    screenState = MainScreenState.LOOP_MODE_ACTIVE;
                    changeScreenState(screenState, "OnResume", true);
                } else {
                    mainFragmentController.bindTestingService();
                    /*screenState = MainScreenState.DEFAULT;
                    changeScreenState(screenState, "OnResume", true);*/
                }
            }
        });


        Timber.e("IP_ADDRESS RESET on RESUME");
        getMeasurementServersInfo();
        refreshIpAddresses();
    }

    private void startScreenServices() {

        try {
            if (startScreenServices) {
                infoCollector.addListener(onInfoChangedListener);
                NetworkInfoCollector.getInstance(context.getApplicationContext()).addOnNetworkChangedListener(onNetworkChangedListener);
                if (informationCollector != null) {
                    Timber.d("SIGNAL CHANGED INIT MainMenu Fragment CLEARED!");
                    informationCollector.init();
                } else {
                    informationCollector = new InformationCollector(this.getContext(), true, true);
                }

                mainFragmentController.setInformationCollector(informationCollector);

                if (infoRunnable == null) {
                    infoRunnable = new MainInfoRunnable(informationCollector, infoHandler);
                } else {
                    infoRunnable.startLoop();
                }
                infoHandler.post(interfaceTrafficRunnable);
                infoCollector.refresh();

                MainActivity mainActivity = getMainActivity();
                if (mainActivity != null) {
//                if (Build.MANUFACTURER.contentEquals("Amazon")) {
//                    Location lastKnownLocation = GPSConfig.getLastKnownLocation(mainActivity, this);
//                    if (lastKnownLocation != null) {
//                        showGeolocation(lastKnownLocation);
//                    }
//                } else {
                    Location lastKnownLocation = GeoLocationX.getInstance(mainActivity.getApplication()).getLastKnownLocation(mainActivity, this);
                    String decodedPosition = GeoLocationX.getInstance(mainActivity.getApplication()).getDecodedPosition();
                    boolean enabledGPS = GeoLocationX.getInstance(mainActivity.getApplication()).isGeolocationEnabled(mainActivity);
                    if (lastKnownLocation != null) {
                        showGeolocation(lastKnownLocation, decodedPosition, enabledGPS);
                    }
//                }

                }

                startScreenServices = false;

                if (context != null) {
                    if (TestConfig.shouldShowResults(context.getApplicationContext())) {
                        TestConfig.setShouldShowResults(false);
                        mainFragmentController.showResult();
                        Timber.d("Showing results of the last test");
                    }
                }
            }
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
    }

    private void refreshAntennaImage(int signal) {
        if (isAdded()) {
            if (antennaView.getVisibility() != View.VISIBLE) {
                antennaView.setVisibility(View.VISIBLE);
            }

            int antennaImageRes = getAntennaImageResourceId(signal);
            antennaView.setImageResource(antennaImageRes);
            antennaView.setTag(antennaImageRes);
        }
    }

    /**
     * @param signal
     * @return
     */
    private int getAntennaImageResourceId(int signal) {
        if (isAdded()) {
            int lastNetworkType = informationCollector.getNetwork();
            String lastNetworkTypeString = Helperfunctions.getNetworkTypeName(lastNetworkType);
            int signalType = informationCollector.getSignalType();
            boolean wlan = "WLAN".equals(lastNetworkTypeString);

            if (showNetworkNameAndType(lastNetworkTypeString)) return R.drawable.cross;

            showSignal(signalType);
            showCellId(lastNetworkTypeString);

            double relativeSignal = -1d;
            MinMax<Integer> signalBounds = NetworkUtil.getSignalStrengthBounds(signalType);

            if (!(signalBounds.min == Integer.MIN_VALUE || signalBounds.max == Integer.MAX_VALUE)) {
                relativeSignal = (double) (signal - signalBounds.min) / (double) (signalBounds.max - signalBounds.min);
            }
            //System.out.println("relativeSignal: " + relativeSignal + ", networkType: " + networkType + ", lastNetworkTypeString: " + lastNetworkTypeString);
            if (relativeSignal < 0.25d) {
                return (wlan ? R.drawable.wifi1 : R.drawable.mobil1);
            } else if (relativeSignal < 0.5d) {
                return (wlan ? R.drawable.wifi1 : R.drawable.mobil2);
            } else if (relativeSignal < 0.75d) {
                return (wlan ? R.drawable.wifi2 : R.drawable.mobil3);
            } else {
                return (wlan ? R.drawable.wifi3 : R.drawable.mobil4);
            }
        }
        //default
        return R.drawable.cross;
    }

    private boolean showNetworkNameAndType(String lastNetworkType) {
        if (this.isAdded()) {
            if (lastNetworkType == null || "UNKNOWN".equalsIgnoreCase(lastNetworkType)) {// || signal == Integer.MIN_VALUE) {
                setViewVisibility(infoNetwork, View.INVISIBLE);
                setViewVisibility(infoNetworkType, View.GONE);
                return true;
            } else {
                if (InfoCollector.getInstance().getNetworkName() != null) {
                    setViewVisibility(infoNetwork, View.VISIBLE);
                    setViewText(infoNetwork, InfoCollector.getInstance().getNetworkName());
                }
                if (InfoCollector.getInstance().getNetworkFamily() != null) {
                    setViewVisibility(infoNetworkType, View.VISIBLE);
                    setViewText(infoNetworkType, InfoCollector.getInstance().getNetworkFamily());
                }
            }
        }
        return false;
    }

    private void showSignal(Integer signalType) {
        if (this.isAdded())
            if (informationCollector != null) {
                int lastNetworkType = informationCollector.getNetwork();
                String lastNetworkTypeString = Helperfunctions.getNetworkTypeName(lastNetworkType);
                boolean showSignal = !("ETHERNET".equals(lastNetworkTypeString)
                        || "LAN".equals(lastNetworkTypeString)
                        || "BLUETOOTH".equals(lastNetworkTypeString));
                if ((signalType != null) && (showSignal)) {
                    Integer signal = InfoCollector.getInstance().getSignal();
                    if ((signal != null) && (signal != Integer.MIN_VALUE)) {
                        if (signalType == InformationCollector.SIGNAL_TYPE_RSRP) {
                            setViewVisibility(infoSignalStrength, View.VISIBLE);
                            setViewText(infoSignalStrength, "RSRP: " + signal + " dBm");

                            Integer signalRsrq = InfoCollector.getInstance().getSignalRsrq();
                            if (signalRsrq != null) {
                                setViewVisibility(infoSignalStrengthExtra, View.VISIBLE);
                                setViewText(infoSignalStrengthExtra, "RSRQ: " + signalRsrq + " dB");
                            } else {
                                setViewVisibility(infoSignalStrengthExtra, View.GONE);
                                setViewText(infoSignalStrengthExtra, "-");
                            }
                        } else {
                            setViewVisibility(infoSignalStrength, View.VISIBLE);
                            setViewText(infoSignalStrength, signal + " dBm");
                            setViewVisibility(infoSignalStrengthExtra, View.GONE);
                        }
                        int color;
                        if (infoCollector.getSignalType() != null) {
                            if (getResources().getBoolean(R.bool.signal_strength_colored)) {
                                if (infoCollector.getSignalType() == InformationCollector.SIGNAL_TYPE_RSRP) {
                                    color = SemaphoreColorHelper.resolveSemaphoreColor(getContext(), signal, SemaphoreColorHelper.SEMAPHORE_TYPE_SIGNAL_LTE);
                                } else if (infoCollector.getSignalType() == InformationCollector.SIGNAL_TYPE_WLAN) {
                                    color = SemaphoreColorHelper.resolveSemaphoreColor(getContext(), signal, SemaphoreColorHelper.SEMAPHORE_TYPE_SIGNAL_WIFI);
                                } else {
                                    color = SemaphoreColorHelper.resolveSemaphoreColor(getContext(), signal, SemaphoreColorHelper.SEMAPHORE_TYPE_SIGNAL_GSM);
                                }
                                setViewTextColorResource(infoSignalStrength, color);
                            }
                        }
                    } else {
                        setViewText(infoSignalStrength, "-");
                        setViewVisibility(infoSignalStrengthExtra, View.GONE);
                        setViewVisibility(infoNetworkType, View.GONE);
                        setViewVisibility(infoNetwork, View.INVISIBLE);
                        setViewText(infoNetwork, "");
                    }
                } else {
                    setViewText(infoSignalStrength, "-");
                    setViewVisibility(infoSignalStrengthExtra, View.GONE);
                    setViewVisibility(infoNetworkType, View.GONE);
                    setViewVisibility(infoNetwork, View.INVISIBLE);
                    setViewText(infoNetwork, "");
                }
            }
    }

    private void showCellId(String lastNetworkTypeString) {
        boolean mobileNetwork = !("WLAN".equals(lastNetworkTypeString)
                || "UNKNOWN".equals(lastNetworkTypeString)
                || "ETHERNET".equals(lastNetworkTypeString)
                || "LAN".equals(lastNetworkTypeString)
                || "BLUETOOTH".equals(lastNetworkTypeString));
        if (mobileNetwork) {
            String cellId = RealTimeInformation.getCellId(context);
            if ((cellId != null) && (!cellId.isEmpty())) {
                setViewVisibility(cellIdContainer, View.VISIBLE);
                setViewVisibility(cellIdText, View.VISIBLE);
                setViewText(cellIdText, cellId);
            } else {
                setViewVisibility(cellIdContainer, View.INVISIBLE);
                setViewVisibility(cellIdText, View.INVISIBLE);
            }
        } else {
            setViewText(cellIdText, "-");
            setViewVisibility(cellIdContainer, View.INVISIBLE);
            setViewVisibility(cellIdText, View.INVISIBLE);
        }
    }

    private void updateServerList(List<MeasurementServer> servers) {
        if (!servers.isEmpty()) {
            testServer.setVisibility(View.VISIBLE);
            testServerName.setVisibility(View.VISIBLE);
            Timber.e("SERVERS SHOW");
            View.OnTouchListener testServerOnClickListener = new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (isShownConsumptionWarning() || !enableMeasurementServersClick) {
                        return false;
                    } else {
                        testServerName.performClick();
                    }
                    return true;
                }
            };

            testServerNameTitle.setOnTouchListener(testServerOnClickListener);
            testServerIcon.setOnTouchListener(testServerOnClickListener);
            testServer.setOnTouchListener(testServerOnClickListener);

            boolean shouldUpdateList = ((this.testServers == null) || (this.testServers.isEmpty() || testServerName.getAdapter() == null));
            if (!shouldUpdateList) {
                if (this.testServers.size() != servers.size()) {
                    shouldUpdateList = true;
                }
                if (!shouldUpdateList) {
                    for (int i = 0; i < servers.size(); i++) {
                        MeasurementServer measurementServer1 = this.testServers.get(i);
                        MeasurementServer measurementServer2 = servers.get(i);
                        if (measurementServer1.getDistance() != measurementServer2.getDistance()) {
                            shouldUpdateList = true;
                        }
                    }
                }
            }


            if (shouldUpdateList || forceUpdate) {

                Timber.e("SERVERS SHOW UPDATE");

                forceUpdate = false;

                this.testServers = servers;

                testServerName.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return isShownConsumptionWarning() || !enableMeasurementServersClick;
                    }
                });

                int selectedMeasurementServerId = ConfigHelper.getSelectedMeasurementServerId(getContext());

                int i;
                for (i = 0; i < this.testServers.size(); i++) {
                    MeasurementServer measurementServer = testServers.get(i);
                    if (measurementServer.getId() == selectedMeasurementServerId) {

                        break;
                    }
                    if (i == this.testServers.size() - 1) {
                        ConfigHelper.setSelectedMeasurementServerId(getContext(), this.testServers.get(0).getId());
                        i = 0;
                        break;
                    }
                }

                ArrayAdapter<MeasurementServer> measurementServerArrayAdapter = new MeasurementServersAdapter(MainMenuFragment.this.getContext(), R.layout.test_server_item, R.id.text, MainMenuFragment.this.testServers, getMainActivity());
                testServerName.setAdapter(measurementServerArrayAdapter);
                testServerName.setSelection(i);

                testServerName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        ConfigHelper.setSelectedMeasurementServerId(getContext(), testServers.get(i).getId());
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });
            }


        } else {
            Timber.e("SERVERS HIDE - EMPTY LIST");
            testServer.setVisibility(View.GONE);
            this.testServers = new ArrayList<>();
        }
    }

    private void resetAllIPandGetNewOne() {
        Timber.e("IP CHANGE resetAllIPandGetNewOne()");
        try {
            NetworkInfoCollector.getInstance(context.getApplicationContext()).resetAllPrivateIps();
            NetworkInfoCollector.getInstance(context.getApplicationContext()).resetAllPublicIps();
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }
        showNetworkNameAndType(InfoCollector.getInstance().getNetworkTypeString());
        refreshIpAddresses();
    }

    private void refreshIpAddresses() {
        FragmentActivity activity = getActivity();
        if (activity != null) {
            ((MainActivity) activity).getMeasurementServers(onMeasurementServersLoaded, null, true);
        }

        NetworkInfoCollector netInfo;
        netInfo = ((MainActivity) getActivity()).getNetworkInfoCollector();
        if (netInfo != null) {
            if (ConfigHelper.isIpPolling(getActivity())) {
                netInfo.gatherIpInformation(true);
            } else {
                if (NetworkInfoCollector.IP_METHOD == NetworkInfoCollector.IP_METHOD_NETWORKINTERFACE) {
                    netInfo.gatherInterfaceInformation(true);
                } else {
                    netInfo.gatherIpInformation(false);
                }
            }

            infoCollector.setHasControlServerConnection(netInfo.hasIpFromControlServer());
            infoCollector.setCaptivePortalFound(netInfo.getCaptivePortalStatus().equals(CaptivePortalStatusEnum.FOUND));
            infoCollector.refreshIpAndAntenna();
            infoCollector.dispatchInfoChangedEvent(InfoCollector.InfoCollectorType.IPV4, null, infoCollector.getIpv4());
            infoCollector.dispatchInfoChangedEvent(InfoCollector.InfoCollectorType.IPV6, null, infoCollector.getIpv6());
//            infoCollector.dispatchInfoChangedEvent(InfoCollectorType.NETWORK_NAME, null, infoCollector.getNetworkName());

            netInfo.onNetworkChange(getActivity(), null);

            if (netInfo.getCaptivePortalStatus() == CaptivePortalStatusEnum.FOUND
                    || netInfo.getCaptivePortalStatus() == CaptivePortalStatusEnum.NOT_FOUND) {
                setCaptivePortalStatus(netInfo.getCaptivePortalStatus() == CaptivePortalStatusEnum.FOUND);
            }
        }
    }

    private void showGeolocation(Location location, String geodecodecodedLocation, boolean enabledGPS) {

        if (isAdded()) {
            if (enabledGPS) {
                if (location != null) {
                    locationInfoObject = location;
                    if (geodecodecodedLocation != null && !geodecodecodedLocation.isEmpty()) {
                        locationText.setText(geodecodecodedLocation);
                        setLocationIcon(null, true);
                        locationButton.setOnClickListener(detailShowOnClickListener);
                    } else {
                        locationText.setText(getString(R.string.enabled));
                        setLocationIcon(null, true);
                        locationButton.setOnClickListener(detailShowOnClickListener);
                    }
                } else {
                    locationInfoObject = null;
                    locationText.setText(getResources().getString(R.string.searching_for_location));
                    setLocationIcon(null, true);
                    locationButton.setOnClickListener(detailShowOnClickListener);
                }
            } else {
                locationInfoObject = null;
                locationText.setText(R.string.disabled);
                setLocationIcon(null, false);
                locationView.setImageResource(R.drawable.ic_action_location_off);
                if (locationButton != null) {
                    locationButton.setOnClickListener(openLocationSettingsOnClickListener);
                }
            }
        }
    }

    private void setLocationIcon(Location location, boolean forceEnabled) {
        if (locationView != null) {
            if (locationView.getVisibility() == View.INVISIBLE) {
                locationView.setVisibility(View.VISIBLE);
            }
            locationView.setImageResource((location != null || forceEnabled) ? R.drawable.ic_action_location_found : R.drawable.ic_action_location_off);
            if (locationButton != null) {
                locationButton.setOnClickListener((location != null || forceEnabled) ? detailShowOnClickListener : openLocationSettingsOnClickListener);
            }
        }
    }

    /**
     * @param hasCaptivePortal
     */
    private void setCaptivePortalStatus(boolean hasCaptivePortal) {
        if (captivePortalWarning != null) {
            captivePortalWarning.setVisibility(hasCaptivePortal ? View.VISIBLE : View.GONE);
        }

        if (infoValueListAdapterMap != null) {
            if (infoValueListAdapterMap.get(OverlayType.IP) != null) {
                if (!hasCaptivePortal) {
                    infoValueListAdapterMap.get(OverlayType.IP).removeElement(InfoOverlayEnum.CAPTIVE_PORTAL_STATUS);
                } else {
                    infoValueListAdapterMap.get(OverlayType.IP).addElement(InfoOverlayEnum.CAPTIVE_PORTAL_STATUS);
                }
            }
        }

    }

    private void showDialogToOpenGPSSettings(String dialogText) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getMainActivity());
        final String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;
        final String message = dialogText;

        builder.setMessage(message)
                .setPositiveButton(R.string._ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                getMainActivity().startActivity(new Intent(action));
                                d.dismiss();
                            }
                        })
                .setNegativeButton(R.string._cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface d, int id) {
                                d.cancel();
                            }
                        });
        builder.create().show();
    }

    private boolean hideConsumptionWarning() {
        if (isShownConsumptionWarning()) {
            increasedConsumptionText.setVisibility(View.GONE);
            return true;
        }
        return false;
    }

    private boolean isShownConsumptionWarning() {
        return ((increasedConsumptionText != null) && (increasedConsumptionText.getVisibility() == View.VISIBLE));
    }

    private void hideOverlayAndReenableOnClickListeners() {
        infoOverlay.setVisibility(View.GONE);
        if (ipButton != null) {
            ipButton.setOnClickListener(detailShowOnClickListener);
        }
        if (cpuMemStatsButton != null) {
            cpuMemStatsButton.setOnClickListener(detailShowOnClickListener);
        }
        trafficButton.setOnClickListener(detailShowOnClickListener);
        locationButton.setOnClickListener(locationInfoObject != null ? detailShowOnClickListener : openLocationSettingsOnClickListener);
        locationText.setOnClickListener(locationInfoObject != null ? detailShowOnClickListener : openLocationSettingsOnClickListener);
        locationView.setOnClickListener(locationInfoObject != null ? detailShowOnClickListener : openLocationSettingsOnClickListener);
        locationTitle.setOnClickListener(locationInfoObject != null ? detailShowOnClickListener : openLocationSettingsOnClickListener);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(BUNDLE_TEST_UUID, testUuid);
        if (antennaView != null && antennaView.getTag() != null) {
            outState.putInt(BUNDLE_INFO_LAST_ANTENNA_IMAGE, (Integer) antennaView.getTag());
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    /**
     * @return
     */
    public InformationCollector getInformationCollector() {
        return informationCollector;
    }

    public void setViewVisibility(View view, int visibility) {
        if (view != null) {
            view.setVisibility(visibility);
        }
    }

    public void setViewText(TextView view, String text) {
        setViewText(view, text, null);
    }

    public void setViewTextColorResource(TextView view, int colorResId) {
        if (view != null) {
            FragmentActivity activity = getActivity();
            if (activity != null) {
                view.setTextColor(activity.getResources().getColor(colorResId));
            }
        }
    }

    public void setViewText(TextView view, String text, TextView alternativeView) {
        if (view != null) {
            view.setText(text);
        } else if (alternativeView != null) {
            alternativeView.setText(alternativeView.getText() + " " + text);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mainFragmentController.dismissDialogs();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (getActivity() != null) {
            mainFragmentController.onDestroy();
        }
        System.gc();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mainFragmentController != null) {
            mainFragmentController.dismissDialogs();
        }
    }

    public boolean onBackPressed() {
        if (hideConsumptionWarning()) return true;
        switch (screenState) {
            case DEFAULT:
                buttonsDisabled = false;
                int screenSize = getResources().getConfiguration().screenLayout &
                        Configuration.SCREENLAYOUT_SIZE_MASK;

                if (infoOverlay != null && infoOverlay.getVisibility() == View.VISIBLE && screenSize < Configuration.SCREENLAYOUT_SIZE_LARGE) {
                    infoOverlay.setVisibility(View.GONE);
                    return true;
                }
                return false;
            case TESTING:
            case LOOP_MODE_ACTIVE:
                TestService testTestService = mainFragmentController.getTestTestService();
                if (testTestService == null || !testTestService.isTestRunning())
                    return false;
                return onBackPressedHandler();
            default:
                break;
        }
        return false;
    }

    public boolean onBackPressedHandler() {
        if (mainFragmentController != null) {
            return mainFragmentController.onMainBackPressed();
        }
        return false;
    }


    @Override
    public boolean isTestVisible() {
        return (screenState == MainScreenState.TESTING || screenState == MainScreenState.QOS_TESTING || screenState == MainScreenState.LOOP_MODE_ACTIVE);
    }

    @Override
    public void setPacketLossText(String packetLossText) {
        testPacketLossProgressValue.setText(packetLossText);
    }

    @Override
    public void setJitterText(String jitterText) {
        Timber.d("TUUI: prepare to set jitter: " + jitterText + "  visible: " + (testJitterProgressValue.getVisibility() == View.VISIBLE));
        testJitterProgressValue.setText(jitterText);
        Timber.d("TUUI: jitter: " + jitterText + "  visible: " + (testJitterProgressValue.getVisibility() == View.VISIBLE));
    }

    public void setSignalValue(Integer signal) {
        if (isAdded()) {
            if (signal != null) {
                if (testSignalProgressValue != null) {
                    testSignalProgressValue.setVisibility(View.VISIBLE);
                    testSignalProgressValue.setText(String.valueOf(signal) + " " + getString(R.string.test_dbm));
                } else {
                    testSignalProgressValue.setVisibility(View.VISIBLE);
                    testSignalProgressValue.setText("- " + getString(R.string.test_dbm));
                }
                if (testSignalProgressTitle != null) {
                    testSignalProgressTitle.setVisibility(View.VISIBLE);
                }
            } else {
                testSignalProgressTitle.setVisibility(View.VISIBLE);
                testSignalProgressValue.setVisibility(View.VISIBLE);
                testSignalProgressValue.setText("- " + getString(R.string.test_dbm));
            }
        }

    }

    @Override
    public void showQosResults(int testsCount, int testsFailed, int successPercentage) {
        testViewQoSResultFailed.setText(String.valueOf(testsFailed));
        testViewQoSResultPassed.setText(String.valueOf(testsCount - testsFailed));
        testViewQoSResultPerformed.setText(String.valueOf(testsCount));
        if (isAdded()) {
            testViewQoSResultPercentage.setText(getString(R.string.result_page_title_qos) + ": " + successPercentage + "%");
        }
    }

    @Override
    public void setPingText(String pingStr) {
        testPingProgressValue.setText(pingStr);
    }

    @Override
    public void setQosTestProgress(int i, boolean hidePointer) {
        testViewLower.setValueAnimated(i);
        if (hidePointer) {
            testViewLower.setShowArrow(false);
        }
    }

    @Override
    public void setTestProgress(int i) {
        testViewUpper.setValue(i);
    }

    @Override
    public void setTestTextProgress(String i) {
        testTextViewUpper.setText(i);
    }

    @Override
    public void updateDownloadGraph(String value, int downloadStatusStringID, int unitStringID) {
        if (testDownloadGraphValue != null) {
            try {
                float v = Float.parseFloat(value);
                Float aFloat = addToFifoandGetMedian(v, downloadFifo);
                value = SpeedTestStatViewController.InfoStat.DOWNLOAD.format((long) (aFloat * SpeedTestStatViewController.InfoStat.DOWNLOAD.getRoundingValue()));
            } catch (NumberFormatException ignored) {
                // do nothing
            }
            testDownloadGraphValue.setText(value.split(" ")[0]);
        }
        if (testDownloadGraphTitle != null) {
            testDownloadGraphTitle.setText(downloadStatusStringID);
        }
        if (testDownloadGraphUnits != null) {
            testDownloadGraphUnits.setText(unitStringID);
        }
    }

    private Float addToFifoandGetMedian(float v, ArrayList<Float> fifoArray) {
        int bufferSize = 10;
        fifoArray.add(v);
        if (fifoArray.size() > 10) {
            fifoArray.remove(0);
        }
        float valuesAdded = 0;
        for (int i = 0; i < fifoArray.size(); i++) {
            valuesAdded += fifoArray.get(i);
        }
        Float result = 0F;
        if (fifoArray.size() > 0)
            result = valuesAdded / fifoArray.size();
        return result;
    }

    @Override
    public void updateUploadGraph(String value, int uploadStatusStringID, int unitStringID) {
        if (testUploadGraphValue != null) {
            try {
                float v = Float.parseFloat(value);
                Float aFloat = addToFifoandGetMedian(v, uploadFifo);
                value = SpeedTestStatViewController.InfoStat.UPLOAD.format((long) (aFloat * SpeedTestStatViewController.InfoStat.UPLOAD.getRoundingValue()));
            } catch (NumberFormatException ignored) {
                // do nothing
            }
            testUploadGraphValue.setText(value.split(" ")[0]);
        }
        if (testUploadGraphTitle != null) {
            testUploadGraphTitle.setText(uploadStatusStringID);
        }
        if (testUploadGraphUnits != null) {
            testUploadGraphUnits.setText(unitStringID);
        }
    }

    @Override
    public int getUnitStringId() {
        return R.string.test_mbps;
    }

    @Override
    public int getUploadStatusStringId() {
        return R.string.test_bottom_test_status_up;
    }

    @Override
    public int getDownloadStatusStringId() {
        return R.string.test_bottom_test_status_down;
    }

    @Override
    public void showQoSProgress(TestService testTestService) {
        if (testQosProgressView != null && testGroupCountContainerView != null && testGroupCountContainerView.getChildCount() == 0
                && testTestService != null && testTestService.getQoSTest() != null) {
            final GroupCountView groupCountView = new GroupCountView(getActivity());
            testQosProgressView.setVisibility(View.VISIBLE);
            //register group counter view as a test progress listener:
            testTestService.getQoSTest().getTestSettings().addTestProgressListener(groupCountView);
            groupCountView.setTaskMap(testTestService.getQoSTest().getTestMap());
            testGroupCountContainerView.addView(groupCountView);
            testGroupCountContainerView.invalidate();
            ((GroupCountView) testGroupCountContainerView.getChildAt(0)).setNdtProgress(testTestService.getNDTProgress());
        } else if (testQosProgressView != null
                && testQosProgressView.getVisibility() == View.VISIBLE && testTestService.getQoSGroupCounterMap() != null) {
            ((GroupCountView) testGroupCountContainerView.getChildAt(0)).setTaskMap(testTestService.getQoSTest().getTestMap());
            ((GroupCountView) testGroupCountContainerView.getChildAt(0)).setNdtProgress(testTestService.getNDTProgress());
            ((GroupCountView) testGroupCountContainerView.getChildAt(0)).setQoSTestStatus(testTestService.getQoSTestStatus());
            ((GroupCountView) testGroupCountContainerView.getChildAt(0)).updateView(testTestService.getQoSGroupCounterMap());
        }
    }

    @Override
    public void setTestUUID(String testUUID) {
        this.testUuid = testUUID;
    }

    @Override
    public String getTestUUID() {
        return this.testUuid;
    }

    @Override
    public int getTestErrorQosStringId() {
        return R.string.test_toast_error_text_qos;
    }

    @Override
    public int getTestErrorStringId() {
        return R.string.test_dialog_error_text;
    }

    @Override
    public int getProgressTitleId() {
        return R.string.test_progress_title;
    }

    @Override
    public int getProgressTextId() {
        return R.string.test_progress_text;
    }

    @Override
    public int getAbortDialogTitleId() {
        return R.string.test_dialog_abort_title;
    }

    @Override
    public int getAbortDialogTextId() {
        return R.string.test_dialog_abort_text;
    }

    @Override
    public int getAbortDialogPositiveButtonText() {
        return R.string.test_dialog_abort_yes;
    }

    @Override
    public int getAbortDialogNegativeButtonText() {
        return R.string.test_dialog_abort_no;
    }

    @Override
    public int getTestErrorTitleId() {
        return R.string.test_dialog_error_title;
    }

    @Override
    public int getErrorControlServerConnectionStringId() {
        return R.string.test_dialog_error_control_server_conn;
    }

    @Override
    public void setSpeedGaugeProgress(int i) {
        testViewLower.setValueAnimated(i);
    }


    @Override
    public String setActionBarTitle() {
        return "";
    }

    @Override
    public void onLocationChange(Location location, String decodedLocation, boolean enabledGPS) {
        showGeolocation(location, decodedLocation, enabledGPS);
    }
}
