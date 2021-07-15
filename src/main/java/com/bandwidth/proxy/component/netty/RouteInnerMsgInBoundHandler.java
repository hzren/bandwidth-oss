/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: RouteInnerMsgInBoundHandler.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.component.netty;

import com.bandwidth.proxy.base.Env;
import com.bandwidth.proxy.base.InnerMsg;
import com.bandwidth.proxy.base.Protocol;
import com.bandwidth.proxy.base.ProxyMsg;
import com.bandwidth.proxy.local.BaseClientProxyServer;
import com.bandwidth.proxy.local.multiplex_sender.MpxSenderClientComponent;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;

/**
 * @Date 2021/4/7
 **/
@ChannelHandler.Sharable
public class RouteInnerMsgInBoundHandler extends ChannelInboundHandlerAdapter {

    public static final RouteInnerMsgInBoundHandler DEFAULT = new RouteInnerMsgInBoundHandler();

    private RouteInnerMsgInBoundHandler() {
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        SocketChannel channel = (SocketChannel) ctx.channel();
        BaseClientProxyServer server = (BaseClientProxyServer) channel.attr(Env.KEY_COMPONENT).get();
        MpxSenderClientComponent sender = server.sender;
        if (msg instanceof ProxyMsg) {
            sender.write(new InnerMsg(channel, (ProxyMsg) msg));
        } else {
            Number id = channel.attr(Env.KEY_ID).get();
            ProxyMsg pm = new ProxyMsg(Protocol.OPE_DATA, id, (ByteBuf) msg);
            sender.write(new InnerMsg(channel, pm));
        }
    }

}
