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
package at.specure.minidns.core.edns;


import at.specure.minidns.core.EDNS;
import at.specure.minidns.core.util.Hex;

public class NSID extends EDNSOption {

    public static final NSID REQUEST = new NSID();

    private NSID() {
        this(new byte[0]);
    }

    public NSID(byte[] payload) {
        super(payload);
    }

    @Override
    public EDNS.OptionCode getOptionCode() {
        return EDNS.OptionCode.NSID;
    }

    @Override
    protected CharSequence toStringInternal() {
        String res = EDNS.OptionCode.NSID + ": ";
        res += new String(optionData);
        return res;
    }

    @Override
    protected CharSequence asTerminalOutputInternal() {
        return Hex.from(optionData);
    }

}
