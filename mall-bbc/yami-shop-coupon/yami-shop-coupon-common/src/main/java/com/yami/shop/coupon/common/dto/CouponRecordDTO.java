/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.coupon.common.dto;

import lombok.Data;

/**
 * @author FrozenWatermelon
 * @date 2020/12/23
 */
@Data
public class CouponRecordDTO {

    /**
     * 订单id
     */
    private String orderNumbers;

    /**
     * 优惠券id
     */
    private Long couponId;

    /**
     * 用户优惠券id
     */
    private Long couponUserId;

    /**
     * 优惠金额
     */
    private Double reduceAmount;

    /**
     * 核销店铺id
     */
    private Long writeOffShopId;
}
