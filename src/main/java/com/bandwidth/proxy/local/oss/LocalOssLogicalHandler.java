/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: LocalOssLogicalHandler.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.local.oss;


import com.bandwidth.proxy.base.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.Attribute;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

/**
 * @Date 2021/4/27
 **/

class LocalOssLogicalHandler extends ChannelDuplexHandler {
    private static int gen = 0;

    private final int ossId = gen++;
    private final LocalOssComponent oss;
    private boolean active;
    private DownloadMsg dm;

    private boolean rspOk;
    private long len;
    private long startTime;

    public LocalOssLogicalHandler(LocalOssComponent oss) {
        this.oss = oss;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.active = true;
        startDownloadRequest(ctx);
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        super.flush(ctx);
        if (active) {
            startDownloadRequest(ctx);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        this.active = false;
        if (this.dm != null) {
            this.dm.download = DownloadMsg.NOT_START;
            this.dm.datas.clear();
            oss.flush();
        }
        super.channelInactive(ctx);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg == Unpooled.EMPTY_BUFFER) {
            startDownloadRequest(ctx);
        } else {
            LogUtils.err("unknown msg:" + msg.getClass().getSimpleName(), new RuntimeException());
        }
    }

    private void startDownloadRequest(ChannelHandlerContext ctx) {
        if (this.dm != null) {
            return;
        }
        for (DownloadMsg downloadMsg : oss.queue) {
            if (downloadMsg.download == DownloadMsg.NOT_START) {
                this.dm = downloadMsg;
                this.dm.download = DownloadMsg.DOWNLOADING;
                break;
            }
        }
        if (this.dm == null) {
            return;
        }
        this.startTime = System.currentTimeMillis();
        String url = "/" + this.dm.key;
        DefaultHttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, url);
        HttpHeaders headers = request.headers();
        headers.set(HttpHeaderNames.HOST, Config.INSTANCE.ucloudOssPublicHost)
                .set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE)
                .set(HttpHeaderNames.CACHE_CONTROL, HttpHeaderValues.MAX_AGE.toString() + "=0")
                .set(HttpHeaderNames.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.72 Safari/537.36")
                .set(HttpHeaderNames.ACCEPT_LANGUAGE, "zh-CN,zh;q=0.9")
                .set("authorization", OssUtil.calcSign(this.dm.key, HttpMethod.GET));
        ctx.write(request);
        ctx.write(new DefaultLastHttpContent());
        ctx.flush();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpResponse) {
            processFullRsp(ctx, (FullHttpResponse) msg);
            return;
        }
        LogUtils.debug(this.dm.key + "-" + ossId + "-msg type:" + msg.getClass().getSimpleName());
        if (msg instanceof HttpResponse) {
            DefaultHttpResponse rsp = (DefaultHttpResponse) msg;
            int code = rsp.status().code();
            String size = rsp.headers().get(HttpHeaderNames.CONTENT_LENGTH.toString());
            this.len = Long.parseLong(size);
            LogUtils.debug("oss download rsp,key:" + this.dm.key + ";code:" + code + ",len:" + size);
            this.rspOk = code == 200;
        } else if (msg instanceof HttpContent) {
            DefaultHttpContent content = (DefaultHttpContent) msg;
            LogUtils.debug(this.dm.key + "-" + ossId + "-msg len:" + content.content().readableBytes());
            if (rspOk) {
                this.dm.datas.addLast(content.content());
                boolean last = content instanceof LastHttpContent;
                if (last) {
                    long cost = System.currentTimeMillis() - startTime;
                    LogUtils.debug("download oss ok,file:" + dm.key + ",size:" + this.len + ",time:" + cost + ",speed:" + (this.len / cost));
                    this.dm.download = DownloadMsg.OK;
                }
                fireData(ctx);
                if (last) {
                    reset(true);
                    startDownloadRequest(ctx);
                }
            } else {
                content.release();
                if (msg instanceof LastHttpContent) {
                    this.dm.download = DownloadMsg.NOT_START;
                    reset(false);
                    ctx.executor().schedule(new Runnable() {
                        @Override
                        public void run() {
                            startDownloadRequest(ctx);
                        }
                    }, 25, TimeUnit.MILLISECONDS);
                }
            }
        }
    }

    private void processFullRsp(ChannelHandlerContext ctx, FullHttpResponse rsp) {
        int code = rsp.status().code();
        this.rspOk = code == 200;
        String size = rsp.headers().get(HttpHeaderNames.CONTENT_LENGTH.toString());
        this.len = Long.parseLong(size);
        LogUtils.debug("oss download rsp,key:" + this.dm.key + ";code:" + code + ",len:" + size);

        if (rspOk) {
            this.dm.datas.addLast(rsp.content());
            long cost = System.currentTimeMillis() - startTime;
            LogUtils.debug("download oss ok,file:" + dm.key + ",size:" + this.len + ",time:" + cost + ",speed:" + (this.len / cost));
            this.dm.download = DownloadMsg.OK;
            fireData(ctx);
            reset(true);
            startDownloadRequest(ctx);
        } else {
            rsp.release();
            this.dm.download = DownloadMsg.NOT_START;
            reset(false);
            ctx.executor().schedule(new Runnable() {
                @Override
                public void run() {
                    startDownloadRequest(ctx);
                }
            }, 25, TimeUnit.MILLISECONDS);
        }
    }

    private void fireData(ChannelHandlerContext ctx) {
        SocketChannel src = this.dm.src;
        LinkedList<DownloadMsg> sameSrcs = new LinkedList<>();
        for (DownloadMsg downloadMsg : oss.queue) {
            if (downloadMsg.src == src) {
                sameSrcs.add(downloadMsg);
            }
        }
        CompositeByteBuf cbb = Unpooled.compositeBuffer();
        boolean srcDataEnd = false;
        l0:
        for (; ; ) {
            DownloadMsg dm = sameSrcs.poll();
            if (dm == null) {
                break;
            }
            switch (dm.download) {
                case DownloadMsg.OK:
                    for (ByteBuf data : dm.datas) {
                        cbb.addComponent(true, data);
                    }
                    dm.datas.clear();
                    oss.queue.remove(dm);
                    srcDataEnd = true;
                    continue l0;
                case DownloadMsg.DOWNLOADING:
                    for (ByteBuf data : dm.datas) {
                        cbb.addComponent(true, data);
                    }
                    dm.datas.clear();
                    srcDataEnd = false;
                    break l0;
                default:
                    break l0;
            }
        }
        ctx.fireChannelRead(new DownloadData(src, cbb));
        if (srcDataEnd) {
            Attribute<EmbeddedChannel> attribute = src.attr(RouteParsedMsgToMpxInboundHandler.KEY_EC);
            EmbeddedChannel ec = attribute.getAndSet(null);
            ec.finishAndReleaseAll();
        }
    }

    private void reset(boolean clean) {
        if (clean) {
            final String name = this.dm.key;
            Env.executor.execute(() -> OssTool.delFile(name));
        }
        this.dm = null;
        this.rspOk = false;
        this.len = 0;
    }

}
