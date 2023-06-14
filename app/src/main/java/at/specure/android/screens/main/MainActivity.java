/*
 Copyright 2015 SPECURE GmbH

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package at.specure.android.screens.main;

import android.Manifest;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.JsonArray;
import com.mapbox.mapboxsdk.Mapbox;
import com.specure.opennettest.BuildConfig;
import com.specure.opennettest.R;
import com.stepstone.apprating.listener.RatingDialogListener;

import java.io.File;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.loader.content.Loader;

import javax.inject.Inject;

import at.specure.android.api.calls.CheckHistoryTask;
import at.specure.android.api.calls.CheckNewsTask;
import at.specure.android.api.calls.CheckSettingsTask;
import at.specure.android.api.calls.GetMeasurementServersTask;
import at.specure.android.api.calls.LogTask;
import at.specure.android.api.calls.RegistrationTask;
import at.specure.android.api.calls.SendZeroMeasurementsTask;
import at.specure.android.api.jsons.MeasurementServer;
import at.specure.android.base.BaseFragment;
import at.specure.android.configs.Config;
import at.specure.android.configs.ConfigHelper;
import at.specure.android.configs.FeatureConfig;
import at.specure.android.configs.LocaleConfig;
import at.specure.android.configs.LoopModeConfig;
import at.specure.android.configs.MapConfig;
import at.specure.android.configs.PermissionHandler;
import at.specure.android.configs.PreferenceConfig;
import at.specure.android.configs.PrivacyConfig;
import at.specure.android.configs.SurveyConfig;
import at.specure.android.configs.TermsAndConditionsConfig;
import at.specure.android.configs.TestConfig;
import at.specure.android.constants.AppConstants;
import at.specure.android.screens.about.AboutFragment;
import at.specure.android.screens.badges.newbadges.BadgeListActivity;
import at.specure.android.screens.help.HelpFragment;
import at.specure.android.screens.history.HistoryFilterFragment;
import at.specure.android.screens.history.HistoryFragment;
import at.specure.android.screens.main.main_activity_interfaces.ExpandedResultInterface;
import at.specure.android.screens.main.main_activity_interfaces.HelpInterface;
import at.specure.android.screens.main.main_activity_interfaces.MapInterface;
import at.specure.android.screens.main.main_fragment.MainMenuFragment;
import at.specure.android.screens.main.main_fragment.MainScreenState;
import at.specure.android.screens.map.MapBoxFragment;
import at.specure.android.screens.map.MapProperties;
import at.specure.android.screens.preferences.PreferenceActivity;
import at.specure.android.screens.result.adapter.result.OnCompleteListener;
import at.specure.android.screens.result.fragments.SimpleResultFragment;
import at.specure.android.screens.result.fragments.main_result_pager.ResultPagerController;
import at.specure.android.screens.result.fragments.main_result_pager.ResultPagerFragment;
import at.specure.android.screens.result.fragments.qos_category.QoSCategoryPagerFragment;
import at.specure.android.screens.result.fragments.qos_detail.QoSTestDetailPagerFragment;
import at.specure.android.screens.sync.sync.SyncFragment;
import at.specure.android.screens.terms.CheckType;
import at.specure.android.screens.terms.check_fragment.CheckFragment;
import at.specure.android.screens.terms.terms_check.TermsCheckFragment;
import at.specure.android.test.TestService;
import at.specure.android.util.AppRater;
import at.specure.android.util.DebugPrintStream;
import at.specure.android.util.EndBooleanTaskListener;
import at.specure.android.util.EndTaskListener;
import at.specure.android.util.Helperfunctions;
import at.specure.android.util.MeasurementTaskEndedListener;
import at.specure.android.util.connectivity.RequestReadPhoneStatePermissionInterface;
import at.specure.android.util.location.GeoLocationX;
import at.specure.android.util.location.RequestBackgroundLocationPermissionInterface;
import at.specure.android.util.location.RequestGPSPermissionInterface;
import at.specure.android.util.net.NetworkInfoCollector;
import at.specure.android.util.network.network.ActiveNetworkLiveData;
import at.specure.androidX.Application;
import at.specure.androidX.data.badges.BadgesViewModel;
import at.specure.androidX.data.history.HistoryItem;
import at.specure.androidX.data.history.HistoryLoader;
import at.specure.androidX.data.map_filter.mappers.MapFilterSaver;
import at.specure.androidX.data.map_filter.view_data.FilterGroup;
import at.specure.androidX.data.map_filter.view_data.FilterLiveData;
import at.specure.androidX.data.map_filter.view_data.FilterViewModel;
import at.specure.androidX.test.LoopModeExecutorInterface;
import at.specure.client.v2.task.result.QoSServerResult;
import at.specure.client.v2.task.result.QoSServerResult.DetailType;
import at.specure.client.v2.task.result.QoSServerResultCollection;
import at.specure.client.v2.task.result.QoSServerResultDesc;
import at.specure.info.strength.SignalStrengthLiveData;
import at.specure.info.strength.SignalStrengthWatcher;
import timber.log.Timber;

import static at.specure.android.configs.FeatureConfig.LAYOUT_SQUARE;
import static at.specure.androidX.loaders.LoaderEnumerator.HISTORY_LOADER_ID;

public class MainActivity extends BasicActivity
        implements
        MapProperties,
        DrawerActionListener,
        MapInterface,
        HelpInterface,
        ExpandedResultInterface,
        RatingDialogListener,
        RequestGPSPermissionInterface,
        SimpleResultFragment.OnFragmentInteractionListener,
        LoopModeExecutorInterface, RequestReadPhoneStatePermissionInterface, RequestBackgroundLocationPermissionInterface {

    private final static int PERM_REQ_LOC_COARSE_TEST = 0;
    private final static int PERM_REQ_LOC_FINE_TEST = 1;
    private final static int PERM_REQ_LOC_COARSE_START = 2;
    private final static int PERM_REQ_LOC_FINE_START = 3;
    private final static int PERM_REQ_READ_PHONE_STATE_TEST = 4;
    private final static int PERM_REQ_READ_PHONE_STATE_START = 5;
    private final static int PERM_REQ_BACKGROUND_LOCATION_ACCESS = 6;

    private final static boolean VIEW_HIERARCHY_SERVER_ENABLED = false;
    private static final String DEBUG_TAG = "MainActivity";
    List<HistoryItem> historyItems = new ArrayList<>();
    private final HashMap<String, String> currentMapOptions = new HashMap<>();
    private CheckNewsTask newsTask;
    private CheckSettingsTask settingsTask;
    private GetMeasurementServersTask measurementServersTask;
    private SendZeroMeasurementsTask sendZeroMeasurementsTask;
    private CheckHistoryTask historyTask;
    private String historyFilterDevices[];
    private String historyFilterNetworks[];
    private ArrayList<String> historyFilterDevicesFilter;
    private ArrayList<String> historyFilterNetworksFilter;
    private int historyResultLimit;
    at.specure.android.screens.map.MapInterface mapInterface;
    private IntentFilter mNetworkStateChangedFilter;
    private BroadcastReceiver mNetworkStateIntentReceiver;
    private boolean mapTypeSatellite;
    private boolean historyDirty = true;
    private ProgressDialog loadingDialog;
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private ActionBarDrawerToggle drawerToggle;
    private boolean exitAfterDrawerClose = false;
    private Menu actionBarMenu;
    private NetworkInfoCollector networkInfoCollector;
    private Toolbar toolbar;
    private List<MeasurementServer> measurementsServers = new ArrayList<>();
    private boolean isPausing;
    private InitialSetupInterface initialSetupInterface;
    private Boolean isTesting = false;
    private boolean firstTimeRun;
    private FirebaseAnalytics mFirebaseAnalytics;
    private FragmentManager fm;
    private LiveData<List<FilterGroup>> filterLiveData;
    private Observer<? super List<FilterGroup>> filterDataObserver = new Observer<List<FilterGroup>>() {
        @Override
        public void onChanged(List<FilterGroup> groups) {
            initializeMapBox();
        }
    };

    @Inject
    ActiveNetworkLiveData activeNetworkLiveData;

    @Inject
    SignalStrengthLiveData signalStrengthLiveData;


    private void initializeMapBox() {
//        BuildConfig.MAPBOX_APIKEY;
        Mapbox.getInstance(this, MapFilterSaver.getChosenMapLayoutAccessToken(this));
    }

    private void preferencesUpdate() {
        final SharedPreferences preferences = PreferenceConfig.getPreferenceSharedPreferences(getApplicationContext());

        //remove control server version on start
        ConfigHelper.setControlServerVersion(this, null);

        final Context context = getApplicationContext();
        final PackageInfo pInfo;
        final int clientVersion;
        try {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            clientVersion = pInfo.versionCode;

            final int lastVersion = preferences.getInt("LAST_VERSION_CODE", -1);
            if (lastVersion == -1) {
                preferences.edit().apply();
                Timber.d(DEBUG_TAG + " preferences cleared");
            }

            if (lastVersion != clientVersion)
                preferences.edit().putInt("LAST_VERSION_CODE", clientVersion).apply();
        } catch (final NameNotFoundException e) {
            Timber.e(e, DEBUG_TAG + "version of the application cannot be found");
        }
    }


    @Override
    public void onCreate(final Bundle savedInstanceState) {

//        PrivacyConfig.updateSettings(this);

        if (PrivacyConfig.isAnalyticsPermitted(this.getApplicationContext())) {
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        }

        if (savedInstanceState == null) {
            firstTimeRun = true;
        }

        if (mFirebaseAnalytics != null) {
            mFirebaseAnalytics.setUserId(ConfigHelper.getUUID(this.getApplicationContext()));
            String isoLanguage = "";
            try {
                isoLanguage = Locale.getDefault().getISO3Language();
            } catch (MissingResourceException e) {
                Timber.e("MISSING ISO3 language %s", Locale.getDefault().toString());
            }

            mFirebaseAnalytics.setUserProperty("Language", isoLanguage);
            Bundle bundle = new Bundle();
            bundle.putString("Language3", isoLanguage);
            bundle.putString("Language2", Locale.getDefault().getLanguage());
            mFirebaseAnalytics.logEvent("AppStartLocale", bundle);
            if ((isoLanguage != null) && (!isoLanguage.isEmpty())) {
                mFirebaseAnalytics.logEvent(isoLanguage, bundle);
            }
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, bundle);
        }

        FilterViewModel filterViewModel = ViewModelProviders.of(this).get(FilterViewModel.class);
        filterLiveData = filterViewModel.getData();
        filterLiveData.observe(this, filterDataObserver);
        List<FilterGroup> value = filterLiveData.getValue();
        if (value != null) {
            initializeMapBox();
        } else {
            Mapbox.getInstance(this, BuildConfig.MAPBOX_APIKEY);
        }

        if (checkForGPSPermissions(PERM_REQ_LOC_COARSE_START, PERM_REQ_LOC_FINE_START)) {
            checkForReadPhoneStatePermissions(PERM_REQ_READ_PHONE_STATE_START);
        }

        restoreInstance(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        NetworkInfoCollector.init(this);
        networkInfoCollector = NetworkInfoCollector.getInstance(this.getApplicationContext());

        preferencesUpdate();
        setContentView(R.layout.main_with_navigation_drawer);

        if (VIEW_HIERARCHY_SERVER_ENABLED) {
            ViewServer.get(this).addWindow(this);
        }

        toolbar = findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);

        // initialize the navigation drawer with the main menu list adapter:
        MainMenuListAdapter mainMenuAdapter = new MainMenuListAdapter(this,
                MainMenuUtil.getMenuTitles(getResources(), this), MainMenuUtil.getMenuIds(this));

        drawerLayout = findViewById(R.id.drawer_layout);
        drawerList = findViewById(R.id.left_drawer);

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.page_title_title_page, R.string.page_title_title_page) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                //refreshActionBar(null);
                exitAfterDrawerClose = false;
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        drawerLayout.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((KeyEvent.KEYCODE_BACK == event.getKeyCode() && exitAfterDrawerClose) && ((initialSetupInterface != null) && !(initialSetupInterface instanceof TermsCheckFragment))) {
                    onBackPressed();
                    return true;
                }
                return false;
            }
        });
        drawerLayout.addDrawerListener(drawerToggle);
        drawerList.setAdapter(mainMenuAdapter);
        drawerList.setOnItemClickListener(new OnItemClickListener() {
            final List<Integer> menuIds = MainMenuUtil.getMenuActionIds(MainActivity.this);

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                selectMenuItem(menuIds.get(position));
                drawerLayout.closeDrawers();
            }
        });

        // Do something against banding effect in gradients
        final Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);

        // Setzt Default-Werte, wenn noch keine Werte vorhanden
//        if (ConfigHelper.isSecretEntered(this)) {
//            PreferenceManager.setDefaultValues(this, R.xml.preferences_after_secret, false);
//        } else {
//            PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
//        }

        final String uuid = ConfigHelper.getUUID(getApplicationContext());

        fm = getSupportFragmentManager();
        final Fragment fragment = fm.findFragmentById(R.id.fragment_content);
        if (!ConfigHelper.isTCAccepted(this)) {
            if (fragment != null && fm.getBackStackEntryCount() >= 1)
                // clear fragment back stack
                fm.popBackStack(fm.getBackStackEntryAt(0).getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);

            setLockNavigationDrawer(true);

            if (TermsAndConditionsConfig.shouldShowPrivacyPolicy(this)) {
                showTermsCheck(CheckType.TERMS_AND_PRIVACY);
            } else {
                showTermsCheck(null);
            }

        } else {
            currentMapOptions.put("highlight", uuid);
            if (fragment == null) {
                //noinspection ConstantConditions,ConstantIfStatement
                if (false) // deactivated for si // ! ConfigHelper.isNDTDecisionMade(this))
                {
                    if (TermsAndConditionsConfig.shouldShowPrivacyPolicy(this)) {
                        showTermsCheck(CheckType.TERMS_AND_PRIVACY);
                    } else {
                        showTermsCheck(null);
                    }
                    showNdtCheck();
                } else
                    initApp(true);
            }
        }

        mNetworkStateChangedFilter = new IntentFilter();
        mNetworkStateChangedFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        mNetworkStateIntentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                if (intent.getAction() != null && intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                    final boolean connected = !intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
                    final boolean isFailOver = intent.getBooleanExtra(ConnectivityManager.EXTRA_IS_FAILOVER, false);

                    if (connected) {
                        if (networkInfoCollector != null) {
                            networkInfoCollector.setHasConnectionFromAndroidApi(true);
                        }
                    } else {
                        if (networkInfoCollector != null) {
                            networkInfoCollector.setHasConnectionFromAndroidApi(false);
                        }
                    }

                    Timber.i(" %s  CONNECTED:  %s  FAILOVER:  %s", DEBUG_TAG, connected, isFailOver);
                }
            }
        };
//        BadgesConfig.setAllBadgeReceived(this);
    }

    private void resetMapFilter() {
        String s = FeatureConfig.countrySpecificOperatorsCountryCode(this);
        if (s.isEmpty()) {
            ConfigHelper.setSelectedCountryInMapFilter("all", this);
        } else {
            ConfigHelper.setSelectedCountryInMapFilter(s, this);
        }
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        ViewServer.get(this).removeWindow(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_menu, menu);
        this.actionBarMenu = menu;

        Timber.e("MActivity onCreateOptionsMenu: CREATED");

        for (int i = 0; i < menu.size(); i++) {
            Drawable drawable = menu.getItem(i).getIcon();
            if (drawable != null) {
                drawable.mutate();
                drawable.setColorFilter(Helperfunctions.getColor(R.color.toolbar_icon_overlay, getApplicationContext()), PorterDuff.Mode.SRC_IN);
            }
        }

        Timber.e("MENU setvisibleMenuItems MainActivity");
        setVisibleMenuItems();

        Fragment currentFragment = getCurrentFragment();
        if (currentFragment != null) {
            if (currentFragment instanceof InitialSetupInterface) {
                ((InitialSetupInterface) currentFragment).setActionBarItems(this);

            }
        }

        if (initialSetupInterface != null) {
            initialSetupInterface.setActionBarItems(this);

            if (initialSetupInterface instanceof TermsCheckFragment) {
                setLockNavigationDrawer(true);
                setToolbarVisible(View.GONE);
            }
        }

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            //show/hide navigation drawer if home button (on the actionbar) was pressed
            if (drawerLayout.isDrawerOpen(drawerList)) {
                drawerLayout.closeDrawer(drawerList);
            } else {
                drawerLayout.openDrawer(drawerList);
            }
        } else {
            selectMenuItem(item.getItemId());
        }
        return true;
    }

    public void selectMenuItem(int id) {
        if (id != R.id.action_settings && id != R.id.action_title_page && id != R.id.action_info) {
            if (networkInfoCollector != null) {
                if (!networkInfoCollector.hasConnectionFromAndroidApi()) {
                    showNoNetworkConnectionToast();
                    return;
                }
            }
        }
        switch (id) {
            case R.id.action_title_page:
                popBackStackFull();
                break;
            case R.id.action_help:
                showHelp(R.string.url_help, true);
                break;
            case R.id.action_history:
                showHistory(false);
                break;
            case R.id.action_info:
                showAbout();
                break;
            case R.id.action_map:
                showMap(true);
                break;
            case R.id.action_settings:
                showSettings();
                break;
            case R.id.action_menu_filter:
                showFilter();
                break;
            case R.id.action_stats:
                showStatistics();
                break;
            case R.id.action_menu_sync:
                showSync();
                break;
            case R.id.action_menu_help:
                showHelp(false);
                break;
            case R.id.action_menu_share:
                showShareResultsIntent();
                break;
            case R.id.action_menu_rtr:
                showRtrWebPage();
                break;
            case R.id.action_menu_map:
                showMapFromPager();
                break;
            case R.id.action_badges:
                showBadges();
                break;
            case R.id.action_survey:
                showSurvey();
                break;

//            case R.id.action_menu_map_info:
//                if (mapInterface != null) {
//                    mapInterface.openInfo();
//                }
//                break;

            case R.id.action_menu_my_position:
                if (mapInterface != null) {
                    mapInterface.centerToMyPosition();
                }
                break;

            case R.id.action_menu_map_filter:
                if (mapInterface != null) {
                    mapInterface.openMapFilter();
                }
                break;

            case R.id.action_menu_map_settings:
                if (mapInterface != null) {
                    mapInterface.openMapSettings();
                }
                break;
//            case R.id.action_netstat:
//                showNetStatFragment();
//                break;
//            case R.id.action_log:
//                showLogFragment();
//                break;

        }
    }

    private void showSurvey() {
        SurveyConfig.openSurveyPage(this);
    }


    @SuppressWarnings({"unchecked", "ConstantConditions"})
    protected void restoreInstance(Bundle b) {
        if (b != null) {
//            historyFilterDevices = (String[]) b.getSerializable("historyFilterDevices");
//            historyFilterNetworks = (String[]) b.getSerializable("historyFilterNetworks");
//            historyFilterDevicesFilter = (ArrayList<String>) b.getSerializable("historyFilterDevicesFilter");
//            historyFilterNetworksFilter = (ArrayList<String>) b.getSerializable("historyFilterNetworksFilter");
//            historyItemList.clear();
//            historyItemList.addAll(b.getSerializable("historyItemList") != null ? (ArrayList<Map<String, String>>) b.getSerializable("historyItemList") : null);
//            historyStorageList.clear();
//            historyStorageList.addAll(b.getSerializable("historyStorageList") != null ? (ArrayList<Map<String, String>>) b.getSerializable("historyStorageList") : null);
            historyResultLimit = b.getInt("historyResultLimit");
//            currentMapOptions.clear();
//            currentMapOptions.putAll(b.getSerializable("currentMapOptions") != null ? (HashMap<String, String>) b.getSerializable("currentMapOptions") : null);
//            currentMapOptionTitles = (HashMap<String, String>) b.getSerializable("currentMapOptionTitles");
//            mapTypeListSectionList = (ArrayList<MapListSection>) b.getSerializable("mapTypeListSectionList");
//            mapFilterListSectionListMap = (HashMap<String, List<MapListSection>>) b.getSerializable("mapFilterListSectionListMap");
//            currentMapType = (MapListEntry) b.getSerializable("currentMapType");
//            mapFilterCountries = new MapFilterCountries();
//            mapFilterCountries.countries = (ArrayList<MapFilterCountry>) b.getSerializable("mapFilterCountriesList");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle b) {
        super.onSaveInstanceState(b);
        isPausing = true;
//        b.putSerializable("historyFilterDevices", historyFilterDevices);
//        b.putSerializable("historyFilterNetworks", historyFilterNetworks);
//        b.putSerializable("historyFilterDevicesFilter", historyFilterDevicesFilter);
//        b.putSerializable("historyFilterNetworksFilter", historyFilterNetworksFilter);
//        b.putSerializable("historyItemList", historyItemList);
//        b.putSerializable("historyStorageList", historyStorageList);
        b.putInt("historyResultLimit", historyResultLimit);
//        b.putSerializable("currentMapOptions", currentMapOptions);
//        b.putSerializable("currentMapOptionTitles", currentMapOptionTitles);
//        b.putSerializable("mapTypeListSectionList", mapTypeListSectionList);
//        b.putSerializable("mapFilterListSectionListMap", mapFilterListSectionListMap);
//        if (mapFilterCountries != null) {
//            b.putSerializable("mapFilterCountriesList", mapFilterCountries.countries);
//        }
//        b.putSerializable("currentMapType", currentMapType);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (LocaleConfig.isUserAbleToChangeLanguage(this)) {
            if (LocaleConfig.isLanguageChanged(this)) {
                LocaleConfig.setLanguageChangedDone(this);
                LocaleConfig.initializeApp(this, true);
            }
        }

        setSendZeroMeasurements();

        isPausing = false;
        ViewServer.get(this).setFocusedWindow(this);
//        refreshMenuItems();

        BaseFragment currentFragment = (BaseFragment) getCurrentFragment();
        if (currentFragment != null) {
            if (currentFragment instanceof MapBoxFragment) {
                MapBoxFragment fragment = (MapBoxFragment) currentFragment;
                fragment.setActionBarItems(this);
            }
//            currentFragment.setActionBarItems(this);
        }
//        if (currentFragment instanceof MapBoxFragment) {
//            showMap(true);
//        }
//        startTestingService(TestService.ACTION_START_SERVICE);

    }

    @Override
    public boolean startLoopMode() {
        MainScreenState mainScreenState = preStartTest(MainScreenState.DEFAULT);
        if (mainScreenState == MainScreenState.LOOP_MODE_ACTIVE) {
            return true;
        }
        return false;
    }

    @Override
    public boolean startTest() {
        MainScreenState mainScreenState = preStartTest(MainScreenState.DEFAULT);
        if (mainScreenState == MainScreenState.TESTING) {
            return true;
        }
        return false;
    }

    public MainScreenState preStartTest(MainScreenState state) {
        return preRunTest(state);
    }

    @Override
    public void stopLoopMode() {
        final Intent stopIntent = new Intent(TestService.ACTION_STOP_LOOP, null, this, TestService.class);
        this.startService(stopIntent);
    }

    @Override
    public void stopTest() {
        final Intent stopIntent = new Intent(TestService.ACTION_ABORT_TEST, null, this, TestService.class);
        this.startService(stopIntent);
    }

    private void refreshMenuItems() {
        invalidateOptionsMenu();
        supportInvalidateOptionsMenu();
        MainMenuListAdapter mainMenuAdapter = new MainMenuListAdapter(this,
                MainMenuUtil.getMenuTitles(getResources(), this), MainMenuUtil.getMenuIds(this));

        drawerList = findViewById(R.id.left_drawer);
        if (drawerList != null) {
            drawerList.setAdapter(mainMenuAdapter);
            drawerList.setOnItemClickListener(new OnItemClickListener() {
                final List<Integer> menuIds = MainMenuUtil.getMenuActionIds(MainActivity.this);

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    selectMenuItem(menuIds.get(position));
                    drawerLayout.closeDrawers();
                }
            });
        }
    }

    @Override
    public void onStart() {
        isPausing = false;
        Timber.i(" %s onStart", DEBUG_TAG);
        super.onStart();

        registerReceiver(mNetworkStateIntentReceiver, mNetworkStateChangedFilter);
        // init location Manager

        if (ConfigHelper.isTCAccepted(this) && ConfigHelper.isNDTDecisionMade(this)) {
            boolean permissionGranted = checkForGPSPermissions(PERM_REQ_LOC_COARSE_START, PERM_REQ_LOC_FINE_START);
            if (permissionGranted) {
                checkForReadPhoneStatePermissions(PERM_REQ_READ_PHONE_STATE_START);
                GeoLocationX.getInstance(this.getApplication()).getLastKnownLocation(this, null);
            }
        }
    }

    @Override
    public void onStop() {
        Timber.i("%s onStop", DEBUG_TAG);
        super.onStop();
        stopBackgroundProcesses();
        unregisterReceiver(mNetworkStateIntentReceiver);
    }

    private void checkNews() {
        newsTask = new CheckNewsTask(this);
        newsTask.execute();
    }

    private void checkLogs(final OnCompleteListener listener) {
        if (ConfigHelper.DEFAULT_SEND_LOG_TO_CONTROL_SERVER) {
            final LogTask logTask = new LogTask(this, listener);
            logTask.execute();
        }
    }

    public boolean haveUuid() {
        final String uuid = ConfigHelper.getUUID(getApplicationContext());
        return (uuid != null && uuid.length() > 0);
    }

    public boolean haveHistoryFilters() {
        return (historyFilterDevices != null && historyFilterNetworks != null);
    }

    @SuppressWarnings("SameParameterValue")
    public void checkSettings(boolean force, final EndTaskListener endTaskListener) {
        if (settingsTask != null && settingsTask.getStatus() == AsyncTask.Status.RUNNING)
            return;

        if (!force && haveUuid() && haveHistoryFilters() && ConfigHelper.shouldUpdateUUID(this))
            return;

        settingsTask = new CheckSettingsTask(this);
        settingsTask.setEndTaskListener(new EndTaskListener() {
            @Override
            public void taskEnded(JsonArray result) {
                if (loadingDialog != null)
                    loadingDialog.dismiss();
                if (endTaskListener != null)
                    endTaskListener.taskEnded(result);
            }
        });
        settingsTask.execute();
    }


    public void setSendZeroMeasurements() {
        if ((this != null) && (ConfigHelper.detectZeroMeasurementEnabled(this))) {
            if (sendZeroMeasurementsTask != null && sendZeroMeasurementsTask.getStatus() == AsyncTask.Status.RUNNING)
                return;

            sendZeroMeasurementsTask = new SendZeroMeasurementsTask(this);
            sendZeroMeasurementsTask.setOnCompleteListener(new EndBooleanTaskListener() {
                @Override
                public void taskEnded(boolean result) {
                    Timber.e("ZERO sent:  %s", result);
                    // If something want to displayed after un/successful sending on the main thread
                }
            });
            sendZeroMeasurementsTask.execute();
        }
    }

    public void getMeasurementServers(final OnMeasurementServersLoaded onMeasurementServersLoaded, at.specure.android.api.jsons.Location location, boolean forceLoad) {
        if (!forceLoad && (this.measurementsServers != null) && (!this.measurementsServers.isEmpty())) {
            onMeasurementServersLoaded.onServersLoaded(this.measurementsServers);
            return;
        }

        if (measurementServersTask != null && measurementServersTask.getStatus() == AsyncTask.Status.RUNNING)
            return;

        if (forceLoad || (measurementServersTask == null) || measurementServersTask.shouldRun(location)) {

            //run sending zero measurements
            setSendZeroMeasurements();

            if (measurementServersTask != null) {
                boolean b = measurementServersTask.shouldRun(location);
                if (!b) {
                    onMeasurementServersLoaded.onServersLoaded(measurementServersTask.getServers());
                    return;
                }
            }

            measurementServersTask = new GetMeasurementServersTask(this);
            measurementServersTask.setOnCompleteListener(new MeasurementTaskEndedListener() {
                @Override
                public void taskEnded(List<MeasurementServer> result) {
                    updateMeasurementServers(result);
                    onMeasurementServersLoaded.onServersLoaded(result);
                }
            });
            measurementServersTask.execute(location);
        } else {
            updateMeasurementServers(measurementServersTask.getServers());
            onMeasurementServersLoaded.onServersLoaded(measurementServersTask.getServers());
        }
    }

    private void updateMeasurementServers(List<MeasurementServer> result) {
        this.measurementsServers = result;
    }


    @SuppressWarnings("SameParameterValue")
    public void waitForSettings(boolean waitForUUID, boolean waitForHistoryFilters, boolean forceWait) {
        final boolean haveUuid = haveUuid();
        if (forceWait || (waitForUUID && !haveUuid) || (waitForHistoryFilters && !haveHistoryFilters())) {
            if (loadingDialog != null)
                loadingDialog.dismiss();
            if (settingsTask != null && settingsTask.getStatus() == AsyncTask.Status.RUNNING) {
                final CharSequence title = getResources().getText(!haveUuid ? R.string.main_dialog_registration_title : R.string.main_dialog_reload_title);
                final CharSequence text = getResources().getText(!haveUuid ? R.string.main_dialog_registration_text : R.string.main_dialog_reload_text);
                loadingDialog = ProgressDialog.show(this, title, text, true, true);
                loadingDialog.setOnCancelListener(new ProgressDialog.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        onBackPressed();
                    }
                });
            }
        }
    }

    public MainScreenState startTest(MainScreenState state) {
        checkForBackgroundLocationPermissions(PERM_REQ_BACKGROUND_LOCATION_ACCESS);
        checkForGPSPermissions(PERM_REQ_LOC_COARSE_TEST, PERM_REQ_LOC_FINE_TEST);
        return preRunTest(state);
    }

    private MainScreenState preRunTest(MainScreenState state) {

        if (networkInfoCollector != null) {
            if (!networkInfoCollector.hasConnectionFromAndroidApi()) {
                showNoNetworkConnectionToast();
                return state;
            }
        }

        if (!PrivacyConfig.isClientUUIDPersistent(this.getApplicationContext())) {
            RegistrationTask registrationTask = new RegistrationTask(this);
            registrationTask.setEndTaskListener(new EndTaskListener() {
                @Override
                public void taskEnded(JsonArray result) {
                    MainScreenState mainScreenState = fireTest();
                    try {
                        MainMenuFragment currentFragment = (MainMenuFragment) getCurrentFragment();
                        if (currentFragment != null) {
                            currentFragment.changeScreenState(mainScreenState, "Main Activity - popBackStackFull", true);
                        }
                    } catch (ClassCastException e) {
                        //DO nothing
                    }
                }
            });
            registrationTask.execute();
            return MainScreenState.DEFAULT;
        } else {
            return fireTest();
        }
    }

    @NonNull
    private MainScreenState fireTest() {
        final boolean loopMode = LoopModeConfig.isLoopMode(this);
        if (loopMode) {
            ConfigHelper.setHistoryIsDirty(this, true);
            LoopModeConfig.resetCurrentTestNumber(this);
            startTestingService(TestService.ACTION_START_LOOP);
            return MainScreenState.LOOP_MODE_ACTIVE;
        } else {
            startTestingService(TestService.ACTION_START_TEST);
            return MainScreenState.TESTING;
        }
    }

    private void startTestingService(String actionStartService) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(actionStartService, null, this, TestService.class));
        } else {
            startService(new Intent(actionStartService, null, this, TestService.class));
        }
    }

    public void checkLoopModeRunning(LoopModeActivityCheckListener listener) {

        //TODO: bind to it and chcek if loop is running


        // Bind to TestService
        final Intent serviceIntent = new Intent(this, TestService.class);
        boolean b = this.bindService(serviceIntent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                final TestService.RMBTBinder binder = (TestService.RMBTBinder) service;
                TestService testService = binder.getService();
                boolean loopMode = testService.isLoopModeRunning();
                if (listener != null) {
                    listener.onLoopModeRunning(loopMode);
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        }, Context.BIND_AUTO_CREATE);
    }

    public boolean isTestRunning() {
        return isMyServiceRunning(TestService.class);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().startsWith(service.service.getClassName())) {
                    if (this.getPackageName().equalsIgnoreCase(service.service.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void showShareResultsIntent() {
        Fragment f = getCurrentFragment();
        if (f != null) {
            switch (FeatureConfig.showLayoutTheme(this)) {
                case LAYOUT_SQUARE:
                    ((SimpleResultFragment) f).startShareResultsIntent();
                    break;
                default:
                    ((ResultPagerFragment) f).getPagerAdapter().startShareResultsIntent();

            }
        }
    }

    public void showTermsCheck(CheckType checkType) {
        popBackStackFull();
        final FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragment_content, TermsCheckFragment.getInstance(checkType), AppConstants.PAGE_TITLE_TERMS_CHECK);
        ft.commit();
    }

    public void showRtrWebPage() {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.menu_rtr_web_link))));
    }


    public boolean showChecksIfNecessary() {
        boolean icDecisionMade = true;
        if (getResources().getBoolean(R.bool.test_use_personal_data_fuzzing)) {
            icDecisionMade = ConfigHelper.isICDecisionMade(this);
            if (!icDecisionMade) {
                showIcCheck();
            }
        }

        boolean ndtDecisionMade = true;
        if (getResources().getBoolean(R.bool.show_ndt_info)) {
            ndtDecisionMade = ConfigHelper.isNDTDecisionMade(this);
            if (!ndtDecisionMade) {
                showNdtCheck();
            }
        }

        return !ndtDecisionMade || !icDecisionMade;
    }

    /**
     * information commissioner check
     */
    public void showIcCheck() {
        final FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragment_content, CheckFragment.newInstance(CheckType.INFORMATION_COMMISSIONER), AppConstants.PAGE_TITLE_CHECK_INFORMATION_COMMISSIONER);
        ft.addToBackStack(AppConstants.PAGE_TITLE_CHECK_INFORMATION_COMMISSIONER);
        ft.commit();
    }

    public void showNdtCheck() {
        final FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragment_content, CheckFragment.newInstance(CheckType.NDT), AppConstants.PAGE_TITLE_NDT_CHECK);
        ft.addToBackStack(AppConstants.PAGE_TITLE_NDT_CHECK);
        ft.commit();
    }

    public void showResultsAfterTest(String testUuid) {
        popBackStackFull();
        TestConfig.setShouldShowResults(false);
        final SimpleResultFragment fragment = SimpleResultFragment.newInstance(testUuid);
        showFragment(AppConstants.PAGE_TITLE_HISTORY_PAGER, fragment);
    }

    public void showDetailedResultsAfterTest(String testUuid) {
        popBackStackFull();
        TestConfig.setShouldShowResults(false);
        final ResultPagerFragment fragment = new ResultPagerFragment();
        final Bundle args = new Bundle();
        args.putString(ResultPagerController.ARG_TEST_UUID, testUuid);
        fragment.setArguments(args);
        showFragment(AppConstants.PAGE_TITLE_HISTORY_PAGER, fragment);
    }

    public void initApp(boolean duringCreate) {
        //check log directory and send log files to control server if available
        checkLogs(new OnCompleteListener() {

            @Override
            public void onComplete(int flag, Object object) {
                //after log check: redirect system output to file if option is set
                redirectSystemOutput(ConfigHelper.isSystemOutputRedirectedToFile(MainActivity.this));
            }
        });

        popBackStackFull();

        showFragment(AppConstants.PAGE_TITLE_MAIN, new MainMenuFragment(), false);

        checkNews();
        checkSettings(false, null);
        //checkIp();
        waitForSettings(true, false, false);
        historyResultLimit = Config.HISTORY_RESULTLIMIT_DEFAULT;

        if (!duringCreate)
            if (checkForGPSPermissions(PERM_REQ_LOC_COARSE_START, PERM_REQ_LOC_FINE_START)) {
                GeoLocationX.getInstance(this.getApplication()).getLastKnownLocation(this, null);
            }
    }

    public void showNoNetworkConnectionToast() {
        Toast.makeText(this, R.string.error_no_internet, Toast.LENGTH_SHORT).show();
    }

    @SuppressWarnings("SameParameterValue")
    public void showHistory(final boolean popStack) {
        popBackStackFull();

        FragmentTransaction ft;
        ft = fm.beginTransaction();

        ft.replace(R.id.fragment_content, new HistoryFragment(), AppConstants.PAGE_TITLE_HISTORY);
        ft.addToBackStack(AppConstants.PAGE_TITLE_HISTORY);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        if (popStack) {
            fm.popBackStack();
        }

        ft.commit();
    }

    public void showHistoryTestDetailPager(final int pos) {
        //noinspection ConstantConditions
        if (historyItems != null) {
            final Bundle args = new Bundle();
//            final ResultPagerFragment fragment = new ResultPagerFragment();
            String testUuid = historyItems.get(pos).getTestUUID();
            BaseFragment fragment = SimpleResultFragment.newInstance(testUuid);

            switch (FeatureConfig.showLayoutTheme(this)) {
                case LAYOUT_SQUARE:
                    fragment = SimpleResultFragment.newInstance(testUuid);
                    break;
                default:
                    fragment = new ResultPagerFragment();
                    args.putString(ResultPagerController.ARG_TEST_UUID, testUuid);
                    fragment.setArguments(args);
                    break;
            }

            final FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.fragment_content, fragment, AppConstants.PAGE_TITLE_HISTORY_PAGER);
            ft.addToBackStack(AppConstants.PAGE_TITLE_HISTORY_PAGER);
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            ft.commit();
        }
    }

    public void showAbout() {
        popBackStackFull();

        FragmentTransaction ft;
        ft = fm.beginTransaction();

        ft.replace(R.id.fragment_content, new AboutFragment(), AppConstants.PAGE_TITLE_ABOUT);
        ft.addToBackStack(AppConstants.PAGE_TITLE_ABOUT);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

        ft.commit();
    }

    public void showExpandedResultDetail(QoSServerResultCollection testResultArray, DetailType detailType, int position) {
        FragmentTransaction ft;

        //final RMBTResultDetailPagerFragment fragment = new RMBTResultDetailPagerFragment();
        final QoSCategoryPagerFragment fragment = new QoSCategoryPagerFragment();

        fragment.setQoSResult(testResultArray);
        fragment.setDetailType(detailType);

        ft = fm.beginTransaction();
        ft.replace(R.id.fragment_content, fragment, AppConstants.PAGE_TITLE_RESULT_QOS);
        ft.addToBackStack("result_detail_expanded");
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();

        fragment.setCurrentPosition(position);
    }

    public void showQoSTestDetails(List<QoSServerResult> resultList, List<QoSServerResultDesc> descList, int index) {
        FragmentTransaction ft;

        //final RMBTResultDetailPagerFragment fragment = new RMBTResultDetailPagerFragment();
        final QoSTestDetailPagerFragment fragment = new QoSTestDetailPagerFragment();

        fragment.setQoSResultList(resultList);
        fragment.setQoSDescList(descList);
        fragment.setInitPosition(index);

        ft = fm.beginTransaction();
        ft.replace(R.id.fragment_content, fragment, AppConstants.PAGE_TITLE_TEST_DETAIL_QOS);
        ft.addToBackStack(AppConstants.PAGE_TITLE_TEST_DETAIL_QOS);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
    }

    public void showMapFromPager() {
        try {
            Fragment f = getCurrentFragment();
            if (f != null) {
                ((ResultPagerFragment) f).getPagerAdapter().showMap();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showMap(boolean popBackStack) {
        if (popBackStack) {
            popBackStackFull();
        }
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                FragmentTransaction ft;
                ft = fm.beginTransaction();
                if (MapConfig.MAP_TYPE_MAPBOX == MapConfig.getMapType(MainActivity.this)) {
                    Fragment f = new MapBoxFragment();
                    mapInterface = (at.specure.android.screens.map.MapInterface) f;
                    Bundle bundle = new Bundle();
                    f.setArguments(bundle);
                    ft.replace(R.id.fragment_content, f, AppConstants.PAGE_TITLE_MAP);
                    ft.addToBackStack(AppConstants.PAGE_TITLE_MAP);
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    ft.commit();
                    ((MapBoxFragment) f).setActionBarItems(MainActivity.this);
                }

            }
        });
    }

    public void showMap(String mapType, LatLng initialCenter, boolean clearFilter, boolean popBackStack) {
        if (popBackStack) {
            popBackStackFull();
        }

        if (MapConfig.MAP_TYPE_MAPBOX == MapConfig.getMapType(this)) {
            final MapBoxFragment fragment = new MapBoxFragment();

            final Bundle bundle = new Bundle();
            bundle.putParcelable("initialCenter", initialCenter);

            System.out.println("SHOW MAP");
            fragment.setArguments(bundle);
            showFragment(AppConstants.PAGE_TITLE_MAP, fragment);
            mapInterface = (at.specure.android.screens.map.MapInterface) fragment;
        }
    }

    public void showSettings() {
        startActivity(new Intent(this, PreferenceActivity.class));
    }

    private void showBadges() {
//        startActivity(new Intent(this, BadgesActivity.class));
        startActivity(new Intent(this, BadgeListActivity.class));
    }

    public void showStatistics() {
        String urlStatistic = getString(R.string.url_statistics); // ConfigHelper.getVolatileSetting("url_statistics");
        showHelp(urlStatistic, true, AppConstants.PAGE_TITLE_STATISTICS, R.string.page_title_statistics);
    }

    public void showFragment(String pageTitle, Fragment fragment) {
        showFragment(pageTitle, fragment, true);
    }

    public void showFragment(final String pageTitle, final Fragment fragment, final boolean addToBackStack) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (!isPausing) { // control because app can request change fragment during activity is going to sleep
                    FragmentTransaction ft;
                    ft = fm.beginTransaction();
                    ft.replace(R.id.fragment_content, fragment, pageTitle);
                    if (addToBackStack) {
                        ft.addToBackStack(pageTitle);
                    }
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    ft.commit();
                }
            }
        });
    }

    public void showHelp(final int resource, boolean popBackStack) {
        showHelp(getResources().getString(resource), popBackStack, AppConstants.PAGE_TITLE_HELP, R.string.page_title_help);
    }

    public void showHelp(boolean popBackStack) {
        showHelp("", popBackStack, AppConstants.PAGE_TITLE_HELP, R.string.page_title_help);
    }

    public void showHelp(final String url, boolean popBackStack, String titleId, int titleResourceId) {
        if (popBackStack) {
            popBackStackFull();
        }
        final Fragment fragment = new HelpFragment();
        final Bundle args = new Bundle();
        args.putString(HelpFragment.ARG_URL, url);
        args.putInt(HelpFragment.ARG_TITLE, titleResourceId);
        fragment.setArguments(args);
        showFragment(titleId, fragment);
    }

    public void showSync() {
        showFragment(AppConstants.PAGE_TITLE_SYNC, new SyncFragment());
    }

    public void showFilter() {
        showFragment(AppConstants.PAGE_TITLE_HISTORY_FILTER, new HistoryFilterFragment());
    }

