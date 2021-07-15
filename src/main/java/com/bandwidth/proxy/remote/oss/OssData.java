/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: OssData.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.remote.oss;

import com.bandwidth.proxy.base.ProxyMsg;
import io.netty.channel.socket.SocketChannel;

/**
 * @Date 2021/7/1
 **/

public class OssData {
    public final ProxyMsg pm;
    public final SocketChannel src;
    public final Integer mpxId;

    public OssData(ProxyMsg pm, SocketChannel src, Integer mpxId) {
        this.pm = pm;
        this.src = src;
        this.mpxId = mpxId;
    }
}
