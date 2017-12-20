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
/*******************************************************************************
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
package at.specure.android.screens.main;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.specure.opennettest.R;

import java.io.File;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import at.specure.android.api.calls.CheckHistoryTask;
import at.specure.android.api.calls.CheckNewsTask;
import at.specure.android.api.calls.CheckSettingsTask;
import at.specure.android.api.calls.GetMapOptionsInfoTask;
import at.specure.android.api.calls.GetMapOptionsProvidersTask;
import at.specure.android.api.calls.GetMeasurementServersTask;
import at.specure.android.api.calls.LogTask;
import at.specure.android.api.calls.SendZeroMeasurementsTask;
import at.specure.android.api.jsons.FilterGroup;
import at.specure.android.api.jsons.MapFilterCountries;
import at.specure.android.api.jsons.MeasurementServer;
import at.specure.android.api.jsons.VoipTestResult;
import at.specure.android.configs.Config;
import at.specure.android.configs.ConfigHelper;
import at.specure.android.configs.LoopModeConfig;
import at.specure.android.constants.AppConstants;
import at.specure.android.screens.about.AboutFragment;
import at.specure.android.screens.help.HelpFragment;
import at.specure.android.screens.main.main_fragment.MainMenuFragment;
import at.specure.android.screens.main.main_fragment.MainScreenState;
import at.specure.android.screens.map.MapFilterTypes;
import at.specure.android.screens.map.MapListEntry;
import at.specure.android.screens.map.MapListSection;
import at.specure.android.screens.map.MapProperties;
import at.specure.android.screens.preferences.PreferenceActivity;
import at.specure.android.screens.result.QoSCategoryPagerFragment;
import at.specure.android.screens.result.QoSTestDetailPagerFragment;
import at.specure.android.screens.result.TestResultDetailFragment;
import at.specure.android.screens.result.adapter.result.OnCompleteListener;
import at.specure.android.screens.terms.CheckFragment;
import at.specure.android.screens.terms.TermsCheckFragment;
import at.specure.android.test.LoopService;
import at.specure.android.test.TestService;
import at.specure.android.util.DebugPrintStream;
import at.specure.android.util.EndBooleanTaskListener;
import at.specure.android.util.EndTaskListener;
import at.specure.android.util.GeoLocation;
import at.specure.android.util.Helperfunctions;
import at.specure.android.util.MeasurementTaskEndedListener;
import at.specure.android.util.net.NetworkInfoCollector;
import at.specure.client.v2.task.result.QoSServerResult;
import at.specure.client.v2.task.result.QoSServerResult.DetailType;
import at.specure.client.v2.task.result.QoSServerResultCollection;
import at.specure.client.v2.task.result.QoSServerResultDesc;
import io.fabric.sdk.android.Fabric;

/**
 * @author
 */
public class MainActivity extends AppCompatActivity implements MapProperties, DrawerActionListener {
    /**
     *
     */
    private final static boolean VIEW_HIERARCHY_SERVER_ENABLED = false;

    /**
     *
     */
    private static final String DEBUG_TAG = "MainActivity";

    /**
     *
     */
    private android.support.v4.app.FragmentManager fm;

    /**
     *
     */
    private GeoLocation geoLocation;

    /**
     *
     */
    private CheckNewsTask newsTask;

    /**
     *
     */
    private CheckSettingsTask settingsTask;

    private GetMeasurementServersTask measurementServersTask;

    private SendZeroMeasurementsTask sendZeroMeasurementsTask;

    /**
     *
     */
    private GetMapOptionsInfoTask getMapOptionsInfoTask;

    /**
     *
     */
    private CheckHistoryTask historyTask;

    /**
     *
     */
    private String historyFilterDevices[];

    /**
     *
     */
    private String historyFilterNetworks[];

    /**
     *
     */
    private ArrayList<String> historyFilterDevicesFilter;

    /**
     *
     */
    private ArrayList<String> historyFilterNetworksFilter;

    /**
     *
     */
    private final ArrayList<Map<String, String>> historyItemList = new ArrayList<Map<String, String>>();

    /**
     *
     */
    private final ArrayList<Map<String, String>> historyStorageList = new ArrayList<Map<String, String>>();

    /**
     *
     */
    private int historyResultLimit;

    /**
     *
     */
    private final HashMap<String, String> currentMapOptions = new HashMap<String, String>();

    /**
     *
     */
    private HashMap<String, String> currentMapOptionTitles = new HashMap<String, String>();

    /**
     *
     */
    private ArrayList<MapListSection> mapTypeListSectionList;

    /**
     *
     */
    private HashMap<String, List<MapListSection>> mapFilterListSectionListMap;

    private MapListEntry currentMapType;

    // /

    /**
     *
     */
    private IntentFilter mNetworkStateChangedFilter;

    /**
     *
     */
    private BroadcastReceiver mNetworkStateIntentReceiver;

    // /

    private boolean mapTypeSatellite;

    /**
     *
     */
    private boolean historyDirty = true;

    /**
     *
     */
    private MapOverlay mapOverlayType = MapOverlay.AUTO;

    /**
     *
     */
    private boolean mapFirstRun = true;

    private ProgressDialog loadingDialog;

    private DrawerLayout drawerLayout;

    private ListView drawerList;

    private ActionBarDrawerToggle drawerToggle;

    private boolean exitAfterDrawerClose = false;

    private Menu actionBarMenu;

    private String title;

    private NetworkInfoCollector networkInfoCollector;
    private Toolbar toolbar;
    private List<MeasurementServer> measurementsServers = new ArrayList<MeasurementServer>();
    private MapFilterCountries mapFilterCountries;
    private FilterGroup mapFilterOperatorList;

