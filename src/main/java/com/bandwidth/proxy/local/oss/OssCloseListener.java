/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: OssCloseListener.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.local.oss;

import com.bandwidth.proxy.base.LogUtils;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

/**
 * @Date 2021/4/28
 **/

class OssCloseListener implements ChannelFutureListener {
    private final LocalOssComponent oss;

    public OssCloseListener(LocalOssComponent oss) {
        this.oss = oss;
    }

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        LogUtils.err("oss channel close, reconnect", future.cause());
        oss.connectOss(false);
    }
}
