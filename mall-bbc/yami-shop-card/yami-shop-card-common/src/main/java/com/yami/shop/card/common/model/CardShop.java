package com.yami.shop.card.common.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.Date;

@Data
@TableName("tz_card_shop")
@ApiModel("会员卡")
public class CardShop {
    private Long cardShopId;

    private Long cardId;

    private Long shopId;

    private Date createTime;


    @TableField(exist = false)
    private String shopLogo;

    @TableField(exist = false)
    private String shopName;

    @TableField(exist = false)
    private String industryType;

    @TableField(exist = false)
    private Integer shopStatus;
}
