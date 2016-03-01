package com.isuwang.soa.doc;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Created by tangliu on 2016/3/1.
 */
public class Main {

    private static final int port = 8080;

    private static final String CONTEXT = "/";

    private static Server createServer() {

        Server server = new Server();
        server.setStopAtShutdown(true);

        ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);
        connector.setReuseAddress(false);

        server.setConnectors(new Connector[]{connector});

        WebAppContext webContext = new WebAppContext("src/main/webapp", CONTEXT);
        webContext.setDescriptor("src/main/webapp/WEB-INF/web.xml");
        webContext.setResourceBase("src/main/webapp");
        webContext.setClassLoader(Thread.currentThread().getContextClassLoader());

        server.setHandler(webContext);

        return server;
    }

    public static void main(String[] args) throws Exception {

        Server server = com.isuwang.soa.doc.Main.createServer();


        try {
            server.stop();
            server.start();
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

}
