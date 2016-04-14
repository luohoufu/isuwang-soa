package com.isuwang.soa.remoting.filter;

import com.isuwang.soa.core.InvocationContext;
import com.isuwang.soa.core.SoaSystemEnvProperties;
import com.isuwang.soa.core.TransactionContext;
import com.isuwang.soa.core.filter.Filter;
import com.isuwang.soa.core.filter.FilterChain;
import com.isuwang.soa.transaction.api.GlobalTransactionCallback;
import com.isuwang.soa.transaction.api.GlobalTransactionCallbackWithoutResult;
import com.isuwang.soa.transaction.api.GlobalTransactionProcessTemplate;
import org.apache.thrift.TException;

/**
 * Created by tangliu on 2016/4/11.
 */
public class SoaTransactionalProcessFilter implements Filter {
    @Override
    public void doFilter(FilterChain chain) throws TException {

        final InvocationContext context = (InvocationContext) chain.getAttribute(StubFilterChain.ATTR_KEY_CONTEXT);

        if (SoaSystemEnvProperties.SOA_TRANSACTIONAL_ENABLE && TransactionContext.hasCurrentInstance() && context.isSoaTransactionProcess()) {// in container and is a transaction process
            Object req = chain.getAttribute(StubFilterChain.ATTR_KEY_REQUEST);

            new GlobalTransactionProcessTemplate<>(req).execute(() -> {
                chain.doFilter();
                return chain.getAttribute(StubFilterChain.ATTR_KEY_RESPONSE);
            });
        } else {
            chain.doFilter();
        }
    }
}
