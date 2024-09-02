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


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.yami.shop.bean.app.dto.CouponDto;
import com.yami.shop.bean.enums.OfflineHandleEventType;
import com.yami.shop.bean.model.OfflineHandleEvent;
import com.yami.shop.bean.param.OfflineHandleEventAuditParam;
import com.yami.shop.bean.param.SendUserCouponsParam;
import com.yami.shop.bean.param.UserUpdateParam;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.coupon.common.constants.CouponStatusEnum;
import com.yami.shop.coupon.common.constants.ValidTimeTypeEnum;
import com.yami.shop.coupon.common.model.Coupon;
import com.yami.shop.coupon.common.model.CouponUser;
import com.yami.shop.coupon.common.service.CouponService;
import com.yami.shop.security.multishop.util.SecurityUtils;
import com.yami.shop.service.OfflineHandleEventService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author lgh on 2018/12/27.
 */
@RestController("adminCouponController")
@RequestMapping("/admin/coupon")
@Api(tags = "商家端优惠券接口")
public class CouponController {

    @Autowired
    private CouponService couponService;

    @Autowired
    private OfflineHandleEventService offlineHandleEventService;

    @GetMapping("/page")
    @PreAuthorize("@pms.hasPermission('admin:coupon:page')")
    @ApiOperation(value = "分页获取优惠券列表信息")
    public ServerResponseEntity<IPage<Coupon>> page(Coupon coupon, PageParam<Coupon> page) {
        Long shopId = SecurityUtils.getShopUser().getShopId();
        coupon.setShopId(shopId);
        IPage<Coupon> multiShopPage = couponService.getMultiShopPage(page, coupon);
        return ServerResponseEntity.success(multiShopPage);
    }

    @GetMapping("/shopCouponsPage")
    @ApiOperation(value = "分页获取优惠券列表信息")
    public ServerResponseEntity<IPage<Coupon>> shopCouponsPage(Coupon coupon, PageParam<Coupon> page) {
        Long shopId = SecurityUtils.getShopUser().getShopId();
        coupon.setShopId(shopId);
        IPage<Coupon> shopCouponsPage = couponService.getShopCouponsPage(page, coupon);
        return ServerResponseEntity.success(shopCouponsPage);
    }

    @GetMapping("/statistic")
    @ApiOperation(value = "统计优惠券信息")
    public ServerResponseEntity<Coupon> statisticCoupon(Coupon coupon) {
        Long shopId = SecurityUtils.getShopUser().getShopId();
        coupon.setShopId(shopId);
        Coupon statisticCoupon = couponService.statisticCoupon(coupon);
        return ServerResponseEntity.success(statisticCoupon);
    }

    @GetMapping("/shopReceiveAndUseStatistic")
    @ApiOperation(value = "统计优惠券领取数量")
    public ServerResponseEntity<Coupon> shopReceiveAndUseStatistic(Coupon coupon) {
        Long shopId = SecurityUtils.getShopUser().getShopId();
        coupon.setShopId(shopId);
        Coupon statisticCoupon = couponService.receiveAndUseStatistic(coupon);
        return ServerResponseEntity.success(statisticCoupon);
    }

    @GetMapping("/info/{id}")
    @ApiOperation(value = "根据优惠券id获取优惠券信息")
    @ApiImplicitParam(name = "id", value = "优惠券id", dataType = "Long")
    public ServerResponseEntity<Coupon> info(@PathVariable("id") Long id) {
        Coupon coupon = couponService.getCouponAndCouponProdsByCouponId(id);
        if (!Objects.equals(SecurityUtils.getShopUser().getShopId(), coupon.getShopId())) {
            throw new YamiShopBindException("yami.no.auth");
        }
        return ServerResponseEntity.success(coupon);
    }

