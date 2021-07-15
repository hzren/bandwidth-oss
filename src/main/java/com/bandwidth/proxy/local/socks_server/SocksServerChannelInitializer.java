/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: SocksServerChannelInitializer.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.local.socks_server;

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
import io.netty.handler.codec.socksx.v5.Socks5CommandRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5InitialRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5ServerEncoder;

@ChannelHandler.Sharable
class SocksServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    public static final SocksServerChannelInitializer DEFAULT = new SocksServerChannelInitializer();

    private SocksServerChannelInitializer() {

    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.closeFuture().addListener(new LocalServerChannelCloseListener());
        LogUtils.debug("socks connect");
        ch.attr(Env.KEY_ID).set(Env.genId());

        ChannelPipeline pipeline = ch.pipeline();
        if (Config.INSTANCE.isLocal()) {
            pipeline.addLast(LogChannelEventTimeHandler.INSTANCE);
        }
        pipeline.addLast(Socks5ServerEncoder.DEFAULT);
        pipeline.addLast(new Socks5InitialRequestDecoder());
        pipeline.addLast(SocksServerChannelInitMsgHandler.DEFAULT);
        pipeline.addLast(new Socks5CommandRequestDecoder());
        pipeline.addLast(new SocksServerChannelConnectMsgHandler());
        pipeline.addLast(RouteInnerMsgInBoundHandler.DEFAULT);
        pipeline.addLast(ChannelErrorHandler.DEFAULT);
    }


}
