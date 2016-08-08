package com.isuwang.dapeng.registry.zookeeper;

import com.isuwang.dapeng.core.ProcessorKey;
import com.isuwang.dapeng.core.Service;
import com.isuwang.dapeng.core.SoaBaseProcessor;
import com.isuwang.dapeng.core.SoaSystemEnvProperties;
import com.isuwang.dapeng.registry.ConfigKey;
import com.isuwang.dapeng.registry.RegistryAgent;
import com.isuwang.dapeng.registry.ServiceInfo;
import com.isuwang.dapeng.route.Route;
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

    private ZookeeperFallbackWatcher zkfbw;

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

        if (SoaSystemEnvProperties.SOA_ZOOKEEPER_FALLBACK_ISCONFIG) {
            zkfbw = new ZookeeperFallbackWatcher();
            zkfbw.init();
        }
    }

    @Override
    public void stop() {
        zooKeeperHelper.destroy();
        if (siw != null)
            siw.destroy();

        if (zkfbw != null)
            zkfbw.destroy();
    }

    @Override
    public void registerService(String serverName, String versionName) {
        try {
            String path = "/soa/runtime/services/" + serverName + "/" + SoaSystemEnvProperties.SOA_CONTAINER_IP + ":" + SoaSystemEnvProperties.SOA_CONTAINER_PORT + ":" + versionName;
            String data = "";
            zooKeeperHelper.addOrUpdateServerInfo(path, data);
            zooKeeperHelper.runForMaster(ZookeeperHelper.generateKey(serverName, versionName));
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

        //如果开启了全局事务，将事务服务也注册到zookeeper,为了主从竞选，只有主全局事务管理器会执行
        if (SoaSystemEnvProperties.SOA_TRANSACTIONAL_ENABLE) {
            this.registerService("com.isuwang.dapeng.transaction.api.service.GlobalTransactionService", "1.0.0");
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
    public List<ServiceInfo> loadMatchedServices(String serviceName, String versionName, boolean compatible) {

        List<ServiceInfo> serviceInfos = siw.getServiceInfo(serviceName, versionName, compatible);
        if (serviceInfos.size() <= 0 && SoaSystemEnvProperties.SOA_ZOOKEEPER_FALLBACK_ISCONFIG) {
            serviceInfos = zkfbw.getServiceInfo(serviceName, versionName, compatible);
        }
        return serviceInfos;
    }

    @Override
    public Map<String, Map<ConfigKey, Object>> getConfig() {
        return siw.getConfig();
    }

    @Override
    public List<Route> getRoutes() {
        return siw.getRoutes();
    }


}
