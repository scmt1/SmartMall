/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.coupon.common.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yami.shop.common.util.PageAdapter;
import com.yami.shop.coupon.common.model.Coupon;
import com.yami.shop.coupon.common.model.CouponOnly;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 优惠券
 *
 * @author lanhai
 */
public interface CouponOnlyMapper extends BaseMapper<CouponOnly> {


    List<CouponOnly> getPlatformPage(@Param("adapter") PageAdapter adapter);

    long countPlatform();

    List<CouponOnly> queryGroupListByCouponId(Long couponId);

    List<Coupon> getCouponByGroupNum(String groupNum);
}
