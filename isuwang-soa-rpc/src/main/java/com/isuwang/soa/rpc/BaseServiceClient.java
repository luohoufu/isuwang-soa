package com.isuwang.soa.rpc;

import com.isuwang.soa.core.*;
import com.isuwang.soa.core.filter.Filter;
import com.isuwang.soa.registry.ConfigKey;
import com.isuwang.soa.registry.ServiceInfoWatcher;
import com.isuwang.soa.rpc.filter.SendMessageFilter;
import com.isuwang.soa.rpc.filter.StubFilterChain;
import com.isuwang.soa.rpc.filter.xml.SoaFilter;
import com.isuwang.soa.rpc.filter.xml.SoaFilters;
import com.isuwang.soa.rpc.netty.IdleConnectionManager;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXB;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
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
                LOGGER.info("client load filter {} with path {}", soaFilter.getName(), soaFilter.getRef());
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }

        IdleConnectionManager connectionManager = new IdleConnectionManager();
        connectionManager.start();

        ServiceInfoWatcher siw = new ServiceInfoWatcher();
        siw.init();
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

    @SuppressWarnings("unchecked")
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
        });

        try {
            stubFilterChain.doFilter();
        } catch (SoaException e) {
            if (e.getCode().equals(SoaBaseCode.NotConnected.getCode()) || e.getCode().equals(SoaBaseCode.TimeOut.getCode())) {

                int failOverTimes = 0;
                String serviceKey = soaHeader.getServiceName() + "." + soaHeader.getVersionName() + "." + soaHeader.getMethodName() + ".consumer";
                Map<ConfigKey, Object> configs = ServiceInfoWatcher.getConfig().get(serviceKey);
                if (null != configs) {
                    failOverTimes = (Integer) configs.get(ConfigKey.FailOver);
                }

                if (context.getFailedTimes() < failOverTimes) {
                    context.setFailedTimes(context.getFailedTimes() + 1);
                    LOGGER.info("connect failed {} times, try again", context.getFailedTimes());
                    sendBase(request, response, requestSerializer, responseSerializer);
                } else
                    throw e;
            } else
                throw e;
        }

        return (RESP) stubFilterChain.getAttribute(StubFilterChain.ATTR_KEY_RESPONSE);
    }

}
