package com.isuwang.dapeng.container.registry;

import com.isuwang.dapeng.container.Container;
import com.isuwang.dapeng.container.spring.SpringContainer;
import com.isuwang.dapeng.core.ProcessorKey;
import com.isuwang.dapeng.core.Service;
import com.isuwang.dapeng.core.SoaBaseProcessor;
import com.isuwang.dapeng.registry.RegistryAgent;
import com.isuwang.dapeng.registry.RegistryAgentProxy;
import com.isuwang.dapeng.registry.zookeeper.RegistryAgentImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Registry Container
 *
 * @author craneding
 * @date 16/1/19
 */
public class ZookeeperRegistryContainer implements Container {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperRegistryContainer.class);

    private final static RegistryAgent registryAgent = new RegistryAgentImpl(false);

    private static ConcurrentHashMap<AtomicInteger,List<ProcessorKey>> tmpServices = new ConcurrentHashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public void start() {
        RegistryAgentProxy.setCurrentInstance(RegistryAgentProxy.Type.Server, registryAgent);

        registryAgent.setProcessorMap(ProcessorCache.getProcessorMap());
        registryAgent.start();

        Map<Object, Class<?>> contexts = SpringContainer.getContexts();
        Set<Object> ctxs = contexts.keySet();

        ctxs.forEach(ZookeeperRegistryContainer::registryService);
    }

    @Override
    public void stop() {
        ProcessorCache.getProcessorMap().clear();

        registryAgent.stop();
    }

    public static Map<ProcessorKey, SoaBaseProcessor<?>> getProcessorMap() {
        return ProcessorCache.getProcessorMap();
    }


    public static void registryService(Object context) {
        registryService(context, false, null);
    }

    public static void registryService(Object context, Boolean isTmp, AtomicInteger clientId) {
        Map<Object, Class<?>> contexts = SpringContainer.getContexts();
        Class<?> contextClass = contexts.get(context);

        List<ProcessorKey> serviceList = new ArrayList<>();

        try {
            Method method = contextClass.getMethod("getBeansOfType", Class.class);
            Map<String, SoaBaseProcessor<?>> processorMap = (Map<String, SoaBaseProcessor<?>>) method.invoke(context, contextClass.getClassLoader().loadClass(SoaBaseProcessor.class.getName()));

            Set<String> keys = processorMap.keySet();
            for (String key : keys) {
                SoaBaseProcessor<?> processor = processorMap.get(key);

                if (processor.getInterfaceClass().getClass() != null) {
                    Service service = processor.getInterfaceClass().getAnnotation(Service.class);

                    ProcessorKey processorKey = new ProcessorKey(processor.getInterfaceClass().getName(), service.version());
                    ProcessorCache.getProcessorMap().put(processorKey, processor);
                    registryAgent.registerService(processor.getInterfaceClass().getName(), service.version());
                    if(isTmp && clientId != null) {
                        serviceList.add(processorKey);
                    }
                }
            }
            if (!serviceList.isEmpty()) {
                tmpServices.put(clientId,serviceList);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

	/**
     * 获得临时服务,这里为了让Bootstrap不依赖dapeng-core，转换List
     * @return
     */
    public static Object getTmpService(AtomicInteger clientId) {
        List<String> serviceList = new ArrayList<>();
        List<ProcessorKey> services = tmpServices.get(clientId);
        for(ProcessorKey processorKey : services) {
            serviceList.add(processorKey.toString());
        }
        return serviceList;
    }

	/**
	 * 从ProcessorCache中移除临时的服务
     */
    public static void deleteFromProcessorCache(AtomicInteger clientId) {
        List<ProcessorKey> services = tmpServices.get(clientId);
        for(ProcessorKey processorKey : services) {
            ProcessorCache.getProcessorMap().remove(processorKey);
        }
    }


}