//    public void showNetStatFragment() {
//        showFragment(AppConstants.PAGE_TITLE_NETSTAT, new NetstatFragment());
//    }
//
//    public void showLogFragment() {
//        showFragment(AppConstants.PAGE_TITLE_LOG, new LogFragment());
//    }


    private void stopBackgroundProcesses() {
        if (newsTask != null) {
            newsTask.cancel(true);
            newsTask = null;
        }
        if (settingsTask != null) {
            settingsTask.cancel(true);
            settingsTask = null;
        }
        if (loadingDialog != null) {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
        if (historyTask != null) {
            historyTask.cancel(true);
            historyTask = null;
        }
    }

    public void setSettings(final String[] history_filter_devices, final String[] history_filter_networks) {
        historyFilterDevices = history_filter_devices;
        historyFilterNetworks = history_filter_networks;

        historyFilterDevicesFilter = new ArrayList<>();
        if (history_filter_devices != null)
            historyFilterDevicesFilter.addAll(Arrays.asList(history_filter_devices));

        historyFilterNetworksFilter = new ArrayList<>();
        if (history_filter_networks != null)
            historyFilterNetworksFilter.addAll(Arrays.asList(history_filter_networks));
    }

    public String[] getHistoryFilterDevices() {
        return historyFilterDevices;
    }

    public String[] getHistoryFilterNetworks() {
        return historyFilterNetworks;
    }

    public ArrayList<String> getHistoryFilterDevicesFilter() {
        return historyFilterDevicesFilter;
    }

    public void setHistoryFilterDevicesFilter(final ArrayList<String> historyFilterDevicesFilter) {
        this.historyFilterDevicesFilter = historyFilterDevicesFilter;
        ConfigHelper.setHistoryIsDirty(this, true);
        historyDirty = true;
    }

    public ArrayList<String> getHistoryFilterNetworksFilter() {
        return historyFilterNetworksFilter;
    }

    public void setHistoryFilterNetworksFilter(final ArrayList<String> historyFilterNetworksFilter) {
        this.historyFilterNetworksFilter = historyFilterNetworksFilter;
        ConfigHelper.setHistoryIsDirty(this, true);
        historyDirty = true;
    }

    public List<HistoryItem> getHistoryItemList() {
        return historyItems;
    }


    public void setToolbarVisible(int visibility) {
        if (toolbar != null) {
            toolbar.setVisibility(visibility);
        }
    }

    @Override
    public void onDrawerOpen() {
        exitAfterDrawerClose = false;
    }

    @Override
    public void onDrawerClose() {

    }

    public void updateTitle(String title) {
        if (toolbar != null) {
            toolbar.setTitle(title);
        }
    }

    @Override
    public void onPositiveButtonClicked(int starsCount, String reviewText) {
        AppRater.onPositiveButtonClicked(this, starsCount, reviewText);
    }

    @Override
    public void onNegativeButtonClicked() {
        AppRater.onNegativeButtonClicked(this);
    }

    @Override
    public void onNeutralButtonClicked() {
        AppRater.onNeutralButtonClicked(this);
    }

    public void showSurveyRequest() {
        if (SurveyConfig.isSurveyEnabledInApp(this)) {
            SurveyConfig.showSurveyDialog(this);
        }
    }

    @Override
    public void requestPermission(int requestCode) {
        if (requestCode == PERM_REQ_LOC_COARSE_START || requestCode == PERM_REQ_LOC_FINE_START || requestCode == PERM_REQ_LOC_FINE_TEST || requestCode == PERM_REQ_LOC_COARSE_TEST) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, requestCode);
            }
        } else if (requestCode == PERM_REQ_READ_PHONE_STATE_START || requestCode == PERM_REQ_READ_PHONE_STATE_TEST) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, requestCode);
            }
        } else if (requestCode == PERM_REQ_BACKGROUND_LOCATION_ACCESS) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, requestCode);
            }
        }
    }

