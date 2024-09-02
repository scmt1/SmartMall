/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.card.platform.task;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.yami.shop.bean.enums.SendType;
import com.yami.shop.bean.event.CouponNotifyEvent;
import com.yami.shop.bean.event.SendMessageEvent;
import com.yami.shop.bean.model.CouponAppConnect;
import com.yami.shop.bean.param.NotifyTemplateParam;
import com.yami.shop.card.common.model.CardUser;
import com.yami.shop.card.common.service.CardService;
import com.yami.shop.card.common.service.CardUserService;
import com.yami.shop.common.util.CacheManagerUtil;
import com.yami.shop.service.CouponOrderService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;


/**
 * 优惠券定时任务
 * //0 0 0/1 * * ?
 * @author lanhai
 */
@Slf4j
@AllArgsConstructor
@Component
public class CardTask {

    private final CardUserService cardUserService;
    private final CardService cardService;
    @Autowired
    private ApplicationContext applicationContext;

    /**
     * 改变用户绑定提货卡的状态(设为失效状态)
     */
    @XxlJob("changeCardUserStatus")
    public void changeCardUserStatus() {
        Date now = new Date();
        // 设置用户的过期优惠券为失效状态
        cardUserService.updateCardUserStatusByTime(now);
    }

    /**
     * 改变提货卡的状态为已失效
     */
    @XxlJob("changeCardStatus")
    public void changeCardStatus() {
        Date now = new Date();
        // 设置提货卡的状态为已失效
        cardService.changeCardStatus(now);
    }

    /**
     * 推送即将失效的提货卡短信给用户
     */
    @XxlJob("cardSendMessage")
    public void cardSendMessage() {
        // 查询即将失效的提货卡并推送短信给用户
        List<CardUser> cardUsers = cardUserService.cardSendMessage();
        for (CardUser cardUser : cardUsers) {
            NotifyTemplateParam param = new NotifyTemplateParam();
            param.setShopId(0L);
            param.setSendType(SendType.CARD_EXPIRATION.getValue());
            param.setUserMobile(cardUser.getUserMobile());
            param.setCardTitle(cardUser.getCardTitle());
            param.setCardDay(cardUser.getDay());
            param.setUserId(cardUser.getUserId());
            applicationContext.publishEvent(new SendMessageEvent(param));
        }
    }
}
