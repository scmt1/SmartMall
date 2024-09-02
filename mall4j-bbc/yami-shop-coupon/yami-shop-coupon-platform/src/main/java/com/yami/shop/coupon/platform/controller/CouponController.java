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


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yami.shop.bean.app.dto.CouponDto;
import com.yami.shop.bean.enums.OfflineHandleEventType;
import com.yami.shop.bean.enums.SendType;
import com.yami.shop.bean.event.SendMessageEvent;
import com.yami.shop.bean.model.OfflineHandleEvent;
import com.yami.shop.bean.param.NotifyTemplateParam;
import com.yami.shop.bean.param.OfflineHandleEventAuditParam;
import com.yami.shop.bean.param.SendUserCouponsParam;
import com.yami.shop.bean.param.UserUpdateParam;
import com.yami.shop.common.config.Constant;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.coupon.common.model.Coupon;
import com.yami.shop.coupon.common.model.CouponUser;
import com.yami.shop.coupon.common.service.CouponService;
import com.yami.shop.security.platform.util.SecurityUtils;
import com.yami.shop.service.OfflineHandleEventService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 优惠券后台接口
 *
 * @author lanhai
 */
@RestController("adminCouponController")
@RequestMapping("/platform/coupon")
@Api(tags = "平台端优惠券接口")
public class CouponController {

    @Autowired
    private CouponService couponService;
    @Autowired
    private OfflineHandleEventService offlineHandleEventService;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @GetMapping("/page")
    @PreAuthorize("@pms.hasPermission('platform:coupon:page')")
    @ApiOperation(value = "分页获取优惠券列表信息")
    public ServerResponseEntity<IPage<Coupon>> page(Coupon coupon, PageParam<Coupon> page) {
        IPage<Coupon> couponPage = couponService.getPlatformPage(page, coupon);
        return ServerResponseEntity.success(couponPage);
    }

    @GetMapping("/couponsPage")
    @ApiOperation(value = "分页获取优惠券列表信息")
    public ServerResponseEntity<IPage<Coupon>> couponsPage(Coupon coupon, PageParam<Coupon> page) {
        IPage<Coupon> couponPage = couponService.getCouponsPage(page, coupon);
        return ServerResponseEntity.success(couponPage);
    }

    @GetMapping("/statistic")
    @ApiOperation(value = "统计优惠券信息")
    public ServerResponseEntity<Coupon> statisticCoupon(Coupon coupon) {
        Coupon statisticCoupon = couponService.statisticCoupon(coupon);
        return ServerResponseEntity.success(statisticCoupon);
    }

    @GetMapping("/platformReceiveAndUseStatistic")
    @ApiOperation(value = "统计优惠券领取数量")
    public ServerResponseEntity<Coupon> platformReceiveStatistic(Coupon coupon) {
        Coupon statisticCoupon = couponService.receiveAndUseStatistic(coupon);
        return ServerResponseEntity.success(statisticCoupon);
    }

    @GetMapping("/list")
    @ApiOperation(value = "获取优惠券列表信息")
    public ServerResponseEntity<IPage<Coupon>> couponList(PageParam<Coupon> page, Coupon coupon) {
        PageParam<Coupon> couponPage = couponService.page(page, new LambdaQueryWrapper<Coupon>()
                .eq(Coupon::getShopId, Constant.PLATFORM_SHOP_ID)
                .eq(Coupon::getPutonStatus, 1)
                .eq(Objects.nonNull(coupon.getGetWay()), Coupon::getGetWay, coupon.getGetWay())
                .like(StrUtil.isNotBlank(coupon.getCouponName()), Coupon::getCouponName, coupon.getCouponName())
                .orderByDesc(Coupon::getCouponId)
        );
        return ServerResponseEntity.success(couponPage);
    }

    @GetMapping("/couponPageList")
    @ApiOperation(value = "获取优惠券列表信息")
    public ServerResponseEntity<IPage<Coupon>> couponPageList(PageParam<Coupon> page, Coupon coupon) {
        PageParam<Coupon> couponPage = couponService.page(page, new LambdaQueryWrapper<Coupon>()
//                .eq(Coupon::getShopId, Constant.PLATFORM_SHOP_ID)
                .and(i -> i.eq(Coupon::getPutonStatus, 0).or().eq(Coupon::getPutonStatus, 1).or().eq(Coupon::getPutonStatus, 4))
                .eq(Objects.nonNull(coupon.getGetWay()), Coupon::getGetWay, coupon.getGetWay())
                .like(StrUtil.isNotBlank(coupon.getCouponName()), Coupon::getCouponName, coupon.getCouponName())
                .notExists("select 1 from tz_coupon_only WHERE coupon_id = tz_coupon.coupon_id")
                .orderByDesc(Coupon::getCouponId)
        );
        return ServerResponseEntity.success(couponPage);
    }

