package com.isuwang.soa.container.filter;

import com.isuwang.soa.core.Context;
import com.isuwang.soa.core.IPUtils;
import com.isuwang.soa.core.SoaHeader;
import com.isuwang.soa.core.SoaSystemEnvProperties;
import com.isuwang.soa.core.filter.FilterChain;
import com.isuwang.soa.core.filter.container.ContainerFilterChain;
import com.isuwang.soa.monitor.api.MonitorServiceClient;
import com.isuwang.soa.monitor.api.domain.PlatformProcessData;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by tangliu on 2016/3/9.
 */
public class PlatformProcessDataFilter implements StatusFilter {

    private final static long period = 3 * 60 * 1000L;
    private final Timer timer = new Timer("PlatformProcessDataFilter-Timer");
    private static final Logger LOGGER = LoggerFactory.getLogger(PlatformProcessDataFilter.class);


    public static Map<String, PlatformProcessData> processDataMap = new ConcurrentHashMap<>();

    @Override
    public void init() {

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.MINUTE, 3);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    List<PlatformProcessData> dataList = new ArrayList<>(processDataMap.values());
                    for (PlatformProcessData data : dataList) {
                        data.setAnalysisTime(System.currentTimeMillis());
                        data.setIAverageTime(data.getITotalTime() / data.getTotalCalls());
                        data.setPAverageTime(data.getPTotalTime() / data.getTotalCalls());
                    }
                    if (dataList.size() > 0)
                        new MonitorServiceClient().uploadPlatformProcessData(dataList);

                } catch (Exception e) {
                    LOGGER.error(e.getMessage());
                } finally {
                    processDataMap.clear();
                }
            }
        }, calendar.getTime(), period);

    }

    @Override
    public void destory() {
        processDataMap.clear();
        timer.cancel();
    }

    @Override
    public void doFilter(FilterChain chain) throws TException {

        SoaHeader soaHeader = Context.Factory.getCurrentInstance().getHeader();
        PlatformProcessData data = getPlatformPorcessData(soaHeader);

        try {
            chain.doFilter();
        } finally {

            Long iProcessTime = (Long) chain.getAttribute(ContainerFilterChain.ATTR_KEY_I_PROCESSTIME);
            if (iProcessTime != null) {
                data.setIMaxTime(data.getIMaxTime() > iProcessTime ? data.getIMaxTime() : iProcessTime);
                data.setIMinTime(data.getIMinTime() < iProcessTime ? data.getIMinTime() : iProcessTime);
                data.setITotalTime(data.getITotalTime() + iProcessTime);
            }
            data.setTotalCalls(data.getTotalCalls() + 1);
        }


    }

    public static String generateKey(SoaHeader soaHeader) {
        return soaHeader.getServiceName() + ":" + soaHeader.getMethodName() + ":" + soaHeader.getVersionName();
    }


    public static PlatformProcessData getPlatformPorcessData(SoaHeader soaHeader) {

        String key = generateKey(soaHeader);
        if (!processDataMap.containsKey(key)) {
            PlatformProcessData data = new PlatformProcessData();
            data.setServiceName(soaHeader.getServiceName());
            data.setMethodName(soaHeader.getMethodName());
            data.setVersionName(soaHeader.getVersionName());
            data.setServerIP(IPUtils.localIp());
            data.setServerPort(SoaSystemEnvProperties.SOA_CONTAINER_PORT);

            data.setPeriod((int) period / 1000 / 60);

            data.setIAverageTime(0L);
            data.setIMaxTime(0L);
            data.setIMinTime(1000000L);
            data.setITotalTime(0L);

            data.setPAverageTime(0L);
            data.setPMaxTime(0L);
            data.setPMinTime(1000000L);
            data.setPTotalTime(0L);

            data.setRequestFlow(0);
            data.setResponseFlow(0);

            data.setTotalCalls(0);
            data.setSucceedCalls(0);
            data.setFailCalls(0);

            processDataMap.put(key, data);
        }
        return processDataMap.get(key);
    }

}
