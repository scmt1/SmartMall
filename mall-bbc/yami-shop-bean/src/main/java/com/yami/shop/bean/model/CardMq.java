package com.yami.shop.bean.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

@Data
@TableName("tz_card_mq")
public class CardMq {
    @TableId
    @ApiModelProperty("id")
    private Long id;

    private String mchOrderNo;

    private String channelOrderNo;

    private String payOrderId;

    private Integer status;

    private Integer count;

    private String state;

    private Double amount;

    /**
     * 回调内容
     */
    private String callbackContent;

    private String userId;
}
