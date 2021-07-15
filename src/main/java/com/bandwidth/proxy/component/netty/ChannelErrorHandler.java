/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: ChannelErrorHandler.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.component.netty;

import com.bandwidth.proxy.base.LogUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;

/**
 * 出错情况下关闭通道
 */
@ChannelHandler.Sharable
public class ChannelErrorHandler extends ChannelDuplexHandler {

    public static final ChannelErrorHandler DEFAULT = new ChannelErrorHandler();

    private ChannelErrorHandler() {
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Channel channel = ctx.channel();
        LogUtils.err("err,close channel:" + channel.remoteAddress(), cause);
        //channel.close();
    }

}
