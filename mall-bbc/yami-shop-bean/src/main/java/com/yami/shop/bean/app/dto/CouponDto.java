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

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.yami.shop.common.serializer.json.ImgJsonSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author Yami
 */
@Data
@ApiModel("优惠券对象")
public class CouponDto implements Serializable {

    @ApiModelProperty(value = "优惠券id")
    private Long couponId;

    @ApiModelProperty(value = "店铺ID")
    private Long shopId;

    @ApiModelProperty(value = "店铺名称")
    private String shopName;

    @JsonSerialize(using = ImgJsonSerializer.class)
    @ApiModelProperty(value = "店铺logo")
    private String shopLogo;

    @ApiModelProperty(value = "优惠券名称")
    private String couponName;

    @ApiModelProperty(value = "副标题")
    private String subTitle;

    @ApiModelProperty(value = "优惠类型 1:代金券 2:折扣券 3:兑换券")
    private Integer couponType;

    @ApiModelProperty(value = "使用条件")
    private Double cashCondition;

    @ApiModelProperty(value = "减免金额")
    private Double reduceAmount;

    @ApiModelProperty(value = "折扣额度")
    private Double couponDiscount;

    @ApiModelProperty(value = "生效类型 1:固定时间 2:领取后生效")
    private Integer validTimeType;

    @ApiModelProperty(value = "开始时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @ApiModelProperty(value = "结束时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    @ApiModelProperty(value = "领券后X天起生效")
    private Integer afterReceiveDays;

    @ApiModelProperty(value = "时间类型 1：按天 2：按小时 3：按分钟")
    private Integer timeType;

    @ApiModelProperty(value = "有效天数")
    private Integer validDays;

    @ApiModelProperty(value = "库存")
    private Integer stocks;

    @ApiModelProperty(value = "初始库存")
    private Integer sourceStock;

    @ApiModelProperty(value = "适用商品类型 0全部商品参与 1指定商品参与 2指定商品不参与")
    private Integer suitableProdType;

    @ApiModelProperty(value = "指定商品图片")
    @JsonSerialize(using = ImgJsonSerializer.class)
    private String pic;

    @ApiModelProperty(value = "指定商品id")
    private Long prodId;

    @ApiModelProperty(value = "指定商品价格")
    private Double price;

    @ApiModelProperty(value = "每个用户领券上限")
    private Integer limitNum;

    @ApiModelProperty(value = "优惠券状态 0:失效 1:有效 2:使用过")
    private Integer status;

    @ApiModelProperty(value = "优惠券过期状态 0:过期 1:未过期")
    private Integer overdueStatus;

    @ApiModelProperty(value = "优惠券投放状态 0:未投放 1:投放")
    private Integer putonStatus;

    @ApiModelProperty(value = "优惠券图片")
    private String couponImg;

    @ApiModelProperty(value = "领取规则")
    private String claimRules;

    @ApiModelProperty(value = "使用规则")
    private String useRules;

    @ApiModelProperty(value = "门店优惠")
    private String storePreferential;

    @ApiModelProperty(value = "取消投放时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date launchEndTime;

    @ApiModelProperty(value = "用户优惠券id")
    private Long couponUserId;

    @ApiModelProperty(value = "领券时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date receiveTime;

    @ApiModelProperty(value = "参与优惠券的商品集合")
    private List<ProductDto> prods;

    @ApiModelProperty(value = "当前登陆用户领取的优惠券数量")
    private Integer curUserReceiveCount;

    @ApiModelProperty(value = "当前登陆用户可用的优惠券数量")
    private Integer userHasCount;

    @ApiModelProperty(value = "指定店铺优惠券可使用店铺")
    private List<Long> useShopIds;

    @ApiModelProperty(value = "用户优惠券编号")
    private String couponUserNumber;

    @ApiModelProperty(value = "投放时间")
    private Date launchTime ;

    @ApiModelProperty(value = "是否团购 0:否 1:是")
    private Integer isGroup;

    @ApiModelProperty(value = "团购金额")
    private Double groupAmount;

    @ApiModelProperty(value = "券有效期开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date userStartTime;

    @ApiModelProperty(value = "券有效期结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date userEndTime;
}
