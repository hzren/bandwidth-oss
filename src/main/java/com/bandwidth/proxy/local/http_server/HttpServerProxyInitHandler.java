/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: HttpServerProxyInitHandler.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.local.http_server;


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

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

/**
 * 对于HTTP请求,浏览器使用HTTP直接代理模式, 浏览器会把请求头,header直接发送到当前代理服务器上,由代理服务器把数据完整转发出去.
 * 对于同一个TCP链接,浏览器会复用此链接发送直接代理请求, 即在次连接上发送多个代理请求
 * <p>
 * 对于HTTPS请求,浏览器使用CONNECT模式, 现邀请代理服务器链接到目标服务器上, 然后再进行SSL握手操作.
 *
 * @Date 2020/7/13
 **/
class HttpServerProxyInitHandler extends ChannelDuplexHandler {
    private int id;
    private final Object CONNECT_RESPONSE = encodeResponse(response());
    private Boolean httpsMode = null;
    private boolean ipGet;

    private HttpRequest request;
    private LinkedList<HttpContent> body = new LinkedList<>();
    private EmbeddedChannel ch;

    private static DefaultFullHttpResponse response() {
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.EMPTY_BUFFER, true, true);
        return response;
    }

    private static Object encodeResponse(FullHttpResponse response) {
        EmbeddedChannel ch = new EmbeddedChannel(new HttpResponseEncoder());
        ch.writeOutbound(response);
        ch.flushOutbound();
        Object data = ch.readOutbound();
        ch.close();
        return data;
    }

    private ByteBuf encodeRequest(Object request) {
        if (ch == null) {
            ch = new EmbeddedChannel(new HttpRequestEncoder());
        }
        ch.writeOutbound(request);
        ch.flushOutbound();
        Object data = ch.readOutbound();
        return (ByteBuf) data;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        SocketChannel channel = (SocketChannel) ctx.channel();
        this.id = (Integer) channel.attr(Env.KEY_ID).get();
        super.channelActive(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        if (ch != null) {
            ch.finishAndReleaseAll();
        }
        super.channelUnregistered(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest){
            this.ipGet = false;
            this.request = (HttpRequest) msg;
            HttpMethod method = request.method();
            if (method == HttpMethod.CONNECT) {
                processHttps(ctx);
            } else {
                processHttp(ctx);
            }
        }else {
            if (httpsMode) {
                //http connect 请求接受完
                ((LastHttpContent) msg).release();
                ctx.pipeline().remove(HttpRequestDecoder.class);
                LogUtils.debug("http connect req receive complete ...");
            } else {
                HttpContent content = (HttpContent) msg;
                if (ipGet){
                    super.channelRead(ctx, new ProxyMsg(Protocol.OPE_DATA, id, encodeRequest(content)));
                }else {
                    body.add(content);
                }
            }
        }
    }

    private void processHttps(ChannelHandlerContext ctx) throws Exception {
        this.httpsMode = Boolean.TRUE;
        String host = this.request.headers().get("Host");
        int port = 443;
        String realHost = host;
        //有些带端口
        if (host.contains(":")) {
            String[] subs = host.split(":");
            realHost = subs[0];
            port = Integer.parseInt(subs[1]);
        }
        super.channelRead(ctx, new ProxyMsg(Protocol.OPE_RESOLV_DNS, id, MsgUtils.buildConnectMsgBody(realHost, port)));
    }

    private void processHttp(ChannelHandlerContext ctx) throws Exception {
        this.httpsMode = Boolean.FALSE;
        String host = this.request.headers().get("Host");
        int port = 80;
        String realHost = host;
        //有些带端口
        if (host.contains(":")) {
            String[] subs = host.split(":");
            realHost = subs[0];
            port = Integer.parseInt(subs[1]);
        }
        request.setUri(request.uri().split(host, 2)[1]);
        super.channelRead(ctx, new ProxyMsg(Protocol.OPE_RESOLV_DNS, id, MsgUtils.buildConnectMsgBody(realHost, port)));
    }

    private void fail(ChannelHandlerContext ctx) {
        LogUtils.err("http dns 域名解析失败:" + request.uri(), null);
        ctx.channel().close();
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (!ipGet){
            this.ipGet = true;
            ProxyMsg pm = (ProxyMsg) msg;
            if (pm.msg.readableBytes() == 0) {
                fail(ctx);
            } else {
                String ipPort = pm.msg.readCharSequence(pm.msg.readableBytes(), StandardCharsets.US_ASCII).toString();
                super.channelRead(ctx, new ProxyMsg(Protocol.OPE_CONNECT, pm.id, Unpooled.wrappedBuffer(ipPort.getBytes())));
                if (httpsMode){
                    ctx.writeAndFlush(CONNECT_RESPONSE);
                    ctx.pipeline().remove(this);
                }else {
                    CompositeByteBuf cbb = Unpooled.compositeBuffer();
                    cbb.addComponent(true, encodeRequest(request));
                    if (!body.isEmpty()){
                        for (HttpContent content : body) {
                            cbb.addComponent(true, encodeRequest(content));
                        }
                        body.clear();
                    }
                    super.channelRead(ctx, new ProxyMsg(Protocol.OPE_DATA, id, cbb));
                }
            }
        }else {
            super.write(ctx, msg, promise);
        }

    }
}
