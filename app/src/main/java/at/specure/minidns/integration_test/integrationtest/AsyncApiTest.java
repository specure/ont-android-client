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

import java.io.IOException;

import at.specure.minidns.async.AsyncNetworkDataSource;
import at.specure.minidns.client.DNSClient;
import at.specure.minidns.client.MiniDnsFuture;
import at.specure.minidns.client.source.DNSDataSource;
import at.specure.minidns.core.DNSMessage;
import at.specure.minidns.core.Record;


public class AsyncApiTest {

    public static void main(String[] args) throws IOException {
//        tcpAsyncApiTest();
    }

    public static void simpleAsyncApiTest(Context context) throws IOException {
        DNSClient client = new DNSClient(context);
        client.setDataSource(new AsyncNetworkDataSource());
        client.getDataSource().setTimeout(60 * 60 * 1000);

        MiniDnsFuture<DNSMessage, IOException> future = client.queryAsync("example.com", Record.TYPE.NS);
        DNSMessage response = future.getOrThrow();
//        assertEquals(DNSMessage.RESPONSE_CODE.NO_ERROR, response.responseCode);
    }

    public static void tcpAsyncApiTest(Context context) throws IOException {
        DNSDataSource dataSource = new AsyncNetworkDataSource();
        dataSource.setTimeout(60 * 60 * 1000);
        dataSource.setUdpPayloadSize(256);
        dataSource.setQueryMode(DNSDataSource.QueryMode.tcp);

        DNSClient client = new DNSClient(context);
        client.setDataSource(dataSource);
        client.setAskForDnssec(true);

        MiniDnsFuture<DNSMessage, IOException> future = client.queryAsync("google.com", Record.TYPE.AAAA);
        DNSMessage response = future.getOrThrow();
//        assertEquals(DNSMessage.RESPONSE_CODE.NO_ERROR, response.responseCode);
    }
}
