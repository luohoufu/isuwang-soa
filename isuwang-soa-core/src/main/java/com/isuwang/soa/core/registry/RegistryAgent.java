package com.isuwang.soa.core.registry;

import com.isuwang.soa.core.IPUtils;
import com.isuwang.soa.core.SoaBaseProcessor;
import com.isuwang.soa.core.SoaSystemEnvProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry Agent
 *
 * @author craneding
 * @date 16/1/13
 */
public class RegistryAgent {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegistryAgent.class);

    private static final RegistryAgent agent = new RegistryAgent();

    private final ZooKeeperHelper zooKeeperHelper = new ZooKeeperHelper();

    private Map<String, SoaBaseProcessor<?>> processorMap;
    private final Map<String, StubRegistry> stubRegistryMap = new ConcurrentHashMap<>();

    public static RegistryAgent getInstance() {
        return agent;
    }

    public void start() {
        zooKeeperHelper.setZookeeperHost(SoaSystemEnvProperties.SOA_ZOOKEEPER_HOST);
        zooKeeperHelper.connect();
    }

    public void stop() {
        zooKeeperHelper.destroy();
    }

    public void registerService(String serverName, String versionName) {
        try {
            zooKeeperHelper.addOrUpdateServerInfo("/soa/runtime/services/" + serverName + "/" + IPUtils.localIp() + ":" + SoaSystemEnvProperties.SOA_CONTAINER_PORT + ":" + versionName, "");
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * processorMap存在的情况下重新注册sevice到zookeeper
     */
    public void registerProcessor() {
        Set<String> keys = processorMap.keySet();

        for (String key : keys) {
            SoaBaseProcessor processor = processorMap.get(key);
            processor.registerService();
        }
    }


    public void setProcessorMap(Map<String, SoaBaseProcessor<?>> processorMap) {
        this.processorMap = processorMap;
    }

}
