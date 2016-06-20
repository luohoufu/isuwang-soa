package com.isuwang.dapeng.route;


import com.isuwang.dapeng.core.InvocationContext;

import java.net.InetAddress;
import java.util.List;

/**
 * @author Shadow
 * @date
 */
public class RouteExecutor {
    public static boolean isServerMatched(InvocationContext ctx, List<Route> routes, InetAddress serverIP) {
        boolean matchOne = false;
        boolean result = false;
        for (Route route : routes) {
            boolean isMatched = checkRouteCondition(ctx, route.left);
            if (isMatched) {
                matchOne = true;

            }
        }

        return true;
    }

    public static boolean checkRouteCondition(InvocationContext ctx, MatchLeftSide left) {
        return true;
    }

    public static boolean checkFieldMatcher() {
        return true;
    }

    public static boolean extractField() {
        return true;
    }

    public static boolean matched() {
        return true;
    }
}
