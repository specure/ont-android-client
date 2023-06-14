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
package at.specure.android.screens.map.overlay;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.specure.opennettest.R;

import at.specure.android.util.Helperfunctions;
import at.specure.android.util.SemaphoreColorHelper;
import timber.log.Timber;

public class BalloonOverlayView extends View {
    private static final String DEBUG_TAG = "RMBTBalloonOverlayView";
    private static LinearLayout resultListView;
    private static TextView emptyView;
    private static ProgressBar progessBar;
    //    private TextView title;
    private JsonArray resultItems;
    private Context context;

    public BalloonOverlayView(final Context context) {
        super(context);
    }

    public View setupView(final Context context, final ViewGroup parent) {
        this.context = context;

        // inflate our custom layout into parent
        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View v = inflater.inflate(R.layout.balloon_overlay, parent);
        // setup our fields
//        title = (TextView) v.findViewById(R.id.balloon_item_title);

        resultListView = v.findViewById(R.id.resultList);
        resultListView.setVisibility(View.GONE);

        emptyView = v.findViewById(R.id.infoText);
        emptyView.setVisibility(View.GONE);

        progessBar = v.findViewById(R.id.progressBar);

        return v;

    }

    public void setBalloonData(final BalloonOverlayItem item, final ViewGroup parent) {
        // map our custom item data to fields
//        title.setText(item.getTitle());
        resultItems = item.getResultItems();

        resultListView.removeAllViews();

        final float scale = getResources().getDisplayMetrics().density;

        final int leftRightItem = Helperfunctions.dpToPx(5, scale);
        final int topBottomItem = Helperfunctions.dpToPx(3, scale);

        final int leftRightDiv = Helperfunctions.dpToPx(0, scale);
        final int topBottomDiv = Helperfunctions.dpToPx(0, scale);
        final int heightDiv = Helperfunctions.dpToPx(1, scale);

        final int topBottomImg = Helperfunctions.dpToPx(1, scale);

        if (resultItems != null && resultItems.size() > 0) {

            for (int i = 0; i < 1; i++)
                // JSONObject resultListItem;
                try {
                    final JsonObject result = resultItems.get(i).getAsJsonObject();

                    final LayoutInflater resultInflater = (LayoutInflater) context
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                    final View resultView = resultInflater.inflate(R.layout.balloon_overlay_listitem, parent);

                    final LinearLayout measurementLayout = resultView
                            .findViewById(R.id.resultMeasurementList);
                    measurementLayout.setVisibility(View.GONE);

                    final LinearLayout deviceInfoLayout = resultView
                            .findViewById(R.id.resultDeviceInfoList);
                    measurementLayout.setVisibility(View.GONE);

                    final LinearLayout netLayout = resultView.findViewById(R.id.resultNetList);
                    netLayout.setVisibility(View.GONE);

                    final TextView measurementHeader = resultView.findViewById(R.id.resultMeasurement);
                    measurementHeader.setVisibility(View.GONE);

                    final TextView deviceInfoHeader = resultView.findViewById(R.id.resultDeviceInfo);
                    deviceInfoHeader.setVisibility(View.GONE);

                    final TextView netHeader = resultView.findViewById(R.id.resultNet);
                    netHeader.setVisibility(View.GONE);

                    final TextView dateHeader = resultView.findViewById(R.id.resultDate);
                    dateHeader.setVisibility(View.GONE);

                    String time = null;
                    if (result.has("time_string")) {
                        time = result.get("time_string").getAsString();
                    }
                    dateHeader.setText(time);

                    final JsonArray measurementArray = result.getAsJsonArray("measurement");

                    JsonArray deviceInfoArray;
                    if (result.has("device")) {
                        deviceInfoArray = result.getAsJsonArray("device");
                        for (int j = 0; j < deviceInfoArray.size(); j++) {
                            final JsonObject singleItem = deviceInfoArray.get(j).getAsJsonObject();

                            final LinearLayout deviceInfoItemLayout = new LinearLayout(context); // (LinearLayout)measurememtItemView.findViewById(R.id.measurement_item);

                            deviceInfoItemLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT));

                            deviceInfoItemLayout.setGravity(Gravity.CENTER_VERTICAL);
                            deviceInfoItemLayout.setPadding(leftRightItem, topBottomItem, leftRightItem, topBottomItem);

                            final TextView itemTitle = new TextView(context, null, R.style.balloonResultItemTitle);
                            itemTitle.setLayoutParams(new LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT, 0.4f));
                            itemTitle.setTextAppearance(context, R.style.balloonResultItemTitle);
                            itemTitle.setWidth(0);
                            itemTitle.setGravity(Gravity.LEFT);
                            itemTitle.setText(singleItem.get("title").getAsString());

                            deviceInfoItemLayout.addView(itemTitle);

                            final ImageView itemClassification = new ImageView(context);
                            itemClassification.setLayoutParams(new LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT, 0.1f));
                            itemClassification.setPadding(0, topBottomImg, 0, topBottomImg);
                            // itemClassification.set setGravity(Gravity.LEFT);

                            itemClassification.setImageDrawable(getResources().getDrawable(
                                    SemaphoreColorHelper.resolveSemaphoreImage(-1)));

                            deviceInfoItemLayout.addView(itemClassification);

                            final TextView itemValue = new TextView(context, null, R.style.balloonResultItemValue);
                            itemValue.setLayoutParams(new LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT, 0.5f));
                            itemValue.setTextAppearance(context, R.style.balloonResultItemValue);
                            itemValue.setWidth(0);
                            itemValue.setGravity(Gravity.LEFT);
                            itemValue.setText(singleItem.get("value").getAsString());

