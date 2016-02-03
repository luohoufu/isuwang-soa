package com.isuwang.soa.container.registry;

import com.isuwang.soa.container.Container;
import com.isuwang.soa.container.Main;
import com.isuwang.soa.container.spring.SpringContainer;
import com.isuwang.soa.core.Service;
import com.isuwang.soa.core.SoaBaseProcessor;
import com.isuwang.soa.core.filter.Filter;
import com.isuwang.soa.core.filter.container.ContainerFilterChain;
import com.isuwang.soa.core.filter.container.xml.SoaFilter;
import com.isuwang.soa.core.filter.container.xml.SoaFilters;
import com.isuwang.soa.registry.RegistryAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXB;
import java.io.BufferedInputStream;
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
public class RegistryContainer implements Container {
    private static final Logger LOGGER = LoggerFactory.getLogger(RegistryContainer.class);
    private static final Map<String, SoaBaseProcessor<?>> processorMap = new ConcurrentHashMap<>();

    @Override
    public void start() {

        try {
            InputStream is = new BufferedInputStream(Main.loadInputStreamInClassLoader("filters-server.xml"));

            SoaFilters soaFilters = JAXB.unmarshal(is, SoaFilters.class);
            for (SoaFilter soaFilter : soaFilters.getSoaFilter()) {

                Class filterClass = RegistryContainer.class.getClassLoader().loadClass(soaFilter.getRef());
                Filter filter = (Filter) filterClass.newInstance();

                ContainerFilterChain.addFilter(filter);
                LOGGER.info("service load filter {} with path {}", soaFilter.getName(), soaFilter.getRef());
            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }

        RegistryAgent.getInstance().start();
        RegistryAgent.getInstance().setProcessorMap(processorMap);

        Map<Object, Class<?>> contexts = SpringContainer.getContexts();
        Set<Object> ctxs = contexts.keySet();

        for (Object ctx : ctxs) {
            Class<?> contextClass = contexts.get(ctx);

            InputStream filterInput = null;

            try {
//                filterInput = contextClass.getClassLoader().getResourceAsStream("filters-container.xml");
//                if(filterInput != null) {
//                    //TODO ContainerFilterChain add filter
//
//                }

                Method method = contextClass.getMethod("getBeansOfType", Class.class);
                Map<String, SoaBaseProcessor<?>> processorMap = (Map<String, SoaBaseProcessor<?>>) method.invoke(ctx, contextClass.getClassLoader().loadClass(SoaBaseProcessor.class.getName()));

                Set<String> keys = processorMap.keySet();
                for (String key : keys) {
                    SoaBaseProcessor<?> processor = processorMap.get(key);

                    RegistryContainer.processorMap.put(processor.getInterfaceClass().getSimpleName(), processor);

                    if (processor.getInterfaceClass().getClass() != null) {
                        Service service = processor.getInterfaceClass().getAnnotation(Service.class);

                        RegistryAgent.getInstance().registerService(processor.getInterfaceClass().getSimpleName(), service.version());
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
        RegistryContainer.processorMap.clear();

        RegistryAgent.getInstance().stop();
    }

    public static Map<String, SoaBaseProcessor<?>> getProcessorMap() {
        return RegistryContainer.processorMap;
    }

}
