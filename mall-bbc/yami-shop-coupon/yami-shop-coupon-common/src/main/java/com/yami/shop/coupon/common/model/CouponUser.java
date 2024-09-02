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

/**
 * 优惠券商品信息
 * @author lanhai
 */
@Data
@TableName("tz_coupon_user")
public class CouponUser implements Serializable {
    private static final long serialVersionUID = 129965893236674626L;
    /**
     * 优惠券用户ID
     */
    @TableId
    private Long couponUserId;

    /**
     * 优惠券ID
     */
    private Long couponId;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 领券时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date receiveTime;

    /**
     * 优惠券状态 0:失效 1:有效 2:使用过
     */
    private Integer status;

    /**
     * 开始时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date userStartTime;

    /**
     * 结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date userEndTime;

    /**
     * 是否删除
     */
    private Integer isDelete;

    /**
     * 优惠券
     */
    @TableField(exist = false)
    private Coupon coupon;

    /**
     * 优惠券使用记录id
     */
    @TableField(exist = false)
    private Long couponUseRecordId;

    /**
     * 用户领取的优惠券数量
     */
    @TableField(exist = false)
    private Integer curUserReceiveCount;

    /**
     * 优惠券数量
     */
    @TableField(exist = false)
    private Integer couponNum;


    @TableField(exist = false)
    private Integer userHasCount;

    @TableField(exist = false)
    private String nickName;

    @TableField(exist = false)
    private String userMobile;

    @TableField(exist = false)
    private String couponName;

    @TableField(exist = false)
    @ApiModelProperty(value = "离当前时间到期天数")
    private Integer day;
}
