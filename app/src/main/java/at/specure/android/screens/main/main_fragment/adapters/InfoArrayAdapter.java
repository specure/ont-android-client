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

package at.specure.android.screens.main.main_fragment.adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.specure.opennettest.R;

import java.text.DecimalFormat;
import java.text.Format;
import java.util.ArrayList;
import java.util.List;

import at.specure.android.screens.main.InfoCollector;
import at.specure.android.screens.main.MainActivity;
import at.specure.android.screens.main.main_fragment.OverlayType;
import at.specure.android.screens.main.main_fragment.enums.InfoOverlayEnum;
import at.specure.android.util.Helperfunctions;
import at.specure.android.util.net.InterfaceTrafficGatherer;
import at.specure.android.util.net.NetworkInfoCollector;

/**
 * Created by michal.cadrik on 10/23/2017.
 */

public class InfoArrayAdapter extends ArrayAdapter<InfoOverlayEnum> {

    private List<InfoOverlayEnum> infoList;
    private final Activity context;
    private final OverlayType overlayType;
    private InfoCollector infoCollector;
    private final DecimalFormat percentFormat = new DecimalFormat("##0.0");
    private Format speedFormat;
    private InterfaceTrafficGatherer interfaceTrafficGatherer;

    class ViewHolder {
        public TextView name;
        public TextView value;
    }

    public InfoArrayAdapter(@NonNull Activity context, OverlayType overlayType, InterfaceTrafficGatherer interfaceTrafficGatherer, InfoOverlayEnum... infoArray) {
        super(context, R.layout.test_result_detail_item);
        this.context = context;
        this.interfaceTrafficGatherer = interfaceTrafficGatherer;
        this.overlayType = overlayType;
        this.infoList = new ArrayList<InfoOverlayEnum>();
        for (InfoOverlayEnum e : infoArray) {
            infoList.add(e);
        }
        this.infoCollector = InfoCollector.getInstance();
        speedFormat = new DecimalFormat(String.format("@@ %s", context.getResources().getString(R.string.test_mbps)));
    }

    public OverlayType getOverlayType() {
        return overlayType;
    }

    public void removeElement(InfoOverlayEnum e) {
        if (this.infoList.contains(e)) {
            //System.out.println("removing element: " + e);
            this.infoList.remove(e);
            notifyDataSetChanged();
        }
    }

    public void addElement(InfoOverlayEnum e) {
        addElement(e, Integer.MAX_VALUE);
    }

    public void addElement(InfoOverlayEnum e, int index) {
        if (!this.infoList.contains(e)) {
            //System.out.println("adding element: " + e);
            if (index > this.infoList.size()) {
                this.infoList.add(e);
            } else {
                this.infoList.add(index, e);
            }
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return (infoList != null ? infoList.size() : 0);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View rowView = convertView;
        // reuse views
        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.info_overlay_detail_item, null);
            // configure view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.name = rowView.findViewById(R.id.name);
            viewHolder.value = rowView.findViewById(R.id.value);
            rowView.setTag(viewHolder);
        }

