package com.isuwang.soa.container.netty;

import com.isuwang.soa.container.util.PlatformProcessDataFactory;
import com.isuwang.soa.core.*;
import com.isuwang.soa.monitor.api.domain.PlatformProcessData;
import com.isuwang.soa.registry.ConfigKey;
import com.isuwang.soa.registry.RegistryAgentProxy;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TMessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Soa Server Handler
 *
 * @author craneding
 * @date 16/1/12
 */
public class SoaServerHandler extends ChannelHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(SoaServerHandler.class);

    private static Map<String, SoaBaseProcessor<?>> soaProcessors;

    /**
     * threadPool to read requests
     */
    private final ExecutorService executorService;
    private final Boolean useThreadPool = SoaSystemEnvProperties.SOA_CONTAINER_USETHREADPOOL;

    static class ServerThreadFactory implements ThreadFactory {
        private static final AtomicInteger executorId = new AtomicInteger();
        private static final String namePrefix = "soa-threadPool";

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, namePrefix + "-" + executorId.getAndIncrement());
        }
    }

    public SoaServerHandler(Map<String, SoaBaseProcessor<?>> soaProcessors) {
        this.soaProcessors = soaProcessors;

        executorService = Executors.newFixedThreadPool(Integer.getInteger("soa.container.threadpool.size", Runtime.getRuntime().availableProcessors() * 2), new ServerThreadFactory());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        readRequestHeader(ctx, (ByteBuf) msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error(cause.getMessage(), cause);

        ctx.close();
    }

    protected void readRequestHeader(ChannelHandlerContext ctx, ByteBuf inputBuf) throws TException {
        final Long startTime = System.currentTimeMillis();

        /**
         * get the length of the request
         */
        int readerIndex = inputBuf.readerIndex();
        int requestLength = inputBuf.readInt();
        inputBuf.readerIndex(readerIndex);

        final Context context = Context.Factory.getNewInstance();
        final SoaHeader soaHeader = new SoaHeader();
        final TSoaTransport inputSoaTransport = new TSoaTransport(inputBuf);
        context.setHeader(soaHeader);
        Context.Factory.setCurrentInstance(context);

        final PlatformProcessData processData = PlatformProcessDataFactory.getNewInstance(soaHeader);
        processData.setRequestFlow(requestLength + Integer.BYTES);
        PlatformProcessDataFactory.setCurrentInstance(processData);

        try {
            final TSoaServiceProtocol inputProtocol = new TSoaServiceProtocol(inputSoaTransport);
            TMessage tMessage = inputProtocol.readMessageBegin();
            context.setSeqid(tMessage.seqid);

            /**
             * check if use executorService for this service and
             */
            boolean b = true;

            String serviceKey = soaHeader.getServiceName() + "." + soaHeader.getVersionName() + "." + soaHeader.getMethodName() + ".producer";
            Map<ConfigKey, Object> configs = RegistryAgentProxy.getCurrentInstance(RegistryAgentProxy.Type.Server).getConfig().get(serviceKey);

            if (null != configs) {
                Boolean aBoolean = (Boolean) configs.get(ConfigKey.ThreadPool);

                if (aBoolean != null)
                    b = aBoolean.booleanValue();
            }

            if (useThreadPool && b) {
                executorService.execute(() -> processRequest(ctx, inputBuf, inputSoaTransport, inputProtocol, context, startTime, processData));
            } else
                processRequest(ctx, inputBuf, inputSoaTransport, inputProtocol, context, startTime, processData);
        } finally {
            if (inputSoaTransport.isOpen())
                inputSoaTransport.close();

            Context.Factory.removeCurrentInstance();
            PlatformProcessDataFactory.removeCurrentInstance();
        }
    }

    protected void processRequest(ChannelHandlerContext ctx, ByteBuf inputBuf, TSoaTransport inputSoaTransport, TSoaServiceProtocol inputProtocol, Context context, Long startTime, PlatformProcessData processData) {
        final ByteBuf outputBuf = ctx.alloc().buffer(8192);

        Context.Factory.setCurrentInstance(context);
        PlatformProcessDataFactory.setCurrentInstance(processData);

        SoaHeader soaHeader = context.getHeader();

        final TSoaTransport outputSoaTransport = new TSoaTransport(outputBuf);
        TSoaServiceProtocol outputProtocol = null;

        boolean isSucceed = false;

        try {
            outputProtocol = new TSoaServiceProtocol(outputSoaTransport);
            SoaBaseProcessor<?> soaProcessor = soaProcessors.get(soaHeader.getServiceName());

            soaProcessor.process(inputProtocol, outputProtocol);

            outputSoaTransport.flush();

            ctx.writeAndFlush(outputBuf);

            if (inputBuf.refCnt() > 0)
                inputBuf.release();

            isSucceed = true;
        } catch (SoaException e) {
            LOGGER.error(e.getMessage(), e);

            writeErrorMessage(ctx, outputBuf, context, soaHeader, outputSoaTransport, outputProtocol, e);
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);

            writeErrorMessage(ctx, outputBuf, context, soaHeader, outputSoaTransport, outputProtocol, new SoaException(SoaBaseCode.NotNull));
        } finally {
            if (inputSoaTransport != null)
                inputSoaTransport.close();

            if (outputSoaTransport != null)
                outputSoaTransport.close();

            final boolean finalIsSucceed = isSucceed;
            PlatformProcessDataFactory.update(soaHeader, cacheProcessData -> {
                long totalTime = System.currentTimeMillis() - startTime;

                if (cacheProcessData.getPMinTime() == 0 || totalTime < cacheProcessData.getPMinTime())
                    cacheProcessData.setPMinTime(cacheProcessData.getPMinTime());
                if (cacheProcessData.getPMaxTime() == 0 || totalTime > cacheProcessData.getPMaxTime())
                    cacheProcessData.setPMaxTime(cacheProcessData.getPMaxTime());
                cacheProcessData.setPTotalTime(cacheProcessData.getPTotalTime() + totalTime);

                if (finalIsSucceed)
                    cacheProcessData.setSucceedCalls(cacheProcessData.getSucceedCalls() + 1);
                else
                    cacheProcessData.setFailCalls(cacheProcessData.getFailCalls() + 1);

                cacheProcessData.setTotalCalls(cacheProcessData.getTotalCalls() + 1);

                cacheProcessData.setRequestFlow(cacheProcessData.getRequestFlow() + processData.getRequestFlow());
                cacheProcessData.setResponseFlow(cacheProcessData.getResponseFlow() + outputBuf.writerIndex());
            });

            Context.Factory.removeCurrentInstance();
            PlatformProcessDataFactory.removeCurrentInstance();
        }
    }

    private void writeErrorMessage(ChannelHandlerContext ctx, ByteBuf outputBuf, Context context, SoaHeader soaHeader, TSoaTransport outputSoaTransport, TSoaServiceProtocol outputProtocol, SoaException e) {
        if (outputProtocol != null) {
            try {
                soaHeader.setRespCode(Optional.of(e.getCode()));
                soaHeader.setRespMessage(Optional.of(e.getMsg()));
                outputProtocol.writeMessageBegin(new TMessage(soaHeader.getServiceName() + ":" + soaHeader.getMethodName(), TMessageType.REPLY, context.getSeqid()));
                outputProtocol.writeMessageEnd();

                outputSoaTransport.flush();

                ctx.writeAndFlush(outputBuf);
            } catch (Throwable e1) {
                LOGGER.error(e1.getMessage(), e1);
            }
        }
    }
}
