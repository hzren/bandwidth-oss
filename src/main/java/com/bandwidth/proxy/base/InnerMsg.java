/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: InnerMsg.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.base;

import io.netty.channel.socket.SocketChannel;




public class InnerMsg {
    public final SocketChannel src;
    public final ProxyMsg msg;

    public InnerMsg(SocketChannel src, ProxyMsg msg) {
        this.src = src;
        this.msg = msg;
    }
}
