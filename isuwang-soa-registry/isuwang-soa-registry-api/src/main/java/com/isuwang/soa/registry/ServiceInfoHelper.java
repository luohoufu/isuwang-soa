package com.isuwang.soa.registry;

/**
 * Created by tangliu on 2016/2/29.
 */
public interface ServiceInfoHelper {

    void setHost(String host);

    void addOrUpdateServerInfo(String path, String data) throws Exception;

    void connect();

}
