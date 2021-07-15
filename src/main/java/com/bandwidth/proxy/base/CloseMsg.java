package com.bandwidth.proxy.base;

import io.netty.channel.socket.SocketChannel;

public class CloseMsg {
    public final Number id;
    public final SocketChannel channel;

    public CloseMsg(Number id, SocketChannel channel) {
        this.id = id;
        this.channel = channel;
    }
}
