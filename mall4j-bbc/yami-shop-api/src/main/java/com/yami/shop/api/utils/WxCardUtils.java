package com.yami.shop.api.utils;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.yami.shop.common.bean.WxMp;
import com.yami.shop.config.ShopConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

@Slf4j
@Component
public class WxCardUtils {
    @Autowired
    private ShopConfig shopConfig;

    public String getWxgAccessToken() {
        WxMp wxMp = shopConfig.getWxMp();
        // 服务号的appid以及秘钥
        String appid = wxMp.getAppId();
        String Wxgsecret = wxMp.getSecret();
        String requestUrl = StrUtil.format("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid={}&secret={}", appid, Wxgsecret);
        String returnMsg = HttpUtil.get(requestUrl);
        cn.hutool.json.JSONObject responseJsonObject = JSONUtil.parseObj(returnMsg);
        log.info("获取微信公众号token：{}", responseJsonObject);
        if (ObjectUtil.isNull(responseJsonObject)) {
            return "";
        }
        String accessToken = responseJsonObject.getStr("access_token");
        return accessToken;
    }


    public JSONObject genParams() throws IOException {
        HashMap<String, String> map = new HashMap<>();
        JSONObject jsonObject = new JSONObject();
        String accessToken = getWxgAccessToken();
        System.err.println("getparam:"+accessToken);
        long time = new Date().getTime();
        String url="https://api.weixin.qq.com/card/membercard/activate/geturl?access_token="+accessToken;
        String body="{ \"card_id\" : \"pzmGcjmMhHzSxHjGqNgWO0pr_kh0" + "\", \"outer_str\" : \""+time+"\" }";

        String post = HttpUtil.post(url, body);
        log.info("请求结果：{}", post);
        JSONObject jsonObject1 = JSONObject.parseObject(post);
        String errcode = jsonObject1.getString("errcode");
        if (errcode.equals("0")){
            String url1 = jsonObject1.getString("url");
            System.err.println("url:=="+url1);
            String returnstr = url1.substring(url1.indexOf("encrypt_card_id=") + "encrypt_card_id".length(), url1.indexOf("#"));
            System.err.println("returnstr:=="+returnstr);
            String[] strparam = returnstr.split("&");
            String encryptCardId = "";
            String outerStr = "";
            String biz = "";
            if (strparam.length > 2){//带有场景值
                encryptCardId=strparam[0];
                outerStr=strparam[1].split("=")[1];
                biz=strparam[2].split("=")[1];
            }else {//没有场景值得情况
                encryptCardId=strparam[0];
                biz=strparam[1].split("=")[1];
            }
            jsonObject.put("encryptCardId", encryptCardId);
            jsonObject.put("outerStr",outerStr);
            jsonObject.put("biz",biz);
            jsonObject.put("success","success");
        }
        return jsonObject;
    }

}
