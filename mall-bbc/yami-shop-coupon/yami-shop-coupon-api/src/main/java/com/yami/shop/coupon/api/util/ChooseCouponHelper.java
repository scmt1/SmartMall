/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.coupon.api.util;

import cn.hutool.core.collection.CollectionUtil;
import com.yami.shop.bean.app.dto.CouponOrderDto;
import com.yami.shop.bean.app.dto.ProductItemDto;
import com.yami.shop.bean.app.dto.ShopCartItemDto;
import com.yami.shop.bean.enums.CouponProdType;
import com.yami.shop.bean.enums.CouponType;
import com.yami.shop.common.util.Arith;
import org.apache.commons.collections.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 协助优惠券进行选择
 *
 * @author LGH
 */
public class ChooseCouponHelper {
    /**
     * 购物项
     */
    private List<ShopCartItemDto> shopCartItems;
    /**
     * 含有的优惠券列表
     */
    private final List<CouponOrderDto> shopCoupons;

    /**
     * 选中的优惠券id
     */
    private List<Long> couponUserIds;

    /**
     * 用户是否选中优惠券
     */
    private final Integer userChangeCoupon;
    /**
     * 选中的优惠券
     */
    private CouponOrderDto chooseCoupon;

    /**
     * 可用优惠券的商品实际金额（商品实际金额 - 商品分摊金额）
     */
    private double prodCanUseCouponActualTotal;

    /**
     * 优惠金额
     */
    private double couponReduce;

    /**
     * 优惠金额参与的商品数量
     */
    private Integer reduceNum;

    /**
     * 优惠金额
     */
    private double chooseCouponProdAmount;

    /**
     * 优惠券在订单中的优惠金额
     */
    private double couponInOrderAmount;

    /**
     * 优惠金额关联店铺map
     */
    private final Map<Long, Double> shopReduceMap = new HashMap<>(12);

    public ChooseCouponHelper(List<ShopCartItemDto> shopCartItems, List<CouponOrderDto> shopCoupons, List<Long> couponUserIds, Integer userChangeCoupon) {
        this.shopCartItems = shopCartItems;
        this.shopCoupons = shopCoupons;
        this.couponUserIds = couponUserIds;
        this.userChangeCoupon = userChangeCoupon;
    }

    public CouponOrderDto getChooseCoupon() {
        return chooseCoupon;
    }

    public Map<Long, Double> getShopReduceMap() {
        return shopReduceMap;
    }

    public double getCouponReduce() {
        return this.couponReduce;
    }

