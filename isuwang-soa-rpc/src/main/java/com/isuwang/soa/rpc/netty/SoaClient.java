package com.isuwang.soa.rpc.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by tangliu on 2016/1/13.
 */
public class SoaClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(SoaClient.class);

    private final String host;
    private final int port;
    private final int readerIdleTimeSeconds = 15;
    private final int writerIdleTimeSeconds = 10;
    private final int allIdleTimeSeconds = 0;
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();

    private Bootstrap b = null;
    private Channel channel = null;

    /**
     * 根据请求的序列号（seqid），存储请求的结果，多个线程请求的时候才能返回正确的结果
     */
    private final Map<String, ByteBuf[]> caches = new ConcurrentHashMap<>();

    public SoaClient(String host, int port) {
        this.host = host;
        this.port = port;

        try {
            connect(host, port);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        initBootstrap();
    }

    protected Bootstrap initBootstrap() {
        b = new Bootstrap();
        b.group(workerGroup);
        b.channel(NioSocketChannel.class);
        b.option(ChannelOption.SO_KEEPALIVE, true);
        b.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        b.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new IdleStateHandler(readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds), new SoaDecoder(), new SoaIdleHandler(), new SoaClientHandler(callBack));
            }
        });
        return b;
    }

    /**
     * 创建连接
     *
     * @param host
     * @param port
     * @return
     * @throws Exception
     */
    private synchronized Channel connect(String host, int port) throws Exception {
        if (channel != null && channel.isActive())
            return channel;

        try {
            Bootstrap b = initBootstrap();
            return channel = b.connect(host, port).sync().channel();
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }

        return null;
    }


    /**
     * 回调方法，SoaClientHandler收到返回结果，即调用此方法处理返回结果，取出seqid,
     * 将结果放入对应的caches中，并释放锁，使得等待的线程返回结果
     */
    private SoaClientHandler.CallBack callBack = msg -> {
        // length(4) stx(1) version(...) protocol(1) seqid(4) header(...) body(...) etx(1)
        int readerIndex = msg.readerIndex();
        msg.skipBytes(5);
        int len = msg.readInt();
        msg.readBytes(new byte[len], 0, len);
        msg.skipBytes(1);
        int seqid = msg.readInt();
        msg.readerIndex(readerIndex);

        ByteBuf[] byteBufs = caches.get(String.valueOf(seqid));

        if (byteBufs == null) {
            LOGGER.error("返回结果超时，siqid为：" + String.valueOf(seqid));
        } else {
            synchronized (byteBufs) {
                byteBufs[0] = msg;

                byteBufs.notify();
            }
        }
    };

    /**
     * 发送请求，阻塞等待结果再返回
     *
     * @param seqid
     * @param request
     * @return
     */
    public ByteBuf send(int seqid, ByteBuf request) throws Exception {
        if (channel == null || !channel.isActive())
            connect(host, port);

        //means that this channel is not idle and would not managered by IdleConnectionManager
        IdleConnectionManager.remove(channel);

        ByteBuf[] byteBufs = new ByteBuf[1];

        caches.put(String.valueOf(seqid), byteBufs);

        try {
            channel.writeAndFlush(request);

            //等待返回结果，soaClientHandler会将结果写入caches并释放锁，此时返回
            synchronized (byteBufs) {
                if (byteBufs[0] != null)
                    return byteBufs[0];

                try {
                    byteBufs.wait(50000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return byteBufs[0];
        } finally {
            caches.remove(String.valueOf(seqid));
        }
    }

    /**
     * 关闭连接
     */
    public void close() {
        if (channel != null)
            channel.close();

        channel = null;
    }

    public void shutdown() {
        if (workerGroup != null)
            workerGroup.shutdownGracefully();
    }

}
