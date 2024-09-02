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

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.yami.shop.bean.app.dto.ChooseComboItemDto;
import com.yami.shop.bean.app.dto.ShopCartItemDiscountDto;
import com.yami.shop.bean.app.dto.ShopCartItemDto;
import com.yami.shop.bean.app.dto.ShopCartOrderDto;
import com.yami.shop.bean.enums.OrderActivityType;
import com.yami.shop.bean.event.SubmitOrderEvent;
import com.yami.shop.bean.order.SubmitOrderOrder;
import com.yami.shop.combo.multishop.model.ComboOrder;
import com.yami.shop.combo.multishop.service.ComboOrderService;
import com.yami.shop.common.config.Constant;
import com.yami.shop.common.util.Arith;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 套餐、赠品
 * @author lanhai
 */
@Component("comboSubmitOrderListener")
@AllArgsConstructor
public class SubmitOrderListener {

    private ComboOrderService comboOrderService;

    /**
     * 套餐赠品
     */
    @EventListener(SubmitOrderEvent.class)
    @Order(SubmitOrderOrder.COMBO)
    public void comboSubmitOrderListener(SubmitOrderEvent event) {
        String userId = event.getMergerOrder().getUserId();
        Map<Integer, ComboOrder> map = new HashMap<>(10);
        Date date = new Date();
        for (ShopCartOrderDto shopCartOrder : event.getMergerOrder().getShopCartOrders()) {
            for (ShopCartItemDiscountDto shopCartItemDiscount : shopCartOrder.getShopCartItemDiscounts()) {
                if (!Objects.equals(shopCartItemDiscount.getActivityType(), OrderActivityType.COMBO.value())) {
                    continue;
                }
                ChooseComboItemDto chooseComboItemDto = shopCartItemDiscount.getChooseComboItemDto();
                Integer index = chooseComboItemDto.getIndex();
                ComboOrder comboOrder = map.get(index);
                if (Objects.isNull(comboOrder)) {
                    comboOrder = new ComboOrder();
                    comboOrder.setShopId(shopCartOrder.getShopId());
                    comboOrder.setComboId(shopCartItemDiscount.getChooseComboItemDto().getComboId());
                    comboOrder.setUserId(userId);
                    comboOrder.setComboNum(chooseComboItemDto.getComboCount());
                    comboOrder.setPayPrice(Constant.ZERO_DOUBLE);
                    comboOrder.setCreateTime(date);
                    comboOrder.setUpdateTime(date);
                    comboOrder.setStatus(0);
                    comboOrder.setOrderNumber("");
                    map.put(index, comboOrder);
                }
                comboOrder.setOrderNumber(shopCartOrder.getOrderNumber() + Constant.COMMA + comboOrder.getOrderNumber());
                for (ShopCartItemDto shopCartItem : shopCartItemDiscount.getShopCartItems()) {
                    comboOrder.setPayPrice(Arith.add(comboOrder.getPayPrice(), shopCartItem.getActualTotal()));
                }
            }
        }
        if (MapUtil.isEmpty(map)) {
            return;
        }
        for (ComboOrder comboOrder : map.values()) {
            comboOrder.setOrderNumber(StrUtil.sub(comboOrder.getOrderNumber(), 0, -1));
        }
        comboOrderService.saveBatch(map.values());
    }
}
