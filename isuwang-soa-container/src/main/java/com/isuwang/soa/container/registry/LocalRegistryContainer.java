package com.isuwang.soa.container.registry;

import com.isuwang.soa.container.Container;
import com.isuwang.soa.container.spring.SpringContainer;
import com.isuwang.soa.core.ProcessorKey;
import com.isuwang.soa.core.Service;
import com.isuwang.soa.core.SoaBaseProcessor;
import com.isuwang.soa.core.SoaSystemEnvProperties;
import com.isuwang.soa.registry.ConfigKey;
import com.isuwang.soa.registry.RegistryAgent;
import com.isuwang.soa.registry.RegistryAgentProxy;
import com.isuwang.soa.registry.ServiceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Local Registry Container
 *
 * @author craneding
 * @date 16/3/13
 */
public class LocalRegistryContainer implements Container, RegistryAgent {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalRegistryContainer.class);

    @Override
    public void start() {
        Map<Object, Class<?>> contexts = SpringContainer.getContexts();
        Set<Object> ctxs = contexts.keySet();

        for (Object ctx : ctxs) {
            Class<?> contextClass = contexts.get(ctx);

            InputStream filterInput = null;

            try {
                Method method = contextClass.getMethod("getBeansOfType", Class.class);
                Map<String, SoaBaseProcessor<?>> processorMap = (Map<String, SoaBaseProcessor<?>>) method.invoke(ctx, contextClass.getClassLoader().loadClass(SoaBaseProcessor.class.getName()));

                Set<String> keys = processorMap.keySet();
                for (String key : keys) {
                    SoaBaseProcessor<?> processor = processorMap.get(key);

                    if (processor.getInterfaceClass().getClass() != null) {
                        Service service = processor.getInterfaceClass().getAnnotation(Service.class);

                        ProcessorKey processorKey = new ProcessorKey(processor.getInterfaceClass().getName(), service.version());
                        getProcessorMap().put(processorKey, processor);

                        this.registerService(processor.getInterfaceClass().getName(), service.version());
                    }
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            } finally {
                if (filterInput != null)
                    try {
                        filterInput.close();
                    } catch (IOException e) {
                    }
            }
        }

        RegistryAgentProxy.setCurrentInstance(RegistryAgentProxy.Type.Server, this);
    }

    @Override
    public void stop() {
        ProcessorCache.getProcessorMap().clear();
    }

    @Override
    public void registerService(String serverName, String versionName) {
        LOGGER.info("注册本地服务:{} {}", serverName, versionName);
    }

    @Override
    public void registerAllServices() {
        Set<ProcessorKey> keys = ProcessorCache.getProcessorMap().keySet();

        for (ProcessorKey key : keys) {
            SoaBaseProcessor<?> processor = ProcessorCache.getProcessorMap().get(key);

            if (null != processor.getInterfaceClass().getClass()) {
                Service service = processor.getInterfaceClass().getAnnotation(Service.class);

                this.registerService(processor.getInterfaceClass().getName(), service.version());
            }
        }
    }

    @Override
    public void setProcessorMap(Map<ProcessorKey, SoaBaseProcessor<?>> processorMap) {
    }

    @Override
    public Map<ProcessorKey, SoaBaseProcessor<?>> getProcessorMap() {
        return ProcessorCache.getProcessorMap();
    }

    @Override
    public List<ServiceInfo> loadMatchedServices(String serviceName, String versionName, boolean compatible) {
        final List<ServiceInfo> objects = new ArrayList<>();
        objects.add(new ServiceInfo("127.0.0.1", SoaSystemEnvProperties.SOA_CONTAINER_PORT, "*"));

        return objects;
    }

    @Override
    public Map<String, Map<ConfigKey, Object>> getConfig() {
        return new HashMap<>();
    }

}
