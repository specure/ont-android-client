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
package at.specure.android.test;

import java.text.DecimalFormat;
import java.text.Format;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.specure.opennettest.R;

import at.specure.client.helper.TestStatus;

/**
 * @author lb
 */
@SuppressWarnings("WeakerAccess")
public class SpeedTestStatViewController implements ChangeableSpeedTestStatus {

    public final static int FLAG_NONE = 0;
    public final static int FLAG_HIDE_PROGRESSBAR = 1;
    public final static int FLAG_SHOW_PROGRESSBAR = 2;

    /**
     * !!! KEEP IN ASCENDING ORDERED FORMAT ACCORDING TO {@link #listPosition}
     * this list refer to #TestStatus in TestClient module
     */
    @SuppressWarnings("SpellCheckingInspection")
    public enum InfoStat {
        WAIT(-1, R.string.test_bottom_test_status_init, TestStatus.WAIT),
        INIT(0, R.string.test_bottom_test_status_init, true, true, R.drawable.traffic_lights_green, 16, new DecimalFormat("@@@@ ms"), 1000000.0, TestStatus.INIT),
        PACKET_LOSS_DOWN(2, R.string.test_bottom_test_status_packet_loss_out, false, true, Integer.MIN_VALUE, 0, new DecimalFormat("0.0 %"), 100.0, TestStatus.PACKET_LOSS_AND_JITTER),
        PACKET_LOSS_UP(2, R.string.test_bottom_test_status_packet_loss_in, false, true, Integer.MIN_VALUE, 0, new DecimalFormat("0.0 %"), 100.0, TestStatus.PACKET_LOSS_AND_JITTER),
        JITTER(1, R.string.test_bottom_test_status_jitter, false, true, Integer.MIN_VALUE, 0, new DecimalFormat("@@@ ms"), 1000000.0, TestStatus.PACKET_LOSS_AND_JITTER),
        PING(3, R.string.test_bottom_test_status_ping, true, true, Integer.MIN_VALUE, 17, new DecimalFormat("@@ ms"), 1000000.0, TestStatus.PING),
        DOWNLOAD(4, R.string.test_bottom_test_status_down, false, true, Integer.MIN_VALUE, 55, new DecimalFormat("@@ Mbps"), 1000000.0, TestStatus.DOWN),
        INIT_UPLOAD(5, R.string.test_bottom_test_status_init_up, TestStatus.INIT_UP),
        UPLOAD(6, R.string.test_bottom_test_status_up, false, true, Integer.MIN_VALUE, 55, new DecimalFormat("@@ Mbps"), 1000000.0, TestStatus.UP),
        SPEEDTEST_END(7, R.string.test_bottom_test_status_end, TestStatus.SPEEDTEST_END),
        QOS_TEST_RUNNING(8, R.string.result_page_title_qos, TestStatus.QOS_TEST_RUNNING),
        QOS_END(9, R.string.result_page_title_qos, TestStatus.QOS_END),
        END(10, R.string.test_bottom_test_status_end, TestStatus.END),
        ERROR(11, R.string.test_bottom_test_status_error, TestStatus.ERROR),
        ABORTED(12, R.string.test_bottom_test_status_aborted, TestStatus.ABORTED);

        final private int listPosition;
        final protected int textResId;
        final protected boolean hasProgressBar;
        final protected boolean showInInfoStat;
        final protected int altImageResId;
        final protected int gaugeProgressSegment;
        final protected double roundingValue;
        final protected Format viewFormat;
        final protected TestStatus globalStatusEnumRef;

        InfoStat(int listPosition, int textResId, TestStatus globalStatusEnumRef) {
            this.listPosition = listPosition;
            this.textResId = textResId;
            this.hasProgressBar = false;
            this.altImageResId = Integer.MIN_VALUE;
            this.gaugeProgressSegment = 0;
            this.viewFormat = null;
            this.roundingValue = 0;
            this.globalStatusEnumRef = globalStatusEnumRef;
            this.showInInfoStat = false;
        }

