/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: PendingChannelReadyHandler.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.component.netty;

import com.bandwidth.proxy.base.Env;
import com.bandwidth.proxy.base.LogUtils;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.socket.SocketChannel;

import java.util.Queue;


public class PendingChannelReadyHandler extends ChannelDuplexHandler {
    private Queue<Object> pending;
    private SocketChannel channel;
    private boolean active = false;
    private Number id;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.active = true;
        this.channel = (SocketChannel) ctx.channel();
        this.pending = channel.attr(Env.KEY_PENDING_MSG).get();
        this.id = channel.attr(Env.KEY_ID).get();
        writePending(ctx);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        pending.add(msg);
        flush(ctx);
    }

    private void writePending(ChannelHandlerContext ctx) throws Exception {
        LogUtils.debug("f pend,id:" + id + ",num:" + pending.size());
        for (Object each = pending.poll(); each != null; each = pending.poll()) {
            super.write(ctx, each, channel.voidPromise());
        }
        super.flush(ctx);
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        if (active) {
            writePending(ctx);
        }
    }
}
