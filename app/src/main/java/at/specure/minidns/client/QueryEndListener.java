package at.specure.minidns.client;

import at.specure.minidns.core.DNSMessage;
import at.specure.minidns.core.record.Data;
import at.specure.minidns.hla.minidns.hla.ResolverResult;

/**
 * Created by michal.cadrik on 29-Mar-18.
 */


public interface QueryEndListener {

    void queryEndListener(DNSMessage message);

    <D extends Data> void queryEndListener(ResolverResult<D> dResolverResult, Exception e);
}
