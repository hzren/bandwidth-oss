/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: SocksFrontApp.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy;

import com.bandwidth.proxy.base.Env;
import com.bandwidth.proxy.base.OssTool;
import com.bandwidth.proxy.local.http_server.HttpServerComponent;
import com.bandwidth.proxy.local.multiplex_sender.MpxSenderClientComponent;
import com.bandwidth.proxy.local.socks_server.SocksServerComponent;
import io.netty.util.ResourceLeakDetector;

public class SocksFrontApp {
    private static MpxSenderClientComponent proxyChannelManager;
    private static SocksServerComponent socksClientComponent;
    private static HttpServerComponent httpClientComponent;

    public static void startMpx() {
        proxyChannelManager = new MpxSenderClientComponent();
        socksClientComponent = new SocksServerComponent(proxyChannelManager);
        httpClientComponent = new HttpServerComponent(proxyChannelManager);
    }

    public static void main(String[] args) {
        OssTool.cleanAll();
        ResourceLeakDetector.setLevel(Env.LEVEL);
        startMpx();
        Thread.yield();
    }
}
