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
import cn.hutool.core.util.StrUtil;
import com.yami.shop.bean.event.SubmitOrderGiveawayEvent;
import com.yami.shop.bean.model.OrderItem;
import com.yami.shop.bean.model.Product;
import com.yami.shop.bean.model.Sku;
import com.yami.shop.bean.order.SubmitOrderOrder;
import com.yami.shop.bean.vo.GiveawayProdVO;
import com.yami.shop.bean.vo.GiveawayVO;
import com.yami.shop.common.config.Constant;
import com.yami.shop.common.i18n.I18nMessage;
import com.yami.shop.common.util.Arith;
import com.yami.shop.service.OrderItemService;
import com.yami.shop.service.OrderService;
import com.yami.shop.service.ProductService;
import com.yami.shop.service.SkuService;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 优惠券提交订单监听
 * @author lanhai
 */
@Component("comboSubmitOrderGiveawayListener")
@AllArgsConstructor
public class SubmitOrderGiveawayListener {

    private SkuService skuService;
    private ProductService productService;
    private OrderService orderService;
    private OrderItemService orderItemService;

    /**
     * 套餐赠品
     */
    @EventListener(SubmitOrderGiveawayEvent.class)
    @Order(SubmitOrderOrder.COMBO)
    public void comboSubmitOrderGiveawayListener(SubmitOrderGiveawayEvent event) {

        List<com.yami.shop.bean.model.Order> orders = event.getOrders();
        Date now = new Date();
        List<OrderItem> orderItems = new ArrayList<>();
        List<OrderItem> updateList = new ArrayList<>();
        List<com.yami.shop.bean.model.Order> orderList = new ArrayList<>();
        for (com.yami.shop.bean.model.Order order : orders) {
            loadOrder(now, orderItems, updateList, orderList, order);
        }

        orderService.updateBatchById(orderList);

        if (CollUtil.isNotEmpty(updateList)) {
            orderItemService.updateBatchById(updateList);
        }

        if (CollUtil.isNotEmpty(orderItems)) {
            orderItemService.insertBatchOrderItem(orderItems);
        }
        Map<String, List<OrderItem>> itemMap = orderItems.stream().collect(Collectors.groupingBy(OrderItem::getOrderNumber));
        Iterator<com.yami.shop.bean.model.Order> iterator = orderList.iterator();
        while (iterator.hasNext()) {
            com.yami.shop.bean.model.Order order = iterator.next();
            List<OrderItem> orderItemList = itemMap.get(order.getOrderNumber());
            if (CollUtil.isEmpty(orderItemList)) {
                iterator.remove();
                continue;
            }
            order.setOrderItems(orderItemList);
        }
    }

    private void loadOrder(Date now , List<OrderItem> orderItems, List<OrderItem> updateList, List<com.yami.shop.bean.model.Order> orderList, com.yami.shop.bean.model.Order order) {
        int productNums = order.getProductNums();
        for (OrderItem mainOrderItem : order.getOrderItems()) {
            GiveawayVO giveaway = mainOrderItem.getGiveaway();
            if (Objects.isNull(giveaway)) {
                continue;
            }
            for (GiveawayProdVO giveawayProd : giveaway.getGiveawayProds()) {
                Product product = productService.getProductByProdId(giveawayProd.getProdId(), I18nMessage.getDbLang());
                OrderItem orderItem = new OrderItem();
                orderItem.setShopId(product.getShopId());
                orderItem.setCategoryId(product.getCategoryId());
                orderItem.setOrderNumber(order.getOrderNumber());
                Sku sku = skuService.getSkuBySkuId(giveawayProd.getSkuId(),I18nMessage.getDbLang());
                orderItem.setProdId(sku.getProdId());
                orderItem.setSkuId(sku.getSkuId());
                orderItem.setSkuNameCn(sku.getSkuName());
                orderItem.setSkuNameEn(sku.getSkuNameEn());
                orderItem.setSkuName(sku.getSkuName());
                orderItem.setPic(StrUtil.isBlank(sku.getPic()) ? product.getPic() : sku.getPic());
                // 保存中英文名称
                orderItem.setProdName(product.getProdName());
                orderItem.setProdNameCn(product.getProdNameCn());
                orderItem.setProdNameEn(product.getProdNameEn());
                int prodCount = giveawayProd.getGiveawayNum();
                orderItem.setProdCount(prodCount);
                productNums = productNums + orderItem.getProdCount();
                orderItem.setPrice(product.getPrice());
                orderItem.setUserId(mainOrderItem.getUserId());
                orderItem.setProductTotalAmount(product.getPrice());
                orderItem.setRecTime(now);
                orderItem.setCommSts(0);
                //平台的补贴优惠金额
                orderItem.setPlatformShareReduce(Constant.ZERO_DOUBLE);
                // 实际订单项支付金额
                orderItem.setActualTotal(Constant.ZERO_DOUBLE);
                // 分摊优惠金额
                orderItem.setShareReduce(Constant.ZERO_DOUBLE);
                orderItem.setDiscountAmount(Constant.ZERO_DOUBLE);
                orderItem.setShopCouponAmount(Constant.ZERO_DOUBLE);
                orderItem.setPlatformCouponAmount(Constant.ZERO_DOUBLE);
                orderItem.setMemberAmount(Constant.ZERO_DOUBLE);
                orderItem.setScoreAmount(Constant.ZERO_DOUBLE);
                //使用积分价格
                orderItem.setUseScore(0L);
                // 赠品
                orderItem.setGiveawayAmount(Arith.mul(giveawayProd.getRefundPrice(), prodCount));
                orderItem.setGiveawayOrderItemId(mainOrderItem.getOrderItemId());
                orderItems.add(orderItem);

                OrderItem orderItemNew = new OrderItem();
                orderItemNew.setOrderItemId(mainOrderItem.getOrderItemId());
                orderItemNew.setIsGiveaway(1);
                updateList.add(orderItemNew);
            }
        }
        com.yami.shop.bean.model.Order orderNew = new com.yami.shop.bean.model.Order();
        orderNew.setOrderId(order.getOrderId());
        orderNew.setProductNums(productNums);
        orderNew.setShopId(order.getShopId());
        orderNew.setOrderNumber(order.getOrderNumber());
        orderList.add(orderNew);
    }
}
