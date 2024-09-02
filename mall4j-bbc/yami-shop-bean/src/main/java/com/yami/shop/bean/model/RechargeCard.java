package com.yami.shop.bean.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("tz_recharge_card")
public class RechargeCard implements Serializable {
    @TableId
    @ApiModelProperty(value = "id")
    private Long id;
    @ApiModelProperty(value = "充值卡类型")
    private Integer type;
    @ApiModelProperty(value = "卡号")
    private String cardNumber;
    @ApiModelProperty(value = "金额")
    private Double money;
    @ApiModelProperty(value = "创建时间")
    private Date createTime;
    @ApiModelProperty(value = "状态 1有效 0失效")
    private Integer state;
    @ApiModelProperty(value = "失效时间")
    private Date expirationTime;
    @ApiModelProperty(value = "充值时间")
    private Date rechargeTime;
    private String rechargeUserId;
}
