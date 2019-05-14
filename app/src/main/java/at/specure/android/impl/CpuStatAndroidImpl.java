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
package at.specure.android.impl;

import com.specure.opennettest.R;

import java.util.regex.Pattern;

import at.specure.util.tools.CpuStat;

public class CpuStatAndroidImpl extends CpuStat {
	
	public static enum CpuMemClassificationEnum {
		
		LOW(0, 50, R.color.cpu_classification_green),
		MID(50, 75, R.color.cpu_classification_yellow),
		HIGH(75, Float.MAX_VALUE, R.color.cpu_classification_red),
		UNKNOWN(Float.MIN_VALUE, Float.MIN_VALUE, R.color.cpu_classification_grey);

		protected float min;
		protected float max;
		protected int resId;
		
		private CpuMemClassificationEnum(float min, float max, int resId) {
			this.min = min;
			this.max = max;
			this.resId = resId;
		}
		
		public float getMin() {
			return min;
		}

		public float getMax() {
			return max;
		}

		public int getResId() {
			return this.resId;
		}
		
		public static CpuMemClassificationEnum classify(float value) {
			for (CpuMemClassificationEnum e : CpuMemClassificationEnum.values()) {
				if (e.getMin() <= value && e.getMax() > value) {
					return e; 
				}
			}
			
			return UNKNOWN;
		}
	}
	
	private final static String PROC_PATH = "/proc/";

	private final static Pattern CPU_PATTERN = Pattern.compile("cpu[^0-9]([\\s0-9]*)");
	
	private final static Pattern CPU_CORE_PATTERN = Pattern.compile("cpu([0-9]+)([\\s0-9]*)");

	public Float getCPUUsagePercentage() {
		float[] cpus = update(false);
		if (cpus != null && cpus.length > 0) {
			float total = 0f;
			for (float cpu : cpus) {
				total += cpu;
			}
			return ((total / (float) cpus.length) * 100f);
		}
		return null;
	}

	public CpuUsage getCurrentCpuUsage(boolean getByCore) {
		CpuUsage cpuUsage = new CpuUsage();
		
		/*String stat = ToolUtils.readFromProc(PROC_PATH + "stat");
		
		if (getByCore) {
			Matcher m = CPU_CORE_PATTERN.matcher(stat);
			while(m.find()) {
				int core = Integer.parseInt(m.group(1));
				String[] cpu = m.group(2).trim().split(" ");
				cpuUsage.getCoreUsageList().add(new
                        CoreUsage(core, Long.parseLong(cpu[0]),
								Long.parseLong(cpu[1]), Long.parseLong(cpu[2]),
								Long.parseLong(cpu[3]), Long.parseLong(cpu[4]),
								Long.parseLong(cpu[5]), Long.parseLong(cpu[6])));
			}
		}
		else {
			Matcher m = CPU_PATTERN.matcher(stat);
			while(m.find()) {
				String[] cpu = m.group(1).trim().split(" ");
				cpuUsage.getCoreUsageList().add(new
                        CoreUsage(0, Long.parseLong(cpu[0]),
								Long.parseLong(cpu[1]), Long.parseLong(cpu[2]),
								Long.parseLong(cpu[3]), Long.parseLong(cpu[4]),
								Long.parseLong(cpu[5]), Long.parseLong(cpu[6])));
			}
		}*/
		return cpuUsage;
	}	
}
