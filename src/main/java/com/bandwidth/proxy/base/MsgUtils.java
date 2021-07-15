/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: MsgUtils.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.base;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.socket.SocketChannel;

import java.nio.charset.StandardCharsets;
import java.util.Queue;


public class MsgUtils {

    public static Long proxyServerClientId(Integer channelId, Integer subId) {
        long pid = channelId;
        return (pid << 32) + subId;
    }

    public static Integer[] proxyChannelId(Long id) {
        return new Integer[]{(int) (id >> 32), id.intValue()};
    }

    public static String readAsString(ByteBuf msg) {
        String res = msg.readCharSequence(msg.readableBytes(), StandardCharsets.UTF_8).toString();
        msg.release();
        return res;
    }

    public static void clearPendingMsg(SocketChannel channel) {
        Queue queue = channel.attr(Env.KEY_PENDING_MSG).get();
        if (queue != null) {
            for (; ; ) {
                Object o = queue.poll();
                if (o == null) {
                    break;
                }
                if (o instanceof ProxyMsg) {
                    ((ProxyMsg) o).msg.release();
                } else if (o instanceof ByteBuf) {
                    ((ByteBuf) o).release();
                }
            }
        }
    }

    public static final ByteBuf buildConnectMsgBody(String host, int port) {
        return buildConnectMsgBody(host + ":" + port);
    }

    public static final ByteBuf buildConnectMsgBody(String uri) {
        return Unpooled.wrappedBuffer(uri.getBytes());
    }

    public static ProxyMsg parseProxyMsg(ByteBuf data) {
        Integer id = data.readInt();
        byte ope = data.readByte();
        return new ProxyMsg(ope, id, data);
    }

    public static ByteBuf encode(ProxyMsg rm) {
        ByteBuf originData = rm.msg;
        //消息体长度 = 通道ID长度(4字节) + OPE长度(1)字节 + 原始数据长度
        int length = 5 + originData.readableBytes();
        ByteBuf bb = Unpooled.directBuffer(9).writeInt(length).writeInt((Integer) rm.id).writeByte(rm.ope);
        CompositeByteBuf cbb = Unpooled.compositeBuffer();
        return cbb.addComponent(true, bb).addComponent(true, originData);
    }

}
