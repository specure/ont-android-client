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

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.specure.opennettest.R;

import java.util.ArrayList;

import at.specure.android.configs.ConfigHelper;
import at.specure.android.test.views.graph.SimpleGraph;
import at.specure.android.util.net.NetworkUtil;
import at.specure.android.views.ResultGraphView;
import at.specure.android.views.graphview.CustomizableGraphView;
import timber.log.Timber;

import static at.specure.android.screens.main.main_fragment.MainMenuFragment.NANO_MULTIPLIER;
import static at.specure.android.screens.main.main_fragment.MainMenuFragment.PROGRESS_SEGMENTS_PROGRESS_RING;
import static at.specure.android.screens.main.main_fragment.MainMenuFragment.RIGHT_GRAPH_SHIFT;
import static at.specure.android.screens.main.main_fragment.MainMenuFragment.STARTING_PERCENTAGE;

/**
 * Class handling all graphs in the test screen
 * Created by michal.cadrik on 10/17/2017.
 */

public class GraphHandler {

    private LineChart testUploadGraph;
    private LineChart testDownloadGraph;
    private SimpleGraph testSignalGraph;
    private SimpleGraph testSpeedGraphDownload;
    private SimpleGraph testSpeedGraphUpload;
    private at.specure.android.views.graphview.GraphView testGraphView;
    private LineDataSet testDownloadSpeedDataSeries;
    private LineDataSet testUploadSpeedDataSeries;
    private boolean testGraphStarted = false;

    public GraphHandler(View rootView, View rootDownload, View rootUpload) {
        if (rootView != null) {
            this.testGraphView = rootView.findViewById(R.id.test_graph);
            initializeGraphs(rootView.getContext());
        }
        if (rootDownload != null) {
            this.testDownloadGraph = rootDownload.findViewById(R.id.test_progress__small_graph);
            testDownloadGraph.setDescription(null);    // Hide the description
            testDownloadGraph.getAxisLeft().setEnabled(false);
            testDownloadGraph.getAxisRight().setEnabled(false);
            testDownloadGraph.getXAxis().setEnabled(false);
            testDownloadGraph.setDrawMarkers(false);
            testDownloadGraph.setAutoScaleMinMaxEnabled(true);
            testDownloadGraph.getLegend().setEnabled(false);
            testDownloadGraph.setDrawGridBackground(false);
            testDownloadGraph.setDrawBorders(false);
            testDownloadGraph.setNoDataText("");
            testDownloadGraph.invalidate();
        }
        if (rootUpload != null) {
            this.testUploadGraph = rootUpload.findViewById(R.id.test_progress__small_graph);
            testUploadGraph.setDescription(null);    // Hide the description
            testUploadGraph.getAxisLeft().setEnabled(false);
            testUploadGraph.getAxisRight().setEnabled(false);
            testUploadGraph.getXAxis().setEnabled(false);
            testUploadGraph.setDrawMarkers(false);
            testUploadGraph.setAutoScaleMinMaxEnabled(true);
            testUploadGraph.getLegend().setEnabled(false);
            testUploadGraph.setDrawGridBackground(false);
            testUploadGraph.setDrawBorders(false);
            testUploadGraph.setNoDataText("");
            testUploadGraph.invalidate();
        }

    }

    private void resetGraphs() {

        if (this.testGraphView != null) {
            this.testGraphView.removeAllgraphs();
        }
        if (testSignalGraph != null)
            testSignalGraph.reset();
        if (testSpeedGraphDownload != null)
            testSpeedGraphDownload.reset();
        if (testSpeedGraphUpload != null)
            testSpeedGraphUpload.reset();
        if (testDownloadGraph != null)
            if (testDownloadGraph.getLineData() != null) {
                testDownloadGraph.getLineData().clearValues();
            }
        if (testUploadGraph != null)
            if (testUploadGraph.getLineData() != null) {
                testUploadGraph.getLineData().clearValues();
            }
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
            testSpeedGraphDownload = SimpleGraph.addGraph(testGraphView, currentDownloadSpeedGraphColor, graphXAxis);  //SmoothGraph.addGraph(graphView, Color.parseColor("#00f940"), SMOOTHING_DATA_AMOUNT, SMOOTHING_FUNCTION, false);
            testSpeedGraphDownload.setMaxTime(graphXAxis);

            int currentSignalGraphColor;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                currentSignalGraphColor = context.getResources().getColor(R.color.graph_current_signal_value_color, context.getTheme());
            } else {
                currentSignalGraphColor = context.getResources().getColor(R.color.graph_current_signal_value_color);
            }
            testSignalGraph = SimpleGraph.addGraph(testGraphView, currentSignalGraphColor, graphXAxis);

