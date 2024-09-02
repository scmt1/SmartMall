package com.yami.shop.card.common.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("用户提货卡对象")
public class CardUserDto {

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

    @ApiModelProperty(value = "会员卡名称")
    private String cardTitle;

    private String cardImg;

    private String cardLogo;
}
