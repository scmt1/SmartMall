package com.yami.shop.coupon.common.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

@Data
@TableName("tz_coupon_only")
@ApiModel("优惠券只能领取一次的组合")
public class CouponOnly {
    @TableId
    private Long id;

    private Long couponId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long groupNum;

    private String groupName;

    @TableField(exist = false)
    private List<Coupon> coupon;
}
