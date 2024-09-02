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
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.coupon.common.model.Coupon;
import com.yami.shop.coupon.common.model.CouponUser;
import com.yami.shop.coupon.common.service.CouponQrcodeService;
import com.yami.shop.coupon.common.service.CouponService;
import com.yami.shop.coupon.common.service.CouponUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;


/**
 * @author lgh on 2018/12/27.
 */
@RestController
@RequestMapping("/admin/couponUser")
@Api(tags = "商家端优惠券用户接口")
public class CouponUserController {

    @Autowired
    private CouponUserService couponUserService;
    @Autowired
    private CouponService couponService;
    @Autowired
    private CouponQrcodeService couponQrcodeService;

    @GetMapping("/page")
    @ApiOperation(value = "分页获取优惠券用户信息")
    public ServerResponseEntity<IPage<CouponUser>> page(CouponUser couponUser, PageParam<CouponUser> page) {
        IPage<CouponUser> couponUsers = couponUserService.page(page, new LambdaQueryWrapper<CouponUser>());
        return ServerResponseEntity.success(couponUsers);
    }

    @GetMapping("/info/{id}")
    @ApiOperation(value = "根据优惠券用户id获取信息")
    @ApiImplicitParam(name = "id", value = "优惠券用户id", dataType = "Long")
    public ServerResponseEntity<CouponUser> info(@PathVariable("id") Long id) {
        CouponUser couponUser = couponUserService.getById(id);
        return ServerResponseEntity.success(couponUser);
    }

    @PostMapping
    @ApiOperation(value = "新增优惠券用户")
    public ServerResponseEntity<Void> save(@RequestBody @Valid CouponUser couponUser) {
        couponUserService.save(couponUser);
        return ServerResponseEntity.success();
    }

    @PutMapping
    @ApiOperation(value = "新增优惠券用户")
    public ServerResponseEntity<Void> update(@RequestBody @Valid CouponUser couponUser) {
        couponUserService.updateById(couponUser);
        return ServerResponseEntity.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "根据优惠券用户id删除信息")
    @ApiImplicitParam(name = "id", value = "优惠券用户id", dataType = "Long")
    public ServerResponseEntity<Void> delete(@PathVariable Long id) {
        couponUserService.removeById(id);
        return ServerResponseEntity.success();
    }

    @GetMapping("/getCouponByCouponUserId")
    @ApiOperation(value = "根据优惠券用户优惠券编号获取信息")
    @ApiImplicitParam(name = "couponUserNumber", value = "优惠券用户优惠券编号", dataType = "String")
    public ServerResponseEntity<Coupon> getCouponByCouponUserId(@RequestParam(value = "couponUserNumber") String couponUserNumber) {
        CouponUser couponUser = couponUserService.getCouponUserByQrCode(couponUserNumber);
        Coupon coupon = null;
        if(couponUser != null){
            coupon = couponService.getById(couponUser.getCouponId());
        }else{
            throw new YamiShopBindException("用户优惠券信息不存在！");
        }
        return ServerResponseEntity.success(coupon);
    }

    @GetMapping("/getCouponUserPage")
    @ApiOperation(value = "分页获取优惠券领取信息", notes = "分页获取优惠券领取信息")
    public ServerResponseEntity<IPage<CouponUser>> getCouponUserPage(PageParam<CouponUser> page, CouponUser couponUser) {
        IPage<CouponUser> couponUsers = couponUserService.getCouponUserPage(page,couponUser);
        return ServerResponseEntity.success(couponUsers);
    }

    @GetMapping("/downloadCouponUser")
    @ApiOperation(value = "导出优惠券领取信息", notes = "导出优惠券领取信息")
    public void downloadCouponUser(HttpServletResponse response, CouponUser couponUser) {
        couponUserService.downloadCouponUser(couponUser,response);
    }
}
