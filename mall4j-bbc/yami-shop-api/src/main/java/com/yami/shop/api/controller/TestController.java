package com.yami.shop.api.controller;


import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.yami.shop.api.utils.WxAppTempMsgUtils;
import com.yami.shop.api.utils.WxCardUtils;
import com.yami.shop.api.utils.WxTempMsgUtils;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.service.UserService;
import com.yami.shop.user.common.model.Coupon;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.io.IOException;
import java.util.Date;

@ApiIgnore
@RestController
@RequestMapping("/test")
@AllArgsConstructor
@Slf4j
public class TestController {

    @Autowired
    private WxAppTempMsgUtils wxAppTempMsgUtils;

    @Autowired
    private WxTempMsgUtils wxTempMsgUtils;

    private final ApplicationContext applicationContext;

    @Autowired
    private WxCardUtils wxCardUtils;

    @Autowired
    private UserService userService;

    @RequestMapping("testPush")
    public ServerResponseEntity<Object> testPush() {

        //String push = wxAppTempMsgUtils.push("oxsi25VezxZjlpTmk7v9G4yTCRLk");
        Coupon coupon = new Coupon();
        coupon.setCouponName("测试优惠券");
        coupon.setCashCondition(100.0);
        coupon.setReduceAmount(88.0);
        coupon.setEndTime(new Date());

//        applicationContext.publishEvent(new CouponReceiveEvent(coupon.getCouponName()
//                , "消费金额满" + coupon.getCashCondition() + "减" + coupon.getReduceAmount(), coupon.getEndTime(), "您有新的优惠券到账！", "27cc75d3c48546feb897cb6cf41ace1d"));


        JSONObject data = new JSONObject();
//        //此处的参数key,需要对照模板中的key来设置
        data.put("first", "这是一条模板通知消息");
        data.put("keyword1", wxTempMsgUtils.getValue(coupon.getCouponName()));
        data.put("keyword2", wxTempMsgUtils.getValue("测试信息"));
        data.put("keyword3", wxTempMsgUtils.getValue("108.88"));
        data.put("keyword4", wxTempMsgUtils.getValue(DateUtil.format(coupon.getEndTime(), "yyyy-MM-dd HH:mm:ss")));
        data.put("remark", wxTempMsgUtils.getValue("请进入小程序查看"));

        wxTempMsgUtils.sendWxgMessage("oA6Gb6OsPtxdxz8v_ijSH1XYxpbE", "6rehsgmfkkGV96Vj3Bc0ANGNfvBoeActM-B-KO5OoMM", data, "https://lhshop.lzjczl.com/lhH5");

        return ServerResponseEntity.success();
    }


//    @RequestMapping("sendMessage")
//    public ServerResponseEntity<Object> sendMessage() {
//        String content = "7月22日会员日至7月31日活动大放送，关注剧荟广场微信小程序领取88元服装优惠券和15元小吃优惠券，更有西瓜免费吃，使用红云钱包支付，另有礼品赠送！";
//        List<User> list = userService.list();
//        for (User user : list) {
//            ShortMessageResult shortMessageResult = ShortMessageUtil.sendMessage("https://api.mix2.zthysms.com/v2/sendSms", user.getUserMobile(), content);
//        }
//        return ServerResponseEntity.success();
//    }

    @RequestMapping("wxTest")
    public ServerResponseEntity<Object> wxTest() throws IOException {
        JSONObject jsonObject = wxCardUtils.genParams();
        return ServerResponseEntity.success(jsonObject);
    }
}



