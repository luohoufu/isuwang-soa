package com.isuwang.soa.container.filter;

import com.isuwang.soa.core.IPUtils;
import com.isuwang.soa.core.SoaHeader;
import com.isuwang.soa.core.SoaSystemEnvProperties;
import com.isuwang.soa.core.TransactionContext;
import com.isuwang.soa.core.filter.FilterChain;
import com.isuwang.soa.monitor.api.MonitorServiceClient;
import com.isuwang.soa.monitor.api.domain.QPSStat;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * QPS Stat Filter
 *
 * @author craneding
 * @date 16/3/7
 */
public class QPSStatFilter implements StatusFilter {

    private final long period = 5 * 1000L;
    private static final Map<String, AtomicInteger> methodCallCount = new HashMap<>();
    private final Timer timer = new Timer("QPSStatFilter-Timer");
    private static final Logger LOGGER = LoggerFactory.getLogger(QPSStatFilter.class);

    @Override
    public void init() {
        ContainerSoaHeader.setup();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 1);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        LOGGER.info("QPSStatFilter 定时时间:{} 上送间隔:{}ms", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss S").format(calendar.getTime()), period);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    final long timeMillis = System.currentTimeMillis() / 1000 * 1000;

                    List<QPSStat> qpsStats = new ArrayList<>();
                    Set<String> keys = methodCallCount.keySet();
                    for (String key : keys) {

                        String[] infos = key.split(":");
                        QPSStat qpsStat = new QPSStat();
                        qpsStat.setPeriod((int) (period / 1000));
                        qpsStat.setAnalysisTime(timeMillis);
                        qpsStat.setServerIP(IPUtils.localIp());
                        qpsStat.setServerPort(SoaSystemEnvProperties.SOA_CONTAINER_PORT);

                        qpsStat.setServiceName(infos[0]);
                        qpsStat.setMethodName(infos[1]);
                        qpsStat.setVersionName(infos[2]);
                        qpsStat.setCallCount(methodCallCount.get(key).get());

                        qpsStats.add(qpsStat);
                    }

                    new MonitorServiceClient().uploadQPSStat(qpsStats);
                } catch (Exception e) {
                    LOGGER.error(e.getMessage());
                } finally {
                    methodCallCount.clear();
                }
            }
        }, calendar.getTime(), period);
    }

    @Override
    public void doFilter(FilterChain chain) throws TException {

        final SoaHeader soaHeader = TransactionContext.Factory.getCurrentInstance().getHeader();
        final String key = generateKey(soaHeader);

        try {
            chain.doFilter();
        } finally {

            synchronized (methodCallCount) {
                if (methodCallCount.containsKey(key)) {
                    methodCallCount.get(key).incrementAndGet();
                } else {
                    AtomicInteger count = new AtomicInteger(1);
                    methodCallCount.put(key, count);
                }
            }
        }
    }

    @Override
    public void destory() {
        timer.cancel();
    }

    private String generateKey(SoaHeader header) {
        return header.getServiceName() + ":" + header.getMethodName() + ":" + header.getVersionName();
    }
}