    public ChooseCouponHelper invoke() {
        // 没有平台优惠券直接返回
        if (CollectionUtils.isEmpty(shopCartItems)) {
            return this;
        }

        // 用户选中的关于该店铺的优惠券，一家店只能选择一张优惠券
        chooseCoupon = null;
        reduceNum = 0;

        if (Objects.isNull(couponUserIds)) {
            couponUserIds = new ArrayList<>();
        }

        // 可用优惠券的商品实际金额（商品实际金额 - 商品分摊金额）
        prodCanUseCouponActualTotal = 0.0;
        Date now = new Date();
        for (CouponOrderDto shopCoupon : shopCoupons) {
            if (shopCoupon.getStartTime().getTime() > now.getTime() || shopCoupon.getEndTime().getTime() < now.getTime()) {
                continue;
            }

            // 该优惠券关联的店铺id
            List<Long> shopIds = shopCoupon.getShopIds();


            // 该优惠券关联的商品id
            List<Long> prodIds = shopCoupon.getProdIds();
            // 可用优惠券的商品实际金额（商品实际金额 - 商品分摊金额）
            prodCanUseCouponActualTotal = 0.0;
            for (ShopCartItemDto shopCartItem : shopCartItems) {
                // 套餐不参与优惠券活动
                if (Objects.nonNull(shopCartItem.getComboId()) && shopCartItem.getComboId() != 0) {
                    continue;
                }
                shopCartItem.setIsShareReduce(0);
                // 该商品是否在该优惠券可用
                boolean isCouponsProd = isCouponsProd(shopCartItem.getProdId(), prodIds, shopCoupon.getSuitableProdType());

                boolean isCouponsShop = isCouponsShop(shopCartItem.getProdId(), prodIds, shopCartItem.getShopId(), shopIds, shopCoupon.getSuitableProdType());

                if (isCouponsProd || isCouponsShop) {
                    shopCartItem.setIsShareReduce(1);
                    prodCanUseCouponActualTotal = Arith.add(prodCanUseCouponActualTotal, shopCartItem.getActualTotal());
                }
            }
            Date date = new Date();
            // 如果该商品实际总金额大于等于优惠券最低使用金额，并且开始时间大于优惠券的使用时间，则优惠券可用
            if (prodCanUseCouponActualTotal >= shopCoupon.getCashCondition() && shopCoupon.getStartTime().getTime() < date.getTime()) {
                // 将优惠券标记为可用状态
                shopCoupon.setCanUse(true);
                if (Objects.nonNull(chooseCoupon)) {
                    continue;
                }
                // 聚合 用户选中的关于该店铺的优惠券
                if (couponUserIds.contains(shopCoupon.getCouponUserId())) {
                    chooseCoupon = shopCoupon;
                    chooseCouponProdAmount = prodCanUseCouponActualTotal;
                }
                // 如果用户没有选择优惠券，系统默认选择一张可用优惠券
//                if (chooseCoupon == null && !Objects.equals(userChangeCoupon, 1)) {
//                    chooseCoupon = shopCoupon;
//                    chooseCouponProdAmount = prodCanUseCouponActualTotal;
//                }
            }
        }
        for (CouponOrderDto shopCoupon : shopCoupons) {
            // 该优惠券关联的商品id
            List<Long> prodIds = shopCoupon.getProdIds();

            List<Long> shopIds = shopCoupon.getShopIds();

            if (Objects.nonNull(chooseCoupon) && !Objects.equals(shopCoupon.getCouponUserId(), chooseCoupon.getCouponUserId())) {
                continue;
            }
            for (ShopCartItemDto shopCartItem : shopCartItems) {
                if (Objects.nonNull(shopCartItem.getComboId()) && shopCartItem.getComboId() != 0) {
                    continue;
                }
                shopCartItem.setIsShareReduce(0);
                // 该商品是否在该优惠券可用
                boolean isCouponsProd = isCouponsProd(shopCartItem.getProdId(), prodIds, shopCoupon.getSuitableProdType());

                boolean isCouponsShop = isCouponsShop(shopCartItem.getProdId(), prodIds, shopCartItem.getShopId(), shopIds, shopCoupon.getSuitableProdType());

                if (isCouponsProd || isCouponsShop) {
                    shopCartItem.setIsShareReduce(1);
                    reduceNum++;
                }
            }
        }
        prodCanUseCouponActualTotal = chooseCouponProdAmount;
        if (chooseCoupon != null) {
            chooseCoupon.setChoose(true);
            // 计算优惠券优惠金额
            calculateCouponReduce();
            // 设置分摊优惠金额
            setShareReduce();
        }
        return this;
    }

