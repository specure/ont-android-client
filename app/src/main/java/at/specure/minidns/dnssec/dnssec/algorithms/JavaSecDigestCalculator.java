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
package at.specure.minidns.dnssec.dnssec.algorithms;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import at.specure.minidns.dnssec.dnssec.DigestCalculator;

public class JavaSecDigestCalculator implements DigestCalculator {
    private MessageDigest md;

    public JavaSecDigestCalculator(String algorithm) throws NoSuchAlgorithmException {
        md = MessageDigest.getInstance(algorithm);
    }

    @Override
    public byte[] digest(byte[] bytes) {
        return md.digest(bytes);
    }
}
