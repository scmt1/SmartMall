/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.combo.multishop.listener;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.yami.shop.bean.event.PaySuccessOrderEvent;
import com.yami.shop.bean.order.PaySuccessOrderOrder;
import com.yami.shop.combo.multishop.model.ComboOrder;
import com.yami.shop.combo.multishop.service.ComboOrderService;
import com.yami.shop.combo.multishop.service.ComboService;
import com.yami.shop.common.config.Constant;
import lombok.AllArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 优惠券
 * @author lanhai
 */
@Component("comboPaySuccessListener")
@AllArgsConstructor
public class PaySuccessOrderListener {

    private final ComboService comboService;
    private final ComboOrderService comboOrderService;

    /**
     * 更新优惠券使用记录状态
     */
    @EventListener(PaySuccessOrderEvent.class)
    @Order(PaySuccessOrderOrder.COMBO)
    public void comboPaySuccessListener(PaySuccessOrderEvent event) {
        if(Objects.equals(event.getOrders().get(0).getShopId(),Constant.PLATFORM_SHOP_ID)){
            return;
        }
        List<String> orderNumberList = Lists.newArrayList();
        List<com.yami.shop.bean.model.Order> orders = event.getOrders();

        for (com.yami.shop.bean.model.Order order : orders) {
            orderNumberList.add(order.getOrderNumber());
        }
        if (CollectionUtils.isEmpty(orderNumberList)) {
            return;
        }
        List<ComboOrder> comboOrders = comboOrderService.listByOrderNumberList(orderNumberList);
        if (CollUtil.isEmpty(comboOrders)) {
            return;
        }
        Date date = new Date();
        Set<Long> comboIds = new HashSet<>();
        for (ComboOrder comboOrder : comboOrders) {
            comboIds.add(comboOrder.getComboId());
            // 已支付
            comboOrder.setStatus(1);
            comboOrder.setUpdateTime(date);
        }
        comboOrderService.updateBatchById(comboOrders);
        // 更新套餐销量
        comboService.updateSoldNum(comboIds);
    }
}
