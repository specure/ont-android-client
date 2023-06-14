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
import androidx.core.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.specure.opennettest.R;
import java.util.HashMap;

import at.specure.android.views.CustomGauge;

import static at.specure.android.screens.main.main_fragment.MainMenuFragment.PROGRESS_SEGMENTS_PROGRESS_RING;
import static at.specure.android.screens.main.main_fragment.MainMenuFragment.PROGRESS_SEGMENTS_QOS;

/**
 * Class handling UI for loop mode state of the main screen fragment
 * Created by michal.cadrik on 10/12/2017.
 */

public class LoopModeViewsHandler extends ViewsHandler {


    public LoopModeViewsHandler(View rootView, HashMap<Integer, View.OnClickListener> onClickListeners) {
        super(rootView, onClickListeners);

        this.viewsToSetGone.add(R.id.show_detailed_result_button);
        this.viewsToSetGone.add(R.id.test_view_qos_results_container);
        this.viewsToSetGone.add(R.id.main_fragment__top_info_container_measurement);
        this.viewsToSetGone.add(R.id.test_view_qos_container);
        this.viewsToSetGone.add(R.id.graph_container);
        this.viewsToSetGone.add(R.id.test_graph);
        this.viewsToSetGone.add(R.id.title_page_map_button);
        this.viewsToSetGone.add(R.id.text_view_upper_test);


        this.viewsToSetVisible.add(R.id.main_fragment__top_info_container);
        this.viewsToSetVisible.add(R.id.start_button_container);
        this.viewsToSetVisible.add(R.id.text_view_lower_test);

        this.viewsToSetInvisble.add(R.id.measurement_graphs_container);
        this.viewsToSetInvisble.add(R.id.main__bottom_info_default_text);
    }

    @Override
    public void initializeViews(View rootView, Context context) {
        super.setViewVisibility();
        if ((rootView != null) && (context != null)) {
            disableClickingOnButtons(rootView);

            TextView startButtonText = rootView.findViewById(R.id.start_button_text);
            if (startButtonText != null) {
                startButtonText.setText(R.string.loop_mode_stop);
                startButtonText.setTextSize(14);
            }

            ImageView startButton = rootView.findViewById(R.id.title_page_start_button);
            setOnClickListener(startButton);

            CustomGauge gaugeUpper = rootView.findViewById(R.id.gauge_upper);
            if (gaugeUpper != null) {
                gaugeUpper.setmStrokeInnerColor(ContextCompat.getColor(context, R.color.gauge_inner_ring_dark));
                gaugeUpper.setGaugeStrings(0);
                gaugeUpper.setDividerSize(0);
                gaugeUpper.invalidate();
                gaugeUpper.setValue(PROGRESS_SEGMENTS_PROGRESS_RING);
            }

            CustomGauge gaugeLower = rootView.findViewById(R.id.gauge_lower);
            if (gaugeLower != null) {
                gaugeLower.setmStrokeInnerColor(ContextCompat.getColor(context, R.color.gauge_basic));
                gaugeLower.setValue(PROGRESS_SEGMENTS_QOS);
                gaugeLower.setShowScale(false);
                gaugeLower.setShowArrow(false);
                gaugeLower.invalidate();
            }


        }
    }
}
