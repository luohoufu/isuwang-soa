package com.isuwang.dapeng.container.spring;

import com.isuwang.dapeng.container.Container;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * Spring Container
 *
 * @author craneding
 * @date 16/1/18
 */
public class SpringContainer implements Container {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpringContainer.class);

    public static final String SPRING_CONFIG = "soa.spring.config";
    //public static final String DEFAULT_SPRING_CONFIG = "classpath*:META-INF/spring/*.xml";
    public static final String DEFAULT_SPRING_CONFIG = "META-INF/spring/services.xml";

    public static List<ClassLoader> appClassLoaders = new ArrayList<>();

    static Map<Object, Class<?>> contexts;

    public static Map<Object, Class<?>> getContexts() {
        return contexts;
    }

    @Override
    public void start() {
        String configPath = System.getProperty(SPRING_CONFIG);
        if (configPath == null || configPath.length() <= 0) {
            configPath = DEFAULT_SPRING_CONFIG;
        }

        contexts = new HashMap<>();

        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

        for (ClassLoader appClassLoader : SpringContainer.appClassLoaders) {
            loadService(appClassLoader, configPath);
        }

        Thread.currentThread().setContextClassLoader(contextClassLoader);
    }

    @Override
    public void stop() {
        try {
            if (contexts != null) {
                Set<Object> cxts = contexts.keySet();

                for (Object cxt : cxts) {
                    Class<?> aClass = contexts.get(cxt);

                    Method stopMethod = aClass.getMethod("stop");
                    stopMethod.invoke(cxt);

                    Method closeMethod = aClass.getMethod("close");
                    closeMethod.invoke(cxt);
                }

                contexts.clear();
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * 动态加载服务
     *
     * @param appClassLoader
     */
    public static Object loadDynamicService(ClassLoader appClassLoader) {

        String configPath = System.getProperty(SPRING_CONFIG);
        if (configPath == null || configPath.length() <= 0) {
            configPath = DEFAULT_SPRING_CONFIG;
        }

        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

        Object context = loadService(appClassLoader, configPath);

        Thread.currentThread().setContextClassLoader(contextClassLoader);

        return context;
    }

    /**
     * 根据services.xml加载服务
     *
     * @param appClassLoader
     * @param configPath
     */
    public static Object loadService(ClassLoader appClassLoader, String configPath) {
        try {
            List<String> xmlPaths = new ArrayList<>();

            Enumeration<URL> resources = appClassLoader.getResources(configPath);

            while (resources.hasMoreElements()) {
                URL nextElement = resources.nextElement();

                // not load isuwang-soa-transaction-impl
                if (!nextElement.getFile().matches(".*dapeng-transaction-impl.*"))
                    xmlPaths.add(nextElement.toString());
            }
            Class<?> appClass = appClassLoader.loadClass("org.springframework.context.support.ClassPathXmlApplicationContext");

            Class<?>[] parameterTypes = new Class[]{String[].class};
            Constructor<?> constructor = appClass.getConstructor(parameterTypes);

            Thread.currentThread().setContextClassLoader(appClassLoader);

            Object context = constructor.newInstance(new Object[]{xmlPaths.toArray(new String[0])});

            Method startMethod = appClass.getMethod("start");
            startMethod.invoke(context);

            contexts.put(context, appClass);
            return context;

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        return null;
    }

}
