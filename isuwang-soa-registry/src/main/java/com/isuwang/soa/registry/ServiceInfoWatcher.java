package com.isuwang.soa.registry;

import com.isuwang.soa.core.SoaSystemEnvProperties;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by tangliu on 2016/1/15.
 */
public class ServiceInfoWatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceInfoWatcher.class);

    private static Map<String, List<ServiceInfo>> caches = new ConcurrentHashMap<>();

    private static Map<String, Map<ConfigKey, Object>> config = new ConcurrentHashMap<>();

    public static Map<String, Map<ConfigKey, Object>> getConfig() {
        return config;
    }

    private ZooKeeper zk;

    public void init() {
        connect();

        createServicesNode();

        getServersList();

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

        caches = null;
        config = null;

        LOGGER.info("关闭连接，清空service info caches");
    }

    public static List<ServiceInfo> getServiceInfo(String serviceName, String versionName) {
        List<ServiceInfo> serverList = caches.get(serviceName);
        List<ServiceInfo> usableList = new ArrayList<>();
        if (serverList != null && serverList.size() > 0) {
            usableList.addAll(serverList.stream().filter(server -> server.getVersionName().equals(versionName)).collect(Collectors.toList()));
        }
        return usableList;
    }

    /**
     * 创建/service持久节点
     */
    private void createServicesNode() {
        zk.create("/soa", new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT, servicesNodeCreateCallBack, null);
        zk.create("/soa/runtime", new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT, servicesNodeCreateCallBack, null);
        zk.create("/soa/runtime/services", new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT, servicesNodeCreateCallBack, null);
    }

    /**
     * 添加主节点回调
     */
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

    //----------------------servicesList相关-----------------------------------

    /**
     * 获取zookeeper中的services节点的子节点，并设置监听器
     *
     * @return
     */
    public void getServersList() {

        zk.getChildren("/soa/runtime/services", servicesListChangeWatcher, getServicesListCallback, null);

    }

    private AsyncCallback.ChildrenCallback getServicesListCallback = (i, path, ctx, children) -> {
        switch (KeeperException.Code.get(i)) {
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
    };
    /**
     * 监听services列表变化
     */
    private Watcher servicesListChangeWatcher = watchedEvent -> {

        //Children发生变化，则重新获取最新的services列表
        if (watchedEvent.getType() == Watcher.Event.EventType.NodeChildrenChanged) {
            LOGGER.info(watchedEvent.getPath() + "子节点发生变化，重新获取子节点...");
            getServersList();
        }
    };

    //----------------------servicesList相关-----------------------------------


    //----------------------serviceInfo相关-----------------------------------

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
        zk.getChildren(servicePath, serviceInfoChangeWatcher, getServiceInfoCallback, serviceName);
    }

    /**
     * 监听serviceName节点的子节点变化
     */
    private Watcher serviceInfoChangeWatcher = watchedEvent -> {

        if (watchedEvent.getType() == Watcher.Event.EventType.NodeChildrenChanged) {
            LOGGER.info(watchedEvent.getPath() + "子节点发生变化，重新获取信息");

            String[] paths = watchedEvent.getPath().split("/");
            getServiceInfoByPath(watchedEvent.getPath(), paths[paths.length - 1]);
        }
    };

    /**
     *
     */
    private AsyncCallback.ChildrenCallback getServiceInfoCallback = (i, path, ctx, children) -> {
        switch (KeeperException.Code.get(i)) {
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
    };


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
    }
    //----------------------servicesInfo相关-----------------------------------

    //----------------------static config-------------------------------------
    private void getConfig(String path) {

        zk.getChildren(path, configChildrenChangeWatcher, getConfigChildrenCallBack, null);
    }

    private Watcher configChildrenChangeWatcher = watchedEvent -> {

        if (watchedEvent.getType() == Watcher.Event.EventType.NodeChildrenChanged) {
            LOGGER.info(watchedEvent.getPath() + "'s children changed, reset config in memory");
            getConfig(watchedEvent.getPath());
        }
    };

    private AsyncCallback.ChildrenCallback getConfigChildrenCallBack = (i, path, ctx, children) -> {
        switch (KeeperException.Code.get(i)) {
            case CONNECTIONLOSS:
                getConfig(path);
                break;
            case OK:
                LOGGER.info("get children of {} succeed.", path);
                resetConfigCache(path, children);
                break;
            default:
                LOGGER.error("get chileren of {} failed", path);
        }
    };

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
        zk.getData(path, configDataChangeWatcher, getConfigDataCallback, configNodeName);
    }

    private Watcher configDataChangeWatcher = watchedEvent -> {

        if (watchedEvent.getType() == Watcher.Event.EventType.NodeDataChanged) {
            LOGGER.info(watchedEvent.getPath() + "'s data changed, reset config in memory");
            getConfigData(watchedEvent.getPath(), null);
        }
    };

    private AsyncCallback.DataCallback getConfigDataCallback = new AsyncCallback.DataCallback() {
        @Override
        public void processResult(int rc, String path, Object ctx, byte[] data, Stat stat) {
            switch (KeeperException.Code.get(rc)) {
                case CONNECTIONLOSS:
                    getConfigData(path, (String) ctx);
                    break;
                case OK:
                    processConfigData((String) ctx, data);
                    break;
                default:
                    LOGGER.error("Error when trying to get data of {}.", path);
            }
        }
    };

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
                    }
                }
            }

            LOGGER.info("get config form {} with data [{}]", configNode, propertiesStr);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws InterruptedException {

        ServiceInfoWatcher siw = new ServiceInfoWatcher();
        siw.init();

        Thread.sleep(Long.MAX_VALUE);
        siw.destroy();
    }
}
