/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: OssLogicalHandler.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.remote.oss;


import com.bandwidth.proxy.base.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.*;

import java.util.LinkedList;

/**
 * fileId 为 0 代表当前无HTTP请求在处理中
 * <p>
 * HTTP 1.1 pipelined 不支持非幂等方法，也就是对于POST,DELETE 之类方法，不能使用用管线技术；
 * 这就要求，必须在收到上个HTTP请求的全部响应后再发送下个HTTP请求。
 * <p>
 * 流程：
 * 1. channel active。 发送一次，直接提交
 * 2. 100ms提交一次
 *
 * @Date 2021/4/25
 **/
class OssLogicalHandler extends ChannelDuplexHandler {
    public static final Integer EVENT_POLL = Integer.valueOf(-1);
    public static final Integer EVENT_COMMIT = Integer.valueOf(-2);

    private RemoteOssComponent component;
    private LinkedList<Object> queue;
    private SocketChannel channel;

    private boolean active = false;
    private OssUpload ossUpload;
    private long reqTime;

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        this.queue = (LinkedList<Object>) ctx.channel().attr(Env.KEY_PENDING_MSG).get();
        this.component = (RemoteOssComponent) ctx.channel().attr(Env.KEY_COMPONENT).get();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.active = true;
        this.channel = (SocketChannel) ctx.channel();
        startRequest(ctx, null);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        this.component.channels.remove(ctx.channel());
        this.active = false;
        if (ossUpload != null) {
            component.reschedule(ossUpload);
            this.ossUpload = null;
        }
        super.channelInactive(ctx);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (!active) {
            return;
        }
        if (msg == EVENT_POLL) {
            if (ossUpload == null) {
                startRequest(ctx, null);
            } else {
                return;
            }
        } else if (msg == EVENT_COMMIT) {
            commit(ctx);
        } else {
            ByteBuf bb = (ByteBuf) msg;
            ctx.writeAndFlush(new DefaultHttpContent(bb));
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpResponse) {
            HttpResponse rsp = (HttpResponse) msg;
            int code = rsp.status().code();
            long now = System.currentTimeMillis();
            LogUtils.debug("oss upload,code:" + code + ",name:" + ossUpload.name + ",size:" + ossUpload.totalSize + ",total:" + (now - reqTime));
            OssUpload tmp = this.ossUpload;
            this.ossUpload = null;
            if (code == 200) {
                if (!this.queue.remove(tmp)) {
                    LogUtils.err("remove upload from queue fail", new RuntimeException());
                    throw new Error();
                }
                tmp.fileChannel.close();
                tmp.file.delete();
                startRequest(ctx, null);
            } else {
                component.reschedule(tmp);
            }
        } else if (msg instanceof HttpContent) {
            ((HttpContent) msg).release();
        }
    }

    private void startRequest(ChannelHandlerContext ctx, OssUpload tmp) {
        if (this.ossUpload != null) {
            return;
        }
        if (tmp != null) {
            this.ossUpload = tmp;
        } else {
            for (Object o : queue) {
                OssUpload upload = (OssUpload) o;
                if (upload.ossChannel == null) {
                    this.ossUpload = upload;
                    this.ossUpload.ossChannel = this.channel;
                    break;
                }
            }
        }
        if (this.ossUpload == null) {
            return;
        }
        this.reqTime = System.currentTimeMillis();
        String key = ossUpload.name;
        LogUtils.debug("start oss upload:" + key);
        this.reqTime = System.currentTimeMillis();
        DefaultHttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.PUT, "/" + key);
        HttpHeaders headers = request.headers();
        headers.set(HttpHeaderNames.HOST, Config.INSTANCE.ucloudOssPrivateHost)
                .set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE)
                .set(HttpHeaderNames.CACHE_CONTROL, HttpHeaderValues.MAX_AGE.toString() + "=0")
                .set(HttpHeaderNames.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.72 Safari/537.36")
                .set(HttpHeaderNames.ACCEPT_LANGUAGE, "zh-CN,zh;q=0.9")
                .set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED)
                .set("authorization", OssUtil.calcSign(key, HttpMethod.PUT));
        ctx.write(request);
        ctx.writeAndFlush(new DefaultHttpContent(FileUtils.read(this.ossUpload.fileChannel)));
        if (this.ossUpload.fileCommit) {
            commit(ctx);
        }
    }

    private void commit(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(new DefaultLastHttpContent());
        ctx.flush();
    }
}
