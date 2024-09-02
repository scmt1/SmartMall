/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.bean.pay;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 支付信息
 * @author LGH
 */
@Data
public class PayInfoDto {

    /**
     * 支付信息，如商品名称
     */
    private String body;

    /**
     * 扩展信息
     */
    private String extParam;

    /**
     * 支付单号
     */
    private String payNo;

    /**
     * 付款金额
     */
    private Double payAmount;

    /**
     * 支付方式
     */
    private Integer payType;

    /**
     * api回调域名
     */
    private String apiNoticeUrl;

    /**
     * 支付完成会跳地址
     */
    private String returnUrl;

    /**
     * 第三方用户id
     */
    private String bizUserId;

    /**
     * 支付回调地址类型
     */
    private Integer backType;


    private Long shopId;

    @ApiModelProperty(value = "支付密码  支付方式为红云时必传")
    private String payPassword;

    @ApiModelProperty(value = "支付卡号  支付方式为提货卡时必传")
    private String cardCode;
}