    @GetMapping("/getAllCouponList")
    @ApiOperation(value = "获取优惠券列表信息")
    public ServerResponseEntity<List<CouponDto>> getShopAllCouponList(Coupon coupon) {
        List<CouponDto> couponList = couponService.getShopAllCouponList(coupon);
        return ServerResponseEntity.success(couponList);
    }

    @GetMapping("/pageByPlatform")
    @PreAuthorize("@pms.hasPermission('platform:coupon:page')")
    @ApiOperation(value = "分页获取平台优惠券")
    public ServerResponseEntity<IPage<Coupon>> pageByPlatform(Coupon coupon, PageParam<Coupon> page) {
        coupon.setShopId(Constant.PLATFORM_SHOP_ID);
        IPage<Coupon> couponPage = couponService.getPlatformPage(page, coupon);
        return ServerResponseEntity.success(couponPage);
    }

    @PutMapping("/removeScore")
    @PreAuthorize("@pms.hasPermission('platform:coupon:delete')")
    @ApiOperation(value = "修改优惠券不为积分商品")
    @ApiImplicitParam(name = "couponId", value = "优惠券id", required = true, dataType = "Long")
    public ServerResponseEntity<IPage<Coupon>> removeScore(@RequestBody Long couponId) {
        Coupon coupon = couponService.getById(couponId);
        coupon.setIsScoreType(0);
        coupon.setScorePrice(0);
        couponService.updateById(coupon);
        return ServerResponseEntity.success();
    }

    @PutMapping("/couponByScore")
    @PreAuthorize("@pms.hasPermission('platform:coupon:update')")
    @ApiOperation(value = "修改平台券可以积分兑换")
    public ServerResponseEntity<IPage<Coupon>> couponByScore(@RequestBody @Valid Coupon coupon) {
        if (coupon.getScorePrice() == null) {
            // 请输入正确的积分数量
            throw new YamiShopBindException("yami.user.score.input.check");
        }
        coupon.setIsScoreType(1);
        couponService.updateById(coupon);
        return ServerResponseEntity.success();
    }

    @GetMapping("/scorePage")
    @PreAuthorize("@pms.hasPermission('platform:coupon:page')")
    @ApiOperation(value = "分页获取积分可换优惠券")
    public ServerResponseEntity<IPage<Coupon>> pageByScore(Coupon coupon, PageParam<Coupon> page) {
        coupon.setIsScoreType(1);
        IPage<Coupon> couponPage = couponService.getPlatformPage(page, coupon);
        return ServerResponseEntity.success(couponPage);
    }

    @GetMapping("/info/{id}")
    @PreAuthorize("@pms.hasPermission('platform:coupon:info')")
    @ApiOperation(value = "根据优惠券id获取优惠券信息")
    @ApiImplicitParam(name = "id", value = "优惠券id", required = true, dataType = "Long")
    public ServerResponseEntity<Coupon> info(@PathVariable("id") Long id) {
        Coupon coupon = couponService.getCouponAndCouponProdsByCouponId(id);
        return ServerResponseEntity.success(coupon);
    }

    @DeleteMapping
    @PreAuthorize("@pms.hasPermission('platform:coupon:delete')")
    @ApiOperation(value = "根据优惠券id删除优惠券")
    @ApiImplicitParam(name = "couponId", value = "优惠券id", required = true, dataType = "Long")
    public ServerResponseEntity<Void> delete(@RequestBody Long couponId) {
        couponService.deleteByCouponId(couponId);
        return ServerResponseEntity.success();
    }

    @GetMapping("/getOfflineHandleEventByCouponId/{couponId}")
    @PreAuthorize("@pms.hasPermission('platform:coupon:info')")
    @ApiOperation(value = "通过优惠券id获取下线信息")
    @ApiImplicitParam(name = "couponId", value = "优惠券id", required = true, dataType = "Long")
    public ServerResponseEntity<OfflineHandleEvent> getOfflineHandleEventByCouponId(@PathVariable("couponId") Long couponId) {
        OfflineHandleEvent offlineHandleEvent = offlineHandleEventService.getProcessingEventByHandleTypeAndHandleId(OfflineHandleEventType.COUPON.getValue(), couponId);
        return ServerResponseEntity.success(offlineHandleEvent);
    }

