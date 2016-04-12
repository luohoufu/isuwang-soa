package com.isuwang.soa.remoting;

import com.isuwang.soa.core.*;
import com.isuwang.soa.core.filter.Filter;
import com.isuwang.soa.registry.ConfigKey;
import com.isuwang.soa.registry.RegistryAgent;
import com.isuwang.soa.registry.RegistryAgentProxy;
import com.isuwang.soa.registry.conf.SoaRegistry;
import com.isuwang.soa.remoting.conf.SoaRemoting;
import com.isuwang.soa.remoting.conf.SoaRemotingConnectionPool;
import com.isuwang.soa.remoting.conf.SoaRemotingFilter;
import com.isuwang.soa.remoting.conf.SoaRemotingFilters;
import com.isuwang.soa.remoting.filter.SendMessageFilter;
import com.isuwang.soa.remoting.filter.StubFilterChain;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXB;
import java.io.FileNotFoundException;
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

    protected static SoaConnectionPool connectionPool;

    static {
        final ClassLoader classLoader = BaseServiceClient.class.getClassLoader();

        try (InputStream is = getInputStream("remoting-conf.xml")) {
            final SoaRemoting soaRemoting = JAXB.unmarshal(is, SoaRemoting.class);
            final SoaRemotingFilters remotingFilters = soaRemoting.getSoaRemotingFilters();

            // load filter
            for (SoaRemotingFilter remotingFilter : remotingFilters.getSoaRemotingFilter()) {
                Class<?> aClass = classLoader.loadClass(remotingFilter.getRef());

                StubFilterChain.addFilter((Filter) aClass.newInstance());

                LOGGER.info("client load filter {} with path {}", remotingFilter.getName(), remotingFilter.getRef());
            }

            // load connection pool
            final SoaRemotingConnectionPool pool = soaRemoting.getSoaRemotingConnectionPool();
            final Class<?> aClass = classLoader.loadClass(pool.getRef());
            BaseServiceClient.connectionPool = (SoaConnectionPool) aClass.newInstance();
        } catch (Exception e) {
            LOGGER.error("client load filter error", e);
        }

        if (!SoaSystemEnvProperties.SOA_REMOTING_MODE.equals("local")) {
            try (InputStream is = getInputStream("registry-conf.xml")) {
                final SoaRegistry soaRegistry = JAXB.unmarshal(is, SoaRegistry.class);

                Class<?> aClass = classLoader.loadClass(soaRegistry.getRef());

                RegistryAgentProxy.setCurrentInstance(RegistryAgentProxy.Type.Client, (RegistryAgent) aClass.newInstance());
                RegistryAgentProxy.getCurrentInstance(RegistryAgentProxy.Type.Client).start();
                LOGGER.info("client load registry {} with path {}", soaRegistry.getName(), soaRegistry.getRef());
            } catch (Exception e) {
                LOGGER.error("client load registry error", e);
            }
        } else {
            LOGGER.info("soa remoting mode is {},client not load registry", SoaSystemEnvProperties.SOA_REMOTING_MODE);
        }
    }

    static InputStream getInputStream(String name) throws FileNotFoundException {
        InputStream stream = BaseServiceClient.class.getClassLoader().getResourceAsStream(name);

        if (stream == null)
            return BaseServiceClient.class.getResourceAsStream(name);

        if (stream == null)
            throw new FileNotFoundException("not found " + name);

        return stream;
    }

    protected String serviceName;
    protected String versionName;

    protected BaseServiceClient(String serviceName, String versionName) {
        this.serviceName = serviceName;
        this.versionName = versionName;
    }

    protected void initContext(String methodName) {
        InvocationContext context = InvocationContext.Factory.getCurrentInstance();

        context.setSeqid(seqid_.incrementAndGet());

        SoaHeader soaHeader = new SoaHeader();

        InvocationContext.Factory.ISoaHeaderProxy headerProxy = InvocationContext.Factory.getSoaHeaderProxy();
        if (headerProxy != null) {
            soaHeader.setCallerFrom(headerProxy.callerFrom());
            soaHeader.setCustomerId(headerProxy.customerId());
            soaHeader.setCustomerName(headerProxy.customerName());
            soaHeader.setOperatorId(headerProxy.operatorId());
            soaHeader.setOperatorName(headerProxy.operatorName());
        }

        try {
            soaHeader.setCallerIp(Optional.of(InetAddress.getLocalHost().getHostAddress()));
        } catch (UnknownHostException e) {
            LOGGER.error(e.getMessage(), e);
        }

        soaHeader.setServiceName(serviceName);
        soaHeader.setMethodName(methodName);
        soaHeader.setVersionName(versionName);

        if (!soaHeader.getCallerFrom().isPresent())
            soaHeader.setCallerFrom(Optional.of(SoaSystemEnvProperties.SOA_SERVICE_CALLERFROM));


        context.setHeader(soaHeader);

        if (context.getCalleeTimeout() <= 0)
            context.setCalleeTimeout(SoaSystemEnvProperties.SOA_SERVICE_TIMEOUT);

        context.setSoaTransactionProcess(isSoaTransactionalProcess());
    }

    protected boolean isSoaTransactionalProcess() {
        return false;
    }

    protected void destoryContext() {
        InvocationContext.Factory.removeCurrentInstance();
    }

    @SuppressWarnings("unchecked")
    protected <REQ, RESP> RESP sendBase(REQ request, RESP response, TBeanSerializer<REQ> requestSerializer, TBeanSerializer<RESP> responseSerializer) throws TException {
        InvocationContext context = InvocationContext.Factory.getCurrentInstance();
        SoaHeader soaHeader = context.getHeader();

        final StubFilterChain stubFilterChain = new StubFilterChain();
        stubFilterChain.setLastFilter(new SendMessageFilter());

        stubFilterChain.setAttribute(StubFilterChain.ATTR_KEY_CONTEXT, context);
        stubFilterChain.setAttribute(StubFilterChain.ATTR_KEY_HEADER, soaHeader);
        stubFilterChain.setAttribute(StubFilterChain.ATTR_KEY_REQUEST, request);
        stubFilterChain.setAttribute(SendMessageFilter.ATTR_KEY_SENDMESSAGE, (SendMessageFilter.SendMessageAction) (chain) -> {
            SoaConnection conn = connectionPool.getConnection();

            RESP resp = conn.send(request, response, requestSerializer, responseSerializer);

            chain.setAttribute(StubFilterChain.ATTR_KEY_RESPONSE, resp);
        });

        try {
            stubFilterChain.doFilter();
        } catch (SoaException e) {
            if (e.getCode().equals(SoaBaseCode.NotConnected.getCode()) || e.getCode().equals(SoaBaseCode.TimeOut.getCode())) {

                int failOverTimes = 0;
                String serviceKey = soaHeader.getServiceName() + "." + soaHeader.getVersionName() + "." + soaHeader.getMethodName() + ".consumer";
                RegistryAgent registryAgent = RegistryAgentProxy.getCurrentInstance(RegistryAgentProxy.Type.Client);
                Map<ConfigKey, Object> configs = registryAgent != null ? registryAgent.getConfig().get(serviceKey) : null;
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
