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
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 优惠券
 * @author lanhai
 */
@Data
@TableName("tz_coupon")
@ApiModel("优惠券")
public class Coupon implements Serializable {
    private static final long serialVersionUID = 8018312153820119913L;

    @TableId
    @ApiModelProperty(value = "优惠券id")
    private Long couponId;

    @ApiModelProperty(value = "店铺ID")
    private Long shopId;

    @ApiModelProperty(value = "优惠券名称")
    private String couponName;

    @ApiModelProperty(value = "副标题")
    private String subTitle;

    @ApiModelProperty(value = "优惠类型 1:代金券 2:折扣券 3:兑换券")
    private Integer couponType;

    @ApiModelProperty(value = "类型 1:线上券 2:线下券")
    private Integer type;

    @ApiModelProperty(value = "投放来源 1：平台 2：工会")
    private Integer putSource;

    @ApiModelProperty(value = "使用条件")
    private Double cashCondition;

    @ApiModelProperty(value = "减免金额")
    private Double reduceAmount;

    @ApiModelProperty(value = "折扣额度")
    private Double couponDiscount;

    @ApiModelProperty(value = "生效类型 1:固定时间 2:领取后生效")
    private Integer validTimeType;

    @ApiModelProperty(value = "投放时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date launchTime;

    @ApiModelProperty(value = "取消投放时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date launchEndTime;

    @ApiModelProperty(value = "开始时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @ApiModelProperty(value = "结束时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    @ApiModelProperty(value = "领券后X天起生效")
    private Integer afterReceiveDays;

    @ApiModelProperty(value = "时间类型 1：按天 2：按小时 3：按分钟")
    private Integer timeType;

    @ApiModelProperty(value = "有效天数")
    private Integer validDays;

    @ApiModelProperty(value = "库存")
    private Integer stocks;

    @ApiModelProperty(value = "原始库存")
    private Integer sourceStock;

    @ApiModelProperty(value = "适用商品类型 0全部商品参与 1指定商品参与 2指定商品不参与")
    private Integer suitableProdType;

    @ApiModelProperty(value = "每个用户领券上限，如不填则默认为1")
    private Integer limitNum;

    @ApiModelProperty(value = "版本号")
    private Integer version;

    @ApiModelProperty(value = "是否积分优惠券 0不是 1是")
    private Integer isScoreType;

    @ApiModelProperty(value = "积分价格")
    private Integer scorePrice;

    @ApiModelProperty(value = "优惠券过期状态 0:过期 1:未过期")
    private Integer overdueStatus;

    @ApiModelProperty(value = "优惠券投放状态 0:未投放 1:投放 -1取消投放")
    private Integer putonStatus;

    @ApiModelProperty(value = "优惠券图片")
    private String couponImg;

    @ApiModelProperty(value = "领取规则")
    private String claimRules;

    @ApiModelProperty(value = "使用规则")
    private String useRules;

    @ApiModelProperty(value = "门店优惠")
    private String storePreferential;

    @ApiModelProperty(value = "优惠商品")
    @TableField(exist = false)
    private List<CouponProd> couponProds;

    @ApiModelProperty(value = "优惠券所在的店铺")
    @TableField(exist = false)
    private String shopName;

    @ApiModelProperty(value = "获取方式  0=客户领取 1=平台发放")
    private Integer getWay = 0;

    @ApiModelProperty(value = "是否团购 0:否 1:是")
    private Integer isGroup;

    @ApiModelProperty(value = "团购金额")
    private Double groupAmount;

    @ApiModelProperty(value = "提货卡/券名称")
    private String cardName;

    @ApiModelProperty(value = "赠送券ID")
    private Long giveCouponId;

    @ApiModelProperty(value = "是否需要付费会员可领取1：否 2：是")
    private Integer isPayMember;

    @ApiModelProperty(value = "优惠券领取次数")
    @TableField(exist = false)
    private Integer takeNum = 0;

    @ApiModelProperty(value = "优惠券微商城使用次数")
    @TableField(exist = false)
    private Integer useNum = 0;

    @ApiModelProperty(value = "可以使用的店铺")
    @TableField(exist = false)
    private List<CouponShop> couponShops;

    @ApiModelProperty(value = "总剩余库存")
    @TableField(exist = false)
    private Integer stocksTotal;

    @ApiModelProperty(value = "总原始库存")
    @TableField(exist = false)
    private Integer sourceStockTotal;

    @ApiModelProperty(value = "总优惠券领取数量")
    @TableField(exist = false)
    private Integer takeNumTotal = 0;

    @ApiModelProperty(value = "总优惠券使用数量")
    @TableField(exist = false)
    private Integer useNumTotal = 0;

    @ApiModelProperty(value = "昨日优惠券领取数量")
    @TableField(exist = false)
    private Integer yesterDayReceiveTotal = 0;

    @ApiModelProperty(value = "今日优惠券领取数量")
    @TableField(exist = false)
    private Integer todayReceiveTotal = 0;

    @ApiModelProperty(value = "昨日优惠券使用数量")
    @TableField(exist = false)
    private Integer yesterDayUseTotal = 0;

    @ApiModelProperty(value = "今日优惠券使用数量")
    @TableField(exist = false)
    private Integer todayUseTotal = 0;
}
