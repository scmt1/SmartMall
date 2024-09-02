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
 * 品牌类型
 * @Author lth
 * @Date 2021/7/28 14:36
 */
public enum BrandType {

    /**
     * 平台品牌
     */
    PLATFORM(0),

    /**
     * 店铺自定义品牌
     */
    CUSTOMIZE(1)
    ;

    private final Integer value;

    public Integer value() {
        return value;
    }

    BrandType(Integer value) {
        this.value = value;
    }
}
