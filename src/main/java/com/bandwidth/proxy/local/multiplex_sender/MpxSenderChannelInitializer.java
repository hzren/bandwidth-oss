/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: MpxSenderChannelInitializer.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.local.multiplex_sender;

import com.bandwidth.proxy.base.Config;
import com.bandwidth.proxy.base.Env;
import com.bandwidth.proxy.component.netty.ChannelErrorHandler;
import com.bandwidth.proxy.component.netty.LogChannelEventTimeHandler;
import com.bandwidth.proxy.component.netty.PendingChannelReadyHandler;
import com.bandwidth.proxy.component.netty.ssl.WriteSecretKeyOutBoundHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

@ChannelHandler.Sharable
class MpxSenderChannelInitializer extends ChannelInitializer<SocketChannel> {

    public static final MpxSenderChannelInitializer DEFAULT = new MpxSenderChannelInitializer();

    private MpxSenderChannelInitializer() {
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        if (Config.INSTANCE.isLocal()) {
            pipeline.addLast(LogChannelEventTimeHandler.INSTANCE);
        }
        pipeline.addLast(WriteSecretKeyOutBoundHandler.DEFAULT);
        pipeline.addLast(new LengthFieldBasedFrameDecoder(Env.MAX_LEN, 0, 4, 0, 4));
        pipeline.addLast(new MpxLogicalHandler());
        pipeline.addLast(new PendingChannelReadyHandler());
        pipeline.addLast(ChannelErrorHandler.DEFAULT);
    }

}
