/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.combo.api.listener;

import cn.hutool.core.collection.CollUtil;
import com.yami.shop.bean.app.dto.ShopCartItemDiscountDto;
import com.yami.shop.bean.app.dto.ShopCartItemDto;
import com.yami.shop.bean.app.dto.ShopCartOrderDto;
import com.yami.shop.bean.app.dto.ShopCartOrderMergerDto;
import com.yami.shop.bean.enums.OrderActivityType;
import com.yami.shop.bean.event.SubmitOrderActivityEvent;
import com.yami.shop.bean.order.SubmitOrderActivityOrder;
import com.yami.shop.combo.multishop.dao.ComboMapper;
import com.yami.shop.combo.multishop.dao.GiveawayMapper;
import com.yami.shop.common.config.Constant;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.util.Arith;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author lanhai
 * @date 2022/3/7 13:54
 */
@Component("CheckComboStatusListener")
@AllArgsConstructor
public class SubmitOrderActivityListener {

    private final ComboMapper comboMapper;
    private final GiveawayMapper giveawayMapper;

    @EventListener(SubmitOrderActivityEvent.class)
    @Order(SubmitOrderActivityOrder.COMBO)
    public void checkComboStatusListener(SubmitOrderActivityEvent event) {
        ShopCartOrderMergerDto orderMergerDto = event.getShopCartOrderMergerDto();
        List<ShopCartOrderDto> shopCartOrders = event.getShopCartOrderMergerDto().getShopCartOrders();
        Set<Long> comboIds = new HashSet<>();
        Set<Long> giveawayIds = new HashSet<>();
        //获取已经选择的所有套餐活动、赠品活动
        for (ShopCartOrderDto shopCartOrder : shopCartOrders) {
            List<ShopCartItemDiscountDto> shopCartItemDiscounts = shopCartOrder.getShopCartItemDiscounts();
            if (CollUtil.isNotEmpty(shopCartItemDiscounts)) {
                for (ShopCartItemDiscountDto shopCartItemDiscount : shopCartItemDiscounts) {
                    List<ShopCartItemDto> shopCartItems = shopCartItemDiscount.getShopCartItems();
                    for (ShopCartItemDto shopCartItem : shopCartItems) {
                        if (Objects.nonNull(shopCartItem.getGiveaway())) {
                            giveawayIds.add(shopCartItem.getGiveaway().getGiveawayId());
                        }
                    }
                    if (Objects.equals(shopCartItemDiscount.getActivityType(), OrderActivityType.COMBO.value())) {
                        comboIds.add(shopCartItemDiscount.getChooseComboItemDto().getComboId());
                    }
                }
            }
        }
        //校验数量
        if (CollUtil.isNotEmpty(comboIds)) {
            int num = comboMapper.countNormalCombo(comboIds);
            if (num < comboIds.size()) {
                //当前选择的套餐活动已经过期，请返回重新提交订单
                throw new YamiShopBindException("yami.order.combo.expire.check");
            }
        }
        if (CollUtil.isNotEmpty(giveawayIds)) {
            int count = giveawayMapper.countNormalGiveaway(giveawayIds);
            if (count < giveawayIds.size()) {
                //当前选择的赠品活动已经过期，请返回重新提交订单
                throw new YamiShopBindException("yami.order.giveaway.expire.check");
            }
        }
        // 拆分订单
        splitShopCartOrderDto(orderMergerDto);
    }

    private void splitShopCartOrderDto(ShopCartOrderMergerDto mergerOrder) {
        List<ShopCartOrderDto> shopCartOrderList = new ArrayList<>();
        for (ShopCartOrderDto shopCartOrder : mergerOrder.getShopCartOrders()) {
            Iterator<ShopCartItemDiscountDto> shopCartIterator = shopCartOrder.getShopCartItemDiscounts().iterator();
            boolean recalculate = false;
            int index = 0;
            while (shopCartIterator.hasNext()) {
                ShopCartItemDiscountDto shopCartItemDiscountDto = shopCartIterator.next();
                // 非套餐项不用处理
                if (!Objects.equals(shopCartItemDiscountDto.getActivityType(), OrderActivityType.COMBO.value())) {
                    continue;
                }
                shopCartItemDiscountDto.getChooseComboItemDto().setIndex(index);
                index++;
                // 如果套餐中没有虚拟商品，不用处理
                Iterator<ShopCartItemDto> iterator = shopCartItemDiscountDto.getShopCartItems().iterator();
                boolean isSplit = false;
                while (iterator.hasNext()) {
                    int size = shopCartItemDiscountDto.getShopCartItems().size();
                    ShopCartItemDto shopCartItemDto = iterator.next();
                    // 不是虚拟商品，不用处理
                    if (Objects.isNull(shopCartItemDto.getMold()) || shopCartItemDto.getMold() != 1) {
                        continue;
                    }
                    if (mergerOrder.getMold() == 0) {
                        mergerOrder.setMold(1);
                    }
                    // 套餐中的虚拟商品，但只有一个订单项
                    if (Objects.equals(size, 1)) {
                        continue;
                    }
                    ShopCartOrderDto shopCartOrderDto = new ShopCartOrderDto();
                    // 拆分虚拟商品
                    shopCartOrderDto.setShopId(shopCartOrder.getShopId());
                    shopCartOrderDto.setShopName(shopCartOrder.getShopName());
                    shopCartOrderDto.setRemarks(shopCartOrder.getRemarks());
                    shopCartOrderDto.setShopCityStatus(shopCartOrder.getShopCityStatus());
                    shopCartOrderDto.setStartDeliveryFee(shopCartOrderDto.getStartDeliveryFee());
                    shopCartOrderDto.setIsRefund(shopCartOrder.getIsRefund());
                    shopCartOrderDto.setOrderType(shopCartOrder.getOrderType());
                    shopCartOrderDto.setShopCartItemDiscounts(new ArrayList<>());
                    ShopCartItemDiscountDto shopCartItemDiscount = new ShopCartItemDiscountDto();
                    shopCartItemDiscount.setChooseComboItemDto(shopCartItemDiscountDto.getChooseComboItemDto());
                    shopCartItemDiscount.setShopCartItems(Collections.singletonList(shopCartItemDto));
                    shopCartOrderDto.getShopCartItemDiscounts().add(shopCartItemDiscount);
                    loadShopCartOrderDto(shopCartOrderDto);
                    shopCartOrderList.add(shopCartOrderDto);
                    // 移除该订单项
                    iterator.remove();
                    if (!isSplit) {
                        isSplit = true;
                    }
                    if (!recalculate) {
                        recalculate = true;
                    }
                }
                // 订单项没有拆分
                if (!isSplit) {
                    continue;
                }
            }
            if (!recalculate) {
                continue;
            }

            loadShopCartOrderDto(shopCartOrder);
        }
        if(CollUtil.isNotEmpty(shopCartOrderList)) {
            mergerOrder.getShopCartOrders().addAll(shopCartOrderList);
        }
    }

