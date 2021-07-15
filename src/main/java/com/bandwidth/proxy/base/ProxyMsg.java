/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: ProxyMsg.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.base;

import io.netty.buffer.ByteBuf;


public class ProxyMsg {
    public final byte ope;
    public final Number id;
    public final ByteBuf msg;

    public ProxyMsg(byte ope, Number id, ByteBuf msg) {
        this.ope = ope;
        this.id = id;
        this.msg = msg;
    }
}
