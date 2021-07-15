/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: FileUtils.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.base;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;



public class FileUtils {

    public static final void write(FileChannel channel, ByteBuf byteBuf) {
        if (byteBuf instanceof CompositeByteBuf) {
            CompositeByteBuf cbb = (CompositeByteBuf) byteBuf;
            int num = cbb.numComponents();
            for (int i = 0; i < num; i++) {
                write(channel, cbb.component(i));
            }
        } else {
            int len = byteBuf.readableBytes();
            ByteBuffer bb = byteBuf.nioBuffer();
            int write = 0;
            while (write < len) {
                try {
                    write = write + channel.write(bb);
                } catch (IOException e) {
                    LogUtils.err("write file fail", e);
                    throw new Error(e);
                }
            }
        }
    }

    public static final ByteBuf read(FileChannel channel) {
        try {
            long position = channel.position();
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect((int) position);
            channel.position(0);
            long read = 0;
            while (read < position) {
                read = read + channel.read(byteBuffer);
            }
            channel.position(position);
            byteBuffer.flip();
            return Unpooled.wrappedBuffer(byteBuffer);
        } catch (Throwable e) {
            LogUtils.err("file read err", e);
            throw new RuntimeException(e);
        }

    }
}