    @PostMapping
    @PreAuthorize("@pms.hasPermission('admin:coupon:save')")
    @ApiOperation(value = "新增优惠券")
    public ServerResponseEntity<Void> save(@RequestBody @Valid Coupon coupon) {
        Long shopId = SecurityUtils.getShopUser().getShopId();
        coupon.setShopId(shopId);
        coupon.setSourceStock(coupon.getStocks());
        //判断为领取后生效类型时，开始时间和结束时间为空
        if (Objects.equals(coupon.getValidTimeType(), ValidTimeTypeEnum.RECEIVE.getValue())) {
            coupon.setEndTime(null);
            coupon.setStartTime(null);
        }
        couponService.insertCouponAndCouponProds(coupon);
        return ServerResponseEntity.success();
    }

    @PutMapping
    @PreAuthorize("@pms.hasPermission('admin:coupon:update')")
    @ApiOperation(value = "修改优惠券")
    public ServerResponseEntity<Void> update(@RequestBody @Valid Coupon coupon) {
        Long shopId = SecurityUtils.getShopUser().getShopId();
        Coupon dbCoupon = couponService.getById(coupon.getCouponId());
        if (!Objects.equals(dbCoupon.getShopId(), shopId)) {
            // 没有权限修改该优惠券信息
            throw new YamiShopBindException("yami.coupon.no.auth");
        }
        if (Objects.equals(dbCoupon.getPutonStatus(), CouponStatusEnum.OFFLINE.getValue()) || Objects.equals(dbCoupon.getPutonStatus(), CouponStatusEnum.WAIT_AUDIT.getValue())) {
            // 优惠券处于违规下线或者待审核状态时，商家不能直接修改优惠券状态
            coupon.setPutonStatus(dbCoupon.getPutonStatus());
        } else if (Objects.equals(coupon.getPutonStatus(), CouponStatusEnum.OFFLINE.getValue()) || Objects.equals(coupon.getPutonStatus(), CouponStatusEnum.WAIT_AUDIT.getValue())) {
            // 当数据库中的优惠券状态不是违规下线或者待审核状态时，如果接口传进来的优惠券状态为违规下线或者待审核状态，说明该优惠券应该已经被审核了
            // 使用数据库的优惠券状态
            coupon.setPutonStatus(dbCoupon.getPutonStatus());
        }
        coupon.setShopId(shopId);
        //被领取数量
        int receNum = dbCoupon.getSourceStock() - dbCoupon.getStocks();
        //新的原始库存
        int newSourceStock = coupon.getStocks() + receNum;
        coupon.setSourceStock(newSourceStock);
        couponService.updateCouponAndCouponProds(coupon);
        return ServerResponseEntity.success();
    }

    @DeleteMapping
    @PreAuthorize("@pms.hasPermission('admin:coupon:delete')")
    @ApiOperation(value = "删除优惠券")
    public ServerResponseEntity<Void> delete(@RequestBody Long couponId) {
        Long shopId = SecurityUtils.getShopUser().getShopId();
        Coupon dbCoupon = couponService.getById(couponId);
        if (!Objects.equals(dbCoupon.getShopId(), shopId)) {
            // 没有权限修改该优惠券信息
            throw new YamiShopBindException("yami.coupon.no.auth");
        }
        couponService.deleteByCouponId(couponId);
        return ServerResponseEntity.success();
    }

    @GetMapping("/getOfflineHandleEventByCouponId/{couponId}")
    @ApiOperation(value = "通过优惠券id获取下线信息")
    @ApiImplicitParam(name = "couponId", value = "优惠券id", dataType = "Long")
    public ServerResponseEntity<OfflineHandleEvent> getOfflineHandleEventByDiscountId(@PathVariable("couponId") Long couponId) {
        OfflineHandleEvent offlineHandleEvent = offlineHandleEventService.getProcessingEventByHandleTypeAndHandleId(OfflineHandleEventType.COUPON.getValue(), couponId);
        return ServerResponseEntity.success(offlineHandleEvent);
    }

    @PostMapping("/auditApply")
    @PreAuthorize("@pms.hasPermission('admin:coupon:auditApply')")
    @ApiOperation(value = "违规商品提交审核")
    public ServerResponseEntity<Void> auditApply(@RequestBody OfflineHandleEventAuditParam offlineHandleEventAuditParam) {
        Coupon coupon = couponService.getCouponAndCouponProdsByCouponId(offlineHandleEventAuditParam.getHandleId());
        if (coupon == null) {
            // 未找到该活动信息
            throw new YamiShopBindException("yami.activity.cannot.find");
        }
        couponService.auditApply(offlineHandleEventAuditParam.getEventId(), offlineHandleEventAuditParam.getHandleId(), offlineHandleEventAuditParam.getReapplyReason());
        // 移除缓存
        couponService.removeCouponAndCouponProdsCache(coupon.getShopId());
        return ServerResponseEntity.success();
    }

