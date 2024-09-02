package com.yami.shop.card.common.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@TableName("tz_card")
@ApiModel("会员卡")
public class Card implements Serializable {
    @TableId
    private Long cardId;

    private String cardTitle;

    private String useInfo;

    private String privilege;

    private String mobile;

    private Date createTime;

    private Long shopId;

    @TableLogic(delval = "1", value = "0")
    private Integer isDelete;


    private String cardImg;

    private String cardLogo;

    private Double balance;

    private String cardNumber;
    private String password;

    private Integer cardType;
    private Integer status; //状态：0 未制卡(券) 1 未出售 2 已出售 3 已绑定 4 已冻结 5 已置换 6 已核销 -1 已失效 7 已作废

    private Integer suitableProdType;

    @TableField(exist = false)
    private Long cardNum;

    @TableField(exist = false)
    private List<CardShop> cardShops;

    private Integer createType;

    private String cardCode;//卡编码


    private String cardPrefix; //卡前缀

    private String batchNumber;//批次号

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date batchTime; //批次时间

    private Integer batchStatus; //批次状态

    private String buyUnit; //购买单位

    private String displaceCardCode;//置换实物券号

    private Integer buyCardType; //买卡(券)类型：0 工会团卡(券) 1个人团卡

    /**
     * 开始时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date userStartTime;

    /**
     * 结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date userEndTime;

    private String reason;//卡作废原因

    private Integer isShopAmount; //商家是否承担金额(0：否，1：是)

    /**
     * 出售时间时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date sellTime;

    private String buyReason;//购买事由

    @TableField(exist = false)
    private String shopName;

    @TableField(exist = false)
    private Double totalAmount; //批次总金额

    @TableField(exist = false)
    private List<String> batchNumbers; //批次号列表

    @TableField(exist = false)
    private List<Long> cardIds; //cardId列表

    @TableField(exist = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;
    @TableField(exist = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    @TableField(exist = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date sellStartTime;//出售开始时间
    @TableField(exist = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date sellEndTime;//出售结束时间

    @TableField(exist = false)
    private Integer startNum; //开始编号
    @TableField(exist = false)
    private Integer endNum; //结束编号

    @TableField(exist = false)
    private String sellRecordNum; //编号

    @TableField(exist = false)
    private List<Card> children;//子提货卡

    @TableField(exist = false)
    private Double remainingAmount; //剩余金额

    @TableField(exist = false)
    private Integer soldNum; //已售数量

    @TableField(exist = false)
    private String couponCardCodes; //置换实物券号

    @TableField(exist = false)
    private String encrypCardNumber;

    @TableField(exist = false)
    private List<Card> cardInfos;//置换多卡信息

    @TableField(exist = false)
    @ApiModelProperty(value = "累计卡(券)数量")
    private Integer cardCount;

    @TableField(exist = false)
    @ApiModelProperty(value = "累计卡(券)金额")
    private Double cardTotalAmount;
}
