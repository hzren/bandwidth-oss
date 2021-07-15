/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: Env.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.base;

import io.netty.bootstrap.AbstractBootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class Env implements Attributes {
    public static final int MAX_LEN = 30 * 1024 * 1024;
    public static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    public static final Class SERVER_TYPE;
    public static final Class CHANNEL_TYPE;
    /**
     * 是否是测试级别
     */
    public static final ResourceLeakDetector.Level LEVEL = ResourceLeakDetector.Level.DISABLED;
    //是否是windows
    public static final boolean isWin;
    private static final AtomicInteger ID_GEN = new AtomicInteger();

    static {
        String os = System.getProperty("os.name").toLowerCase();
        isWin = os.contains("windows");
        System.out.println("will user epoll:" + !isWin);
        SERVER_TYPE = isWin ? NioServerSocketChannel.class : EpollServerSocketChannel.class;
        CHANNEL_TYPE = isWin ? NioSocketChannel.class : EpollSocketChannel.class;
    }

    public static final Integer genId() {
        return ID_GEN.getAndIncrement();
    }

    public static final EventLoopGroup eventLoopGroup(String name, int num) {
        if (isWin) {
            NioEventLoopGroup group = new NioEventLoopGroup(num, new DefaultThreadFactory(name));
            group.setIoRatio(30);
            return group;
        }
        return new EpollEventLoopGroup(num, new DefaultThreadFactory(name));
    }

    public static final void setOption(AbstractBootstrap bootstrap) {
        if (!isWin) {
            if (bootstrap instanceof ServerBootstrap) {
                bootstrap.option(EpollChannelOption.TCP_FASTOPEN, 3)
                        .option(EpollChannelOption.EPOLL_MODE, EpollMode.EDGE_TRIGGERED);
                ServerBootstrap sb = (ServerBootstrap) bootstrap;
                sb.childOption(EpollChannelOption.TCP_KEEPIDLE, 60)
                        .childOption(EpollChannelOption.TCP_KEEPINTVL, 60)
                        .childOption(EpollChannelOption.TCP_KEEPCNT, 10)
                        .childOption(EpollChannelOption.EPOLL_MODE, EpollMode.EDGE_TRIGGERED);
            } else {
                bootstrap.option(EpollChannelOption.TCP_KEEPIDLE, 60)
                        .option(EpollChannelOption.TCP_KEEPINTVL, 60)
                        .option(EpollChannelOption.TCP_KEEPCNT, 10)
                        .option(ChannelOption.TCP_FASTOPEN_CONNECT, true)
                        .option(EpollChannelOption.EPOLL_MODE, EpollMode.EDGE_TRIGGERED);
            }
        }
    }
}
