/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: SocksBackendApp.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy;

import com.bandwidth.proxy.base.Env;
import com.bandwidth.proxy.base.OssTool;
import com.bandwidth.proxy.remote.client.SenderComponent;
import com.bandwidth.proxy.remote.multiplex_server.MpxServerComponent;
import io.netty.util.ResourceLeakDetector;

public class SocksBackendApp {
    public static MpxServerComponent serverComponent;
    public static SenderComponent senderComponent;

    public static void main(String[] args) {
        OssTool.cleanTmp();
        ResourceLeakDetector.setLevel(Env.LEVEL);
        senderComponent = new SenderComponent();
        serverComponent = new MpxServerComponent(senderComponent);
        serverComponent.start();
        Thread.yield();
    }
}
