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
 * @author lanhai
 */
@Getter
@AllArgsConstructor
public enum CouponStatusEnum {
    /**   取消投放   */
    CANCEL(-1,"取消投放"),

    /**   自动投放   */
    AUTO_LAUNCH(0, "自动投放"),

    /**   投放       */
    PUT_ON(1, "投放"),

    /**   违规下架   */
    OFFLINE(2, "违规下架"),

    /**   等待审核   */
    WAIT_AUDIT(3, "等待审核"),

    /** 暂不投放*/
    NOT_LAUNCH(4, "暂不投放"),
    ;
    private int value;
    private String desc;
}
