/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: ChannelConnectListener.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.remote.client;


import com.bandwidth.proxy.base.*;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.socket.SocketChannel;


/**
 * 链接建立失败情况下,关闭另一半
 */
class ChannelConnectListener implements ChannelFutureListener {
    final SenderComponent component;

    public ChannelConnectListener(SenderComponent component) {
        this.component = component;
    }

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        SocketChannel channel = (SocketChannel) future.channel();
        Long id = (Long) channel.attr(Env.KEY_ID).get();
        String uri = channel.attr(Env.KEY_TARGET).get();
        if (future.isSuccess()) {
            channel.closeFuture().addListener(new SenderChannelCloseListener());
            LogUtils.debug("http connect id:" + id + ",uri:" + uri);
        } else {
            MsgUtils.clearPendingMsg(channel);
            SocketChannel proxy = channel.attr(Env.KEY_SRC).get();
            proxy.writeAndFlush(new CloseMsg(id, channel));
            LogUtils.err("http connect fail,id:" + id + ",remote:" + uri, future.cause());
        }
    }
}
