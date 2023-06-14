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

import com.specure.opennettest.R;
import java.util.HashMap;

import at.specure.android.views.CustomGauge;

/**
 * Class handling UI for test performing mode state of the main screen fragment
 * Created by michal.cadrik on 10/12/2017.
 */

public class TestViewsHandler extends ViewsHandler {


    public TestViewsHandler(View rootView, HashMap<Integer, View.OnClickListener> onClickListeners) {
        super(rootView, onClickListeners);
        this.viewsToSetGone.add(R.id.title_page_start_button);
        this.viewsToSetGone.add(R.id.title_page_map_button);
        this.viewsToSetGone.add(R.id.text_view_lower_test);
        this.viewsToSetGone.add(R.id.show_detailed_result_button);
        this.viewsToSetGone.add(R.id.test_view_qos_results_container);
        this.viewsToSetGone.add(R.id.main_fragment__top_info_container);
        this.viewsToSetGone.add(R.id.main__bottom_info_default_text);
        this.viewsToSetGone.add(R.id.test_view_qos_container);
        this.viewsToSetGone.add(R.id.start_button_container);
        this.viewsToSetGone.add(R.id.title_page_map_button);
        this.viewsToSetGone.add(R.id.test_view_info_container);


        this.viewsToSetVisible.add(R.id.text_view_upper_test);
        this.viewsToSetVisible.add(R.id.test_graph);
        this.viewsToSetVisible.add(R.id.graph_container);
        this.viewsToSetVisible.add(R.id.measurement_graphs_container);
        this.viewsToSetVisible.add(R.id.main_fragment__top_info_container_measurement);
    }

    @Override
    public void initializeViews(View rootView, Context context) {
        super.setViewVisibility();
        if ((rootView != null) && (context != null)) {
            disableClickingOnButtons(rootView);

            CustomGauge testViewUpper = rootView.findViewById(R.id.gauge_upper);
            if (testViewUpper != null) {
                testViewUpper.setmStrokeInnerColor(ContextCompat.getColor(context, R.color.gauge_inner_ring_dark));
                testViewUpper.setGaugeStrings(R.array.strings_for_upper_curve);
                testViewUpper.setStringShifts(R.array.strings_shifts_for_upper_curve);
                testViewUpper.setDividerSize(1);
                testViewUpper.setValue(0);
                testViewUpper.setEndValue(143);
                testViewUpper.invalidate();
            }

            CustomGauge testViewLower = rootView.findViewById(R.id.gauge_lower);
            if (testViewLower != null) {
                testViewLower.setmStrokeInnerColor(ContextCompat.getColor(context, R.color.gauge_inner_ring_dark));
                testViewLower.setValue(-1);
                testViewLower.setShowScale(true);
                testViewLower.setShowArrow(true);
                testViewLower.setEndValue(143);
                testViewLower.invalidate();
            }



        }
    }
}
