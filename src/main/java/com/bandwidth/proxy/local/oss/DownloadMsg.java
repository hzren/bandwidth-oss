/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: DownloadMsg.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.local.oss;


import io.netty.buffer.ByteBuf;
import io.netty.channel.socket.SocketChannel;

import java.util.LinkedList;

/**
 * @Date 2021/4/27
 **/

public class DownloadMsg {
    public static final int NOT_START = 0, DOWNLOADING = 1, OK = 2;

    public final String key;
    public final SocketChannel src;
    public final int size;
    public int download = NOT_START;
    public LinkedList<ByteBuf> datas = new LinkedList<>();

    public DownloadMsg(String key, SocketChannel src, int size) {
        this.key = key;
        this.src = src;
        this.size = size;
    }

}
