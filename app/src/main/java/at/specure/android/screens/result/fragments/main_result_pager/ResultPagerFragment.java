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
package at.specure.android.screens.result.fragments.main_result_pager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.specure.opennettest.R;

import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;
import at.specure.android.base.BaseFragment;
import at.specure.android.configs.TestConfig;
import at.specure.android.screens.main.MainActivity;
import at.specure.android.screens.main.main_activity_interfaces.ExpandedResultInterface;
import at.specure.android.screens.main.main_activity_interfaces.HelpInterface;
import at.specure.android.screens.main.main_activity_interfaces.MapInterface;
import at.specure.android.screens.result.adapter.result.OnCompleteListener;
import at.specure.android.screens.result.adapter.result.OnDataChangedListener;
import at.specure.android.screens.result.adapter.result.ResultPagerAdapter;
import at.specure.android.views.ExtendedViewPager;

import static at.specure.android.screens.result.fragments.main_result_pager.ResultPagerController.ARG_TEST_UUID;
import static at.specure.android.screens.result.fragments.main_result_pager.ResultPagerController.MAP_INDICATOR_DYNAMIC_VISIBILITY;


public class ResultPagerFragment extends BaseFragment implements ViewPager.OnPageChangeListener, ResultPagerInterface {
    /**
     * use this flag to make the map indicator visible only if coordinates are available
     */

