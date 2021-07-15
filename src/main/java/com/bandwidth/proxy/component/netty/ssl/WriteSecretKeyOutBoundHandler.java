/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: WriteSecretKeyOutBoundHandler.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.component.netty.ssl;

import com.bandwidth.proxy.base.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

@ChannelHandler.Sharable
public class WriteSecretKeyOutBoundHandler extends ChannelInboundHandlerAdapter {
    public static final WriteSecretKeyOutBoundHandler DEFAULT = new WriteSecretKeyOutBoundHandler();

    private WriteSecretKeyOutBoundHandler() {
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ByteBuf byteBuf = ctx.alloc().buffer(8);
        byteBuf.writeBytes(Config.INSTANCE.secretKey.getBytes());
        LogUtils.debug("write key,length:" + byteBuf.readableBytes());
        ctx.writeAndFlush(MsgUtils.encode(new ProxyMsg(Protocol.OPE_SECRET_KEY, 0, byteBuf)));
        super.channelActive(ctx);
        ctx.pipeline().remove(this);
    }

}
