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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.Serializable;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import org.json.JSONException;

import at.specure.client.v2.task.result.QoSServerResult.DetailType;

/**
 * contains the description of a test result after evaluation
 * @author lb
 *
 */
public class QoSServerResultDesc implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public final static String JSON_KEY_DESCRIPTION = "desc";
	
	public final static String JSON_KEY_TESTTYPE = "test";
	
	public final static String JSON_KEY_STATUS = "status";
	
	public final static String JSON_KEY_UID_LIST = "uid";
	
	private String desc;
	private QoSTestResultEnum testType;
	private DetailType status;
	private Set<Long> uidSet = new TreeSet<Long>();
	
	/**
	 * 
	 * @param desc
	 * @param testType
	 * @param status
	 */
	public QoSServerResultDesc(String desc, QoSTestResultEnum testType, DetailType status, Set<Long> uidSet) {
		this.desc = desc;
		this.testType = testType;
		this.status = status;
		this.uidSet = uidSet;
	}
	
	/**
	 * 
	 * @param json
	 * @throws JSONException
	 */
	public QoSServerResultDesc(JsonObject json) throws JSONException {
		this.desc = json.get(JSON_KEY_DESCRIPTION).getAsString();
		this.testType = QoSTestResultEnum.valueOf(json.get(JSON_KEY_TESTTYPE).getAsString());
		this.status = DetailType.valueOf(json.get(JSON_KEY_STATUS).getAsString().toUpperCase(Locale.US));
		JsonArray uidArray = json.getAsJsonArray(JSON_KEY_UID_LIST);
		for (int i = 0; i < uidArray.size(); i++) {
			uidSet.add(uidArray.get(i).getAsLong());
		}
	}
	
	public String getDesc() {
		return desc;
	}
	
	public void setDesc(String desc) {
		this.desc = desc;
	}
	
	public QoSTestResultEnum getTestType() {
		return testType;
	}
	
	public void setTestType(QoSTestResultEnum testType) {
		this.testType = testType;
	}
	
	public DetailType getStatus() {
		return status;
	}
	
	public void setStatus(DetailType status) {
		this.status = status;
	}
	
	public Set<Long> getUidSet() {
		return uidSet;
	}

	public void setUidSet(Set<Long> uidSet) {
		this.uidSet = uidSet;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "QoSServerResultDesc [desc=" + desc + ", testType="
				+ testType + ", status=" + status + ", uidSet=" + uidSet + "]";
	}
}
