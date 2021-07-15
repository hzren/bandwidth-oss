/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: ServerStartupListener.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.component.netty.start;

import com.bandwidth.proxy.base.LogUtils;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

public class ServerStartupListener implements ChannelFutureListener {

    public static final ServerStartupListener DEFAULT = new ServerStartupListener();

    private ServerStartupListener() {
    }

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        if (!future.isSuccess()) {
            LogUtils.err("Server Startup Fail...", future.cause());
            System.exit(-1);
        } else {
            LogUtils.info("Server Startup OK...");
        }
    }
}
