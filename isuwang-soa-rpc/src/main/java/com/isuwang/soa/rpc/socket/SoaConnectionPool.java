package com.isuwang.soa.rpc.socket;

/**
 * Soa连接池
 *
 * @author craneding
 * @date 15/8/5
 */
public class SoaConnectionPool {

    private static final SoaConnectionPool pool = new SoaConnectionPool();

    public static SoaConnectionPool getInstance() {
        return pool;
    }

    public SoaConnection getConnection() {
        return new SoaConnection();
    }

}
