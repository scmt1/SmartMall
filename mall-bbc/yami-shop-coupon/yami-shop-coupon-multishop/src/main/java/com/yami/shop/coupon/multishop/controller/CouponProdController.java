/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.coupon.multishop.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.coupon.common.model.CouponProd;
import com.yami.shop.coupon.common.service.CouponProdService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


/**
 * @author lgh on 2018/12/27.
 */
@RestController
@RequestMapping("/admin/couponProd")
@Api(tags = "商家端优惠券商品接口")
public class CouponProdController {

    @Autowired
    private CouponProdService couponProdService;

    @GetMapping("/page")
    @ApiOperation(value = "分页获取优惠券商品")
    public ServerResponseEntity<IPage<CouponProd>> page(CouponProd couponProd, PageParam<CouponProd> page) {
        IPage<CouponProd> couponProds = couponProdService.page(page, new LambdaQueryWrapper<CouponProd>());
        return ServerResponseEntity.success(couponProds);
    }

    @GetMapping("/info/{id}")
    @ApiOperation(value = "根据优惠券商品id获取信息")
    @ApiImplicitParam(name = "id", value = "优惠券商品id", dataType = "Long")
    public ServerResponseEntity<CouponProd> info(@PathVariable("id") Long id) {
        CouponProd couponProd = couponProdService.getById(id);
        return ServerResponseEntity.success(couponProd);
    }

    @PostMapping
    @ApiOperation(value = "保存优惠券商品信息")
    public ServerResponseEntity<Void> save(@RequestBody @Valid CouponProd couponProd) {
        couponProdService.save(couponProd);
        return ServerResponseEntity.success();
    }

    @PutMapping
    @ApiOperation(value = "修改优惠券商品信息")
    public ServerResponseEntity<Void> update(@RequestBody @Valid CouponProd couponProd) {
        couponProdService.updateById(couponProd);
        return ServerResponseEntity.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "根据id删除优惠券商品")
    @ApiImplicitParam(name = "id", value = "优惠券商品id", dataType = "Long")
    public ServerResponseEntity<Void> delete(@PathVariable Long id) {
        couponProdService.removeById(id);
        return ServerResponseEntity.success();
    }
}
