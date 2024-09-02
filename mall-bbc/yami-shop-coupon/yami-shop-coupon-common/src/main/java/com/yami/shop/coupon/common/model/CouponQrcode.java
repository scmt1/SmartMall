package com.yami.shop.coupon.common.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("tz_coupon_qrcode")
public class CouponQrcode {
    private Long id;
    private Long couponUserId;
    private Integer qrCode;
    private String qrImg;
    private Date expiredTime;
    private Integer type;
}
