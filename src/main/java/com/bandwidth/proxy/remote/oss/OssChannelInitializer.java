/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: OssChannelInitializer.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.remote.oss;

import com.bandwidth.proxy.base.Env;
import com.bandwidth.proxy.component.netty.ChannelErrorHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;

/**
 * @Date 2021/4/25
 **/
@ChannelHandler.Sharable
class OssChannelInitializer extends ChannelInitializer<SocketChannel> {
    private final RemoteOssComponent component;

    public OssChannelInitializer(RemoteOssComponent component) {
        this.component = component;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        ch.attr(Env.KEY_COMPONENT).set(component);
        pipeline.addLast("codec", new HttpClientCodec());
        pipeline.addLast("oss", new OssLogicalHandler());
        pipeline.addLast("err", ChannelErrorHandler.DEFAULT);
    }
}
