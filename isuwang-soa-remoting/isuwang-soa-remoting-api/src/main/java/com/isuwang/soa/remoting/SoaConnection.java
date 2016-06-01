package com.isuwang.soa.remoting;

import com.isuwang.soa.core.TBeanSerializer;
import com.isuwang.org.apache.thrift.TException;

import java.util.concurrent.Future;

/**
 * @author craneding
 * @date 16/3/1
 */
public interface SoaConnection {

    <REQ, RESP> RESP send(REQ request, RESP response, TBeanSerializer<REQ> requestSerializer, TBeanSerializer<RESP> responseSerializer) throws TException;

    <REQ, RESP> Future<RESP> sendAsync(REQ request, RESP response, TBeanSerializer<REQ> requestSerializer, TBeanSerializer<RESP> responseSerializer) throws TException;

}
