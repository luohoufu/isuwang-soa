package com.isuwang.soa.remoting;

import com.isuwang.soa.core.SoaException;

/**
 * @author craneding
 * @date 16/3/1
 */
public interface SoaConnectionPool {

    SoaConnection getConnection() throws SoaException;

}
