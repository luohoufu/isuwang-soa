package com.isuwang.soa.core;

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
    private static final String KEY_SOA_APIDOC_PORT = "soa.apidoc.port";
    private static final String KEY_SOA_REMOTING_MODE = "soa.remoting.mode";

    public static final String SOA_SERVICE_IP = get(KEY_SOA_SERVICE_IP, "127.0.0.1");
    public static final Integer SOA_SERVICE_PORT = Integer.valueOf(get(KEY_SOA_SERVICE_PORT, "9090"));
    public static final String SOA_ZOOKEEPER_HOST = get(KEY_SOA_ZOOKEEPER_HOST, "127.0.0.1:2181");
    public static final boolean SOA_CONTAINER_USETHREADPOOL = Boolean.valueOf(get(KEY_SOA_CONTAINER_USETHREADPOOL, Boolean.TRUE.toString()));
    //public static final boolean ZOOKEEPER_JMX_LOG4J_DISABLE = Boolean.valueOf(get(KEY_ZOOKEEPER_JMX_LOG4J_DISABLE, Boolean.FALSE.toString()));
    public static final Integer SOA_CONTAINER_PORT = Integer.valueOf(get(KEY_SOA_CONTAINER_PORT, "9090"));
    public static final Integer SOA_APIDOC_PORT = Integer.valueOf(get(KEY_SOA_APIDOC_PORT, "8080"));
    public static final String SOA_REMOTING_MODE = get(KEY_SOA_REMOTING_MODE, "remote");

    public static String get(String key) {
        return get(key, null);
    }

    public static String get(String key, String defaultValue) {
        String envValue = System.getenv(key.replaceAll("\\.", "_"));

        if (envValue == null)
            return System.getProperty(key, defaultValue);

        return envValue;
    }


    private static boolean isNotBlank(String val) {
        return val != null && !val.trim().isEmpty();
    }
}
