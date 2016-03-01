package com.isuwang.soa.container.netty;

import com.isuwang.soa.container.Container;
import com.isuwang.soa.container.registry.ZookeeperRegistryContainer;
import com.isuwang.soa.core.SoaSystemEnvProperties;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Netty Container
 *
 * @author craneding
 * @date 16/1/21
 */
public class NettyContainer implements Container {

    private static final Logger LOGGER = LoggerFactory.getLogger(NettyContainer.class);

    private final int port = SoaSystemEnvProperties.SOA_CONTAINER_PORT;

    private final EventLoopGroup bossGroup = new NioEventLoopGroup();
    private final EventLoopGroup workerGroup = new NioEventLoopGroup();

    private ServerBootstrap bootstrap;

    @Override
    public void start() {
        LOGGER.info("Bind Local Port {} [Netty]", port);

        new Thread() {
            @Override
            public void run() {
                try {
                    bootstrap = new ServerBootstrap();

                    bootstrap.group(bossGroup, workerGroup)
                            .channel(NioServerSocketChannel.class)
                            .childHandler(new ChannelInitializer<SocketChannel>() {
                                @Override
                                protected void initChannel(SocketChannel ch) throws Exception {
                                    ch.pipeline().addLast(new IdleStateHandler(15, 0, 0), new SoaDecoder(), new SoaIdleHandler(), new SoaServerHandler(ZookeeperRegistryContainer.getProcessorMap()));
                                }
                            })
                            .option(ChannelOption.SO_BACKLOG, 1024)
                            .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)//重复利用之前分配的内存空间(PooledByteBuf -> ByteBuf)
                            .childOption(ChannelOption.SO_KEEPALIVE, true)
                            .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

                    // Start the server.
                    ChannelFuture f = bootstrap.bind(port).sync();

                    // Wait until the connection is closed.
                    f.channel().closeFuture().sync();
                } catch (InterruptedException e) {
                    LOGGER.error(e.getMessage(), e);
                } finally {
                    workerGroup.shutdownGracefully();
                    bossGroup.shutdownGracefully();
                }
            }
        }.start();
    }

    @Override
    public void stop() {
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }

}
