/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: OssUpload.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.remote.oss;

import io.netty.channel.socket.SocketChannel;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;



class OssUpload {
    final String name;
    final File file;
    final Integer srcId;
    final SocketChannel srcChannel;
    final long startTime = System.currentTimeMillis();
    final FileChannel fileChannel;
    //文件存储
    long totalSize = 0;
    boolean fileCommit = false;
    //http上传
    SocketChannel ossChannel;

    public OssUpload(String name, Integer srcId, SocketChannel srcChannel) {
        this.name = name;
        this.file = new File("/tmp/" + name);
        try {
            this.fileChannel = FileChannel.open(file.toPath(), StandardOpenOption.CREATE_NEW, StandardOpenOption.READ, StandardOpenOption.WRITE);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        this.srcId = srcId;
        this.srcChannel = srcChannel;
    }
}
