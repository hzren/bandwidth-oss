/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: SenderChannelInBoundHandler.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.remote.client;

import com.bandwidth.proxy.base.Env;
import com.bandwidth.proxy.base.LogUtils;
import com.bandwidth.proxy.base.Protocol;
import com.bandwidth.proxy.base.ProxyMsg;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;


/**
 * @Date 2021/4/27
 **/
@ChannelHandler.Sharable
class SenderChannelInBoundHandler extends ChannelInboundHandlerAdapter {

    public static final SenderChannelInBoundHandler DEFAULT = new SenderChannelInBoundHandler();

    private SenderChannelInBoundHandler() {
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        SocketChannel channel = (SocketChannel) ctx.channel();
        Long id = (Long) channel.attr(Env.KEY_ID).get();
        ProxyMsg pm = new ProxyMsg(Protocol.OPE_DATA, id, (ByteBuf) msg);
        SocketChannel proxy = channel.attr(Env.KEY_SRC).get();
        LogUtils.debug("write msg to mpx,id:" + id + ",size:" + ((ByteBuf) msg).readableBytes());
        proxy.writeAndFlush(pm);
    }
}
