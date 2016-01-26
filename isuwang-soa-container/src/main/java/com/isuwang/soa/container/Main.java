package com.isuwang.soa.container;

import com.isuwang.soa.container.logback.LogbackContainer;
import com.isuwang.soa.container.netty.NettyContainer;
import com.isuwang.soa.container.registry.RegistryContainer;
import com.isuwang.soa.container.spring.SpringContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
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
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(is);

            while (reader.hasNext()) {
                int type = reader.next();
                if (type == XMLStreamConstants.START_ELEMENT) {
                    if (reader.getName().toString().equals("soa-container")) {
                        String serviceName = "";
                        while (reader.hasNext()) {
                            if (reader.next() == XMLStreamConstants.START_ELEMENT) {

                                if (reader.getName().toString().equals("name")) {
                                    serviceName = reader.getElementText();
                                } else if (reader.getName().toString().equals("ref")) {

                                    String path = reader.getElementText();
                                    Class containerClass = Main.class.getClassLoader().loadClass(path);
                                    Container container = (Container) containerClass.newInstance();
                                    containers.add(container);

                                    LOGGER.info("load container {} with path {}", serviceName, path);
                                    break;
                                }
                            }
                        }
                    }
                }
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