        /**
         * @param listPosition                     probably unused right now
         * @param textResId                        text resource to be shown
         * @param hasProgressBar                   show circular progress bar in info stat list during test
         * @param showInInfoStat                   show this part in info stat (top left corner)
         * @param alternativeImageResIdAfterFinish drawable showed after part is finished
         * @param gaugeProgressSegment             how many pieces it takes on the gauge
         * @param viewFormat                       format for displaying results with units
         * @param roundingValue                    number to divide value incoming from server to round it (default 1)
         * @param globalStatusEnumRef              reference to global enum of statuses
         */
        InfoStat(int listPosition, int textResId, boolean hasProgressBar, boolean showInInfoStat, int alternativeImageResIdAfterFinish, int gaugeProgressSegment, Format viewFormat, double roundingValue, TestStatus globalStatusEnumRef) {
            this.listPosition = listPosition;
            this.textResId = textResId;
            this.hasProgressBar = hasProgressBar;
            this.altImageResId = alternativeImageResIdAfterFinish;
            this.gaugeProgressSegment = gaugeProgressSegment;
            this.viewFormat = viewFormat;
            this.roundingValue = roundingValue;
            this.globalStatusEnumRef = globalStatusEnumRef;
            this.showInInfoStat = showInInfoStat;
        }


        private int getListPosition() {
            return listPosition;
        }

        public int getTextResId() {
            return textResId;
        }

        public boolean hasProgressBar() {
            return hasProgressBar;
        }

        public int getAltImageResId() {
            return altImageResId;
        }

        public String format(Long value) {
            if (value != null) {
                if (viewFormat != null) {
                    return viewFormat.format(value / roundingValue);
                } else {
                    return " - ";
                }
            }
            return " - ";
        }

        public int getProgressForGauge(float currentValue) {
            InfoStat[] values = values();
            int progressSegment = 0;
            for (int i = 0; i < values.length; i++) {
                InfoStat state = values[i];
                if (state == this) {
                    progressSegment += Math.round(state.gaugeProgressSegment * currentValue);
                    return progressSegment;
                } else {
                    progressSegment += state.gaugeProgressSegment;
                }
            }
            return progressSegment;
        }
    }

    final class GroupViewArrayAdapter extends ArrayAdapter<GroupView> {

        private final Activity context;
        private final List<GroupView> groups;

        final class ViewHolder {
            public TextView title;
            public TextView result;
            public ImageView image;
            public ProgressBar progressBar;
        }

        public GroupViewArrayAdapter(Activity context, List<GroupView> objects) {
            super(context, R.layout.test_view_info_list_element, objects);
            this.context = context;
            this.groups = objects;
        }

        @Override
        public
        @NonNull
        View getView(int position, View rowView, @NonNull ViewGroup parent) {
            if (rowView == null) {
                LayoutInflater inflater = context.getLayoutInflater();
                rowView = inflater.inflate(R.layout.test_view_info_list_element, parent, false);
                // configure view holder
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.title = (TextView) rowView.findViewById(R.id.test_view_info_list_title);
                viewHolder.result = (TextView) rowView.findViewById(R.id.test_view_info_list_result);
                viewHolder.image = (ImageView) rowView.findViewById(R.id.test_view_info_list_image);
                viewHolder.progressBar = (ProgressBar) rowView.findViewById(R.id.test_view_info_list_progress_bar);
                rowView.setTag(viewHolder);
            }

            ViewHolder holder = (ViewHolder) rowView.getTag();
            holder.progressBar.setVisibility(View.VISIBLE);

            holder.title.setText(groups.get(position).title);
            holder.result.setText(groups.get(position).result);

            if (groups.get(position).hideProgressBar) {
                holder.result.setText(groups.get(position).result);
                holder.progressBar.setVisibility(View.GONE);
                holder.result.setVisibility(View.VISIBLE);

                int imgResId = groups.get(position).infoType.getAltImageResId();
                if (imgResId != Integer.MIN_VALUE) {
                    holder.result.setVisibility(View.GONE);
                    holder.image.setVisibility(View.VISIBLE);
                    holder.image.setImageResource(imgResId);
                }
            } else if (!groups.get(position).infoType.hasProgressBar) {
                holder.progressBar.setVisibility(View.GONE);
            } else if (forceProgressBarHide) {
                holder.progressBar.setVisibility(View.GONE);
            }

            return rowView;
        }


        /**
         * Find test status item in adapter list if it is marked to be shown in info stat panel
         *
         * @param stat
         * @return position of InfoStat in the list or Null if it is not found
         */
        public Integer findStatPosition(InfoStat stat) {
            for (int i = 0; i < groups.size(); i++) {
                if (groups.get(i).infoType == stat) {
                    return i;
                }
            }
            return null;
        }

