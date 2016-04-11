package com.isuwang.soa.remoting.filter;

import com.isuwang.soa.core.InvocationContext;
import com.isuwang.soa.core.TransactionContext;
import com.isuwang.soa.core.filter.Filter;
import com.isuwang.soa.core.filter.FilterChain;
import org.apache.thrift.TException;

/**
 * Created by tangliu on 2016/4/11.
 */
public class SoaTransactionalProcessFilter implements Filter {
    @Override
    public void doFilter(FilterChain chain) throws TException {

        final InvocationContext context = (InvocationContext) chain.getAttribute(StubFilterChain.ATTR_KEY_CONTEXT);

        if (TransactionContext.hasCurrentInstance() && context.getIsSoaTransactionProcess()) {// in container and is a transaction process
//            new SoaTransactionalProcessTemplate().execute(() -> chain.doFilter());
            chain.doFilter();
        } else {
            chain.doFilter();
        }
    }
}
