/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: Attributes.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.base;

import io.netty.channel.socket.SocketChannel;
import io.netty.util.AttributeKey;

import java.util.HashMap;
import java.util.Queue;

public interface Attributes {
    AttributeKey<Object> KEY_COMPONENT = AttributeKey.valueOf("COMPONENT");
    AttributeKey<Number> KEY_ID = AttributeKey.valueOf("ID");
    AttributeKey<String> KEY_TARGET = AttributeKey.valueOf("TARGET");
    AttributeKey<HashMap<Number, SocketChannel>> KEY_PROXY_FOR = AttributeKey.valueOf("PROXY_FOR");
    AttributeKey<SocketChannel> KEY_SRC = AttributeKey.valueOf("SRC");
    AttributeKey<Queue<Object>> KEY_PENDING_MSG = AttributeKey.valueOf("PENDING_MSG");
}
