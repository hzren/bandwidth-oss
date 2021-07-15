/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: DownloadData.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.local.oss;

import io.netty.buffer.ByteBuf;
import io.netty.channel.socket.SocketChannel;

/**
 * @Date 2021/7/2
 **/

class DownloadData {
    final SocketChannel src;
    final ByteBuf data;

    public DownloadData(SocketChannel src, ByteBuf data) {
        this.src = src;
        this.data = data;
    }
}
