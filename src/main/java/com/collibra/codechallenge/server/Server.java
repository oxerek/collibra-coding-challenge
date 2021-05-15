package com.collibra.codechallenge.server;

import com.collibra.codechallenge.ioc.Configuration;
import com.google.common.collect.Maps;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;

import static io.vavr.control.Try.of;

public class Server {

    private static final Logger log = Logger.getLogger(Server.class);

    private static final int PORT = 50000;

    private final Map<String, Map<String, List<Integer>>> nodes = Maps.newConcurrentMap();

    public void start() {
        EventLoopGroup parentGroup = new NioEventLoopGroup();
        EventLoopGroup childGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(parentGroup, childGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel channel) {
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline.addLast("decoder", new StringDecoder());
                        pipeline.addLast("encoder", new StringEncoder());
                        pipeline.addLast("handler", Configuration.channelHandler(nodes));
                    }
                }).option(ChannelOption.SO_BACKLOG, 1024)
                .childOption(ChannelOption.SO_SNDBUF, 1024)
                .childOption(ChannelOption.SO_RCVBUF, 1024)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        of(() -> {
            log.info("Server started");
            return bootstrap.bind(PORT).sync().channel().closeFuture().sync();
        }).onFailure(throwable -> log.error(throwable)).andFinally(() -> {
            parentGroup.shutdownGracefully();
            childGroup.shutdownGracefully();
        });
    }
}
