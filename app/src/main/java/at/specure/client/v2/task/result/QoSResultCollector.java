/*******************************************************************************
 * Copyright 2014-2017 Specure GmbH
 * Copyright 2013-2014 alladin-IT GmbH
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
 ******************************************************************************/
package at.specure.client.v2.task.result;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

import java.util.ArrayList;
import java.util.List;

/**
 * collects all test results
 * @author lb
 *
 */
public class QoSResultCollector {
	private List<QoSTestResult> results;
	
	public QoSResultCollector() {
		results = new ArrayList<QoSTestResult>();
	}

	/**
	 * 
	 * @return
	 */
	public List<QoSTestResult> getResults() {
		return results;
	}

	/**
	 * 
	 * @param results
	 */
	public void setResults(List<QoSTestResult> results) {
		this.results = results;
	}
	
	/**
	 * 
	 * @return
	 */
	public JsonArray toJson() {
		JsonArray json = null;
		json = new JsonArray();
		Gson gson = new Gson();
		for (QoSTestResult result : results) {
			json.add(gson.toJsonTree(result.getResultMap()));
		}
		return json;
	}
}
