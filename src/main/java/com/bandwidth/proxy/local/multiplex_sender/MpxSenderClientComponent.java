/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: MpxSenderClientComponent.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.local.multiplex_sender;


import com.bandwidth.proxy.component.ClientProxySender;
import com.bandwidth.proxy.local.oss.LocalOssComponent;
import com.bandwidth.proxy.base.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ReflectiveChannelFactory;
import io.netty.channel.socket.SocketChannel;

import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 提供 链接复用 的代理链接服务
 * <p>
 * 只维持一个长链接
 *
 * @Date 2020/7/14
 **/
public class MpxSenderClientComponent implements ClientProxySender {
    public final LocalOssComponent oss = new LocalOssComponent(this);
    private final EventLoopGroup group = Env.eventLoopGroup("mpx-proxy-sender", 1);
    private volatile SocketChannel channel;

    public MpxSenderClientComponent() {
        openChannel();
    }

    void openChannel() {
        group.execute(new Runnable() {
            @Override
            public void run() {
                SocketChannel now = MpxSenderClientComponent.this.channel;
                if (now != null && !now.isShutdown()) {
                    return;
                }
                Bootstrap bootstrap = new Bootstrap();
                Env.setOption(bootstrap);
                SocketChannel newChannel = (SocketChannel) bootstrap.channelFactory(new ReflectiveChannelFactory<>(Env.CHANNEL_TYPE))
                        .group(group)
                        .handler(MpxSenderChannelInitializer.DEFAULT)
                        .attr(Env.KEY_PROXY_FOR, new HashMap<>())
                        .attr(Env.KEY_PENDING_MSG, new ConcurrentLinkedQueue<>())
                        .attr(Env.KEY_COMPONENT, MpxSenderClientComponent.this)
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5 * 1000)
                        .option(ChannelOption.SO_SNDBUF, 16 * 1024)
                        .option(ChannelOption.SO_RCVBUF, 32 * 1024)
                        .option(ChannelOption.AUTO_READ, true)
                        .option(ChannelOption.TCP_NODELAY, true)
                        .connect(Config.INSTANCE.serverHost, Config.INSTANCE.serverPort)
                        .addListener(MpxChannelEstablishListener.DEFAULT)
                        .channel();
                MpxSenderClientComponent.this.channel = newChannel;
            }
        });
    }

    @Override
    public void write(InnerMsg msg) {
        group.execute(new Runnable() {
            @Override
            public void run() {
                SocketChannel now = channel;
                Queue<Object> queue = now.attr(Env.KEY_PENDING_MSG).get();
                ProxyMsg pm = msg.msg;
                HashMap<Number, SocketChannel> set = now.attr(Env.KEY_PROXY_FOR).get();
                if (pm.ope == Protocol.OPE_CONNECT || pm.ope == Protocol.OPE_RESOLV_DNS) {
                    set.put(pm.id, msg.src);
                }
                queue.add(pm);
                now.flush();
            }
        });
    }
}
