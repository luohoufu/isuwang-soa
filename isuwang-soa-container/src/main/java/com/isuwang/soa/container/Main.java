package com.isuwang.soa.container;

import com.isuwang.soa.container.logback.LogbackContainer;
import com.isuwang.soa.container.netty.NettyContainer;
import com.isuwang.soa.container.registry.RegistryContainer;
import com.isuwang.soa.container.spring.SpringContainer;

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

    public static void main(String[] args) {
        //System.setProperty(SoaSystemEnvProperties.KEY_SOA_ZOOKEEPER_HOST, "192.168.3.39:2181");

        final List<Container> containers = new ArrayList<>();
        containers.add(new LogbackContainer());
        containers.add(new SpringContainer());
        containers.add(new RegistryContainer());
        containers.add(new NettyContainer());
        //containers.add(new SocketContainer());

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
