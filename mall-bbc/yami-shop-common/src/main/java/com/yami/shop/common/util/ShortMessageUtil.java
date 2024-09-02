package com.yami.shop.common.util;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yami.shop.common.bean.ShortMessageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author ycy
 */
@Component
@Slf4j
public class ShortMessageUtil {


    private static String md5(String param) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] md5Byte = md5.digest(param.getBytes("utf8"));
        String result = byteToHex(md5Byte);
        return result;
    }

    private static String byteToHex(byte[] md5Byte) {
        String result = "";
        StringBuilder sb = new StringBuilder();
        for (byte each : md5Byte) {
            int value = each & 0xff;
            String hex = Integer.toHexString(value);
            if (value < 16) {
                sb.append("0");
            }
            sb.append(hex);
        }
        result = sb.toString();
        return result;
    }


    public static int byte4ToInteger(byte[] b, int offset) {
        return (0xff & b[offset]) << 24 | (0xff & b[offset + 1]) << 16 |
                (0xff & b[offset + 2]) << 8 | (0xff & b[offset + 3]);
    }


    /**
     * 发送短信
     *
     * @return
     */
    public static ShortMessageResult sendMessage(String url, String username, String pwd, String phone, String content) {
        HttpClient httpClient = null;
        try {
            httpClient = new DefaultHttpClient();
//            String username = "szjchy";
//            String pwd = "qe23g821";
            //此处加密后加盐再进行加密
            Long tKey = System.currentTimeMillis() / 1000;
            String password = md5(md5(pwd) + tKey);
            JSONObject json = new JSONObject();
            //账号
            json.put("username", username);
            //密码
            json.put("password", password);
            //tKey
            json.put("tKey", tKey + "");
            //手机号
            json.put("mobile", phone);
            //内容
            json.put("content", "【剧荟广场】" + content);
            String result = HttpRequest.post(url)
                    .timeout(60000)
                    .body(json.toJSONString(), MediaType.APPLICATION_JSON_UTF8_VALUE)
                    .execute()
                    .body();
            ShortMessageResult shortMessageResult = JSONArray.parseObject(result, ShortMessageResult.class);
            return shortMessageResult;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (httpClient != null) {
                httpClient.getConnectionManager().shutdown();
            }
        }
    }
}
