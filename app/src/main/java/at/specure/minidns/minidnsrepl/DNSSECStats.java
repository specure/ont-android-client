/*
 * Copyright 2015-2018 the original author or authors
 *
 * This software is licensed under the Apache License, Version 2.0,
 * the GNU Lesser General Public License version 2 or later ("LGPL")
 * and the WTFPL.
 * You may choose either license to govern your use of this software only
 * upon the condition that you accept all of the terms of either
 * the Apache License 2.0, the LGPL 2.1+ or the WTFPL.
 */
package at.specure.minidns.minidnsrepl;

import android.content.Context;

import java.io.IOException;

import at.specure.minidns.core.DNSName;
import at.specure.minidns.core.Record;
import at.specure.minidns.dnssec.dnssec.DNSSECClient;
import at.specure.minidns.dnssec.dnssec.DNSSECMessage;
import at.specure.minidns.dnssec.dnssec.UnverifiedReason;
import at.specure.minidns.integration_test.integrationtest.IntegrationTestTools;
import at.specure.minidns.integration_test.jul.MiniDnsJul;
import at.specure.minidns.iterative_resolver.iterative.ReliableDNSClient;

public class DNSSECStats {

    private static final DNSName DOMAIN = DNSName.from("verteiltesysteme.net");
    private static final Record.TYPE RR_TYPE = Record.TYPE.A;

    public static void iterativeDnssecLookupNormalVsExtendedCache(Context context) throws IOException {
        // iterativeDnssecLookup(CacheConfig.normal);
        iterativeDnssecLookup(context, IntegrationTestTools.CacheConfig.extended);
    }

    private static void iterativeDnssecLookup(Context context, IntegrationTestTools.CacheConfig cacheConfig) throws IOException {
        DNSSECClient client = MiniDNSStats.getClient(context, cacheConfig);
        client.setMode(ReliableDNSClient.Mode.iterativeOnly);
        DNSSECMessage secRes = client.queryDnssec(DOMAIN, RR_TYPE);

        StringBuilder stats = MiniDNSStats.getStats(client);
        stats.append('\n');
        stats.append(secRes);
        stats.append('\n');
        for (UnverifiedReason r : secRes.getUnverifiedReasons()) {
            stats.append(r);
        }
        stats.append("\n\n");
        // CHECKSTYLE:OFF
        System.out.println(stats);
        // CHECKSTYLE:ON
    }

    public static void iterativeDnsssecTest() throws SecurityException, IllegalArgumentException, IOException {
        MiniDnsJul.enableMiniDnsTrace();
//        DNSSECClient client = new DNSSECClient(new ExtendedLRUCache());
//        client.setMode(ReliableDNSClient.Mode.iterativeOnly);
//
//        DNSSECMessage secRes = client.queryDnssec("verteiltesysteme.net", Record.TYPE.A);

        // CHECKSTYLE:OFF
//        System.out.println(secRes);
        // CHECKSTYLE:ON
    }

}
