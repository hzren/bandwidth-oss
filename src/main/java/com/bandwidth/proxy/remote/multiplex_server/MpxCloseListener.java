/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: MpxCloseListener.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.remote.multiplex_server;

import com.bandwidth.proxy.base.LogUtils;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

/**
 * @Date 2021/4/28
 **/

class MpxCloseListener implements ChannelFutureListener {

    private final Integer id;

    public MpxCloseListener(Integer id) {
        this.id = id;
    }

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        LogUtils.err("mpx closed:" + id, null);
    }
}
