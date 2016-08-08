package com.isuwang.dapeng.registry.zookeeper;

import com.isuwang.dapeng.core.SoaSystemEnvProperties;
import com.isuwang.dapeng.core.version.Version;
import com.isuwang.dapeng.registry.ConfigKey;
import com.isuwang.dapeng.registry.ServiceInfo;
import com.isuwang.dapeng.route.Route;
import com.isuwang.dapeng.route.parse.RouteParser;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by tangliu on 2016/2/29.
 */
public class ZookeeperFallbackWatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperFallbackWatcher.class);

    private final static Map<String, List<ServiceInfo>> caches = new ConcurrentHashMap<>();
    private final static Map<String, Map<ConfigKey, Object>> config = new ConcurrentHashMap<>();
    private final static List<Route> routes = new ArrayList<>();

    private ZooKeeper zk;

    public void init() {

        connect();

        getServersList();

        getConfig("/soa/config/service");

        getRouteConfig("/soa/config/route");
    }


    /**
     * 获取路由配置
     *
     * @param path
     */
    private void getRouteConfig(String path) {

        tryCreateNode(path);

        try {
            byte[] data = zk.getData(path, watchedEvent -> {

                if (watchedEvent.getType() == Watcher.Event.EventType.NodeDataChanged) {
                    LOGGER.info(watchedEvent.getPath() + "'s data changed, reset route config in memory");
                    getRouteConfig(watchedEvent.getPath());
                } else if (watchedEvent.getType() == Watcher.Event.EventType.NodeDeleted) {
                    LOGGER.info(watchedEvent.getPath() + " is deleted, clear route config");
                    routes.clear();
                }
            }, null);

            processRouteDate(data);

        } catch (KeeperException e) {
            LOGGER.error("Error when trying to get data of {}.", path);
            LOGGER.error(e.getMessage(), e);
        } catch (InterruptedException e) {
            LOGGER.error("Error when trying to get data of {}.", path);
            LOGGER.error(e.getMessage(), e);
            getRouteConfig(path);
        }
    }


    /**
     * 拿到路由配置信息，解析成Routes列表
     *
     * @param bytes
     */
    private void processRouteDate(byte[] bytes) {

        try {
            String data = new String(bytes, "utf-8");

            if (data.trim().equals("") || data.equals("/soa/config/route")) {
                routes.clear();
                return;
            }
            synchronized (routes) {
                routes.clear();
                new RouteParser().parseAll(routes, data);
            }
        } catch (UnsupportedEncodingException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public List<Route> getRoutes() {
        return this.routes;
    }


    private void tryCreateNode(String path) {

        String[] paths = path.split("/");

        String createPath = "/";
        for (int i = 1; i < paths.length; i++) {
            createPath += paths[i];
            addPersistServerNode(createPath, path);
            createPath += "/";
        }
    }

    /**
     * 添加持久化的节点
     *
     * @param path
     * @param data
     */
    private void addPersistServerNode(String path, String data) {
        Stat stat = exists(path);

        try {
            if (stat == null) {
                zk.create(path, data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                LOGGER.info("创建节点{}成功", path);
            }
        } catch (KeeperException e) {
            LOGGER.error("创建节点{}失败", path);
            LOGGER.error(e.getMessage(), e);
        } catch (InterruptedException e) {
            LOGGER.info("创建节点{}失败", path);
            LOGGER.error(e.getMessage(), e);
            addPersistServerNode(path, data);
        }
    }

    /**
     * 判断节点是否存在
     *
     * @param path
     * @return
     */
    private Stat exists(String path) {
        Stat stat = null;
        try {
            stat = zk.exists(path, false);
        } catch (KeeperException e) {
        } catch (InterruptedException e) {
        }
        return stat;
    }

    public void destroy() {
        if (zk != null) {
            try {
                zk.close();
                zk = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        caches.clear();
        config.clear();

        LOGGER.info("关闭连接，清空service info caches");
    }

    public List<ServiceInfo> getServiceInfo(String serviceName, String versionName, boolean compatible) {

        List<ServiceInfo> serverList = caches.get(serviceName);
        List<ServiceInfo> usableList = new ArrayList<>();

        if (serverList != null && serverList.size() > 0) {

            if (!compatible) {
                usableList.addAll(serverList.stream().filter(server -> server.getVersionName().equals(versionName)).collect(Collectors.toList()));
            } else {
                usableList.addAll(serverList.stream().filter(server -> Version.toVersion(versionName).compatibleTo(Version.toVersion(server.getVersionName()))).collect(Collectors.toList()));
            }
        }
        return usableList;
    }


    /**
     * 获取zookeeper中的services节点的子节点，并设置监听器
     *
     * @return
     */
    public void getServersList() {

        tryCreateNode("/soa/runtime/services");

        try {
            List<String> children = zk.getChildren("/soa/runtime/services", event -> {
                if (event.getType() == Watcher.Event.EventType.NodeChildrenChanged) {
                    LOGGER.info("{}子节点发生变化，重新获取子节点...", event.getPath());
                    getServersList();
                }
            });

            resetServiceCaches("/soa/runtime/services", children);

        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 对每一个serviceName,要获取serviceName下的子节点
     *
     * @param path
     * @param serviceList
     */
    private void resetServiceCaches(String path, List<String> serviceList) {
        for (String serviceName : serviceList) {
            getServiceInfoByPath(path + "/" + serviceName, serviceName);
        }
    }

    /**
     * 根据serviceName节点的路径，获取下面的子节点，并监听子节点变化
     *
     * @param servicePath
     */
    private void getServiceInfoByPath(String servicePath, String serviceName) {

        try {
            List<String> children = zk.getChildren(servicePath, event -> {

                if (event.getType() == Watcher.Event.EventType.NodeChildrenChanged) {
                    LOGGER.info("{}子节点发生变化，重新获取信息", event.getPath());

                    String[] paths = event.getPath().split("/");
                    getServiceInfoByPath(event.getPath(), paths[paths.length - 1]);
                }
            });

            WatcherUtils.resetServiceInfoByName(serviceName, servicePath, children, caches);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
            getServiceInfoByPath(servicePath, serviceName);
        }
    }
    //----------------------servicesInfo相关-----------------------------------

    //----------------------static config-------------------------------------
    private void getConfig(String path) {


        //每次getConfig之前，先判断父节点是否存在，若不存在，则创建
        tryCreateNode("/soa/config/service");

        try {
            List<String> children = zk.getChildren(path, watchedEvent -> {
                if (watchedEvent.getType() == Watcher.Event.EventType.NodeChildrenChanged) {
                    LOGGER.info(watchedEvent.getPath() + "'s children changed, reset config in memory");
                    getConfig(watchedEvent.getPath());
                }
            });

            resetConfigCache(path, children);
        } catch (KeeperException e) {
            LOGGER.error("get children of node {} failed. ", path);
            LOGGER.error(e.getMessage(), e);
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
            getConfig(path);
        }
    }

    private void resetConfigCache(String path, List<String> children) {
        for (String key : children) {
            String configNodePath = path + "/" + key;
            getConfigData(configNodePath, key);
        }
    }

    private void getConfigData(String path, String configNodeName) {
        if (configNodeName == null) {
            String[] tmp = path.split("/");
            configNodeName = tmp[tmp.length - 1];
        }

        try {
            byte[] data = zk.getData(path, watchedEvent -> {
                if (watchedEvent.getType() == Watcher.Event.EventType.NodeDataChanged) {
                    LOGGER.info(watchedEvent.getPath() + "'s data changed, reset config in memory");
                    getConfigData(watchedEvent.getPath(), null);
                }
            }, null);

            WatcherUtils.processConfigData(configNodeName, data, config);
        } catch (KeeperException e) {
            LOGGER.error("Error when trying to get data of {}.", path);
            LOGGER.error(e.getMessage(), e);
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
            getConfigData(path, configNodeName);
        }
    }

    //---------------------static config end-----------------------------------

    /**
     * 连接zookeeper
     */
    private void connect() {
        try {
            zk = new ZooKeeper(SoaSystemEnvProperties.SOA_ZOOKEEPER_FALLBACK_HOST, 15000, e -> {
                if (e.getState() == Watcher.Event.KeeperState.Expired) {
                    LOGGER.info("ZookeeperFallbackWatcher到Zookeeper Server({})的session过期，重连", SoaSystemEnvProperties.SOA_ZOOKEEPER_FALLBACK_HOST);
                    destroy();
                    init();
                } else if (e.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    LOGGER.info("客户端ZookeeperFallbackWatcher 已连接 zookeeper Server ({})", SoaSystemEnvProperties.SOA_ZOOKEEPER_FALLBACK_HOST);
                }
            });
        } catch (Exception e) {
            LOGGER.info(e.getMessage(), e);
        }
    }

    public Map<String, Map<ConfigKey, Object>> getConfig() {
        return config;
    }


    public static void main(String[] args) {

        ZookeeperFallbackWatcher zw = new ZookeeperFallbackWatcher();
        zw.init();

        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
