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
 * @author Yami
 */
public enum RefundStsType {
    /**
     * 待审核
     */
    PROCESS(1),
    /**
     * 同意
     */
    AGREE(2),
    /**
     * 不同意
     */
    DISAGREE(3);

    private Integer num;

    RefundStsType(Integer num) {
        this.num = num;
    }

    public Integer value() {
        return num;
    }

    public static RefundStsType instance(Integer value) {
        RefundStsType[] enums = values();
        for (RefundStsType statusEnum : enums) {
            if (statusEnum.value().equals(value)) {
                return statusEnum;
            }
        }
        return null;
    }
}