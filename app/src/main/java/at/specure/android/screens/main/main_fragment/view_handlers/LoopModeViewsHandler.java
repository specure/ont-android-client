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
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.specure.opennettest.R;

import java.util.HashMap;

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
        this.viewsToSetGone.add(R.id.main__bottom_info_default_text);
        this.viewsToSetGone.add(R.id.test_view_qos_container);
        this.viewsToSetGone.add(R.id.title_page_map_button);
        this.viewsToSetGone.add(R.id.text_view_upper_test);


        this.viewsToSetVisible.add(R.id.main_fragment__top_info_container);
        this.viewsToSetVisible.add(R.id.start_button_container);
        this.viewsToSetVisible.add(R.id.text_view_lower_test);

    }

    @Override
    public void initializeViews(View rootView, Context context) {
        super.setViewVisibility();
        if (rootView != null) {
            disableClickingOnButtons(rootView);

            TextView startButtonText = rootView.findViewById(R.id.start_button_text);
            if (startButtonText != null) {
                startButtonText.setText(R.string.loop_mode_stop);
                startButtonText.setTextSize(14);
            }

            ImageView startButton = rootView.findViewById(R.id.title_page_start_button);
            setOnClickListener(startButton);
        }
    }
}
