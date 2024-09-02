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

@Slf4j
@Component
public class WxTempMsgUtils {
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


    public Boolean sendWxgMessage(String openid, String templateId, JSONObject data, String pageUrl) {
        String wxgAccessToken = getWxgAccessToken();
        JSONObject jsonData = new JSONObject();
        jsonData.put("touser", openid);
        jsonData.put("template_id", templateId);
        jsonData.put("url", pageUrl);
        jsonData.put("data", data);
        // 发送消息
        String returnMsg = HttpUtil.post(StrUtil.format("https://api.weixin.qq.com/cgi-bin/message/template/send?access_token={}", wxgAccessToken), jsonData.toJSONString());
        cn.hutool.json.JSONObject jsonObject = JSONUtil.parseObj(returnMsg);
        log.info("发送微信模板消息：{}", jsonObject);
        String errmsg = jsonObject.getStr("errmsg");
        if (!StrUtil.equals("ok", errmsg)) {
            return false;
        }
        return true;
    }


    public JSONObject getValue(String value) {
        // TODO Auto-generated method stub
        JSONObject json = new JSONObject();
        json.put("value", value);
        json.put("color", "#173177");
        return json;
    }
}
