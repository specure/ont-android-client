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
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.ScrollView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.specure.opennettest.R;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import at.specure.android.api.calls.CheckTestResultDetailTask;
import at.specure.android.screens.main.main_fragment.MainMenuFragment;
import at.specure.android.screens.result.adapter.result.ResultDetailType;
import at.specure.android.test.StaticGraph;
import at.specure.android.test.views.graph.SmoothGraph;
import at.specure.android.util.EndTaskListener;
import at.specure.android.util.InformationCollector;
import at.specure.android.util.net.NetworkUtil;
import at.specure.android.util.net.NetworkUtil.MinMax;
import at.specure.android.views.graphview.CustomizableGraphView;
import at.specure.android.views.graphview.GraphService;
import at.specure.android.views.graphview.GraphView.GraphLabel;
import at.specure.client.helper.IntermediateResult;
import timber.log.Timber;

public class ResultGraphView extends ScrollView implements EndTaskListener {
	
	public final static List<GraphLabel> SPEED_LABELS;
	
	public final static String VERTICAL_INFO_COLOR = "#C80fb82f";
	public final static String COLOR_SIGNAL_3G = "#ffe000";
	public final static String COLOR_SIGNAL_4G = "#40a0f8";

	//private static final String DEBUG_TAG = "ResultGraphView";
	public final static String COLOR_SIGNAL_WLAN = "#f8a000";
	public final static String SIGNAL_COLOR_MARKER = "#cccccc";
	public final static String COLOR_UL_GRAPH = "#81c1dc";
	public final static String COLOR_DL_GRAPH = "#31d13c";
	public static final String ARG_UID = "uid";

	static {
		SPEED_LABELS = new ArrayList<GraphLabel>();
		SPEED_LABELS.add(new GraphLabel("0.0", VERTICAL_INFO_COLOR));
		SPEED_LABELS.add(new GraphLabel("0.1", VERTICAL_INFO_COLOR));
		SPEED_LABELS.add(new GraphLabel("1.0", VERTICAL_INFO_COLOR));
		SPEED_LABELS.add(new GraphLabel("10", VERTICAL_INFO_COLOR));
		SPEED_LABELS.add(new GraphLabel("100", VERTICAL_INFO_COLOR));
	}

	private View view;
	private Activity activity;
    
    private String uid;
    
    private String openTestUid;
    
    private JsonArray testResult;

	private EndTaskListener resultFetchEndTaskListener;

	private CheckTestResultDetailTask testResultOpenDataTask;
	
	private JsonArray uploadArray;
	private JsonArray downloadArray;
	private JsonArray signalArray;
	
	private CustomizableGraphView signalGraph;
	private CustomizableGraphView ulGraph;
	private CustomizableGraphView dlGraph;
	
	private ProgressBar dlProgress;
	private ProgressBar ulProgress;
	private ProgressBar signalProgress;
	
	/**
	 * 
	 * @param context
	 */
	public ResultGraphView(Context context, Activity activity, String uid, String openTestUid, JsonArray testResult, ViewGroup vg) {
		this(context, null, activity, uid, openTestUid, testResult, vg);
	}
	
	/**
	 * 
	 * @param context
	 * @param attrs
	 */
	public ResultGraphView(Context context, AttributeSet attrs, Activity activity, String uid, String openTestUid, JsonArray testResult, ViewGroup vg) {
		super(context, attrs);
		setFillViewport(true);
		
		this.activity = activity;
		this.uid = uid;
		this.openTestUid = openTestUid;
		this.testResult = testResult;
		createView(vg);
	}
	
	/**
	 * 
	 * @param openTestUid
	 */
	public void setOpenTestUuid(String openTestUid) {
		this.openTestUid = openTestUid;
	}
	
    public void initialize(EndTaskListener resultFetchEndTaskListener) {
    	this.resultFetchEndTaskListener = resultFetchEndTaskListener;
    	
        if ((testResultOpenDataTask == null || testResultOpenDataTask != null || testResultOpenDataTask.isCancelled()) && uid != null)
        {
        	if (this.testResult!=null) {
        		System.out.println("TESTRESULT found GraphView");
        		taskEnded(this.testResult);
        	}
        	else {
        		System.out.println("TESTRESULT NOT found GraphView");
            	System.out.println("initializing ResultGraphView");
            	
                testResultOpenDataTask = new CheckTestResultDetailTask(activity, ResultDetailType.OPENDATA);
                
                testResultOpenDataTask.setEndTaskListener(this);
                testResultOpenDataTask.execute(uid, openTestUid);        		
        	}
        }
    }
    
