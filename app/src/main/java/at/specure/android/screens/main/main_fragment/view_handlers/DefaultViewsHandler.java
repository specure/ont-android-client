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

package at.specure.android.screens.main.main_fragment.view_handlers;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.specure.opennettest.R;

import java.util.HashMap;

/**
 * Class handling UI for default state of the main screen fragment
 * Created by michal.cadrik on 10/12/2017.
 */
public class DefaultViewsHandler extends ViewsHandler {


    public DefaultViewsHandler(View rootView, HashMap<Integer, View.OnClickListener> onClickListeners) {
        super(rootView, onClickListeners);
        this.viewsToSetVisible.add(R.id.title_page_start_button);
        this.viewsToSetVisible.add(R.id.title_page_map_button);
        this.viewsToSetVisible.add(R.id.main_fragment__top_info_container);
        this.viewsToSetVisible.add(R.id.main__bottom_info_default_text);
        this.viewsToSetVisible.add(R.id.start_button_container);
        this.viewsToSetGone.add(R.id.text_view_upper_test);

        this.viewsToSetGone.add(R.id.text_view_lower_test);
        this.viewsToSetGone.add(R.id.show_detailed_result_button);
        this.viewsToSetGone.add(R.id.test_view_qos_results_container);
        this.viewsToSetVisible.add(R.id.main_fragment__top_info_container_measurement);
        this.viewsToSetGone.add(R.id.test_view_qos_container);
        this.viewsToSetGone.add(R.id.increased_consumption_button_text);
        this.viewsToSetGone.add(R.id.info_signal_strength_extra);
        this.viewsToSetGone.add(R.id.test_view_info_container);

        this.viewsToSetInvisble.add(R.id.main_fragment__cell_id_container);
    }

    @Override
    public void initializeViews(View rootView, Context context) {
        super.setViewVisibility();
        if (rootView != null) {

            ImageView startButton = rootView.findViewById(R.id.title_page_start_button);
            setOnClickListener(startButton);

            //enable clicking
            enableClickingOnButtons(rootView);
            setOnClickListeners(rootView, onClickListeners);

            TextView startButtonText = rootView.findViewById(R.id.start_button_text);
            if (startButtonText != null) {
                startButtonText.setText(R.string.menu_button_start);
                startButtonText.setTextSize(30);
            }

            PorterDuff.Mode mMode = PorterDuff.Mode.SRC_ATOP;

            ImageView increasedConsumptionInfoButton = rootView.findViewById(R.id.increased_consumption_button);
            final TextView increasedConsumptionInfoText = rootView.findViewById(R.id.increased_consumption_button_text);
            final ScrollView scrollView = rootView.findViewById(R.id.main__scroll_view);

            if ((increasedConsumptionInfoButton != null) &&(increasedConsumptionInfoText != null)) {
                Drawable increasedConsumptionDrawable = context.getResources().getDrawable(R.drawable.ic_action_help);
                increasedConsumptionDrawable.setColorFilter(context.getResources().getColor(R.color.titlepage_stats_foreground), mMode);
                increasedConsumptionInfoButton.setImageDrawable(increasedConsumptionDrawable);
                increasedConsumptionInfoButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int visibility = increasedConsumptionInfoText.getVisibility();
                        if (visibility == View.GONE) {
                            increasedConsumptionInfoText.setVisibility(View.VISIBLE);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (scrollView != null) {
                                        scrollView.fullScroll(View.FOCUS_DOWN);
                                    }
                                }
                            }, 500);

                        } else {
                            increasedConsumptionInfoText.setVisibility(View.GONE);
                        }
                    }
                });
            }

            ImageView serverIcon = rootView.findViewById(R.id.main_fragment__test_server_icon);
            if (serverIcon != null) {
                Drawable serverDrawable = context.getResources().getDrawable(R.drawable.select);
                serverDrawable.setColorFilter(context.getResources().getColor(R.color.titlepage_stats_foreground), mMode);
                serverIcon.setImageDrawable(serverDrawable);
            }

        }
    }


}
