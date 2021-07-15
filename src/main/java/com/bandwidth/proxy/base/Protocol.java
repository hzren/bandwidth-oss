/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: Protocol.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.base;

/**
 * @Date 2021/7/6
 **/

public class Protocol {
    /**
     * 会话key
     */
    public static final byte OPE_SECRET_KEY = 0;
    /**
     * 消息类型, 链接 1
     */
    public static final byte OPE_CONNECT = 1;
    /**
     * 消息类型, 数据 2
     */
    public static final byte OPE_DATA = 2;
    /**
     * 消息类型, 关闭通道 3
     */
    public static final byte OPE_CLOSE = 3;
    /**
     * 消息类型， oss 数据
     */
    public static final byte OPE_OSS_FILE = 4;
    /**
     * 消息类型, 心跳 5
     */
    public static final byte OPE_HB = 5;
    /**
     * 告诉客户端MPX通道ID
     */
    public static final byte OPE_MPX_ID = 6;
    /**
     * 告诉客户端MPX通道ID
     */
    public static final byte OPE_RESOLV_DNS = 7;
}
