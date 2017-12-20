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

package at.specure.android.screens.main.main_fragment;

import com.specure.opennettest.R;

/**
 * Created by michal.cadrik on 9/14/2017.
 */

public enum OverlayType {
    IP(R.string.title_screen_ip, R.id.title_page_ip_button),
    CPU_MEM(R.string.title_screen_cpu_mem_info, R.id.title_page_cpu_stats_button),
    TRAFFIC(R.string.title_screen_traffic, R.id.title_page_traffic_button),
    LOCATION(R.string.result_page_title_map, R.id.title_page_location_button),
    MEASUREMENT_SERVERS(R.string.title_screen_info_overlay_measurement_servers, R.id.main_fragment__test_server_container);

    protected int resId;
    protected int buttonId;

    OverlayType(int resId, int buttonId) {
        this.resId = resId;
        this.buttonId = buttonId;
    }

    public int getResourceId() {
        return resId;
    }

    public int getButtonId() {
        return buttonId;
    }

}
