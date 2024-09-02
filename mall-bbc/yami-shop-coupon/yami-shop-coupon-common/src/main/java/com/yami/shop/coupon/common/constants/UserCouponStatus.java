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

/**
 * @author FrozenWatermelon
 * @date 2020/12/28
 */
public enum UserCouponStatus {
    /**
     * 0:失效
     */
    INVALID(0),

    /**
     * 1:有效
     */
    EFFECTIVE(1),

    /**
     * 使用过
     */
    USED(2)
    ;

    private Integer value;

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    UserCouponStatus(Integer value) {
        this.value = value;
    }
}
