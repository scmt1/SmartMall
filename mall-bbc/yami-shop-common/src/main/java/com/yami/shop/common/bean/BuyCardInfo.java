/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.common.bean;

import lombok.Data;

/**
 * 买提货卡配置信息
 * @author LGH
 */
@Data
public class BuyCardInfo {

    private Long couponId; //优惠券id

    private Double actualTotal; //金额

    private Long shopId; //店铺id

    private String cardName; //卡名称

    private Long giveCouponId; //赠送券id
}
