/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: LocalOssChannelInitializer.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.local.oss;

import com.bandwidth.proxy.component.netty.ChannelErrorHandler;
import com.bandwidth.proxy.local.multiplex_sender.MpxSenderClientComponent;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;

/**
 * @Date 2021/4/27
 **/

class LocalOssChannelInitializer extends ChannelInitializer<SocketChannel> {
    private final MpxSenderClientComponent sender;
    private final LocalOssComponent oss;

    public LocalOssChannelInitializer(MpxSenderClientComponent sender, LocalOssComponent oss) {
        this.sender = sender;
        this.oss = oss;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new HttpClientCodec());
        //pipeline.addLast(new HttpObjectAggregator(Config.MAX_LEN));
        pipeline.addLast(new LocalOssLogicalHandler(oss));
        //如果把下载的文件的原始数据给到mpx channel去从头解析，会和mpx的自身数据冲突，我们解析好之后再转交mpx的逻辑handler处理
        pipeline.addLast(new RouteParsedMsgToMpxInboundHandler());
        pipeline.addLast(ChannelErrorHandler.DEFAULT);
    }
}
