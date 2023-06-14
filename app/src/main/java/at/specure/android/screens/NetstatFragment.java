/*
 Copyright 2015 SPECURE GmbH

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package at.specure.android.screens;

import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.specure.opennettest.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import at.specure.android.base.BaseFragment;
import at.specure.android.impl.CpuStatAndroidImpl;
import at.specure.android.impl.MemInfoAndroidImpl;
import at.specure.android.impl.NetStatAndroidImpl;
import at.specure.android.impl.TracerouteAndroidImpl;
import at.specure.util.tools.ConnectionInformation;
import at.specure.util.tools.CpuStat;
import at.specure.util.tools.MemInfo;
import at.specure.util.tools.NetStat;
import at.specure.util.tools.TracerouteService;

public class NetstatFragment extends BaseFragment {

	private final static DecimalFormat PERCENT_FORMAT = new DecimalFormat("##0.000");
	NetStat netStat;
	CpuStat cpuStat;
	MemInfo memInfo;
	TracerouteService traceroute;
	float[] cpu;
	Handler handler = new Handler();
	TextView cpuView;
	Runnable cpuUsageRunnable = new Runnable() {

		@Override
		public void run() {

			String mem = "";
			memInfo.update();
			mem += "Total memory: " + memInfo.getTotalMem() + " kB\n";
			mem += "Free memory: " + memInfo.getFreeMem() + " kB\n";
			mem += "File buffers: " + memInfo.getMemoryMap().get("Buffers") + " kB\n";
			mem += "Cache memory: " + memInfo.getMemoryMap().get("Cached") + " kB\n";

			cpu = cpuStat.update(false);
			String cpuInfo = "";
			if (cpu != null) {
				//cpuInfo = "CORES FOUND: " + cpu.length + "\n";
				for (int i = 0; i < cpu.length; i++) {
					if (Float.isNaN(cpu[i])) {
						cpu[i] = 0f;
					}

					cpuInfo += "CPU: " + PERCENT_FORMAT.format(cpu[i]).toString() + "%\n";
				}
			} else {
				cpuInfo = "No cpu stats yet...\n";
			}

			cpuView.setText(cpuInfo + "\n" + mem);

			handler.postDelayed(cpuUsageRunnable, 1000);
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		netStat = new NetStatAndroidImpl();
		cpuStat = new CpuStatAndroidImpl();
		memInfo = new MemInfoAndroidImpl();
		traceroute = new TracerouteAndroidImpl();
	}
	
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

		final View view = inflater.inflate(R.layout.netstat_fragment, container, false);
		final ListView listView = view.findViewById(R.id.valueList);
		cpuView = view.findViewById(R.id.cpu_usage);
		final List<ConnectionInformation> connList = netStat.getConnectionList();
		final ArrayList<HashMap<String, String>> itemList = new ArrayList<>();

		HashMap<String, String> viewItem;

        for (int i = 0; i < connList.size(); i++)
        {

			ConnectionInformation conn = connList.get(i);

			viewItem = new HashMap<>();
			viewItem.put("protocol", conn.getProtocolType().name());
            viewItem.put("localAddr", conn.getLocalAddr().toString());
            viewItem.put("state", conn.getConnectionState().name());

            if (conn.getRemoteAddr() != null) {
            	viewItem.put("remoteAddr", conn.getRemoteAddr().toString());
            }
            else {
            	viewItem.put("remoteAddr", "");
            }

			itemList.add(viewItem);
		}

		SimpleAdapter valueList = new SimpleAdapter(getActivity(), itemList, R.layout.netstat_detail_item,
				new String[]{"protocol", "remoteAddr", "localAddr", "state"},
				new int[]{R.id.protocol, R.id.remoteAddr, R.id.localAddress, R.id.state});

		listView.setAdapter(valueList);

		listView.invalidate();

//        traceroute.setMaxHops(50);
//        traceroute.setHost("alladin.at");
//        try {
//			List<HopDetail> list = traceroute.call();
//			System.out.println(list);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		return view;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		handler.postDelayed(cpuUsageRunnable, 100);
	}
	
	@Override
	public void onPause() {
		handler.removeCallbacks(cpuUsageRunnable);
		super.onPause();
	}

	@Override
	public String setActionBarTitle() {
		if (this.isAdded())
			return getString(R.string.menu_button_netstat);
		else return "";
	}
}

