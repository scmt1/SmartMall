package com.yami.shop.card.common.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@ApiModel("会员卡对象")
public class CardDto {
    private Long cardId;

    @ApiModelProperty(value = "店铺ID")
    private Long shopId;

    @ApiModelProperty(value = "店铺名称")
    private String shopName;

    @ApiModelProperty(value = "店铺logo")
    private String shopLogo;


    @ApiModelProperty(value = "会员卡名称")
    private String cardTitle;

    private String useInfo;

    private String privilege;

    private String mobile;

    private Date createTime;


    private Double discount;

    private Integer integralRatio;


    private Integer isDelete;

    private String cardImg;

    private String cardLogo;


    private Long cardUserId;

    private String cardNumber;

    private String encrypCardNumber;

    private Integer status;

    /**
     * 用户ID
     */
    private String userId;

    private Double score;

    private Double balance;

    private Date receiveTime;

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

    @ApiModelProperty(value = "买卡(券)类型：0 工会团卡(券) 1个人团卡")
    private Integer buyCardType;
}
