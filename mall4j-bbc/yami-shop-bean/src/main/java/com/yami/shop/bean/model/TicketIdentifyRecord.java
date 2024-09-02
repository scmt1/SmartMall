package com.yami.shop.bean.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.yami.shop.common.serializer.json.ImgJsonSerializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@TableName("tz_ticket_identify_record")
public class TicketIdentifyRecord {
    @TableId
    @ApiModelProperty("id")
    private Long ticketId;

    @ApiModelProperty(value = "单号")
    private String orderNumber;

    @ApiModelProperty(value = "销售日期")
    private String saleTime;

    @ApiModelProperty(value = "付款金额")
    private Double payment;

    @ApiModelProperty(value = "积分")
    private Long score;

    @ApiModelProperty(value = "用户手机号")
    private String userMobile;

    @ApiModelProperty("店铺id")
    private Long shopId;

    @ApiModelProperty(value = "店铺名称")
    private String shopName;

    @ApiModelProperty(value = "创建日期")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @ApiModelProperty(value = "用户id")
    @TableField(exist = false)
    private String userId;

    @ApiModelProperty(value = "小票图片地址")
    private String imgUrl;

    @ApiModelProperty(value = "是否冲红(0：未冲红 1：已冲红)")
    private Integer isRedBlood;

    @ApiModelProperty(value = "店铺配置小票")
    @JsonSerialize(using = ImgJsonSerializer.class)
    @TableField(exist = false)
    private String ticketImg;

}
