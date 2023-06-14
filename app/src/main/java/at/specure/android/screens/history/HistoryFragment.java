/*
 Copyright 2013-2015 alladin-IT GmbH

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
package at.specure.android.screens.history;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.specure.opennettest.R;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import at.specure.android.base.BaseFragment;
import at.specure.android.configs.PrivacyConfig;
import at.specure.android.screens.main.MainActivity;
import timber.log.Timber;

public class HistoryFragment extends BaseFragment implements HistoryFragmentInterface {

    private static final String DEBUG_TAG = "HistoryFragment";
    private MainActivity activity;
    private View mRoot;
    private RecyclerView listView;
    private int listViewIdx;
    private int listViewTop;
    private ProgressBar progressBar;
    private TextView emptyView;
    private int orientation;
    private View content;
    private HistoryFragmentController historyFragmentController;
    private Button openSettingsButton;
    private Boolean started = null;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = (MainActivity) getActivity();
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        orientation = getScreenOrientation(activity);
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setRetainInstance(false);

        if (savedInstanceState != null) {
            listViewIdx = savedInstanceState.getInt("listViewIdx", listViewIdx);
            listViewTop = savedInstanceState.getInt("listViewTop", listViewTop);
            Timber.d("loaded: idx: %s top: %s", listViewIdx, listViewTop);
        }

        mRoot = inflater.inflate(R.layout.history_container, container, false);
        historyFragmentController = new HistoryFragmentController(this);
        return mRoot;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.init();
    }

    public int getScreenOrientation(Activity activity) {
        orientation = Configuration.ORIENTATION_PORTRAIT;
        if (activity != null) {
            Display getOrient = activity.getWindowManager().getDefaultDisplay();
            Point resolution = new Point();
            getOrient.getSize(resolution);
            if (resolution.x == resolution.y) {
                orientation = Configuration.ORIENTATION_SQUARE;
            } else {
                if (resolution.x < resolution.y) {
                    orientation = Configuration.ORIENTATION_PORTRAIT;
                } else {
                    orientation = Configuration.ORIENTATION_LANDSCAPE;
                }
            }
        }
        return orientation;
    }

    private void init() {
        setLayout();
    }

    @Override
    public void setRetainInstance(boolean retain) {
        super.setRetainInstance(false);
    }

    @SuppressLint("InflateParams")
    private void setLayout() {

        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = null;
        int orientation = getScreenOrientation(getActivity());
        if (orientation == Configuration.ORIENTATION_LANDSCAPE)
            view = layoutInflater.inflate(R.layout.history_land, null);
        else if (orientation == Configuration.ORIENTATION_PORTRAIT)
            view = layoutInflater.inflate(R.layout.history, null);

        if (orientation == Configuration.ORIENTATION_LANDSCAPE || orientation == Configuration.ORIENTATION_PORTRAIT) {
            ViewGroup viewGroup = this.mRoot.findViewById(R.id.historyListBox);
            viewGroup.removeAllViews();
            viewGroup.addView(view, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
            this.initComponent();
        }
    }


    private void initComponent() {
        content = mRoot.findViewById(R.id.history_fragment__content);
        listView = mRoot.findViewById(R.id.historyList);
        emptyView = mRoot.findViewById(R.id.infoText);
        openSettingsButton = mRoot.findViewById(R.id.infoButton);
        progressBar = mRoot.findViewById(R.id.progressBar);

        TextView pingText = mRoot.findViewById(R.id.col2);
        TextView downloadText = mRoot.findViewById(R.id.col3);
        TextView uploadText = mRoot.findViewById(R.id.col4);
        TextView packetLossText = mRoot.findViewById(R.id.col5);
        TextView jitterText = mRoot.findViewById(R.id.col6);

        String ping = getResources().getText(R.string.history_ping).toString();
        ping = ping.split(" ")[0];
        pingText.setText(ping);

        String download = getResources().getText(R.string.history_down).toString();
        download = download.split(" ")[0];
        downloadText.setText(download);

        String upload = getResources().getText(R.string.history_up).toString();
        upload = upload.split(" ")[0];
        uploadText.setText(upload);

        if (packetLossText != null) {
            String packetLoss = getResources().getText(R.string.history_packet_loss).toString();
            packetLoss = packetLoss.split(":")[0];
            packetLossText.setText(packetLoss);
        }

        if (jitterText != null) {
            String jitter = getResources().getText(R.string.history_jitter).toString();
            jitter = jitter.split(":")[0];
            jitterText.setText(jitter);
        }

        openSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity mainActivity = getMainActivity();
                if (mainActivity != null) {
                    mainActivity.showSettings();
                }
            }
        });

    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        orientation = newConfig.orientation;
        this.setLayout();
        initViews();
    }

    private void saveListViewState() {
        if ((listView != null) && (listView.getLayoutManager() != null)) {
            listViewIdx = ((LinearLayoutManager) listView.getLayoutManager()).findFirstVisibleItemPosition();
            final View v = listView.getChildAt(0);
            listViewTop = v == null ? 0 : v.getTop();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        if (listView != null) {
            saveListViewState();
            outState.putInt("listViewIdx", listViewIdx);
            outState.putInt("listViewTop", listViewTop);
//            outState.putInt("orientation", orientation);
            outState.putBoolean("uuid_mod_state", PrivacyConfig.isClientUUIDPersistent(getContext().getApplicationContext()));
            Timber.d("saved: idx: %s top: %s", listViewIdx, listViewTop);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }

    @Override
    public void onResume() {
        super.onResume();
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        this.setLayout();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Context applicationContext = null;
                try {
                    applicationContext = getContext().getApplicationContext();
                } catch (Exception e) {
                    Timber.e(e);
                }
                if (applicationContext != null) {

//                    if (started != null && PrivacyConfig.isClientUUIDPersistent(applicationContext) != started) {
                        initViews();
//                    }
//                    started = Boolean.valueOf(PrivacyConfig.isClientUUIDPersistent(applicationContext));
                }
            }
        }, 1000);


    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        boolean uuid_mod_state = true;
        if (savedInstanceState != null) {
            uuid_mod_state = savedInstanceState.getBoolean("uuid_mod_state", true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
//        started = false;
    }

    private void initViews() {
        content.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        openSettingsButton.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        if (!PrivacyConfig.isClientUUIDPersistent(getContext().getApplicationContext())) {
            progressBar.setVisibility(View.GONE);
            openSettingsButton.setVisibility(View.VISIBLE);
        } else {
            historyFragmentController.updateHistory();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        saveListViewState();
    }

    @Override
    public String setActionBarTitle() {
        if (this.isAdded())
            return getString(R.string.page_title_history);
        else return "";
    }

    @Override
    public void setActionBarItems(Context context) {
        if (PrivacyConfig.isClientUUIDPersistent(context.getApplicationContext())) {
            ((MainActivity) context).setVisibleMenuItems(R.id.action_menu_filter, R.id.action_menu_sync);
        } else {
            ((MainActivity) context).setVisibleMenuItems(R.id.action_menu_filter);
        }
    }


    @Override
    public MainActivity getMainActivity() {
        if (this.isAdded())
            return (MainActivity) this.getActivity();
        else
            return null;
    }

    @Override
    public int getOrientation() {
        return orientation;
    }

    @Override
    public void dataSuccessfullyLoaded(HistoryAdapter historyList) {
        listView.setLayoutManager(new LinearLayoutManager(getMainActivity()));
        listView.setAdapter(historyList);
        listView.scrollToPosition(listViewTop);
        listView.invalidate();
        Timber.d("LOADED OK, %s  set: idx: %s top: %s", historyList.getItemCount(), listViewIdx, listViewTop);
        progressBar.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);
        openSettingsButton.setVisibility(View.GONE);
        content.setVisibility(View.VISIBLE);
    }

    @Override
    public void dataSuccessfullyLoadedEmpty() {
        progressBar.setVisibility(View.GONE);
        openSettingsButton.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
        emptyView.setText(getString(R.string.error_no_data));
        emptyView.invalidate();
        Timber.d("LOADED OK, EMPTY            set: idx: %s top: %s", listViewIdx, listViewTop);
    }

    @Override
    public void dataError() {
        progressBar.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
        openSettingsButton.setVisibility(View.GONE);
        emptyView.setText(getString(R.string.error_history_no_data_no_connection));
        emptyView.invalidate();
    }
}
