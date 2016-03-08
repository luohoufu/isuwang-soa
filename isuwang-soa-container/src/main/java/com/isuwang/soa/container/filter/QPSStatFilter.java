package com.isuwang.soa.container.filter;

import com.isuwang.soa.core.filter.FilterChain;
import org.apache.thrift.TException;

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
    private final int period = 5;
    private final AtomicInteger callCount = new AtomicInteger(0);
    private final Timer timer = new Timer("QPSStatFilter-Timer");

    @Override
    public void init() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                //TODO upload data and set zero into AtomicInteger
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
