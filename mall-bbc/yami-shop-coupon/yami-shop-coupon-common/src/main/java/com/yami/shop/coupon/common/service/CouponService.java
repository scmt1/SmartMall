/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.coupon.common.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yami.shop.bean.app.dto.CouponDto;
import com.yami.shop.bean.app.dto.CouponOrderDto;
import com.yami.shop.bean.app.dto.ProductDto;
import com.yami.shop.bean.model.CouponOrder;
import com.yami.shop.bean.param.OfflineHandleEventAuditParam;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.coupon.common.model.Coupon;
import com.yami.shop.coupon.common.model.CouponUser;
import org.apache.ibatis.annotations.Param;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author lgh on 2018/12/27.
 */
public interface CouponService extends IService<Coupon> {

    /**
     * 修改优惠券关联商品信息
     * @param coupon 优惠券信息
     */
    void updateCouponAndCouponProds(@Valid Coupon coupon);

    /**
     * 插入优惠券关联商品信息
     * @param coupon 优惠券信息
     */
    void insertCouponAndCouponProds(@Valid Coupon coupon);

    /**
     * 获取优惠券关联商品信息
     * @param id 优惠券id
     * @return
     */
    Coupon getCouponAndCouponProdsByCouponId(Long id);

    /**
     * 根据用户id发放优惠券
     * @param coupon 优惠券信息
     * @param userId 用户id
     * @param lat
     * @param lng
     */
    void receive(Coupon coupon, String userId, Double lat, Double lng);

    /**
     * 定时任务，改变店铺优惠券的状态
     *
     * @param now 时间
     */
    void changeCoupon(Date now);

    /**
     * 投放优惠券
     * @param now 时间
     */
    void putonCoupon(Date now);

    /**
     * 删除优惠券
     * @param couponId 优惠券id
     */
    void deleteByCouponId(Long couponId);

    /**
     * 获取通用券列表
     * @param userId 用户id
     * @return 通用券列表
     */
    List<CouponDto> generalCouponList(String userId,Coupon coupon);

    /**
     * 游客状态查询所有通用优惠券列表
     * @return 通用优惠券列表
     */
    List<CouponDto> generalCouponList(Coupon coupon);

    /**
     * 通过状态查看用户的优惠券列表信息
     * @param page 分页信息
     * @param userId 用户id
     * @param status 状态
     * @return 用户的优惠券列表信息
     */
    IPage<CouponDto> getCouponListByStatus(IPage<CouponDto> page, String userId, Integer status);

    /**
     * 通过优惠券id删除用户优惠券
     * @param userId 用户id
     * @param couponId 优惠券id
     */
    void deleteUserCouponByCouponId(String userId, Long couponId);

    /**
     * 获取用户有效的优惠券
     * @param userId 用户id
     * @param shopId 店铺id
     * @return 用户有效的优惠券列表
     */
    List<CouponOrderDto> getCouponListByShopIds(String userId, Long shopId);

    /**
     * 获取已投放优惠券
     *
     * @param shopId 店铺id
     * @return 已投放优惠券
     */
    List<Coupon> listPutonByShopId(Long shopId);

    /**
     * 查看用户拥有的所有优惠券
     * @param userId 用户id
     * @return 用户拥有的所有优惠券
     */
    List<CouponDto> listCouponIdsByUserId(String userId);

    /**
     * 通过优惠券id获取商品列表
     * @param page 分页信息
     * @param coupon 优惠券信息
     * @param dbLang 语言
     * @return 商品列表
     */
    IPage<ProductDto> prodListByCoupon(PageParam<ProductDto> page, Coupon coupon, Integer dbLang);

    /**
     * 商家端分页获取优惠券数据
     * @param page 分页参数
     * @param coupon 优惠券
     * @return 优惠券数据
     */
    IPage<Coupon> getMultiShopPage(PageParam<Coupon> page, Coupon coupon);

    /**
     * 商家端分页获取优惠券数据
     * @param page 分页参数
     * @param coupon 优惠券
     * @return 优惠券数据
     */
    IPage<Coupon> getShopCouponsPage(PageParam<Coupon> page, Coupon coupon);

