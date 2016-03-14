package com.isuwang.soa.remoting.netty;

import com.isuwang.soa.core.InvocationContext;
import com.isuwang.soa.core.SoaBaseCode;
import com.isuwang.soa.core.SoaException;
import com.isuwang.soa.remoting.SoaConnection;
import com.isuwang.soa.remoting.SoaConnectionPool;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SoaConnectionPoolImpl implements com.isuwang.soa.remoting.SoaConnectionPool {

    private static final SoaConnectionPoolImpl pool = new SoaConnectionPoolImpl();

    static {
        IdleConnectionManager connectionManager = new IdleConnectionManager();
        connectionManager.start();
    }

    public static SoaConnectionPool getInstance() {
        return pool;
    }

    private Map<String, SoaConnectionImpl> connectionMap = new ConcurrentHashMap<>();

    @Override
    public synchronized SoaConnection getConnection() throws SoaException {
        InvocationContext context = InvocationContext.Factory.getCurrentInstance();

        if (context.getCalleeIp() == null || context.getCalleePort() <= 0)
            throw new SoaException(SoaBaseCode.NotFoundServer);

        String connectKey = context.getCalleeIp() + ":" + String.valueOf(context.getCalleePort());

        if (connectionMap.containsKey(connectKey)) {
            return connectionMap.get(connectKey);
        }

        SoaConnectionImpl soaConnection = new SoaConnectionImpl(context.getCalleeIp(), context.getCalleePort());

        connectionMap.put(connectKey, soaConnection);

        return soaConnection;
    }

}
