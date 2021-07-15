/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: VerifySecretKeyHandler.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.component.netty.ssl;

import com.bandwidth.proxy.base.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;

import java.util.concurrent.TimeUnit;

public class VerifySecretKeyHandler extends ChannelInboundHandlerAdapter {
    private boolean verify = false;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.executor().schedule(new Runnable() {
            @Override
            public void run() {
                if (!verify) {
                    ctx.close();
                }
            }
        }, 3, TimeUnit.SECONDS);
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ProxyMsg proxyMsg = MsgUtils.parseProxyMsg((ByteBuf) msg);
        if (verify) {
            super.channelRead(ctx, proxyMsg);
            return;
        }
        SocketChannel channel = (SocketChannel) ctx.channel();
        if (proxyMsg.ope != Protocol.OPE_SECRET_KEY || proxyMsg.id.intValue() != 0 || !Config.INSTANCE.secretKey.equals(MsgUtils.readAsString(proxyMsg.msg))) {
            LogUtils.err(channel, "key verify fail...");
            channel.close();
            return;
        }
        verify = true;
    }

}
