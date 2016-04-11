package com.isuwang.soa.container.filter;

import com.isuwang.soa.core.SoaHeader;
import com.isuwang.soa.core.TransactionContext;
import com.isuwang.soa.core.filter.Filter;
import com.isuwang.soa.core.filter.FilterChain;
import com.isuwang.soa.core.filter.container.ContainerFilterChain;
import org.apache.thrift.TException;

/**
 * Created by tangliu on 2016/4/11.
 */
public class SoaGlobalTransactionalFilter implements Filter {
    @Override
    public void doFilter(FilterChain chain) throws TException {
        final SoaHeader soaHeader = (SoaHeader) chain.getAttribute(ContainerFilterChain.ATTR_KEY_HEADER);
        final TransactionContext context = (TransactionContext) chain.getAttribute(ContainerFilterChain.ATTR_KEY_CONTEXT);

        if (soaHeader.getTransactionId().isPresent()) {// in a global transaction
            chain.doFilter();
        } else {
            if (context.getIsSoaGlobalTransactional()) {
//                new SoaGlobalTransactionalTemplate().execute(() -> chain.doFilter());
            } else {
                chain.doFilter();
            }
        }
    }
}
