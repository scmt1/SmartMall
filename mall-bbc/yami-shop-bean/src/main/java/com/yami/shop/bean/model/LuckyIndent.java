package com.yami.shop.bean.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@TableName("tz_lucky_indent")
public class LuckyIndent {
    @TableId
    @ApiModelProperty("id")
    private Long indentId;

    @ApiModelProperty(value = "幸运抽奖ID")
    private Long drawId;

    @ApiModelProperty(value = "用户id")
    private String userId;

    @ApiModelProperty(value = "奖品名称")
    private String prizeName;

    @ApiModelProperty(value = "奖品类型(0：未中奖，1：优惠券，2：积分，3：成长值，4：商品)")
    private Integer prizeType;

    @ApiModelProperty(value = "中奖积分")
    private Long score;

    @ApiModelProperty(value = "中奖成长值")
    private Integer growth;

    @ApiModelProperty(value = "中奖优惠券id")
    private Long couponId;

    @ApiModelProperty(value = "中奖商品id")
    private Long prodId;

    @ApiModelProperty(value = "中奖奖品数量")
    private Integer prizeNum;

    @ApiModelProperty(value = "状态(0:失效 1:未领取 2:已领取)")
    private Integer status;

    @ApiModelProperty(value = "中奖奖品条码编号")
    private Integer qrCode;

    @ApiModelProperty(value = "创建时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @ApiModelProperty(value = "中奖人")
    @TableField(exist = false)
    private String nickName;

    @ApiModelProperty(value = "店铺名")
    @TableField(exist = false)
    private String shopName;
}
