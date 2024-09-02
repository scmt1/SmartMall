/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.coupon.common.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.shop.coupon.common.dao.CouponProdMapper;
import com.yami.shop.coupon.common.dao.CouponShopMapper;
import com.yami.shop.coupon.common.model.CouponProd;
import com.yami.shop.coupon.common.model.CouponShop;
import com.yami.shop.coupon.common.service.CouponProdService;
import com.yami.shop.coupon.common.service.CouponShopService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 * @author lgh on 2018/12/27.
 */
@Service
@AllArgsConstructor
public class CouponShopServiceImpl extends ServiceImpl<CouponShopMapper, CouponShop> implements CouponShopService {

    private final CouponShopMapper couponShopMapper;

    @Override
    public List<Long> getShopIdByCouponId(Long couponId) {
        return couponShopMapper.getShopIdByCouponId(couponId);
    }
}
