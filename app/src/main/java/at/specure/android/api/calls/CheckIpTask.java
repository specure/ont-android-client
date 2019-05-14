/*******************************************************************************
 * Copyright 2014-2017 Specure GmbH
 * Copyright 2013-2015 alladin-IT GmbH
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
package at.specure.android.api.calls;

import android.os.AsyncTask;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Enumeration;

import at.specure.android.api.ControlServerConnection;
import at.specure.android.configs.ConfigHelper;
import at.specure.android.screens.main.MainActivity;
import at.specure.android.screens.result.adapter.result.OnCompleteListener;
import at.specure.android.util.net.NetworkInfoCollector;
import timber.log.Timber;

public class CheckIpTask extends AsyncTask<Void, Void, JsonArray> {
    private final MainActivity activity;

    private JsonArray newsList;

    String lastIp;

    InetAddress privateIpv6;
    InetAddress privateIpv4;
    String publicIpv4;
    String publicIpv6;

    boolean needsRetry = false;

    ControlServerConnection serverConn;

    private OnCompleteListener onCompleteListener;

    /**
     *
     */
    private static final String DEBUG_TAG = "CheckIpTask";


    public CheckIpTask(final MainActivity activity) {
        this.activity = activity;

    }

    /**
     * @param listener
     */
    public void setOnCompleteListener(OnCompleteListener listener) {
        this.onCompleteListener = listener;
    }

    @Override
    protected JsonArray doInBackground(final Void... params) {
        needsRetry = false;
        serverConn = new ControlServerConnection(activity);


        try {
            Socket s = new Socket();
            InetSocketAddress addr = new InetSocketAddress(ConfigHelper.getCachedControlServerNameIpv4(activity.getApplicationContext()),
                    ConfigHelper.getControlServerPort(activity.getApplicationContext()));
            s.connect(addr, 5000);

            privateIpv4 = s.getLocalAddress();
            s.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        InetAddress s1 = /*privateIpv4 =*/ getLocalIpv4();
//
        Timber.e("IP ADDRESS V4 COMPARE: %s vs %s", privateIpv4, s1);



        try {
            Socket s = new Socket();
            InetSocketAddress addr = new InetSocketAddress(ConfigHelper.getCachedControlServerNameIpv6(activity.getApplicationContext()),
                    ConfigHelper.getControlServerPort(activity.getApplicationContext()));
            s.connect(addr, 5000);

            privateIpv6 = s.getLocalAddress();
            s.close();
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            needsRetry = ConfigHelper.isRetryRequiredOnIpv6SocketTimeout(activity);
        } catch (java.net.UnknownHostException | java.net.NoRouteToHostException e) {
            System.err.println("Unknown host error:" + ConfigHelper.getCachedControlServerNameIpv6(activity.getApplicationContext()));
        } catch (Exception e) {
            needsRetry = false;
            e.printStackTrace();
        }

        InetAddress ipv6 = /*privateIpv6 = */getLocalIpV6();

        Timber.e("IP ADDRESS V6 COMPARE: %s vs %s", privateIpv6, ipv6);

        newsList = new JsonArray();

        if (privateIpv4 != null) {
            JsonArray response = serverConn.requestIp(false);
            if (response != null && response.size() >= 1) {
                newsList.add(response.get(0));
            }
        } else {
            Timber.d("no private ipv4 found");
        }

        if (privateIpv6 != null) {
            JsonArray response = serverConn.requestIp(true);
            if (response != null && response.size() >= 1) {
                newsList.add(response.get(0));
            }
        } else {
            Timber.d("no private ipv6 found");
        }

        return newsList;
    }


    public InetAddress getLocalIpv4() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    System.out.println("ip1--:" + inetAddress);
                    System.out.println("ip2--:" + inetAddress.getHostAddress());

                    if ((inetAddress instanceof Inet4Address) && (!inetAddress.isLoopbackAddress())) {
                        return inetAddress;
                    }

//                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
//                        String ipaddress = inetAddress.getHostAddress().toString();
//                        return ipaddress;
//                    }


                }
            }
        } catch (Exception ex) {
            Timber.e("IP Address v4 new %s", ex.toString());
        }
        return null;
    }

    public InetAddress getLocalIpV6() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    System.out.println("ip1--:" + inetAddress);
                    System.out.println("ip2--:" + inetAddress.getHostAddress());


                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet6Address) {
                        return inetAddress;
                    }

