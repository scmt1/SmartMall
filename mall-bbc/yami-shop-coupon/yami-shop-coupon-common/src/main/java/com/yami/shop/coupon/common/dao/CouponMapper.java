/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.coupon.common.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yami.shop.bean.app.dto.CouponDto;
import com.yami.shop.bean.app.dto.CouponOrderDto;
import com.yami.shop.bean.model.CouponOrder;
import com.yami.shop.common.util.PageAdapter;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.coupon.common.model.Coupon;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 优惠券
 *
 * @author lanhai
 */
public interface CouponMapper extends BaseMapper<Coupon> {

    /**
     * 根据优惠券id获取优惠券的信息及可用商品信息
     *
     * @param id 优惠券id
     * @return 优惠券的信息及可用商品信息
     */
    Coupon getCouponAndCouponProdsByCouponId(@Param("id") Long id);

    /**
     * 修改优惠券库存
     *
     * @param couponId 优惠券id
     * @return 结果
     */
    int updateCouponStocksAndVersion(@Param("couponId") Long couponId);

    /**
     * 设置店铺的过期优惠券为过期状态
     *
     * @param now 时间
     */
    void updateOverdueStatusByTime(@Param("now") Date now);

    /**
     * 投放优惠券
     *
     * @param now 时间
     */
    void putonCoupon(@Param("now") Date now);

    /**
     * 获取通用优惠券列表
     *
     * @param userId 用户id
     * @return 通用优惠券列表
     */
    List<CouponDto> generalCouponList(@Param("userId") String userId,@Param("coupon") Coupon coupon);

    /**
     * 获取通用团购券列表
     *
     * @param userId 用户id
     * @return 通用团购券列表
     */
    List<CouponDto> generalGroupCouponList(@Param("userId") String userId);

    /**
     * 通过优惠券状态获取优惠券列表
     *
     * @param page   分页
     * @param userId 用户id
     * @param status 状态
     * @return 优惠券分页列表
     */
    IPage<CouponDto> getCouponListByStatus(IPage<CouponDto> page, @Param("userId") String userId, @Param("status") Integer status);

    /**
     * 通过店铺id及用户id获取用户未使用的优惠券
     *
     * @param userId 用户id
     * @param shopId 店铺id
     * @return 用户未使用的优惠券列表
     */
    List<CouponOrderDto> getCouponListByShopIds(@Param("userId") String userId, @Param("shopId") Long shopId);


    /**
     * 查询需要更新的店铺id列表
     *
     * @param now 当前时间
     * @return 店铺id列表
     */
    List<Long> listOverdueStatusShopIds(@Param("now") Date now);

    /**
     * 获取某店铺所有的投放优惠券和优惠商品列表
     *
     * @param shopId 店铺id
     * @return 优惠券列表
     */
    List<Coupon> listPutonCouponAndCouponProdsByShopId(@Param("shopId") Long shopId);

    /**
     * 获取用户的优惠券信息
     *
     * @param userId 用户id
     * @return 优惠券列表
     */
    List<CouponDto> listCouponIdsByUserId(@Param("userId") String userId);

    /**
     * 获取用户各个状态下的优惠券个数
     *
     * @param userId 用户id
     * @return 用户各个状态下的优惠券个数
     */
    Map<String, Long> getCouponCountByStatus(@Param("userId") String userId);

    /**
     * 平台  -- 分页获取优惠券数据
     *
     * @param page   分页
     * @param coupon 优惠券
     * @return 分页优惠券数据
     */
    IPage<Coupon> getPlatformPage(@Param("page") PageParam<Coupon> page, @Param("coupon") Coupon coupon);

    /**
     * 分页获取优惠券数据
     *
     * @param page   分页
     * @param coupon 优惠券
     * @return 分页优惠券数据
     */
    IPage<Coupon> getCouponsPage(@Param("page") PageParam<Coupon> page, @Param("coupon") Coupon coupon);

    /**
     * 统计优惠券信息
     * @param coupon
     * @return
     */
    Coupon statisticCoupon(@Param("coupon") Coupon coupon);

    /**
     * 统计优惠券领取数量
     * @param coupon
     * @return
     */
    Coupon receiveAndUseStatistic(@Param("coupon") Coupon coupon);

    /**
     * 获取优惠券id列表
     * @param coupon
     * @return
     */
    List<Coupon> getCoupons(@Param("coupon") Coupon coupon);

