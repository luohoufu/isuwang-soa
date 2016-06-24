package com.isuwang.dapeng.route.parse;

import com.isuwang.dapeng.route.*;
import com.isuwang.dapeng.route.pattern.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eric on 2016/6/22.
 */
public class RouteParser {
    private static final String whiteSpace = " ";
    private static final String eol = "\\n";

    public List<Route> parse(String config) {
        List<Route> routes = new ArrayList<>();
        parseAll(routes, config);
        return routes;
    }

    /**
     * %'1024n+0..9'
     * ~%'1024n+0..9'
     * n'10'
     * ~n'10..20'
     * s"getSku"
     * r"get.*"
     * ip"1.2.3/24"
     *
     * @param rulePatterStr
     * @return
     */
    public void parseRulePatterns(List<Pattern> patterns, String rulePatterStr) {
        Pattern pattern = null;
        String prefix = rulePatterStr.substring(0, 2);
        int strLength = rulePatterStr.length();
        switch (prefix) {
            case "%'":
                pattern = getModPattern(rulePatterStr, strLength);
                break;
            case "~%":
                String rulePatterStrTemp = rulePatterStr.substring(1);
                int length = rulePatterStrTemp.length();
                pattern = new NotPattern(getModPattern(rulePatterStrTemp, length));
                break;
            case "n'":
                String rangeStr0 = rulePatterStr.substring(2, strLength - 1);
                pattern = getRangePattern(rangeStr0);
                break;
            case "~n":
                String rangeStr1 = rulePatterStr.substring(3, strLength - 1);
                pattern = new NotPattern(getRangePattern(rangeStr1));
                break;
            case "r'":
                String methodName = rulePatterStr.substring(2, strLength - 1);
                pattern = new RegexpPattern(methodName);
                break;
            case "s'":
                String callerFrom = rulePatterStr.substring(2, strLength - 1);
                pattern = new StringPattern(callerFrom);
                break;
            case "~s":
                String notS = rulePatterStr.substring(3, strLength - 1);
                pattern = new NotPattern(new StringPattern(notS));
                break;
            case "ip":
                String ip = rulePatterStr.substring(3, strLength - 1);
                pattern = extractIp(ip);
                break;
            case "~i":
                String notIp = rulePatterStr.substring(4, strLength - 1);
                pattern = new NotPattern(extractIp(notIp));
                break;
            default:
        }
        patterns.add(pattern);
    }

    private Pattern getRangePattern(String rangeStr) {
        Pattern pattern;
        if (rangeStr.indexOf("..") != -1) {
            int low = Integer.valueOf(rangeStr.substring(0, rangeStr.indexOf("..")));
            int high = Integer.valueOf(rangeStr.substring(rangeStr.indexOf("..") + 2));
            pattern = new RangePattern(low, high);
        } else {
            pattern = new NumberPattern(rangeStr);
        }
        return pattern;
    }

    private Pattern getModPattern(String rulePatterStr, int strLength) {
        Pattern pattern;
        int base = Integer.valueOf(rulePatterStr.substring(2, rulePatterStr.indexOf("+") - 1));
        String range = rulePatterStr.substring(rulePatterStr.indexOf("+") + 1, strLength - 1);
        int low = Integer.valueOf(range.substring(0, 1));
        int high = Integer.valueOf(range.substring(range.length() - 1));
        RangePattern rangePattern = new RangePattern(low, high);
        pattern = new ModPattern(base, rangePattern);
        return pattern;
    }

    public Pattern parseIpPattern(String rulePatterStr) {
        Pattern pattern = null;
        String ipStr = rulePatterStr.substring(rulePatterStr.indexOf("'") + 1, rulePatterStr.lastIndexOf("'"));
        if (rulePatterStr.startsWith("~")) {
            pattern = new NotPattern(extractIp(ipStr));
        } else {
            pattern = extractIp(ipStr);
        }
        return pattern;
    }

    /**
     * 1.2.3.4 | 1.2.3.5/32 | 1。2.3.6/32
     *
     * @param str
     * @return
     */
    public Pattern extractIp(String str) {
        Pattern pattern = null;

        String[] ips = str.split("[|]");
        List<IpNode> ipNodes = new ArrayList<>();
        for (String ip : ips) {

            ip = ip.trim();
            if (ip.indexOf("/") != -1) {

                int mask = Integer.valueOf(ip.substring(ip.indexOf("/") + 1));
                ip = ip.substring(0, ip.indexOf("/"));
                while (ip.split("[.]").length < 4) {
                    ip += ".0";
                }
                IpNode node = new IpNode(ip, mask);
                ipNodes.add(node);
            } else {

                IpNode node = new IpNode(ip, 32);
                ipNodes.add(node);
            }
        }

        pattern = new IpPattern(ipNodes);

        return pattern;
    }

    public List<Matcher> parseMatchers(String matcherStr, String rulePatterStr) {
        List<Matcher> matchers = new ArrayList<>();
        Matcher matcher = null;
        Id id;
        if (matcherStr != null) {
            id = new Id(matcherStr, false);
            matcher = new Matcher();
            matcher.setId(id);
            List<Pattern> patterns = new ArrayList<>();
            parseRulePatterns(patterns, rulePatterStr);
            matcher.setPatterns(patterns);
        }
        matchers.add(matcher);
        return matchers;
    }

    public Route constructRoute(String routeLine) {
        Route route = new Route();
        String ruleArray[] = routeLine.split(whiteSpace);
        String idStr = null;
        String rulePatterStr = null;
        String targetPatterStr = null;
        MatchLeftSide left;
        if (ruleArray.length == 5) {
            idStr = ruleArray[0];
            rulePatterStr = ruleArray[2];
            targetPatterStr = ruleArray[4];
            left = new Matchers();
            left.matchers = (parseMatchers(idStr, rulePatterStr));
        } else {
            targetPatterStr = ruleArray[2];
            left = new OtherWise();
        }

        Pattern right = parseIpPattern(targetPatterStr);

        route.setLeft(left);
        route.setRight(right);
        return route;
    }

    public void parseAll(List<Route> routes, String config) {
        String[] routeLines = config.split(eol);
        for (String routeLine : routeLines) {
            routes.add(constructRoute(routeLine.trim()));
        }
    }
}
