package com.yami.shop.bean.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;


@Data
public class CouponAppConnect {
    @ApiModelProperty(value = "优惠券名称")
    private String couponName;

    @ApiModelProperty(value = "使用条件")
    private Double cashCondition;

    @ApiModelProperty(value = "减免金额")
    private Double reduceAmount;


    /**
     * 结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date userEndTime;


    private String openid;
}
