package com.isuwang.soa.core.socket;

import com.isuwang.org.apache.thrift.transport.TFramedTransport;
import com.isuwang.org.apache.thrift.transport.TIOStreamTransport;
import com.isuwang.org.apache.thrift.transport.TTransport;
import com.isuwang.org.apache.thrift.transport.TTransportException;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Soa Transport
 *
 * @author craneding
 * @date 15/8/5
 */
public class TSoaTransport extends TTransport {

    static class TSoaIOStreamTransport extends TIOStreamTransport {

        public TSoaIOStreamTransport() {

        }

        public TSoaIOStreamTransport(InputStream inputStream, OutputStream outputStream) {
            this.inputStream_ = inputStream;
            this.outputStream_ = outputStream;
        }

        public void setInputStream(InputStream inputStream) {
            this.inputStream_ = inputStream;
        }

        public void setOutputStream(OutputStream outputStream) {
            this.outputStream_ = outputStream;
        }
    }

    private final TFramedTransport delegate;
    private final TSoaIOStreamTransport ioStream;

    public TSoaTransport() {
        delegate = new TFramedTransport(ioStream = new TSoaIOStreamTransport());
    }

    public void setInputStream(InputStream inputStream) {
        ioStream.setInputStream(inputStream);
    }

    public void setOutputStream(OutputStream outputStream) {
        ioStream.setOutputStream(outputStream);
    }

    @Override
    public void open() throws TTransportException {
        delegate.open();
    }

    @Override
    public boolean isOpen() {
        return delegate.isOpen();
    }

    @Override
    public void close() {
        delegate.close();
    }

    @Override
    public int read(byte[] buf, int off, int len) throws TTransportException {
        return delegate.read(buf, off, len);
    }

    @Override
    public byte[] getBuffer() {
        return delegate.getBuffer();
    }

    @Override
    public int getBufferPosition() {
        return delegate.getBufferPosition();
    }

    @Override
    public int getBytesRemainingInBuffer() {
        return delegate.getBytesRemainingInBuffer();
    }

    @Override
    public void consumeBuffer(int len) {
        delegate.consumeBuffer(len);
    }

    @Override
    public void write(byte[] buf, int off, int len) throws TTransportException {
        delegate.write(buf, off, len);
    }

    @Override
    public void flush() throws TTransportException {
        delegate.flush();
    }
}
