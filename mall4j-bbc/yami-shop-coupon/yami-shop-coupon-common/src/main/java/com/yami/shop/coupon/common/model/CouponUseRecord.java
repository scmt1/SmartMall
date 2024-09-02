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
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 优惠券使用记录
 *
 * @author yami code generator
 * @date 2019-05-15 09:04:57
 */
@Data
@TableName("tz_coupon_use_record")
public class CouponUseRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 优惠券使用id
     */
    @TableId
    private Long couponUseRecordId;
    @ApiModelProperty(value = "创建时间")
    private Date createTime;
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
    /**
     * 用户优惠券id
     */
    private Long couponUserId;

    /**
     * 用户id
     */
    private String userId;
    /**
     * 订单编码
     */
    private String orderNumber;
    /**
     * 金额
     */
    private Double amount;
    /**
     * 使用时间
     */
    private Date useTime;

    /**
     * 使用状态(1:冻结 2:已使用 3:已退还)
     */
    private Integer status;

    @ApiModelProperty(value = "核券店铺ID")
    private Long writeOffShopId;

    @ApiModelProperty(value = "类型(0：抖音)")
    private Integer type;

    @ApiModelProperty(value = "抖音优惠券编号")
    private String dyCouponNum;

    @ApiModelProperty(value = "店铺实得金额")
    private Double shopAmount;

    @ApiModelProperty(value = "物业承担金额")
    private Double wyAmount;

    @ApiModelProperty(value = "店铺核券数量")
    @TableField(exist = false)
    private Integer writeOffNum;

    /**
     * 用户领取优惠券
     */
    @TableField(exist = false)
    private CouponUser couponUser;

    //店铺名称
    @TableField(exist = false)
    private String shopName;

    //优惠券名称
    @TableField(exist = false)
    private Long couponId;

    //优惠券名称
    @TableField(exist = false)
    private String couponName;

    @ApiModelProperty(value = "开始时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(exist = false)
    private Date beginTime;

    @ApiModelProperty(value = "结束时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(exist = false)
    private Date endTime;

    @ApiModelProperty(value = "使用人")
    @TableField(exist = false)
    private String nickName;

    @ApiModelProperty(value = "使用人电话号码")
    @TableField(exist = false)
    private String userMobile;

    @ApiModelProperty(value = "优惠券发放店铺")
    @TableField(exist = false)
    private Long couponShopId;

    @ApiModelProperty(value = "优惠券开始时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(exist = false)
    private Date couponStartTime;

    @ApiModelProperty(value = "优惠券结束时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(exist = false)
    private Date couponEndTime;

    @ApiModelProperty(value = "投放时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(exist = false)
    private Date launchTime;

    @ApiModelProperty(value = "取消投放时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(exist = false)
    private Date launchEndTime;

    @ApiModelProperty(value = "优惠券过期状态 0:过期 1:未过期")
    @TableField(exist = false)
    private Integer overdueStatus;

    @ApiModelProperty(value = "优惠券投放状态 0:未投放 1:投放 -1取消投放")
    @TableField(exist = false)
    private Integer putonStatus;

    @ApiModelProperty(value = "优惠券投放来源 1：平台 2：工会")
    @TableField(exist = false)
    private Integer putSource;

    @TableField(exist = false)
    @ApiModelProperty(value = "实际累计核销金额")
    private Double actualAmount;

    @TableField(exist = false)
    @ApiModelProperty(value = "累计核销笔数")
    private Integer writeOffCount;

    @TableField(exist = false)
    @ApiModelProperty(value = "昨日核销金额")
    private Double yesterdayAmount;

    @TableField(exist = false)
    @ApiModelProperty(value = "昨日核销笔数")
    private Integer yesterdayCount;

    @TableField(exist = false)
    @ApiModelProperty(value = "今日核销金额")
    private Double toDayAmount;

    @TableField(exist = false)
    @ApiModelProperty(value = "今日核销笔数")
    private Integer toDayCount;

}
