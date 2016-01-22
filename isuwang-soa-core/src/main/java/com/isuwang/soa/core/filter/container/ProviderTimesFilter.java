package com.isuwang.soa.core.filter.container;

import com.isuwang.soa.core.filter.Filter;
import com.isuwang.soa.core.filter.FilterChain;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Network Times Filter
 *
 * @author craneding
 * @date 16/1/21
 */
public class ProviderTimesFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProviderTimesFilter.class);

    @Override
    public void init() {

    }

    @Override
    public void doFilter(FilterChain chain) throws TException {
        long startTime = System.currentTimeMillis();

        try {
            chain.doFilter();
        } finally {
            LOGGER.info("{} 耗时:{}ms", chain.getAttribute(ContainerFilterChain.ATTR_KEY_LOGID).toString(), System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public void destory() {

    }

}