                            deviceInfoItemLayout.addView(itemValue);

                            deviceInfoLayout.addView(deviceInfoItemLayout);

                            final View divider = new View(context);
                            divider.setLayoutParams(new LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT, heightDiv, 1));
                            divider.setPadding(leftRightDiv, topBottomDiv, leftRightDiv, topBottomDiv);

                            divider.setBackgroundResource(R.drawable.bg_trans_light_10);

                            deviceInfoLayout.addView(divider);

                            deviceInfoLayout.invalidate();
                        }

                        deviceInfoLayout.setVisibility(View.VISIBLE);
                        deviceInfoHeader.setVisibility(View.VISIBLE);

                    }

                    final JsonArray netArray = result.getAsJsonArray("net");

                    for (int j = 0; j < measurementArray.size(); j++) {

                        final JsonObject singleItem = measurementArray.get(j).getAsJsonObject();

                        final LinearLayout measurememtItemLayout = new LinearLayout(context); // (LinearLayout)measurememtItemView.findViewById(R.id.measurement_item);

                        measurememtItemLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));

                        measurememtItemLayout.setGravity(Gravity.CENTER_VERTICAL);
                        measurememtItemLayout.setPadding(leftRightItem, topBottomItem, leftRightItem, topBottomItem);

                        final TextView itemTitle = new TextView(context, null, R.style.balloonResultItemTitle);
                        itemTitle.setLayoutParams(new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT, 0.4f));
                        itemTitle.setTextAppearance(context, R.style.balloonResultItemTitle);
                        itemTitle.setWidth(0);
                        itemTitle.setGravity(Gravity.LEFT);
                        itemTitle.setText(singleItem.get("title").getAsString());

                        measurememtItemLayout.addView(itemTitle);

                        final ImageView itemClassification = new ImageView(context);
                        itemClassification.setLayoutParams(new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT, 0.1f));
                        itemClassification.setPadding(0, topBottomImg, 0, topBottomImg);
                        // itemClassification.set setGravity(Gravity.LEFT);

                        itemClassification.setImageDrawable(getResources().getDrawable(
                                SemaphoreColorHelper.resolveSemaphoreImage(singleItem.get("classification").getAsInt())));
                        
                        measurememtItemLayout.addView(itemClassification);

                        final TextView itemValue = new TextView(context, null, R.style.balloonResultItemValue);
                        itemValue.setLayoutParams(new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT, 0.5f));
                        itemValue.setTextAppearance(context, R.style.balloonResultItemValue);
                        itemValue.setWidth(0);
                        itemValue.setGravity(Gravity.LEFT);
                        itemValue.setText(singleItem.get("value").getAsString());

                        measurememtItemLayout.addView(itemValue);

                        measurementLayout.addView(measurememtItemLayout);

                        final View divider = new View(context);
                        divider.setLayoutParams(new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT, heightDiv, 1));
                        divider.setPadding(leftRightDiv, topBottomDiv, leftRightDiv, topBottomDiv);

                        divider.setBackgroundResource(R.drawable.bg_trans_light_10);

                        measurementLayout.addView(divider);

                        measurementLayout.invalidate();
                    }

                    //JITTER AND PACKET LOSS

