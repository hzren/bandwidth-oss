/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: HttpServerChannelInitializer.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.local.http_server;

import com.bandwidth.proxy.base.Config;
import com.bandwidth.proxy.base.Env;
import com.bandwidth.proxy.base.LogUtils;
import com.bandwidth.proxy.component.netty.ChannelErrorHandler;
import com.bandwidth.proxy.component.netty.LogChannelEventTimeHandler;
import com.bandwidth.proxy.component.netty.RouteInnerMsgInBoundHandler;
import com.bandwidth.proxy.local.LocalServerChannelCloseListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;

@ChannelHandler.Sharable
class HttpServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    public static final HttpServerChannelInitializer DEFAULT = new HttpServerChannelInitializer();

    private HttpServerChannelInitializer() {
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        LogUtils.debug("http client connect");
        ch.attr(Env.KEY_ID).set(Env.genId());
        ch.closeFuture().addListener(new LocalServerChannelCloseListener());

        ChannelPipeline pipeline = ch.pipeline();
        if (Config.INSTANCE.isLocal()) {
            pipeline.addLast(LogChannelEventTimeHandler.INSTANCE);
        }
        pipeline.addLast(new HttpRequestDecoder());
        pipeline.addLast(new HttpServerProxyInitHandler());
        pipeline.addLast(RouteInnerMsgInBoundHandler.DEFAULT);
        pipeline.addLast(ChannelErrorHandler.DEFAULT);
    }
}
