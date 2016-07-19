package com.isuwang.dapeng.bootstrap.dynamic;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by tangliu on 2016/7/15.
 */
public class DynamicInfo {

    public String serviceName;

    public String versionName;

    public File serviceFile;

    public ClassLoader appClassLoader;

    public Object context;

    public List<URL> appUrl;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public File getServiceFile() {
        return serviceFile;
    }

    public void setServiceFile(File serviceFile) {
        this.serviceFile = serviceFile;
    }

    public ClassLoader getAppClassLoader() {
        return appClassLoader;
    }

    public void setAppClassLoader(ClassLoader appClassLoader) {
        this.appClassLoader = appClassLoader;
    }

    public Object getContext() {
        return context;
    }

    public void setContext(Object context) {
        this.context = context;
    }

    public List<URL> getAppUrl() {
        return appUrl;
    }

    public void setAppUrl(List<URL> appUrl) {
        this.appUrl = appUrl;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }
}
