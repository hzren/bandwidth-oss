/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: RemoteOssComponent.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.remote.oss;


import com.bandwidth.proxy.base.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ReflectiveChannelFactory;
import io.netty.channel.socket.SocketChannel;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;



public class RemoteOssComponent {
    final LinkedList<SocketChannel> channels = new LinkedList<>();
    private final EventLoopGroup group = Env.eventLoopGroup("oss", 1);
    private final LinkedList<Object> files = new LinkedList<>();
    private final HashMap<Integer, Long> bufferTimeMap = new HashMap<>();

    public RemoteOssComponent() {
        Future future = group.submit(new Runnable() {
            @Override
            public void run() {
                connectOss(true);
            }
        });
        while (!future.isDone()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            LogUtils.info("等待oss链接连接完成...");
        }
        LogUtils.info("oss链接连接完成...");
        for (int i = 1; i < 5; i++) {
            group.schedule(new Runnable() {
                @Override
                public void run() {
                    connectOss(false);
                }
            }, i * 1000, TimeUnit.MILLISECONDS);
        }
    }

    private static void processOssData(OssUpload upload, OssData ossData) {
        ByteBuf bb = MsgUtils.encode(ossData.pm);
        upload.totalSize = upload.totalSize + bb.readableBytes();
        FileUtils.write(upload.fileChannel, bb);
        if (upload.ossChannel != null) {
            upload.ossChannel.write(bb);
        } else {
            bb.release();
        }
    }

    void connectOss(boolean wait) {
        Bootstrap bootstrap = new Bootstrap();
        Env.setOption(bootstrap);
        ChannelFuture future = bootstrap.channelFactory(new ReflectiveChannelFactory<>(Env.CHANNEL_TYPE))
                .group(group)
                .handler(new OssChannelInitializer(this))
                .attr(Env.KEY_PENDING_MSG, files)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5 * 1000)
                .option(ChannelOption.SO_SNDBUF, 16 * 1024)
                .option(ChannelOption.SO_RCVBUF, 4 * 1024)
                .option(ChannelOption.AUTO_READ, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .connect(Config.INSTANCE.ucloudOssPrivateHost, 80)
                .addListener(new OssConnectListener(this));
        if (wait) {
            try {
                future.sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    void firePoll() {
        for (SocketChannel channel : channels) {
            if (channel.isActive()) {
                channel.write(OssLogicalHandler.EVENT_POLL);
            }
        }
    }

    void reschedule(OssUpload upload) {
        upload.ossChannel = null;
        firePoll();
    }

    public void upload(OssData msg) {
        group.execute(new Runnable() {
            @Override
            public void run() {
                OssUpload upload = findUpload(msg);
                processOssData(upload, msg);
                firePoll();
            }
        });
    }

    private OssUpload findUpload(OssData data) {
        OssUpload target = null;
        for (Object file : files) {
            OssUpload upload = (OssUpload) file;
            if (upload.srcId.intValue() == data.mpxId.intValue() && !upload.fileCommit) {
                target = upload;
                break;
            }
        }
        if (target == null) {
            String name = System.currentTimeMillis() + OssTool.SP + data.mpxId;
            target = new OssUpload(name, data.mpxId, data.src);
            files.addLast(target);
            OssUpload finalTarget = target;
            group.next().schedule(new Runnable() {
                @Override
                public void run() {
                    commit(finalTarget);
                }
            }, getBufferTime(data.mpxId), TimeUnit.MILLISECONDS);
        }
        return target;
    }

    private void commit(OssUpload target) {
        target.fileCommit = true;
        if (target.ossChannel != null) {
            target.ossChannel.write(OssLogicalHandler.EVENT_COMMIT);
            //http 请求ok关闭fileChannel
        }
        long fileSize = target.totalSize;
        long speed = fileSize / (System.currentTimeMillis() - target.startTime);
        long lastBuffer = getBufferTime(target.srcId);
        if (speed > 128) {
            lastBuffer = lastBuffer * 2 > 800 ? 800 : lastBuffer * 2;
        } else {
            lastBuffer = lastBuffer / 2 > 50 ? lastBuffer / 2 : 50;
        }
        if (lastBuffer == 50) {
            bufferTimeMap.remove(target.srcId);
        } else {
            bufferTimeMap.put(target.srcId, lastBuffer);
        }
        LogUtils.debug("fileId:" + target.name + ",size:" + fileSize + ",bufferTime:" + lastBuffer + ",speed:" + speed);
        //notify mpx
        ProxyMsg pm = new ProxyMsg(Protocol.OPE_OSS_FILE, target.srcId, Unpooled.wrappedBuffer(target.name.getBytes(StandardCharsets.ISO_8859_1)));
        target.srcChannel.eventLoop().execute(new Runnable() {
            @Override
            public void run() {
                target.srcChannel.write(pm);
            }
        });
    }

    private long getBufferTime(Integer mpxId) {
        return bufferTimeMap.getOrDefault(mpxId, 50l);
    }
}