        if (this.infoCollector == null) {
            this.infoCollector = InfoCollector.getInstance();
        }
        if (context != null) {
            // fill data
            ViewHolder holder = (ViewHolder) rowView.getTag();
            holder.name.setText(infoList.get(position).getTitle(context));
            switch (infoList.get(position)) {
                case DL_TRAFFIC:
                    holder.value.setText(speedFormat.format(((double) interfaceTrafficGatherer.getRxRate() / 125000D)));
                    break;
                case UL_TRAFFIC:
                    holder.value.setText(speedFormat.format(((double) interfaceTrafficGatherer.getTxRate() / 125000D)));
                    break;
                case LOCATION:
                    String locationString = "";
                    if (infoCollector.getLocation() != null) {
                        locationString = Helperfunctions.getLocationString(context, context.getResources(), infoCollector.getLocation(), 0);
                    } else {
                        locationString = context.getResources().getString(R.string.not_available);
                    }
                    holder.value.setText(locationString);
                    break;
                case LOCATION_ACCURACY:
                    locationString = "";
                    if (infoCollector.getLocation() != null) {
                        final int satellites;
                        if (infoCollector.getLocation().getExtras() != null)
                            satellites = infoCollector.getLocation().getExtras().getInt("satellites");
                        else
                            satellites = 0;
                        locationString = Helperfunctions.convertLocationAccuracy(context.getResources(),
                                infoCollector.getLocation().hasAccuracy(),
                                infoCollector.getLocation().getAccuracy(),
                                satellites);
                    }
                    holder.value.setText(locationString);
                    break;
                case LOCATION_AGE:
                    locationString = "";
                    if (infoCollector.getLocation() != null) {
                        locationString = Helperfunctions.convertLocationTime(infoCollector.getLocation().getTime());
                    }
                    holder.value.setText(locationString);
                    break;
                case LOCATION_SOURCE:
                    locationString = "";
                    if (infoCollector.getLocation() != null) {
                        locationString = Helperfunctions.convertLocationProvider(context.getResources(), infoCollector.getLocation().getProvider());
                    }
                    holder.value.setText(locationString);
                    break;
                case LOCATION_ALTITUDE:
                    locationString = "";
                    if (infoCollector.getLocation() != null) {
                        if (infoCollector.getLocation().hasAltitude()) {
                            locationString = Helperfunctions.convertLocationAltitude(context.getResources(),
                                    infoCollector.getLocation().hasAltitude(), infoCollector.getLocation().getAltitude());
                        } else {
                            locationString = context.getString(R.string.not_available);
                        }
                    }
                    holder.value.setText(locationString);
                    break;
                case IPV4:
                    NetworkInfoCollector netInfo = ((MainActivity) context).getNetworkInfoCollector();
                    if (netInfo != null && netInfo.getPrivateIpv4() != null) {
                        holder.value.setText(netInfo.getPrivateIpv4().getHostAddress());
                    } else {
                        holder.name.setText(context.getResources().getString(R.string.title_screen_ipv4));
                        holder.value.setText(context.getResources().getString(R.string.not_available));
                    }
                    break;
                case IPV6:
                    netInfo = ((MainActivity) context).getNetworkInfoCollector();
                    if (netInfo != null && netInfo.getPrivateIpv6() != null) {
                        holder.value.setText(netInfo.getPrivateIpv6String());
                    } else {
                        holder.name.setText(context.getResources().getString(R.string.title_screen_ipv6));
                        holder.value.setText(context.getResources().getString(R.string.not_available));
                    }
                    break;
                case IPV4_PUB:
                    netInfo = ((MainActivity) context).getNetworkInfoCollector();
                    if (netInfo != null && netInfo.getPublicIpv4() != null) {
                        holder.value.setText(netInfo.getPublicIpv4());
                    } else {
                        holder.value.setText(context.getResources().getString(R.string.not_available));
                    }
                    break;
                case IPV6_PUB:
                    netInfo = ((MainActivity) context).getNetworkInfoCollector();
                    if (netInfo != null && netInfo.getPublicIpv6() != null) {
                        holder.value.setText(netInfo.getPublicIpv6());
                    } else {
                        holder.value.setText(context.getResources().getString(R.string.not_available));
                    }
                    break;
                case CAPTIVE_PORTAL_STATUS:
                    netInfo = ((MainActivity) context).getNetworkInfoCollector();
                    holder.value.setText(netInfo.getCaptivePortalStatus().getTitle(context));
                    break;
                case CONTROL_SERVER_CONNECTION:
                    netInfo = ((MainActivity) context).getNetworkInfoCollector();
                    holder.value.setText("" + (netInfo != null && netInfo.hasIpFromControlServer()));
                    break;
                case IS_LINK_LOCAL6:
                    netInfo = ((MainActivity) context).getNetworkInfoCollector();
                    if (netInfo != null && netInfo.getPrivateIpv6() != null) {
                        holder.value.setText("" + netInfo.getPrivateIpv6().isLinkLocalAddress());
                    }
                    break;
                case IS_LOOPBACK6:
                    netInfo = ((MainActivity) context).getNetworkInfoCollector();
                    if (netInfo != null && netInfo.getPrivateIpv6() != null) {
                        holder.value.setText("" + netInfo.getPrivateIpv6().isLoopbackAddress());
                    }
                    break;
                case IS_LOOPBACK4:
                    netInfo = ((MainActivity) context).getNetworkInfoCollector();
                    if (netInfo != null && netInfo.getPrivateIpv4() != null) {
                        holder.value.setText("" + netInfo.getPrivateIpv4().isLoopbackAddress());
                    }
                    break;
                case CPU_CORES:
                    holder.value.setText("" + infoCollector.getCpuCoresCount());
                    break;
                case CPU_USAGE:
                    holder.value.setText("" + percentFormat.format(infoCollector.getCpuUsage()) + "%");
                    break;
                case MEM_USAGE:
                    holder.value.setText("" + percentFormat.format(infoCollector.getMemUsage()) + "%");
                    break;
                case MEM_FREE:
                    holder.value.setText("" + infoCollector.getMemFree() + " kB");
                    break;
                case MEM_TOTAL:
                    holder.value.setText("" + infoCollector.getMemTotal() + " kB");
                    break;
            }
        }
        return rowView;

    }
}

