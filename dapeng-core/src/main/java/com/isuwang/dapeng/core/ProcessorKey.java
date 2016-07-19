package com.isuwang.dapeng.core;

/**
 * Created by tangliu on 2016/3/29.
 */
public class ProcessorKey {

    public ProcessorKey(String serviceName, String versionName) {
        this.serviceName = serviceName;
        this.versionName = versionName;
    }

    private String serviceName;

    private String versionName;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    @Override
    public int hashCode() {
        return serviceName.hashCode() + versionName.hashCode();
    }

    @Override
    public boolean equals(Object o) {

        if (o instanceof ProcessorKey) {
            ProcessorKey target = (ProcessorKey) o;

            if (target.getServiceName().equals(this.serviceName) && target.getVersionName().equals(this.versionName))
                return true;
        }

        return false;
    }

    @Override
    public String toString() {
        return serviceName + ":" + versionName;
    }
}
