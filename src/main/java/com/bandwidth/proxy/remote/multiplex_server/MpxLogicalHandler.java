/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: MpxLogicalHandler.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.remote.multiplex_server;


import com.bandwidth.proxy.base.*;
import com.bandwidth.proxy.local.BaseClientProxyServer;
import com.bandwidth.proxy.remote.client.SenderComponent;
import com.bandwidth.proxy.remote.oss.OssData;
import com.bandwidth.proxy.remote.oss.RemoteOssComponent;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.EventLoop;
import io.netty.channel.internal.ChannelUtils;
import io.netty.channel.socket.SocketChannel;
import io.netty.resolver.AddressResolver;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Queue;

/**
 * @Date 2020/7/13
 **/
class MpxLogicalHandler extends ChannelDuplexHandler {
    private final RemoteOssComponent oss;
    private boolean init = false;
    private SocketChannel channel;
    private HashMap<Number, SocketChannel> map;
    private Integer id;
    private MpxServerComponent component;

    public MpxLogicalHandler(RemoteOssComponent oss) {
        this.oss = oss;
    }

    private void init(ChannelHandlerContext ctx) {
        this.channel = (SocketChannel) ctx.channel();
        this.component = (MpxServerComponent) channel.attr(Env.KEY_COMPONENT).get();
        this.id = Env.genId();
        this.channel.attr(Env.KEY_ID).set(id);
        this.channel.closeFuture().addListener(new MpxCloseListener(id));
        this.map = new HashMap<>();
        this.channel.attr(Env.KEY_PROXY_FOR).set(this.map);

        ProxyMsg pm = new ProxyMsg(Protocol.OPE_MPX_ID, id, Unpooled.EMPTY_BUFFER);
        write(ctx, pm, channel.voidPromise());
        LogUtils.debug("mpx connect:" + id);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!init) {
            init(ctx);
            this.init = true;
        }
        ProxyMsg proxyMsg = (ProxyMsg) msg;
        Long id = MsgUtils.proxyServerClientId(this.id, (Integer) proxyMsg.id);
        switch (proxyMsg.ope) {
            case Protocol.OPE_CONNECT:
                SenderComponent sender = component.sender;
                ProxyMsg pm = new ProxyMsg(Protocol.OPE_CONNECT, id, proxyMsg.msg);
                SocketChannel target = map.get(id);
                if (target != null){
                    target.close();
                }
                try {
                    target = sender.openChannel(new InnerMsg(channel, pm));
                    map.put(id, target);
                } catch (Exception e) {
                    LogUtils.err("connect http fail", e);
                    write(ctx, new ProxyMsg(Protocol.OPE_CLOSE, id, Unpooled.EMPTY_BUFFER), channel.voidPromise());
                }
                break;
            case Protocol.OPE_DATA:
                target = map.get(id);
                if (target != null) {
                    LogUtils.debug("forward msg to:" + id);
                    Queue<Object> pending = target.attr(Env.KEY_PENDING_MSG).get();
                    pending.add(proxyMsg.msg);
                    //确保写完队列，channel仍不活跃，如果活跃，触发flush
                    target.flush();
                } else {
                    proxyMsg.msg.release();
                }
                break;
            case Protocol.OPE_CLOSE:
                proxyMsg.msg.release();
                target = map.get(id);
                if (target != null) {
                    target.close();
                }
                break;
            case Protocol.OPE_HB:
                LogUtils.debug("rev hb msg");
                proxyMsg.msg.release();
                break;
            case Protocol.OPE_RESOLV_DNS:
                resolveIp(ctx, proxyMsg);
                break;
            default:
                proxyMsg.msg.release();
                LogUtils.info("不支持的消息操作类型:" + proxyMsg.ope);
        }
    }

    private void resolveIp(ChannelHandlerContext ctx, ProxyMsg rm) {
        String uri = rm.msg.readCharSequence(rm.msg.readableBytes(), StandardCharsets.US_ASCII).toString();
        LogUtils.debug("resolv dns:" + uri);
        rm.msg.release();
        String[] subs = uri.split(":");
        try {
            final EventLoop eventLoop = channel.eventLoop();
            AddressResolver<InetSocketAddress> resolver;
            try {
                resolver = BaseClientProxyServer.resolverGroup.getResolver(eventLoop);
            } catch (Throwable cause) {
                resloveComplete(ctx, rm.id, subs[0], null);
                return;
            }
            InetSocketAddress address = InetSocketAddress.createUnresolved(subs[0], Integer.valueOf(subs[1]));
            final Future<InetSocketAddress> resolveFuture = resolver.resolve(address);

            if (resolveFuture.isDone()) {
                final Throwable resolveFailureCause = resolveFuture.cause();
                if (resolveFailureCause != null) {
                    resloveComplete(ctx, rm.id, subs[0], null);
                } else {
                    // Succeeded to resolve immediately; cached? (or did a blocking lookup)
                    resloveComplete(ctx, rm.id, subs[0], resolveFuture.getNow());
                }
                return;
            }

            // Wait until the name resolution is finished.
            resolveFuture.addListener(new FutureListener<SocketAddress>() {
                @Override
                public void operationComplete(Future<SocketAddress> future) throws Exception {
                    if (future.cause() != null) {
                        resloveComplete(ctx, rm.id, subs[0], null);
                    } else {
                        resloveComplete(ctx, rm.id, subs[0], resolveFuture.getNow());
                    }
                }
            });
        } catch (Throwable cause) {
            resloveComplete(ctx, rm.id, subs[0], null);
        }
    }

    private void resloveComplete(ChannelHandlerContext ctx, Number id, String host, InetSocketAddress address) {
        ByteBuf data;
        if (address == null) {
            data = Unpooled.EMPTY_BUFFER;
        } else {
            String ip = address.getAddress().getHostAddress();
            LogUtils.info("dns parse," + host + "->" + ip);
            data = MsgUtils.buildConnectMsgBody(ip, address.getPort());
        }
        write(ctx, new ProxyMsg(Protocol.OPE_RESOLV_DNS, id, data), channel.voidPromise());
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        if (msg instanceof CloseMsg) {
            CloseMsg cm = (CloseMsg) msg;
            SocketChannel channel = map.remove(cm.id);
            if (channel == cm.channel){
                msg = new ProxyMsg(Protocol.OPE_CLOSE, cm.id, Unpooled.EMPTY_BUFFER);
            }else {
                map.put(cm.id, channel);
                return;
            }
        }

        ProxyMsg pm = (ProxyMsg) msg;
        if (pm.ope == Protocol.OPE_DATA || pm.ope == Protocol.OPE_CLOSE) {
            Integer[] ids = MsgUtils.proxyChannelId((Long) (pm.id));
            oss.upload(new OssData(new ProxyMsg(pm.ope, ids[1], pm.msg), channel, id));
        } else {
            try {
                super.write(ctx, MsgUtils.encode(pm), promise);
                super.flush(ctx);
            } catch (Exception e) {
                LogUtils.err("write fail", e);
                throw new RuntimeException(e);
            }
        }
    }

}
