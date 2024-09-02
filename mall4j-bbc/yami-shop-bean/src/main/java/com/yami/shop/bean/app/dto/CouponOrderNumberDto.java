/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.bean.app.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Yami
 */
@Data
public class CouponOrderNumberDto {

    @ApiModelProperty(value = "订单号",required=true)
    private String couponOrderNumber;

    @ApiModelProperty(value = "错误提交，为1时表示前端防重复提交没做好，需要返回商品详情",required=true)
    private Integer duplicateError;
    public CouponOrderNumberDto(String couponOrderNumber) {
        this.couponOrderNumber = couponOrderNumber;
    }
}
