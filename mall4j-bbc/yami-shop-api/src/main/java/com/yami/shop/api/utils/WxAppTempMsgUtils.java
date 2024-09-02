package com.yami.shop.api.utils;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.yami.shop.common.bean.WxMiniApp;
import com.yami.shop.config.ShopConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Component
public class WxAppTempMsgUtils {


    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ShopConfig shopConfig;

    public String getWxgAccessToken() {
        WxMiniApp wxMiniApp = shopConfig.getWxMiniApp();
        // 服务号的appid以及秘钥
        String appid = wxMiniApp.getAppId();
        String Wxgsecret = wxMiniApp.getSecret();
        String requestUrl = StrUtil.format("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid={}&secret={}", appid, Wxgsecret);
        String returnMsg = HttpUtil.get(requestUrl);
        cn.hutool.json.JSONObject responseJsonObject = JSONUtil.parseObj(returnMsg);
        log.info("获取微信token：{}", responseJsonObject);
        if (ObjectUtil.isNull(responseJsonObject)) {
            return "";
        }
        String accessToken = responseJsonObject.getStr("access_token");
        return accessToken;
    }


    @ResponseBody
    @RequestMapping(value = "/push",  method = RequestMethod.POST,produces = "application/json;charset=UTF-8")
    public String push(String openid) {
        //获取需要推送的用户openid

        //获取用户token
        String token = getWxgAccessToken();

        String resultStatus = "0";//0:失败，1：成功
        try {
            //小程序统一消息推送
            String path = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + token;
            //封装参数
            JSONObject jsonData = new JSONObject();
            jsonData.put("template_id", "ex4zs0K5_WO3MTWFkGKzpRc2cweA7h1d6reo3kNWCYc"); //所需下发的订阅模板id
            jsonData.put("page", "pages/home/home"); //点击模板卡片后的跳转页面，仅限本小程序内的页面。支持带参数,（示例index?foo=bar）。该字段不填则模板无跳转
            jsonData.put("touser", openid); //接收者（用户）的 openid

            //公众号消息数据封装
            JSONObject data = new JSONObject();
            //此处的参数key,需要对照模板中的key来设置
            data.put("character_string1", getValue("P123456498456456"));
            data.put("phrase2", getValue("已支付"));
            data.put("time3", getValue("2023-07-17 12:30:16"));
            data.put("thing4", getValue("这是订单的描述"));

            jsonData.put("data", data); //模板内容，格式形如 { "key1": { "value": any }, "key2": { "value": any } }的object
            //jsonData.put("miniprogram_state", "formal"); //跳转小程序类型：developer为开发版；trial为体验版；formal为正式版；默认为正式版
            //jsonData.put("lang", "zh_CN"); //进入小程序查看”的语言类型，支持zh_CN(简体中文)、en_US(英文)、zh_HK(繁体中文)、zh_TW(繁体中文)，默认为zh_CN
            String s = HttpUtil.post(path, jsonData.toJSONString());
            System.out.println("返回结果："+s);
            resultStatus="1";
        } catch (Exception e) {
            log.error("微信发送消息失败！",e.getMessage());
            resultStatus="0";
        }
        return resultStatus;
    }


    public String templateMsgPush(String openid,String templateId, JSONObject data, String pageUrl) {
        //获取需要推送的用户openid

        //获取用户token
        String token = getWxgAccessToken();

        String resultStatus = "0";//0:失败，1：成功
        try {
            //小程序统一消息推送
            String path = "https://api.weixin.qq.com/cgi-bin/message/subscribe/send?access_token=" + token;
            //封装参数
            JSONObject jsonData = new JSONObject();
            jsonData.put("template_id", templateId); //所需下发的订阅模板id
            jsonData.put("page", pageUrl); //点击模板卡片后的跳转页面，仅限本小程序内的页面。支持带参数,（示例index?foo=bar）。该字段不填则模板无跳转
            jsonData.put("touser", openid); //接收者（用户）的 openid

            jsonData.put("data", data); //模板内容，格式形如 { "key1": { "value": any }, "key2": { "value": any } }的object
            //jsonData.put("miniprogram_state", "formal"); //跳转小程序类型：developer为开发版；trial为体验版；formal为正式版；默认为正式版
            //jsonData.put("lang", "zh_CN"); //进入小程序查看”的语言类型，支持zh_CN(简体中文)、en_US(英文)、zh_HK(繁体中文)、zh_TW(繁体中文)，默认为zh_CN
            String s = HttpUtil.post(path, jsonData.toJSONString());
            System.out.println("返回结果："+s);
            resultStatus="1";
        } catch (Exception e) {
            log.error("微信发送消息失败！",e.getMessage());
            resultStatus="0";
        }
        return resultStatus;
    }


    private JSONObject getValue(String value) {
        // TODO Auto-generated method stub
        JSONObject json = new JSONObject();
        json.put("value", value);
        json.put("color", "#173177");
        return json;
    }
}
