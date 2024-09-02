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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yami.shop.common.config.Constant;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.coupon.common.model.Coupon;
import com.yami.shop.coupon.common.model.CouponUseRecord;
import com.yami.shop.coupon.common.service.CouponService;
import com.yami.shop.coupon.common.service.CouponUseRecordService;
import com.yami.shop.security.multishop.util.SecurityUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author lgh on 2018/12/27.
 */
@RestController
@RequestMapping("/admin/couponUseRecord")
@Api(tags = "商家端优惠券核销记录接口")
public class CouponUseRecordController {

    @Autowired
    private CouponUseRecordService couponUseRecordService;
    @Autowired
    private CouponService couponService;

    @GetMapping("/writeOffDetail")
    @ApiOperation(value = "获取优惠券列表信息")
    public ServerResponseEntity<List<Coupon>> writeOffDetail(Coupon coupon) {
        Long shopId = SecurityUtils.getShopUser().getShopId();
        coupon.setShopId(shopId);
        List<Coupon> writeOffDetail = couponService.shopWriteOffDetail(coupon);
        return ServerResponseEntity.success(writeOffDetail);
    }

    /**
     * 功能描述：导出全部数据
     *
     * @param response 请求参数
     * @param couponUseRecord   查询参数
     * @return
     */
    @ApiOperation("导出优惠券核销数据")
    @GetMapping("/download")
    public void downloadAll(HttpServletResponse response, CouponUseRecord couponUseRecord) {
        Long shopId = SecurityUtils.getShopUser().getShopId();
        couponUseRecord.setWriteOffShopId(shopId);
        couponUseRecordService.download(couponUseRecord,response);
    }

    @GetMapping("/statisticCouponUse")
    @ApiOperation(value = "统计优惠券消费信息")
    public ServerResponseEntity<CouponUseRecord> statisticBalanceUse(CouponUseRecord couponUseRecord){
        Long shopId = SecurityUtils.getShopUser().getShopId();
        couponUseRecord.setWriteOffShopId(shopId);
        CouponUseRecord statisticBalanceUse = couponUseRecordService.statisticCouponUse(couponUseRecord);
        return ServerResponseEntity.success(statisticBalanceUse);
    }
}
