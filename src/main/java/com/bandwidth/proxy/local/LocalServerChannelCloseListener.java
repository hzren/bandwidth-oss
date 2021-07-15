/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: LocalServerChannelCloseListener.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.local;


import com.bandwidth.proxy.base.*;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.socket.SocketChannel;


/**
 * @Date 2021/4/27
 **/

public class LocalServerChannelCloseListener implements ChannelFutureListener {

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        SocketChannel channel = (SocketChannel) future.channel();
        Integer id = (Integer) channel.attr(Env.KEY_ID).get();
        BaseClientProxyServer server = (BaseClientProxyServer) channel.attr(Env.KEY_COMPONENT).get();
        server.sender.write(new InnerMsg(channel, new ProxyMsg(Protocol.OPE_CLOSE, id, Unpooled.EMPTY_BUFFER)));
        LogUtils.info("local client closed,id:" + id);
    }
}