    private ResultPagerAdapter pagerAdapter;
    private ExtendedViewPager viewPager;
    private TabHost tabHost;
    private HorizontalScrollView scroller;
    private String uuid;
    private Bundle bundle;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle args = getArguments();
        uuid = args.getString(ARG_TEST_UUID);
        System.out.println("ResultPagerFragment: test uuid: " + uuid);
        FragmentActivity activity = getActivity();
        if (activity != null) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if ((uuid == null) && (savedInstanceState != null)) {
            uuid = savedInstanceState.getString(ARG_TEST_UUID, null);
        }
        View v = inflater.inflate(R.layout.result_tabhost_pager, container, false);
        TestConfig.setShouldShowResults(false);
        return createView(v);
    }

    private View createView(View v) {
        tabHost = v.findViewById(android.R.id.tabhost);
        tabHost.setup();
        scroller = v.findViewById(R.id.tabwidget_scrollview);
        viewPager = v.findViewById(R.id.pager);
        return v;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (pagerAdapter != null) {
            pagerAdapter.mapboxOnStop();
        }
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ARG_TEST_UUID, uuid);
        if (pagerAdapter != null) {
            pagerAdapter.mapboxOnSaveInstanceState(outState);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (pagerAdapter != null) {
            pagerAdapter.mapboxOnLowMemory();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (pagerAdapter != null) {
            pagerAdapter.onPause();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bundle = savedInstanceState;
        setActionBarItems(getActivity());
        fillData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (pagerAdapter != null) {
            pagerAdapter.destroy();
        }
    }

    public void setCurrentPosition(final int pos) {
        viewPager.setCurrentItem(pos);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int index) {
        tabHost.setCurrentTab(index);
        scrollToTabTab(index);
        setActionBarItems(getActivity());
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public void scrollToTabTab(int scrollToPosition) {
        if (scroller != null && tabHost != null && tabHost.getTabWidget() != null) {
            int startX = (scroller.getWidth() / 2);
            scroller.scrollTo(tabHost.getTabWidget().getChildAt(0).getWidth() * scrollToPosition - startX, 0);
        }
    }

    public ExtendedViewPager getViewPager() {
        return viewPager;
    }

    public ResultPagerAdapter getPagerAdapter() {
        return pagerAdapter;
    }

    @Override
    public void setActionBarItems(Context context) {
        System.out.println("SET ACTIONBAR ITEMS");
        if ((viewPager != null) && (isAdded())) {
            switch (viewPager.getCurrentItem()) {
                case ResultPagerAdapter.RESULT_PAGE_MAP:
                    ((MainActivity) getActivity()).setVisibleMenuItems(R.id.action_menu_map);
                    break;
                default:
                    ((MainActivity) getActivity()).setVisibleMenuItems();
                    break;
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        populateViewForOrientation(inflater, (ViewGroup) getView());
    }

    private void populateViewForOrientation(LayoutInflater inflater, ViewGroup view) {
        view.removeAllViewsInLayout();
        View v = inflater.inflate(R.layout.result_tabhost_pager, view);
        createView(v);
        fillData();
    }


    @Override
    public void onResume() {
        super.onResume();
        FragmentActivity activity = getActivity();
    }

    private void fillData() {
        final FragmentActivity activity = getActivity();
        if (activity != null) {
            ((MainActivity) activity).updateTitle(setActionBarTitle());
        }
        ResultPagerController resultPagerController = new ResultPagerController(this);
        tabHost.setOnTabChangedListener(resultPagerController);

        pagerAdapter = resultPagerController.getPagerAdapter(activity, uuid, (MapInterface) activity, (HelpInterface) activity, (ExpandedResultInterface) activity, bundle);
        if (pagerAdapter != null) {
            pagerAdapter.setOnCompleteListener(new OnCompleteListener() {
                @SuppressLint("InflateParams")
                @Override
                public void onComplete(int flag, Object object) {
                    if (pagerAdapter.getCount() > tabHost.getTabWidget().getChildCount()) {
                        if (activity != null) {
                            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                            for (int i = tabHost.getTabWidget().getChildCount(); i < pagerAdapter.getCount(); i++) {
                                TabSpec tab = tabHost.newTabSpec(String.valueOf(i));
                                tab.setContent(android.R.id.tabcontent);

                                View indicator = null;
                                TextView title;
                                if (inflater != null) {
                                    indicator = inflater.inflate(R.layout.tabhost_indicator, null);
                                    if (indicator != null) {
                                        title = indicator.findViewById(android.R.id.title);
                                        title.setText(activity.getResources().getStringArray(R.array.result_page_title)[ResultPagerAdapter.getTitleMapping(getContext()).get(i)]);
                                    }
                                }
                                tab.setIndicator(indicator);
                                tabHost.addTab(tab);
                            }
                        }
                    }
                }
            });

            LayoutInflater inflater = null;
            if (activity != null) {
                inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }
            if (inflater != null) {
                for (int i = 0; i < pagerAdapter.getCount(); i++) {
                    TabSpec tab = tabHost.newTabSpec(String.valueOf(i));
                    tab.setContent(android.R.id.tabcontent);

                    @SuppressLint("InflateParams") View indicator = inflater.inflate(R.layout.tabhost_indicator, null);
                    TextView title = indicator.findViewById(android.R.id.title);
                    title.setText(getActivity().getResources().getStringArray(R.array.result_page_title)[ResultPagerAdapter.getTitleMapping(getContext()).get(i)]);

                    if (MAP_INDICATOR_DYNAMIC_VISIBILITY) {
                        if (i == ResultPagerAdapter.RESULT_PAGE_MAP) {
                            indicator.setVisibility(View.GONE);
                        }
                    }
                    tab.setIndicator(indicator);
                    tabHost.addTab(tab);
                }
            }
            viewPager.setAdapter(pagerAdapter);
            viewPager.addOnPageChangeListener(this);

            setCurrentPosition(0);
        }

        if (MAP_INDICATOR_DYNAMIC_VISIBILITY) {
            pagerAdapter.setOnDataChangedListener(new OnDataChangedListener() {

                @Override
                public void onChange(Object oldValue, Object newValue, Object flag) {
                    if (flag.equals("HAS_MAP")) {
                        boolean b = (Boolean) newValue;
                        if (b) {
                            tabHost.getTabWidget().getChildTabViewAt(ResultPagerAdapter.RESULT_PAGE_MAP).setVisibility(View.VISIBLE);
                        } else {
                            tabHost.getTabWidget().getChildTabViewAt(ResultPagerAdapter.RESULT_PAGE_MAP).setVisibility(View.GONE);
                        }
                    }
                }
            });
        }
        setActionBarItems(getActivity());
    }

    @Override
    public String setActionBarTitle() {
        if (this.isAdded())
            return getString(R.string.page_title_main_result);
        else return "";
    }

    @Override
    public MapInterface getMapInterface() {
        FragmentActivity activity = getActivity();
        if (activity != null) {
            return (MapInterface) activity;
        }
        return null;
    }
}