    /**
     * 判断某个商品是否在此优惠券中
     *
     * @param prodId         在该店铺中的商品商品id
     * @param couponProdIds  优惠券关联的商品id
     * @param couponProdType 优惠券适用商品类型
     * @return 商品是否在此优惠券中
     */
    private boolean isCouponsProd(Long prodId, List<Long> couponProdIds, Integer couponProdType) {
        if (CouponProdType.ALL.value().equals(couponProdType)) {
            return true;
        }

        if (CouponProdType.PROD_IN.value().equals(couponProdType)) {
            if (CollectionUtil.isEmpty(couponProdIds)) {
                return false;
            }
            for (Long couponProdId : couponProdIds) {
                if (Objects.equals(couponProdId, prodId)) {
                    return true;
                }
            }
            return false;
        }

        if (CouponProdType.PROD_NO_IN.value().equals(couponProdType)) {
            if (CollectionUtil.isEmpty(couponProdIds)) {
                return true;
            }
            for (Long couponProdId : couponProdIds) {
                if (Objects.equals(couponProdId, prodId)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * 判断某个商品是否在此优惠券中
     *
     * @param shopId         该店铺id
     * @param prodId         在该店铺中的商品商品id
     * @param couponShopIds  优惠券关联的店铺id
     * @param couponProdIds  优惠券关联的商品id
     * @param couponProdType 优惠券适用商品类型
     * @return 商品是否在此优惠券中
     */
    private boolean isCouponsShop(Long prodId, List<Long> couponProdIds, Long shopId, List<Long> couponShopIds, Integer couponProdType) {
        if (CouponProdType.ALL.value().equals(couponProdType)) {
            return true;
        }

        if (CouponProdType.PROD_IN.value().equals(couponProdType)) {
            if (CollectionUtil.isEmpty(couponProdIds)) {
                return false;
            }
            for (Long couponProdId : couponProdIds) {
                if (Objects.equals(couponProdId, prodId)) {
                    return true;
                }
            }
            return false;
        }

        if (CouponProdType.PROD_NO_IN.value().equals(couponProdType)) {
            if (CollectionUtil.isEmpty(couponProdIds)) {
                return true;
            }
            for (Long couponProdId : couponProdIds) {
                if (Objects.equals(couponProdId, prodId)) {
                    return false;
                }
            }
            return true;
        }
        if (CouponProdType.SHOP_IN.value().equals(couponProdType)) {
            if (CollectionUtil.isEmpty(couponShopIds)) {
                return false;
            }
            for (Long couponShopId : couponShopIds) {
                if (Objects.equals(couponShopId, shopId)) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    /**
     * 计算优惠金额
     */
    private void calculateCouponReduce() {
        couponReduce = 0.0;
        couponInOrderAmount = 0.0;
        // 代金券
        if (CouponType.C2M.value().equals(chooseCoupon.getCouponType())) {
            couponInOrderAmount = chooseCoupon.getReduceAmount();
        }
        // 折扣券
        if (CouponType.C2D.value().equals(chooseCoupon.getCouponType())) {
            couponInOrderAmount = Arith.roundByBanker(Arith.sub(prodCanUseCouponActualTotal, Arith.div(Arith.mul(prodCanUseCouponActualTotal,
                    chooseCoupon.getCouponDiscount()), 10)), 2);
            chooseCoupon.setReduceAmount(couponInOrderAmount);
        }
        couponReduce = Arith.add(couponReduce, couponInOrderAmount);
    }

    /**
     * 设置分摊优惠金额
     */
    private void setShareReduce() {
        double sumReduce = 0.0;
        shopCartItems = shopCartItems.stream().sorted(Comparator.comparing(ProductItemDto::getActualTotal)).collect(Collectors.toList());
        int count = 0;
        for (ShopCartItemDto shopCartItem : shopCartItems) {
            if (Objects.nonNull(shopCartItem.getComboId()) && shopCartItem.getComboId() != 0) {
                continue;
            }
            double shareReduce;
            if (shopCartItem.getIsShareReduce() == 1) {
//                shareReduce = Arith.roundByBanker(Arith.mul(couponInOrderAmount, Arith.div(shopCartItem.getActualTotal(), prodCanUseCouponActualTotal, 10)), 2);
                shareReduce = Arith.div(Arith.mul(shopCartItem.getActualTotal(), couponInOrderAmount),prodCanUseCouponActualTotal, 2);
                count++;
                //如果是最后一项可以参与优惠的商品，直接将剩余的优惠金额赋值给他
                if (count >= reduceNum) {
                    shareReduce = Math.min(Arith.sub(couponReduce, sumReduce), shopCartItem.getProductTotalAmount());
                }
                if (Objects.equals(chooseCoupon.getShopId(), 0L)) {
                    // 平台分摊的每一个购物项优惠金额
                    shopCartItem.setPlatformShareReduce(Arith.add(shopCartItem.getPlatformShareReduce(), shareReduce));
                    shopCartItem.setPlatformCouponAmount(shareReduce);
                } else {
                    // 商家分摊的每一个购物项优惠金额
                    shopCartItem.setShareReduce(Arith.add(shopCartItem.getShareReduce(), shareReduce));
                    shopCartItem.setShopCouponAmount(shareReduce);
                }
            } else {
                shareReduce = 0.0;
            }
            // 分摊的优惠金额 不能大于商品金额
            double minShareReduce = Math.min(shareReduce, shopCartItem.getProductTotalAmount());
            shopCartItem.setActualTotal(Arith.sub(shopCartItem.getActualTotal(), minShareReduce));
            // 将对应店铺优惠券优惠的金额放入map中
            if (shopReduceMap.containsKey(shopCartItem.getShopId())) {
                double shopReduce = shopReduceMap.get(shopCartItem.getShopId());
                shopReduceMap.put(shopCartItem.getShopId(), Arith.add(shopReduce, minShareReduce));
            } else {
                shopReduceMap.put(shopCartItem.getShopId(), minShareReduce);
            }
            sumReduce = Arith.add(minShareReduce, sumReduce);
        }
    }
}
