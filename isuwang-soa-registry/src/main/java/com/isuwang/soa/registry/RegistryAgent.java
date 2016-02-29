package com.isuwang.soa.registry;

import com.isuwang.soa.core.IPUtils;
import com.isuwang.soa.core.Service;
import com.isuwang.soa.core.SoaBaseProcessor;
import com.isuwang.soa.core.SoaSystemEnvProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

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
            String path = "/soa/runtime/services/" + serverName + "/" + IPUtils.localIp() + ":" + SoaSystemEnvProperties.SOA_CONTAINER_PORT + ":" + versionName;
            String data = "";
            zooKeeperHelper.addOrUpdateServerInfo(path, data);
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
            SoaBaseProcessor<?> processor = processorMap.get(key);

            if (processor.getInterfaceClass().getClass() != null) {
                Service service = processor.getInterfaceClass().getAnnotation(Service.class);
                RegistryAgent.getInstance().registerService(processor.getInterfaceClass().getName(), service.version());
            }
        }
    }


    public void setProcessorMap(Map<String, SoaBaseProcessor<?>> processorMap) {
        this.processorMap = processorMap;
    }

}