	public View createView(ViewGroup vg)
    {       
		final LayoutInflater inflater = (LayoutInflater) vg.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		try {
	    	//view = inflater.inflate(R.layout.result_graph, vg, false);
			view = inflater.inflate(R.layout.result_graph, this);

			signalGraph = view.findViewById(R.id.graph_signal);
			ulGraph = view.findViewById(R.id.graph_upload);
			ulGraph.setShowLog10Lines(false);
			dlGraph = view.findViewById(R.id.graph_download);
			dlGraph.setShowLog10Lines(false);

			signalProgress = view.findViewById(R.id.signal_progress);
			ulProgress = view.findViewById(R.id.upload_progress);
			dlProgress = view.findViewById(R.id.download_progress);

		} catch (Exception e) {
			e.printStackTrace();
		}
        
    	
    	return view;
    }
		
	/**
	 * 
	 * @return
	 */
	public View getView() {
		return view;
	}
	
//	@Override
//	protected void onDraw(Canvas canvas) {
//		super.onDraw(canvas);
//		
//		if (view == null) {
//			Timber.d("ResultGraphView", "ONDRAW");
//			//removeAllViews();
//			createView();
//			
//			taskEnded(testResult);			
//		}
//	}

	@Override
	public void taskEnded(JsonArray result) {
		//addView(view);
		if (result != null) {
			testResult = result;
		}
		
		if (view == null || result == null) {
			return;
		}
		
		if (resultFetchEndTaskListener != null) {
			resultFetchEndTaskListener.taskEnded(result);
		}
		
		if (testResult != null) {
			redraw(testResult);
		}
	}
	
	public void refresh(JsonArray result) {
		this.testResult = result;
		redraw(result);
	}
	