    /**
     *
     */
    private void preferencesUpdate() {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

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
                preferences.edit().clear().commit();
                Log.d(DEBUG_TAG, "preferences cleared");
            }

            if (lastVersion != clientVersion)
                preferences.edit().putInt("LAST_VERSION_CODE", clientVersion).commit();
        } catch (final NameNotFoundException e) {
            Log.e(DEBUG_TAG, "version of the application cannot be found", e);
        }
    }

    /**
     *
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        //Log.i("MAIN ACTIVITY", "onCreate");
        try {
            Fabric.with(this, new Crashlytics());
        } catch (Exception e) {
            e.printStackTrace();
            // ignored... if you want to use crashlytics you must follow steps in the https://fabric.io/sign_up,
        }
        restoreInstance(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        NetworkInfoCollector.init(this);
        networkInfoCollector = NetworkInfoCollector.getInstance();


        preferencesUpdate();
        setContentView(R.layout.main_with_navigation_drawer);

        if (VIEW_HIERARCHY_SERVER_ENABLED) {
            ViewServer.get(this).addWindow(this);
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);

//        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
//        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
//        actionBar.setDisplayUseLogoEnabled(true);
//        actionBar.setBackgroundDrawable(new ColorDrawable(Color.WHITE));


        // initialize the navigation drawer with the main menu list adapter:

        MainMenuListAdapter mainMenuAdapter = new MainMenuListAdapter(this,
                MainMenuUtil.getMenuTitles(getResources(), this), MainMenuUtil.getMenuIds(this));

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList = (ListView) findViewById(R.id.left_drawer);

//        drawerToggle = new EndDrawerToggle(this, drawerLayout, toolbar, R.string.title_screen_empty, R.string.title_screen_empty, this);

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
                if (KeyEvent.KEYCODE_BACK == event.getKeyCode() && exitAfterDrawerClose) {
                    onBackPressed();
                    return true;
                }
                return false;
            }
        });
        drawerLayout.setDrawerListener(drawerToggle);
        drawerList.setAdapter(mainMenuAdapter);
        drawerList.setOnItemClickListener(new OnItemClickListener() {
            final List<Integer> menuIds = MainMenuUtil.getMenuActionIds(MainActivity.this);

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                selectMenuItem(menuIds.get(position));
//                selectMenuItem((int) id);
                drawerLayout.closeDrawers();
            }
        });

//        actionBar.setDisplayHomeAsUpEnabled(true);
//        actionBar.setHomeButtonEnabled(true);

        // Do something against banding effect in gradients
        // Dither flag might mess up on certain devices??
        final Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
        window.addFlags(WindowManager.LayoutParams.FLAG_DITHER);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);


        final String uuid = ConfigHelper.getUUID(getApplicationContext());

        fm = getSupportFragmentManager();
        final Fragment fragment = fm.findFragmentById(R.id.fragment_content);
        if (!ConfigHelper.isTCAccepted(this)) {
            if (fragment != null && fm.getBackStackEntryCount() >= 1)
                // clear fragment back stack
                fm.popBackStack(fm.getBackStackEntryAt(0).getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);

//            getActionBar().hide();
            setLockNavigationDrawer(true);

            showTermsCheck();
        } else {
            currentMapOptions.put("highlight", uuid);
            if (fragment == null) {
                if (false) // deactivated for si // ! ConfigHelper.isNDTDecisionMade(this))
                {
                    showTermsCheck();
                    showNdtCheck();
                } else
                    initApp(true);
            }
        }

        geoLocation = new MainGeoLocation(getApplicationContext());

        mNetworkStateChangedFilter = new IntentFilter();
        mNetworkStateChangedFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        mNetworkStateIntentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                    final boolean connected = !intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
                    final boolean isFailover = intent.getBooleanExtra(ConnectivityManager.EXTRA_IS_FAILOVER, false);


                    if (connected) {
                        if (networkInfoCollector != null) {
                            networkInfoCollector.setHasConnectionFromAndroidApi(true);

                        }
                    } else {
                        if (networkInfoCollector != null) {
                            networkInfoCollector.setHasConnectionFromAndroidApi(false);
                        }
                    }

                    Log.i(DEBUG_TAG, "CONNECTED: " + connected + " FAILOVER: " + isFailover);
                }
            }
        };
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

        title = getTitle(getCurrentFragmentName());
        refreshActionBar(getCurrentFragmentName());

        for (int i = 0; i < menu.size(); i++) {
            Drawable drawable = menu.getItem(i).getIcon();
            if (drawable != null) {
                drawable.mutate();
                drawable.setColorFilter(getResources().getColor(R.color.toolbar_icon_overlay), PorterDuff.Mode.SRC_IN);
            }
        }

        return true;
        //return super.onCreateOptionsMenu(menu);
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

    /**
     * @param id
     */
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
            case R.id.action_log:
                showLogFragment();
                break;
            case R.id.action_stats:
                showStatistics();
                break;
            /*case R.id.action_menu_filter:
                showFilter();
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
                break;*/

        }
    }

    @SuppressWarnings("unchecked")
    protected void restoreInstance(Bundle b) {
        if (b == null)
            return;
        historyFilterDevices = (String[]) b.getSerializable("historyFilterDevices");
        historyFilterNetworks = (String[]) b.getSerializable("historyFilterNetworks");
        historyFilterDevicesFilter = (ArrayList<String>) b.getSerializable("historyFilterDevicesFilter");
        historyFilterNetworksFilter = (ArrayList<String>) b.getSerializable("historyFilterNetworksFilter");
        historyItemList.clear();
        historyItemList.addAll((ArrayList<Map<String, String>>) b.getSerializable("historyItemList"));
        historyStorageList.clear();
        historyStorageList.addAll((ArrayList<Map<String, String>>) b.getSerializable("historyStorageList"));
        historyResultLimit = b.getInt("historyResultLimit");
        currentMapOptions.clear();
        currentMapOptions.putAll((HashMap<String, String>) b.getSerializable("currentMapOptions"));
        currentMapOptionTitles = (HashMap<String, String>) b.getSerializable("currentMapOptionTitles");
        mapTypeListSectionList = (ArrayList<MapListSection>) b.getSerializable("mapTypeListSectionList");
        mapFilterListSectionListMap = (HashMap<String, List<MapListSection>>) b.getSerializable("mapFilterListSectionListMap");
        currentMapType = (MapListEntry) b.getSerializable("currentMapType");
    }

    @Override
    protected void onSaveInstanceState(Bundle b) {
        super.onSaveInstanceState(b);
        b.putSerializable("historyFilterDevices", historyFilterDevices);
        b.putSerializable("historyFilterNetworks", historyFilterNetworks);
        b.putSerializable("historyFilterDevicesFilter", historyFilterDevicesFilter);
        b.putSerializable("historyFilterNetworksFilter", historyFilterNetworksFilter);
        b.putSerializable("historyItemList", historyItemList);
        b.putSerializable("historyStorageList", historyStorageList);
        b.putInt("historyResultLimit", historyResultLimit);
        b.putSerializable("currentMapOptions", currentMapOptions);
        b.putSerializable("currentMapOptionTitles", currentMapOptionTitles);
        b.putSerializable("mapTypeListSectionList", mapTypeListSectionList);
        b.putSerializable("mapFilterListSectionListMap", mapFilterListSectionListMap);
        b.putSerializable("currentMapType", currentMapType);

    }

    @Override
    protected void onResume() {
        super.onResume();
        ViewServer.get(this).setFocusedWindow(this);

    }

    /**
     *
     */
    @Override
    public void onStart() {
        Log.i(DEBUG_TAG, "onStart");
        super.onStart();

        registerReceiver(mNetworkStateIntentReceiver, mNetworkStateChangedFilter);
        // init location Manager

        if (ConfigHelper.isTCAccepted(this) && ConfigHelper.isNDTDecisionMade(this)) {
            geoLocation.start();
        }

        title = getTitle(getCurrentFragmentName());
        refreshActionBar(getCurrentFragmentName());
    }

    /**
     *
     */
    @Override
    public void onStop() {
        Log.i(DEBUG_TAG, "onStop");
        super.onStop();
        stopBackgroundProcesses();
        unregisterReceiver(mNetworkStateIntentReceiver);
    }

    public void setOverlayVisibility(boolean isVisible) {
        final LinearLayout overlay = (LinearLayout) findViewById(R.id.overlay);

        if (isVisible) {
            overlay.setVisibility(View.VISIBLE);
            overlay.setClickable(true);
            overlay.bringToFront();
        } else {
            overlay.setVisibility(View.GONE);
        }
    }

    /**
     * @param context
     */
    private void checkNews(final Context context) {
        newsTask = new CheckNewsTask(this);
        newsTask.execute();
        // newsTask.setEndTaskListener(this);
    }

    /**
     * @param context
     */
    private void checkLogs(final Context context, final OnCompleteListener listener) {
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

    /**
     *
     */
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
        if (sendZeroMeasurementsTask != null && sendZeroMeasurementsTask.getStatus() == AsyncTask.Status.RUNNING)
            return;

        sendZeroMeasurementsTask = new SendZeroMeasurementsTask(this);
        sendZeroMeasurementsTask.setOnCompleteListener(new EndBooleanTaskListener() {
            @Override
            public void taskEnded(boolean result) {
                // If something want to displayed after un/successful sending on the main thread
            }
        });
        sendZeroMeasurementsTask.execute();
    }

    public void getMeasurementServers(final OnMeasurementServersLoaded onMeasurementServersLoaded, at.specure.android.api.jsons.Location location, boolean forceLoad) {
        if (measurementServersTask != null && measurementServersTask.getStatus() == AsyncTask.Status.RUNNING)
            return;

        if (forceLoad || (measurementServersTask == null) || measurementServersTask.shouldRun()) {

            //run sending zero measurements
            setSendZeroMeasurements();

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

    /**
     *
     */
    public void fetchMapOptions() {
        if (getMapOptionsInfoTask != null && getMapOptionsInfoTask.getStatus() == AsyncTask.Status.RUNNING)
            return;

        getMapOptionsInfoTask = new GetMapOptionsInfoTask(this);
        getMapOptionsInfoTask.execute();
    }

    /**
     * @param popStack
     */
    public MainScreenState startTest(final boolean popStack, MainScreenState state) {
        if (networkInfoCollector != null) {
            if (!networkInfoCollector.hasConnectionFromAndroidApi()) {
                showNoNetworkConnectionToast();
                return state;
            }
        }


        final boolean loopMode = LoopModeConfig.isLoopMode(this);
        if (loopMode) {
            //TOTO - HistoryDirty
            setHistoryDirty(true);
            startService(new Intent(this, LoopService.class));
            return MainScreenState.LOOP_MODE_ACTIVE;
        } else {
//            FragmentTransaction ft;
//            ft = fm.beginTransaction();
//            final RMBTTestFragment rmbtTestFragment = new RMBTTestFragment();
//            ft.replace(R.id.fragment_content, rmbtTestFragment, AppConstants.PAGE_TITLE_TEST);
//            ft.addToBackStack(AppConstants.PAGE_TITLE_TEST);
//            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
//            if (popStack)
//                fm.popBackStack();
//            ft.commit();

            final Intent service = new Intent(TestService.ACTION_START_TEST, null, this, TestService.class);
            startService(service);
            return MainScreenState.TESTING;
        }
    }

    public boolean isLoopModeRunning() {
        return isMyServiceRunning(LoopService.class);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().startsWith(service.service.getClassName())) {
                if (this.getPackageName().equalsIgnoreCase(service.service.getPackageName())) {
                    return true;
                }
            }
        }
        return false;
    }

    public void showTermsCheck() {
        popBackStackFull();

        final FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragment_content, TermsCheckFragment.getInstance(null), AppConstants.PAGE_TITLE_TERMS_CHECK);
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
        ft.replace(R.id.fragment_content, CheckFragment.newInstance(CheckFragment.CheckType.INFORMATION_COMMISSIONER), AppConstants.PAGE_TITLE_CHECK_INFORMATION_COMMISSIONER);
        ft.addToBackStack(AppConstants.PAGE_TITLE_CHECK_INFORMATION_COMMISSIONER);
        ft.commit();
    }

    public void showNdtCheck() {
        final FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragment_content, CheckFragment.newInstance(CheckFragment.CheckType.NDT), AppConstants.PAGE_TITLE_NDT_CHECK);
        ft.addToBackStack(AppConstants.PAGE_TITLE_NDT_CHECK);
        ft.commit();
    }

    public void showResultsAfterTest(String testUuid) {
        popBackStackFull();

        /* SHOW RESULT */
    }

    public void initApp(boolean duringCreate) {
        //check log directory and send log files to control server if available
        checkLogs(getApplicationContext(), new OnCompleteListener() {

            @Override
            public void onComplete(int flag, Object object) {
                //after log check: redirect system output to file if option is set
                redirectSystemOutput(ConfigHelper.isSystemOutputRedirectedToFile(MainActivity.this));
            }
        });

        popBackStackFull();

        FragmentTransaction ft;
        ft = fm.beginTransaction();
        ft.replace(R.id.fragment_content, new MainMenuFragment(), AppConstants.PAGE_TITLE_MAIN);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();

        checkNews(getApplicationContext());
        checkSettings(false, null);
        //checkIp();
        waitForSettings(true, false, false);
        fetchMapOptions();
        historyResultLimit = Config.HISTORY_RESULTLIMIT_DEFAULT;

        if (!duringCreate && geoLocation != null)
            geoLocation.start();
    }

    /**
     *
     */
    public void showNoNetworkConnectionToast() {
        Toast.makeText(this, R.string.error_no_internet, Toast.LENGTH_SHORT).show();
    }

    /**
     * @param popStack
     */
    public void showHistory(final boolean popStack) {
        popBackStackFull();

        /* show history fragment */

        refreshActionBar(AppConstants.PAGE_TITLE_HISTORY);
    }

    public void showHistoryPager(final int pos) {
        if (historyStorageList != null) {

            final Bundle args = new Bundle();

          /* SHOW HISTORY FRAGMENT */
            refreshActionBar(AppConstants.PAGE_TITLE_HISTORY_PAGER);
        }
    }

    public void showResultDetail(final String testUUid) {
        FragmentTransaction ft;

        final Fragment fragment = new TestResultDetailFragment();

        final Bundle args = new Bundle();

        args.putString(TestResultDetailFragment.ARG_UID, testUUid);
        fragment.setArguments(args);

        ft = fm.beginTransaction();
        ft.replace(R.id.fragment_content, fragment, AppConstants.PAGE_TITLE_RESULT_DETAIL);
        ft.addToBackStack(AppConstants.PAGE_TITLE_RESULT_DETAIL);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();

        refreshActionBar(AppConstants.PAGE_TITLE_RESULT_DETAIL);
    }

    public void showAbout() {
        popBackStackFull();

        FragmentTransaction ft;
        ft = fm.beginTransaction();

        ft.replace(R.id.fragment_content, new AboutFragment(), AppConstants.PAGE_TITLE_ABOUT);
        ft.addToBackStack(AppConstants.PAGE_TITLE_ABOUT);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

        ft.commit();
        refreshActionBar(AppConstants.PAGE_TITLE_ABOUT);
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
        refreshActionBar(AppConstants.PAGE_TITLE_RESULT_QOS);
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

        refreshActionBar(AppConstants.PAGE_TITLE_TEST_DETAIL_QOS);
    }

    public void showMapFromPager() {
        try {
            Fragment f = getCurrentFragment();
            if (f != null) {
                /* Show map */
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showMap(boolean popBackStack) {
        if (popBackStack) {
            popBackStackFull();
        }

       /* Show map fragment */

        refreshActionBar(AppConstants.PAGE_TITLE_MAP);
    }

    public void showMap(String mapType, LatLng initialCenter, boolean clearFilter, boolean popBackStack) {
        showMap(mapType, initialCenter, clearFilter, -1, popBackStack);
    }

    public void showMap(String mapType, LatLng initialCenter, boolean clearFilter, int viewId, boolean popBackStack) {
        if (popBackStack) {
            popBackStackFull();
        }

        FragmentTransaction ft;

        setCurrentMapType(mapType);

        if (clearFilter) {
            final List<MapListSection> mapFilterListSelectionList = getMapFilterListSelectionList();
            if (mapFilterListSelectionList != null) {
                for (final MapListSection section : mapFilterListSelectionList) {
                    for (final MapListEntry entry : section.getMapListEntryList())
                        entry.setChecked(entry.isDefault());
                }
                updateMapFilter();
            }
        }

        /* show map fragment */
    }

    public void showHelp(final int resource, boolean popBackStack) {
        showHelp(getResources().getString(resource), popBackStack, AppConstants.PAGE_TITLE_HELP);
    }

    public void showHelp(boolean popBackStack) {
        showHelp("", popBackStack, AppConstants.PAGE_TITLE_HELP);
    }

    public void showHelp(final String url, boolean popBackStack, String titleId) {
//        System.err.println("Showing help. Url:" + getResources().getString(R.string.url_help));
//        System.err.println("Label Tat:" + getResources().getString(R.string.terms_accept_text));

        if (popBackStack) {
            popBackStackFull();
        }

        FragmentTransaction ft;


        ft = fm.beginTransaction();

        final Fragment fragment = new HelpFragment();
        final Bundle args = new Bundle();

        args.putString(HelpFragment.ARG_URL, url);
        fragment.setArguments(args);
        ft.replace(R.id.fragment_content, fragment, titleId);
        ft.addToBackStack(titleId);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

        ft.commit();
        refreshActionBar(titleId);
    }

    /**
     *
     */
    public void showSync() {
        FragmentTransaction ft;
        ft = fm.beginTransaction();

       /* Show sync fragment */
        refreshActionBar(AppConstants.PAGE_TITLE_SYNC);
    }

    public void showFilter() {
        final FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft;

        /* Show filter fragment */

        refreshActionBar(AppConstants.PAGE_TITLE_HISTORY_FILTER);
    }

    /**
     *
     */
    public void showSettings() {
        startActivity(new Intent(this, PreferenceActivity.class));
    }


    /**
     *
     */
    public void showStatistics() {
        String urlStatistic = null; // ConfigHelper.getVolatileSetting("url_statistics");
        if (urlStatistic == null || urlStatistic.length() == 0) {
            if ((urlStatistic = ConfigHelper.getCachedStatisticsUrl(getApplicationContext())) == null) {
                return;
            }
        }
        showHelp(urlStatistic, true, AppConstants.PAGE_TITLE_STATISTICS);
    }

    public void showLogFragment() {
       /* Log fragment */
        refreshActionBar(AppConstants.PAGE_TITLE_LOG);
    }


    /**
     *
     */
    private void stopBackgroundProcesses() {
        geoLocation.stop();
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

        if (getMapOptionsInfoTask != null) {
            getMapOptionsInfoTask.cancel(true);
            getMapOptionsInfoTask = null;
        }
        if (historyTask != null) {
            historyTask.cancel(true);
            historyTask = null;
        }
    }

    /**
     * @param history_filter_devices
     * @param history_filter_networks
     */
    public void setSettings(final String[] history_filter_devices, final String[] history_filter_networks) {
        historyFilterDevices = history_filter_devices;
        historyFilterNetworks = history_filter_networks;

        historyFilterDevicesFilter = new ArrayList<String>();
        if (history_filter_devices != null)
            for (final String history_filter_device : history_filter_devices)
                historyFilterDevicesFilter.add(history_filter_device);

        historyFilterNetworksFilter = new ArrayList<String>();
        if (history_filter_networks != null)
            for (final String history_filter_network : history_filter_networks)
                historyFilterNetworksFilter.add(history_filter_network);
    }

    /**
     * @return
     */
    public String[] getHistoryFilterDevices() {
        return historyFilterDevices;
    }

    /**
     * @return
     */
    public String[] getHistoryFilterNetworks() {
        return historyFilterNetworks;
    }

    public void setHistoryFilterDevicesFilter(final ArrayList<String> historyFilterDevicesFilter) {
        this.historyFilterDevicesFilter = historyFilterDevicesFilter;
        historyDirty = true;
    }

    public void setHistoryFilterNetworksFilter(final ArrayList<String> historyFilterNetworksFilter) {
        this.historyFilterNetworksFilter = historyFilterNetworksFilter;
        historyDirty = true;
    }

    /**
     * @return
     */
    public ArrayList<String> getHistoryFilterDevicesFilter() {
        return historyFilterDevicesFilter;
    }

    /**
     * @return
     */
    public ArrayList<String> getHistoryFilterNetworksFilter() {
        return historyFilterNetworksFilter;
    }

    /**
     * @return
     */
    public List<Map<String, String>> getHistoryItemList() {
        return historyItemList;
    }

    /**
     * @return
     */
    public ArrayList<Map<String, String>> getHistoryStorageList() {
        return historyStorageList;
    }

    /**
     *
     */
    @Override
    public Map<String, String> getCurrentMapOptions() {
        return currentMapOptions;
    }

    /**
     *
     */
    public Map<String, String> getCurrentMapOptionTitles() {
        return currentMapOptionTitles;
    }

    // /

    public void setCurrentMapType(MapListEntry currentMapType, MapListSection section) {
        this.currentMapType = currentMapType;

        // set the filter options in activity
        final String uuid = ConfigHelper.getUUID(getApplicationContext());
        currentMapOptions.clear();
        currentMapOptionTitles.clear();
        currentMapOptions.put("highlight", uuid);
        currentMapOptions.put(currentMapType.getKey(), currentMapType.getValue());
        currentMapOptions.put("overlay_type", currentMapType.getOverlayType());
        currentMapOptionTitles.put(currentMapType.getKey(),
                section.getTitle() + ": " + currentMapType.getTitle());

        updateMapFilter();
    }

    public void setCurrentMapType(String mapType) {
        if (mapTypeListSectionList == null || mapType == null)
            return;
        for (final MapListSection section : mapTypeListSectionList) {
            for (MapListEntry entry : section.getMapListEntryList()) {
                if (entry.getValue().equals(mapType)) {
                    setCurrentMapType(entry, section);
                    return;
                }
            }
        }
    }

    public MapListEntry getCurrentMapType() {
        return currentMapType;
    }

    public String getCurrentMainMapType() {
        final String mapTypeString = currentMapType == null ? null : currentMapType.getValue();
        String part = null;
        if (mapTypeString != null) {
            final String[] parts = mapTypeString.split("/");
            part = parts[0];
        }
        return part;
    }

    public void setMapFilterCountries(MapFilterCountries mapFilterCountries) {
        this.mapFilterCountries = mapFilterCountries;
    }

    public MapFilterCountries getMapFilterCountries() {
        return this.mapFilterCountries;
    }

    /**
     * @return
     */
    public List<MapListSection> getMapTypeListSectionList() {
        return mapTypeListSectionList;
    }

    /**
     * @param mapTypeListSectionList
     */
    public void setMapTypeListSectionList(final ArrayList<MapListSection> mapTypeListSectionList) {
        this.mapTypeListSectionList = mapTypeListSectionList;
    }

    /**
     * @return
     */
    public Map<String, List<MapListSection>> getMapFilterListSectionListMap() {
        return mapFilterListSectionListMap;
    }

    public void getOperatorsForCountry(String countryCode) {
        String operatorType = getFilterOperatorType();
        new GetMapOptionsProvidersTask(this).execute(countryCode, operatorType);
    }

    @NonNull
    public String getFilterOperatorType() {
        String operatorType = MapFilterTypes.MAP_FILTER_TYPE_MOBILE;
        Map<String, String> currentMapOptions = getCurrentMapOptions();
        if (currentMapOptions != null) {
            String map_options = currentMapOptions.get("map_options");
            if (map_options != null) {
                if (map_options.contains("mobile")) {
                    operatorType = MapFilterTypes.MAP_FILTER_TYPE_MOBILE;
                } else if (map_options.contains("wifi")) {
                    operatorType = MapFilterTypes.MAP_FILTER_TYPE_WLAN;
                } else if (map_options.contains("browser")) {
                    operatorType = MapFilterTypes.MAP_FILTER_TYPE_BROWSER;
                } else if (map_options.contains("all")) {
                    operatorType = MapFilterTypes.MAP_FILTER_TYPE_ALL;
                }
            }
        }
        return operatorType;
    }

    public List<MapListSection> getMapFilterListSelectionList() {

        final Map<String, List<MapListSection>> mapFilterListSectionListMap = getMapFilterListSectionListMap();
        if (mapFilterListSectionListMap == null)
            return null;
        return mapFilterListSectionListMap.get(getCurrentMainMapType());
    }

    /**
     * @param mapFilterListSectionList
     */
    public void setMapFilterListSectionListMap(final HashMap<String, List<MapListSection>> mapFilterListSectionList) {
        this.mapFilterListSectionListMap = mapFilterListSectionList;
        updateMapFilter();
    }

    public void updateMapFilter() {
        final List<MapListSection> mapFilterListSelectionList = getMapFilterListSelectionList();
        if (mapFilterListSelectionList == null)
            return;
        for (final MapListSection section : mapFilterListSelectionList) {
            final MapListEntry entry = section.getCheckedMapListEntry();

            if (entry != null && entry.getKey() != null && entry.getValue() != null) {
                getCurrentMapOptions().put(entry.getKey(), entry.getValue());
                getCurrentMapOptionTitles().put(entry.getKey(),
                        section.getTitle() + ": " + entry.getTitle());
            }
        }

        if (mapFilterOperatorList != null) {
            MapListSection mapListSection = mapFilterOperatorList.convertOperatorsToMapListSection(getApplicationContext());
            MapListEntry checkedMapListEntry = mapListSection.getCheckedMapListEntry();
            if (checkedMapListEntry != null && checkedMapListEntry.getKey() != null && checkedMapListEntry.getValue() != null) {
                getCurrentMapOptions().put(checkedMapListEntry.getKey(), checkedMapListEntry.getValue());
                getCurrentMapOptionTitles().put(checkedMapListEntry.getKey(),
                        mapListSection.getTitle() + ": " + checkedMapListEntry.getTitle());
            }
        }
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

    public void setOperatorList(FilterGroup result) {
        this.mapFilterOperatorList = result;
    }


    /**
     * @author
     */
    private class MainGeoLocation extends GeoLocation {
        /**
         * @param context
         */
        public MainGeoLocation(final Context context) {
            super(context, ConfigHelper.isGPS(context));
        }

        /**
         *
         */
        @Override
        public void onLocationChanged(final Location curLocation) {
        }
    }


    /**
     * @return
     */
    public boolean isMapFirstRun() {
        return mapFirstRun;
    }

    /**
     * @param mapFirstRun
     */
    public void setMapFirstRun(final boolean mapFirstRun) {
        this.mapFirstRun = mapFirstRun;
    }

    /**
     *
     */
    @Override
    public void onBackPressed() {
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (drawerLayout.isDrawerOpen(drawerList) && !exitAfterDrawerClose) {
            drawerLayout.closeDrawer(drawerList);
            return;
        }

//
//        final RMBTNDTCheckFragment ndtCheckFragment = (RMBTNDTCheckFragment) getSupportFragmentManager().findFragmentByTag("ndt_check");
//        if (ndtCheckFragment != null)
//            if (ndtCheckFragment.onBackPressed())
//                return;


        final TermsCheckFragment tcFragment = (TermsCheckFragment) getSupportFragmentManager().findFragmentByTag("terms_check");
        if (tcFragment != null && tcFragment.isResumed()) {
            if (tcFragment.onBackPressed())
                return;
        }


//        final RMBTTestFragment testFragment = (RMBTTestFragment) getSupportFragmentManager().findFragmentByTag("test");
//        if (testFragment != null && testFragment.isResumed()) {
//            if (testFragment.onBackPressed())
//                return;
//        }

        final MainMenuFragment mainMenuCodeFragment = (MainMenuFragment) getSupportFragmentManager()
                .findFragmentByTag(AppConstants.PAGE_TITLE_MAIN);
        if (mainMenuCodeFragment != null && mainMenuCodeFragment.isResumed()) {
            if (mainMenuCodeFragment.onBackPressed())
                return;
        }

        refreshActionBarAndTitle();

        if (getSupportFragmentManager().getBackStackEntryCount() > 0 || exitAfterDrawerClose) {
            super.onBackPressed();
        } else {
            System.out.println(getCurrentFragment());
            super.onBackPressed();
        }
    }

    private void refreshActionBarAndTitle() {
        title = getTitle(getPreviousFragmentName());
        refreshActionBar(title);
    }

    /**
     * @return
     */
    public boolean isHistoryDirty() {
        return historyDirty;
    }

    /**
     * @param historyDirty
     */
    public void setHistoryDirty(final boolean historyDirty) {
        this.historyDirty = historyDirty;
    }

    /**
     * @author bp
     */
    public interface HistoryUpdatedCallback {
        public final static int SUCCESSFUL = 0;
        public final static int LIST_EMPTY = 1;
        public final static int ERROR = 2;

        public void historyUpdated(int status);
    }

    /**
     * @param callback
     */
    public void updateHistory(final HistoryUpdatedCallback callback) {
        if (historyDirty
                && (historyTask == null || historyTask.isCancelled() || historyTask.getStatus() == AsyncTask.Status.FINISHED)) {
            historyTask = new CheckHistoryTask(this, historyFilterDevicesFilter, historyFilterNetworksFilter);

            historyTask.setEndTaskListener(new EndTaskListener() {
                @Override
                public void taskEnded(final JsonArray resultList) {
                    if (resultList != null && resultList.size() > 0 && !historyTask.hasError()) {
                        historyStorageList.clear();
                        historyItemList.clear();

                        final Date tmpDate = new Date();
                        final DateFormat dateFormat = Helperfunctions.getDateFormat(false);

                        for (int i = 0; i < resultList.size(); i++) {

                            JsonObject resultListItem;
                            try {
                                resultListItem = resultList.get(i).getAsJsonObject();

                                final HashMap<String, String> storageItem = new HashMap<String, String>();
                                String testUUID = null;
                                if (resultListItem.has("test_uuid")) {
                                    testUUID = resultListItem.get("test_uuid").getAsString();
                                }
                                storageItem.put("test_uuid", testUUID);

                                Long time = 0l;
                                if (resultListItem.has("time")) {
                                    time = resultListItem.get("time").getAsLong();
                                }
                                storageItem.put("time", String.valueOf(time));

                                String timezone = null;
                                if (resultListItem.has("timezone")) {
                                    timezone = resultListItem.get("timezone").getAsString();
                                }
                                storageItem.put("timezone", timezone);
                                historyStorageList.add(storageItem);

                                final HashMap<String, String> viewItem = new HashMap<String, String>();
                                // viewIitem.put( "device",
                                // resultListItem.optString("plattform","none"));

                                String model = " - ";
                                if (resultListItem.has("model")) {
                                    model = resultListItem.get("model").getAsString();
                                }
                                viewItem.put("device", model);

                                String type = "";
                                if (resultListItem.has("network_type")) {
                                    type = resultListItem.get("network_type").getAsString();
                                }
                                viewItem.put("type", type);

                                Long time2 = 0l;
                                if (resultListItem.has("time")) {
                                    time2 = resultListItem.get("time").getAsLong();
                                }
                                String timezone2 = null;
                                if (resultListItem.has("timezone")) {
                                    timezone2 = resultListItem.get("timezone").getAsString();
                                }
                                final String timeString = Helperfunctions.formatTimestampWithTimezone(tmpDate,
                                        dateFormat, time2, timezone2);

                                viewItem.put("date", timeString == null ? " - " : timeString);

                                String speedResult = " - ";
                                if (resultListItem.has("speed_download")) {
                                    speedResult = resultListItem.get("speed_download").getAsString();
                                }
                                viewItem.put("down", speedResult);


                                String speedUpload = " - ";
                                if (resultListItem.has("speed_upload")) {
                                    speedUpload = resultListItem.get("speed_upload").getAsString();
                                }
                                viewItem.put("up", speedUpload);

                                String ping = " - ";
                                if (resultListItem.has("ping")) {
                                    ping = resultListItem.get("ping").getAsString();
                                }
                                viewItem.put("ping", ping);

                                String meanPacketLossInPercent = " - ";
                                String meanJitter = "-";

                                if (resultListItem.has("jpl")) {
                                    Gson gson = new Gson();
                                    VoipTestResult jpl = gson.fromJson(resultListItem.get("jpl"), VoipTestResult.class);

                                    meanPacketLossInPercent = jpl.getVoipResultPacketLoss();
                                    meanJitter = jpl.getVoipResultJitter();
                                }

                                String networkName = " - ";
                                if (resultListItem.has("network_name")) {
                                    networkName = resultListItem.get("network_name").getAsString();
                                }

                                String quality_result = " - ";
                                if (resultListItem.has("qos_result")) {
                                    quality_result = resultListItem.get("qos_result").getAsString();
                                }

                                viewItem.put("packet_loss", meanPacketLossInPercent);
                                viewItem.put("jitter", meanJitter);
                                viewItem.put("quality", quality_result);
                                viewItem.put("network_name", networkName);

                                historyItemList.add(viewItem);
                            } catch (final JsonParseException e) {
                                e.printStackTrace();
                            }
                        }
                        historyDirty = false;
                        if (callback != null)
                            callback.historyUpdated(HistoryUpdatedCallback.SUCCESSFUL);
                    } else if (callback != null) {
                        callback.historyUpdated(historyTask.hasError() ? HistoryUpdatedCallback.ERROR : HistoryUpdatedCallback.LIST_EMPTY);
                    }
                }
            });
            historyTask.execute();
        } else if (callback != null)
            callback.historyUpdated(!(historyStorageList.isEmpty() && historyStorageList.isEmpty()) ? HistoryUpdatedCallback.SUCCESSFUL : HistoryUpdatedCallback.LIST_EMPTY);
    }

    public void setMapOverlayType(final MapOverlay mapOverlayType) {
        this.mapOverlayType = mapOverlayType;
    }

    public MapOverlay getMapOverlayType() {
        return mapOverlayType;
    }

    /**
     * @return
     */
    public int getHistoryResultLimit() {
        return historyResultLimit;
    }

    /**
     * @param limit
     */
    public void setHistoryResultLimit(final int limit) {
        historyResultLimit = limit;
    }

    public void setMapTypeSatellite(boolean mapTypeSatellite) {
        this.mapTypeSatellite = mapTypeSatellite;
    }

    public boolean getMapTypeSatellite() {
        return mapTypeSatellite;
    }

    public void popBackStackFull() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (fm.getBackStackEntryCount() > 0) {
                    fm.popBackStack(fm.getBackStackEntryAt(0).getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }

                try {
                    MainMenuFragment currentFragment = (MainMenuFragment) getCurrentFragment();
                    if (currentFragment != null) {
                        boolean loopModeRunning = isLoopModeRunning();
                        if (loopModeRunning) {
                            currentFragment.changeScreenState(MainScreenState.LOOP_MODE_ACTIVE, "Main Activity - popBackStackFull", true);
                        } else {
                            currentFragment.changeScreenState(MainScreenState.DEFAULT, "Main Activity - popBackStackFull", true);
                        }

                    }
                } catch (ClassCastException e) {
                    //DO nothing
                }

                refreshActionBarAndTitle();
            }
        });
    }

    /**
     * @param toFile
     */
    public void redirectSystemOutput(boolean toFile) {
        try {
            if (toFile) {
                Log.i(DEBUG_TAG, "redirecting sysout to file");
                //Redirecting console output and runtime exceptions to file (System.out.println)
                File f = new File(Environment.getExternalStorageDirectory(), "qosdebug");
                if (!f.exists()) {
                    f.mkdir();
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
                Log.i(DEBUG_TAG, "redirecting sysout to default");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

    /**
     * @return
     */
    public String getCurrentFragmentName() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            String fragmentTag = getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1).getName();
            return fragmentTag;
        }

        Fragment f = getSupportFragmentManager().findFragmentByTag(AppConstants.PAGE_TITLE_MAIN);
        return f != null ? AppConstants.PAGE_TITLE_MAIN : null;
    }

    /**
     * @return
     */
    protected String getPreviousFragmentName() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            String fragmentTag = getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 2).getName();
            return fragmentTag;
        }

        return null;
    }

    /**
     * @return
     */
    protected String getTitle(String fragmentName) {
        String name = fragmentName; // (fragmentName != null ? fragmentName : getCurrentFragmentName());
        Integer id = null;
        if (name != null)
            id = AppConstants.TITLE_MAP.get(name);

        if (id != null) {
//    	    id = R.string.page_title_title_page;
            title = getResources().getString(id);
        } else {
            title = "";
        }

        return title;
    }

    public void setLockNavigationDrawer(boolean isLocked) {
        if (isLocked) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        } else {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }
    }

    public void refreshActionBar(String name) {
        if (name == null && title == null) {
            toolbar.setTitle(getTitle(getCurrentFragmentName()));
//    		getActionBar().setTitle(getTitle(getCurrentFragmentName()));
        } else {
            if (name == null) {
                toolbar.setTitle("");
            }
            toolbar.setTitle((name != null || title == null) ? getTitle(name) : title);
//    		getActionBar().setTitle((name != null || title == null) ? getTitle(name) : title);
        }

        if (actionBarMenu != null) {
            if (AppConstants.PAGE_TITLE_HISTORY.equals(name)) {
                setVisibleMenuItems(R.id.action_menu_filter, R.id.action_menu_sync);
            }
            /*
            //enable this option only if a logo/icon is present
    		else if (AppConstants.PAGE_TITLE_ABOUT.equals(name)) {
    			setVisibleMenuItems(R.id.action_menu_rtr);
    		}
    		*/
            else {
                setVisibleMenuItems();
            }
        }
    }

    /**
     * @param id
     */
    public void setVisibleMenuItems(Integer... id) {
        if (actionBarMenu != null) {
            if (id != null && id.length > 0) {
                Set<Integer> idSet = new HashSet<Integer>();
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
     * @return
     */
    public NetworkInfoCollector getNetworkInfoCollector() {
        return this.networkInfoCollector;
    }

    /*
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
     * @param context
     * @return
     */
    public static boolean isNetworkAvailable(Context context) {
        boolean isMobile = false, isWifi = false;

        try {
            NetworkInfo[] infoAvailableNetworks = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getAllNetworkInfo();

            if (infoAvailableNetworks != null) {
                for (NetworkInfo network : infoAvailableNetworks) {

                    if (network.getType() == ConnectivityManager.TYPE_WIFI) {
                        if (network.isConnected() && network.isAvailable())
                            isWifi = true;
                    }
                    if (network.getType() == ConnectivityManager.TYPE_MOBILE) {
                        if (network.isConnected() && network.isAvailable())
                            isMobile = true;
                    }
                }
            }

            return isMobile || isWifi;
        } catch (Exception e) {
            return false;
        }
    }
}
