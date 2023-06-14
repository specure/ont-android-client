package at.specure.android.screens.result.fragments;

import android.app.Activity;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonSyntaxException;

import at.specure.android.api.calls.CheckTestResultDetailTask;
import at.specure.android.api.calls.CheckTestResultTask;
import at.specure.android.api.jsons.TestResult.TestResult;
import at.specure.android.api.jsons.TestResultDetailOpenData.OpenDataJson;
import at.specure.android.screens.result.adapter.result.ResultDetailType;
import at.specure.android.util.EndTaskListener;
import at.specure.client.v2.task.result.QoSServerResultCollection;
import timber.log.Timber;

@SuppressWarnings("WeakerAccess")
public class SimpleResultController implements EndTaskListener {

    private TestResult result;
    private QoSServerResultCollection qosResults;
    private ResultLoaderInterface resultInterface;
    private OpenDataJson graphDataOpenDataJson;
    private JsonArray testResultDetails;
    private String testUUID;


    SimpleResultController(ResultLoaderInterface resultInterface) {
        this.resultInterface = resultInterface;
    }

    public void loadTestResults(final String testUUID, final Activity activity) {


        if ((this.result != null) && (activity != null)) {

            if (resultInterface != null) {
                resultInterface.onBasicResultLoaded(result);
                loadQoSResults(testUUID, activity);
                loadTestResultDetails(testUUID, activity);
                loadGraphsData(testUUID, result.getTestOpenUUID(), activity);

                if (qosResults == null) {
                    loadQoSResults(testUUID, activity);
                } else {
                    setTestResultQoSDetails(qosResults);
                }
                if (graphDataOpenDataJson == null) {
                    loadGraphsData(testUUID, result.getTestOpenUUID(), activity);
                } else {
                    resultInterface.onGraphDataLoaded(graphDataOpenDataJson);
                }
            }
        }
        if (activity != null) {
            final CheckTestResultTask testResultTask = new CheckTestResultTask(activity);
            testResultTask.setEndTaskListener(new EndTaskListener() {
                @Override
                public void taskEnded(final JsonArray testResult) {
                    if (testResult != null) {
                        System.out.println("testResultTask.hasError() = " + testResultTask.hasError() + ", testResult.length() = " + testResult.size());
                        Timber.e("SIMPLE  DATA LOADED %s", testResultTask.hasError());
                        Gson gson = new Gson();
                        try {

                            if (testResult.size() > 0) {
                                SimpleResultController.this.result = gson.fromJson(testResult.get(0).getAsJsonObject(), TestResult.class);
                            }

                        } catch (JsonSyntaxException e) {
                            e.printStackTrace();
                            return;
                        }

                        if (resultInterface != null) {
                            resultInterface.onBasicResultLoaded(result);
                        }

                        loadTestResultDetails(testUUID, activity);
                        loadQoSResults(testUUID, activity);
                        loadGraphsData(testUUID, result.getTestOpenUUID(), activity);
                    }
                }
            });

            testResultTask.execute(testUUID);
        }
    }

    private void loadTestResultDetails(String testUUID, final Activity activity) {

        if ((testResultDetails != null) && (this.testUUID.compareToIgnoreCase(testUUID) == 0)) {
            if (resultInterface != null) {
                resultInterface.onDetailResultLoaded(this.testResultDetails);
            } else {
                final CheckTestResultDetailTask checkTestResultDetailTask = new CheckTestResultDetailTask(activity, ResultDetailType.SPEEDTEST);

                checkTestResultDetailTask.setEndTaskListener(new EndTaskListener() {


                    @Override
                    public void taskEnded(JsonArray testResultDetail) {
                        //isCheckingQoSResult.set(false);
                        if (testResultDetail != null && testResultDetail.size() > 0) {
                            SimpleResultController.this.testResultDetails = testResultDetail;


                            System.out.println("testResultDetail: " + testResultDetail);



                            /* */

//                        SimpleAdapter valueList = new SimpleAdapter(activity, itemList, R.layout.test_result_detail_item, new String[]{
//                                "name", "value"}, new int[]{R.id.name, R.id.value});

//                        listView.setAdapter(valueList);


                        } else {
//                        Timber.i(DEBUG_TAG, "LEERE LISTE");
//                        progessBar.setVisibility(View.GONE);
//                        emptyView.setVisibility(View.VISIBLE);
//                        emptyView.setText(activity.getString(R.string.error_no_data));
//                        emptyView.invalidate();
                        }
                    }
                });
                checkTestResultDetailTask.execute(testUUID);
            }
        }
    }


    public void loadGraphsData(String testUUID, String openTestUUID, Activity activity) {

        if ((graphDataOpenDataJson != null) && (this.testUUID != null) && (this.testUUID.compareToIgnoreCase(testUUID) == 0)) {
            if (resultInterface != null) {
                resultInterface.onGraphDataLoaded(graphDataOpenDataJson);
            }
        } else {
            CheckTestResultDetailTask checkTestResultDetailTask = new CheckTestResultDetailTask(activity, ResultDetailType.OPENDATA);

            checkTestResultDetailTask.setEndTaskListener(this);
            checkTestResultDetailTask.execute(testUUID, openTestUUID);
        }
    }


    public void loadQoSResults(String testUUID, Activity activity) {
        if ((qosResults != null) && (this.testUUID != null) && (this.testUUID.compareToIgnoreCase(testUUID) == 0)) {
            setTestResultQoSDetails(qosResults);
        } else {
            if (activity != null) {
                CheckTestResultDetailTask testResultDetailTask = new CheckTestResultDetailTask(activity, ResultDetailType.QUALITY_OF_SERVICE_TEST);

                testResultDetailTask.setEndTaskListener(new EndTaskListener() {


                    @Override
                    public void taskEnded(JsonArray result) {
                        //isCheckingQoSResult.set(false);
                        if (result != null) {
                            qosResults = new QoSServerResultCollection(result);
                            setTestResultQoSDetails(qosResults);
                            Timber.e("SIMPLE %s", result.toString());
                        } else {
                            setTestResultQoSDetails(null);
                        }
                    }
                });

                testResultDetailTask.execute(testUUID);
            }
        }
    }


    /**
     * Here come graph result
     **/
    @Override
    public void taskEnded(JsonArray result) {

        Gson gson = new Gson();
        graphDataOpenDataJson = gson.fromJson(result.get(0).getAsJsonObject(), OpenDataJson.class);
        if (this.resultInterface != null) {
            this.resultInterface.onGraphDataLoaded(graphDataOpenDataJson);
        }
        Timber.e("SIMPLE %S", result.toString());
    }


    private synchronized void setTestResultQoSDetails(QoSServerResultCollection qosResults) {

        Timber.e("QOS_RESULTS %s", qosResults);
//        int testCounter = 0;
//        int failedTestsCounter = 0;
        int percentage = 0;
        if (qosResults != null) {
            QoSServerResultCollection.QoSResultStats qoSStatistics = qosResults.getQoSStatistics();
//            testCounter = qoSStatistics.getTestCounter();
//            failedTestsCounter = qoSStatistics.getFailedTestsCounter();
            percentage = qoSStatistics.getPercentageForTests();
        }
        if (this.resultInterface != null) {
            this.resultInterface.onQosResultLoaded(qosResults, percentage);
        }
    }

    public TestResult getResult() {
        return result;
    }

    public interface ResultLoaderInterface {

        void onGraphDataLoaded(OpenDataJson openDataJson);

        void onBasicResultLoaded(TestResult result);

        void onDetailResultLoaded(JsonArray result);

        void onQosResultLoaded(QoSServerResultCollection qosResult, int percentage);

    }

}
