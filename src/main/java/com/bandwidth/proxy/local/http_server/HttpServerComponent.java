/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: HttpServerComponent.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.local.http_server;

import com.bandwidth.proxy.base.Config;
import com.bandwidth.proxy.base.Env;
import com.bandwidth.proxy.component.netty.start.ServerStartupListener;
import com.bandwidth.proxy.local.BaseClientProxyServer;
import com.bandwidth.proxy.local.multiplex_sender.MpxSenderClientComponent;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;

public class HttpServerComponent extends BaseClientProxyServer {

    public HttpServerComponent(MpxSenderClientComponent component) {
        super(component);
    }

    @Override
    public void start() {
        ServerBootstrap b = new ServerBootstrap();
        Env.setOption(b);
        b.group(group, group)
                .channel(Env.SERVER_TYPE)
                .option(ChannelOption.AUTO_READ, true)
                .childHandler(HttpServerChannelInitializer.DEFAULT)
                .option(ChannelOption.AUTO_READ, true)
                .childOption(ChannelOption.ALLOW_HALF_CLOSURE, false)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, false)
                .childOption(ChannelOption.SO_SNDBUF, 64 * 1024)
                .childOption(ChannelOption.SO_RCVBUF, 64 * 1024)
                .childAttr(Env.KEY_COMPONENT, this)
                .bind("127.0.0.1", Config.INSTANCE.localHttpPort)
                .addListener(ServerStartupListener.DEFAULT);
    }


}