    @GetMapping("/getCouponList")
    @ApiOperation(value = "通过状态查看用户的优惠券列表信息", notes = "通过状态查看用户的优惠券列表信息,优惠券状态 0:已过期 1:未使用 2:使用过")
    @ApiImplicitParam(name = "status", value = "优惠券状态 0:失效 1:有效 2:使用过", required = true, dataType = "Integer")
    public ServerResponseEntity<IPage<CouponDto>> getCouponList(PageParam<CouponDto> page, @RequestParam("status") Integer status, @RequestParam("userId") String userId) {
        IPage<CouponDto> couponDtoList = couponService.getCouponListByStatus(page, userId, status);
        return ServerResponseEntity.success(couponDtoList);
    }

    @GetMapping("/pageByUserId")
    @ApiOperation(value = "获取某个用户的优惠券明细")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户id", dataType = "String"),
            @ApiImplicitParam(name = "status", value = "优惠券状态 0:失效 1:有效 2:使用过", dataType = "Integer")
    })
    public ServerResponseEntity<IPage<CouponUser>> getPageByUserId(PageParam<CouponUser> page, String userId, Integer status) {
        IPage<CouponUser> resPage = couponService.getPageByUserId(page, userId, status);
        return ServerResponseEntity.success(resPage);
    }

    @GetMapping("/getShopAllCouponList")
    @ApiOperation(value = "获取优惠券列表信息")
    public ServerResponseEntity<List<CouponDto>> getShopAllCouponList(Coupon coupon) {
        Long shopId = SecurityUtils.getShopUser().getShopId();
        if(coupon.getShopId() == null){
            coupon.setShopId(shopId);
        }
        List<CouponDto> couponList = couponService.getShopAllCouponList(coupon);
        return ServerResponseEntity.success(couponList);
    }

    @GetMapping("/systemCouponList")
    @ApiOperation(value = "获取优惠券列表信息")
    public ServerResponseEntity<IPage<Coupon>> systemCouponList(PageParam<Coupon> page, Coupon coupon) {
        Long shopId = SecurityUtils.getShopUser().getShopId();
        PageParam<Coupon> couponPage = couponService.page(page, new LambdaQueryWrapper<Coupon>()
                .eq(Coupon::getShopId, shopId)
                .eq(Coupon::getPutonStatus, 1)
                .eq(Objects.nonNull(coupon.getGetWay()), Coupon::getGetWay, coupon.getGetWay())
                .like(StrUtil.isNotBlank(coupon.getCouponName()), Coupon::getCouponName, coupon.getCouponName())
                .orderByDesc(Coupon::getCouponId)
        );
        return ServerResponseEntity.success(couponPage);
    }


    @PutMapping("/sendUserCoupon")
    @ApiOperation(value = "店铺批量发放优惠券给会员")
    public ServerResponseEntity<Boolean> sendUserCoupon(@RequestBody UserUpdateParam param) {
        Long shopId = SecurityUtils.getShopUser().getShopId();
        if (StringUtils.isBlank(param.getUserId())) {
            throw new YamiShopBindException("请选择会员");
        }
        List<SendUserCouponsParam> coupons = param.getCoupons();
        if (CollectionUtils.isEmpty(coupons)) {
            throw new YamiShopBindException("请选择优惠券");
        }
        for (SendUserCouponsParam coupon : coupons) {
            Long couponId = coupon.getCouponId();
            Integer nums = coupon.getNums();
            if (Objects.equals(0, nums)) {
                continue;
            }
            for (int i = 0; i < nums; i++) {
                couponService.batchBindCouponByIds(Collections.singletonList(couponId), param.getUserId(), shopId);
            }
        }
        return ServerResponseEntity.success();
    }
}
