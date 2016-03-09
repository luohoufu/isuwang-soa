package com.isuwang.soa.container.filter;

import com.isuwang.soa.core.Context;
import com.isuwang.soa.core.IPUtils;
import com.isuwang.soa.core.SoaHeader;
import com.isuwang.soa.core.SoaSystemEnvProperties;
import com.isuwang.soa.core.filter.FilterChain;
import com.isuwang.soa.core.filter.container.ContainerFilterChain;
import com.isuwang.soa.monitor.api.MonitorServiceClient;
import com.isuwang.soa.monitor.api.domain.PlatformProcessData;
import com.isuwang.soa.monitor.api.domain.PlatformProcessDataAtomic;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by tangliu on 2016/3/9.
 */
public class PlatformProcessDataFilter implements StatusFilter {

    private final static long period = 3 * 60 * 1000L;
    private final Timer timer = new Timer("PlatformProcessDataFilter-Timer");
    private static final Logger LOGGER = LoggerFactory.getLogger(PlatformProcessDataFilter.class);


    public static Map<String, PlatformProcessDataAtomic> processDataMap = new ConcurrentHashMap<>();

    public static Boolean lock = new Boolean(true);

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
                    List<PlatformProcessDataAtomic> atomicDataList = new ArrayList<>(processDataMap.values());
                    List<PlatformProcessData> platformProcessDataList = new ArrayList<>();

                    for (PlatformProcessDataAtomic atomicData : atomicDataList) {

                        PlatformProcessData data = new PlatformProcessData();

                        copyValue(data, atomicData);

                        data.setAnalysisTime(System.currentTimeMillis());
                        data.setIAverageTime(data.getITotalTime() / data.getTotalCalls());
                        data.setPAverageTime(data.getPTotalTime() / data.getTotalCalls());

                        platformProcessDataList.add(data);
                    }
                    if (platformProcessDataList.size() > 0)
                        new MonitorServiceClient().uploadPlatformProcessData(platformProcessDataList);

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
        PlatformProcessDataAtomic data = getPlatformPorcessData(soaHeader);

        try {
            chain.doFilter();
        } finally {

            Long iProcessTime = (Long) chain.getAttribute(ContainerFilterChain.ATTR_KEY_I_PROCESSTIME);
            if (iProcessTime != null) {

                data.getiMaxTime().set(data.getiMaxTime().get() > iProcessTime ? data.getiMaxTime().get() : iProcessTime);
                data.getiMinTime().set(data.getiMinTime().get() < iProcessTime ? data.getiMinTime().get() : iProcessTime);
                data.getiTotalTime().addAndGet(iProcessTime);
            }
            data.getTotalCalls().incrementAndGet();
        }


    }

    public static String generateKey(SoaHeader soaHeader) {
        return soaHeader.getServiceName() + ":" + soaHeader.getMethodName() + ":" + soaHeader.getVersionName();
    }


    public static PlatformProcessDataAtomic getPlatformPorcessData(SoaHeader soaHeader) {

        synchronized (lock) {

            String key = generateKey(soaHeader);
            if (!processDataMap.containsKey(key)) {
                PlatformProcessDataAtomic data = new PlatformProcessDataAtomic();
                data.setServiceName(soaHeader.getServiceName());
                data.setMethodName(soaHeader.getMethodName());
                data.setVersionName(soaHeader.getVersionName());
                data.setServerIP(IPUtils.localIp());
                data.setServerPort(SoaSystemEnvProperties.SOA_CONTAINER_PORT);

                data.setPeriod((int) period / 1000 / 60);

                data.setiAverageTime(0L);
                data.setiMaxTime(new AtomicLong(0L));
                data.setiMinTime(new AtomicLong(1000000L));
                data.setiTotalTime(new AtomicLong(0L));

                data.setpAverageTime(0L);
                data.setpMaxTime(new AtomicLong(0L));
                data.setpMinTime(new AtomicLong(1000000L));
                data.setpTotalTime(new AtomicLong(0L));

                data.setRequestFlow(new AtomicInteger(0));
                data.setResponseFlow(new AtomicInteger(0));

                data.setTotalCalls(new AtomicInteger(0));
                data.setSucceedCalls(new AtomicInteger(0));
                data.setFailCalls(new AtomicInteger(0));

                processDataMap.put(key, data);
            }
            return processDataMap.get(key);
        }
    }


    private void copyValue(PlatformProcessData data, PlatformProcessDataAtomic atomic) {

        data.setServiceName(atomic.getServiceName());
        data.setMethodName(atomic.getMethodName());
        data.setVersionName(atomic.getVersionName());
        data.setServerIP(atomic.getServerIP());
        data.setServerPort(atomic.getServerPort());

        data.setPeriod(atomic.getPeriod());

        data.setIAverageTime(0L);
        data.setIMaxTime(atomic.getiMaxTime().get());
        data.setIMinTime(atomic.getiMinTime().get());
        data.setITotalTime(atomic.getiTotalTime().get());

        data.setPAverageTime(0L);
        data.setPMaxTime(atomic.getpMaxTime().get());
        data.setPMinTime(atomic.getpMinTime().get());
        data.setPTotalTime(atomic.getpTotalTime().get());

        data.setRequestFlow(atomic.getRequestFlow().get());
        data.setResponseFlow(atomic.getResponseFlow().get());

        data.setTotalCalls(atomic.getTotalCalls().get());
        data.setSucceedCalls(atomic.getSucceedCalls().get());
        data.setFailCalls(atomic.getFailCalls().get());

    }

}
