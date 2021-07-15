/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: MpxServerChannelInitializer.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.remote.multiplex_server;

import com.bandwidth.proxy.base.Config;
import com.bandwidth.proxy.component.netty.ChannelErrorHandler;
import com.bandwidth.proxy.component.netty.LogChannelEventTimeHandler;
import com.bandwidth.proxy.component.netty.ssl.VerifySecretKeyHandler;
import com.bandwidth.proxy.remote.oss.RemoteOssComponent;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;


@ChannelHandler.Sharable
class MpxServerChannelInitializer extends ChannelInitializer<SocketChannel> {
    private final RemoteOssComponent oss;

    public MpxServerChannelInitializer(RemoteOssComponent oss) {
        this.oss = oss;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        if (Config.INSTANCE.isLocal()) {
            pipeline.addLast(LogChannelEventTimeHandler.INSTANCE);
        }
        pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
        pipeline.addLast(new VerifySecretKeyHandler());
        pipeline.addLast(new MpxLogicalHandler(oss));
        pipeline.addLast(ChannelErrorHandler.DEFAULT);
    }


}
