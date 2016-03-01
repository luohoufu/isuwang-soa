package com.isuwang.soa.container.socket;

import com.isuwang.soa.container.Container;
import com.isuwang.soa.container.registry.ZookeeperRegistryContainer;
import com.isuwang.soa.core.SoaSystemEnvProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Socket Container
 *
 * @author craneding
 * @date 16/1/21
 */
public class SocketContainer implements Container {

    private static final Logger LOGGER = LoggerFactory.getLogger(SocketContainer.class);

    private final int port = SoaSystemEnvProperties.SOA_CONTAINER_PORT;
    private final SoaTransPool soaTransPool = new SoaTransPool();
    private volatile boolean live = true;

    private ServerSocket server = null;

    @Override
    public void start() {
        new Thread(() -> {
            try {
                server = new ServerSocket(port);

                server.setReuseAddress(true);
                server.setReceiveBufferSize(1024 * 4);// 4k
                server.setSoTimeout(60 * 1000);// 60s

                do {
                    try {
                        final Socket client = server.accept();

                        soaTransPool.execute(new SoaTransPool.SoaCodecTask(client, ZookeeperRegistryContainer.getProcessorMap()));
                    } catch (IOException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                } while (live);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }).start();
    }

    @Override
    public void stop() {
        live = false;

        try {
            server.close();
        } catch (IOException e) {
        }

        soaTransPool.shutdown();
    }

}
