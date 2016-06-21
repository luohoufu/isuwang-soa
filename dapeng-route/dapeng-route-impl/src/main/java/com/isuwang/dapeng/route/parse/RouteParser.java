package com.isuwang.dapeng.route.parse;

import com.isuwang.dapeng.route.MatcherId;
import com.isuwang.dapeng.route.Matchers;
import com.isuwang.dapeng.route.Route;
import com.isuwang.dapeng.route.pattern.IpPattern;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tangliu on 2016/6/19.
 */
public class RouteParser {


    private static Pattern routePattern = Pattern.compile("^(\\S+)\\s+match\\s+([~]?)([^\"]+)\"(\\S+)\"\\s+=>\\s+([~]?)ip\"([^\"]+)\"");

    /**
     * 将配置文件，解析成路由规则列表
     *
     * @param source
     * @return
     */
    public static List<Route> parseAll(String source) throws IOException {

        List<Route> routes = new ArrayList<>();

        source = "operatorId match ~%\"1024n+0..9\" => ip\"1.2.3/24\"\n" +
                "      operatorId match ~%\"1024n+0..9\" => ~ip\"1.2.3.4\"\n" +
                "      operatorId match ~n\"10\" => ip\"1.2.3.4\"\n" +
                "      operatorId match ~n\"10..20\n => ip\"1.2.3.4\"\n" +
                "      methodName match r\"get.*\" => ip\"1.2.3.4\"\n" +
                "      callerFrom match s\"app\" => ip\"1.2.3.4\"\n" +
                "      ip match ip\"1.2.3/24\" => ip\"1.2.3.4\"\n" +
                "      otherwise => ip\"1.2.3.4\"";


        BufferedReader br = new BufferedReader(new StringReader(source));

        String line = "";
        while ((line = br.readLine()) != null) {

            line = line.trim();

            Matcher matcher = routePattern.matcher(line);
            if (matcher.find()) {
                if (matcher.group(2).equals("~")) {

                } else {
                    switch (matcher.group(3)) {
                        case "r":
                            break;
                        case "kv":
                            break;
                        case "ip":
                            routes.add(generateIpRoute(matcher.group(4)));
                            break;
                        case "n":
                            break;
                        case "s":
                            break;
                        case "%":
                            break;
                        default:
                            break;
                    }

                }
            }

        }
        return routes;
    }


    /**
     * 生成作用于ip的规则
     *
     * @return
     */
    private static Route generateIpRoute(String str) {

        Route route = new Route();
        Matchers matchers = new Matchers();
        com.isuwang.dapeng.route.Matcher matcher = new com.isuwang.dapeng.route.Matcher();

        matcher.setMatcherID(MatcherId.IP);

        com.isuwang.dapeng.route.pattern.Pattern pattern = IpPattern.parse(str);
        List<com.isuwang.dapeng.route.pattern.Pattern> patterns = new ArrayList<>();
        patterns.add(pattern);
        matcher.setPatterns(patterns);

        List<com.isuwang.dapeng.route.Matcher> matcherList = new ArrayList<>();
        matcherList.add(matcher);
        matchers.setMatchers(matcherList);
        route.setLeft(matchers);

        return route;
    }

    public static void main(String[] args) {
        try {
            parseAll("");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
