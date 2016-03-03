package com.isuwang.soa.doc.codec;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;

/**
 * 序列化接口
 *
 * @author craneding
 * @date 15/4/26
 */
public interface TBeanSerializer<T> {

    void read(T bean, TProtocol iprot) throws TException;

    void write(T bean, TProtocol oprot) throws TException;

}
