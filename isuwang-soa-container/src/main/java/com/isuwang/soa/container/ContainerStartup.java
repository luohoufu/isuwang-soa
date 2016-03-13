package com.isuwang.soa.container;

import com.isuwang.soa.container.apidoc.ApidocContainer;
import com.isuwang.soa.container.conf.SoaServer;
import com.isuwang.soa.container.conf.SoaServerContainer;
import com.isuwang.soa.core.SoaSystemEnvProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXB;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * ContainerStartup
 *
 * @author craneding
 * @date 16/1/18
 */
public class ContainerStartup {

    private static volatile boolean running = true;
    public static final String SOA_BASE = System.getProperty("soa.base");
    public static final String SOA_RUN_MODE = System.getProperty("soa.run.mode", "maven");
    public static SoaServer soaServer = null;

    public static void startup() {
        final long startTime = System.currentTimeMillis();

        final List<Container> containers = new ArrayList<>();

        try (InputStream is = new BufferedInputStream(loadInputStreamInClassLoader("server-conf.xml"))) {
            soaServer = JAXB.unmarshal(is, SoaServer.class);

            for (SoaServerContainer soaContainer : soaServer.getSoaServerContainers().getSoaServerContainer()) {
                Class containerClass = ContainerStartup.class.getClassLoader().loadClass(soaContainer.getRef());
                Container container = (Container) containerClass.newInstance();

                containers.add(container);

                System.out.println("load container " + soaContainer.getName() + " with path " + soaContainer.getRef());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 本地模式
        final boolean localMode = SoaSystemEnvProperties.SOA_REMOTING_MODE.equals("local");

        if (localMode) {// 剔除Registry的容器
            List<SoaServerContainer> collect = soaServer.getSoaServerContainers()
                    .getSoaServerContainer()
                    .stream()
                    .filter(soaServerContainer -> soaServerContainer.getRef().startsWith("com.isuwang.soa.container.registry."))
                    .collect(toList());

            soaServer.getSoaServerContainers().getSoaServerContainer().removeAll(collect);
        }

        final boolean hasApidocContainer = soaServer.getSoaServerContainers()
                .getSoaServerContainer()
                .stream()
                .filter(soaContainer -> soaContainer.getRef().equals(ApidocContainer.class.getName()))
                .count() > 0;

        if ("maven".equals(SOA_RUN_MODE) && !hasApidocContainer)
            containers.add(new ApidocContainer());

        try {
            containers.forEach(Container::start);
        } catch (Throwable e) {
            e.printStackTrace();

            System.exit(-1);
        }

        final Logger logger = LoggerFactory.getLogger(ContainerStartup.class);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                for (Container container : containers)
                    container.stop();

                synchronized (ContainerStartup.class) {
                    running = false;

                    ContainerStartup.class.notify();
                }
            }
        });

        logger.info("Server startup in {} ms", System.currentTimeMillis() - startTime);

        synchronized (ContainerStartup.class) {
            while (running) {
                try {
                    ContainerStartup.class.wait();
                } catch (InterruptedException e) {
                }

                logger.info("Server shutdown");
            }
        }
    }

    public static InputStream loadInputStreamInClassLoader(String path) throws FileNotFoundException {
        if (SOA_RUN_MODE.endsWith("maven"))
            return ContainerStartup.class.getClassLoader().getResourceAsStream(path);
        return new FileInputStream(new File(SOA_BASE, "conf/" + path));
    }

}
