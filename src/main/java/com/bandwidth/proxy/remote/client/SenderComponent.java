/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: SenderComponent.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.remote.client;

import com.bandwidth.proxy.base.DnsUtils;
import com.bandwidth.proxy.base.Env;
import com.bandwidth.proxy.base.InnerMsg;
import com.bandwidth.proxy.base.LogUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ReflectiveChannelFactory;
import io.netty.channel.socket.SocketChannel;
import io.netty.resolver.dns.DnsAddressResolverGroup;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SenderComponent {
    private final EventLoopGroup group = Env.eventLoopGroup("http", 1);
    private final DnsAddressResolverGroup resolverGroup = DnsUtils.resolverGroup(group.next());

    public SocketChannel openChannel(InnerMsg msg) {
        ByteBuf data = msg.msg.msg;
        String uri = data.readCharSequence(data.readableBytes(), StandardCharsets.US_ASCII).toString();
        LogUtils.debug("connect to,id:" + msg.msg.id + ",to:" + uri);
        data.release();
        String[] subs = uri.split(":");
        String dest = subs[0];
        int port = Integer.valueOf(subs[1]);

        Bootstrap bootstrap = new Bootstrap();
        Env.setOption(bootstrap);
        bootstrap.resolver(resolverGroup)
                .channelFactory(new ReflectiveChannelFactory<>(Env.CHANNEL_TYPE))
                .group(group)
                .handler(SenderChannelInitializer.DEFAULT)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5 * 1000)
                .option(ChannelOption.SO_SNDBUF, 4 * 1024)
                .option(ChannelOption.SO_RCVBUF, 16 * 1024)
                .option(ChannelOption.AUTO_READ, Boolean.TRUE)
                .option(ChannelOption.TCP_NODELAY, true)
                .attr(Env.KEY_TARGET, uri)
                .attr(Env.KEY_ID, msg.msg.id)
                .attr(Env.KEY_SRC, msg.src)
                .attr(Env.KEY_PENDING_MSG, new ConcurrentLinkedQueue<>())
                .attr(Env.KEY_COMPONENT, this);
        SocketChannel channel = (SocketChannel) bootstrap
                .connect(dest, port)
                .addListener(new ChannelConnectListener(SenderComponent.this))
                .channel();
        return channel;
    }

}