            int currentUploadSpeedGraphColor;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                currentUploadSpeedGraphColor = context.getResources().getColor(R.color.graph_current_speed_upload_value_color, context.getTheme());
            } else {
                currentUploadSpeedGraphColor = context.getResources().getColor(R.color.graph_current_speed_upload_value_color);
            }
            testSpeedGraphUpload = SimpleGraph.addGraph(testGraphView, currentUploadSpeedGraphColor, graphXAxis);  //SmoothGraph.addGraph(graphView, Color.parseColor("#00f940"), SMOOTHING_DATA_AMOUNT, SMOOTHING_FUNCTION, false);
            testSpeedGraphUpload.setMaxTime(graphXAxis);

            //graphView.getLabelInfoVerticalList().add(new GraphLabel(getActivity().getString(R.string.test_dbm), "#f8a000"));
            testGraphView.setRowLinesLabelList(ResultGraphView.SPEED_LABELS);
            if (testGraphView instanceof CustomizableGraphView) {
                ((CustomizableGraphView) testGraphView).setShowLog10Lines(false);
            }
            testGraphView.invalidate();
        }


        testDownloadSpeedDataSeries = new LineDataSet(new ArrayList<Entry>(), "");
//        testDownloadSpeedDataSeries = new LineGraphSeries<>();
        testUploadSpeedDataSeries = new LineDataSet(new ArrayList<Entry>(), "");
//        testUploadSpeedDataSeries = new LineGraphSeries<>();

        testDownloadSpeedDataSeries.setMode(LineDataSet.Mode.LINEAR);
        testDownloadSpeedDataSeries.setDrawValues(false);
        testDownloadSpeedDataSeries.setLineWidth(2f);
        testDownloadSpeedDataSeries.setDrawCircles(false);
        testDownloadSpeedDataSeries.setDrawFilled(true);

        testUploadSpeedDataSeries.setMode(LineDataSet.Mode.LINEAR);
        testUploadSpeedDataSeries.setDrawValues(false);
        testUploadSpeedDataSeries.setLineWidth(2f);
        testUploadSpeedDataSeries.setDrawCircles(false);
        testUploadSpeedDataSeries.setDrawFilled(true);

        testDownloadSpeedDataSeries.setColor(context.getResources().getColor(R.color.graph_current_speed_download_value_color));
        testUploadSpeedDataSeries.setColor(context.getResources().getColor(R.color.graph_current_speed_upload_value_color));
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

            if (testDownloadSpeedDataSeries.getEntryCount() == 0) {
                testDownloadSpeedDataSeries.addEntry(new Entry((float) 0, (float) speedValueRelative));
                Timber.e("Download DATA: 0, %s", speedValueRelative);
            } else {
                testDownloadSpeedDataSeries.addEntry(new Entry((float) (testDownloadSpeedDataSeries.getEntryCount() + 1), (float) speedValueRelative));//appendData(new DataPoint(testDownloadSpeedDataSeries.getHighestValueX() + 10, speedValueRelative), false, 100);
                Timber.e("Download DATA: %s , %s", testDownloadSpeedDataSeries.getEntryCount() + 1 , speedValueRelative);
            }

            LineData lineData = new LineData(testDownloadSpeedDataSeries);
            testDownloadGraph.setData(lineData);
            testDownloadGraph.invalidate();
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
            if (testUploadSpeedDataSeries.getEntryCount() == 0) {
                testUploadSpeedDataSeries.addEntry(new Entry((float) 0, (float) speedValueRelative));
            } else {
                testUploadSpeedDataSeries.addEntry(new Entry((float) (testUploadSpeedDataSeries.getEntryCount() + 1), (float) speedValueRelative));
            }
            LineData lineData = new LineData(testUploadSpeedDataSeries);
            testUploadGraph.setData(lineData);
            testUploadGraph.invalidate();
        }
    }

    public void addNewSignalValue(double progressSegments, Double relativeSignal) {
        if (testGraphView != null) {
            if (relativeSignal != null && testSignalGraph != null)
                testSignalGraph.addValue(relativeSignal, progressSegments * NANO_MULTIPLIER - STARTING_PERCENTAGE + RIGHT_GRAPH_SHIFT);
            testGraphView.invalidate();
        }
    }

    public void signalTypeChanged(Double relativeSignal, NetworkUtil.MinMax<Integer> signalBounds) {
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
        long graphXAxis = 0;
        if (context != null) {
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
        }
        return graphXAxis;
    }

}
