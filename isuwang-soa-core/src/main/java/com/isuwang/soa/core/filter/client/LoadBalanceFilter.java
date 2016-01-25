package com.isuwang.soa.core.filter.client;

import com.isuwang.soa.core.Context;
import com.isuwang.soa.core.SoaHeader;
import com.isuwang.soa.core.SoaSystemEnvProperties;
import com.isuwang.soa.core.filter.Filter;
import com.isuwang.soa.core.filter.FilterChain;
import com.isuwang.soa.core.registry.ServiceInfo;
import com.isuwang.soa.core.registry.ServiceInfoWatcher;
import org.apache.commons.lang3.StringUtils;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by tangliu on 2016/1/15.
 */
public class LoadBalanceFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoadBalanceFilter.class);

    private static int roundRobinIndex = 8;

    @Override
    public void init() {

    }

    @Override
    public void doFilter(FilterChain chain) throws TException {
        final Context context = (Context) chain.getAttribute(StubFilterChain.ATTR_KEY_CONTEXT);
        final SoaHeader soaHeader = (SoaHeader) chain.getAttribute(StubFilterChain.ATTR_KEY_HEADER);

        String callerInfo = null;

        List<ServiceInfo> usableList = ServiceInfoWatcher.getServiceInfo(soaHeader.getServiceName(), soaHeader.getVersionName());

        //TODO load static config in zookeeper
        LoadBalanceStratage balance = LoadBalanceStratage.Random;

        switch (balance) {
            case Random:
                callerInfo = random(callerInfo, usableList, chain);
                break;
            case RoundRobin:
                callerInfo = roundRobin(callerInfo, usableList, chain);
                break;
            case LeastActive:
                callerInfo = leastActive(callerInfo, usableList, chain);
                break;
            case ConsistentHash:
                break;
        }

        if (callerInfo != null) {
            LOGGER.info("{} {} {} zookeeper:{}", soaHeader.getServiceName(), soaHeader.getVersionName(), soaHeader.getMethodName(), callerInfo);

            String[] infos = callerInfo.split(":");
            context.setCalleeIp(infos[0]);
            context.setCalleePort(Integer.valueOf(infos[1]));
        } else if (StringUtils.isNotBlank(SoaSystemEnvProperties.SOA_SERVICE_IP) && SoaSystemEnvProperties.SOA_SERVICE_PORT != null) {
            context.setCalleeIp(SoaSystemEnvProperties.SOA_SERVICE_IP);
            context.setCalleePort(SoaSystemEnvProperties.SOA_SERVICE_PORT);
        }

        chain.doFilter();
    }

    private String random(String callerInfo, List<ServiceInfo> usableList, FilterChain chain) {
        //随机选择一个可用server
        if (usableList.size() > 0) {
            final Random random = new Random();

            final int index = random.nextInt(usableList.size());

            ServiceInfo serviceInfo = usableList.get(index);

            serviceInfo.getActiveCount().incrementAndGet();
            chain.setAttribute(StubFilterChain.ATTR_KEY_SERVERINFO, serviceInfo);
            LOGGER.info(serviceInfo.getHost() + ":" + serviceInfo.getPort() + " concurrency：" + serviceInfo.getActiveCount());

            callerInfo = serviceInfo.getHost() + ":" + String.valueOf(serviceInfo.getPort());
        }
        return callerInfo;
    }

    private String leastActive(String callerInfo, List<ServiceInfo> usableList, FilterChain chain) {

        if (usableList.size() > 0) {

            AtomicInteger count = usableList.get(0).getActiveCount();
            int index = 0;

            for (int i = 1; i < usableList.size(); i++) {
                if (usableList.get(i).getActiveCount().intValue() < count.intValue()) {
                    index = i;
                }
            }

            ServiceInfo serviceInfo = usableList.get(index);
            serviceInfo.getActiveCount().incrementAndGet();
            chain.setAttribute(StubFilterChain.ATTR_KEY_SERVERINFO, serviceInfo);

            callerInfo = serviceInfo.getHost() + ":" + String.valueOf(serviceInfo.getPort());
        }
        return callerInfo;
    }

    private String roundRobin(String callerInfo, List<ServiceInfo> usableList, FilterChain chain) {

        if (usableList.size() > 0) {
            roundRobinIndex = (roundRobinIndex++) % usableList.size();
            ServiceInfo serviceInfo = usableList.get(roundRobinIndex);
            serviceInfo.getActiveCount().incrementAndGet();
            chain.setAttribute(StubFilterChain.ATTR_KEY_SERVERINFO, serviceInfo);

            callerInfo = serviceInfo.getHost() + ":" + String.valueOf(serviceInfo.getPort());
        }
        return callerInfo;
    }

    @Override
    public void destory() {

    }

}
