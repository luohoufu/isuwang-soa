package com.isuwang.soa.rpc.netty;

import com.isuwang.soa.core.Context;
import com.isuwang.soa.core.SoaBaseCode;
import com.isuwang.soa.core.SoaException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SoaConnectionPool {

    private static final SoaConnectionPool pool = new SoaConnectionPool();

    public static SoaConnectionPool getInstance() {
        return pool;
    }

    private Map<String, SoaConnection> connectionMap = new ConcurrentHashMap<>();

    public synchronized SoaConnection getConnection() throws SoaException {
        Context context = Context.Factory.getCurrentInstance();

        if(context.getCalleeIp() == null || context.getCalleePort() <= 0)
            throw new SoaException(SoaBaseCode.NotFoundServer);

        String connectKey = context.getCalleeIp() + ":" + String.valueOf(context.getCalleePort());

        if (connectionMap.containsKey(connectKey)) {
            return connectionMap.get(connectKey);
        }

        SoaConnection soaConnection = new SoaConnection(context.getCalleeIp(), context.getCalleePort());

        connectionMap.put(connectKey, soaConnection);

        return soaConnection;
    }

}
