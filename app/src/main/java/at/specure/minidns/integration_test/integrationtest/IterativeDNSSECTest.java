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
package at.specure.minidns.integration_test.integrationtest;


import java.io.IOException;

import at.specure.minidns.client.source.NetworkDataSourceWithAccounting;
import at.specure.minidns.core.DNSName;
import at.specure.minidns.core.Record;
import at.specure.minidns.dnssec.dnssec.DNSSECClient;
import at.specure.minidns.dnssec.dnssec.DNSSECMessage;


public class IterativeDNSSECTest {

    private static final DNSName DNSSEC_DOMAIN = IntegrationTestHelper.DNSSEC_DOMAIN;
    private static final Record.TYPE RR_TYPE = IntegrationTestHelper.RR_TYPE;

    @IntegrationTest
    public static void shouldRequireLessQueries() throws IOException {
        DNSSECClient normalCacheClient = getClient(IntegrationTestTools.CacheConfig.normal);
        DNSSECMessage normalCacheResult = normalCacheClient.queryDnssec(DNSSEC_DOMAIN, RR_TYPE);
//        assertTrue(normalCacheResult.authenticData);
        NetworkDataSourceWithAccounting normalCacheNdswa = NetworkDataSourceWithAccounting.from(normalCacheClient);

        DNSSECClient extendedCacheClient = getClient(IntegrationTestTools.CacheConfig.extended);
        DNSSECMessage extendedCacheResult = extendedCacheClient.queryDnssec(DNSSEC_DOMAIN, RR_TYPE);
//        assertTrue(extendedCacheResult.authenticData);
        NetworkDataSourceWithAccounting extendedCacheNdswa = NetworkDataSourceWithAccounting.from(extendedCacheClient);

//        assertTrue(normalCacheNdswa.getStats().successfulQueries > extendedCacheNdswa.getStats().successfulQueries);
    }

    private static DNSSECClient getClient(IntegrationTestTools.CacheConfig cacheConfig) {
//        DNSSECClient client = IntegrationTestTools.getClient(cacheConfig);
//        client.setMode(ReliableDNSClient.Mode.iterativeOnly);
//        return client;
        return null;
    }
}
