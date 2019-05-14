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
package at.specure.android.screens.result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.specure.opennettest.R;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import at.specure.android.screens.main.MainActivity;
import at.specure.android.api.calls.CheckTestResultDetailTask;
import at.specure.android.util.EndTaskListener;
import at.specure.android.util.Helperfunctions;
import at.specure.android.util.InformationCollector;
import at.specure.client.v2.task.result.QoSTestResult;

import static at.specure.android.screens.result.ResultDetailType.SPEEDTEST;

public class TestResultDetailFragment extends Fragment implements EndTaskListener
{
	public static enum ResultDetailType {
		SPEEDTEST,
		QOS_TEST
	}
	
    private static final String DEBUG_TAG = "RMBTTestResultDetailFra";
    
    public static final String ARG_UID = "uid";
    
    private MainActivity activity;
    
    private CheckTestResultDetailTask testResultDetailTask;
    
    private ListAdapter valueList;
    
    private ListView listView;
    
    private TextView emptyView;
    
    private ProgressBar progessBar;
    
    private ArrayList<HashMap<String, String>> itemList;
    
    private ResultDetailType testType;
    
    /**
     * 
     */
    public TestResultDetailFragment() {
    	
    }
    
//    /**
//     * 
//     * @param testType
//     */
//    public TestResultDetailFragment(ResultDetailType testType) {
//    	this.testType = testType;
//    }
    
    public void setTestResultDetailType(ResultDetailType testType) {
    	this.testType = testType;
    }
    
    
    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        activity = (MainActivity) getActivity();
        
    }
    
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
    {
        // The last two arguments ensure LayoutParams are inflated
        // properly.
        
        super.onCreateView(inflater, container, savedInstanceState);
        
        final View view = inflater.inflate(R.layout.test_result_detail, container, false);
        
        final Bundle args = getArguments();
        /*
         * ((TextView) view.findViewById(R.id.text1)).setText(Integer
         * .toString(args.getInt(ARG_UID)));
         */
        
        listView = (ListView) view.findViewById(R.id.valueList);
        listView.setVisibility(View.GONE);
        
        emptyView = (TextView) view.findViewById(R.id.infoText);
        emptyView.setVisibility(View.GONE);
        
        progessBar = (ProgressBar) view.findViewById(R.id.progressBar);
        
        if ((testResultDetailTask == null || testResultDetailTask != null || testResultDetailTask.isCancelled())
                && args.getString(ARG_UID) != null)
        {
            testResultDetailTask = new CheckTestResultDetailTask(activity, at.specure.android.screens.result.adapter.result.ResultDetailType.SPEEDTEST);
            
            testResultDetailTask.setEndTaskListener(this);
            testResultDetailTask.execute(args.getString(ARG_UID));
        }
        
        itemList = new ArrayList<HashMap<String, String>>();
        
        // listView.setEmptyView(emptyView);
        
        return view;
    }
    
    @Override
    public void taskEnded(final JsonArray testResultDetail)
    {
        if (!isVisible())
            return;
        
        if (testResultDetail != null && testResultDetail.size() > 0 && !testResultDetailTask.hasError())
        {
            
            try
            {
                
                HashMap<String, String> viewItem;
                
                if (testType == ResultDetailType.SPEEDTEST) {
                    for (int i = 0; i < testResultDetail.size(); i++)
                    {
                        
                        final JsonObject singleItem = testResultDetail.get(i).getAsJsonObject();
                        
                        viewItem = new HashMap<String, String>();
                        JsonElement o = null;
                        if (singleItem.has("title")) {
                            o = singleItem.get("title");
                        }

                        
                        if (o.isJsonObject()) {
                            JsonObject jsonObject = o.getAsJsonObject();
                            Iterator<Entry<String, JsonElement>> iterator = jsonObject.entrySet().iterator();
                        	while(iterator.hasNext()) {
                                Entry<String, JsonElement> next = iterator.next();
                                String key = next.getKey();
                        		viewItem.put("name", key);
                        		viewItem.put("value", next.getValue().getAsString());
                        	}
                        }
                        else {
                            String title = "";
                            if (singleItem.has("title")) {
                                title = singleItem.get("title").getAsString();
                            }

	                        viewItem.put("name", title);
	                        
	                        if (singleItem.has("time") && (singleItem.has("timezone")))
	                        {
	                            final String timeString = Helperfunctions
	                                    .formatTimestampWithTimezone(singleItem.get("time").getAsLong(),
	                                            singleItem.get("timezone").getAsString(), true /* seconds */);
	                            viewItem.put("value", timeString == null ? "-" : timeString);
	                        }
	                        else 
	                        {
                                String value = "";
                                if (singleItem.has("value")) {
                                    value = singleItem.get("value").getAsString();
                                }
                                viewItem.put("value", value);
	                        }
                        }
                        
                        itemList.add(viewItem);
                    }	
                }
                else if (testType == ResultDetailType.QOS_TEST) {
                    if (InformationCollector.qoSResult != null) {
                    	int c = 0;
                        for (QoSTestResult nnResult : InformationCollector.qoSResult.getResults()) {
                            viewItem = new HashMap<String, String>();
                            viewItem.put("name", "TEST " + (c++) + "");
                            viewItem.put("value", nnResult.getTestType().name());
                            itemList.add(viewItem);

                        	for (Entry<String, Object> item : nnResult.getResultMap().entrySet()) {
                                viewItem = new HashMap<String, String>();
                                viewItem.put("name", item.getKey());
                                viewItem.put("value", String.valueOf(item.getValue()));
                                itemList.add(viewItem);
                        	}
                        }                	
                    }
                    
                	
                }
                                
            }
            catch (final JsonParseException e)
            {
                e.printStackTrace();
            }
            
            valueList = new SimpleAdapter(getActivity(), itemList, R.layout.test_result_detail_item, new String[] {
                    "name", "value" }, new int[] { R.id.name, R.id.value });
            
            listView.setAdapter(valueList);
            
            listView.invalidate();
            
            progessBar.setVisibility(View.GONE);
            emptyView.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            
        }
        else
        {
            Log.i(DEBUG_TAG, "LEERE LISTE");
            progessBar.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText(getString(R.string.error_no_data));
            emptyView.invalidate();
        }
        
    }
    
}
