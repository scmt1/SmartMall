/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.coupon.platform.task;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.yami.shop.bean.enums.SendType;
import com.yami.shop.bean.event.CouponNotifyEvent;
import com.yami.shop.bean.event.SendMessageEvent;
import com.yami.shop.bean.model.CouponAppConnect;
import com.yami.shop.bean.model.CouponOrder;
import com.yami.shop.bean.param.NotifyTemplateParam;
import com.yami.shop.common.util.CacheManagerUtil;
import com.yami.shop.coupon.common.dao.CouponMapper;
import com.yami.shop.coupon.common.model.CouponUser;
import com.yami.shop.coupon.common.service.CouponService;
import com.yami.shop.coupon.common.service.CouponUserService;
import com.yami.shop.service.CouponOrderService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
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
public class CouponTask {

    private final CouponUserService couponUserService;
    private final CouponService couponService;
    private final CouponMapper couponMapper;
    private final CacheManagerUtil cacheManagerUtil;

    private final ApplicationContext applicationContext;

    private final CouponOrderService couponOrderService;

    /**
     * 删除用户失效30天以上的优惠券
     */
    @XxlJob("deleteCouponUser")
    public void deleteCouponUser() {
        log.info("删除用户失效180天以上的优惠券");
        // 删除用户失效半年以上的优惠券 参考京东
        couponUserService.deleteUnValidTimeCoupons(DateUtil.offsetDay(new Date(), -180));
    }

    /**
     * 改变用户优惠券的状态(设为失效状态)
     */
    @XxlJob("changeCouponUser")
    public void changeCouponUser() {
        Date now = new Date();
        log.info("设置用户的过期优惠券为失效状态");
        // 设置用户的过期优惠券为失效状态
        couponUserService.updateStatusByTime(now);
    }

    /**
     * 改变店铺优惠券的状态(设为过期状态)
     */
    @XxlJob("changeCoupon")
    public void changeCoupon() {
        Date now = new Date();
        log.info("设置店铺的过期优惠券为过期状态");
        // 查询需要更新的店铺id列表
        List<Long> shopIds = couponMapper.listOverdueStatusShopIds(now);
        // 取消投放
        couponService.changeCoupon(now);
        //清除缓存
        if (CollectionUtils.isNotEmpty(shopIds)) {
            for (Long shopId : shopIds) {
                cacheManagerUtil.evictCache("couponAndCouponProds", String.valueOf(shopId));
            }
        }
    }

    /**
     * 投放优惠券
     */
    @XxlJob("putOnCoupon")
    public void putOnCoupon() {
        Date now = new Date();
//        log.info("投放优惠券");
        // 投放优惠券
        couponService.putonCoupon(now);
    }


    @XxlJob("cancelPutCoupon")
    public void cancelPutCoupon() {
        Date now = new Date();
//        log.info("关闭投放优惠券");
        // 关闭投放优惠券
        couponService.cancelPutCoupon(now);
    }


    /**
     * 查询出未使用的优惠券   该任务每天执行一次
     */
    @XxlJob("remindCoupon")
    public void remindCoupon() {
        List<CouponAppConnect> list = couponUserService.queryNotUseCouponUserData();
        applicationContext.publishEvent(new CouponNotifyEvent(list));
    }

    /**
     * 查询出超出取消时间未支付的团券订单，并还原团券库存
     */
    @XxlJob("cancelCouponOrder")
    public void cancelCouponOrder() {
        Date now = new Date();
//        log.info("取消超时未支付团券订单。。。");
        // 获取5分钟之前未支付的订单
        List<CouponOrder> unPayCouponOrder = couponOrderService.getUnPayCouponOrder(1, now);
        if (CollectionUtil.isEmpty(unPayCouponOrder)) {
            return;
        }
        couponOrderService.cancelCouponOrders(unPayCouponOrder);
    }

    /**
     * 推送即将失效的优惠券短信给用户
     */
    @XxlJob("couponSendMessage")
    public void couponSendMessage() {
        // 查询即将失效的优惠券并推送短信给用户
        List<CouponUser> couponUsers = couponUserService.couponSendMessage();
        for (CouponUser couponUser : couponUsers) {
            NotifyTemplateParam param = new NotifyTemplateParam();
            param.setShopId(0L);
            param.setSendType(SendType.BUY_COUPON_EXPIRATION.getValue());
            param.setUserMobile(couponUser.getUserMobile());
            param.setCouponName(couponUser.getCouponName());
            param.setCouponDay(couponUser.getDay());
            param.setUserId(couponUser.getUserId());
            applicationContext.publishEvent(new SendMessageEvent(param));
        }
    }
}
