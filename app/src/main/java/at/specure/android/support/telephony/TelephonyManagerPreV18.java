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
package at.specure.android.support.telephony;

import android.annotation.SuppressLint;
import android.content.Context;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;

import java.util.ArrayList;
import java.util.List;

import static at.specure.android.configs.PermissionHandler.isCoarseLocationPermitted;

/**
 * 
 * @author lb
 *
 */
public class TelephonyManagerPreV18 extends TelephonyManagerSupport {

	Context context;

	public TelephonyManagerPreV18(TelephonyManager telephonyManager, Context context) {
		super(telephonyManager);
		this.context = context;
	}

	@Override
	public List<CellInfoSupport> getAllCellInfo() {
		final List<CellInfoSupport> wrappedList = new ArrayList<CellInfoSupport>();

		boolean accessToLocationGranted = isCoarseLocationPermitted(context);

		if (accessToLocationGranted) {
			//it is checked in static method above
			@SuppressLint("MissingPermission") final CellLocation cellLocation = telephonyManager.getCellLocation();
			if (cellLocation != null && cellLocation instanceof GsmCellLocation) {
				wrappedList.add(new CellInfoPreV18((GsmCellLocation) cellLocation));
			}
		}
		return wrappedList;
	}
}
