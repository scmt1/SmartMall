/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.coupon.common.listener;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yami.shop.bean.dto.OrderRefundDto;
import com.yami.shop.bean.enums.RefundStatusEnum;
import com.yami.shop.bean.event.OrderRefundSuccessEvent;
import com.yami.shop.bean.model.Order;
import com.yami.shop.coupon.common.dao.CouponUseRecordMapper;
import com.yami.shop.coupon.common.model.CouponUseRecord;
import com.yami.shop.coupon.common.model.CouponUser;
import com.yami.shop.coupon.common.service.CouponUserService;
import com.yami.shop.service.OrderService;
import lombok.AllArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 退款订单监听
 * @author lhd
 */
@Component("couponOrderRefundListener")
@AllArgsConstructor
public class OrderRefundListener {

    private final OrderService orderService;
    private final CouponUseRecordMapper couponUseRecordMapper;
    private final CouponUserService couponUserService;


    /**
     * 退还用户此笔退款订单使用的优惠券
     */
    @EventListener(OrderRefundSuccessEvent.class)
    public void conponOrderRefundSuccessEvent(OrderRefundSuccessEvent event) {
        OrderRefundDto orderRefundDto = event.getOrderRefundDto();
        //获取退款的所有订单项
        if (CollectionUtils.isEmpty(orderRefundDto.getOrderItems())) {
            return;
        }
        Order order = orderService.getOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderNumber, orderRefundDto.getOrderNumber()));
        // 不是整单退款，不需要退优惠券
        if (!Objects.equals(order.getRefundStatus(), RefundStatusEnum.SUCCEED.value())) {
            return;
        }
        String orderNumber = order.getOrderNumber();
        List<CouponUseRecord> records = couponUseRecordMapper.getOrderNumberContact(orderNumber);
        List<CouponUseRecord> list = handlerCouponRecords(records, orderNumber);
        // 订单没有使用优惠券，不需要处理
        if (CollUtil.isEmpty(list)) {
            return;
        }
        long currentTime = System.currentTimeMillis();
        List<CouponUser> couponUsers = new ArrayList<>();
        List<Long> ids = new ArrayList<>();
        for (CouponUseRecord couponUseRecord : list) {
            CouponUser couponUser = couponUseRecord.getCouponUser();
            // 已过有效时间、违规下架或已过期的优惠券， 不退还
            boolean notHandle = currentTime >= couponUser.getUserEndTime().getTime() || couponUser.getCoupon().getPutonStatus() > 1 || Objects.equals(couponUser.getCoupon().getOverdueStatus(), 0);
            if (notHandle) {
                continue;
            }
            couponUseRecord.setStatus(3);
            ids.add(couponUseRecord.getCouponUseRecordId());
            couponUseRecord.getCouponUser().setStatus(1);
            couponUsers.add(couponUseRecord.getCouponUser());
        }
        couponUseRecordMapper.batchUpdateRecordStatusByStatusAndIds(3, ids);
        couponUserService.updateBatchById(couponUsers);
    }

    private List<CouponUseRecord> handlerCouponRecords(List<CouponUseRecord> records, String orderNumber) {
        if (CollUtil.isEmpty(records)) {
            return new ArrayList<>();
        }
        Set<CouponUseRecord> resList = new HashSet<>();
        for (CouponUseRecord record : records) {
            String dbOrderNumber = record.getOrderNumber();
            String[] orderNums = dbOrderNumber.split(",");
            //查看除当前订单号之外的其他订单是否已经退款完成
            boolean flag = true;
            if (orderNums.length > 1) {
                for (String orderNum : orderNums) {
                    if (!orderNum.equals(orderNumber)) {
                        Order order = orderService.getOrderByOrderNumber(orderNum);
                        boolean isRefundSuccess = Objects.nonNull(order.getRefundStatus()) && Objects.equals(order.getRefundStatus(),2);
                        if (!isRefundSuccess) {
                            flag = false;
                        }
                    }
                }
            }
            List<CouponUseRecord> couponUseRecords;
            if (flag) {
                // 单个或多个订单均退款成功
                couponUseRecords = couponUseRecordMapper.listByOrderNumber(dbOrderNumber);
            } else {
                //有订单没有退款完成就只查当前订单自己的优惠券记录
                couponUseRecords = couponUseRecordMapper.listByOrderNumber(orderNumber);
            }
            resList.addAll(couponUseRecords);
        }
        return new ArrayList<>(resList);
    }
}