    /**
     * 商家端--分页获取优惠券数据
     *
     * @param page   分页参数
     * @param coupon 优惠券
     * @return 优惠券数据
     */
    IPage<Coupon> getMultiShopPage(@Param("page") PageParam<Coupon> page, @Param("coupon") Coupon coupon);

    /**
     * 商家端--分页获取优惠券数据
     *
     * @param page   分页参数
     * @param coupon 优惠券
     * @return 优惠券数据
     */
    IPage<Coupon> getShopCouponsPage(@Param("page") PageParam<Coupon> page, @Param("coupon") Coupon coupon);

    /**
     * 商家端--获取优惠券核销数据
     *
     * @param coupon 优惠券
     * @return 优惠券数据
     */
    List<Coupon> shopWriteOffDetail(@Param("coupon") Coupon coupon);

    /**
     * 根据优惠券id获取其领取次数
     *
     * @param couponId 优惠券id
     * @param shopId   店铺id
     * @return 领取次数
     */
    Integer countTakeNum(@Param("couponId") Long couponId, @Param("shopId") Long shopId);

    /**
     * 根据优惠券id获取其使用次数
     *
     * @param couponId 优惠券id
     * @return 使用次数
     */
    Integer countUseNum(@Param("couponId") Long couponId);

    /**
     * 获取总领取次数
     *
     * @param couponIds 优惠券id列表
     * @param shopId   店铺id
     * @return 领取次数
     */
    Integer countTakeNumTotal(@Param("couponIds") List<Long> couponIds, @Param("shopId") Long shopId);

    /**
     * 获取总使用次数
     *
     * @param couponIds 优惠券id列表
     * @return 使用次数
     */
    Integer countUseNumTotal(@Param("couponIds") List<Long> couponIds);

    /**
     * 更新投放状态
     *
     * @param couponId 优惠券id
     * @param status   更新的状态
     * @return 结果
     */
    int updatePutOnStatusByCouponId(@Param("couponId") Long couponId, @Param("status") int status);

    /**
     * 更新投放状态
     *
     * @param couponId      优惠券id
     * @param overdueStatus 投放状态
     * @return 结果
     */
    int updateOverdueStatusByCouponId(@Param("couponId") Long couponId, @Param("overdueStatus") int overdueStatus);

    /**
     * 根据优惠券适用类型，获取优惠券列表
     *
     * @param couponIds 用户id
     * @param lang      语言
     * @return 优惠券列表
     */
    List<CouponDto> getCouponList(@Param("couponIds") List<Long> couponIds, @Param("lang") Integer lang);

    /**
     * 根据优惠券适用类型，获取优惠券列表
     *
     * @param couponForAllProd  适用于所有商品列表
     * @param couponForProd     适用于部分商品列表
     * @param couponForElseProd 不适用于那些商品列表
     * @return 优惠券列表
     */
    List<CouponDto> getCouponListTourist(@Param("couponForAllProd") List<CouponDto> couponForAllProd, @Param("couponForProd") List<Long> couponForProd,
                                         @Param("couponForElseProd") List<CouponDto> couponForElseProd);

    /**
     * 获取店铺可领取优惠券id列表
     *
     * @param pageAdapter 分页信息
     * @return 优惠券列表
     */
    List<Long> getShopAvailableCouponIds(@Param("adapter") PageAdapter pageAdapter);

    /**
     * 获取优惠券数量
     *
     * @return 优惠券数量
     */
    long countCouponPageByCouponDto();

    /**
     * 游客状态查询通用优惠券列表
     *
     * @return 通用优惠券列表
     */
    List<CouponDto> generalCouponListTourist(@Param("coupon") Coupon coupon);

    /**
     * 更新优惠券
     *
     * @param coupon
     */
    void updateCoupon(@Param("coupon") Coupon coupon);

    CouponDto getCouponUserInfo(@Param("couponUserId") Long couponUserId);

    void cancelPutCoupon(@Param("now") Date now);

    CouponDto queryCouponUserDetail(@Param("userId") String userId, @Param("couponId") Long couponId, @Param("shopId") Long shopId);

    List<CouponDto> getShopAllCouponList(@Param("coupon") Coupon coupon);

    List<CouponOrder> queryNoUseCouponList();

}
