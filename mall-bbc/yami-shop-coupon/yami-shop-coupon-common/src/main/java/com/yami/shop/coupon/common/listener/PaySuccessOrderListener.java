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

import com.google.common.collect.Lists;
import com.yami.shop.bean.event.BatchBindCouponEvent;
import com.yami.shop.bean.event.PaySuccessOrderEvent;
import com.yami.shop.bean.order.PaySuccessOrderOrder;
import com.yami.shop.common.config.Constant;
import com.yami.shop.coupon.common.constants.UserCouponRecordStatus;
import com.yami.shop.coupon.common.service.CouponService;
import com.yami.shop.coupon.common.service.CouponUseRecordService;
import com.yami.shop.coupon.common.service.CouponUserService;
import lombok.AllArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * 优惠券
 * @author lanhai
 */
@Component("couponPaySuccessListener")
@AllArgsConstructor
public class PaySuccessOrderListener {

    private final CouponUseRecordService couponUseRecordService;
    private final CouponService couponService;
    private final CouponUserService couponUserService;

    /**
     * 更新优惠券使用记录状态
     */
    @EventListener(PaySuccessOrderEvent.class)
    @Order(PaySuccessOrderOrder.COUPON)
    public void couponPaySuccessListener(PaySuccessOrderEvent event) {
        if(Objects.equals(event.getOrders().get(0).getShopId(),Constant.PLATFORM_SHOP_ID)){
            return;
        }
        List<String> orderNumberList = Lists.newArrayList();
        List<com.yami.shop.bean.model.Order> orders = event.getOrders();

        for (com.yami.shop.bean.model.Order order : orders) {
            orderNumberList.add(order.getOrderNumber());
        }
        if (CollectionUtils.isNotEmpty(orderNumberList)) {
            couponUseRecordService.batchUpdateRecordByStatusAndOrderNums(UserCouponRecordStatus.USED.getValue(), orderNumberList);
        }

    }
    /**
     * 批量绑定优惠券，如果已达上限等领取不了的情况直接退出。
     */
    @EventListener(BatchBindCouponEvent.class)
    public void batchBindCouponEventListener(BatchBindCouponEvent event) {
        couponService.batchBindCouponByIds(event.getCouponIds(),event.getUserId(),event.getShopId());
    }
}
