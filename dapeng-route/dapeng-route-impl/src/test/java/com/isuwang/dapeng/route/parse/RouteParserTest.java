package com.isuwang.dapeng.route.parse;

import com.isuwang.dapeng.route.Route;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Eric on 2016-06-21
 * @date
 */
public class RouteParserTest {

    public static void main(String args[]) {
        String source = "operatorId match %'1024n+0..9' => ip'1.2.3/24'\n" +
                "      operatorId match ~%'1024n+0..9' => ~ip'1.2.3.4'\n" +
                "      operatorId match n'10' => ip'1.2.3.4'\n" +
                "      operatorId match ~n'10..20' => ip'1.2.3.4'\n" +
                "      methodName match r'get.*' => ip'1.2.3.4'\n" +
                "      callerFrom match s'app' => ip'1.2.3.4'\n" +
                "      ip match ip'1.2.3/24' => ip'1.2.3.4'\n" +
                "      otherwise => ip'1.2.3.4'";

        String str = "uip match %'1024n+0..9' => ip'1.2.3/24'";
        RouteParser parser = new RouteParser();

        List routs = new ArrayList<Route>();
        parser.parseAll(routs, source);

//        List<String> servers =new ArrayList<>();
//        servers.add("1.2.3.4");
//        servers.add("1.2.3.5");
//        servers.add("1.2.4.3");
//        servers.add("1.2.5.3");
//
//        InvocationContext ctx = new InvocationContext();
//        ctx.setCalleeIp("1.2.3.4");
//        List<InetAddress> serverResult = RouteExecutor.execute(ctx, routes, servers);
//
//        assert(serverResult == List("1.2.3.4", "1.2.3.5").map(InetAddress.getByName))
    }
}