//    private class MainGeoLocation extends GeoLocation {
//
//        MainGeoLocation(final Context context) {
//            super(context, ConfigHelper.isGPS(context));
//        }
//
//        @Override
//        public void onLocationChanged(final Location curLocation) {
//        }
//    }

    @Override
    public void onBackPressed() {

        if ((drawerLayout.isDrawerOpen(drawerList) && !exitAfterDrawerClose) && ((initialSetupInterface != null) && !(initialSetupInterface instanceof TermsCheckFragment))) {
            drawerLayout.closeDrawer(drawerList);
            return;
        }

        final TermsCheckFragment tcFragment = (TermsCheckFragment) getSupportFragmentManager().findFragmentByTag("terms_check");
        if (tcFragment != null && tcFragment.isResumed()) {
            if (tcFragment.onBackPressed())
                return;
        }

        final SyncFragment syncCodeFragment = (SyncFragment) getSupportFragmentManager()
                .findFragmentByTag("sync");
        if (syncCodeFragment != null && syncCodeFragment.isResumed()) {
            if (syncCodeFragment.onBackPressed())
                return;
        }

        final MainMenuFragment mainMenuCodeFragment = (MainMenuFragment) getSupportFragmentManager()
                .findFragmentByTag(AppConstants.PAGE_TITLE_MAIN);
        if (mainMenuCodeFragment != null && mainMenuCodeFragment.isResumed()) {
            if (mainMenuCodeFragment.isTestVisible()) {
                mainMenuCodeFragment.onBackPressed();
                return;
            } else {
                if (ConfigHelper.isDontShowMainMenuOnClose(this)) {
                    startTestingService(TestService.ACTION_STOP_SERVICE);
                    super.onBackPressed();
                    return;
                } else {
                    if (exitAfterDrawerClose) {
                        startTestingService(TestService.ACTION_STOP_SERVICE);
                        super.onBackPressed();
                    } else {
                        exitAfterDrawerClose = true;
                        drawerLayout.openDrawer(drawerList);
                        return;
                    }
                }
            }

        }

        if (getSupportFragmentManager().getBackStackEntryCount() > 0 || exitAfterDrawerClose) {
            super.onBackPressed();
        } else {
            System.out.println(getCurrentFragment());
            if (ConfigHelper.isDontShowMainMenuOnClose(this)) {
                super.onBackPressed();
            } else {
                if (((initialSetupInterface != null) && (initialSetupInterface instanceof TermsCheckFragment))) {
                    super.onBackPressed();
                    return;
                }
                exitAfterDrawerClose = true;
                drawerLayout.openDrawer(drawerList);
            }
        }
    }

    public void updateHistory(final HistoryUpdatedCallback callback) {


        androidx.loader.app.LoaderManager loaderManager = androidx.loader.app.LoaderManager.getInstance(this);

        if (((historyItems == null) || (historyItems.isEmpty()))
                || (ConfigHelper.getHistoryIsDirty(getApplicationContext()))) {
            loaderManager.destroyLoader(HISTORY_LOADER_ID);
            Loader<List<HistoryItem>> historyLoader = loaderManager.initLoader(HISTORY_LOADER_ID, null, new androidx.loader.app.LoaderManager.LoaderCallbacks<List<HistoryItem>>() {
                @NonNull
                @Override
                public Loader<List<HistoryItem>> onCreateLoader(int id, @Nullable Bundle args) {
                    Loader loader = null;
                    switch (id) {
                        case HISTORY_LOADER_ID:
                            loader = new HistoryLoader(MainActivity.this, historyFilterDevicesFilter, historyFilterNetworksFilter);
                    }
                    return loader;
                }

                @Override
                public void onLoadFinished(@NonNull Loader<List<HistoryItem>> loader, List<HistoryItem> data) {
                    historyItems = data;
                    if (data.size() > 0) {
                        if (callback != null) {
                            callback.historyUpdated(HistoryUpdatedCallback.SUCCESSFUL);
                        }
                    } else {
                        if (callback != null) {
                            callback.historyUpdated(HistoryUpdatedCallback.LIST_EMPTY);
                        }
                    }
                    historyDirty = false;
                    ConfigHelper.setHistoryIsDirty(getApplicationContext(), false);
                }

                @Override
                public void onLoaderReset(@NonNull Loader<List<HistoryItem>> loader) {
                    historyItems = new ArrayList<>();
                }
            });
            historyLoader.forceLoad();
            Timber.d("History should load");
        } else {
            if (historyItems.size() > 0) {
                if (callback != null) {
                    callback.historyUpdated(HistoryUpdatedCallback.SUCCESSFUL);
                }
            } else {
                if (callback != null) {
                    callback.historyUpdated(HistoryUpdatedCallback.LIST_EMPTY);
                }
            }
        }
    }

    public int getHistoryResultLimit() {
        return historyResultLimit;
    }

    public void setHistoryResultLimit(final int limit) {
        historyResultLimit = limit;
    }

    public boolean getMapTypeSatellite() {
        return mapTypeSatellite;
    }

    public void setMapTypeSatellite(boolean mapTypeSatellite) {
        this.mapTypeSatellite = mapTypeSatellite;
    }

    public void popBackStackFull() {
//        runOnUiThread(new Runnable() {
//
//            @Override
//            public void run() {
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStackImmediate(fm.getBackStackEntryAt(0).getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        try {
            MainMenuFragment currentFragment = (MainMenuFragment) getCurrentFragment();
            if (currentFragment != null) {
                checkLoopModeRunning(new LoopModeActivityCheckListener() {
                    @Override
                    public void onLoopModeRunning(boolean isRunning) {
                        boolean loopModeRunning = isRunning;
                        if (loopModeRunning) {
                            currentFragment.changeScreenState(MainScreenState.LOOP_MODE_ACTIVE, "Main Activity - popBackStackFull", true);
                        } else {
                            currentFragment.changeScreenState(MainScreenState.DEFAULT, "Main Activity - popBackStackFull", true);
                        }
                    }
                });
            }
        } catch (ClassCastException e) {
            //DO nothing
        }

//            }
//        });
    }

    public void redirectSystemOutput(boolean toFile) {
        try {
            if (toFile) {
                Timber.i("%s redirecting sysout to file", DEBUG_TAG);
                //Redirecting console output and runtime exceptions to file (System.out.println)
                File f = new File(Environment.getExternalStorageDirectory(), "qosdebug");
                if (!f.exists()) {
                    boolean mkdir = f.mkdir();
                    if (!mkdir) {
                        Timber.e("%s redirectSystemOutput: failed create dir", "ERROR");
                    }
                }

                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmm", Locale.GERMAN);
                //PrintStream fileStream = new PrintStream(new File(f, sdf.format(new Date()) + ".txt"));
                PrintStream fileStream = new DebugPrintStream(new File(f, sdf.format(new Date()) + ".txt"));

                //System.setOut(fileStream);
                System.setErr(fileStream);
            } else {
                //Redirecting console output and runtime exceptions to default output stream
                //System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
                //System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err)));
                Timber.i(" %s redirecting sysout to default", DEBUG_TAG);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    public Fragment getCurrentFragment() {
        final int backStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();
        if (backStackEntryCount > 0) {
            try {
                final FragmentManager.BackStackEntry backStackEntryAt = getSupportFragmentManager().getBackStackEntryAt(backStackEntryCount - 1);
                String fragmentTag = backStackEntryAt.getName();
                Fragment currentFragment = getSupportFragmentManager().findFragmentByTag(fragmentTag);
                return currentFragment;
            } catch (Exception e) {
                // fix possible race condition:
                // when called in background thread - back stack could be different between call of
                // getBackStackEntryCount() and getBackStackEntryAt()
                e.printStackTrace();
            }
        }

        return getSupportFragmentManager().findFragmentByTag(AppConstants.PAGE_TITLE_MAIN);
    }

    public void setLockNavigationDrawer(boolean isLocked) {
        if (isLocked) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        } else {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }
    }

    @Override
    public void onAttachFragment(final Fragment fragment) {
        super.onAttachFragment(fragment);
        if ((fragment != null) && (fragment instanceof InitialSetupInterface)) {
            initialSetupInterface = (InitialSetupInterface) fragment;
            if (initialSetupInterface instanceof TermsCheckFragment) {
                setToolbarVisible(View.GONE);
            } else {
                setToolbarVisible(View.VISIBLE);
                refreshMenuItems();
            }
        }
    }





    public void setVisibleMenuItems(Integer... id) {
        Timber.e("MENU  MActivity setVisibleMenuItems: %s", actionBarMenu);
        if (actionBarMenu == null) {
            invalidateOptionsMenu();
            supportInvalidateOptionsMenu();
        }
        if (actionBarMenu != null) {
            if (id != null && id.length > 0) {
                Set<Integer> idSet = new HashSet<>();
                Collections.addAll(idSet, id);
                for (int i = 0; i < actionBarMenu.size(); i++) {
                    MenuItem item = actionBarMenu.getItem(i);
                    if (idSet.contains(item.getItemId())) {
                        item.setVisible(true);
                    } else {
                        item.setVisible(false);
                    }
                }
            } else {
                for (int i = 0; i < actionBarMenu.size(); i++) {
                    MenuItem item = actionBarMenu.getItem(i);
                    item.setVisible(false);
                }
            }
        }
    }

    /**
     * @return true if some location provider is permitted
     */
    public boolean checkForGPSPermissions(int requestCodeCoarse, int requestCodeFine) {

        boolean returnValue = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                PermissionHandler.showLocationExplanationDialog(this, requestCodeFine, this);
            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, requestCodeFine);
            }
        } else {
            returnValue = true;
        }

        return returnValue;
    }

    public boolean checkForBackgroundLocationPermissions(int requestCodeBackgroundLocation) {

        boolean returnValue = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                PermissionHandler.showBackgroundLocationExplanationDialog(this, requestCodeBackgroundLocation, this);
            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, requestCodeBackgroundLocation);
            }
        } else {
            returnValue = true;
        }

        return returnValue;
    }

    public boolean checkForReadPhoneStatePermissions(int requestCodePhone) {

        boolean returnValue = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_PHONE_STATE)) {
                PermissionHandler.showReadPhoneStateExplanationDialog(this, requestCodePhone, this);
            } else {
                requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, requestCodePhone);
            }
        } else {
            returnValue = true;
        }

        return returnValue;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERM_REQ_LOC_COARSE_START:
            case PERM_REQ_LOC_FINE_START:
                checkForReadPhoneStatePermissions(PERM_REQ_READ_PHONE_STATE_START);
                if ((grantResults.length > 0) && (grantResults[0] == PermissionChecker.PERMISSION_GRANTED)) {
                    GeoLocationX.getInstance(this.getApplication()).getLastKnownLocation(this, null);
                }
                break;
            case PERM_REQ_LOC_FINE_TEST:
            case PERM_REQ_LOC_COARSE_TEST:
                if ((grantResults.length > 0) && (grantResults[0] == PermissionChecker.PERMISSION_GRANTED)) {
                    checkForBackgroundLocationPermissions(PERM_REQ_BACKGROUND_LOCATION_ACCESS);
                    GeoLocationX.getInstance(this.getApplication()).getLastKnownLocation(this, null);
                }
                break;
            case PERM_REQ_READ_PHONE_STATE_START:
                if ((grantResults.length > 0) && (grantResults[0] == PermissionChecker.PERMISSION_GRANTED)) {
                }
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public NetworkInfoCollector getNetworkInfoCollector() {
        return this.networkInfoCollector;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    public interface HistoryUpdatedCallback {
        int SUCCESSFUL = 0;
        int LIST_EMPTY = 1;
        int ERROR = 2;

        void historyUpdated(int status);
    }


}
