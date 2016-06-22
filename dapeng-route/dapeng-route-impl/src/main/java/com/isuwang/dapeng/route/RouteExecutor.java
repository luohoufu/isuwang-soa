package com.isuwang.dapeng.route;


import com.isuwang.dapeng.core.InvocationContext;
import com.isuwang.dapeng.route.pattern.IpPattern;
import com.isuwang.dapeng.route.pattern.NotPattern;

import java.net.InetAddress;
import java.util.List;

/**
 * @author Eric
 * @date
 */
public class RouteExecutor {
    public static boolean isServerMatched(InvocationContext ctx, List<Route> routes, InetAddress server) {
        boolean matchOne = false;
        boolean result = false;
        for (Route route : routes) {
            boolean isMatched = checkRouteCondition(ctx, route.left);
            if (isMatched) {
                matchOne = true;
                Class rightClass = route.right.getClass();
                if (IpPattern.class.equals(rightClass)) {
//                    result = matched(server, p);
                } else if (NotPattern.class.equals(rightClass)) {
//                    result = !matched(server, p);
                } else {
                    throw new AssertionError("route right must be IpPattern or ~IpPattern");
                }
            }
        }

        if (matchOne)
            return result;
        return true;
    }


    public static List<InetAddress> execute(InvocationContext ctx, List routes, List<String> servers) {
        return null;
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
