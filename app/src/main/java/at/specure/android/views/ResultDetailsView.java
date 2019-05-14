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
package at.specure.android.views;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.specure.opennettest.R;

import java.util.ArrayList;
import java.util.HashMap;

import at.specure.android.api.calls.CheckTestResultDetailTask;
import at.specure.android.api.jsons.TestResultDetails.TestResultDetails;
import at.specure.android.screens.result.adapter.result.ResultDetailType;
import at.specure.android.util.EndTaskListener;
import timber.log.Timber;

public class ResultDetailsView extends LinearLayout implements EndTaskListener {
	
    public static final String ARG_UID = "uid";
    private static final String DEBUG_TAG = "ResultDetailsView";
    private View view;
    private Activity activity;
    
    private CheckTestResultDetailTask testResultDetailTask;
    
    private ListAdapter valueList;
    
    private ListView listView;
    
    private TextView emptyView;
    
    private ProgressBar progessBar;
    
    private ArrayList<HashMap<String, String>> itemList;

    private String uid;
    
    private JsonArray testResult;
    
    private EndTaskListener resultFetchEndTaskListener;

	    
	/**
	 * 
	 * @param context
	 */
	public ResultDetailsView(Context context, Activity activity, String uid, JsonArray testResult) {
		this(context, null, activity, uid, testResult);
	}
	
	/**
	 * 
	 * @param context
	 * @param attrs
	 */
	public ResultDetailsView(Context context, AttributeSet attrs, Activity activity, String uid, JsonArray testResult) {
		super(context, attrs);

		LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.view = createView(layoutInflater);
		this.activity = activity;
		this.uid = uid;
		this.testResult = testResult;
	}
    
    /**
     * 
     * @param inflater
     * @return
     */
    public View createView(final LayoutInflater inflater)
    {        
        final View view = inflater.inflate(R.layout.test_result_detail, this);

        listView = view.findViewById(R.id.valueList);
        listView.setVisibility(View.GONE);

        emptyView = view.findViewById(R.id.infoText);
        emptyView.setVisibility(View.GONE);

        progessBar = view.findViewById(R.id.progressBar);
                
        return view;
    }
    
    public void initialize(EndTaskListener resultFetchEndTaskListener) {        
        itemList = new ArrayList<HashMap<String, String>>();

    	this.resultFetchEndTaskListener = resultFetchEndTaskListener;
    	
        if ((testResultDetailTask == null || testResultDetailTask != null || testResultDetailTask.isCancelled()) && uid != null)
        {
        	if (this.testResult!=null) {
        		System.out.println("TESTRESULT found ResultDetailsView");
        		taskEnded(this.testResult);
        	}
        	else {
        		System.out.println("TESTRESULT NOT found ResultDetailsView");
            	System.out.println("initializing ResultDetailsView");
            	
                testResultDetailTask = new CheckTestResultDetailTask(activity, ResultDetailType.SPEEDTEST);
                
                testResultDetailTask.setEndTaskListener(this);
                testResultDetailTask.execute(uid);        		
        	}
        }
    }
    
    @Override
    public void taskEnded(final JsonArray testResultDetail)
    {
        //if (getVisibility()!=View.VISIBLE)
        //    return;
        
    	if (this.resultFetchEndTaskListener != null) {
            this.resultFetchEndTaskListener.taskEnded(testResultDetail);
        }

        if (testResultDetail != null && testResultDetail.size() > 0 && (testResultDetailTask==null || !testResultDetailTask.hasError()))
        {
            this.testResult = testResultDetail;

            System.out.println("testResultDetail: " + testResultDetail);

            TestResultDetails testResultDetails = new TestResultDetails(testResultDetail, activity);

            itemList = testResultDetails.itemList;

            valueList = new SimpleAdapter(activity, itemList, R.layout.test_result_detail_item, new String[] {
                    "name", "value" }, new int[] { R.id.name, R.id.value });

            listView.setAdapter(valueList);

            listView.invalidate();

            progessBar.setVisibility(View.GONE);
            emptyView.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);

        }
        else
        {
            Timber.i( "LEERE LISTE");
            progessBar.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText(activity.getString(R.string.error_no_data));
            emptyView.invalidate();
        }

    }
    
    /**
     * 
     * @return
     */
    public JsonArray getTestResult() {
    	return testResult;
    }
}
