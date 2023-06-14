package at.specure.android.screens.result.fragments;

import android.content.Context;
import android.os.Build;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

import at.specure.android.api.jsons.TestResultDetailOpenData.GraphSpeedItem;

public class ResultGraphHandler {

    public static void fillResultGraph(LineChart chart, List<GraphSpeedItem> data, Context context) {
        chart.setDescription(null);    // Hide the description
        chart.getAxisLeft().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.getXAxis().setEnabled(false);
        chart.setDrawMarkers(false);
        chart.setAutoScaleMinMaxEnabled(true);
        chart.getLegend().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setDrawBorders(false);
        chart.setNoDataText("");
        chart.invalidate();

        if (chart != null)
            if (chart.getLineData() != null) {
                chart.getLineData().clearValues();
            }

        int graphColor;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            graphColor = context.getResources().getColor(android.R.color.white, context.getTheme());
        } else {
            graphColor = context.getResources().getColor(android.R.color.white);
        }

        LineDataSet testDownloadSpeedDataSeries = new LineDataSet(new ArrayList<Entry>(), "");

        testDownloadSpeedDataSeries.setMode(LineDataSet.Mode.LINEAR);
        testDownloadSpeedDataSeries.setDrawValues(false);
        testDownloadSpeedDataSeries.setLineWidth(0f);
        testDownloadSpeedDataSeries.setDrawCircles(false);
        testDownloadSpeedDataSeries.setDrawFilled(true);


//        testDownloadSpeedDataSeries.setColor(graphColor);
        testDownloadSpeedDataSeries.setColor(graphColor);

        for (GraphSpeedItem datum : data) {
//            if (testDownloadSpeedDataSeries.getEntryCount() == 0) {
//                testDownloadSpeedDataSeries.addEntry(new Entry((float) 0, (float) speedValueRelative));
//                Timber.e("Download DATA: ", "0, " + speedValueRelative);
//            } else {
            testDownloadSpeedDataSeries.addEntry(new Entry((float) (datum.getTimeElapsed()), (float) datum.getBytesTotalTransfered() / datum.getTimeElapsed()));//appendData(new DataPoint(testDownloadSpeedDataSeries.getHighestValueX() + 10, speedValueRelative), false, 100);
//            }
        }

        LineData lineData = new LineData(testDownloadSpeedDataSeries);
        chart.setData(lineData);
        chart.invalidate();

//           SimpleGraph testSpeedGraphDownload = SimpleGraph.addGraph(chart, graphColor, data.get(data.size()-1).get);  //SmoothGraph.addGraph(graphView, Color.parseColor("#00f940"), SMOOTHING_DATA_AMOUNT, SMOOTHING_FUNCTION, false);
//            testSpeedGraphDownload.setMaxTime(graphXAxis);

    }
}
