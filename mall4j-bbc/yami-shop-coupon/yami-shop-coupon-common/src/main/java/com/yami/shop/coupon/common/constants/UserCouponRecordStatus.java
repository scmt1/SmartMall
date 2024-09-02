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
public enum UserCouponRecordStatus {

    /**
     * 1:冻结
     */
    FREEZE(1),

    /**
     * 使用过
     */
    USED(2),
    /**
     * 3:退回
     */
    REFUND(3)
    ;

    private Integer value;

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    UserCouponRecordStatus(Integer value) {
        this.value = value;
    }
}