    private void loadShopCartOrderDto(ShopCartOrderDto shopCartOrderDto) {
        if(Objects.isNull(shopCartOrderDto.getFreeTransFee())) {
            shopCartOrderDto.setFreeTransFee(Constant.ZERO_DOUBLE);
        }
        if(Objects.isNull(shopCartOrderDto.getTransFee())) {
            shopCartOrderDto.setTransFee(Constant.ZERO_DOUBLE);
        }
        shopCartOrderDto.setUseScore(0L);
        shopCartOrderDto.setActualTotal(Arith.sub(shopCartOrderDto.getTransFee(), shopCartOrderDto.getFreeTransFee()));
        shopCartOrderDto.setTotal(Constant.ZERO_DOUBLE);
        shopCartOrderDto.setTotalCount(0);
        shopCartOrderDto.setShopComboAmount(Constant.ZERO_DOUBLE);
        shopCartOrderDto.setDiscountReduce(Constant.ZERO_DOUBLE);
        shopCartOrderDto.setCouponReduce(Constant.ZERO_DOUBLE);
        shopCartOrderDto.setPlatformCouponReduce(Constant.ZERO_DOUBLE);
        shopCartOrderDto.setScoreReduce(Constant.ZERO_DOUBLE);
        shopCartOrderDto.setPlatformAmount(Constant.ZERO_DOUBLE);
        shopCartOrderDto.setLevelReduce(Constant.ZERO_DOUBLE);
        shopCartOrderDto.setShopReduce(Constant.ZERO_DOUBLE);
        List<ShopCartItemDiscountDto> shopCartItemDiscounts = shopCartOrderDto.getShopCartItemDiscounts();
        for (ShopCartItemDiscountDto shopCartItemDiscount : shopCartItemDiscounts) {
            for (ShopCartItemDto cartItemDto : shopCartItemDiscount.getShopCartItems()) {
                shopCartOrderDto.setActualTotal(Arith.add(shopCartOrderDto.getActualTotal(), cartItemDto.getActualTotal()));
                shopCartOrderDto.setTotal(Arith.add(shopCartOrderDto.getTotal(), cartItemDto.getProductTotalAmount()));
                shopCartOrderDto.setTotalCount(shopCartOrderDto.getTotalCount() + cartItemDto.getProdCount());
                shopCartOrderDto.setShopReduce(Arith.add(shopCartOrderDto.getShopReduce(), cartItemDto.getShareReduce()));

                if(Objects.nonNull(cartItemDto.getDiscountAmount())) {
                    shopCartOrderDto.setDiscountReduce(Arith.add(shopCartOrderDto.getDiscountReduce(), cartItemDto.getDiscountAmount()));
                }
                if(Objects.nonNull(cartItemDto.getShopCouponAmount())) {
                    shopCartOrderDto.setCouponReduce(Arith.add(shopCartOrderDto.getCouponReduce(), cartItemDto.getShopCouponAmount()));
                }
                if(Objects.nonNull(cartItemDto.getPlatformCouponAmount())) {
                    shopCartOrderDto.setPlatformCouponReduce(Arith.add(shopCartOrderDto.getPlatformCouponReduce(), cartItemDto.getPlatformCouponAmount()));
                }
                if(Objects.nonNull(cartItemDto.getScorePayReduce())) {
                    shopCartOrderDto.setScoreReduce(Arith.add(shopCartOrderDto.getScoreReduce(), cartItemDto.getScorePayReduce()));
                }
                if(Objects.nonNull(cartItemDto.getPlatformShareReduce())) {
                    shopCartOrderDto.setPlatformAmount(Arith.add(shopCartOrderDto.getPlatformAmount(), cartItemDto.getPlatformShareReduce()));
                }
                if(Objects.nonNull(cartItemDto.getLevelReduce())) {
                    shopCartOrderDto.setLevelReduce(Arith.add(shopCartOrderDto.getLevelReduce(), cartItemDto.getLevelReduce()));
                }
                if(Objects.nonNull(cartItemDto.getScorePrice())) {
                    shopCartOrderDto.setUseScore(shopCartOrderDto.getUseScore() + cartItemDto.getScorePrice());
                }
                if(Objects.nonNull(cartItemDto.getComboAmount())) {
                    shopCartOrderDto.setShopComboAmount(shopCartOrderDto.getShopComboAmount() + cartItemDto.getComboAmount());
                }
            }
        }
    }
}
