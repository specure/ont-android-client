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

package at.specure.android.screens.main.main_fragment.runnables;

import android.content.Context;
import android.location.Location;
import android.os.Handler;

import at.specure.android.configs.LoopModeConfig;
import at.specure.android.impl.CpuStatAndroidImpl;
import at.specure.android.impl.MemInfoAndroidImpl;
import at.specure.android.screens.main.InfoCollector;
import at.specure.android.util.Helperfunctions;
import at.specure.android.util.InformationCollector;
import at.specure.android.util.location.GeoLocationX;
import at.specure.android.util.net.NetworkFamilyEnum;
import at.specure.android.util.net.RealTimeInformation;
import at.specure.android.util.network.network.NRConnectionState;
import at.specure.util.tools.CpuStat;
import at.specure.util.tools.MemInfo;
import timber.log.Timber;

import static at.specure.android.screens.main.main_fragment.MainMenuFragment.INFORMATION_COLLECTOR_TIME;


/**
 * Created by michal.cadrik on 10/18/2017.
 */

public class MainInfoRunnable implements Runnable {

    private InformationCollector informationCollector;
    private InfoCollector infoCollector;
    CpuStat cpuStat = new CpuStatAndroidImpl();
    MemInfo memInfo = new MemInfoAndroidImpl();
    boolean stop = false;
    Handler infoHandler;


    public MainInfoRunnable(InformationCollector informationCollector, Handler infoHandler) {
        this.informationCollector = informationCollector;
        this.infoHandler = infoHandler;
        this.infoCollector = InfoCollector.getInstance();
        this.infoHandler.post(this);
        Timber.e( "CONSTRUCT");
    }

    public void setStop() {
        Timber.e( "STOP SET");
        stop = true;
    }

    @Override
    public void run() {
        int curSignal = Integer.MIN_VALUE;
        Timber.e( "NEW RUN");
        if (informationCollector != null) {
            if (cpuStat != null) {
                Float cpuUsagePercentage = cpuStat.getCPUUsagePercentage();
                if (cpuUsagePercentage != null) {
                    infoCollector.setCpuUsage(cpuUsagePercentage);
                    infoCollector.setCpuCoresCount(cpuStat.getLastCpuUsage().getNumCores());
                }
            }

            if (memInfo != null) {
                Float freeMemPercentage = memInfo.getFreeMemPercentage();
                if (freeMemPercentage != null) {
                    infoCollector.setMemUsage(freeMemPercentage);
                    infoCollector.setMemTotal(memInfo.getTotalMem());
                    infoCollector.setMemFree(memInfo.getFreeMem());
                }
            }

            Context context = informationCollector.getContext();

            if (context != null) {
                Location loc = GeoLocationX.getInstance(context).getLastKnownLocation(context, null);
                infoCollector.setLocation(loc);

                int loopModeMaxTests = LoopModeConfig.getLoopModeMaxTests(context);
                int currentTestNumber = LoopModeConfig.getCurrentTestNumber(context);
                long remainingTimeToNextLoopTest = LoopModeConfig.getRemainingTimeToNextLoopTest(context);

                if (remainingTimeToNextLoopTest < 0) {
                    remainingTimeToNextLoopTest = 0;
                }

                infoCollector.setLoopModeMaxTests(loopModeMaxTests);
                infoCollector.setLoopModeCurrentTest(currentTestNumber);
                infoCollector.setLoopModeRemainingTimeToNextTest(remainingTimeToNextLoopTest);
            }
            int lastNetworkType = informationCollector.getNetwork();
            NRConnectionState lastNRConnectionState = informationCollector.getLastNRConnectionState();
            String lastNetworkTypeString = Helperfunctions.getNetworkTypeName(lastNetworkType);
            //System.out.println("lastNetworkType: " + lastNetworkType + ", lastNetworkTypeString: " + lastNetworkTypeString);
            infoCollector.setNetworkTypeString(lastNetworkTypeString);
            int signalType = informationCollector.getSignalType();

            if (!"UNKNOWN".equals(lastNetworkTypeString)) {
                Integer signal = informationCollector.getSignal();
                if (!"BLUETOOTH".equals(lastNetworkTypeString) && !"ETHERNET".equals(lastNetworkTypeString)) {
                    if (signal != null && signal > Integer.MIN_VALUE) {
                        curSignal = signal;

                        if (signalType != InformationCollector.SIGNAL_TYPE_WLAN) {
                            String cellId = RealTimeInformation.getCellId(informationCollector.getContext());
                            if ((cellId == null) || (cellId.isEmpty())) {
                                cellId = "-";
                            }
                            infoCollector.setCellId(cellId);
                        } else {
                            infoCollector.setCellId(null);
                        }
                        infoCollector.setSignalType(signalType);
                        infoCollector.setSignal(curSignal);

                        if (signalType == InformationCollector.SIGNAL_TYPE_RSRP) {
                            Integer signalRsrq = informationCollector.getSignalRsrq();
                            if (signalRsrq != null) {
                                infoCollector.setSignalRsrq(signalRsrq);
                            } else {
                                infoCollector.setSignalRsrq(null);
                            }
                        } else {
                            infoCollector.setSignalRsrq(null);
                        }
                    } else {
                        curSignal = Integer.MIN_VALUE;
                    }
                }
            } else {
                infoCollector.setCellId(null);
                infoCollector.setSignalRsrq(null);
                infoCollector.setSignalType(signalType);
                infoCollector.setSignal(null);
                infoCollector.setNetworkName("UNKNOWN");
            }

            NetworkFamilyEnum networkFamily = NetworkFamilyEnum.getFamilyByNetworkId(lastNetworkTypeString, lastNRConnectionState);
            infoCollector.setNetworkFamily(networkFamily.getNetworkFamily());
            if (NetworkFamilyEnum.UNKNOWN.equals(networkFamily)) {
                infoCollector.setNetworkTypeString(lastNetworkTypeString);
            } else {

                if (!lastNetworkTypeString.equals(NetworkFamilyEnum.WLAN.getNetworkFamily())) {
                    if (lastNetworkTypeString.equals(networkFamily.getNetworkFamily())) {
                        infoCollector.setNetworkTypeString(lastNetworkTypeString);
                    } else {
                        infoCollector.setNetworkTypeString(networkFamily.getNetworkFamily() + "/" + lastNetworkTypeString);
                    }
                }
            }

            if (!"UNKNOWN".equals(lastNetworkTypeString)) {
                String networkName = informationCollector.getOperatorName();
                if (networkName != null && !"()".equals(networkName)) {
                    infoCollector.setNetworkName(networkName);
                } else {
                    infoCollector.setNetworkName(networkName);
                }
            }

        } else {
            infoCollector.setCellId(null);
            infoCollector.setSignalRsrq(null);
            infoCollector.setSignal(null);
            infoCollector.setNetworkName("UNKNOWN");
        }

        if (!stop && informationCollector != null) {
            Timber.d("SIGNAL CHANGED MIR 180 reinit CLEARED!");
//            informationCollector.reInit();
            infoHandler.postDelayed(this, INFORMATION_COLLECTOR_TIME);
        } else {
            Timber.e("STOPPED");
        }

    }

    public void startLoop() {
        if (infoHandler != null) {
            infoHandler.removeCallbacks(this);
            this.setStop();
            this.stop = false;
            Timber.e("SET RUN");
            infoHandler.postDelayed(this, INFORMATION_COLLECTOR_TIME);
        }
    }
}