//                    VoipTestResultHandler voipTestResultHandler = new VoipTestResultHandler();
//                    String packetLossTitle = voipTestResultHandler.getMeanPacketLossString(this.getContext());
//                    String jitterTitle = voipTestResultHandler.getMeanJitterTitleString(this.getContext());
//                    String packetLossValue = " - ";
//                    String jitterValue = " - ";
//                    int jitterClasification = -1;
//                    int packetLossClasification = -1;
//
//
//                    if (result.has(VoipTestResult.JSON_OBJECT_IDENTIFIER)) {
//                        Gson gson = new Gson();
//                        VoipTestResult voipTestResult = gson.fromJson(result.get(VoipTestResult.JSON_OBJECT_IDENTIFIER), VoipTestResult.class);
//                        packetLossValue = voipTestResult.getVoipResultPacketLoss() + " %";
//                        jitterValue = voipTestResult.getVoipResultJitter() + " " + getResources().getString(R.string.test_ms);
//                        jitterClasification = voipTestResult.getClassificationJitter();
//                        packetLossClasification = voipTestResult.getClassificationPacketLoss();
//                    }
//
//
//                    addJitterAndPacketLoss(packetLossTitle, packetLossValue, packetLossClasification, leftRightItem, topBottomItem, leftRightDiv, topBottomDiv, heightDiv, topBottomImg, measurementLayout);
//                    addJitterAndPacketLoss(jitterTitle, jitterValue, jitterClasification, leftRightItem, topBottomItem, leftRightDiv, topBottomDiv, heightDiv, topBottomImg, measurementLayout);

                    // END PACKET LOSS

                    for (int j = 0; j < netArray.size(); j++) {

                        final JsonObject singleItem = netArray.get(j).getAsJsonObject();

                        final LinearLayout netItemLayout = new LinearLayout(context); // (LinearLayout)measurememtItemView.findViewById(R.id.measurement_item);

                        netItemLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));
                        netItemLayout.setPadding(leftRightItem, topBottomItem, leftRightItem, topBottomItem);

                        netItemLayout.setGravity(Gravity.CENTER_VERTICAL);

                        final TextView itemTitle = new TextView(context, null, R.style.balloonResultItemTitle);
                        itemTitle.setLayoutParams(new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT, 0.4f));
                        itemTitle.setTextAppearance(context, R.style.balloonResultItemTitle);
                        itemTitle.setWidth(0);
                        itemTitle.setGravity(Gravity.LEFT);
                        itemTitle.setText(singleItem.get("title").getAsString());

                        netItemLayout.addView(itemTitle);

                        final ImageView itemClassification = new ImageView(context);
                        itemClassification.setLayoutParams(new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT, 0.1f));
                        itemClassification.setPadding(0, topBottomImg, 0, topBottomImg);

                        itemClassification.setImageDrawable(context.getResources().getDrawable(
                                R.drawable.traffic_lights_none));
                        netItemLayout.addView(itemClassification);

                        final TextView itemValue = new TextView(context, null, R.style.balloonResultItemValue);
                        itemValue.setLayoutParams(new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT, 0.5f));
                        itemValue.setTextAppearance(context, R.style.balloonResultItemValue);
                        itemValue.setWidth(0);
                        itemValue.setGravity(Gravity.LEFT);
                        String value = null;
                        if (singleItem.has("value")) {
                            value = singleItem.get("value").getAsString();
                        }
                        itemValue.setText(value);

                        netItemLayout.addView(itemValue);

                        netLayout.addView(netItemLayout);

                        final View divider = new View(context);
                        divider.setLayoutParams(new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT, heightDiv, 1));
                        divider.setPadding(leftRightDiv, topBottomDiv, leftRightDiv, topBottomDiv);

                        divider.setBackgroundResource(R.drawable.bg_trans_light_10);

                        netLayout.addView(divider);

                        netLayout.invalidate();
                    }

                    measurementHeader.setVisibility(View.VISIBLE);
                    netHeader.setVisibility(View.VISIBLE);

                    measurementLayout.setVisibility(View.VISIBLE);
                    netLayout.setVisibility(View.VISIBLE);

                    dateHeader.setVisibility(View.VISIBLE);

                    resultListView.addView(resultView);

                    Timber.d( "View Added");
                    // codeText.setText(resultListItem.getString("sync_code"));

                } catch (final JsonParseException e) {
                    e.printStackTrace();
                }

            progessBar.setVisibility(View.GONE);
            emptyView.setVisibility(View.GONE);

            resultListView.setVisibility(View.VISIBLE);

            resultListView.invalidate();
        } else {
            Timber.i( "LEERE LISTE");
            progessBar.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText(context.getString(R.string.error_no_data));
            emptyView.invalidate();
        }
    }

    private void addJitterAndPacketLoss(String title, String value, int itemClasification, int leftRightItem, int topBottomItem, int leftRightDiv, int topBottomDiv, int heightDiv, int topBottomImg, LinearLayout measurementLayout) {
        final LinearLayout measurememtItemLayout = new LinearLayout(context); // (LinearLayout)measurememtItemView.findViewById(R.id.measurement_item);

        measurememtItemLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        measurememtItemLayout.setGravity(Gravity.CENTER_VERTICAL);
        measurememtItemLayout.setPadding(leftRightItem, topBottomItem, leftRightItem, topBottomItem);

        final TextView itemTitle = new TextView(context, null, R.style.balloonResultItemTitle);
        itemTitle.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0.4f));
        itemTitle.setTextAppearance(context, R.style.balloonResultItemTitle);
        itemTitle.setWidth(0);
        itemTitle.setGravity(Gravity.LEFT);
        itemTitle.setText(title);

        measurememtItemLayout.addView(itemTitle);

        final ImageView itemClassification = new ImageView(context);
        itemClassification.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0.1f));
        itemClassification.setPadding(0, topBottomImg, 0, topBottomImg);
        // itemClassification.set setGravity(Gravity.LEFT);

        itemClassification.setImageDrawable(getResources().getDrawable(
                SemaphoreColorHelper.resolveSemaphoreImage(itemClasification)));

        measurememtItemLayout.addView(itemClassification);

        final TextView itemValue = new TextView(context, null, R.style.balloonResultItemValue);
        itemValue.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, 0.5f));
        itemValue.setTextAppearance(context, R.style.balloonResultItemValue);
        itemValue.setWidth(0);
        itemValue.setGravity(Gravity.LEFT);
        itemValue.setText(value);

        measurememtItemLayout.addView(itemValue);

        measurementLayout.addView(measurememtItemLayout);

        final View divider = new View(context);
        divider.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, heightDiv, 1));
        divider.setPadding(leftRightDiv, topBottomDiv, leftRightDiv, topBottomDiv);

        divider.setBackgroundResource(R.drawable.bg_trans_light_10);

        measurementLayout.addView(divider);

        measurementLayout.invalidate();
    }
}
