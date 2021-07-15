/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: SocksServerComponent.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.local.socks_server;

import com.bandwidth.proxy.base.Config;
import com.bandwidth.proxy.base.Env;
import com.bandwidth.proxy.component.netty.start.ServerStartupListener;
import com.bandwidth.proxy.local.BaseClientProxyServer;
import com.bandwidth.proxy.local.multiplex_sender.MpxSenderClientComponent;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;

public class SocksServerComponent extends BaseClientProxyServer {

    public SocksServerComponent(MpxSenderClientComponent sender) {
        super(sender);
    }

    @Override
    public void start() {
        ServerBootstrap b = new ServerBootstrap();
        Env.setOption(b);
        EventLoopGroup group = Env.eventLoopGroup("socks-server", 1);
        b.group(group, group)
                .channel(Env.SERVER_TYPE)
                .option(ChannelOption.AUTO_READ, true)
                .childHandler(SocksServerChannelInitializer.DEFAULT)
                .option(ChannelOption.AUTO_READ, true)
                .childOption(ChannelOption.ALLOW_HALF_CLOSURE, false)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, false)
                .childOption(ChannelOption.SO_SNDBUF, 64 * 1024)
                .childOption(ChannelOption.SO_RCVBUF, 64 * 1024)
                .childAttr(Env.KEY_COMPONENT, this)
                .bind("127.0.0.1", Config.INSTANCE.localSocksPort)
                .addListener(ServerStartupListener.DEFAULT);
    }


}
