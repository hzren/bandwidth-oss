/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: LocalOssComponent.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.local.oss;

import com.bandwidth.proxy.base.Config;
import com.bandwidth.proxy.base.Env;
import com.bandwidth.proxy.base.LogUtils;
import com.bandwidth.proxy.base.OssUtil;
import com.bandwidth.proxy.local.multiplex_sender.MpxSenderClientComponent;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ReflectiveChannelFactory;
import io.netty.channel.socket.SocketChannel;

import java.util.LinkedList;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @Date 2021/4/25
 **/

public class LocalOssComponent {
    final LinkedList<SocketChannel> channels = new LinkedList<>();
    final LinkedList<DownloadMsg> queue = new LinkedList<>();
    private final EventLoopGroup group = Env.eventLoopGroup("oss-download", 1);
    private final MpxSenderClientComponent sender;

    public LocalOssComponent(MpxSenderClientComponent sender) {
        this.sender = sender;
        Runnable task = new Runnable() {
            @Override
            public void run() {
                connectOss(true);
            }
        };
        Future future = group.submit(task);
        while (!future.isDone()) {
            LogUtils.err(null, "等待oss链接建立完成。。。");
            try {
                Thread.sleep(100l);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        LogUtils.err(null, "oss链接建立完成。。。");
        for (int i = 1; i < 5; i++) {
            group.schedule(new Runnable() {
                @Override
                public void run() {
                    connectOss(false);
                }
            }, i * 1000, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 启动
     * 1. 连接到oss外网域名
     * 2. 保持心跳，发送默认请求保持链接不死，获取一个默认空文件
     */
    void connectOss(boolean wait) {
        Bootstrap bootstrap = new Bootstrap();
        Env.setOption(bootstrap);
        ChannelFuture future = bootstrap.channelFactory(new ReflectiveChannelFactory<>(Env.CHANNEL_TYPE))
                .group(group)
                .handler(new LocalOssChannelInitializer(this.sender, this))
                .attr(OssUtil.KEY_DM_MSG, queue)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5 * 1000)
                .option(ChannelOption.SO_SNDBUF, 4 * 1024)
                .option(ChannelOption.SO_RCVBUF, 16 * 1024)
                .option(ChannelOption.AUTO_READ, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .connect(Config.INSTANCE.ucloudOssPublicHost, 80)
                .addListener(new OssConnectListener(this));
        if (wait) {
            try {
                future.sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    void flush() {
        //只使用第一个可用链接下载文件
        for (SocketChannel channel : channels) {
            channel.flush();
        }
    }

    public void download(String name, SocketChannel src, int size) {
        LogUtils.debug("add download to queue:" + name);
        group.execute(new Runnable() {
            @Override
            public void run() {
                queue.add(new DownloadMsg(name, src, size));
                LogUtils.debug("oss download size:" + queue.size());
                flush();
            }
        });
    }

}
