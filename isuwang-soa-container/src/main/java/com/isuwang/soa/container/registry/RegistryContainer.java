package com.isuwang.soa.container.registry;

import com.isuwang.soa.container.Container;
import com.isuwang.soa.container.spring.SpringContainer;
import com.isuwang.soa.core.SoaBaseProcessor;
import com.isuwang.soa.core.registry.RegistryAgent;
import org.springframework.context.support.ClassPathXmlApplicationContext;

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

    private static final Map<String, SoaBaseProcessor<?>> processorMap = new ConcurrentHashMap<>();

    @Override
    public void start() {
        RegistryAgent.getInstance().start();
        RegistryAgent.getInstance().setProcessorMap(processorMap);

        ClassPathXmlApplicationContext context = SpringContainer.getContext();

        Map<String, SoaBaseProcessor> processorMap = context.getBeansOfType(SoaBaseProcessor.class);

        Set<String> keys = processorMap.keySet();
        for (String key : keys) {
            SoaBaseProcessor processor = processorMap.get(key);

            RegistryContainer.processorMap.put(processor.getInterfaceClass().getSimpleName(), processor);

            processor.registerService();
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
