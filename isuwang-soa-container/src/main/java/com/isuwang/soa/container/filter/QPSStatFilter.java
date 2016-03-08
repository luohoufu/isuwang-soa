package com.isuwang.soa.container.filter;

import com.isuwang.soa.core.filter.FilterChain;
import com.isuwang.soa.monitor.api.MonitorServiceClient;
import com.isuwang.soa.monitor.api.domain.QPSStat;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private final long period = 5 * 60 * 1000L;
    private final AtomicInteger callCount = new AtomicInteger(0);
    private final Timer timer = new Timer("QPSStatFilter-Timer");
    private static final Logger LOGGER = LoggerFactory.getLogger(QPSStatFilter.class);

    private MonitorServiceClient monitorServiceClient;

    @Override
    public void init() {

        monitorServiceClient = new MonitorServiceClient();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                try {
                    QPSStat qpsStat = new QPSStat();
                    qpsStat.setPeriod((int) period / 1000 / 60);
                    qpsStat.setCallCount(callCount.get());
                    monitorServiceClient.uploadQPSStat(new QPSStat());
                } catch (Exception e) {
                    LOGGER.error(e.getMessage());
                } finally {
                    callCount.set(0);
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
