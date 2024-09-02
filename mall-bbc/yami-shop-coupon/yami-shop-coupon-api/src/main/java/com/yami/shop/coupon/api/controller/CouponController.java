/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.coupon.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yami.shop.bean.app.dto.CouponDto;
import com.yami.shop.bean.app.dto.ProductDto;
import com.yami.shop.bean.enums.CouponProdType;
import com.yami.shop.common.i18n.I18nMessage;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.coupon.common.model.Coupon;
import com.yami.shop.coupon.common.model.CouponProd;
import com.yami.shop.coupon.common.service.CouponService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 优惠券接口
 *
 * @author lanhai
 */
@RestController
@RequestMapping("/coupon")
@Api(tags = "优惠券接口")
public class CouponController {

    @Autowired
    private CouponService couponService;

    @Autowired
    private MapperFacade mapperFacade;

    @GetMapping("/listShopCoupon")
    @ApiOperation(value = "查看一店铺的所有优惠券", notes = "通过店铺id(shopId)获取该店优惠券")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shopId", value = "店铺ID", required = true, dataType = "Long")
    })
    public ServerResponseEntity<List<CouponDto>> listShopCoupon(@RequestParam(value = "shopId") Long shopId) {
        List<Coupon> couponList = couponService.listPutonByShopId(shopId);
        List<CouponDto> couponDtos = mapperFacade.mapAsList(couponList, CouponDto.class);
        return ServerResponseEntity.success(couponDtos);
    }

    @GetMapping("/listByProdId")
    @ApiOperation(value = "获取商品可用的优惠券列表", notes = "获取该商品可用的优惠券列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shopId", value = "店铺id", required = true),
            @ApiImplicitParam(name = "prodId", value = "商品id", required = true)
    })
    public ServerResponseEntity<List<CouponDto>> listByProdId(Long shopId, Long prodId) {
        // 获取已投放优惠券
        List<Coupon> couponList = couponService.listPutonByShopId(shopId);
        // 过滤商品
        couponList = couponList.stream().filter(a -> {
            boolean flag = true;

            if (a.getGetWay() == null) {
                // 如果getWay为空，默认当成0处理
                a.setGetWay(0);
            } else if (a.getGetWay() == 1) {
                // 不是用户直接领取的优惠券，直接不显示该优惠券
                return false;
            }
            //过滤指定商品不参与
            if (Objects.equals(a.getSuitableProdType(), CouponProdType.PROD_NO_IN.value())) {
                for (CouponProd couponProd : a.getCouponProds()) {
                    if (Objects.equals(couponProd.getProdId(), prodId)) {
                        flag = false;
                        break;
                    }
                }
            }
            //过滤指定商品参与
            else if (Objects.equals(a.getSuitableProdType(), CouponProdType.PROD_IN.value())) {
                flag = false;
                for (CouponProd couponProd : a.getCouponProds()) {
                    if (Objects.equals(couponProd.getProdId(), prodId)) {
                        flag = true;
                        break;
                    }
                }
            }
            return flag;
        }).collect(Collectors.toList());
        List<CouponDto> couponDtos = mapperFacade.mapAsList(couponList, CouponDto.class);
        return ServerResponseEntity.success(couponDtos);
    }

    @GetMapping("/prodListByCouponId")
    @ApiOperation(value = "获取优惠券可用的商品列表", notes = "获取优惠券可用的商品列表")
    @ApiImplicitParam(name = "couponId", value = "优惠券id", required = true, dataType = "Long")
    public ServerResponseEntity<IPage<ProductDto>> prodListByCouponId(PageParam<ProductDto> page, @RequestParam("couponId") Long couponId) {
        if (couponId <= 0) {
            page.setRecords(Collections.emptyList());
            return ServerResponseEntity.success(page);
        }
        Coupon coupon = couponService.getById(couponId);
        if (Objects.isNull(coupon) || coupon.getOverdueStatus() != 1) {
            page.setRecords(Collections.emptyList());
            return ServerResponseEntity.success(page);
        }
        IPage<ProductDto> productPage = couponService.prodListByCoupon(page, coupon, I18nMessage.getDbLang());
        return ServerResponseEntity.success(productPage);
    }

    @GetMapping("/couponScorePage")
    @ApiOperation(value = "积分优惠券列表", notes = "获取积分商城可换的优惠券列表")
    public ServerResponseEntity<IPage<Coupon>> couponScorePage(PageParam<Coupon> page) {
        IPage<Coupon> couponDtos = couponService.page(page, new LambdaQueryWrapper<Coupon>()
                .eq(Coupon::getOverdueStatus, 1)
                .eq(Coupon::getPutonStatus, 1)
                .eq(Coupon::getIsScoreType, 1));
        return ServerResponseEntity.success(couponDtos);
    }

    @GetMapping("/couponById")
    @ApiOperation(value = "获取一条优惠券信息", notes = "通过id获取对应优惠券")
    @ApiImplicitParam(name = "couponId", value = "优惠券id", required = true, dataType = "Long")
    public ServerResponseEntity<Coupon> getCouponById(Long couponId) {
        Coupon coupon = couponService.getById(couponId);
        return ServerResponseEntity.success(coupon);
    }

    @GetMapping("/getCouponPage")
    @ApiOperation(value = "商品券列表(商家优惠券)，游客访问", notes = "获取商品券列表(指定商品可用优惠券)，游客访问")
    public ServerResponseEntity<IPage<CouponDto>> getCouponList(PageParam<CouponDto> page) {
        IPage<CouponDto> couponDto = couponService.getCouponList(page);
        return ServerResponseEntity.success(couponDto);
    }

    @GetMapping("/generalCouponList")
    @ApiOperation(value = "通用券列表(平台优惠券)", notes = "获取通用券列表")
    public ServerResponseEntity<List<CouponDto>> generalCouponList(Coupon coupon) {
        List<CouponDto> couponList = couponService.generalCouponList(coupon);
        return ServerResponseEntity.success(couponList);
    }
}
