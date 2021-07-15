/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: MpxChannelEstablishListener.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.local.multiplex_sender;

import com.bandwidth.proxy.base.Env;
import com.bandwidth.proxy.base.LogUtils;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.socket.SocketChannel;

/**
 * @Date 2020/12/10
 **/

class MpxChannelEstablishListener implements ChannelFutureListener {

    public static final MpxChannelEstablishListener DEFAULT = new MpxChannelEstablishListener();

    private MpxChannelEstablishListener() {

    }

    @Override
    public void operationComplete(ChannelFuture future) {
        SocketChannel channel = (SocketChannel) future.channel();
        MpxSenderClientComponent component = (MpxSenderClientComponent) channel.attr(Env.KEY_COMPONENT).get();
        if (future.isSuccess()) {
            channel.closeFuture().addListener(new MpxSenderChannelCloseListener());
            LogUtils.debug("mpx channel connected");
        } else {
            LogUtils.err(channel, "server connect fail-" + future.cause());
            component.openChannel();
        }
    }
}
