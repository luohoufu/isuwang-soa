package com.isuwang.soa.registry.zookeeper;

import com.isuwang.soa.core.SoaSystemEnvProperties;
import com.isuwang.soa.registry.ConfigKey;
import com.isuwang.soa.registry.ServiceInfo;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by tangliu on 2016/2/29.
 */
public class ZookeeperWatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperWatcher.class);

    private final boolean isClient;
    private final Map<String, List<ServiceInfo>> caches = new ConcurrentHashMap<>();
    private final Map<String, Map<ConfigKey, Object>> config = new ConcurrentHashMap<>();

    private ZooKeeper zk;

    public AtomicBoolean serviceListInitialized = new AtomicBoolean(false);

    public ZookeeperWatcher(boolean isClient) {
        this.isClient = isClient;
    }

    public void init() {
        connect();

        if (isClient) {
            getServersList();
        }

        getConfig("/soa/config");
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

    public List<ServiceInfo> getServiceInfo(String serviceName, String versionName) {
        synchronized (serviceListInitialized) {
            if (!serviceListInitialized.get()) {
                try {
                    serviceListInitialized.wait(10000L);
                } catch (InterruptedException e) {
                }
            }
        }

        List<ServiceInfo> serverList = caches.get(serviceName);
        List<ServiceInfo> usableList = new ArrayList<>();
        if (serverList != null && serverList.size() > 0) {
            usableList.addAll(serverList.stream().filter(server -> server.getVersionName().equals(versionName)).collect(Collectors.toList()));
        }
        return usableList;
    }

    /*
    private void createServicesNode() {
        zk.create("/soa", new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT, servicesNodeCreateCallBack, null);
        zk.create("/soa/runtime", new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT, servicesNodeCreateCallBack, null);
        zk.create("/soa/runtime/services", new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT, servicesNodeCreateCallBack, null);
    }

    private AsyncCallback.StringCallback servicesNodeCreateCallBack = (rc, path, ctx, name) -> {
        switch (KeeperException.Code.get(rc)) {
            case CONNECTIONLOSS:
                LOGGER.info("创建" + path + "节点，连接断开，重新添加");
                createServicesNode();
                break;
            case OK:
                LOGGER.info("创建" + path + "节点成功");
                break;
            case NODEEXISTS:
                LOGGER.info(path + "节点已存在");
                break;
            default:
                LOGGER.info("Something went wrong when creating server info");
        }
    };
    */

    //----------------------servicesList相关-----------------------------------

    /**
     * 获取zookeeper中的services节点的子节点，并设置监听器
     *
     * @return
     */
    public void getServersList() {
        zk.getChildren("/soa/runtime/services", watchedEvent -> {
            //Children发生变化，则重新获取最新的services列表
            if (watchedEvent.getType() == Watcher.Event.EventType.NodeChildrenChanged) {
                LOGGER.info("{}子节点发生变化，重新获取子节点...", watchedEvent.getPath());

                getServersList();
            }
        }, (rc, path, ctx, children) -> {
            switch (KeeperException.Code.get(rc)) {
                case CONNECTIONLOSS:
                    getServersList();

                    break;
                case OK:
                    LOGGER.info("获取services列表成功");

                    resetServiceCaches(path, children);
                    break;
                default:
                    LOGGER.error("get services list fail");
            }
        }, null);
    }

    //----------------------servicesList相关-----------------------------------


    //----------------------serviceInfo相关-----------------------------------

    private AtomicInteger serviceCounts = new AtomicInteger(0);

    /**
     * 对每一个serviceName,要获取serviceName下的子节点
     *
     * @param path
     * @param serviceList
     */
    private void resetServiceCaches(String path, List<String> serviceList) {
        serviceCounts.set(serviceList.size());

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
        zk.getChildren(servicePath, watchedEvent -> {
            if (watchedEvent.getType() == Watcher.Event.EventType.NodeChildrenChanged) {
                LOGGER.info("{}子节点发生变化，重新获取信息", watchedEvent.getPath());

                String[] paths = watchedEvent.getPath().split("/");
                getServiceInfoByPath(watchedEvent.getPath(), paths[paths.length - 1]);
            }
        }, (rc, path, ctx, children) -> {
            switch (KeeperException.Code.get(rc)) {
                case CONNECTIONLOSS:
                    getServiceInfoByPath(path, (String) ctx);
                    break;
                case OK:
                    LOGGER.info("获取" + path + "的子节点成功");

                    resetServiceInfoByName((String) ctx, path, children);
                    break;
                default:
                    LOGGER.error("get services list fail");
            }
        }, serviceName);
    }

    /**
     * serviceName下子节点列表即可用服务地址列表
     * 子节点命名为：host:port:versionName
     *
     * @param serviceName
     * @param path
     * @param infos
     */
    private void resetServiceInfoByName(String serviceName, String path, List<String> infos) {
        LOGGER.info(serviceName + "\n" + infos);

        List<ServiceInfo> sinfos = new ArrayList<>();

        for (String info : infos) {
            String[] serviceInfo = info.split(":");
            ServiceInfo sinfo = new ServiceInfo(serviceInfo[0], Integer.valueOf(serviceInfo[1]), serviceInfo[2]);
            sinfos.add(sinfo);
        }

        if (caches.containsKey(serviceName)) {
            List<ServiceInfo> currentInfos = caches.get(serviceName);

            for (ServiceInfo sinfo : sinfos) {
                for (ServiceInfo currentSinfo : currentInfos) {
                    if (sinfo.equalTo(currentSinfo)) {
                        sinfo.setActiveCount(currentSinfo.getActiveCount());
                        break;
                    }
                }
            }
        }
        caches.put(serviceName, sinfos);

        if (serviceCounts.decrementAndGet() <= 0) {
            synchronized (serviceListInitialized) {
                serviceListInitialized.set(true);

                serviceListInitialized.notifyAll();
            }
        }
    }
    //----------------------servicesInfo相关-----------------------------------

    //----------------------static config-------------------------------------
    private void getConfig(String path) {
        zk.getChildren(path, watchedEvent -> {
            if (watchedEvent.getType() == Watcher.Event.EventType.NodeChildrenChanged) {
                LOGGER.info(watchedEvent.getPath() + "'s children changed, reset config in memory");

                getConfig(watchedEvent.getPath());
            }
        }, (rc, path1, ctx, children) -> {
            switch (KeeperException.Code.get(rc)) {
                case CONNECTIONLOSS:
                    LOGGER.info("connect loss, reset {} config in memory", path1);

                    getConfig(path1);
                    break;
                case OK:
                    LOGGER.info("get children of {} succeed.", path1);

                    resetConfigCache(path1, children);

                    break;
                default:
                    LOGGER.error("get chileren of {} failed", path1);
            }
        }, null);
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

        zk.getData(path, watchedEvent -> {
            if (watchedEvent.getType() == Watcher.Event.EventType.NodeDataChanged) {
                LOGGER.info(watchedEvent.getPath() + "'s data changed, reset config in memory");
                getConfigData(watchedEvent.getPath(), null);
            }
        }, (rc, path1, ctx, data, stat) -> {
            switch (KeeperException.Code.get(rc)) {
                case CONNECTIONLOSS:
                    getConfigData(path1, (String) ctx);
                    break;
                case OK:
                    processConfigData((String) ctx, data);
                    break;
                default:
                    LOGGER.error("Error when trying to get data of {}.", path1);
            }
        }, configNodeName);
    }

    private void processConfigData(String configNode, byte[] data) {
        Map<ConfigKey, Object> propertyMap = new HashMap<>();
        try {
            String propertiesStr = new String(data, "utf-8");

            String[] properties = propertiesStr.split(";");
            for (String property : properties) {

                String[] key_values = property.split("=");
                if (key_values.length == 2) {

                    ConfigKey type = ConfigKey.findByValue(key_values[0]);
                    switch (type) {

                        case Thread:
                            Integer value = Integer.valueOf(key_values[1]);
                            propertyMap.put(type, value);
                            break;
                        case ThreadPool:
                            Boolean bool = Boolean.valueOf(key_values[1]);
                            propertyMap.put(type, bool);
                            break;
                        case Timeout:
                            Integer timeout = Integer.valueOf(key_values[1]);
                            propertyMap.put(type, timeout);
                            break;
                        case LoadBalance:
                            propertyMap.put(type, key_values[1]);
                            break;
                        case FailOver:
                            propertyMap.put(type, Integer.valueOf(key_values[1]));
                            break;
                    }
                }
            }

            LOGGER.info("get config form {} with data [{}]", configNode, propertiesStr);
        } catch (UnsupportedEncodingException e) {
            LOGGER.error(e.getMessage(), e);
        }

        config.put(configNode, propertyMap);
    }

    //---------------------static config end-----------------------------------

    /**
     * 连接zookeeper
     */
    private void connect() {
        try {
            zk = new ZooKeeper(SoaSystemEnvProperties.SOA_ZOOKEEPER_HOST, 15000, e -> {
                if (e.getState() == Watcher.Event.KeeperState.Expired) {
                    LOGGER.info("session过期，重连");

                    destroy();

                    init();
                } else if (e.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    LOGGER.info("已连接zookeeper Server");
                }
            });
        } catch (Exception e) {
            LOGGER.info(e.getMessage(), e);
        }
    }

    public Map<String, Map<ConfigKey, Object>> getConfig() {
        return config;
    }
}
