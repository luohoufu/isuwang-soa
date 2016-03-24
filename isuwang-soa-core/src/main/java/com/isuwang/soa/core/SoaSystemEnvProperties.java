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
    private static final String KEY_SOA_MONITOR_ENABLE = "soa.monitor.enable";
    private static final String KEY_SOA_SERVICE_CALLERFROM = "soa.service.callerfrom";
    private static final String KEY_SOA_SERVICE_TIMEOUT = "soa.service.timeout";
    private static final String KEY_SOA_CORE_POOL_SIZE = "soa.core.pool.size";
    //private static final String KEY_SOA_MAX_POOL_CORE = "soa.max.pool.core";
    //private static final String KEY_SOA_KEEP_ALIVE_TIME = "soa.keep.alive.time";
    private static final String KEY_SOA_MAX_READ_BUFFER_SIZE = "soa.max.read.buffer.size";
    private static final String KEY_SOA_LOCAL_HOST_NAME = "soa.local.host.name";

    public static final String SOA_SERVICE_IP = get(KEY_SOA_SERVICE_IP, "127.0.0.1");
    public static final Integer SOA_SERVICE_PORT = Integer.valueOf(get(KEY_SOA_SERVICE_PORT, "9090"));
    public static final String SOA_ZOOKEEPER_HOST = get(KEY_SOA_ZOOKEEPER_HOST, "127.0.0.1:2181");
    public static final boolean SOA_CONTAINER_USETHREADPOOL = Boolean.valueOf(get(KEY_SOA_CONTAINER_USETHREADPOOL, Boolean.TRUE.toString()));
    //public static final boolean ZOOKEEPER_JMX_LOG4J_DISABLE = Boolean.valueOf(get(KEY_ZOOKEEPER_JMX_LOG4J_DISABLE, Boolean.FALSE.toString()));
    public static final Integer SOA_CONTAINER_PORT = Integer.valueOf(get(KEY_SOA_CONTAINER_PORT, "9090"));
    public static final Integer SOA_APIDOC_PORT = Integer.valueOf(get(KEY_SOA_APIDOC_PORT, "8080"));
    public static final String SOA_REMOTING_MODE = get(KEY_SOA_REMOTING_MODE, "remote");
    public static final boolean SOA_MONITOR_ENABLE = Boolean.valueOf(get(KEY_SOA_MONITOR_ENABLE, "false"));
    public static final String SOA_SERVICE_CALLERFROM = get(KEY_SOA_SERVICE_CALLERFROM, "unknown");
    public static final Long SOA_SERVICE_TIMEOUT = Long.valueOf(get(KEY_SOA_SERVICE_TIMEOUT, "45000"));

    public static final Integer SOA_CORE_POOL_SIZE = Integer.valueOf(get(KEY_SOA_CORE_POOL_SIZE, (Runtime.getRuntime().availableProcessors() * 2) + ""));
    //public static final Integer SOA_MAX_POOL_SIZE = Integer.valueOf(get(KEY_SOA_MAX_POOL_CORE, SOA_CORE_POOL_SIZE.toString()));
    //public static final Long SOA_KEEP_ALIVE_TIME = Long.valueOf(get(KEY_SOA_KEEP_ALIVE_TIME, "60000"));
    public static final Long SOA_MAX_READ_BUFFER_SIZE = Long.valueOf(get(KEY_SOA_MAX_READ_BUFFER_SIZE, (1024 * 1024 * 5) + ""));// 5M

    public static final String SOA_LOCAL_HOST_NAME = get(KEY_SOA_LOCAL_HOST_NAME);

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
