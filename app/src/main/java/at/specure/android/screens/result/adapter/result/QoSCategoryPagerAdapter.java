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

import android.content.Context;
import android.os.Handler;
import androidx.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import com.specure.opennettest.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.viewpager.widget.PagerAdapter;
import at.specure.android.configs.ConfigHelper;
import at.specure.android.screens.main.MainActivity;
import at.specure.android.screens.result.views.QoSCategoryView;
import at.specure.client.v2.task.result.QoSServerResult;
import at.specure.client.v2.task.result.QoSServerResultCollection;
import at.specure.client.v2.task.result.QoSServerResultDesc;
import at.specure.client.v2.task.result.QoSTestResultEnum;

public class QoSCategoryPagerAdapter extends PagerAdapter {

	public final static HashMap<QoSTestResultEnum, Integer> TITLE_MAP;
	
	static {
		TITLE_MAP = new HashMap<QoSTestResultEnum, Integer>();
		TITLE_MAP.put(QoSTestResultEnum.WEBSITE, R.string.qos_test_name_website);
		TITLE_MAP.put(QoSTestResultEnum.HTTP_PROXY, R.string.qos_test_name_http_proxy);
		TITLE_MAP.put(QoSTestResultEnum.NON_TRANSPARENT_PROXY, R.string.qos_test_name_non_transparent_proxy);
		TITLE_MAP.put(QoSTestResultEnum.DNS, R.string.qos_test_name_dns);
		TITLE_MAP.put(QoSTestResultEnum.TCP, R.string.qos_test_name_tcp);
		TITLE_MAP.put(QoSTestResultEnum.UDP, R.string.qos_test_name_udp);
		TITLE_MAP.put(QoSTestResultEnum.VOIP, R.string.qos_test_name_voip);
		TITLE_MAP.put(QoSTestResultEnum.TRACEROUTE, R.string.qos_test_name_traceroute);
	}
	
	private final List<QoSTestResultEnum> titleList = new ArrayList<QoSTestResultEnum>();
    private final MainActivity activity;
    private final QoSServerResultCollection results;
    private final Map<QoSTestResultEnum, List<QoSServerResult>> resultMap;
    private final Map<QoSTestResultEnum, List<QoSServerResultDesc>> descMap;
        
	public QoSCategoryPagerAdapter(final MainActivity _activity,
                                   final Handler _handler, final QoSServerResultCollection results)
    {
		super();
		
    	this.activity = _activity;
        this.results = results;

        this.descMap = results.getDescMap();
        this.resultMap = results.getResultMap();
        
        for (QoSTestResultEnum type : QoSTestResultEnum.values()) {
        	if (results.getQoSStatistics().getTestCounter(type) > 0) {
        		titleList.add(type);
        	}
        }
    }

	@Override
	public CharSequence getPageTitle(int position) {
		//return results.getTestDescMap().get(titleList.get(position)).getName();
		return ConfigHelper.getCachedQoSNameByTestType(titleList.get(position), activity);
	}

	/**
	 * 
	 * @param key
	 * @return
	 */
	public boolean hasResults(QoSTestResultEnum key) {
		return titleList.contains(key);
	}

	@NonNull
	@Override
	public Object instantiateItem(@NonNull ViewGroup container, int position) {
		final Context context = container.getContext();

		//QoSTestResultEnum key = QoSTestResultEnum.values()[position];
        QoSTestResultEnum key = titleList.get(position);
        View view = null;
        view = new QoSCategoryView(context, activity, results.getTestDescMap().get(key), resultMap.get(key), descMap.get(key));
        container.addView(view);
        return view;
	}
	
	@Override
	public int getItemPosition(@NonNull Object object) {
		return PagerAdapter.POSITION_NONE;
	}
	
	@Override
	public int getCount() {
		//return QoSTestResultEnum.values().length;
		return titleList.size();
	}
    
    public boolean onBackPressed()
    {
    	activity.getSupportFragmentManager().popBackStack();
    	return true;
    }

	@Override
	public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
		return view == object;
	}

	@Override
	public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
		final View view = (View) object;
		container.removeView(view);
	}	
}
