/*******************************************************************************
 * Copyright 2014-2017 Specure GmbH
 * Copyright 2013-2014 alladin-IT GmbH
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
import androidx.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import androidx.viewpager.widget.PagerAdapter;
import at.specure.android.screens.main.MainActivity;
import at.specure.android.screens.result.views.QoSTestDetailView;
import at.specure.client.v2.task.result.QoSServerResult;
import at.specure.client.v2.task.result.QoSServerResultDesc;

public class QoSTestDetailPagerAdapter extends PagerAdapter {

    private final MainActivity activity;
    private final List<QoSServerResult> resultList;
    private final List<QoSServerResultDesc> descList;
        
	public QoSTestDetailPagerAdapter(final MainActivity _activity,
                                     final List<QoSServerResult> resultList, final List<QoSServerResultDesc> descList) {
		
		super();
		
    	this.activity = _activity;
        this.resultList = resultList;
        this.descList = descList;
    }

	@Override
	public CharSequence getPageTitle(int position) {
		return "#" + (position + 1);
	}

	@NonNull
	@Override
	public Object instantiateItem(@NonNull ViewGroup container, int position) {
		final Context context = container.getContext();
		View view = null;
		//view = new QoSCategoryView(context, activity, results.getTestDescMap().get(key), resultMap.get(key), descMap.get(key));
		view = new QoSTestDetailView(context, activity, resultList.get(position), descList);
		container.addView(view);
		return view;
	}
	
	@Override
	public int getItemPosition(@NonNull Object object) {
		return PagerAdapter.POSITION_NONE;
	}
	
	@Override
	public int getCount() {
		return resultList.size();
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
