package com.isuwang.soa.remoting.socket;

import com.isuwang.soa.remoting.SoaConnection;
import com.isuwang.soa.remoting.SoaConnectionPool;

/**
 * Soa连接池
 *
 * @author craneding
 * @date 15/8/5
 */
public class SoaConnectionPoolImpl implements com.isuwang.soa.remoting.SoaConnectionPool {

    private static final SoaConnectionPoolImpl pool = new SoaConnectionPoolImpl();

    public static SoaConnectionPool getInstance() {
        return pool;
    }

    @Override
    public SoaConnection getConnection() {
        return new SoaConnectionImpl();
    }

}
