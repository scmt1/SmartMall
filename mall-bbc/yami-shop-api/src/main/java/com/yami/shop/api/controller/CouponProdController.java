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
import com.yami.shop.bean.model.Product;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.coupon.common.model.CouponProd;
import com.yami.shop.coupon.common.service.CouponProdService;
import com.yami.shop.service.ProductService;
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
@Api(tags = "优惠券商品")
@RequestMapping("/p/CouponProd")
public class CouponProdController {

    @Autowired
    private CouponProdService couponProdService;
    @Autowired
    private ProductService productService;

    @GetMapping("/info/{couponId}")
    @ApiOperation(value = "获取优惠券绑定的店铺信息", notes = "获取优惠券绑定的店铺信息")
    public ServerResponseEntity<List<CouponProd>> info(@PathVariable("couponId") Long couponId) {
        QueryWrapper<CouponProd> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(CouponProd::getCouponId,couponId);
        List<CouponProd> couponProdList = couponProdService.list(queryWrapper);
        for (CouponProd couponProd1:couponProdList) {
            Product product = productService.getProductById(couponProd1.getProdId());
            couponProd1.setPic(product.getPic());
            couponProd1.setProdName(product.getProdName());
        }
        return ServerResponseEntity.success(couponProdList);
    }
}
