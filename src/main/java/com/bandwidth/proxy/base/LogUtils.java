/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: LogUtils.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.base;

import io.netty.channel.socket.SocketChannel;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import static java.time.temporal.ChronoField.*;

public class LogUtils {
    public static final int err = 0, info = 1, debug = 2;
    public static final int level = 0;
    public static final DateTimeFormatter FORMATTER;

    static {
        FORMATTER = new DateTimeFormatterBuilder()
                .appendValue(HOUR_OF_DAY, 2)
                .appendLiteral(':')
                .appendValue(MINUTE_OF_HOUR, 2)
                .optionalStart()
                .appendLiteral(':')
                .appendValue(SECOND_OF_MINUTE, 2)
                .optionalStart()
                .appendFraction(MILLI_OF_SECOND, 3, 3, true)
                .toFormatter();
    }

    private static void addLog(String msg) {
        System.out.println(LocalTime.now().format(FORMATTER) + "-" + Thread.currentThread().getName() + "-" + msg);
    }

    public static final void err(String msg, Throwable throwable) {
        if (level < err) {
            return;
        }
        if (throwable != null) {
            msg = msg + ": " + throwable.getMessage();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(bos);
            throwable.printStackTrace(ps);
            msg = msg + "\r\n" + new String(bos.toByteArray());
            ps.close();
        }
        info(msg);
    }

    public static final void err(SocketChannel channel, String msg) {
        if (level >= err) {
            addLog(channel + "-" + msg);
        }
    }

    public static final void info(String msg) {
        if (level >= info) {
            addLog(msg);
        }
    }

    public static final void debug(String msg) {
        if (level >= debug) {
            addLog(msg);
        }
    }

}
