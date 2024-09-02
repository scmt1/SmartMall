/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.coupon.common.listener;


import com.yami.shop.bean.event.CancelOrderEvent;
import com.yami.shop.bean.order.CancelOrderOrder;
import com.yami.shop.coupon.common.service.CouponUseRecordService;
import com.yami.shop.coupon.common.service.CouponUserService;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 取消订单事件
 * @author lanhai
 */
@Component("couponCancelOrderListener")
@AllArgsConstructor
public class CancelOrderListener {

//    final private CouponUseRecordService couponUseRecordService;
    final private CouponUserService couponUserService;

    @EventListener(CancelOrderEvent.class)
    @Order(CancelOrderOrder.COUPON)
    public void couponCancelOrderEvent(CancelOrderEvent event) {
        // 将优惠券状态改为可用状态并删除优惠券记录
        String userId = event.getOrder().getUserId();
        String orderNumber = event.getOrder().getOrderNumber();
        couponUserService.cancelOrder(userId, orderNumber);
//        couponUseRecordService.unlockCoupon(event.getOrder());
    }

}
