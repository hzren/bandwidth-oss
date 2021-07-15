/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: RouteParsedMsgToMpxInboundHandler.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.local.oss;

import com.bandwidth.proxy.base.Env;
import com.bandwidth.proxy.component.netty.ChannelErrorHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoop;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

/**
 * @Date 2021/4/30
 **/

class RouteParsedMsgToMpxInboundHandler extends ChannelInboundHandlerAdapter {
    static final AttributeKey<EmbeddedChannel> KEY_EC = AttributeKey.valueOf("EmbeddedChannel");

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        final DownloadData now = (DownloadData) msg;
        Attribute<EmbeddedChannel> attr = now.src.attr(KEY_EC);
        EmbeddedChannel embeddedChannel = attr.get();
        if (embeddedChannel == null) {
            embeddedChannel = new EmbeddedChannel();
            ChannelPipeline pipeline = embeddedChannel.pipeline();
            pipeline.addLast(new LengthFieldBasedFrameDecoder(Env.MAX_LEN, 0, 4, 0, 4));
            pipeline.addLast(new ChannelInboundHandlerAdapter() {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                    ByteBuf bb = (ByteBuf) msg;
                    fireSrcChannelRead(bb, now.src);
                }
            }, ChannelErrorHandler.DEFAULT);
            now.src.attr(KEY_EC).setIfAbsent(embeddedChannel);
        }
        embeddedChannel.writeOneInbound(now.data);
        embeddedChannel.flushInbound();
    }

    private void fireSrcChannelRead(ByteBuf bb, SocketChannel src) {
        EventLoop eventLoop = src.eventLoop();
        eventLoop.execute(new Runnable() {
            @Override
            public void run() {
                ChannelPipeline pipeline = src.pipeline();
                pipeline.context(LengthFieldBasedFrameDecoder.class).fireChannelRead(bb);
            }
        });
    }
}
