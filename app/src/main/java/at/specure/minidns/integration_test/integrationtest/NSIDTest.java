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
import java.net.InetAddress;

import at.specure.minidns.client.DNSClient;
import at.specure.minidns.core.DNSMessage;
import at.specure.minidns.core.EDNS;
import at.specure.minidns.core.Question;
import at.specure.minidns.core.Record;
import at.specure.minidns.core.edns.NSID;
import at.specure.minidns.iterative_resolver.iterative.IterativeDNSClient;


public class NSIDTest {

    @IntegrationTest
    public static NSID testNsidLRoot() {
        DNSClient client = new DNSClient(null) {
            @Override
            protected DNSMessage.Builder newQuestion(DNSMessage.Builder message) {
                message.getEdnsBuilder().addEdnsOption(NSID.REQUEST);
                return super.newQuestion(message);
            }
        };
        DNSMessage response = null;
        Question q = new Question("de", Record.TYPE.NS);
        for (InetAddress lRoot : IterativeDNSClient.getRootServer('l')) {
            try {
                response = client.query(q, lRoot);
            } catch (IOException e) {
                continue;
            }
            break;
        }
        NSID nsid = response.getEdns().getEdnsOption(EDNS.OptionCode.NSID);
//        assertNotNull(nsid);
        return nsid;
    }
}
