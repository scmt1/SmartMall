/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.bean.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Yami
 */
@Data
@TableName("tz_registr_item")
public class RegistrItem implements Serializable {

    private static final long serialVersionUID = 7307405761190788407L;

    @TableId(type = IdType.AUTO)
    @ApiModelProperty("挂单id")
    private Long registrItemId;

    @ApiModelProperty(value = "挂单流水号")
    private String registrNumber;

    @ApiModelProperty(value = "店铺id")
    private Long shopId;

    @ApiModelProperty(value = "商品id")
    private Long prodId;

    @ApiModelProperty(value = "skuId")
    private Long skuId;

    @ApiModelProperty(value = "用户ID")
    private String userId;

    @ApiModelProperty(value = "产品个数")
    private Integer prodCount;

    @ApiModelProperty("产品名称")
    private String prodName;

    @ApiModelProperty("产品主图片路径")
    private String pic;

    @ApiModelProperty("产品价格")
    private Double price;

    @ApiModelProperty("商品总金额")
    private Double productTotalAmount;
}
