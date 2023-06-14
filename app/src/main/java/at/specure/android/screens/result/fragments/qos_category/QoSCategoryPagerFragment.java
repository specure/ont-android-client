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
package at.specure.android.screens.result.fragments.qos_category;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParseException;
import com.specure.opennettest.R;

import androidx.viewpager.widget.ViewPager;
import at.specure.android.base.BaseFragment;
import at.specure.android.screens.main.MainActivity;
import at.specure.android.screens.result.adapter.result.QoSCategoryPagerAdapter;
import at.specure.android.views.ExtendedViewPager;
import at.specure.client.v2.task.result.QoSServerResult.DetailType;
import at.specure.client.v2.task.result.QoSServerResultCollection;

/**
 * Pager Fragment that manages all qos test category fragments
 * @author lb
 */
public class QoSCategoryPagerFragment extends BaseFragment implements ViewPager.OnPageChangeListener, OnTabChangeListener {

    public final static String BUNDLE_QOS_RESULTS = "qosresults";
    public final static String BUNDLE_DETAIL_TYPE = "detailtype";
    private QoSServerResultCollection results;
    private DetailType detailType;
    private QoSCategoryPagerAdapter pagerAdapter;
    private ExtendedViewPager viewPager;
    private Handler handler = new Handler();
    private Integer initPosition;
    private TabHost tabHost;
    private HorizontalScrollView scroller;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //final Bundle args = getArguments();
        if (savedInstanceState != null && savedInstanceState.containsKey(BUNDLE_QOS_RESULTS)) {
            try {
                String json = savedInstanceState.getString(BUNDLE_QOS_RESULTS);
                JsonArray jsonArray = new Gson().fromJson(json, JsonArray.class);
                setQoSResult(new QoSServerResultCollection(jsonArray));
                setDetailType(DetailType.valueOf(savedInstanceState.getString(BUNDLE_DETAIL_TYPE)));
            } catch (JsonParseException e) {
                //e.printStackTrace();
            }
        }
        pagerAdapter = new QoSCategoryPagerAdapter((MainActivity) getActivity(), handler, results);
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View v = inflater.inflate(R.layout.result_tabhost_pager, container, false);
        tabHost = v.findViewById(android.R.id.tabhost);
        tabHost.setup();
        tabHost.setOnTabChangedListener(this);

        for (int i = 0; i < pagerAdapter.getCount(); i++) {
            TabSpec tab = tabHost.newTabSpec(String.valueOf(i));
            //tab.setIndicator(getActivity().getResources().getStringArray(R.array.result_page_title)[i]);
            tab.setContent(android.R.id.tabcontent);

            @SuppressLint("InflateParams") View indicator = inflater.inflate(R.layout.tabhost_indicator, null);
            TextView title = indicator.findViewById(android.R.id.title);
            title.setText(pagerAdapter.getPageTitle(i));
            tab.setIndicator(indicator);
            tabHost.addTab(tab);
        }

        viewPager = v.findViewById(R.id.pager);
        viewPager.setAdapter(pagerAdapter);

        viewPager.addOnPageChangeListener(this);
        setCurrentPosition(0);

        scroller = v.findViewById(R.id.tabwidget_scrollview);

        if (initPosition != null) {
            viewPager.setCurrentItem(initPosition);
        }

        return v;
    }

    public void setQoSResult(QoSServerResultCollection results) {
        this.results = results;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public void setCurrentPosition(final int pos) {
        if (viewPager != null) {
            viewPager.setCurrentItem(pos);
        } else {
            initPosition = pos;
        }
    }

    public void setDetailType(DetailType detailType) {
        this.detailType = detailType;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(BUNDLE_QOS_RESULTS, results.getTestResultArray().toString());
        outState.putString(BUNDLE_DETAIL_TYPE, detailType.toString());
    }

    @Override
    public void onTabChanged(String tabId) {
        int tabIndex = Integer.valueOf(tabId);
        if (viewPager != null && (tabIndex != viewPager.getCurrentItem())) {
            viewPager.setCurrentItem(tabIndex);
            scrollToTabTab(tabIndex);
        }
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onPageSelected(int arg0) {
        tabHost.setCurrentTab(arg0);
        scrollToTabTab(arg0);
    }

    private void scrollToTabTab(int scrollToPosition) {
        if (scroller != null && tabHost != null && tabHost.getTabWidget() != null) {
            int startX = (scroller.getWidth() / 2);
            scroller.scrollTo(tabHost.getTabWidget().getChildAt(0).getWidth() * scrollToPosition - startX, 0);
        }
    }

    @Override
    public String setActionBarTitle() {
        if (this.isAdded())
            return getString(R.string.page_title_qos_result);
        else return "";
    }
}
