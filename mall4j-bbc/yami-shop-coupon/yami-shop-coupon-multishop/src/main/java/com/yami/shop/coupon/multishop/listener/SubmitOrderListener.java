/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.coupon.multishop.listener;

import com.yami.shop.coupon.common.dao.CouponUserMapper;
import com.yami.shop.coupon.common.service.CouponUseRecordService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 优惠券提交订单监听
 * @author lanhai
 */
@Component("couponSubmitOrderListener")
@AllArgsConstructor
public class SubmitOrderListener {

    private final CouponUserMapper couponUserMapper;
    private final CouponUseRecordService couponUseRecordService;
//
//    /**
//     * 修改优惠券为已使用状态
//     */
//    @EventListener(SubmitOrderEvent.class)
//    @Order(SubmitOrderOrder.COUPON)
//    public void couponSubmitOrderListener(SubmitOrderEvent event) {
//
//        //待改状态的优惠列表
//        List<Long> userCouponIds = Lists.newArrayList();
//
//        List<CouponUseRecord> couponUseRecordList = Lists.newArrayList();
//        List<ShopCartOrderDto> shopCartOrders = event.getMergerOrder().getShopCartOrders();
//
//
//        if(CollectionUtils.isEmpty(shopCartOrders)){
//            return ;
//        }
//        // 订单号集合
//        StringBuilder orderNumbers = new StringBuilder(100);
//
//        for (ShopCartOrderDto shopCartOrder : shopCartOrders) {
//            orderNumbers.append(shopCartOrder.getOrderNumber()).append(StrUtil.COMMA);
//            List<CouponOrderDto> shopCartOrderCoupons = shopCartOrder.getCoupons();
//
//            if (CollectionUtils.isEmpty(shopCartOrderCoupons)) {
//                continue;
//            }
//            // 聚合店铺优惠券
//            addCouponUseRecordList(shopCartOrderCoupons, shopCartOrder.getOrderNumber(), couponUseRecordList, userCouponIds);
//        }
//        orderNumbers.deleteCharAt(orderNumbers.length() - 1);
//        // 聚合平台优惠券
//        addCouponUseRecordList(event.getMergerOrder().getCoupons(),orderNumbers.toString(), couponUseRecordList, userCouponIds);
//
//
//        if (CollectionUtils.isNotEmpty(userCouponIds)) {
//            // 批量更新用户优惠券使用状态
//            couponUserMapper.batchUpdateUserCouponStatus(2, userCouponIds);
//        }
//        if (CollectionUtils.isNotEmpty(couponUseRecordList)) {
//            // 批量插用户优惠券使用记录
//            couponUseRecordService.addCouponUseRecode(couponUseRecordList);
//        }
//
//    }
//
//    private void addCouponUseRecordList(List<CouponOrderDto> shopCartOrderCoupons, String orderNumber,List<CouponUseRecord> couponUseRecordList, List<Long> userCouponIds){
//        if (CollectionUtils.isEmpty(shopCartOrderCoupons)) {
//            return;
//        }
//
//        for (CouponOrderDto couponOrderDto : shopCartOrderCoupons) {
//            if (couponOrderDto.isChoose() && couponOrderDto.isCanUse()) {
//                CouponUseRecord couponUseRecord = new CouponUseRecord();
//                couponUseRecord.setAmount(couponOrderDto.getReduceAmount());
//                // 获取用户优惠券id
//                couponUseRecord.setCouponUserId(couponOrderDto.getCouponUserId());
//                couponUseRecord.setUserId(SecurityUtils.getUser().getUserId());
//                couponUseRecord.setOrderNumber(orderNumber);
//                couponUseRecord.setUseTime(new Date());
//                //优惠券记录为冻结状态
//                couponUseRecord.setStatus(1);
//                couponUseRecordList.add(couponUseRecord);
//                userCouponIds.add(couponOrderDto.getCouponUserId());
//            }
//        }
//    }

}
