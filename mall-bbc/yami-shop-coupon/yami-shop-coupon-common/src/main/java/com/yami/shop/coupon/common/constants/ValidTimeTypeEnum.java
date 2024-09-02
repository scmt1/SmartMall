/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.coupon.common.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 优惠券状态
 * @author lhd
 */
@Getter
@AllArgsConstructor
public enum ValidTimeTypeEnum {

    /**   固定时间       */
    FIXED(1, "固定时间"),

    /**   领取后生效   */
    RECEIVE(2, "领取后生效")
    ;
    private int value;
    private String desc;

    public Integer value() {
        return value;
    }
}
