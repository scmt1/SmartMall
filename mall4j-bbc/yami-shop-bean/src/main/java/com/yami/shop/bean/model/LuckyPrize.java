package com.yami.shop.bean.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@TableName("tz_Lucky_prize")
public class LuckyPrize {
    @TableId
    @ApiModelProperty("id")
    private Long prizeId;

    @ApiModelProperty(value = "幸运抽奖ID")
    private Long drawId;

    @ApiModelProperty(value = "奖品名称")
    private String prizeName;

    @ApiModelProperty(value = "奖品类型(0：未中奖，1：优惠券，:2：积分，3：成长值，4：商品)")
    private Integer prizeType;

    @ApiModelProperty(value = "积分")
    private Long score;

    @ApiModelProperty(value = "成长值")
    private Integer growth;

    @ApiModelProperty(value = "优惠券id")
    private Long couponId;

    @ApiModelProperty(value = "商品id")
    private Long prodId;

    @ApiModelProperty(value = "奖品数量")
    private Integer prizeNum;

    @ApiModelProperty(value = "奖品剩余数量")
    private Integer remaining;

    @ApiModelProperty(value = "奖项权重")
    private Integer prizeWeight;

    @ApiModelProperty(value = "奖品等级")
    private String level;

    @ApiModelProperty(value = "优惠券名称")
    @TableField(exist = false)
    private String couponName;

    @ApiModelProperty(value = "商品名称")
    @TableField(exist = false)
    private String prodName;

    @ApiModelProperty(value = "优惠券图片")
    @TableField(exist = false)
    private String couponImg;

    @ApiModelProperty(value = "商品图片")
    @TableField(exist = false)
    private String prodPic;
}
