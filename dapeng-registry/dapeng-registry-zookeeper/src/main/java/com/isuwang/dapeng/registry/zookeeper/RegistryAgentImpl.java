package com.isuwang.dapeng.registry.zookeeper;

import com.isuwang.dapeng.core.*;
import com.isuwang.dapeng.registry.ConfigKey;
import com.isuwang.dapeng.registry.RegistryAgent;
import com.isuwang.dapeng.registry.ServiceInfo;
import com.isuwang.dapeng.registry.ServiceInfos;
import com.isuwang.dapeng.route.Route;
import com.isuwang.dapeng.route.RouteExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * RegistryAgent using Synchronous zookeeper requesting
 *
 * @author tangliu
 * @date 2016-08-12
 */
public class RegistryAgentImpl implements RegistryAgent {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegistryAgentImpl.class);

    private final boolean isClient;
    private final ZookeeperHelper zooKeeperHelper = new ZookeeperHelper(this);

    private ZookeeperWatcher siw, zkfbw;

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

        siw = new ZookeeperWatcher(isClient, SoaSystemEnvProperties.SOA_ZOOKEEPER_HOST);
        siw.init();

        if (SoaSystemEnvProperties.SOA_ZOOKEEPER_FALLBACK_ISCONFIG) {
            zkfbw = new ZookeeperWatcher(isClient, SoaSystemEnvProperties.SOA_ZOOKEEPER_FALLBACK_HOST);
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
    public ServiceInfos loadMatchedServices(String serviceName, String versionName, boolean compatible) {

        boolean usingFallbackZookeeper = false;
        List<ServiceInfo> serviceInfos = siw.getServiceInfo(serviceName, versionName, compatible);
        if (serviceInfos.size() <= 0 && SoaSystemEnvProperties.SOA_ZOOKEEPER_FALLBACK_ISCONFIG) {
            usingFallbackZookeeper = true;
            serviceInfos = zkfbw.getServiceInfo(serviceName, versionName, compatible);
        }

        //使用路由规则，过滤可用服务器 （local模式不考虑）
        final boolean isLocal = SoaSystemEnvProperties.SOA_REMOTING_MODE.equals("local");
        if (!isLocal) {
            InvocationContext context = InvocationContext.Factory.getCurrentInstance();
            List<Route> routes = usingFallbackZookeeper ? zkfbw.getRoutes() : siw.getRoutes();
            List<ServiceInfo> tmpList = new ArrayList<>();

            for (ServiceInfo sif : serviceInfos) {
                try {
                    InetAddress inetAddress = InetAddress.getByName(sif.getHost());
                    if (RouteExecutor.isServerMatched(context, routes, inetAddress)) {
                        tmpList.add(sif);
                    }
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }

            LOGGER.info("路由过滤前可用列表{}", serviceInfos.stream().map(s -> s.getHost()).collect(Collectors.toList()));
            serviceInfos = tmpList;
            LOGGER.info("路由过滤后可用列表{}", serviceInfos.stream().map(s -> s.getHost()).collect(Collectors.toList()));
        }

        return new ServiceInfos(usingFallbackZookeeper, serviceInfos);
    }

    @Override
    public Map<ConfigKey, Object> getConfig(boolean usingFallback, String serviceKey) {

        if (usingFallback) {
            if (zkfbw.getConfigWithKey(serviceKey).entrySet().size() <= 0)
                return null;
            else
                return zkfbw.getConfigWithKey(serviceKey);
        } else {

            if (siw.getConfigWithKey(serviceKey).entrySet().size() <= 0)
                return null;
            else
                return siw.getConfigWithKey(serviceKey);
        }
    }

    @Override
    public List<Route> getRoutes(boolean usingFallback) {
        return null;
    }
}
