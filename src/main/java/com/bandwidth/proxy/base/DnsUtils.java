/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: DnsUtils.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.base;

import io.netty.channel.EventLoop;
import io.netty.channel.ReflectiveChannelFactory;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.resolver.dns.DnsAddressResolverGroup;
import io.netty.resolver.dns.DnsNameResolverBuilder;

import java.util.ArrayList;
import java.util.Arrays;


public class DnsUtils {

    public static final DnsAddressResolverGroup resolverGroup(EventLoop eventLoop) {
        String[] ips = Config.INSTANCE.dnsList.split(",");
        ArrayList<String> ipList = new ArrayList<>(ips.length);
        for (String ip : ips) {
            ip = ip.trim();
            if (ip.length() > 7){
                ipList.add(ip);
            }
        }
        DnsNameResolverBuilder builder = new DnsNameResolverBuilder()
                .eventLoop(eventLoop)
                .channelFactory(new ReflectiveChannelFactory<>(Env.isWin ? NioDatagramChannel.class : EpollDatagramChannel.class))
                .searchDomains(ipList);
        return new DnsAddressResolverGroup(builder);
    }
}
