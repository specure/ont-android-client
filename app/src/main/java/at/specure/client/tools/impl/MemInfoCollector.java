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
import java.util.Map;
import java.util.concurrent.TimeUnit;

import at.specure.util.tools.Collector;
import at.specure.util.tools.MemInfo;

public class MemInfoCollector implements Collector<Map<String, Long>, JsonObject> {
	
	public final static String JSON_KEY = "mem_usage";

	List<CollectorData<Map<String, Long>>> collectorDataList = new ArrayList<Collector.CollectorData<Map<String, Long>>>();
	final MemInfo memInfo;
	final long pauseNs;

	/**
	 * @param pauseNs
	 */
	public MemInfoCollector(MemInfo memInfoImpl, long pauseNs) {
		this.memInfo = memInfoImpl;
		this.pauseNs = pauseNs;
	}
	
	/*
	 * (non-Javadoc)
	 * @see at.specure.client.tools.Collector#update(float, java.util.concurrent.TimeUnit)
	 */
	public synchronized CollectorData<Map<String, Long>> update(float delta, TimeUnit timeUnit) {
		memInfo.update();
		CollectorData<Map<String, Long>> data = new CollectorData<Map<String, Long>>(memInfo.getMemoryMap());
		collectorDataList.add(data);
		return data;
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
		
		for (CollectorData<Map<String, Long>> data : collectorDataList) {
			final JsonObject dataJson = new JsonObject();
			//dataJson.addProperty("value", 100f - ((float) data.getValue().get("MemFree") / (float) data.getValue().get("MemTotal")) * 100f);
			//dataJson.addProperty("time_ns", data.getTimeStampNs() - relativeTimeStampNs);
			jsonArray.add(dataJson);
		}
		
		if (clean) {
			collectorDataList.clear();
		}

		final JsonObject jsonObject = new JsonObject();
		jsonObject.add("values", jsonArray);
		
		return jsonObject;
	}


	/*
	 * (non-Javadoc)
	 * @see at.specure.client.tools.Collector#getJsonKey()
	 */
	public String getJsonKey() {
		return JSON_KEY;
	}
}
