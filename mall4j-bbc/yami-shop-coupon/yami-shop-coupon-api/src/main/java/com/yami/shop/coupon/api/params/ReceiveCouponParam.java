package com.yami.shop.coupon.api.params;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ReceiveCouponParam {
    @NotNull(message="优惠券id不能为空")
    private Long couponId;
    @NotNull(message="纬度不能为空")
    private Double lat;
    @NotNull(message="经度不能为空")
    private Double lng;
}
