/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.card.common.model;

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
 * 提货卡使用记录
 *
 * @author yami code generator
 * @date 2019-05-15 09:04:57
 */
@Data
@TableName("tz_card_use_record")
public class CardUseRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 优惠券使用id
     */
    @TableId
    private Long cardUseRecordId;
    @ApiModelProperty(value = "创建时间")
    private Date createTime;
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;
    /**
     * 用户提货卡id
     */
    private Long cardUserId;

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
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date useTime;

    /**
     * 使用状态(1:冻结 2:已使用 3:已退还)
     */
    private Integer status;

    /**
     * 结算状态(0：未结算，1：已结算)
     */
    private Integer settlement;

    @ApiModelProperty(value = "核券店铺ID")
    private Long writeOffShopId;

    @ApiModelProperty(value = "卡编号")
    private String cardCode;

    @ApiModelProperty(value = "核销人员id")
    private Long writeOffPersonId;

    @ApiModelProperty(value = "店铺实得金额")
    private Double shopAmount;

    @ApiModelProperty(value = "物业承担金额")
    private Double wyAmount;

    /**
     * 核销人员昵称
     */
    @TableField(exist = false)
    private String employeeNickName;

    @TableField(exist = false)
    private CardUser cardUser;

    //店铺名称
    @TableField(exist = false)
    private String shopName;

    @TableField(exist = false)
    private Long cardId;

    @TableField(exist = false)
    private String cardTitle;


    @ApiModelProperty(value = "使用人")
    @TableField(exist = false)
    private String nickName;

    @ApiModelProperty(value = "使用人电话号码")
    @TableField(exist = false)
    private String userMobile;

    @ApiModelProperty(value = "卡号")
    @TableField(exist = false)
    private String cardNumber;

    @ApiModelProperty(value = "发放店铺")
    @TableField(exist = false)
    private Long cardShopId;

    @ApiModelProperty(value = "使用开始时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(exist = false)
    private Date startTime;

    @ApiModelProperty(value = "使用结束时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(exist = false)
    private Date endTime;

    @TableField(exist = false)
    private Integer cardType;

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

    @TableField(exist = false)
    @ApiModelProperty(value = "买卡(券)类型：0 工会团卡(券) 1个人团卡")
    private Integer buyCardType;

    @ApiModelProperty(value = "店铺核销提货卡(券)次数")
    @TableField(exist = false)
    private Integer writeOffNum;

    @ApiModelProperty(value = "店铺核销提货卡(券)总金额")
    @TableField(exist = false)
    private Double writeOffTotalAmount;

    @TableField(exist = false)
    private List<Long> cardUseRecordIds; //记录ids

    @ApiModelProperty(value = "核券店铺IDs")
    @TableField(exist = false)
    private List<Long> writeOffShopIds;
}
