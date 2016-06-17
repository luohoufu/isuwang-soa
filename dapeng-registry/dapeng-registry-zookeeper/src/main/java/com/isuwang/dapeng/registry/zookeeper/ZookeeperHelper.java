package com.isuwang.dapeng.registry.zookeeper;

import com.isuwang.dapeng.registry.RegistryAgent;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by tangliu on 2016/2/29.
 */
public class ZookeeperHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperHelper.class);

    private String zookeeperHost = "127.0.0.1:2181";

    private ZooKeeper zk;
    private RegistryAgent registryAgent;

    public ZookeeperHelper(RegistryAgent registryAgent) {
        this.registryAgent = registryAgent;
    }

    public void connect() {
        try {
            zk = new ZooKeeper(zookeeperHost, 15000, watchedEvent -> {
                if (watchedEvent.getState() == Watcher.Event.KeeperState.Expired) {
                    LOGGER.info("Registry {} Session过期,重连 [Zookeeper]", zookeeperHost);
                    destroy();
                    connect();

                    registryAgent.registerAllServices();//重新注册服务
                } else if (Watcher.Event.KeeperState.SyncConnected == watchedEvent.getState()) {
                    LOGGER.info("Registry {} [Zookeeper]", zookeeperHost);
                }
            });
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void destroy() {
        if(zk != null){
            try {
                zk.close();
                zk = null;
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

    }

    public void addOrUpdateServerInfo(String path, String data) {
        String[] paths = path.split("/");

        String createPath = "/";
        for (int i = 1; i < paths.length - 1; i++) {
            createPath += paths[i];
            addPersistServerNode(createPath, "");
            createPath += "/";
        }

        addServerInfo(path, data);
    }

    /**
     * 添加持久化的节点
     *
     * @param path
     * @param data
     */
    private void addPersistServerNode(String path, String data) {
        Stat stat = exists(path);

        if (stat == null)
            zk.create(path, data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT, persistNodeCreateCb, data);
//        else
//            try {
//                zk.setData(path, data.getBytes(), -1);
//            } catch (KeeperException e) {
//            } catch (InterruptedException e) {
//            }
    }

    private Stat exists(String path) {
        Stat stat = null;
        try {
            stat = zk.exists(path, false);
        } catch (KeeperException e) {
        } catch (InterruptedException e) {
        }
        return stat;
    }

    /**
     * 添加持久化节点回调方法
     */
    private AsyncCallback.StringCallback persistNodeCreateCb = (rc, path, ctx, name) -> {
        switch (KeeperException.Code.get(rc)) {
            case CONNECTIONLOSS:
                LOGGER.info("创建节点:{},连接断开，重新创建", path);
                addPersistServerNode(path, (String) ctx);
                break;
            case OK:
                LOGGER.info("创建节点:{},成功", path);
                break;
            case NODEEXISTS:
                LOGGER.info("创建节点:{},已存在", path);
                updateServerInfo(path, (String) ctx);
                break;
            default:
                LOGGER.info("创建节点:{},失败", path);
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
                LOGGER.info("添加serviceInfo:{},连接断开，重新添加", path);
                addOrUpdateServerInfo(path, (String) ctx);
                break;
            case OK:
                LOGGER.info("添加serviceInfo:{},成功", path);
                break;
            case NODEEXISTS:
                LOGGER.info("添加serviceInfo:{},已存在，删掉后重新添加", path);
                try {
                    zk.delete(path, -1);
                } catch (Exception e) {
                    LOGGER.error("删除serviceInfo:{} 失败:{}", path, e.getMessage());
                }
                addOrUpdateServerInfo(path, (String) ctx);
                break;
            default:
                LOGGER.info("添加serviceInfo:{}，出错", path);
        }
    };

    /**
     * 异步更新节点信息
     */
    public void updateServerInfo(String path, String data) {
        zk.setData(path, data.getBytes(), -1, serverAddrUpdateCb, data);
    }

    /**
     * 异步更新节点信息的回调方法
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
