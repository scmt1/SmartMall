/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.combo.multishop.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Citrus
 * @date 2021/11/10 10:53
 */
@Data
public class CalculateComboVO {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "原价")
    private Double price;

    @ApiModelProperty(value = "套餐价")
    private Double matchingPrice;

    @ApiModelProperty(value = "节省金额")
    private Double reduceAmount;
}
