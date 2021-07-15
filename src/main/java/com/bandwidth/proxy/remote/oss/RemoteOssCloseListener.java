/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: RemoteOssCloseListener.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.remote.oss;

import com.bandwidth.proxy.base.LogUtils;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;



class RemoteOssCloseListener implements ChannelFutureListener {
    private final RemoteOssComponent component;

    public RemoteOssCloseListener(RemoteOssComponent component) {
        this.component = component;
    }

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        LogUtils.err("oss close, reconnect", future.cause());
        component.channels.remove(future.channel());
        component.connectOss(false);
    }
}
