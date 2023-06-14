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
package at.specure.android.util.net;

import at.specure.android.util.network.network.NRConnectionState;

/**
 * 
 * @author lb
 *
 */
public enum NetworkFamilyEnum {
	LAN("LAN"),
	ETHERNET("ETHERNET"),
	BLUETOOTH("BLUETOOTH"),	
	WLAN("WLAN"),
	_1xRTT("1xRTT","2G"),
	_2G3G("2G/3G"),
	_3G4G("3G/4G"),
	_2G4G("2G/4G"),
	_2G3G4G("2G/3G/4G"),
	CLI("CLI"),
	CELLULAR_ANY("MOBILE","CELLULAR_ANY"),
	GSM("GSM","2G"),
	EDGE("EDGE","2G"),
	UMTS("UMTS","3G"),
	CDMA("CDMA","2G"),
	EVDO_0("EVDO_0","2G"),
	EVDO_A("EVDO_A","2G"),
	HSDPA("HSDPA","3G"),
	HSUPA("HSUPA","3G"),
	HSPA("HSPA","3G"),
	IDEN("IDEN","2G"),
	EVDO_B("EVDO_B","2G"),
	LTE("LTE","4G LTE"),
	EHRPD("EHRPD","2G"),
	HSPA_PLUS("HSPA+","3G"),
	UNKNOWN("UNKNOWN"),
	IWLAN("IWLAN", "UNKNOWN"),
	GPRS("GPRS","2G"),
	TD_SCMA("TD_SCMA","3G"),
	LTE_CA("LTE_CA","4G+"),
	_5G_SA("NR", "5G"),
	_5G_NSA("NR NSA", "5G NSA"),
	_5G_SIGNALLING("NR AVAILABLE", "4G LTE+(NR)");

	protected final String networkId;

	/**
	 * 	this needs to contain the same values as they are in {@link at.specure.android.util.Helperfunctions.getNetworkTypeName()}
 	 */
	protected final String networkFamily;
	
	NetworkFamilyEnum(String networkId, String family) {
		this.networkFamily = family;
		this.networkId = networkId;
	}
	
	NetworkFamilyEnum(String family) {
		this(family, family);
	}
	
	public String getNetworkId() {
		return networkId;
	}

	public String getNetworkFamily() {
		return networkFamily;
	}

	public static NetworkFamilyEnum getFamilyByNetworkId(String networkId, NRConnectionState nrConnectionState) {
		for (NetworkFamilyEnum item : NetworkFamilyEnum.values()) {
			if (item.getNetworkId().equals(networkId)) {
				if (item == LTE || item == LTE_CA) {
					if (nrConnectionState == null || nrConnectionState == NRConnectionState.NOT_AVAILABLE) {
						return item;
					} else {
						switch (nrConnectionState) {
							case AVAILABLE:
								return _5G_SIGNALLING;
							case NSA:
								return _5G_NSA;
							case SA:
								return _5G_SA;
						}
					}
				}
				return item;
			}
		}

		return UNKNOWN;
	}
}
