package com.isuwang.soa.core.registry;

import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by tangliu on 2016/1/15.
 */
public class ZooKeeperHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZooKeeperHelper.class);

    private String zookeeperHost = "127.0.0.1:2181";

    private ZooKeeper zk;

    public void connect() {
        try {
            zk = new ZooKeeper(zookeeperHost, 15000, watchedEvent -> {

                if (watchedEvent.getState() == Watcher.Event.KeeperState.Expired) {

                    LOGGER.info("session过期，重连");
                    destroy();
                    connect();
                    RegistryAgent.getInstance().registerProcessor();//重新注册服务

                } else if (Watcher.Event.KeeperState.SyncConnected == watchedEvent.getState()) {
                    LOGGER.info("ZookeeperHelper已连接到zookeeper Server");
                }
            });
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void destroy() {
        try {
            zk.close();
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void addOrUpdateServerInfo(String path, String data) throws KeeperException, InterruptedException {

        String[] paths = path.split("/");

        String createPath = "/";
        for (int i = 1; i < paths.length - 1; i++) {
            createPath += paths[i];
            addPersistServerNode(createPath, "");
            createPath += "/";
        }

        addServerInfo(path, data);

//        destroy();
//        LOGGER.info("到zookeeper的连接已关闭");

    }

    /**
     * 添加持久化的节点，节点名为serviceName
     *
     * @param path
     * @param data
     */
    private void addPersistServerNode(String path, String data) {
        zk.create(path, data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT, serverNameCreateCb, data);
    }

    /**
     * 异步添加serverName节点的回调处理
     */
    private AsyncCallback.StringCallback serverNameCreateCb = (rc, path, ctx, name) -> {
        switch (KeeperException.Code.get(rc)) {
            case CONNECTIONLOSS:
                LOGGER.info("添加节点, path:" + path + "连接断开，重新添加");
                addPersistServerNode(path, (String) ctx);
                break;
            case OK:
                LOGGER.info("添加节点成功，path:" + path + "  值为：" + ((String) ctx));
                break;
            case NODEEXISTS:
                LOGGER.info("添加节点, path:" + path + "已存在，更新");
                updateServerInfo(path, (String) ctx);
                break;
            default:
                LOGGER.info("Something went wrong when creating server info");
        }
    };


    /**
     * 异步添加serverInfo,为临时节点，如果server挂了就木有了
     */
    public void addServerInfo(String path, String data) {
        zk.create(path, data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL, serverAddrCreateCb, data);
    }

    /**
     * 异步添加serverInfo的回调处理
     */
    private AsyncCallback.StringCallback serverAddrCreateCb = (rc, path, ctx, name) -> {
        switch (KeeperException.Code.get(rc)) {
            case CONNECTIONLOSS:
                LOGGER.info("添加server info, path:" + path + "连接断开，重新添加");
                addServerInfo(path, (String) ctx);
                break;
            case OK:
                LOGGER.info("添加server info成功，path:" + path + "  值为：" + ((String) ctx));
                break;
            case NODEEXISTS:
                LOGGER.info("添加server info, path:" + path + "已存在，更新");
                updateServerInfo(path, (String) ctx);
                break;
            default:
                LOGGER.info("Something went wrong when creating server info");
        }
    };

    /**
     * 异步更新serverInfo
     */
    public void updateServerInfo(String path, String data) {
        zk.setData(path, data.getBytes(), -1, serverAddrUpdateCb, data);
    }

    /**
     * 异步更新serverInfo的回调
     */
    private AsyncCallback.StatCallback serverAddrUpdateCb = (rc, path1, ctx, stat) -> {
        switch (KeeperException.Code.get(rc)) {
            case CONNECTIONLOSS:
                updateServerInfo(path1, (String) ctx);
                return;
        }
    };

    public void setZookeeperHost(String zookeeperHost) {
        this.zookeeperHost = zookeeperHost;
    }
}
