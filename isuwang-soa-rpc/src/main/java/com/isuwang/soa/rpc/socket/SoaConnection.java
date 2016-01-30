package com.isuwang.soa.rpc.socket;

import com.isuwang.soa.core.*;
import com.isuwang.soa.core.socket.TSoaTransport;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TMessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.Socket;

/**
 * SOA连接
 *
 * @author craneding
 * @date 15/8/5
 */
public class SoaConnection {
    private static final Logger LOGGER = LoggerFactory.getLogger(SoaConnection.class);

    public <REQ, RESP> RESP send(REQ request, RESP response, TBeanSerializer<REQ> requestSerializer, TBeanSerializer<RESP> responseSerializer) throws TException {
        final Context context = Context.Factory.getCurrentInstance();
        final SoaHeader header = context.getHeader();

        try (
                TSoaTransport transport = new TSoaTransport();
                Socket socket = new Socket(context.getCalleeIp(), context.getCalleePort());
                BufferedOutputStream output = new BufferedOutputStream(socket.getOutputStream());
                BufferedInputStream input = new BufferedInputStream(socket.getInputStream());
        ) {
            socket.setKeepAlive(true);
            socket.setSoTimeout((int) context.getCalleeTimeout());

            transport.setInputStream(input);
            transport.setOutputStream(output);

            TSoaServiceProtocol protocol = new TSoaServiceProtocol(transport);

            protocol.writeMessageBegin(new TMessage(header.getServiceName() + ":" + header.getMethodName(), TMessageType.CALL, context.getSeqid()));
            requestSerializer.write(request, protocol);
            protocol.writeMessageEnd();

            transport.flush();

            TMessage msg = protocol.readMessageBegin();
            if (msg.type == TMessageType.EXCEPTION) {
                TApplicationException x = TApplicationException.read(protocol);
                protocol.readMessageEnd();
                throw x;
            } else if (msg.seqid != context.getSeqid()) {
                throw new TApplicationException(4, header.getMethodName() + " failed: out of sequence response");
            } else {
                if ("0000".equals(header.getRespCode())) {
                    responseSerializer.read(response, protocol);
                    protocol.readMessageEnd();
                } else {
                    throw new SoaException(header.getRespCode().get(), header.getRespMessage().get());
                }
                return response;
            }
        } catch (TException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);

            throw new TException(e);
        }
    }

}
