package com.isuwang.soa.container.filter;

import com.isuwang.soa.core.SoaGlobalTransactional;
import com.isuwang.soa.core.SoaHeader;
import com.isuwang.soa.core.SoaSystemEnvProperties;
import com.isuwang.soa.core.TransactionContext;
import com.isuwang.soa.core.filter.Filter;
import com.isuwang.soa.core.filter.FilterChain;
import com.isuwang.soa.core.filter.container.ContainerFilterChain;
import com.isuwang.soa.transaction.api.GlobalTransactionCallbackWithoutResult;
import com.isuwang.soa.transaction.api.GlobalTransactionTemplate;
import org.apache.thrift.TException;

/**
 * Created by tangliu on 2016/4/11.
 */
public class SoaGlobalTransactionalFilter implements Filter {
    @Override
    public void doFilter(FilterChain chain) throws TException {
        final SoaHeader soaHeader = (SoaHeader) chain.getAttribute(ContainerFilterChain.ATTR_KEY_HEADER);
        final TransactionContext context = (TransactionContext) chain.getAttribute(ContainerFilterChain.ATTR_KEY_CONTEXT);
        final Object iface = chain.getAttribute(ContainerFilterChain.ATTR_KEY_IFACE);

        final boolean isSoaGlobalTransactional = iface.getClass().isAnnotationPresent(SoaGlobalTransactional.class);
        if (isSoaGlobalTransactional) {
            context.setSoaGlobalTransactional(true);
        }

        if (soaHeader.getTransactionId().isPresent() || !SoaSystemEnvProperties.SOA_TRANSACTIONAL_ENABLE) {// in a global transaction
            chain.doFilter();
        } else {
            if (context.isSoaGlobalTransactional()) {
                new GlobalTransactionTemplate().execute(new GlobalTransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult() throws TException {
                        chain.doFilter();
                    }
                });
            } else {
                chain.doFilter();
            }
        }
    }
}
