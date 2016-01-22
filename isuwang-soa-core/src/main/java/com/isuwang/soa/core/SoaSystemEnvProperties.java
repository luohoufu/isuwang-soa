package com.isuwang.soa.core;

import org.apache.commons.lang3.StringUtils;

/**
 * Soa System Env Properties
 *
 * @author craneding
 * @date 16/1/19
 */
public class SoaSystemEnvProperties {

    private static final String KEY_SOA_SERVICE_IP = "soa.service.ip";
    private static final String KEY_SOA_SERVICE_PORT = "soa.service.port";
    private static final String KEY_SOA_ZOOKEEPER_HOST = "soa.zookeeper.host";
    private static final String KEY_SOA_CONTAINER_USETHREADPOOL = "soa.container.usethreadpool";
    //public static final String KEY_ZOOKEEPER_JMX_LOG4J_DISABLE = "zookeeper.jmx.log4j.disable";
    private static final String KEY_SOA_CONTAINER_PORT = "soa.container.port";

    public static final String SOA_SERVICE_IP = get(KEY_SOA_SERVICE_IP);
    public static final Integer SOA_SERVICE_PORT = StringUtils.isNotBlank(get(KEY_SOA_SERVICE_PORT)) ? Integer.valueOf(get(KEY_SOA_SERVICE_PORT)) : null;
    public static final String SOA_ZOOKEEPER_HOST = get(KEY_SOA_ZOOKEEPER_HOST, "127.0.0.1:2181");
    public static final boolean SOA_CONTAINER_USETHREADPOOL = Boolean.valueOf(get(KEY_SOA_CONTAINER_USETHREADPOOL, Boolean.TRUE.toString()));
    //public static final boolean ZOOKEEPER_JMX_LOG4J_DISABLE = Boolean.valueOf(get(KEY_ZOOKEEPER_JMX_LOG4J_DISABLE, Boolean.FALSE.toString()));
    public static final Integer SOA_CONTAINER_PORT = Integer.valueOf(get(KEY_SOA_CONTAINER_PORT, "9090"));

    public static String get(String key) {
        return get(key, null);
    }

    public static String get(String key, String defaultValue) {
        String envValue = System.getenv(key.replaceAll("\\.", "_"));

        if (envValue == null)
            return System.getProperty(key, defaultValue);

        return envValue;
    }


}
