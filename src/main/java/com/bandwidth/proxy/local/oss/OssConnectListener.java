/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: OssConnectListener.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.local.oss;

import com.bandwidth.proxy.base.LogUtils;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.socket.SocketChannel;

/**
 * @Date 2021/4/27
 **/

class OssConnectListener implements ChannelFutureListener {
    private final LocalOssComponent oss;

    public OssConnectListener(LocalOssComponent oss) {
        this.oss = oss;
    }

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        if (future.isSuccess()) {
            LogUtils.debug("oss connect ok...");
            future.channel().closeFuture().addListener(new OssCloseListener(oss));
            oss.channels.add((SocketChannel) future.channel());
        } else {
            LogUtils.err("oss connect fail", future.cause());
        }
    }
}
