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
import com.yami.shop.coupon.common.model.CouponShop;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 优惠券商品信息
 * @author lanhai
 */
public interface CouponShopMapper extends BaseMapper<CouponShop> {

    /**
     * 删除优惠券关联
     * @param couponId 优惠券id
     */
    void deleteCouponShopsByCouponId(@Param("couponId") Long couponId);

    /**
     * 新增优惠券关联
     * @param couponId 优惠券id
     * @param shopIds 商品ids
     */
    void insertCouponShopsBatch(@Param("couponId") Long couponId, @Param("shopIds") List<Long> shopIds);

    /**
     * 根据优惠券ids删除优惠券关联
     * @param couponIds 优惠券ids
     */
    void deleteCouponShopsByCouponIds(List<String> couponIds);

    /**
     * 根据优惠券id查询关联店铺Id
     * @param couponId 优惠券id
     */
    List<Long> getShopIdByCouponId(@Param("couponId") Long couponId);
}
