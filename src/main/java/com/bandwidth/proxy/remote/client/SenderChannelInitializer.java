/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: SenderChannelInitializer.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.remote.client;

import com.bandwidth.proxy.base.Config;
import com.bandwidth.proxy.component.netty.ChannelErrorHandler;
import com.bandwidth.proxy.component.netty.LogChannelEventTimeHandler;
import com.bandwidth.proxy.component.netty.PendingChannelReadyHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

@ChannelHandler.Sharable
class SenderChannelInitializer extends ChannelInitializer<SocketChannel> {

    public static final SenderChannelInitializer DEFAULT = new SenderChannelInitializer();

    private SenderChannelInitializer() {
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        if (Config.INSTANCE.isLocal()) {
            pipeline.addLast(LogChannelEventTimeHandler.INSTANCE);
        }
        pipeline.addLast(SenderChannelInBoundHandler.DEFAULT);
        pipeline.addLast(new PendingChannelReadyHandler());
        pipeline.addLast(ChannelErrorHandler.DEFAULT);
    }

}
