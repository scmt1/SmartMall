/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.bean.app.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author Yami
 */
@Data
@ApiModel(value = "支付参数")
public class PayParam {

    @NotBlank(message = "订单号不能为空")
    @ApiModelProperty(value = "订单号", required = true)
    private String orderNumbers;

    @NotNull(message = "支付方式不能为空")
    @ApiModelProperty(value = "支付方式 (1:微信小程序支付 2:支付宝 3微信扫码支付 4 微信h5支付 5微信公众号支付 6支付宝H5支付 7支付宝APP支付 8微信APP支付 9余额支付 10全球PayPal支付 11计全支付 12现金支付 13计全微信小程序支付 14红云余额支付 15提货卡支付 16组合支付)", required = true)
    private Integer payType;

    @ApiModelProperty(value = "支付完成回跳地址", required = true)
    private String returnUrl;


    @ApiModelProperty(value = "支付方式", required = true)
    private String wayCode; //QR_CASHIER 聚合扫码(用户扫商家)    AUTO_BAR	聚合条码(商家扫用户)


    @ApiModelProperty(value = "支付的用户id", required = true)
    private String userId;

    @ApiModelProperty(value = "支付条形码，wayCode=AUTO_BAR时必传")
    private String authCode;

    @ApiModelProperty(value = "支付金额")
    private Double totalAmount;

    @ApiModelProperty(value = "实际支付金额")
    private Double actualAmount;

    @ApiModelProperty(value = "车辆支付类型")
    private String carPayType;

    @ApiModelProperty(value = "车辆订单号")
    private String tradeno;

    @ApiModelProperty(value = "车牌号")
    private String carNo;

    @ApiModelProperty(value = "车辆入场时间")
    private String entranceTime;

    @ApiModelProperty(value = "停车时长")
    private String parkTime;

    @ApiModelProperty(value = "支付密码  支付方式为红云时必传")
    private String payPassword;

    @ApiModelProperty(value = "提货卡名称")
    private String cardName;

    @ApiModelProperty(value = "提货卡金额")
    private Double cardBalance;

    @ApiModelProperty(value = "提货卡赠送券id")
    private Long giveCouponId;

    @ApiModelProperty(value = "购买类型 (1:商品 2:买券/提货卡)")
    private Integer buyType;

    @ApiModelProperty(value = "支付卡号  支付方式为提货卡时必传")
    private String cardCode;
}
