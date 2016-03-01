package com.isuwang.soa.container.registry;

import com.isuwang.soa.container.Container;
import com.isuwang.soa.container.spring.SpringContainer;
import com.isuwang.soa.core.Service;
import com.isuwang.soa.core.SoaBaseProcessor;
import com.isuwang.soa.registry.RegistryAgent;
import com.isuwang.soa.registry.RegistryAgentProxy;
import com.isuwang.soa.registry.zookeeper.RegistryAgentImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry Container
 *
 * @author craneding
 * @date 16/1/19
 */
public class ZookeeperRegistryContainer implements Container {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperRegistryContainer.class);
    private static final Map<String, SoaBaseProcessor<?>> processorMap = new ConcurrentHashMap<>();

    private final RegistryAgent registryAgent = new RegistryAgentImpl(false);

    @Override
    @SuppressWarnings("unchecked")
    public void start() {
        RegistryAgentProxy.setCurrentInstance(registryAgent);

        /*
        ZookeeperWatcher siw = new ZookeeperWatcher();
        siw.usedByClent = false;
        siw.init();
        LOGGER.info("service info watcher started.");
        */

        registryAgent.setProcessorMap(processorMap);
        registryAgent.start();

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

                    ZookeeperRegistryContainer.processorMap.put(processor.getInterfaceClass().getName(), processor);

                    if (processor.getInterfaceClass().getClass() != null) {
                        Service service = processor.getInterfaceClass().getAnnotation(Service.class);

                        registryAgent.registerService(processor.getInterfaceClass().getName(), service.version());
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
    }

    @Override
    public void stop() {
        ZookeeperRegistryContainer.processorMap.clear();

        registryAgent.stop();
    }

    public static Map<String, SoaBaseProcessor<?>> getProcessorMap() {
        return ZookeeperRegistryContainer.processorMap;
    }

}
