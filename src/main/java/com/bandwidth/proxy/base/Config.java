/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: Config.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.base;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class Config {
    public static final Config INSTANCE;

    static {
        String fname = "config.json";
        try {
            InputStream stream = Config.class.getClassLoader().getResourceAsStream(fname);
            if (stream == null) {
                System.out.println("read config-local.json from classpath fail...");
                String path = System.getProperty("user.home") + "//" + fname;
                try {
                    stream = new FileInputStream(path);
                } catch (FileNotFoundException e) {
                    System.out.println("read config-local.json from:" + path + " fail...");
                }
            }
            if (stream == null) {
                System.out.println("config-local.json read fail, exit...");
                throw new RuntimeException("配置读取失败");
            }
            INSTANCE = new Gson().fromJson(new InputStreamReader(stream, "UTF-8"), Config.class);
            System.out.println("读取配置成功:");
            System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(INSTANCE));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("配置读取失败");
        }
    }

    public final int localSocksPort;
    public final int localHttpPort;
    public final String serverHost;
    public final int serverPort;
    public final String secretKey;
    public final String serverBindIp;
    public final int serverBindPort;
    public final String ucloudOssPublicKey;
    public final String ucloudOssPrivateKey;
    public final String ucloudOssBucketName;
    public final String ucloudOssPublicHost;
    public final String ucloudOssPrivateHost;
    public final String dnsList;

    public Config(int localSocksPort, int localHttpPort, String serverHost, int serverPort, String secretKey, String serverBindIp, int serverBindPort, String ucloudOssPublicKey, String ucloudOssPrivateKey, String ucloudOssBucketName, String ucloudOssPublicHost, String ucloudOssPrivateHost, String dnsList) {
        this.localSocksPort = localSocksPort;
        this.localHttpPort = localHttpPort;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.secretKey = secretKey;
        this.serverBindIp = serverBindIp;
        this.serverBindPort = serverBindPort;
        this.ucloudOssPublicKey = ucloudOssPublicKey;
        this.ucloudOssPrivateKey = ucloudOssPrivateKey;
        this.ucloudOssBucketName = ucloudOssBucketName;
        this.ucloudOssPublicHost = ucloudOssPublicHost;
        this.ucloudOssPrivateHost = ucloudOssPrivateHost;
        this.dnsList = dnsList;
    }

    public boolean isLocal() {
        return "127.0.0.1".equals(serverHost);
    }
}
