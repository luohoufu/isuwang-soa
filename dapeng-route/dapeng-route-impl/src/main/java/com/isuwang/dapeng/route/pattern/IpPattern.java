package com.isuwang.dapeng.route.pattern;

/**
 * Created by tangliu on 2016/6/19.
 */
public class IpPattern extends Pattern {

    public String ip;

    public int mask;

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
}
