/*
 * Copyright (c) 2018-2999 广州亚米信息科技有限公司 All rights reserved.
 *
 * https://www.gz-yami.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */

package com.yami.shop.coupon.api.manager.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.yami.shop.bean.app.dto.*;
import com.yami.shop.bean.param.ChooseCouponParam;
import com.yami.shop.bean.param.PlatformChooseCouponParam;
import com.yami.shop.common.util.Arith;
import com.yami.shop.coupon.api.util.ChooseCouponHelper;
import com.yami.shop.coupon.common.service.CouponService;
import com.yami.shop.manager.CouponConfirmOrderManager;
import com.yami.shop.security.api.util.SecurityUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 确认订单信息时的优惠券的相关操作，计算优惠券 优惠金额
 *
 * @author LGH
 */
@Component
@AllArgsConstructor
public class CouponConfirmOrderManagerImpl implements CouponConfirmOrderManager {

    private final CouponService couponService;

    @Override
    public List<ShopCartDto> chooseShopCoupon(ChooseCouponParam param) {
        String userId = SecurityUtils.getUser().getUserId();

        List<ShopCartDto> shopCarts = param.getShopCarts();

        for (ShopCartDto shopCart : shopCarts) {
            List<ShopCartItemDiscountDto> shopCartItemDiscounts = shopCart.getShopCartItemDiscounts();
            List<ShopCartItemDto> shopCartItems = new ArrayList<>();
            for (ShopCartItemDiscountDto shopCartItemDiscount : shopCartItemDiscounts) {
                shopCartItems.addAll(shopCartItemDiscount.getShopCartItems());
            }
            // 因为经过满减，实际金额的顺序已经变了
            shopCartItems = shopCartItems.stream().sorted(Comparator.comparingDouble(ShopCartItemDto::getActualTotal)).collect(Collectors.toList());

            // 获取用户有效的优惠券
            List<CouponOrderDto> shopCoupons = couponService.getCouponListByShopIds(userId, shopCart.getShopId());

            // 用户选中的所有优惠券
            List<Long> couponUserIds = param.getCouponUserIds();

            Integer userChangeCoupon = param.getUserChangeCoupon();

            ChooseCouponHelper chooseCouponHelper = new ChooseCouponHelper(shopCartItems, shopCoupons, couponUserIds, userChangeCoupon).invoke();

            if (chooseCouponHelper.getChooseCoupon() != null) {

                double couponReduce = chooseCouponHelper.getCouponReduce();
                // 最后组装订单信息
                shopCart.setCouponReduce(Math.min(couponReduce, shopCart.getActualTotal()));
                shopCart.setActualTotal(Arith.sub(shopCart.getActualTotal(), shopCart.getCouponReduce()));
                shopCart.setShopReduce(Arith.add(shopCart.getShopReduce(), shopCart.getCouponReduce()));
            }

            shopCart.setCoupons(shopCoupons);
        }
        return shopCarts;
    }

    @Override
    public ShopCartOrderMergerDto choosePlatformCoupon(PlatformChooseCouponParam param) {
        String userId = SecurityUtils.getUser().getUserId();
        ShopCartOrderMergerDto shopCartOrderMergerVO = param.getShopCartOrderMergerDto();

        // 获取用户可用平台优惠券
        List<CouponOrderDto> couponList = couponService.getCouponListByShopIds(userId, 0L);
        if (CollectionUtil.isEmpty(couponList)) {
            return shopCartOrderMergerVO;
        }

        // 完整的订单信息
        // 订单项目
        List<ShopCartItemDto> shopAllShopCartItems = new ArrayList<>();
        List<ShopCartOrderDto> shopCartOrders = shopCartOrderMergerVO.getShopCartOrders();
        for (ShopCartOrderDto shopCartOrder : shopCartOrders) {
            List<ShopCartItemDiscountDto> shopCartItemDiscounts = shopCartOrder.getShopCartItemDiscounts();
            for (ShopCartItemDiscountDto shopCartItemDiscount : shopCartItemDiscounts) {
                shopAllShopCartItems.addAll(shopCartItemDiscount.getShopCartItems());
            }
        }
        // 因为经过满减，实际金额的顺序已经变了
        shopAllShopCartItems = shopAllShopCartItems.stream().sorted(Comparator.comparingDouble(ShopCartItemDto::getActualTotal)).collect(Collectors.toList());

        ChooseCouponHelper chooseCouponHelper = new ChooseCouponHelper(shopAllShopCartItems, couponList, param.getCouponUserIds(), param.getUserChangeCoupon()).invoke();


        if (chooseCouponHelper.getChooseCoupon() != null) {
            double couponReduce = chooseCouponHelper.getCouponReduce();
            Map<Long, Double> shopReduceMap = chooseCouponHelper.getShopReduceMap();
            for (ShopCartOrderDto shopCartOrder : shopCartOrders) {
                Double couponAmount = shopReduceMap.get(shopCartOrder.getShopId());
                shopCartOrder.setPlatformCouponReduce(couponAmount);
                couponAmount = Objects.isNull(shopCartOrder.getPlatformAmount()) ? couponAmount : Arith.add(shopCartOrder.getPlatformAmount(), couponAmount);
                shopCartOrder.setPlatformAmount(couponAmount);
                shopCartOrder.setActualTotal(Arith.sub(shopCartOrder.getActualTotal(), shopCartOrder.getPlatformCouponReduce()));
                shopCartOrder.setShopReduce(Arith.add(shopCartOrder.getShopReduce(), shopCartOrder.getPlatformCouponReduce()));
            }
            shopCartOrderMergerVO.setOrderReduce(Arith.add(shopCartOrderMergerVO.getOrderReduce(), couponReduce));
            shopCartOrderMergerVO.setActualTotal(Arith.sub(shopCartOrderMergerVO.getActualTotal(), couponReduce));
        }
        shopCartOrderMergerVO.setCoupons(couponList);
        return shopCartOrderMergerVO;
    }
}
