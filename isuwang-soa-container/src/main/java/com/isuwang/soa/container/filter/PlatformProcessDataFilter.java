package com.isuwang.soa.container.filter;

import com.isuwang.soa.core.Context;
import com.isuwang.soa.core.IPUtils;
import com.isuwang.soa.core.SoaHeader;
import com.isuwang.soa.core.SoaSystemEnvProperties;
import com.isuwang.soa.core.filter.FilterChain;
import com.isuwang.soa.monitor.api.MonitorServiceClient;
import com.isuwang.soa.monitor.api.domain.PlatformProcessData;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by tangliu on 2016/3/9.
 */
public class PlatformProcessDataFilter implements StatusFilter {

    private final long period = 3 * 60 * 1000L;
    private final AtomicInteger callCount = new AtomicInteger(0);
    private final Timer timer = new Timer("PlatformProcessDataFilter-Timer");
    private static final Logger LOGGER = LoggerFactory.getLogger(PlatformProcessDataFilter.class);


    public static Map<String, PlatformProcessData> processDataMap = new ConcurrentHashMap<>();

    @Override
    public void init() {

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.MINUTE, 1);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    List<PlatformProcessData> dataList = new ArrayList<>(processDataMap.values());
                    new MonitorServiceClient().uploadPlatformProcessData(dataList);

                } catch (Exception e) {
                    LOGGER.error(e.getMessage());
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

        data.setTotalCalls(data.getTotalCalls() + 1);

        chain.doFilter();
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

            processDataMap.put(key, data);
        }
        return processDataMap.get(key);
    }

}
