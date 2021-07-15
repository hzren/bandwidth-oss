/*
 * Copyright (c) 2021
 * User: hzren@outlook.com
 * File: OssUtil.java
 * Date: 2021/07/08 15:23:08
 */

package com.bandwidth.proxy.base;

import cn.ucloud.ufile.auth.ObjectAuthorization;
import cn.ucloud.ufile.auth.ObjectOptAuthParam;
import cn.ucloud.ufile.auth.UfileObjectLocalAuthorization;
import com.bandwidth.proxy.local.oss.DownloadMsg;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.util.AttributeKey;

import java.util.LinkedList;

/**
  * @Date 2021/4/23
 **/

public class OssUtil {

    /**
     * 本地Object相关API的签名器
     * 请修改下面的公私钥
     */
    public static final ObjectAuthorization OBJECT_AUTHORIZER = new UfileObjectLocalAuthorization(
            Config.INSTANCE.ucloudOssPublicKey,
            Config.INSTANCE.ucloudOssPrivateKey);

    public static final AttributeKey<LinkedList<DownloadMsg>> KEY_DM_MSG = AttributeKey.valueOf("DM_MSG");

    public static String calcSign(String keyName, HttpMethod method) {
        cn.ucloud.ufile.util.HttpMethod hm = cn.ucloud.ufile.util.HttpMethod.GET;
        if (method == HttpMethod.DELETE) {
            hm = cn.ucloud.ufile.util.HttpMethod.DELETE;
        } else if (method == HttpMethod.PUT) {
            hm = cn.ucloud.ufile.util.HttpMethod.PUT;
        }
        ObjectOptAuthParam param = new ObjectOptAuthParam(hm, Config.INSTANCE.ucloudOssBucketName, keyName,
                "", "", "");
        try {
            return OssUtil.OBJECT_AUTHORIZER.authorization(param);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
