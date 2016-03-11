package com.isuwang.soa.remoting.filter;

import com.isuwang.soa.core.Context;
import com.isuwang.soa.core.SoaHeader;
import com.isuwang.soa.core.SoaSystemEnvProperties;
import com.isuwang.soa.core.filter.Filter;
import com.isuwang.soa.core.filter.FilterChain;
import com.isuwang.soa.registry.ConfigKey;
import com.isuwang.soa.registry.RegistryAgentProxy;
import com.isuwang.soa.registry.ServiceInfo;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by tangliu on 2016/1/15.
 */
public class LoadBalanceFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoadBalanceFilter.class);

    private static AtomicInteger roundRobinIndex = new AtomicInteger(8);

    @Override
    public void doFilter(FilterChain chain) throws TException {
        final Context context = (Context) chain.getAttribute(StubFilterChain.ATTR_KEY_CONTEXT);
        final SoaHeader soaHeader = (SoaHeader) chain.getAttribute(StubFilterChain.ATTR_KEY_HEADER);
        final boolean isLocal = SoaSystemEnvProperties.SOA_REMOTING_MODE.equals("local");

        String callerInfo = null;

        List<ServiceInfo> usableList;
        if (isLocal)
            usableList = new ArrayList<>();
        else
            usableList = RegistryAgentProxy.getCurrentInstance(RegistryAgentProxy.Type.Client).loadMatchedServices(soaHeader.getServiceName(), soaHeader.getVersionName());

        String serviceKey = soaHeader.getServiceName() + "." + soaHeader.getVersionName() + "." + soaHeader.getMethodName() + ".consumer";
        LoadBalanceStratage balance = getLoadBalanceStratage(serviceKey) == null ? LoadBalanceStratage.LeastActive : getLoadBalanceStratage(serviceKey);

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
        } else if (isLocal) {
            context.setCalleeIp(SoaSystemEnvProperties.SOA_SERVICE_IP);
            context.setCalleePort(SoaSystemEnvProperties.SOA_SERVICE_PORT);
        }

        chain.doFilter();
    }

    private LoadBalanceStratage getLoadBalanceStratage(String key) {
        Map<ConfigKey, Object> configs = RegistryAgentProxy.getCurrentInstance(RegistryAgentProxy.Type.Client).getConfig().get(key);
        if (null != configs) {
            return LoadBalanceStratage.findByValue((String) configs.get(ConfigKey.LoadBalance));
        }
        return null;
    }

    private String random(String callerInfo, List<ServiceInfo> usableList, FilterChain chain) {
        //随机选择一个可用server
        if (usableList.size() > 0) {
            final Random random = new Random();

            final int index = random.nextInt(usableList.size());

            ServiceInfo serviceInfo = usableList.get(index);
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
            chain.setAttribute(StubFilterChain.ATTR_KEY_SERVERINFO, serviceInfo);

            callerInfo = serviceInfo.getHost() + ":" + String.valueOf(serviceInfo.getPort());
        }
        return callerInfo;
    }

    private String roundRobin(String callerInfo, List<ServiceInfo> usableList, FilterChain chain) {

        if (usableList.size() > 0) {
            roundRobinIndex = new AtomicInteger(roundRobinIndex.incrementAndGet() % usableList.size());
            ServiceInfo serviceInfo = usableList.get(roundRobinIndex.get());
            chain.setAttribute(StubFilterChain.ATTR_KEY_SERVERINFO, serviceInfo);

            callerInfo = serviceInfo.getHost() + ":" + String.valueOf(serviceInfo.getPort());
        }
        return callerInfo;
    }

}
