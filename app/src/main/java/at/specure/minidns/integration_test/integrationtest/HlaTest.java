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

public class HlaTest {

    @IntegrationTest
    public static void resolverTest() throws IOException {
//        ResolverResult<A> res = ResolverApi.getInstance().resolve("geekplace.eu", A.class);
//        assertEquals(true, res.wasSuccessful());
//        Set<A> answers = res.getAnswers();
//        assertEquals(1, answers.size());
//        assertEquals(new A(37, 221, 197, 223).toByteArray(), answers.iterator().next().toByteArray());
    }

    @IntegrationTest
    public static void idnSrvTest() throws IOException {
//        ResolverResult<SRV> res = ResolverApi.INSTANCE.resolve("_xmpp-client._tcp.im.plä.net", SRV.class);
//        Set<SRV> answers = res.getAnswers();
//        assertEquals(1, answers.size());
//
//        SRV srv = answers.iterator().next();
//
//        ResolverResult<A> aRes = ResolverApi.INSTANCE.resolve(srv.target, A.class);
//
//        assertTrue(aRes.wasSuccessful());
    }
}
