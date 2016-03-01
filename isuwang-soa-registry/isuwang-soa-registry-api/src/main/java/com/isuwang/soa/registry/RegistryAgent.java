package com.isuwang.soa.registry;

import com.isuwang.soa.core.SoaBaseProcessor;

import java.util.List;
import java.util.Map;

/**
 * Registry Agent
 *
 * @author craneding
 * @date 16/3/1
 */
public interface RegistryAgent {

    void start();

    void stop();

    /**
     * 注册服务
     *
     * @param serverName  服务名
     * @param versionName 版本号
     */
    void registerService(String serverName, String versionName);

    /**
     * 注册服务集合
     */
    void registerAllServices();

    /**
     * 设置处理器集合
     *
     * @param processorMap 处理器集合
     */
    void setProcessorMap(Map<String, SoaBaseProcessor<?>> processorMap);

    /**
     * 加载匹配的服务
     *
     * @param serviceName 服务名称
     * @param methodName  方法名称
     * @return
     */
    List<ServiceInfo> loadMatchedServices(String serviceName, String methodName);

    /**
     * 获取配置
     *
     * @return
     */
    Map<String, Map<ConfigKey, Object>> getConfig();
}
