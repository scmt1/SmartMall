/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.bean.enums;

/**
 * 快递配送状态
 */
/**
 * @author Yami
 */
public enum DeliveryStatus {

    /**
     * 不在配送范围内
     */
    OUT_OF_RANGE(-1),

    /**
     * 可配送
     */
    USABLE(1)
    ;

    private Integer num;

    public Integer value() {
        return num;
    }

    DeliveryStatus(Integer num) {
        this.num = num;
    }

    public static DeliveryStatus instance(Integer value) {
        DeliveryStatus[] enums = values();
        for (DeliveryStatus statusEnum : enums) {
            if (statusEnum.value().equals(value)) {
                return statusEnum;
            }
        }
        return null;
    }
}
