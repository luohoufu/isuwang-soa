package com.isuwang.soa.container;

import com.isuwang.soa.container.xml.SoaContainer;
import com.isuwang.soa.container.xml.SoaContainers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXB;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Main
 *
 * @author craneding
 * @date 16/1/18
 */
public class Main {

    private static volatile boolean running = true;
    public static final String SOA_BASE = System.getProperty("soa.base");
    public static final String SOA_RUN_MODE = System.getProperty("soa.run.mode");

    public static void main(String[] args) {
        final long startTime = System.currentTimeMillis();

        final List<Container> containers = new ArrayList<>();

        try (InputStream is = new BufferedInputStream(loadInputStreamInClassLoader("containers.xml"))) {
            SoaContainers soaContainers = JAXB.unmarshal(is, SoaContainers.class);
            for (SoaContainer soaContainer : soaContainers.getSoaContainer()) {

                Class containerClass = Main.class.getClassLoader().loadClass(soaContainer.getRef());
                Container container = (Container) containerClass.newInstance();

                containers.add(container);

                System.out.println("load container " + soaContainer.getName() + " with path " + soaContainer.getRef());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            containers.forEach(Container::start);
        } catch (Throwable e) {
            e.printStackTrace();

            System.exit(-1);
        }

        final Logger logger = LoggerFactory.getLogger(Main.class);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                for (Container container : containers)
                    container.stop();

                synchronized (Main.class) {
                    running = false;

                    Main.class.notify();
                }
            }
        });

        logger.info("Server startup in {} ms", System.currentTimeMillis() - startTime);

        synchronized (Main.class) {
            while (running) {
                try {
                    Main.class.wait();
                } catch (InterruptedException e) {
                }

                logger.info("Server shutdown");
            }
        }
    }

    public static InputStream loadInputStreamInClassLoader(String path) throws FileNotFoundException {
        if (SOA_RUN_MODE.endsWith("maven"))
            return Main.class.getClassLoader().getResourceAsStream(path);
        return new FileInputStream(new File(SOA_BASE, "conf/" + path));
    }

}
