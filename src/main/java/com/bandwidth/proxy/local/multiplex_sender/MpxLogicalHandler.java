/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: MpxLogicalHandler.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.local.multiplex_sender;


import com.bandwidth.proxy.base.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.socket.SocketChannel;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * @Date 2020/12/10
 **/
class MpxLogicalHandler extends ChannelDuplexHandler {

    private MpxSenderClientComponent sender;
    private HashMap<Number, SocketChannel> map;
    private SocketChannel channel;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        SocketChannel channel = (SocketChannel) ctx.channel();
        this.channel = channel;
        this.sender = (MpxSenderClientComponent) channel.attr(Env.KEY_COMPONENT).get();
        this.map = channel.attr(Env.KEY_PROXY_FOR).get();
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ProxyMsg rm = MsgUtils.parseProxyMsg((ByteBuf) msg);
        LogUtils.debug("read pm msg,id:" + rm.id + ",ope:" + rm.ope);
        switch (rm.ope) {
            case Protocol.OPE_CLOSE:
                rm.msg.release();
                SocketChannel target = map.get(rm.id);
                if (target != null) {
                    target.close();
                }
                break;
            case Protocol.OPE_DATA:
                target = map.get(rm.id);
                if (target != null) {
                    target.writeAndFlush(rm.msg);
                } else {
                    rm.msg.release();
                }
                break;
            case Protocol.OPE_MPX_ID:
                Integer mpxId = (int) rm.id;
                LogUtils.info("set mpxId:" + mpxId);
                channel.attr(Env.KEY_ID).set(mpxId);
                rm.msg.release();
                break;
            case Protocol.OPE_OSS_FILE:
                String name = rm.msg.readCharSequence(rm.msg.readableBytes(), StandardCharsets.ISO_8859_1).toString();
                sender.oss.download(name, channel, (Integer) rm.id);
                rm.msg.release();
                break;
            case Protocol.OPE_RESOLV_DNS:
                target = map.get(rm.id);
                if (target != null) {
                    target.writeAndFlush(rm);
                } else {
                    rm.msg.release();
                }
                break;

            default:
                rm.msg.release();
                LogUtils.err(channel, "不支持的消息操作类型:" + rm.ope);
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        ProxyMsg pm = (ProxyMsg) msg;
        if (pm.ope == Protocol.OPE_CLOSE) {
            SocketChannel target = map.remove(pm.id);
            if (target != null) {
                LogUtils.info("remove from mpx map,id:" + pm.id);
            }
        }
        super.write(ctx, MsgUtils.encode(pm), promise);
        super.flush(ctx);
    }

}
