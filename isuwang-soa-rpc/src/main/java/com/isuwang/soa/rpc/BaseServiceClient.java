package com.isuwang.soa.rpc;

import com.isuwang.soa.core.Context;
import com.isuwang.soa.core.SoaHeader;
import com.isuwang.soa.core.TBeanSerializer;
import com.isuwang.soa.core.filter.Filter;
import com.isuwang.soa.registry.ServiceInfo;
import com.isuwang.soa.rpc.filter.client.SendMessageFilter;
import com.isuwang.soa.rpc.filter.client.StubFilterChain;
import com.isuwang.soa.rpc.filter.client.xml.SoaFilter;
import com.isuwang.soa.rpc.filter.client.xml.SoaFilters;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXB;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 基础客户端工具
 *
 * @author craneding
 * @date 15/9/24
 */
public class BaseServiceClient {

    protected static final Logger LOGGER = LoggerFactory.getLogger(BaseServiceClient.class);

    protected static final AtomicInteger seqid_ = new AtomicInteger(0);

    static {
        try (InputStream is = getFilterInputStream()) {
            if (is == null)
                throw new RuntimeException("not found filters-client.xml in the classloader.");

            SoaFilters soaFilters = JAXB.unmarshal(is, SoaFilters.class);
            for (SoaFilter soaFilter : soaFilters.getSoaFilter()) {
                Class filterClass = BaseServiceClient.class.getClassLoader().loadClass(soaFilter.getRef());
                Filter filter = (Filter) filterClass.newInstance();
                //filter.init();

                StubFilterChain.addFilter(filter);
                LOGGER.info("load filter {} with path {}", soaFilter.getName(), soaFilter.getRef());
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    static InputStream getFilterInputStream() {
        InputStream stream = BaseServiceClient.class.getClassLoader().getResourceAsStream("filters-client.xml");

        if (stream == null)
            return BaseServiceClient.class.getResourceAsStream("filters-client.xml");

        return stream;
    }

    protected String serviceName;
    protected String versionName;

    protected BaseServiceClient(String serviceName, String versionName) {
        this.serviceName = serviceName;
        this.versionName = versionName;
    }

    protected void initContext(String methodName) {
        Context context = Context.Factory.getCurrentInstance();

        context.setSeqid(seqid_.incrementAndGet());

        SoaHeader soaHeader = new SoaHeader();
        try {
            soaHeader.setCallerIp(Optional.of(InetAddress.getLocalHost().getHostAddress()));
        } catch (UnknownHostException e) {
            LOGGER.error(e.getMessage(), e);
        }

        soaHeader.setServiceName(serviceName);
        soaHeader.setMethodName(methodName);
        soaHeader.setVersionName(versionName);
        soaHeader.setCallerFrom(Optional.of(System.getProperty("soa.service.callerfrom", "web")));

        context.setHeader(soaHeader);

        context.setCalleeTimeout(Long.valueOf(System.getProperty("soa.service.timeout", "45000")));
    }

    protected void destoryContext() {
        Context.Factory.removeCurrentInstance();
    }

    protected <REQ, RESP> RESP sendBase(REQ request, RESP response, TBeanSerializer<REQ> requestSerializer, TBeanSerializer<RESP> responseSerializer) throws TException {
        Context context = Context.Factory.getCurrentInstance();
        SoaHeader soaHeader = context.getHeader();

        final StubFilterChain stubFilterChain = new StubFilterChain();
        stubFilterChain.setLastFilter(new SendMessageFilter());

        stubFilterChain.setAttribute(StubFilterChain.ATTR_KEY_CONTEXT, context);
        stubFilterChain.setAttribute(StubFilterChain.ATTR_KEY_HEADER, soaHeader);
        stubFilterChain.setAttribute(StubFilterChain.ATTR_KEY_REQUEST, request);
        stubFilterChain.setAttribute(SendMessageFilter.ATTR_KEY_SENDMESSAGE, (SendMessageFilter.SendMessageAction) (chain) -> {
            //com.isuwang.soa.rpc.socket.SoaConnection conn = com.isuwang.soa.rpc.socket.SoaConnectionPool.getInstance().getConnection();
            com.isuwang.soa.rpc.netty.SoaConnection conn = com.isuwang.soa.rpc.netty.SoaConnectionPool.getInstance().getConnection();

            RESP resp = conn.send(request, response, requestSerializer, responseSerializer);

            chain.setAttribute(StubFilterChain.ATTR_KEY_RESPONSE, resp);

            final ServiceInfo serviceInfo = (ServiceInfo) chain.getAttribute(StubFilterChain.ATTR_KEY_SERVERINFO);
            serviceInfo.getActiveCount().decrementAndGet();
        });

        stubFilterChain.doFilter();

        return (RESP) stubFilterChain.getAttribute(StubFilterChain.ATTR_KEY_RESPONSE);
    }

}
