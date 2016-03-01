package com.isuwang.soa.remoting;

import com.isuwang.soa.core.TBeanSerializer;
import org.apache.thrift.TException;

/**
 * @author craneding
 * @date 16/3/1
 */
public interface SoaConnection {

    <REQ, RESP> RESP send(REQ request, RESP response, TBeanSerializer<REQ> requestSerializer, TBeanSerializer<RESP> responseSerializer) throws TException;

}
