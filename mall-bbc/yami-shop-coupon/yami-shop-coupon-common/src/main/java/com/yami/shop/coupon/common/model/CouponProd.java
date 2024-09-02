/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.coupon.common.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.yami.shop.common.serializer.json.ImgJsonSerializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 优惠券商品信息
 * @author lanhai
 */
@Data
@TableName("tz_coupon_prod")
public class CouponProd implements Serializable {

    @TableId
    @ApiModelProperty("优惠券商品ID")
    private Long couponProdId;

    @ApiModelProperty("优惠券ID")
    private Long couponId;

    @ApiModelProperty("商品ID")
    private Long prodId;

    @ApiModelProperty("商品名称")
    @TableField(exist=false)
    private String prodName;

    @ApiModelProperty("商品主图")
    @TableField(exist=false)
    @JsonSerialize(using = ImgJsonSerializer.class)
    private String pic;
}
