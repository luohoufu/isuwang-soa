package com.isuwang.soa.remoting.filter;

import com.isuwang.soa.core.SoaHeader;
import com.isuwang.soa.core.filter.Filter;
import com.isuwang.soa.core.filter.FilterChain;
import com.isuwang.org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Network Times Filter
 *
 * @author craneding
 * @date 16/1/20
 */
public class NetworkTimesFilter implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkTimesFilter.class);

    @Override
    public void doFilter(FilterChain chain) throws TException {
        final long startTime = System.currentTimeMillis();
        final SoaHeader soaHeader = (SoaHeader) chain.getAttribute(StubFilterChain.ATTR_KEY_HEADER);

        try {
            chain.doFilter();
        } finally {
            LOGGER.info("{} {} {} 耗时:{}ms", soaHeader.getServiceName(), soaHeader.getVersionName(), soaHeader.getMethodName(), System.currentTimeMillis() - startTime);
        }
    }

}
