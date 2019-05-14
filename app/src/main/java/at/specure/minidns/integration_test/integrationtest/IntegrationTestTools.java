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

import android.content.Context;

import at.specure.minidns.client.DNSCache;
import at.specure.minidns.client.cache.ExtendedLRUCache;
import at.specure.minidns.client.cache.FullLRUCache;
import at.specure.minidns.client.cache.LRUCache;
import at.specure.minidns.client.source.NetworkDataSourceWithAccounting;
import at.specure.minidns.dnssec.dnssec.DNSSECClient;

public class IntegrationTestTools {

    public enum CacheConfig {
        without,
        normal,
        extended,
        full,
    }

    public static DNSSECClient getClient(Context context, CacheConfig cacheConfig) {
        DNSCache cache;
        switch (cacheConfig) {
            case without:
                cache = null;
                break;
            case normal:
                cache = new LRUCache();
                break;
            case extended:
                cache = new ExtendedLRUCache();
                break;
            case full:
                cache = new FullLRUCache();
                break;
            default:
                throw new IllegalStateException();
        }

        DNSSECClient client = new DNSSECClient(context, cache);
        client.setDataSource(new NetworkDataSourceWithAccounting());
        return client;
    }

}
