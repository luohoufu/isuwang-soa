package com.isuwang.soa.container.registry;

import com.isuwang.soa.container.Container;
import com.isuwang.soa.container.spring.SpringContainer;
import com.isuwang.soa.core.SoaBaseProcessor;
import com.isuwang.soa.core.registry.RegistryAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class RegistryContainer implements Container {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegistryContainer.class);
    private static final Map<String, SoaBaseProcessor<?>> processorMap = new ConcurrentHashMap<>();

    @Override
    public void start() {
        RegistryAgent.getInstance().start();
        RegistryAgent.getInstance().setProcessorMap(processorMap);

        Map<Object, Class<?>> contexts = SpringContainer.getContexts();
        Set<Object> ctxs = contexts.keySet();

        for (Object ctx : ctxs) {
            Class<?> contextClass = contexts.get(ctx);

            try {
                Method method = contextClass.getMethod("getBeansOfType", Class.class);
                Map<String, SoaBaseProcessor> processorMap = (Map<String, SoaBaseProcessor>) method.invoke(ctx, contextClass.getClassLoader().loadClass(SoaBaseProcessor.class.getName()));

                Set<String> keys = processorMap.keySet();
                for (String key : keys) {
                    SoaBaseProcessor processor = processorMap.get(key);

                    RegistryContainer.processorMap.put(processor.getInterfaceClass().getSimpleName(), processor);

                    processor.registerService();
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void stop() {
        RegistryContainer.processorMap.clear();

        RegistryAgent.getInstance().stop();
    }

    public static Map<String, SoaBaseProcessor<?>> getProcessorMap() {
        return RegistryContainer.processorMap;
    }

}
