/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.api.listener;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yami.shop.api.utils.WxAppTempMsgUtils;
import com.yami.shop.api.utils.WxTempMsgUtils;
import com.yami.shop.bean.event.CouponNotifyEvent;
import com.yami.shop.bean.event.CouponReceiveEvent;
import com.yami.shop.bean.model.CouponAppConnect;
import com.yami.shop.security.common.model.AppConnect;
import com.yami.shop.security.common.service.AppConnectService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 确认订单信息时的优惠券的相关操作，计算优惠券 优惠金额
 *
 * @author LGH
 */
@Component("couponReceiveListener")
@AllArgsConstructor
@Slf4j
public class CouponReceiveListener {

    @Autowired
    private WxAppTempMsgUtils wxAppTempMsgUtils;

    @Autowired
    private WxTempMsgUtils wxTempMsgUtils;


    @Autowired
    private AppConnectService appConnectService;

    /**
     *
     */
    @EventListener(CouponReceiveEvent.class)
    public void checkCouponStatusListener(CouponReceiveEvent event) {
        log.info("接收到公众号消息推送1111");
        try {
            LambdaQueryWrapper<AppConnect> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AppConnect::getUserId, event.getUserId());
            queryWrapper.eq(AppConnect::getAppId, 2);
            queryWrapper.orderByDesc(AppConnect::getId);
            queryWrapper.last("LIMIT 1");
            AppConnect one = appConnectService.getOne(queryWrapper);
            //公众号消息数据封装
            JSONObject data = new JSONObject();
            //此处的参数key,需要对照模板中的key来设置
            data.put("thing1", getValue(event.getCouponName()));
            data.put("thing2", getValue(event.getCashCondition()));
            data.put("time3", getValue(DateUtil.format(event.getEndTime(), "yyyy-MM-dd HH:mm:ss")));
            data.put("thing4", getValue(event.getRemark()));

            String pageUrl = "https://lhshop.lzjczl.com/lhH5/#/package-activities/pages/my-coupon/my-coupon";

            wxTempMsgUtils.sendWxgMessage(one.getBizUserId(), "", data, pageUrl);
        } catch (Exception e) {
            log.error("推送公众号消息异常1：{}", e.getMessage());
        }
    }


    @EventListener(CouponNotifyEvent.class)
    public void couponNotify(CouponNotifyEvent event) {
        log.info("接收到公众号消息推送2222");
        try {
            List<CouponAppConnect> openidList = event.getOpenidList();
            for (CouponAppConnect couponAppConnect : openidList) {
                JSONObject data = new JSONObject();
                //此处的参数key,需要对照模板中的key来设置
                data.put("first", "这是一条模板通知消息");
                data.put("keyword1", getValue(couponAppConnect.getCouponName()));
                data.put("keyword2", getValue("消费金额满" + couponAppConnect.getCashCondition() + "减" + couponAppConnect.getReduceAmount()));
                data.put("keyword3", getValue(DateUtil.format(couponAppConnect.getUserEndTime(), "yyyy-MM-dd HH:mm:ss")));
                data.put("remark", getValue("点击查看详情"));
                wxTempMsgUtils.sendWxgMessage(couponAppConnect.getOpenid(), "", data, "https://lhshop.lzjczl.com/lhH5/#/package-activities/pages/my-coupon/my-coupon");
            }
        } catch (Exception e) {
            log.error("推送公众号消息异常2：{}", e.getMessage());
        }
    }


    private JSONObject getValue(String value) {
        // TODO Auto-generated method stub
        JSONObject json = new JSONObject();
        json.put("value", value);
        json.put("color", "#173177");
        return json;
    }
}
