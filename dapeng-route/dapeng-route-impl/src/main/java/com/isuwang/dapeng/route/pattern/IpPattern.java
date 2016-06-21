package com.isuwang.dapeng.route.pattern;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;

/**
 * Created by tangliu on 2016/6/19.
 */
public class IpPattern extends Pattern {

    public String ip;

    public int mask;

    public IpPattern() {
    }

    public IpPattern(String ip, int mask) {

        this.ip = ip;
        this.mask = mask;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getMask() {
        return mask;
    }

    public void setMask(int mask) {
        this.mask = mask;
    }

    public static java.util.regex.Pattern IP_PATTERN = java.util.regex.Pattern.compile("([\\d|.]+)([/]([\\d]+))?");

    /**
     * 将类似于"1.2.3.4/32"的字符串转为IpPattern
     *
     * @param str
     * @return
     */
    public static IpPattern parse(String str) {

        str = str.trim();
        Matcher matcher = IP_PATTERN.matcher(str);
        if (!matcher.find())
            return null;

        String ip = matcher.group(1);
        for (int i = 0; i < (4 - matcher.group(1).split("[.]").length); i++) {
            ip += ".0";
        }

        int mask = matcher.group(3) == null ? 32 : Integer.valueOf(matcher.group(3));

        return new IpPattern(ip, mask);
    }

    public String toString() {
        return "ip: " + this.ip + "  mask:" + this.mask;
    }

    public static void main(String[] args) throws IOException {

        String source = "1.2.3.4\n" +
                "1.2.3.4/32\n" +
                "1.2.3/24\n" +
                "1.2.5/16\n" +
                "1.2/16\n" +
                "1.5/8\n" +
                "1/8\n" +
                "1.2.3.123/25";


        BufferedReader br = new BufferedReader(new StringReader(source));

        String line = "";
        while ((line = br.readLine()) != null) {

            IpPattern ip = IpPattern.parse(line);
            if (ip != null)
                System.out.println(ip.toString());
        }
    }
}
