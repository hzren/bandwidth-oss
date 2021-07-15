/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: BaseClientProxyServer.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.local;

import com.bandwidth.proxy.base.DnsUtils;
import com.bandwidth.proxy.base.Env;
import com.bandwidth.proxy.local.multiplex_sender.MpxSenderClientComponent;
import io.netty.channel.EventLoopGroup;
import io.netty.resolver.dns.DnsAddressResolverGroup;

/**
 * @Date 2021/4/7
 **/

public abstract class BaseClientProxyServer {
    public static final EventLoopGroup group = Env.eventLoopGroup("local-proxy-server", 1);
    public static final DnsAddressResolverGroup resolverGroup = DnsUtils.resolverGroup(group.next());

    public final MpxSenderClientComponent sender;

    public BaseClientProxyServer(MpxSenderClientComponent sender) {
        this.sender = sender;
        start();
    }

    public abstract void start();
}
