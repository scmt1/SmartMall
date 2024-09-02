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
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yami.shop.bean.model.CouponAppConnect;
import com.yami.shop.bean.param.CouponAnalysisParam;
import com.yami.shop.bean.param.CustomerReqParam;
import com.yami.shop.bean.param.MemberReqParam;
import com.yami.shop.bean.param.ProdEffectParam;
import com.yami.shop.common.util.PageAdapter;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.coupon.common.model.CouponUser;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 优惠券商品信息
 *
 * @author lanhai
 */
public interface CouponUserMapper extends BaseMapper<CouponUser> {

    /**
     * 获取用户优惠券信息
     *
     * @param userId 用户id
     * @return 用户优惠券信息
     */
    List<CouponUser> getCouponAndCouponUserByUserId(@Param("userId") String userId);

    /**
     * 通过关联ids获取用户优惠券信息
     *
     * @param couponUserIds 用户优惠券ids
     * @return 用户优惠券信息
     */
    List<CouponUser> getCouponAndCouponUserByCouponUserIds(@Param("couponUserIds") List<Long> couponUserIds);

    /**
     * 删除用户失效半年以上的优惠券
     *
     * @param date 时间
     */
    void deleteUnValidTimeCoupons(@Param("date") Date date);

    /**
     * 设置用户的过期优惠券为失效状态
     *
     * @param now 时间
     */
    void updateStatusByTime(@Param("now") Date now);

    /**
     * 删除用户优惠关联信息
     *
     * @param userId   用户id
     * @param couponId 优惠券id
     */
    void deleteUserCoupon(@Param("userId") String userId, @Param("couponId") Long couponId);

    /**
     * 修改用户优惠券状态
     *
     * @param status       状态
     * @param couponUserId 优惠券用户id
     */
    void updateUseStatusByCouponUserId(@Param("status") Integer status, @Param("couponUserId") Long couponUserId);

    /**
     * 批量修改用户优惠券状态
     *
     * @param status        状态
     * @param couponUserIds 优惠券用户ids
     */
    void batchUpdateUserCouponStatus(@Param("status") int status, @Param("couponUserIds") List<Long> couponUserIds);

    /**
     * 批量修改用户优惠券状态通过订单编号
     *
     * @param status      状态
     * @param orderNumber 订单编号
     */
    void updateStatusByOrderNumber(@Param("status") int status, @Param("orderNumber") String orderNumber);

    /**
     * 根据日期判断用户优惠券详情
     *
     * @param page
     * @param param 日期
     * @return 优惠券详情
     */
    Page<CouponAnalysisParam> getCouponAnalysisParamByDate(Page page, @Param("param") ProdEffectParam param);

    /**
     * 根据日期获取优惠券使用次数
     *
     * @param couponId 优惠券id
     * @param param
     * @return 使用次数
     */
    Integer countUseNum(@Param("couponId") Long couponId, @Param("param") ProdEffectParam param);

    /**
     * 获取用户优惠券领取次数
     *
     * @param couponId 优惠券id
     * @param status   状态
     * @param param    参数
     * @return 领取次数
     */
    Integer countTakeNum(@Param("couponId") Long couponId, @Param("status") Integer status, @Param("param") ProdEffectParam param);

    /**
     * 领券会员数
     *
     * @param param 相关条件参数
     * @return 领券会员数
     */
    Integer countMemberGetCoupon(@Param("param") CustomerReqParam param);

    /**
     * 领券会员数
     *
     * @param param 会员类型参数
     * @return 领券会员数
     */
    Integer countMemberCouponByParam(@Param("param") MemberReqParam param);

    /**
     * 用户未使用的并且是未过期的优惠券数量
     *
     * @param userId 用户id
     * @param date   时间
     * @return 优惠券数量
     */
    Integer countCouponUsableNums(@Param("userId") String userId, @Param("date") Date date);

    /**
     * 用户已使用的优惠券数量
     *
     * @param userId 用户id
     * @return 优惠券数量
     */
    Integer countCouponUsedNums(@Param("userId") String userId);

    /**
     * 用户已失效的优惠券
     *
     * @param userId 用户id
     * @return 优惠券数量
     */
    Integer countCouponExpiredNums(@Param("userId") String userId);

    /**
     * 获取某个用户的优惠券明细
     *
     * @param page   分页信息
     * @param userId 用户id
     * @param status 状态
     * @return 分页优惠券明细
     */
    IPage<CouponUser> getPageByUserId(PageParam<CouponUser> page, @Param("userId") String userId, @Param("status") Integer status);

    /**
     * 根据id获取用户正常优惠券的数量
     *
     * @param couponUserIds 选择的优惠券
     * @param userId        用户id
     * @return 用户正常优惠券的数量
     */
    int countNormalByCouponUserId(@Param("couponUserIds") List<Long> couponUserIds, @Param("userId") String userId);

    /**
     * 获取用户领取的优惠券及数量
     * @param couponIds
     * @param userId
     * @return
     */
    List<CouponUser> listByCouponIdsAndUserId(@Param("couponIds") List<Long> couponIds, @Param("userId") String userId);

    CouponUser getCouponUserByQrCode(String couponUserNumber);

    List<CouponUser> getCouponUserPage(@Param("adapter") PageAdapter adapter, @Param("couponUser") CouponUser couponUser);

    /**
     * 根据参数，统计查询到的使用记录总数
     * @param couponUser   条件查询参数
     * @return 数量
     */
    Integer countGetCouponUserPageByParam(@Param("couponUser") CouponUser couponUser);

    List<CouponAppConnect> queryNotUseCouponUserData();

    /**
     * 推送即将失效的优惠券短信给用户
     */
    List<CouponUser> couponSendMessage();

    List<CouponUser> getCouponUserList(@Param("couponUser") CouponUser couponUser);
}
