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
package at.specure.client.tools.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import at.specure.util.tools.Collector;
import at.specure.util.tools.CpuStat;

public class CpuStatCollector implements Collector<Float, JsonObject> {

	public final static String JSON_KEY = "cpu_usage";

	List<CollectorData<Float>> collectorDataList = new ArrayList<Collector.CollectorData<Float>>();
	final CpuStat cpuStat;
	final long pauseNs;

	/**
	 * 
	 * @param cpuStatImpl
	 * @param pauseNs
	 */
	public CpuStatCollector(CpuStat cpuStatImpl, long pauseNs) {
		this.cpuStat = cpuStatImpl;
		this.pauseNs = pauseNs;
	}
	
	/*
	 * (non-Javadoc)
	 * @see at.specure.client.tools.Collector#update(float, java.util.concurrent.TimeUnit)
	 */
	public synchronized CollectorData<Float> update(float delta, TimeUnit timeUnit) {
		float[] cores = cpuStat.update(false);

		if (cores  != null) {
			float cpu = 0f;
			
			for (float c : cores) {
				cpu += c;
			}

			CollectorData<Float> data = new CollectorData<Float>(cpu / cores.length);
			
			if (delta != 0f) {
				collectorDataList.add(data);
			}
			
			return data;
		}
		
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see at.specure.client.tools.Collector#getNanoPause()
	 */
	public long getNanoPause() {
		return pauseNs;
	}

	/*
	 * (non-Javadoc)
	 * @see at.specure.client.tools.Collector#getJsonKey()
	 */
	public String getJsonKey() {
		return JSON_KEY;
	}
	
	/*
	 * (non-Javadoc)
	 * @see at.specure.client.tools.Collector#getJsonResult()
	 */
	public JsonObject getJsonResult(boolean clean) {
		return getJsonResult(clean, 0, TimeUnit.NANOSECONDS);
	}

	/*
	 * (non-Javadoc)
	 * @see Collector#getJsonResult(boolean, long, java.util.concurrent.TimeUnit)
	 */
	public JsonObject getJsonResult(boolean clean, long relTimeStamp, TimeUnit timeUnit) {
		final long relativeTimeStampNs = TimeUnit.NANOSECONDS.convert(relTimeStamp, timeUnit);
		final JsonArray jsonArray = new JsonArray();
		for (CollectorData<Float> data : collectorDataList) {
			final JsonObject dataJson = new JsonObject();
			//dataJson.addProperty("value", ((data.getValue() * 100f)));
			//dataJson.addProperty("time_ns", data.getTimeStampNs() - relativeTimeStampNs);
			jsonArray.add(dataJson);
		}

		final JsonObject jsonObject = new JsonObject();
//		if (!cpuStat.getLastCpuUsage().isDetectedIdleOrIoWaitDrop()) {
//			jsonObject.add("values", jsonArray);
//		}
//		else {
//			jsonObject.add("values", new JsonArray());
//			final JsonArray flagArray = new JsonArray();
//			JsonObject flag = new JsonObject();
//			flag.addProperty("info", "implausible idle/iowait");
//			flagArray.add(flag);
//			jsonObject.add("flags", flagArray);
//		}
		
		if (clean) {
			collectorDataList.clear();
		}
		
		return jsonObject;
	}
}
