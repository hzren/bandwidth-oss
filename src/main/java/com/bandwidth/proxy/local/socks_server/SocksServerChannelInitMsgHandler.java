/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: SocksServerChannelInitMsgHandler.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.local.socks_server;

import com.bandwidth.proxy.base.LogUtils;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.socksx.v5.DefaultSocks5InitialRequest;
import io.netty.handler.codec.socksx.v5.DefaultSocks5InitialResponse;
import io.netty.handler.codec.socksx.v5.Socks5InitialRequestDecoder;

/**
  * @Date 2020/7/13
 **/
@ChannelHandler.Sharable
class SocksServerChannelInitMsgHandler extends ChannelInboundHandlerAdapter {

    public static final SocksServerChannelInitMsgHandler DEFAULT = new SocksServerChannelInitMsgHandler();

    private SocksServerChannelInitMsgHandler() {
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        DefaultSocks5InitialRequest request = (DefaultSocks5InitialRequest) msg;
        LogUtils.debug("client socks5 init req received");
        ctx.writeAndFlush(new DefaultSocks5InitialResponse(request.authMethods().get(0)));
        ctx.pipeline().remove(Socks5InitialRequestDecoder.class);
        ctx.pipeline().remove(this);
    }
}
