package com.isuwang.soa.container.util;

import com.isuwang.soa.core.IPUtils;
import com.isuwang.soa.core.SoaHeader;
import com.isuwang.soa.core.SoaSystemEnvProperties;
import com.isuwang.soa.monitor.api.domain.PlatformProcessData;

import java.util.HashMap;
import java.util.Map;

/**
 * Platform Process Data Factory
 *
 * @author craneding
 * @date 16/3/10
 */
public class PlatformProcessDataFactory {

    private static final Map<String, PlatformProcessData> dataMap = new HashMap<>();

    private static final ThreadLocal<PlatformProcessData> threadLocal = new ThreadLocal<>();

    public static PlatformProcessData getCurrentInstance() {
        return threadLocal.get();
    }

    public static PlatformProcessData getNewInstance(SoaHeader soaHeader) {
        PlatformProcessData processData = new PlatformProcessData();

        processData.setServiceName(soaHeader.getServiceName());
        processData.setMethodName(soaHeader.getMethodName());
        processData.setVersionName(soaHeader.getVersionName());
        processData.setServerIP(IPUtils.localIp());
        processData.setServerPort(SoaSystemEnvProperties.SOA_CONTAINER_PORT);
        processData.setIAverageTime(0l);
        processData.setIMaxTime(0l);
        processData.setIMinTime(0l);
        processData.setITotalTime(0l);
        processData.setPAverageTime(0l);
        processData.setPMaxTime(0l);
        processData.setPMinTime(0l);
        processData.setPTotalTime(0l);
        processData.setFailCalls(0);
        processData.setSucceedCalls(0);
        processData.setTotalCalls(0);
        processData.setRequestFlow(0);
        processData.setResponseFlow(0);

        return processData;
    }

    public static void setCurrentInstance(PlatformProcessData processData) {
        threadLocal.set(processData);
    }

    public static void removeCurrentInstance() {
        threadLocal.remove();
    }

    public static void update(SoaHeader soaHeader, Action action) {
        String key = generateKey(soaHeader);

        synchronized (dataMap) {
            PlatformProcessData cacheProcessData = dataMap.get(key);

            if (cacheProcessData == null) {
                dataMap.put(key, cacheProcessData = PlatformProcessDataFactory.getNewInstance(soaHeader));
            }

            action.doAction(cacheProcessData);
        }
    }

    private static String generateKey(SoaHeader soaHeader) {
        return soaHeader.getServiceName() + ":" + soaHeader.getMethodName() + ":" + soaHeader.getVersionName();
    }

    public static Map<String, PlatformProcessData> getDataMap() {
        return dataMap;
    }

    public static void clearDataMap() {
        synchronized (dataMap) {
            dataMap.clear();
        }
    }

    public interface Action {
        void doAction(PlatformProcessData processData);
    }
}
