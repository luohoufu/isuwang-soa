package com.isuwang.soa.container.filter;

import com.isuwang.soa.core.IPUtils;
import com.isuwang.soa.core.SoaSystemEnvProperties;
import com.isuwang.soa.core.filter.FilterChain;
import com.isuwang.soa.monitor.api.MonitorServiceClient;
import com.isuwang.soa.monitor.api.domain.QPSStat;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * QPS Stat Filter
 *
 * @author craneding
 * @date 16/3/7
 */
public class QPSStatFilter implements StatusFilter {

    private final long period = 5 * 1000L;
    private final AtomicInteger callCount = new AtomicInteger(0);
    private final Timer timer = new Timer("QPSStatFilter-Timer");
    private static final Logger LOGGER = LoggerFactory.getLogger(QPSStatFilter.class);

    @Override
    public void init() {
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
                    final int callCountNum = callCount.getAndSet(0);

                    QPSStat qpsStat = new QPSStat();
                    qpsStat.setPeriod((int) (period / 1000));
                    qpsStat.setAnalysisTime(timeMillis);
                    qpsStat.setServerIP(IPUtils.localIp());
                    qpsStat.setServerPort(SoaSystemEnvProperties.SOA_CONTAINER_PORT);
                    qpsStat.setCallCount(callCountNum);

                    new MonitorServiceClient().uploadQPSStat(qpsStat);
                } catch (Exception e) {
                    LOGGER.error(e.getMessage());
                }
            }
        }, calendar.getTime(), period);
    }

    @Override
    public void doFilter(FilterChain chain) throws TException {
        try {
            chain.doFilter();
        } finally {
            callCount.incrementAndGet();
        }
    }

    @Override
    public void destory() {
        timer.cancel();
    }
}
