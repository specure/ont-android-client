package at.specure.android.api.jsons.TestResultDetails;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.specure.opennettest.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import at.specure.android.api.jsons.VoipTestResult;
import at.specure.android.screens.test_results.VoipTestResultHandler;
import at.specure.android.util.Helperfunctions;

public class TestResultDetails {


    public ArrayList<HashMap<String, String>> itemList;

    HashMap<String, String> viewItemJitter;
    HashMap<String, String> viewItemPL;

    ArrayList<CellInfoGet> cellsInfo;


    public TestResultDetails(JsonArray testResultsJson, Context activity) {
        itemList = new ArrayList<>();

        viewItemJitter = new HashMap<>();
        viewItemPL = new HashMap<>();


        HashMap<String, String> viewItem;


        for (int i = 0; i < testResultsJson.size(); i++) {

            final JsonObject singleItem = testResultsJson.get(i).getAsJsonObject();

            final JsonElement o = singleItem.get("title");

            if (o != null && o.isJsonObject()) {
                JsonObject json = o.getAsJsonObject();
                final Iterator<Map.Entry<String, JsonElement>> keys = json.entrySet().iterator();
                JsonObject valueJson = null;
                if (json.has("value")) {
                    valueJson = json.getAsJsonObject("value");
                }
                if (valueJson != null) {
                    while (keys.hasNext()) {
                        viewItem = new HashMap<>();
                        Map.Entry<String, JsonElement> next = keys.next();
                        final String key = next.getKey();
                        viewItem.put("name", key);
                        viewItem.put("value", next.getValue().getAsString());
                        itemList.add(viewItem);
                    }
                }

            } else {

                if (singleItem.has(VoipTestResult.JSON_OBJECT_IDENTIFIER)) {
                    VoipTestResultHandler voipTestResultHandler = new VoipTestResultHandler();
                    Gson gson = new Gson();
                    VoipTestResult voipTestResult = gson.fromJson(singleItem.get(VoipTestResult.JSON_OBJECT_IDENTIFIER), VoipTestResult.class);

                    String meanPacketLossInPercent = voipTestResult.getVoipResultPacketLoss();
                    String meanPacketLossTitleString = voipTestResultHandler.getMeanPacketLossString(activity);
                    viewItemJitter.put("name", meanPacketLossTitleString);
                    viewItemJitter.put("value", meanPacketLossInPercent + " %");

                    String meanJitter = voipTestResult.getVoipResultJitter() + " " + activity.getResources().getString(R.string.test_ms);
                    String meanJitterTitleString = voipTestResultHandler.getMeanJitterTitleString(activity);
                    viewItemPL.put("name", meanJitterTitleString);
                    viewItemPL.put("value", meanJitter);

                } else if (singleItem.has(CellsInfoGet.OBJECT_NAME)) {
                    CellsInfoGet cellsInfoGet = new Gson().fromJson(singleItem, CellsInfoGet.class);
                    cellsInfo = cellsInfoGet.cellsInfo;

                    if ((cellsInfo != null) && (!cellsInfo.isEmpty())) {
                        HashMap<String, String> viewItemBandName = new HashMap<>();
                        HashMap<String, String> viewItemBandNumber = new HashMap<>();
                        HashMap<String, String> viewItemBandFrequencyDL = new HashMap<>();
                        HashMap<String, String> viewItemBandFrequencyUpload = new HashMap<>();
                        HashMap<String, String> viewItemBandwidth = new HashMap<>();

                        String bandNameTitle = activity.getResources().getString(R.string.band_name);
                        String bandTitle = activity.getResources().getString(R.string.band_channel);
                        String frequencyULTitle = activity.getResources().getString(R.string.frequency_UL);
                        String frequencyDLTitle = activity.getResources().getString(R.string.frequency_DL);
                        String bandwidthTitle = activity.getResources().getString(R.string.bandwidth);

                        String bandNameValue = null;
                        String bandValue = null;
                        String frequencyULValue = null;
                        String frequencyDLValue = null;
                        String bandwidthValue = null;

                        boolean isFirst = true;

                        for (CellInfoGet cellInfoGet : cellsInfo) {
                            if (cellInfoGet != null) {

                                bandNameValue = mergeStrings(bandNameValue, cellInfoGet.bandName, ", ", "-", isFirst);
                                bandwidthValue = mergeStrings(bandwidthValue, cellInfoGet.bandwidth.toString(), ", ", "-", isFirst);
                                bandValue = mergeStrings(bandValue, cellInfoGet.band.toString(), ", ", "-", isFirst);
                                frequencyULValue = mergeStrings(frequencyULValue, cellInfoGet.frequencyUpload.toString(), ", ", "-", isFirst);
                                frequencyDLValue = mergeStrings(frequencyDLValue, cellInfoGet.frequencyDownload.toString(), ", ", "-", isFirst);
                                isFirst = false;
                            }
                        }

                        viewItemBandName.put("name", bandNameTitle);
                        viewItemBandName.put("value", bandNameValue);

                        viewItemBandwidth.put("name", bandwidthTitle);
                        viewItemBandwidth.put("value", bandwidthValue);

                        viewItemBandNumber.put("name", bandTitle);
                        viewItemBandNumber.put("value", bandValue);

                        viewItemBandFrequencyUpload.put("name", frequencyULTitle);
                        viewItemBandFrequencyUpload.put("value", frequencyULValue);

                        viewItemBandFrequencyDL.put("name", frequencyDLTitle);
                        viewItemBandFrequencyDL.put("value", frequencyDLValue);

                        itemList.add(viewItemBandName);
                        itemList.add(viewItemBandwidth);
                        itemList.add(viewItemBandNumber);
                        itemList.add(viewItemBandFrequencyUpload);
                        itemList.add(viewItemBandFrequencyDL);
                    }
                }
                viewItem = new HashMap<>();
                String title = "";
                if (singleItem.has("title")) {
                    title = singleItem.get("title").getAsString();
                }

                viewItem.put("name", title);


                if ((singleItem.has("time") && (singleItem.has("timezone")))) {
                    final String timeString = Helperfunctions
                            .formatTimestampWithTimezone(singleItem.get("time").getAsLong(),
                                    singleItem.get("timezone").getAsString(), true); // seconds
                    viewItem.put("value", timeString == null ? "-" : timeString);
                } else {
                    String value = "";
                    if (singleItem.has("value")) {
                        value = singleItem.get("value").getAsString();
                    }
                    viewItem.put("value", value);
                }


                itemList.add(viewItem);

            }

            // we added jitter and packet loss info to the 7th position in the test results list (i == 6)
            if ((i == 6) && !viewItemJitter.isEmpty() && (!viewItemPL.isEmpty())) {
                itemList.add(viewItemJitter);
                itemList.add(viewItemPL);
            }


        }
    }

    public String mergeStrings(String first, String second, String merger, String nullSubstitution, boolean isFirst) {
        String result = first;
        if (!isFirst) {
            if (result == null) {
                if (nullSubstitution != null) {
                    result = nullSubstitution;
                }
            }

            if (result != null) {
                result = result + merger;
            }
        } else {
            result = "";
        }

        if (result == null) {
            if (second != null) {
                result = second;
            }
        } else {
            result += second;
        }
        return result;
    }

}