	private void redraw(JsonArray result) {
		try {
			JsonObject curve = result.get(0).getAsJsonObject().getAsJsonObject("speed_curve");
			if (curve != null) {
				uploadArray = curve.getAsJsonArray("upload");
				downloadArray = curve.getAsJsonArray("download");
				signalArray = curve.getAsJsonArray("signal");
			}
			
			//System.out.println(signalArray);

			JsonObject object = result.get(0).getAsJsonObject();

			long maxTimeUpload = 0;
			if (object.has("time_ul_ms")) {
				try {
					maxTimeUpload = object.get("time_ul_ms").getAsLong();
				} catch(Exception e) {
					// total fail of constructing JSON from backend side
				}
			}
			if (object.has("duration_upload_ms")) {
				maxTimeUpload += object.get("duration_upload_ms").getAsLong();
			}

			long timeElapsed = Math.round((double)maxTimeUpload / 1000);
			
			if (signalGraph != null && signalArray != null && signalArray.size() > 0 && signalGraph.getGraphs().size() < 1) {
				Timber.d("ResultGraphView DRAWING SIGNAL GRAPH\n %s" , signalArray);

				final long maxTimeSignal = signalArray.get(signalArray.size()-1).getAsJsonObject().get("time_elapsed").getAsLong();
				final long timeElapsedMs = Math.max(maxTimeSignal, maxTimeUpload);
				timeElapsed = Math.round((double)timeElapsedMs / 1000);
				
				signalGraph.getLabelHMaxList().clear();
				signalGraph.addLabelHMax(String.valueOf(timeElapsed < 7 ? 7 : timeElapsed));
				signalGraph.updateGrid((int)timeElapsed, 4);
				/* [{"network_type":"WLAN","time_elapsed":0,"signal_strength":-56},
				 * {"network_type":"WLAN","time_elapsed":10121,"signal_strength":-55},
				 * {"network_type":"WLAN","time_elapsed":37478,"signal_strength":-57}]
				 */
				MinMax<Integer> signalBoundsRsrp = NetworkUtil.getSignalStrengthBounds(InformationCollector.SIGNAL_TYPE_RSRP);
				MinMax<Integer> signalBoundsWlan = NetworkUtil.getSignalStrengthBounds(InformationCollector.SIGNAL_TYPE_WLAN);
				MinMax<Integer> signalBoundsMobile = NetworkUtil.getSignalStrengthBounds(InformationCollector.SIGNAL_TYPE_MOBILE);
				
				List<GraphService> signalList = new ArrayList<GraphService>(); 
				
				boolean has4G = false;
				boolean has3G = false;
				boolean hasWlan = false;
				String lastNetworkType = "";
				String lastCatTechnology = "";
				
				GraphService curGraph = null;

				double prevValue = 0d;
				
				boolean hasSignalCategoryChanged = false;
				double lastLabelYPosition = -1d;
				double time = 0d;
				double signalChangeStartTime = 0d;
				double value = 0d;
				double oldValidValue = 0d;
				
				System.out.println("MAXTIME: " + timeElapsedMs);
				
				for (int i = 0; i < signalArray.size(); i++) {
					JsonObject signalObject = signalArray.get(i).getAsJsonObject();
					String networkType = null;
					if (signalObject.has("network_type")) {
						networkType = signalObject.get("network_type").getAsString();
					}
					String catTechnology = null;
					if (signalObject.has("cat_technology")) {
						catTechnology = signalObject.get("cat_technology").getAsString();
					}
					//set proper signal strength attribute
					String signalAttribute = "signal_strength";
					String signalColor = "#ffffff";
					
					time = i > 0 ? ((double)signalObject.get("time_elapsed").getAsInt() / (double)timeElapsedMs) : 0d;
					
					oldValidValue = value > 0d ? value : oldValidValue;
					
					if ("LTE".equals(networkType)) {
						if (!lastNetworkType.equals(networkType) && !lastCatTechnology.equals(catTechnology)) {
							if (curGraph != null) {
								curGraph.addValue((1-prevValue), time);
							}

							GraphService newGraph = StaticGraph.addGraph(signalGraph, Color.parseColor(COLOR_SIGNAL_4G),
									signalArray.size() == 1);
							signalList.add(newGraph);
							if (curGraph != null) {
								curGraph.addValue((1-prevValue), time);
							}
							signalChangeStartTime = time;
							hasSignalCategoryChanged = true;
							curGraph = newGraph;
						}
						has4G = true;
						signalAttribute = "lte_rsrp";
						signalColor = COLOR_SIGNAL_4G;
						value = getRelativeSignal(signalBoundsRsrp, signalAttribute, signalObject);
					}
					else if ("WLAN".equals(networkType)) {
						if (!lastNetworkType.equals(networkType) && !lastCatTechnology.equals(catTechnology)) {
							if (curGraph != null) {
								curGraph.addValue((1-prevValue), time);
							}

							GraphService newGraph = StaticGraph.addGraph(signalGraph, Color.parseColor(COLOR_SIGNAL_WLAN),
									signalArray.size() == 1);
							signalList.add(newGraph);
							
							if (curGraph != null) {
								curGraph.addValue((1-prevValue), time);
							}
							signalChangeStartTime = time;
							hasSignalCategoryChanged = true;
							curGraph = newGraph;
						}
						hasWlan = true;
						signalAttribute = "signal_strength";
						signalColor = COLOR_SIGNAL_WLAN;
						value = getRelativeSignal(signalBoundsWlan, signalAttribute, signalObject);
					}
					else {
						if (!lastNetworkType.equals(networkType)) {
							signalChangeStartTime = time;
							hasSignalCategoryChanged = true;
							
							if (curGraph != null) {
								curGraph.addValue((1-prevValue), time);
							}
							
							if ((!lastCatTechnology.equals(catTechnology)) && 
									("4G".equals(lastCatTechnology) || "WLAN".equals(lastCatTechnology) || "".equals(lastCatTechnology))) {

								GraphService newGraph = StaticGraph.addGraph(signalGraph, Color.parseColor(COLOR_SIGNAL_3G),
										signalArray.size() == 1);

								signalList.add(newGraph);
								if (curGraph != null) {
									curGraph.addValue((1-prevValue), time);
								}
								curGraph = newGraph;
							}
						}
						has3G = true;
						signalAttribute = "signal_strength";
						signalColor = COLOR_SIGNAL_3G;
						value = getRelativeSignal(signalBoundsMobile, signalAttribute, signalObject);
					}					
					
					if (value > 0d) {				
						System.out.println("SIGNAL: " + value + "@" + time + " = " +  signalObject);
						if (value >= 0d && curGraph != null) {
							if (hasSignalCategoryChanged) {
								curGraph.addValue((1 - value), signalChangeStartTime);
								hasSignalCategoryChanged = false;
								
								if (lastLabelYPosition == -1d) {
									lastLabelYPosition = (float) (1 - (value > 0d ? value : prevValue));
								}
								else {
									if (Math.abs(signalChangeStartTime - time) < .125d) {
										float curPosition = (float) (1 - (value > 0d ? value : prevValue));
										if (Math.abs(curPosition - lastLabelYPosition) <= .11d) {
											lastLabelYPosition = curPosition + (curPosition > lastLabelYPosition ? +.1d : -.1d);
										}
										else {
											lastLabelYPosition = curPosition;
										}
									}
									else {
										lastLabelYPosition = (float) (1 - (value > 0d ? value : prevValue));
									}
								}
								
								//lastLabelXPosition = (float) time;								
								double labelDiff = lastLabelYPosition - (1-value);
								
								System.out.println("i" + i + " -> " + lastLabelYPosition + " : " + (1-value) + " diff: " + Math.abs(labelDiff) + " istoolow (<.09d)? " + (Math.abs(labelDiff) < .09d));
								if (Math.abs(labelDiff) < .09d && i == 0) {
									if (labelDiff < 0d) {
										lastLabelYPosition = lastLabelYPosition < .50d ? lastLabelYPosition - .075d : lastLabelYPosition + .075d;
									}
									else {
										lastLabelYPosition = lastLabelYPosition < .50d ? lastLabelYPosition + .075d : lastLabelYPosition - .075d;
									}
								}

								signalGraph.addLabel((float) signalChangeStartTime, (float) lastLabelYPosition, networkType, signalColor);
							}
	
							//System.out.println("ADDING VALUE TO GRAPH " + (1 - value) + " on: " + time);
							curGraph.addValue((1 - value), time);
							prevValue = value;
						}
					}
					
					lastNetworkType = networkType;
					lastCatTechnology = catTechnology;
				}
				
				//draw signal graph to the end
				if (prevValue > 0 && curGraph != null) {
					curGraph.addValue((1 - prevValue), 1f);
				}
				
				signalGraph.clearLabels(CustomizableGraphView.LABELLIST_VERTICAL_MAX);
				signalGraph.clearLabels(CustomizableGraphView.LABELLIST_VERTICAL_MIN);
				
				if (has3G) {
					signalGraph.addLabelVMax(String.valueOf(signalBoundsMobile.max), COLOR_SIGNAL_3G);
					signalGraph.addLabelVMin(String.valueOf(signalBoundsMobile.min), COLOR_SIGNAL_3G);
				}
				if (has4G) {
					signalGraph.addLabelVMax(String.valueOf(signalBoundsRsrp.max), COLOR_SIGNAL_4G);
					signalGraph.addLabelVMin(String.valueOf(signalBoundsRsrp.min), COLOR_SIGNAL_4G);
				}
				if (hasWlan) {
					signalGraph.addLabelVMax(String.valueOf(signalBoundsWlan.max), COLOR_SIGNAL_WLAN);
					signalGraph.addLabelVMin(String.valueOf(signalBoundsWlan.min), COLOR_SIGNAL_WLAN);
				}
				//signalGraph.repaint(getContext());
			}
			else if (signalGraph != null && signalGraph.getGraphs().size() > 0) {
				Timber.d("ResultGraphView REDRAWING SIGNAL GRAPH");
				//signalGraph.repaint(getContext());
				signalGraph.invalidate();
			}
			
			signalProgress.setVisibility(View.GONE);
			JsonObject jsonobject = result.get(0).getAsJsonObject();
			if (uploadArray != null && uploadArray != null && uploadArray.size() > 0 && ulGraph.getGraphs().size() < 1) {
				Timber.d("ResultGraphView DRAWING UL GRAPH");

				Double durationUpload = 0d;
				if (jsonobject.has("duration_upload_ms")) {
					durationUpload = jsonobject.get("duration_upload_ms").getAsDouble();
				}

				drawCurve(uploadArray, ulGraph, COLOR_UL_GRAPH, String.valueOf(Math.round(durationUpload / 1000d)));

				Double timeUlMs = 0d;
				if (jsonobject.has("time_ul_ms")) {
					try {
						timeUlMs = jsonobject.get("time_ul_ms").getAsDouble();
					} catch (Exception e) {
						// the same fail as above with same field
					}
				}

				if (!timeUlMs.equals(0d)) {
					addStaticMarker(signalArray, signalGraph, COLOR_UL_GRAPH, 70,
							timeUlMs,
							timeUlMs + durationUpload,
							timeElapsed * 1000);
				}
				
				double timeUl = durationUpload;
				long timeElapsedUl = Math.round(timeUl / 1000);
				ulGraph.setRowLinesLabelList(SPEED_LABELS);
				ulGraph.updateGrid((int) timeElapsedUl, 4.5f);
			}
			else if (uploadArray.size() > 0 && ulGraph != null && ulGraph.getGraphs().size() > 0) {
				Timber.d("ResultGraphView REDRAWING UL GRAPH");
				//ulGraph.repaint(getContext());
				ulGraph.invalidate();
			}

			ulProgress.setVisibility(View.GONE);

			if (downloadArray != null && downloadArray != null && downloadArray.size() > 0 && dlGraph.getGraphs().size() < 1) {
				Timber.d("ResultGraphView DRAWING DL GRAPH");
				Double durationDownload = 0d;
				if (jsonobject.has("duration_download_ms")) {
					durationDownload = jsonobject.get("duration_download_ms").getAsDouble();
				}
				Double timeDlMs = 0d;
				if (jsonobject.has("time_dl_ms")) {
					try {
						timeDlMs = jsonobject.get("time_dl_ms").getAsDouble();
					} catch (Exception e) {
						// "null" sent in some cases server fail (browser synced measurements)
					}

				}
				drawCurve(downloadArray, dlGraph, COLOR_DL_GRAPH, String.valueOf(Math.round(result.get(0).getAsJsonObject().get("duration_download_ms").getAsDouble() / 1000d)));
				if (!timeDlMs.equals(0d)) {
					addStaticMarker(signalArray, signalGraph, COLOR_DL_GRAPH, 70,
							timeDlMs,
							timeDlMs + durationDownload,
							timeElapsed * 1000);
				}
				
				double timeDl = durationDownload;
				long timeElapsedDl = Math.round(timeDl / 1000);
				dlGraph.setRowLinesLabelList(SPEED_LABELS);
				dlGraph.updateGrid((int) timeElapsedDl, 4.5f);
			}
			else if (downloadArray.size() > 0 && dlGraph != null && dlGraph.getGraphs().size() > 0) {
				Timber.d("ResultGraphView REDRAWING DL GRAPH");
				//dlGraph.repaint(getContext());
				dlGraph.invalidate();
			}

			dlProgress.setVisibility(View.GONE);

		}
		catch (Exception e) {
			if (signalGraph != null) {
				signalGraph.invalidate();
			}
			if (ulGraph != null) {
				ulGraph.invalidate();
			}
			if (dlGraph != null) {
				dlGraph.invalidate();
			}
			e.printStackTrace();
			//TODO show no data available view 
		}
	}
	
