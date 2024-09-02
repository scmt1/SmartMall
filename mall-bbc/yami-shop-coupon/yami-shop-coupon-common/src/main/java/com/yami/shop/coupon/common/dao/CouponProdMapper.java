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
import com.yami.shop.coupon.common.model.CouponProd;
import org.apache.ibatis.annotations.Param;

import java.util.List;
/**
 * 优惠券商品信息
 * @author lanhai
 */
public interface CouponProdMapper extends BaseMapper<CouponProd> {

    /**
     * 删除优惠券关联
     * @param couponId 优惠券id
     */
    void deleteCouponProdsByCouponId(@Param("couponId") Long couponId);

    /**
     * 新增优惠券关联
     * @param couponId 优惠券id
     * @param prodIds 商品ids
     */
    void insertCouponProdsBatch(@Param("couponId") Long couponId, @Param("prodIds") List<Long> prodIds);

    /**
     * 根据优惠券ids删除优惠券关联
     * @param couponIds 优惠券ids
     */
    void deleteCouponProdsByCouponIds(List<String> couponIds);
}
