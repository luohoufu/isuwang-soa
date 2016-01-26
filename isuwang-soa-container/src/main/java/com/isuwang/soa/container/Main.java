package com.isuwang.soa.container;

import com.isuwang.soa.container.xml.SoaContainer;
import com.isuwang.soa.container.xml.SoaContainers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXB;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Main
 *
 * @author craneding
 * @date 16/1/18
 */
public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private static volatile boolean running = true;

    public static void main(String[] args) {
        //System.setProperty(SoaSystemEnvProperties.KEY_SOA_ZOOKEEPER_HOST, "192.168.3.39:2181");

        final List<Container> containers = new ArrayList<>();

        try {
            InputStream is = Main.class.getClassLoader().getResourceAsStream("containers.xml");


            SoaContainers soaContainers = JAXB.unmarshal(is, SoaContainers.class);
            for (SoaContainer soaContainer : soaContainers.getSoaContainer()) {

                Class containerClass = Main.class.getClassLoader().loadClass(soaContainer.getRef());
                Container container = (Container) containerClass.newInstance();

                containers.add(container);

                LOGGER.info("load container {} with path {}", soaContainer.getName(), soaContainer.getRef());
            }

        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }

        try {
            containers.forEach(Container::start);
        } catch (Throwable e) {
            e.printStackTrace();

            System.exit(-1);
        }

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

        System.out.println("soa is started.");

        synchronized (Main.class) {
            while (running) {
                try {
                    Main.class.wait();
                } catch (InterruptedException e) {
                }

                System.out.println("soa is stopped.");
            }
        }
    }

}