    @PostMapping("/offline")
    @PreAuthorize("@pms.hasPermission('platform:coupon:update')")
    @ApiOperation(value = "下线活动")
    public ServerResponseEntity<Void> offline(@RequestBody OfflineHandleEvent offlineHandleEvent) {
        Long sysUserId = SecurityUtils.getSysUser().getUserId();
        Coupon coupon = couponService.getCouponAndCouponProdsByCouponId(offlineHandleEvent.getHandleId());
        if (coupon == null) {
            // 未找到该活动信息
            throw new YamiShopBindException("yami.activity.cannot.find");
        }
        couponService.offline(coupon, offlineHandleEvent.getOfflineReason(), sysUserId);
        //发送活动下架提醒给商家
        NotifyTemplateParam shopParam = new NotifyTemplateParam();
        shopParam.setShopId(coupon.getShopId());
        shopParam.setActivityId(coupon.getCouponId());
        shopParam.setActivityName(coupon.getCouponName());
        shopParam.setSendType(SendType.ACTIVITY_OFFLINE.getValue());
        eventPublisher.publishEvent(new SendMessageEvent(shopParam));
        return ServerResponseEntity.success();
    }

    @PostMapping("/auditCoupon")
    @PreAuthorize("@pms.hasPermission('platform:coupon:update')")
    @ApiOperation(value = "审核活动")
    public ServerResponseEntity<Void> auditCoupon(@RequestBody OfflineHandleEventAuditParam offlineHandleEventAuditParam) {
        Long sysUserId = SecurityUtils.getSysUser().getUserId();
        Coupon coupon = couponService.getCouponAndCouponProdsByCouponId(offlineHandleEventAuditParam.getHandleId());
        if (coupon == null) {
            // 未找到该活动信息
            throw new YamiShopBindException("yami.activity.cannot.find");
        }
        couponService.auditCoupon(offlineHandleEventAuditParam, sysUserId);
        //发送活动审核提醒给商家
        NotifyTemplateParam shopParam = new NotifyTemplateParam();
        shopParam.setShopId(coupon.getShopId());
        shopParam.setActivityId(coupon.getCouponId());
        shopParam.setActivityName(coupon.getCouponName());
        shopParam.setSendType(SendType.ACTIVITY_AUDIT.getValue());
        eventPublisher.publishEvent(new SendMessageEvent(shopParam));
        // 移除缓存
        couponService.removeCouponAndCouponProdsCache(coupon.getShopId());
        return ServerResponseEntity.success();
    }

    @PostMapping
    @ApiOperation(value = "保存优惠券")
    public ServerResponseEntity<Void> save(@RequestBody @Valid Coupon coupon) {
        // 此处新增优惠券皆为平台通用
        coupon.setShopId(Constant.PLATFORM_SHOP_ID);
        coupon.setSourceStock(coupon.getStocks());
        couponService.insertCouponAndCouponProds(coupon);
        return ServerResponseEntity.success();
    }

    @PutMapping
    @ApiOperation(value = "修改优惠券")
    public ServerResponseEntity<Void> update(@RequestBody @Valid Coupon coupon) {
        // 查询平台下优惠券
        Coupon dbCoupon = couponService.getById(coupon.getCouponId());
        if (null == dbCoupon) {
            // 优惠券不存在
            throw new YamiShopBindException("yami.coupon.no.exist");
        }
        coupon.setShopId(dbCoupon.getShopId());
        //被领取数量
        int receNum = dbCoupon.getSourceStock() - dbCoupon.getStocks();
        //新的原始库存
        int newSourceStock = coupon.getStocks() + receNum;
        coupon.setSourceStock(newSourceStock);
        couponService.updateCouponAndCouponProds(coupon);
        return ServerResponseEntity.success();
    }

    @PutMapping("/sendUserCoupon")
    @PreAuthorize("@pms.hasPermission('platform:coupon:sendUserCoupon')")
    @ApiOperation(value = "平台批量发放优惠券给会员")
    public ServerResponseEntity<Boolean> sendUserCoupon(@RequestBody UserUpdateParam param) {
        List<String> userIds = param.getUserIds();
        if (CollectionUtils.isEmpty(userIds)) {
            return ServerResponseEntity.success();
        }
        List<SendUserCouponsParam> coupons = param.getCoupons();
        if (CollectionUtils.isEmpty(coupons)) {
            return ServerResponseEntity.success();
        }
        for (String userId : userIds) {
            for (SendUserCouponsParam coupon : coupons) {
                Long couponId = coupon.getCouponId();
                Integer nums = coupon.getNums();
                if (Objects.equals(0, nums)) {
                    continue;
                }
                for (int i = 0; i < nums; i++) {
                    couponService.batchBindCouponByIds(Collections.singletonList(couponId), userId, 0L);
                }
            }
        }
        return ServerResponseEntity.success();
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
}
