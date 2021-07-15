/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: LogChannelEventTimeHandler.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.component.netty;

import com.bandwidth.proxy.base.Env;
import com.bandwidth.proxy.base.LogUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

@ChannelHandler.Sharable
public class LogChannelEventTimeHandler extends ChannelDuplexHandler {
    public static final LogChannelEventTimeHandler INSTANCE = new LogChannelEventTimeHandler();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Number id = ctx.channel().attr(Env.KEY_ID).get();
        LogUtils.debug("in,id:" + id + ",size:" + ((ByteBuf) msg).readableBytes());
        super.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        Number id = ctx.channel().attr(Env.KEY_ID).get();
        LogUtils.debug("out,id:" + id + ",size:" + ((ByteBuf) msg).readableBytes());
        super.write(ctx, msg, promise);
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        LogUtils.debug("channel flush fired");
        super.flush(ctx);
    }

}
