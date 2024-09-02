/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.combo.multishop.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author
 * @date 2019/9/23 19:01
 */
@Data
@ApiModel("用户勾选套餐信息")
public class ComboSkuDto implements Serializable {

    @ApiModelProperty(value = "套餐id")
    private Long comboId;

    @ApiModelProperty(value = "套餐名称")
    private String name;

    @ApiModelProperty(value = "sku id")
    private Long skuId;

    @ApiModelProperty(value = "sku 价格")
    private Double price;

    @ApiModelProperty(value = "sku 套餐价格")
    private Double matchingPrice;

    @ApiModelProperty(value = "起搭数量")
    private Integer leastNum;
}
