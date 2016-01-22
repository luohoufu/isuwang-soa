package com.isuwang.soa.container.spring;

import com.isuwang.soa.container.Container;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Spring Container
 *
 * @author craneding
 * @date 16/1/18
 */
public class SpringContainer implements Container {

    public static final String SPRING_CONFIG = "soa.spring.config";
    public static final String DEFAULT_SPRING_CONFIG = "classpath*:META-INF/spring/*.xml";

    static ClassPathXmlApplicationContext context;

    public static ClassPathXmlApplicationContext getContext() {
        return context;
    }

    @Override
    public void start() {
        String configPath = System.getProperty(SPRING_CONFIG);
        if (configPath == null || configPath.length() <= 0) {
            configPath = DEFAULT_SPRING_CONFIG;
        }

        context = new ClassPathXmlApplicationContext(configPath.split("[,\\s]+"));
        context.start();
    }

    @Override
    public void stop() {
        if (context != null) {
            context.stop();
            context.close();
            context = null;
        }
    }

}