	public double getRelativeSignal(final MinMax<Integer> signalBounds, final String signalAttribute, final JsonObject signalObject) throws JsonParseException {
		final int signal = signalObject.get(signalAttribute).getAsInt();
		final double value = signal < 0? ((double)(signal - signalBounds.max) / (double)(signalBounds.min - signalBounds.max)) : signal;
		System.out.println("signalAttrib: " + signalAttribute + ", signal: " + signal + " value: " + value);
		return value;
	}
	
	/**
	 * 
	 * @param graphArray
	 * @param graphView
	 * @param color
	 * @throws JSONException
	 */
	public void addStaticMarker(JsonArray graphArray, CustomizableGraphView graphView, String color, double absoluteMarkerMs, double maxTimeMs) throws JsonParseException {
		StaticGraph markerGraph = StaticGraph.addGraph(graphView, Color.parseColor(color), false);
		final double startTime = (absoluteMarkerMs / maxTimeMs);
		markerGraph.addValue(1, startTime);
		markerGraph.addValue(0, startTime);
	}
	
	/**
	 * 
	 * @param graphArray
	 * @param graphView
	 * @param color
	 * @param alpha
	 * @param absoluteMarkerStartMs
	 * @param absoluteMarkerEndMs
	 * @param maxTimeMs
	 * @throws JSONException
	 */
	public void addStaticMarker(JsonArray graphArray, CustomizableGraphView graphView, String color, int alpha, double absoluteMarkerStartMs, double absoluteMarkerEndMs, double maxTimeMs) throws JSONException {
		StaticGraph markerGraph = StaticGraph.addGraph(graphView, Color.parseColor(color), false);
		StaticGraph markerGraphStartLine = StaticGraph.addGraph(graphView, Color.parseColor(color), false);
		StaticGraph markerGraphEndLine = StaticGraph.addGraph(graphView, Color.parseColor(color), false);
		markerGraph.setFillAlpha(alpha/2);
		markerGraph.setPaintAlpha(alpha/2);
		markerGraphStartLine.setPaintAlpha(alpha);
		markerGraphEndLine.setPaintAlpha(alpha);
		
		final double startTime = (absoluteMarkerStartMs / maxTimeMs);
		final double endTime = (absoluteMarkerEndMs / maxTimeMs);
		markerGraph.addValue(1, startTime);
		markerGraph.addValue(1, endTime);
		markerGraphStartLine.addValue(1, startTime);
		markerGraphStartLine.addValue(0, startTime);
		markerGraphEndLine.addValue(1, endTime);
		markerGraphEndLine.addValue(0, endTime);
	}

	
	/**
	 * 
	 * @param graphArray
	 * @param graphView
	 * @throws JSONException
	 */
	public void drawCurve(JsonArray graphArray, CustomizableGraphView graphView, String color, String labelHMax) throws JSONException {
		JsonObject json = graphArray.get(graphArray.size()-1).getAsJsonObject();
		Double timeElapsed = 0d;
		if (json.has("time_elapsed")) {
			timeElapsed = json.get("time_elapsed").getAsDouble();
		}
		final double maxTime = timeElapsed;
		//final double pointDistance = (0.25d / (maxTime / 1000));

		graphView.getLabelHMaxList().clear();
		graphView.addLabelHMax(labelHMax);
		
		//StaticGraph signal = StaticGraph.addGraph(graphView, Color.parseColor(color));
		GraphService signal = SmoothGraph.addGraph(graphView, Color.parseColor(color), MainMenuFragment.SMOOTHING_DATA_AMOUNT,
				MainMenuFragment.SMOOTHING_FUNCTION, false);
		
		if (graphArray != null && graphArray.size() > 0) {
			long bytes = 0;
			for (int i = 0; i < graphArray.size(); i++) {
				JsonObject uploadObject = graphArray.get(i).getAsJsonObject();
				double time_elapsed = uploadObject.get("time_elapsed").getAsInt();
				bytes = uploadObject.get("bytes_total").getAsInt();
				double bitPerSec = (bytes * 8000 / time_elapsed);
				double time = (time_elapsed / maxTime);
				
				if (i+1 == graphArray.size()) {
					signal.addValue(IntermediateResult.toLog((long) bitPerSec), time, SmoothGraph.FLAG_ALIGN_RIGHT);
				}
				else {
					signal.addValue(IntermediateResult.toLog((long) bitPerSec), time, 
							i == 0 ? SmoothGraph.FLAG_ALIGN_LEFT : SmoothGraph.FLAG_NONE);
				}
			}
		}
	}
	
	@Override
	public void invalidate() {
		setFillViewport(true);
		super.invalidate();
	}
	
	@Override
	protected void onConfigurationChanged(Configuration newConfig) {
		view = null;
		super.onConfigurationChanged(newConfig);
	}
	

	@Override
	protected Parcelable onSaveInstanceState() {
		super.onSaveInstanceState();
        Bundle bundle = new Bundle();
        bundle.putString("test_result", testResult.toString());
        return bundle;

	}
	
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		super.onRestoreInstanceState(state);
        if (state instanceof Bundle)
        {
            Bundle bundle = (Bundle) state;
            try {
				Gson gson = new Gson();
				testResult = gson.toJsonTree(bundle.getString("test_result")).getAsJsonArray();
			} catch (JsonParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            return;
        }
	}
	
	public void recycle() {
		if (dlGraph != null) { 
			dlGraph.recycle();
			dlGraph = null;
		}
		if (ulGraph != null) {
			ulGraph.recycle();
			ulGraph = null;
		}
		if (signalGraph != null) {
			signalGraph.recycle();
			signalGraph = null;
		}
	}
}
