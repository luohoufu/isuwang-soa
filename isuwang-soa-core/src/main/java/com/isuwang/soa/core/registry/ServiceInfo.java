package com.isuwang.soa.core.registry;

/**
 * Created by tangliu on 2016/1/15.
 */
public class ServiceInfo {

    public String versionName;

    public String host;

    public Integer port;

    public String getVersionName() {
        return versionName;
    }

    public Integer getCount() {
        return count;
    }

    public String getHost() {
        return host;

    }

    public Integer getPort() {
        return port;
    }

    public Integer count;

    public void setCount(Integer count) {
        this.count = count;
    }

    public ServiceInfo(String host, Integer port, String versionName) {

        this.versionName = versionName;
        this.host = host;
        this.port = port;

        this.count = 0;
    }

    public boolean equalTo(ServiceInfo sinfo) {

        if (!versionName.equals(sinfo.getVersionName()))
            return false;

        if (!host.equals(sinfo.getHost()))
            return false;

        if (port != sinfo.getPort())
            return false;

        return true;
    }

}
