package com.isuwang.soa.container.filter;

import com.isuwang.soa.core.filter.Filter;
import com.isuwang.soa.core.filter.FilterChain;
import com.isuwang.soa.core.filter.container.ContainerFilterChain;
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
    public void doFilter(FilterChain chain) throws TException {
        long startTime = System.currentTimeMillis();

        try {
            chain.doFilter();
        } finally {
            LOGGER.info("{} 耗时:{}ms", chain.getAttribute(ContainerFilterChain.ATTR_KEY_LOGID).toString(), System.currentTimeMillis() - startTime);
        }
    }

}