//                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet6Address) {
//                        String ipaddress = inetAddress.getHostAddress().toString();
//                        return ipaddress;
//                    }
                }
            }
        } catch (Exception ex) {
            Timber.e("IP Address v6 new %s", ex.toString());
        }
        return null;
    }

        /* NEW SOLUTION TO GET IPv4 and IPv6 for connection
        public String getLocalIpv4() {
            try {
                for (Enumeration<NetworkInterface> en = NetworkInterface
                        .getNetworkInterfaces(); en.hasMoreElements(); ) {
                    NetworkInterface intf = en.nextElement();
                    for (Enumeration<InetAddress> enumIpAddr = intf
                            .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        System.out.println("ip1--:" + inetAddress);
                        System.out.println("ip2--:" + inetAddress.getHostAddress());

                        if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                            String ipaddress = inetAddress.getHostAddress().toString();
                            return ipaddress;
                        }


                    }
                }
            } catch (Exception ex) {
                Log.e("IP Address", ex.toString());
            }
            return null;
        }

//ipv6
  public String getLocalIpV6() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    System.out.println("ip1--:" + inetAddress);
                    System.out.println("ip2--:" + inetAddress.getHostAddress());

                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet6Address) {
                        String ipaddress = inetAddress.getHostAddress().toString();
                        return ipaddress;
                    }


                }
            }
        } catch (Exception ex) {
            Log.e("IP Address", ex.toString());
        }
        return null;
    }
         */

    @Override
    protected void onCancelled() {
        if (serverConn != null) {
            serverConn.unload();
            serverConn = null;
        }
    }

    @Override
    protected void onPostExecute(final JsonArray newsList) {

        try {
            Timber.d("News: %s", newsList);
            int ipv = 4;

            if (newsList != null && newsList.size() > 0) {
                for (int i = 0; i < newsList.size(); i++) {
                    if (!isCancelled() && !Thread.interrupted()) {
                        try {

                            final JsonObject newsItem = newsList.get(i).getAsJsonObject();

                            if (newsItem.has("v")) {
                                ipv = newsItem.get("v").getAsInt();

                                if (ipv == 6) {
                                    try {
                                        publicIpv6 = newsItem.get("ip").getAsString();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    try {
                                        publicIpv4 = newsItem.get("ip").getAsString();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        } catch (final JsonParseException e) {
                            e.printStackTrace();
                        }
                    }
                }

                if (onCompleteListener != null && !needsRetry) {
                    onCompleteListener.onComplete(NetworkInfoCollector.FLAG_PRIVATE_IPV6, privateIpv6);
                    onCompleteListener.onComplete(NetworkInfoCollector.FLAG_PRIVATE_IPV4, privateIpv4);
                    onCompleteListener.onComplete(NetworkInfoCollector.FLAG_IPV4, publicIpv4);
                    onCompleteListener.onComplete(NetworkInfoCollector.FLAG_IPV6, publicIpv6);
                    onCompleteListener.onComplete(NetworkInfoCollector.FLAG_IP_TASK_COMPLETED, null);
                } else if (onCompleteListener != null) {
                    onCompleteListener.onComplete(OnCompleteListener.ERROR, NetworkInfoCollector.FLAG_IP_TASK_NEEDS_RETRY);
                }

            } else {
                ConfigHelper.setLastIp(activity.getApplicationContext(), null);
                if (onCompleteListener != null) {
                    onCompleteListener.onComplete(OnCompleteListener.ERROR, null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        finally {
//        	if (onCompleteListener != null) {
//        		onCompleteListener.onComplete(NetworkInfoCollector.FLAG_IP_TASK_COMPLETED, null);
//        	}
//        }
    }

}
