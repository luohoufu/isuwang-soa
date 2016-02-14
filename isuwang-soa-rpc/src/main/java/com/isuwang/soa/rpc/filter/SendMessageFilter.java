package com.isuwang.soa.rpc.filter;

import com.isuwang.soa.core.filter.Filter;
import com.isuwang.soa.core.filter.FilterChain;
import com.isuwang.soa.registry.ServiceInfo;
import org.apache.thrift.TException;

/**
 * Send Message Filter
 *
 * @author craneding
 * @date 16/1/20
 */
public class SendMessageFilter implements Filter {

    public static final String ATTR_KEY_SENDMESSAGE = "send.message.action";

    public interface SendMessageAction {

        void doAction(FilterChain chain) throws TException;

    }

    @Override
    public void doFilter(FilterChain chain) throws TException {

        //add active count
        ServiceInfo serviceInfo = (ServiceInfo) chain.getAttribute(StubFilterChain.ATTR_KEY_SERVERINFO);
        serviceInfo.getActiveCount().incrementAndGet();

        SendMessageAction action = (SendMessageAction) chain.getAttribute(SendMessageFilter.ATTR_KEY_SENDMESSAGE);

        action.doAction(chain);
    }

}
