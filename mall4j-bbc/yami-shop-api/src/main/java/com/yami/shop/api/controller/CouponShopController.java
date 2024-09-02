/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yami.shop.bean.model.ShopDetail;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.coupon.common.model.CouponShop;
import com.yami.shop.coupon.common.service.CouponShopService;
import com.yami.shop.service.ShopDetailService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lgh on 2018/10/26.
 */
@RestController
@Api(tags = "优惠券")
@RequestMapping("/p/CouponShop")
public class CouponShopController {

    @Autowired
    private CouponShopService couponShopService;
    @Autowired
    private ShopDetailService shopDetailService;

    @GetMapping("/info/{couponId}")
    @ApiOperation(value = "获取优惠券绑定的店铺信息", notes = "获取优惠券绑定的店铺信息")
    public ServerResponseEntity<CouponShop> info(@PathVariable("couponId") Long couponId) {
        QueryWrapper<CouponShop> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(CouponShop::getCouponId,couponId);
        queryWrapper.orderByAsc("coupon_shop_id");
        CouponShop couponShop = new CouponShop();
        List<CouponShop> couponShopList = couponShopService.list(queryWrapper);
        List<ShopDetail> shopDetails = new ArrayList<>();
        for (CouponShop couponShop1:couponShopList) {
            ShopDetail shopDetail = shopDetailService.getById(couponShop1.getShopId());
            shopDetails.add(shopDetail);
        }
        couponShop.setShopDetails(shopDetails);
        return ServerResponseEntity.success(couponShop);
    }
}
