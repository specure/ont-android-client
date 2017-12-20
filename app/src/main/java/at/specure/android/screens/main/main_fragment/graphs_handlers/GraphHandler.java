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

package at.specure.android.screens.main.main_fragment.graphs_handlers;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.View;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.specure.opennettest.R;

import at.specure.android.configs.ConfigHelper;
import at.specure.android.screens.result.TestResultDetailFragment;
import at.specure.android.test.views.graph.SimpleGraph;
import at.specure.android.util.net.NetworkUtil;
import at.specure.android.views.graphview.CustomizableGraphView;

import static at.specure.android.screens.main.main_fragment.MainMenuFragment.NANO_MULTIPLIER;
import static at.specure.android.screens.main.main_fragment.MainMenuFragment.PROGRESS_SEGMENTS_PROGRESS_RING;
import static at.specure.android.screens.main.main_fragment.MainMenuFragment.RIGHT_GRAPH_SHIFT;
import static at.specure.android.screens.main.main_fragment.MainMenuFragment.STARTING_PERCENTAGE;

/**
 * Created by michal.cadrik on 10/17/2017.
 */

public class GraphHandler {

    GraphView testDownloadGraph;
    GraphView testUploadGraph;
    SimpleGraph testSignalGraph;
    SimpleGraph testSpeedGraphDownload;
    SimpleGraph testSpeedGraphUpload;
    at.specure.android.views.graphview.GraphView testGraphView;
    private LineGraphSeries<DataPointInterface> testDownloadSpeedDataSeries;
    private LineGraphSeries<DataPointInterface> testUploadSpeedDataSeries;
    private boolean testGraphStarted = false;

    public GraphHandler(View rootView, View rootDownload, View rootUpload) {
        if (rootView != null) {
            this.testGraphView = rootView.findViewById(R.id.test_graph);
        }
        if (rootDownload != null) {
            this.testDownloadGraph = rootDownload.findViewById(R.id.test_progress__small_graph);
        }
        if (rootUpload != null) {
            this.testUploadGraph = rootUpload.findViewById(R.id.test_progress__small_graph);
        }
    }

    public void resetGraphs() {

        if (testSignalGraph != null)
            testSignalGraph.reset();
        if (testSpeedGraphDownload != null)
            testSpeedGraphDownload.reset();
        if (testSpeedGraphUpload != null)
            testSpeedGraphUpload.reset();
        if (testDownloadGraph != null)
            testDownloadGraph.removeAllSeries();
        if (testUploadGraph != null)
            testUploadGraph.removeAllSeries();
        testGraphStarted = false;
    }

    public void releaseGraphs() {
        if (testGraphView != null)
            testGraphView.recycle();
    }


    public void initializeGraphs(Context context) {
        resetGraphs();
        long graphXAxis = getGraphXAxisMaxValue(context);
        int currentDownloadSpeedGraphColor;

        if (testGraphView != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                currentDownloadSpeedGraphColor = context.getResources().getColor(R.color.graph_current_speed_download_value_color, context.getTheme());
            } else {
                currentDownloadSpeedGraphColor = context.getResources().getColor(R.color.graph_current_speed_download_value_color);
            }
            if (testSpeedGraphDownload != null) {
                testSpeedGraphDownload = SimpleGraph.addGraph(testGraphView, currentDownloadSpeedGraphColor, graphXAxis);  //SmoothGraph.addGraph(graphView, Color.parseColor("#00f940"), SMOOTHING_DATA_AMOUNT, SMOOTHING_FUNCTION, false);
                testSpeedGraphDownload.setMaxTime(graphXAxis);
            }

            int currentSignalGraphColor;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                currentSignalGraphColor = context.getResources().getColor(R.color.graph_current_signal_value_color, context.getTheme());
            } else {
                currentSignalGraphColor = context.getResources().getColor(R.color.graph_current_signal_value_color);
            }
            if (testSignalGraph != null) {
                testSignalGraph = SimpleGraph.addGraph(testGraphView, currentSignalGraphColor, graphXAxis);
            }

