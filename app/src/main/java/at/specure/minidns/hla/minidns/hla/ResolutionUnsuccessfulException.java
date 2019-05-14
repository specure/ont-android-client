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
package at.specure.minidns.hla.minidns.hla;

import at.specure.minidns.client.MiniDNSException;
import at.specure.minidns.core.DNSMessage;
import at.specure.minidns.core.Question;

public class ResolutionUnsuccessfulException extends MiniDNSException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public final Question question;
    public final DNSMessage.RESPONSE_CODE responseCode;

    public ResolutionUnsuccessfulException(Question question, DNSMessage.RESPONSE_CODE responseCode) {
        super("Asking for " + question + " yielded an error response " + responseCode);
        this.question = question;
        this.responseCode = responseCode;
    }
}
