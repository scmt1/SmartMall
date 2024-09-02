package com.yami.shop.card.common.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("tz_card_user")
@ApiModel("用户会员卡信息")
public class CardUser implements Serializable {

    @TableId
    private Long cardUserId;

    private Long cardId;

    /**
     * 用户ID
     */
    private String userId;

    private Double score;

    private Double balance;

    private Date receiveTime;

    private Integer status;

    private Integer isDelete;

    private String cardNumber;

    private Long couponId;

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

    @TableField(exist = false)
    private String cardTitle;

    @TableField(exist = false)
    private String userMobile;

    @TableField(exist = false)
    private String nickName;

    @TableField(exist = false)
    @ApiModelProperty(value = "卡(券)原始金额")
    private Double cardAmount;

    @TableField(exist = false)
    @ApiModelProperty(value = "订单号")
    private String orderNumber;

    @TableField(exist = false)
    @ApiModelProperty(value = "离当前时间到期天数")
    private Integer day;

    @TableField(exist = false)
    @ApiModelProperty(value = "核销方式提示")
    private String writeOffTip;
}
