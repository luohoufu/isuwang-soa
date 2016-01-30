package com.isuwang.soa.container.netty;

import com.isuwang.soa.core.*;
import com.isuwang.soa.core.netty.TSoaTransport;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
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

        if (useThreadPool)
            executorService.execute(() -> callService(ctx, (ByteBuf) msg));
        else
            callService(ctx, (ByteBuf) msg);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error(cause.getMessage(), cause);

        ctx.close();
    }

    protected void callService(ChannelHandlerContext ctx, ByteBuf inputBuf) {
        final ByteBuf outputBuf = ctx.alloc().buffer(8192);
        final Context context = Context.Factory.getCurrentInstance();
        final SoaHeader soaHeader = new SoaHeader();
        final TSoaTransport inputSoaTransport = new TSoaTransport(inputBuf);
        final TSoaTransport outputSoaTransport = new TSoaTransport(outputBuf);

        context.setHeader(soaHeader);

        TSoaServiceProtocol inputProtocol, outputProtocol = null;

        try {
            inputProtocol = new TSoaServiceProtocol(inputSoaTransport);
            outputProtocol = new TSoaServiceProtocol(outputSoaTransport);
            TMessage tMessage = inputProtocol.readMessageBegin();

            context.setSeqid(tMessage.seqid);

            SoaBaseProcessor<?> soaProcessor = soaProcessors.get(soaHeader.getServiceName());

            soaProcessor.process(inputProtocol, outputProtocol);

            outputSoaTransport.flush();

            ctx.writeAndFlush(outputBuf);

            if (inputBuf.refCnt() > 0)
                inputBuf.release();
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
