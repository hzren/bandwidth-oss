/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: ClientProxySender.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.component;

import com.bandwidth.proxy.base.InnerMsg;

/**
 * 发送proxy报文的组件
 */
public interface ClientProxySender {

    /**
     * 消息通知
     */
    void write(InnerMsg msg);
}
