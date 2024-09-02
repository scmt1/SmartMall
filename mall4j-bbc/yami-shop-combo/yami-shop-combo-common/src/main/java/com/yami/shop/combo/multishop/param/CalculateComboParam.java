/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.combo.multishop.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @author
 * @date 2019/9/23 19:01
 */
@Data
@ApiModel("用户勾选套餐信息")
public class CalculateComboParam implements Serializable {

    @NotNull(message = "套餐id不能为空")
    @ApiModelProperty(value = "套餐id")
    private Long comboId;

    @NotNull(message = "主商品id不能为空")
    @ApiModelProperty(value = "主商品id")
    private Long prodId;

    @NotNull(message = "主商品sku id不能为空")
    @ApiModelProperty(value = "主商品Sku id")
    private Long skuId;

    @ApiModelProperty(value = "搭配商品Sku id列表")
    private List<Long> matchingSkuIds;

    @ApiModelProperty(value = "搭配商品id列表")
    private List<Long> matchingProdIds;

    @NotNull(message = "套餐数量不能为空")
    @ApiModelProperty(value = "套餐数量")
    private Integer comboNum;
}
