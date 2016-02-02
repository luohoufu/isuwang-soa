package com.isuwang.soa.rpc.filter.client;

import com.isuwang.soa.core.SoaHeader;
import com.isuwang.soa.core.filter.Filter;
import com.isuwang.soa.core.filter.FilterChain;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Network Log Filter
 *
 * @author craneding
 * @date 16/1/20
 */
public class NetworkLogFilter implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkLogFilter.class);

    @Override
    public void doFilter(FilterChain chain) throws TException {
        final SoaHeader soaHeader = (SoaHeader) chain.getAttribute(StubFilterChain.ATTR_KEY_HEADER);
        final Object request = chain.getAttribute(StubFilterChain.ATTR_KEY_REQUEST);

        LOGGER.info("{} {} {} request:{}", soaHeader.getServiceName(), soaHeader.getVersionName(), soaHeader.getMethodName(), request.toString());

        try {
            chain.doFilter();
        } finally {
            Object response = chain.getAttribute(StubFilterChain.ATTR_KEY_RESPONSE);

            if (response != null)
                LOGGER.info("{} {} {} response:{}", soaHeader.getServiceName(), soaHeader.getVersionName(), soaHeader.getMethodName(), response.toString());
        }
    }

}
