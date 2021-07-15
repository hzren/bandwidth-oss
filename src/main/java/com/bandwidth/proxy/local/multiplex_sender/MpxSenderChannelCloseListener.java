/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: MpxSenderChannelCloseListener.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.local.multiplex_sender;

import com.bandwidth.proxy.base.LogUtils;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

/**
 * @Date 2021/4/27
 **/

class MpxSenderChannelCloseListener implements ChannelFutureListener {

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        LogUtils.err("mpx channel closed", future.cause());
    }
}
