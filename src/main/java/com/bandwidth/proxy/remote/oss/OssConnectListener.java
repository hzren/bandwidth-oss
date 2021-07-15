/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: OssConnectListener.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.remote.oss;

import com.bandwidth.proxy.base.LogUtils;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.socket.SocketChannel;

/**
 * @Date 2021/4/25
 **/

class OssConnectListener implements ChannelFutureListener {
    private final RemoteOssComponent component;

    public OssConnectListener(RemoteOssComponent component) {
        this.component = component;
    }

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        if (future.isSuccess()) {
            LogUtils.debug("oss connect..." + future.channel().toString());
            future.channel().closeFuture().addListener(new RemoteOssCloseListener(component));
            component.channels.add((SocketChannel) future.channel());
        } else {
            LogUtils.err("oss connect fail:" + future.channel().toString(), future.cause());
        }
    }
}
