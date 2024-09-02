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

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Yami
 */
@Data
@TableName("tz_coupon_order")
public class CouponOrder implements Serializable {
    private static final long serialVersionUID = 6222259729062826852L;
    /**
     * 订单ID
     */
    @TableId
    private Long couponOrderId;

    /**
     * 优惠券ID
     */
    private Long couponId;

    /**
     * 店铺ID
     */
    private Long shopId;

    /**
     * 订购用户ID
     */
    private String userId;

    /**
     * 订购流水号
     */
    private String couponOrderNumber;

    /**
     * 实际总值
     */
    private Double groupAmount;

    /**
     * 支付方式 1 微信支付 2 支付宝
     */
    private Integer payType;

    /**
     * 订单状态 1:待付款 2:成功 3:失败
     */
    private Integer status;

    /**
     * 订购时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 订单更新时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 付款时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date payTime;

    /**
     * 取消时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date cancelTime;

    /**
     * 是否已经支付，1：已经支付过，0：，没有支付过
     */
    private Integer isPayed;

    /**
     * 用户订单删除状态，0：没有删除， 1：回收站， 2：永久删除
     */
    private Integer deleteStatus;

    /**
     * 是否可以退款 1.可以 0不可以
     */
    @ApiModelProperty(value = "是否可以退款 1.可以 0不可以")
    private Integer isRefund;


    @TableField(exist = false)
    private String PayNo;

    @TableField(exist = false)
    private Double payAmount;
}