    /**
     * 商家端获取优惠券使用数据
     * @param coupon 优惠券
     * @return 优惠券数据
     */
    List<Coupon> shopWriteOffDetail(Coupon coupon);
    /**
     * 获取各个状态下优惠券个数
     *
     * @param userId 用户id
     * @return 各个状态下优惠券个数map
     */
    Map<String, Long> getCouponCountByStatus(String userId);

    /**
     *  平台  --获取分页优惠券数据
     * @param page 分页信息
     * @param coupon 优惠券信息
     * @return 分页优惠券数据
     */
    IPage<Coupon> getPlatformPage(PageParam<Coupon> page, Coupon coupon);

    /**
     *  获取分页优惠券数据
     * @param page 分页信息
     * @param coupon 优惠券信息
     * @return 分页优惠券数据
     */
    IPage<Coupon> getCouponsPage(PageParam<Coupon> page, Coupon coupon);

    /**
     * 统计优惠券信息
     * @param coupon
     * @return
     */
    Coupon statisticCoupon(Coupon coupon);

    /**
     * 统计优惠券领取和使用数量
     * @param coupon
     * @return
     */
    Coupon receiveAndUseStatistic(Coupon coupon);

    /**
     * 平台 --下线活动
     * @param coupon 优惠券信息
     * @param offlineReason 下线备注
     * @param sysUserId 系统用户id
     */
    void offline(Coupon coupon, String offlineReason, Long sysUserId);

    /**
     * 平台 --审核活动
     * @param offlineHandleEventAuditParam 审核信息
     * @param sysUserId 系统用户id
     */
    void auditCoupon(OfflineHandleEventAuditParam offlineHandleEventAuditParam, Long sysUserId);

    /**
     * 申请活动审核
     * @param eventId 事件id
     * @param couponId 优惠券id
     * @param reapplyReason 申请理由
     */
    void auditApply(Long eventId, Long couponId, String reapplyReason);

    /**
     *  移除优惠券缓存
     *
     * @param shopId 店铺id
     */
    void removeCouponAndCouponProdsCache(Long shopId);

    /**
     * 批量绑定优惠券，如果已达上限等领取不了的情况直接退出
     * @param couponIds 优惠券ids
     * @param userId 用户id
     * @param shopId 店铺id
     */
    void batchBindCouponByIds(List<Long> couponIds, String userId,Long shopId);

    /**
     * 绑定优惠券，只发放优惠券不改库存
     * @param couponIds 优惠券ids
     * @param userId 用户id
     * @param shopId 店铺id
     */
    void bindCouponById(List<Long> couponIds, String userId,Long shopId);

    /**
     * 获取  领优惠卷 接口的优惠券列表
     * @param page 分页信息
     * @param userId 用户id
     * @return 优惠券列表
     */
    IPage<CouponDto> getCouponList(Page<CouponDto> page, String userId);

    /**
     * 获取所有可领取的优惠券
     * @param page 分页信息
     * @return 可领取的优惠券
     */
    IPage<CouponDto> getCouponList(Page<CouponDto> page);

    /**
     * 获取某个用户的优惠券明细
     * @param page 分页
     * @param userId 用户id
     * @param status 状态
     * @return 优惠券明细
     */
    IPage<CouponUser> getPageByUserId(PageParam<CouponUser> page, String userId, Integer status);

    /**
     * 通过用户优惠券id获取优惠券信息
     * @param couponUserId 用户优惠券id
     */
    CouponDto getCouponUserInfo(Long couponUserId);

    void cancelPutCoupon(Date now);

    List<CouponDto> queryCouponUserDetail(Long couponId, String userId, Long couponUserId);

    List<CouponDto> getShopAllCouponList(Coupon coupon);

    /**
     * 修改优惠券库存
     *
     * @param couponId 优惠券id
     * @return 结果
     */
    int updateCouponStocksAndVersion(@Param("couponId") Long couponId);

    List<CouponOrder> queryNoUseCouponList();

}