        public void updateStat(InfoStat stat, String result, boolean hideProgressBar) {
            Integer statPosition = findStatPosition(stat);
            if (statPosition != null && ((result != null && !result.equals(groups.get(statPosition).result)) || groups.get(statPosition).hideProgressBar != hideProgressBar)) {
                groups.get(statPosition).result = result;
                groups.get(statPosition).hideProgressBar = hideProgressBar;
                notifyDataSetChanged();
            }
        }

    }

    final class GroupView {
        InfoStat infoType;
        String title;
        String result;
        boolean hideProgressBar = false;

        public GroupView(final String title, final InfoStat infoType) {
            this.title = title;
            this.infoType = infoType;
        }

        @Override
        public String toString() {
            return "GroupView [infoType=" + infoType + ", title=" + title
                    + ", result=" + result + "]";
        }
    }

    private final GroupViewArrayAdapter mainTestInfoStatAdapter;

    private boolean forceProgressBarHide = false;

    public SpeedTestStatViewController(Activity context) {

        List<GroupView> groupViewList = new ArrayList<GroupView>();
        for (InfoStat infoStat : InfoStat.values()) {
            if (infoStat.showInInfoStat) {
                groupViewList.add(new GroupView(context.getString(infoStat.getTextResId()), infoStat));
            }
        }

        System.out.println(groupViewList);

        mainTestInfoStatAdapter = new GroupViewArrayAdapter(context, groupViewList);
    }

    public GroupViewArrayAdapter getMainTestInfoStatAdapter() {
        return this.mainTestInfoStatAdapter;
    }

    @Override
    public String getResultInitString() {
        return mainTestInfoStatAdapter.getItem(InfoStat.INIT.getListPosition()).result;
    }

    @Override
    public String getResultPingString() {
        return mainTestInfoStatAdapter.getItem(InfoStat.PING.getListPosition()).result;
    }

    @Override
    public String getResultDownString() {
        return mainTestInfoStatAdapter.getItem(InfoStat.DOWNLOAD.getListPosition()).result;
    }

    @Override
    public String getResultJitterString() {
        return mainTestInfoStatAdapter.getItem(InfoStat.JITTER.getListPosition()).result;
    }

    @Override
    public String getResultPacketLossInString() {
        return mainTestInfoStatAdapter.getItem(InfoStat.PACKET_LOSS_UP.getListPosition()).result;
    }

    @Override
    public String getResultPacketLossOutString() {
        return mainTestInfoStatAdapter.getItem(InfoStat.PACKET_LOSS_DOWN.getListPosition()).result;
    }

    @Override
    public String getResultUpString() {
        return mainTestInfoStatAdapter.getItem(InfoStat.UPLOAD.getListPosition()).result;
    }

    @Override
    public void setResultDownString(String s, Object flag) {
        mainTestInfoStatAdapter.updateStat(InfoStat.DOWNLOAD, s, (Integer) flag == FLAG_HIDE_PROGRESSBAR);
    }

    @Override
    public void setResultUpString(String s, Object flag) {
        mainTestInfoStatAdapter.updateStat(InfoStat.UPLOAD, s, (Integer) flag == FLAG_HIDE_PROGRESSBAR);
    }

    @Override
    public void setResultInitString(String s, Object flag) {
        mainTestInfoStatAdapter.updateStat(InfoStat.INIT, s, (Integer) flag == FLAG_HIDE_PROGRESSBAR);
    }

    @Override
    public void setResultPingString(String s, Object flag) {
        mainTestInfoStatAdapter.updateStat(InfoStat.PING, s, (Integer) flag == FLAG_HIDE_PROGRESSBAR);
    }

    @Override
    public void setResultJitterString(String s, Object flag) {
        mainTestInfoStatAdapter.updateStat(InfoStat.JITTER, s, (Integer) flag == FLAG_HIDE_PROGRESSBAR);
    }

    @Override
    public void setResultPacketLossInString(String s, Object flag) {
        mainTestInfoStatAdapter.updateStat(InfoStat.PACKET_LOSS_UP, s, (Integer) flag == FLAG_HIDE_PROGRESSBAR);
    }

    @Override
    public void setResultPacketLossOutString(String s, Object flag) {
        mainTestInfoStatAdapter.updateStat(InfoStat.PACKET_LOSS_DOWN, s, (Integer) flag == FLAG_HIDE_PROGRESSBAR);
    }

    @Override
    public void setForceHideProgressBar(boolean forceHide) {
        this.forceProgressBarHide = forceHide;
    }

    @Override
    public boolean isForceHideProgressBar() {
        return !this.forceProgressBarHide;
    }

}
