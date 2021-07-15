/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: SocksServerChannelConnectMsgHandler.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.local.socks_server;


import com.bandwidth.proxy.base.*;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.socksx.v5.*;

import java.nio.charset.StandardCharsets;


/**
 * @Date 2020/7/13
 **/

class SocksServerChannelConnectMsgHandler extends ChannelDuplexHandler {

    private DefaultSocks5CommandRequest request;
    private Integer id;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        this.request = (DefaultSocks5CommandRequest) msg;
        SocketChannel channel = (SocketChannel) ctx.channel();
        this.id = (Integer) channel.attr(Env.KEY_ID).get();
        LogUtils.debug("socks connect command received:" + request.toString());

        Socks5CommandType type = request.type();
        if (type == Socks5CommandType.CONNECT) {
            Socks5AddressType addressType = request.dstAddrType();
            if (addressType == Socks5AddressType.DOMAIN) {
                resolveIp(ctx, id);
            } else if (addressType == Socks5AddressType.IPv4) {
                fire(ctx, request.dstAddr() + ":" + request.dstPort());
            } else {
                fail(ctx, request);
            }
        } else {
            fail(ctx, request);
        }
    }

    private void fail(ChannelHandlerContext ctx, DefaultSocks5CommandRequest request) {
        LogUtils.err("不支持的Socks命令类型:" + request.type().toString() + "-" + request.dstAddr(), null);
        ctx.writeAndFlush(new DefaultSocks5CommandResponse(Socks5CommandStatus.COMMAND_UNSUPPORTED, request.dstAddrType()));
        ctx.channel().close();
    }

    private void fire(ChannelHandlerContext ctx, String ipPort) throws Exception {
        ChannelPipeline pipeline = ctx.pipeline();
        super.channelRead(ctx, new ProxyMsg(Protocol.OPE_CONNECT, id, MsgUtils.buildConnectMsgBody(ipPort)));
        ctx.writeAndFlush(new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS, request.dstAddrType()));
        pipeline.remove(Socks5CommandRequestDecoder.class);
        pipeline.remove(Socks5ServerEncoder.DEFAULT);
        pipeline.remove(this);
    }

    private void resolveIp(ChannelHandlerContext ctx, Integer id) throws Exception {
        super.channelRead(ctx, new ProxyMsg(Protocol.OPE_RESOLV_DNS, id, MsgUtils.buildConnectMsgBody(request.dstAddr(), request.dstPort())));
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        ProxyMsg pm = (ProxyMsg) msg;
        if (pm.msg.readableBytes() == 0) {
            fail(ctx, request);
        } else {
            String ipPort = pm.msg.readCharSequence(pm.msg.readableBytes(), StandardCharsets.US_ASCII).toString();
            fire(ctx, ipPort);
        }
        super.write(ctx, msg, promise);
    }
}
