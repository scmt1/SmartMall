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
import com.yami.shop.common.config.Constant;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.coupon.common.model.CouponUseRecord;
import com.yami.shop.coupon.common.service.CouponUseRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Objects;

/**
 * @author lgh on 2018/12/27.
 */
@RestController
@RequestMapping("/platform/couponUseRecord")
@Api(tags = "商家端优惠券核销记录接口")
public class CouponUseRecordController {

    @Autowired
    private CouponUseRecordService couponUseRecordService;

    @GetMapping("/writeOffDetail")
    @ApiOperation(value = "获取优惠券核销明细信息")
    public ServerResponseEntity<List<CouponUseRecord>> writeOffDetail(CouponUseRecord couponUseRecord) {
        couponUseRecord.setWriteOffShopId(Constant.PLATFORM_SHOP_ID);
        List<CouponUseRecord> couponUseRecordList = couponUseRecordService.writeOffDetail(couponUseRecord);
        return ServerResponseEntity.success(couponUseRecordList);
    }

    @GetMapping("/shopWriteOffDetail")
    @ApiOperation(value = "获取店铺优惠券核销明细")
    public ServerResponseEntity<IPage<CouponUseRecord>> shopWriteOffDetail(PageParam<CouponUseRecord> page,CouponUseRecord couponUseRecord) {
        IPage<CouponUseRecord> writeOffDetail = couponUseRecordService.shopWriteOffDetail(page,couponUseRecord);
        return ServerResponseEntity.success(writeOffDetail);
    }

    @GetMapping("/writeOffRecordPage")
    @ApiOperation(value = "分页获取优惠券核销信息", notes = "分页获取优惠券核销信息")
    public ServerResponseEntity<IPage<CouponUseRecord>> writeOffRecordPage(PageParam<CouponUseRecord> page, CouponUseRecord couponUseRecord) {
        IPage<CouponUseRecord> couponUseRecords = couponUseRecordService.writeOffRecordPage(page,couponUseRecord);
        return ServerResponseEntity.success(couponUseRecords);
    }

    /**
     * 功能描述：导出店铺核销平台优惠券数据
     *
     * @param response 请求参数
     * @param couponUseRecord   查询参数
     * @return
     */
    @ApiOperation("导出店铺核销平台优惠券数据")
    @GetMapping("/shopDownloadWriteOff")
    public void shopDownloadAll(HttpServletResponse response, CouponUseRecord couponUseRecord) {
        couponUseRecordService.shopDownload(couponUseRecord,response);
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
        couponUseRecordService.download(couponUseRecord,response);
    }

    @GetMapping("/statisticCouponUse")
    @ApiOperation(value = "统计优惠券消费信息")
    public ServerResponseEntity<CouponUseRecord> statisticBalanceUse(CouponUseRecord couponUseRecord){
        CouponUseRecord statisticBalanceUse = couponUseRecordService.statisticCouponUse(couponUseRecord);
        return ServerResponseEntity.success(statisticBalanceUse);
    }
}
