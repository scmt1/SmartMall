/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.coupon.platform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.coupon.common.model.CouponUser;
import com.yami.shop.coupon.common.service.CouponUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

/**
 * @author lgh on 2018/12/27.
 */
@RestController
@RequestMapping("/platform/couponUser")
@Api(tags = "优惠券领取记录接口")
public class CouponUserController {

    @Autowired
    private CouponUserService couponUserService;

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
