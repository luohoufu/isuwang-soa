package com.isuwang.soa.remoting.netty;

import com.isuwang.soa.core.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TMessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SoaConnectionImpl implements com.isuwang.soa.remoting.SoaConnection {

    private static final Logger LOGGER = LoggerFactory.getLogger(SoaConnectionImpl.class);

    private SoaClient soaClient;

    public SoaConnectionImpl(String host, int port) {
        try {
            soaClient = new SoaClient(host, port);
        } catch (SoaException e) {
            LOGGER.error("connect to {}:{} failed", host, port);
        }
    }

    public <REQ, RESP> RESP send(REQ request, RESP response, TBeanSerializer<REQ> requestSerializer, TBeanSerializer<RESP> responseSerializer) throws TException {
        Context context = Context.Factory.getCurrentInstance();
        SoaHeader soaHeader = context.getHeader();

        final ByteBuf requestBuf = Unpooled.buffer(8192);
        final TSoaTransport outputSoaTransport = new TSoaTransport(requestBuf);

        TSoaServiceProtocol outputProtocol;

        try {
            outputProtocol = new TSoaServiceProtocol(outputSoaTransport);
            outputProtocol.writeMessageBegin(new TMessage(soaHeader.getServiceName() + ":" + soaHeader.getMethodName(), TMessageType.CALL, context.getSeqid()));
            requestSerializer.write(request, outputProtocol);
            outputProtocol.writeMessageEnd();

            outputSoaTransport.flush();//在报文头部写入int,代表报文长度(不包括自己)
            if (soaClient == null) {
                throw new SoaException(SoaBaseCode.NotConnected);
            }
            ByteBuf responseBuf = soaClient.send(context.getSeqid(), requestBuf); //发送请求，返回结果

            if (responseBuf == null) {
                throw new SoaException(SoaBaseCode.TimeOut);
//                throw new TException("request time out.");
            } else {
                final TSoaTransport inputSoaTransport = new TSoaTransport(responseBuf);
                TSoaServiceProtocol inputProtocol = new TSoaServiceProtocol(inputSoaTransport);

                TMessage msg = inputProtocol.readMessageBegin();
                if (TMessageType.EXCEPTION == msg.type) {
                    TApplicationException x = TApplicationException.read(inputProtocol);
                    inputProtocol.readMessageEnd();
                    throw x;
                } else if (context.getSeqid() != msg.seqid) {
                    throw new TApplicationException(4, soaHeader.getMethodName() + " failed: out of sequence response");
                } else {
                    if ("0000".equals(soaHeader.getRespCode().get())) {
                        responseSerializer.read(response, inputProtocol);
                        inputProtocol.readMessageEnd();
                    } else {
                        throw new SoaException(soaHeader.getRespCode().get(), soaHeader.getRespMessage().get());
                    }

                    return response;
                }
            }
        } catch (SoaException e) {
            LOGGER.error(e.getMessage(), e);
            throw e;

        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);

            throw new SoaException(SoaBaseCode.UnKnown);
        } finally {
            outputSoaTransport.close();
        }
    }

}
