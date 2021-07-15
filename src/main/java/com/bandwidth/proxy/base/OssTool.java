/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: OssTool.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.base;

import cn.ucloud.ufile.UfileClient;
import cn.ucloud.ufile.api.object.ObjectConfig;
import cn.ucloud.ufile.bean.ObjectInfoBean;
import cn.ucloud.ufile.bean.ObjectListBean;
import cn.ucloud.ufile.bean.base.BaseObjectResponseBean;
import cn.ucloud.ufile.exception.UfileClientException;
import cn.ucloud.ufile.exception.UfileServerException;

import java.io.File;
import java.util.List;


public class OssTool {
    public static final String SP = "_da_";
    private static final ObjectConfig config = new ObjectConfig(Config.INSTANCE.ucloudOssPublicHost);

    public static void cleanTmp() {
        File file = new File("/tmp/");
        File[] files = file.listFiles();
        for (File file1 : files) {
            try {
                file1.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void cleanAll() {
        LogUtils.info("clean all oss file");
        String marker = null;
        long now = System.currentTimeMillis();
        for (; ; ) {
            try {
                ObjectListBean rsp = UfileClient.object(OssUtil.OBJECT_AUTHORIZER, config)
                        .objectList(Config.INSTANCE.ucloudOssBucketName)
                        .withMarker(marker)
                        .withPrefix("")
                        .dataLimit(100)
                        .execute();
                List<ObjectInfoBean> beans = rsp.getObjectList();
                LogUtils.info("clean oss files,size:" + beans.size());
                if (beans.size() == 0) {
                    return;
                }
                for (ObjectInfoBean bean : beans) {
                    String name = bean.getFileName();
                    if (!name.contains(SP)) {
                        return;
                    }
                    String[] subs = name.split(SP);
                    long time = Long.parseLong(subs[0]);
                    if (now - time < 600l * 1000) {
                        return;
                    }
                    delFile(bean.getFileName());
                }
                marker = rsp.getNextMarker();
                if (beans.size() < 100) {
                    break;
                }
            } catch (Exception e) {
                LogUtils.err("clean oss err", e);
            }
        }
    }

    public static void delFile(String name) {
        LogUtils.debug("del file:" + name);
        for (; ; ) {
            try {
                BaseObjectResponseBean rsp = UfileClient.object(OssUtil.OBJECT_AUTHORIZER, config)
                        .deleteObject(name, Config.INSTANCE.ucloudOssBucketName)
                        .execute();
                LogUtils.debug("del " + name + ",rsp:" + rsp.getRetCode());
                return;
            } catch (UfileClientException | UfileServerException e) {
                LogUtils.err("del file fail", e);
                continue;
            }
        }
    }
}
