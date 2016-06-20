package com.isuwang.route;

import com.isuwang.dapeng.core.InvocationContext;

import java.net.InetAddress;


public interface RouteEngine {

    public interface RouteConfig {

        boolean isServerMatched(InvocationContext ctx, InetAddress serverIP);

    }

    RouteConfig parse(String routeText);

}
