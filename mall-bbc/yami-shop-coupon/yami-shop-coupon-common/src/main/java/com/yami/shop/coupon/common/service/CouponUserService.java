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
import com.yami.shop.bean.model.CouponAppConnect;
import com.yami.shop.bean.param.*;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.coupon.common.model.CouponUser;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

/**
 * @author lgh on 2018/12/27.
 */
public interface CouponUserService extends IService<CouponUser> {

//    List<CouponUser> getCouponAndCouponUserByCouponUserIds(List<Long> couponUserIds);

    /**
     * 删除用户失效半年以上的优惠券
     * @param date 时间
     */
    void deleteUnValidTimeCoupons(Date date);

    /**
     * 设置用户的过期优惠券为失效状态
     * @param now 时间
     */
    void updateStatusByTime(Date now);

    /**
     * 将优惠券状态改为可用状态并删除优惠券记录
     * @param userId 用户id
     * @param orderNumber 订单编号
     */
    void cancelOrder(String userId, String orderNumber);

    /**
     * 卡券分析，卡券昨日关键指标
     * @param param 参数信息
     * @return 卡券昨日关键指标
     */
    List<CouponAnalysisParam> getCouponAnalysis(ProdEffectParam param);

    /**
     * 领券会员数
     * @param param 参数信息
     * @return 领券会员数
     */
    Integer countMemberGetCoupon(CustomerReqParam param);

    /**
     * 领券会员数
     * @param param 参数信息
     * @return 领券会员数
     */
    Integer countMemberCouponByParam(MemberReqParam param);

    /**
     * 获取优惠券各种状态的数量
     * @param userId 用户id
     * @return 用户优惠券信息
     */
    CouponUserParam getCouponCountByUserId(String userId);

    /**
     * 根据日期获得优惠券详情信息
     * @param page page参数
     * @param param 日期参数
     * @return 优惠券详情信息
     */
    Page<CouponAnalysisParam> getCouponAnalysisParamByDate(Page page,ProdEffectParam param);

    CouponUser getCouponUserByQrCode(String couponUserNumber);

    IPage<CouponUser> getCouponUserPage(PageParam<CouponUser> page, CouponUser couponUser);

    List<CouponAppConnect> queryNotUseCouponUserData();

    /**
     * 推送即将失效的优惠券短信给用户
     */
    List<CouponUser> couponSendMessage();

    /**
     * 功能描述： 导出导出优惠券领取信息
     * @param couponUser 查询参数
     * @param response response参数
     */
    public void downloadCouponUser(CouponUser couponUser, HttpServletResponse response);

}
