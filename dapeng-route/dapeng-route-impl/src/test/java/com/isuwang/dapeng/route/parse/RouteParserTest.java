package com.isuwang.dapeng.route.parse;

import com.isuwang.dapeng.core.InvocationContext;
import com.isuwang.dapeng.core.SoaHeader;
import com.isuwang.dapeng.route.Route;
import com.isuwang.dapeng.route.RouteExecutor;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
                "      callerFrom match s'app' => ip'1.2.3.4'\n" +
                "      ip match ip'1.2.3/24' => ip'1.2.3.4'\n" +
                "      otherwise => ip'1.2.3.4'";

        String str = "uip match %'1024n+0..9' => ip'1.2.3/24'";
        RouteParser parser = new RouteParser();

        List routes = new ArrayList<Route>();
        parser.parseAll(routes, source);

        List<String> servers = new ArrayList<>();
        servers.add("1.2.3.4");
        servers.add("1.2.3.5");
        servers.add("1.2.4.3");
        servers.add("1.2.5.3");

        InvocationContext ctx = new InvocationContext();
        SoaHeader soaHeader = new SoaHeader();
        soaHeader.setOperatorId(Optional.of(12));
        soaHeader.setCallerIp(Optional.of("1.2.3.4"));
        ctx.setHeader(soaHeader);

        Set<InetAddress> serverResult = RouteExecutor.execute(ctx, routes, servers);

//        assert(serverResult == List("1.2.3.4", "1.2.3.5").map(InetAddress.getByName))
    }
}
