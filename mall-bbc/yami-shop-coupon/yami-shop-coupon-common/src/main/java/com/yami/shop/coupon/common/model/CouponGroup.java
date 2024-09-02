package com.yami.shop.coupon.common.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
@TableName("tz_coupon_group")
@ApiModel("优惠券")
public class CouponGroup {
    @TableId
    @ApiModelProperty(value = "优惠券id")
    private Long id;

    private Long couponId;

    private Long shopId;

    private String title;

    private String groupCode;
}