            int currentUploadSpeedGraphColor;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                currentUploadSpeedGraphColor = context.getResources().getColor(R.color.graph_current_speed_upload_value_color, context.getTheme());
            } else {
                currentUploadSpeedGraphColor = context.getResources().getColor(R.color.graph_current_speed_upload_value_color);
            }
            if (testSpeedGraphUpload != null) {
                testSpeedGraphUpload = SimpleGraph.addGraph(testGraphView, currentUploadSpeedGraphColor, graphXAxis);  //SmoothGraph.addGraph(graphView, Color.parseColor("#00f940"), SMOOTHING_DATA_AMOUNT, SMOOTHING_FUNCTION, false);
                testSpeedGraphUpload.setMaxTime(graphXAxis);
            }

            //graphView.getLabelInfoVerticalList().add(new GraphLabel(getActivity().getString(R.string.test_dbm), "#f8a000"));
            if (testGraphView instanceof CustomizableGraphView) {
                ((CustomizableGraphView) testGraphView).setShowLog10Lines(false);
            }
        }

        testDownloadSpeedDataSeries = new LineGraphSeries<>();
        testUploadSpeedDataSeries = new LineGraphSeries<>();

        testDownloadSpeedDataSeries.setDrawBackground(true);
        testUploadSpeedDataSeries.setDrawBackground(true);

        testDownloadSpeedDataSeries.setBackgroundColor(context.getResources().getColor(R.color.graph_current_speed_download_value_color));
        testUploadSpeedDataSeries.setBackgroundColor(context.getResources().getColor(R.color.graph_current_speed_upload_value_color));

        testDownloadSpeedDataSeries.setColor(context.getResources().getColor(android.R.color.transparent));
        testUploadSpeedDataSeries.setColor(context.getResources().getColor(android.R.color.transparent));

        if (testDownloadGraph != null) {
            testDownloadGraph.addSeries(testDownloadSpeedDataSeries);
            testDownloadGraph.getGridLabelRenderer().setNumHorizontalLabels(0);
            testDownloadGraph.getGridLabelRenderer().setNumVerticalLabels(0);
            testDownloadGraph.getGridLabelRenderer().setHorizontalAxisTitle("");
            testDownloadGraph.getGridLabelRenderer().setVerticalAxisTitle("");
            testDownloadGraph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.NONE);

            testDownloadGraph.getViewport().setScrollable(true);
            testDownloadGraph.getViewport().setScalableY(true);

            testDownloadGraph.getViewport().setXAxisBoundsManual(true);
            testDownloadGraph.getViewport().setMinX(0);
            testDownloadGraph.getViewport().setMaxX(500);

            testDownloadGraph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
            testDownloadGraph.getGridLabelRenderer().setVerticalLabelsVisible(false);
        }

        if (testUploadGraph != null) {
            testUploadGraph.addSeries(testUploadSpeedDataSeries);
            testUploadGraph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
            testUploadGraph.getGridLabelRenderer().setVerticalLabelsVisible(false);

            testUploadGraph.getGridLabelRenderer().setNumHorizontalLabels(0);
            testUploadGraph.getGridLabelRenderer().setNumVerticalLabels(0);
            testUploadGraph.getGridLabelRenderer().setHorizontalAxisTitle("");
            testUploadGraph.getGridLabelRenderer().setVerticalAxisTitle("");
            testUploadGraph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.NONE);

            testUploadGraph.getViewport().setScrollable(true);
            testUploadGraph.getViewport().setScalableY(true);

            testUploadGraph.getViewport().setXAxisBoundsManual(true);
            testUploadGraph.getViewport().setMinX(0);
            testUploadGraph.getViewport().setMaxX(500);

            testUploadGraph.getGridLabelRenderer().setHorizontalLabelsVisible(false);
            testUploadGraph.getGridLabelRenderer().setVerticalLabelsVisible(false);
        }






    }

    public void addNewDownloadValue(double speedValueRelative, double progressSegments, Double relativeSignal) {
        if (testGraphView != null) {
            if (testGraphStarted || speedValueRelative != 0) {
                testGraphStarted = true;
                if (testSpeedGraphDownload != null) {
                    testSpeedGraphDownload.addValue(speedValueRelative, progressSegments * NANO_MULTIPLIER - STARTING_PERCENTAGE + RIGHT_GRAPH_SHIFT);
                }
                addNewSignalValue(progressSegments, relativeSignal);
            }
        }
        if (testDownloadGraph != null) {

            if (testDownloadSpeedDataSeries.isEmpty()) {
                testDownloadSpeedDataSeries.appendData(new DataPoint(0, speedValueRelative), false, 100);
                Log.e("Download DATA: ", "0, " + speedValueRelative);
            } else {
                testDownloadSpeedDataSeries.appendData(new DataPoint(testDownloadSpeedDataSeries.getHighestValueX() + 10, speedValueRelative), false, 100);
                Log.e("Download DATA: ", testDownloadSpeedDataSeries.getHighestValueX() + 10 + ", " + speedValueRelative);
            }

            testDownloadGraph.addSeries(testDownloadSpeedDataSeries);
            testDownloadGraph.getViewport().setMaxX(testDownloadSpeedDataSeries.getHighestValueX());
            testDownloadGraph.getViewport().setMinX(testDownloadSpeedDataSeries.getLowestValueX());
            testDownloadGraph.getViewport().setXAxisBoundsManual(true);
        }
    }

    public void addNewUploadValue(double speedValueRelative, double progressSegments, Double relativeSignal) {
        if (testGraphView != null) {
            if (testGraphStarted || speedValueRelative != 0) {
                testGraphStarted = true;
                if (testSpeedGraphUpload != null) {
                    testSpeedGraphUpload.addValue(speedValueRelative, progressSegments * NANO_MULTIPLIER - STARTING_PERCENTAGE + RIGHT_GRAPH_SHIFT);
                }
                addNewSignalValue(progressSegments, relativeSignal);
            }
        }
        if (testUploadGraph != null) {
            if (testUploadSpeedDataSeries.isEmpty()) {
                testUploadSpeedDataSeries.appendData(new DataPoint(0, speedValueRelative), false, 100);
            } else {
                testUploadSpeedDataSeries.appendData(new DataPoint(testUploadSpeedDataSeries.getHighestValueX() + 10, speedValueRelative), false, 100);
            }
            testUploadGraph.addSeries(testUploadSpeedDataSeries);
            testUploadGraph.getViewport().setMaxX(testUploadSpeedDataSeries.getHighestValueX());
            testUploadGraph.getViewport().setMinX(testUploadSpeedDataSeries.getLowestValueX());
            testUploadGraph.getViewport().setXAxisBoundsManual(true);
        }
    }

    public void addNewSignalValue(double progressSegments, Double relativeSignal) {
        if (testGraphView != null) {
            if (relativeSignal != null && testSignalGraph != null)
                testSignalGraph.addValue(relativeSignal, progressSegments * NANO_MULTIPLIER - STARTING_PERCENTAGE + RIGHT_GRAPH_SHIFT);
            testGraphView.invalidate();
        }
    }

    public void signalTypeChanged(Double relativeSignal,  NetworkUtil.MinMax<Integer> signalBounds) {
        if (testGraphView != null) {
            if (testSignalGraph != null)
                testSignalGraph.clearGraphDontResetTime();
            if (relativeSignal != null)
                testGraphView.setSignalRange(signalBounds.min, signalBounds.max);
            else
                testGraphView.removeSignalRange();
            testGraphView.invalidate();
        }
    }

    private long getGraphXAxisMaxValue(Context context) {
        long graphXAxis;
        boolean qosEnabled = ConfigHelper.isQosEnabled(context);
        if (qosEnabled) {
            // when speedtest is only to 50% qos is turned on
//            graphXAxis = 50 * NANO_MULTIPLIER - STARTING_PERCENTAGE + RIGHT_GRAPH_SHIFT + 40;
            graphXAxis = PROGRESS_SEGMENTS_PROGRESS_RING * NANO_MULTIPLIER + RIGHT_GRAPH_SHIFT - 35 * NANO_MULTIPLIER;
        } else {
            // when speedtest is to 100%
            graphXAxis = PROGRESS_SEGMENTS_PROGRESS_RING * NANO_MULTIPLIER + RIGHT_GRAPH_SHIFT - 35 * NANO_MULTIPLIER;
//            graphXAxis = 100 * NANO_MULTIPLIER - STARTING_PERCENTAGE + RIGHT_GRAPH_SHIFT + 20;
        }
        return graphXAxis;
    }

}
