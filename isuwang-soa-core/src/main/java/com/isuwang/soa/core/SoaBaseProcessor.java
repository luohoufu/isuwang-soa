package com.isuwang.soa.core;

import com.isuwang.soa.core.filter.container.ContainerFilterChain;
import com.isuwang.soa.core.filter.container.DispatchFilter;
import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TMessageType;
import org.apache.thrift.protocol.TProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Soa基础处理器
 *
 * @author craneding
 * @date 15/9/18
 */
public class SoaBaseProcessor<I> implements TProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(SoaBaseProcessor.class);

    private final I iface;
    private Class<I> interfaceClass;
    private final Map<String, SoaProcessFunction<I, ?, ?, ? extends TBeanSerializer<?>, ? extends TBeanSerializer<?>>> processMap;

    protected SoaBaseProcessor(I iface, Map<String, SoaProcessFunction<I, ?, ?, ? extends TBeanSerializer<?>, ? extends TBeanSerializer<?>>> processMap) {
        this.iface = iface;
        this.processMap = processMap;
    }

    @Override
    public boolean process(TProtocol in, TProtocol out) throws TException {
        // threadlocal
        Context context = Context.Factory.getCurrentInstance();
        String methodName = context.getHeader().getMethodName();

        final String logId = context.getHeader().getServiceName() + "/" + context.getHeader().getMethodName();

        ContainerFilterChain filterChain = new ContainerFilterChain();
        filterChain.setLastFilter(new DispatchFilter());

        filterChain.setAttribute(ContainerFilterChain.ATTR_KEY_LOGID, logId);
        filterChain.setAttribute(ContainerFilterChain.ATTR_KEY_CONTEXT, context);
        filterChain.setAttribute(ContainerFilterChain.ATTR_KEY_HEADER, context.getHeader());
        filterChain.setAttribute(DispatchFilter.ATTR_KEY_CONTAINER_DISPATCH_ACTION, (DispatchFilter.DispatchAction) chain -> {

            // read
            //TMessage tMessage = in.readMessageBegin();
            @SuppressWarnings("unchecked")
            SoaProcessFunction<I, Object, Object, ? extends TBeanSerializer<Object>, ? extends TBeanSerializer<Object>> soaProcessFunction = (SoaProcessFunction<I, Object, Object, ? extends TBeanSerializer<Object>, ? extends TBeanSerializer<Object>>) getProcessMapView().get(methodName);
            Object args = soaProcessFunction.getEmptyArgsInstance();
            soaProcessFunction.getReqSerializer().read(args, in);
            in.readMessageEnd();

            LOGGER.info("{} request:{}", logId, soaProcessFunction.getReqSerializer().toString(args));
            long startTime = System.currentTimeMillis();

            Object result = soaProcessFunction.getResult(iface, args);

            chain.setAttribute(ContainerFilterChain.ATTR_KEY_I_PROCESSTIME, System.currentTimeMillis() - startTime);
            LOGGER.info("{} response:{}", logId, soaProcessFunction.getResSerializer().toString(result));

            // write
            context.getHeader().setRespCode(Optional.of("0000"));
            context.getHeader().setRespMessage(Optional.of("成功"));
            out.writeMessageBegin(new TMessage(context.getHeader().getMethodName(), TMessageType.CALL, context.getSeqid()));
            soaProcessFunction.getResSerializer().write(result, out);
            out.writeMessageEnd();
        });

        filterChain.doFilter();

        return true;
    }

    public Map<String, SoaProcessFunction<I, ?, ?, ? extends TBeanSerializer<?>, ? extends TBeanSerializer<?>>> getProcessMapView() {
        return Collections.unmodifiableMap(processMap);
    }

    public Class<I> getInterfaceClass() {
        return interfaceClass;
    }

    public void setInterfaceClass(Class<I> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }
}
