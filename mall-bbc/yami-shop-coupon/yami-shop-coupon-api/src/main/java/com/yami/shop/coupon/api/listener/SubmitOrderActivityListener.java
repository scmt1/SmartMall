/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.coupon.api.listener;

import cn.hutool.core.collection.CollectionUtil;
import com.yami.shop.bean.app.dto.CouponOrderDto;
import com.yami.shop.bean.app.dto.ShopCartOrderDto;
import com.yami.shop.bean.app.dto.ShopCartOrderMergerDto;
import com.yami.shop.bean.event.SubmitOrderActivityEvent;
import com.yami.shop.bean.order.SubmitOrderActivityOrder;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.coupon.common.dao.CouponUserMapper;
import com.yami.shop.security.api.util.SecurityUtils;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 确认订单信息时的优惠券的相关操作，计算优惠券 优惠金额
 *
 * @author LGH
 */
@Component("checkCouponStatusListener")
@AllArgsConstructor
public class SubmitOrderActivityListener {

    private final CouponUserMapper couponUserMapper;

    /**
     * 将店铺下的所有商品归属到该店铺的购物车当中
     *
     * @param event#ShopCartOrderMergerDto 组装完成的商品订单信息
     * @return 是否继续组装
     */
    @EventListener(SubmitOrderActivityEvent.class)
    @Order(SubmitOrderActivityOrder.COUPON)
    public void checkCouponStatusListener(SubmitOrderActivityEvent event) {
        String userId = SecurityUtils.getUser().getUserId();
        ShopCartOrderMergerDto shopCartOrderMergerDto = event.getShopCartOrderMergerDto();
        List<Long> couponUserIds = new ArrayList<>();
        if(Objects.isNull(shopCartOrderMergerDto.getCoupons()) || CollectionUtil.isEmpty(shopCartOrderMergerDto.getCoupons())){
            return;
        }
        for (CouponOrderDto coupon : shopCartOrderMergerDto.getCoupons()) {
            if(coupon.isChoose()){
                couponUserIds.add(coupon.getCouponUserId());
                break;
            }
        }
        for (ShopCartOrderDto shopCartOrderDto : shopCartOrderMergerDto.getShopCartOrders()) {
            for (CouponOrderDto coupon : shopCartOrderDto.getCoupons()) {
                if(coupon.isChoose()){
                    couponUserIds.add(coupon.getCouponUserId());
                    break;
                }
            }
        }
        if(CollectionUtil.isEmpty(couponUserIds)){
            return;
        }
        int num = couponUserMapper.countNormalByCouponUserId(couponUserIds,userId);
        if(num < couponUserIds.size()){
            throw new YamiShopBindException("yami.order.coupon.expire.check");
        }
    }



}
