package com.isuwang.soa.registry.zookeeper;

import com.isuwang.soa.core.*;
import com.isuwang.soa.registry.ConfigKey;
import com.isuwang.soa.registry.RegistryAgent;
import com.isuwang.soa.registry.ServiceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Registry Agent
 *
 * @author craneding
 * @date 16/1/13
 */
public class RegistryAgentImpl implements RegistryAgent {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegistryAgentImpl.class);

    private final boolean isClient;
    private final ZookeeperHelper zooKeeperHelper = new ZookeeperHelper(this);

    private ZookeeperWatcher siw;
    private Map<ProcessorKey, SoaBaseProcessor<?>> processorMap;

    public RegistryAgentImpl() {
        this(true);
    }

    public RegistryAgentImpl(boolean isClient) {
        this.isClient = isClient;
    }

    @Override
    public void start() {

        if (!isClient) {
            zooKeeperHelper.setZookeeperHost(SoaSystemEnvProperties.SOA_ZOOKEEPER_HOST);
            zooKeeperHelper.connect();
        }

        siw = new ZookeeperWatcher(isClient);
        siw.init();
    }

    @Override
    public void stop() {
        zooKeeperHelper.destroy();
    }

    @Override
    public void registerService(String serverName, String versionName) {
        try {
            String path = "/soa/runtime/services/" + serverName + "/" + IPUtils.localIp() + ":" + SoaSystemEnvProperties.SOA_CONTAINER_PORT + ":" + versionName;
            String data = "";
            zooKeeperHelper.addOrUpdateServerInfo(path, data);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public void registerAllServices() {
        if (processorMap == null)
            return;

        Set<ProcessorKey> keys = processorMap.keySet();

        for (ProcessorKey key : keys) {
            SoaBaseProcessor<?> processor = processorMap.get(key);

            if (processor.getInterfaceClass().getClass() != null) {
                Service service = processor.getInterfaceClass().getAnnotation(Service.class);

                this.registerService(processor.getInterfaceClass().getName(), service.version());
            }
        }
    }

    @Override
    public void setProcessorMap(Map<ProcessorKey, SoaBaseProcessor<?>> processorMap) {
        this.processorMap = processorMap;
    }

    @Override
    public Map<ProcessorKey, SoaBaseProcessor<?>> getProcessorMap() {
        return this.processorMap;
    }

    @Override
    public List<ServiceInfo> loadMatchedServices(String serviceName, String methodName) {
        return siw.getServiceInfo(serviceName, methodName);
    }

    @Override
    public Map<String, Map<ConfigKey, Object>> getConfig() {
        return siw.getConfig();
    }

}
