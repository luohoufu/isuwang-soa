package com.isuwang.soa.container.filter;

import com.isuwang.soa.core.filter.Filter;
import com.isuwang.soa.core.filter.FilterChain;
import org.apache.thrift.TException;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * QPS Stat Filter
 *
 * @author craneding
 * @date 16/3/7
 */
public class QPSStatFilter implements Filter {
    private final AtomicInteger callCount = new AtomicInteger(0);

    @Override
    public void doFilter(FilterChain chain) throws TException {
        try {
            chain.doFilter();
        } finally {
            callCount.incrementAndGet();
        }
    }

}
