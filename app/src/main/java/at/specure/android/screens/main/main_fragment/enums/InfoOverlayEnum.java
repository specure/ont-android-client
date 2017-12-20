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

package at.specure.android.screens.main.main_fragment.enums;

import android.content.Context;

import com.specure.opennettest.R;

/**
 * Created by michal.cadrik on 10/23/2017.
 */

public enum InfoOverlayEnum {
    IPV4(R.string.title_screen_info_overlay_ipv4_private),
    IS_LOOPBACK4(R.string.title_screen_info_overlay_is_loopback),
    IPV6(R.string.title_screen_info_overlay_ipv6_private),
    IS_LOOPBACK6(R.string.title_screen_info_overlay_is_loopback),
    IS_LINK_LOCAL6(R.string.title_screen_info_overlay_is_link_local),
    IPV4_PUB(R.string.title_screen_info_overlay_ipv4_public),
    IPV6_PUB(R.string.title_screen_info_overlay_ipv6_public),
    UL_TRAFFIC(R.string.title_screen_info_overlay_ul_traffic),
    DL_TRAFFIC(R.string.title_screen_info_overlay_dl_traffic),
    CONTROL_SERVER_CONNECTION(R.string.title_screen_info_overlay_control_server_conn),
    CAPTIVE_PORTAL_STATUS(R.string.title_screen_info_overlay_captive_portal_status),
    LOCATION_ACCURACY(R.string.title_screen_info_overlay_location_accuracy),
    LOCATION_AGE(R.string.title_screen_info_overlay_location_age),
    LOCATION_SOURCE(R.string.title_screen_info_overlay_location_source),
    LOCATION_ALTITUDE(R.string.title_screen_info_overlay_location_altitude),
    LOCATION(R.string.title_screen_info_overlay_location_position),
    CPU_USAGE(R.string.title_screen_info_overlay_cpu_usage),
    CPU_CORES(R.string.title_screen_info_overlay_cpu_cores),
    MEM_USAGE(R.string.title_screen_info_overlay_mem_usage),
    MEM_FREE(R.string.title_screen_info_overlay_mem_free),
    MEM_TOTAL(R.string.title_screen_info_overlay_mem_total),
    MEASUREMENT_SERVERS(R.string.title_screen_info_overlay_measurement_servers);

    protected final int resourceId;
    protected final String title;

    InfoOverlayEnum(final int resourceId) {
        this.resourceId = resourceId;
        this.title = null;
    }

    public String getTitle(Context context) {
        if (title != null) {
            return title;
        }
        return context.getString(resourceId);
    }

}
