/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.api.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.yami.shop.bean.app.dto.ShopCartItemDiscountDto;
import com.yami.shop.bean.app.dto.ShopCartItemDto;
import com.yami.shop.bean.app.dto.ShopCartOrderDto;
import com.yami.shop.bean.app.dto.ShopCartOrderMergerDto;
import com.yami.shop.bean.enums.DeliveryType;
import com.yami.shop.bean.enums.OrderInvoiceState;
import com.yami.shop.bean.enums.OrderStatus;
import com.yami.shop.bean.event.SubmitOrderEvent;
import com.yami.shop.bean.model.*;
import com.yami.shop.bean.order.SubmitOrderOrder;
import com.yami.shop.bean.vo.VirtualRemarkVO;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.i18n.I18nMessage;
import com.yami.shop.common.i18n.LanguageEnum;
import com.yami.shop.common.util.Json;
import com.yami.shop.common.util.WriteOffCodeUtil;
import com.yami.shop.dao.OrderSettlementMapper;
import com.yami.shop.service.*;
import lombok.AllArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 确认订单信息时的默认操作
 *
 * @author LGH
 */
@Component("defaultSubmitOrderListener")
@AllArgsConstructor
public class SubmitOrderListener {


    private final ProductService productService;

    private final SkuService skuService;

    private final OrderSettlementMapper orderSettlementMapper;

    private final OrderVirtualInfoService orderVirtualInfoService;

    private final BasketService basketService;

    private final OrderInvoiceService orderInvoiceService;

    /**
     * 计算订单金额
     */
    @EventListener(SubmitOrderEvent.class)
    @Order(SubmitOrderOrder.DEFAULT)
    public void defaultSubmitOrderListener(SubmitOrderEvent event) {
        ShopCartOrderMergerDto mergerOrder = event.getMergerOrder();
        String userId = mergerOrder.getUserId();
        // 订单商品参数
        List<ShopCartOrderDto> shopCartOrders = mergerOrder.getShopCartOrders();

        List<Long> basketIds = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(shopCartOrders)) {
            // 每个店铺生成一个订单
            for (ShopCartOrderDto shopCartOrderDto : shopCartOrders) {
                buildOrder(event, mergerOrder, basketIds, shopCartOrderDto);
            }
        }

        // 删除购物车的商品信息
        if (!basketIds.isEmpty()) {
            basketService.deleteShopCartItemsByBasketIds(userId, basketIds);
        }
    }

    private void buildOrder(SubmitOrderEvent event, ShopCartOrderMergerDto mergerOrder, List<Long> basketIds, ShopCartOrderDto shopCartOrderDto) {
        Date now = new Date();
        String userId = mergerOrder.getUserId();
        String orderNumber = shopCartOrderDto.getOrderNumber();
        shopCartOrderDto.setOrderNumber(orderNumber);
        Long shopId = shopCartOrderDto.getShopId();
        // 订单商品名称
        StringBuilder orderProdName = new StringBuilder(100);

        // 订单商品中文名称
        StringBuilder orderProdNameCn = new StringBuilder(100);

        // 订单商品英文名称
        StringBuilder orderProdNameEn = new StringBuilder(100);

        List<OrderItem> orderItems = new ArrayList<>();

        // 构建订单项并获取预售时间
        buildOrderItem(basketIds, shopCartOrderDto, orderNumber, shopId,
                orderProdName, orderProdNameCn, orderProdNameEn, orderItems,userId);

        //中英文
        orderProdName = new StringBuilder(orderProdName.subSequence(0, Math.min(orderProdName.length() - 1, 100)));
        orderProdNameCn = new StringBuilder(orderProdNameCn.subSequence(0, Math.min(orderProdNameCn.length() - 1, 100)));
        orderProdNameEn = new StringBuilder(orderProdNameEn.subSequence(0, Math.min(orderProdNameEn.length() - 1, 100)));

        if (orderProdName.lastIndexOf(StrUtil.COMMA) == orderProdName.length() - 1) {
            orderProdName.deleteCharAt(orderProdName.length() - 1);
        }
        if (orderProdNameCn.lastIndexOf(StrUtil.COMMA) == orderProdNameCn.length() - 1) {
            orderProdNameCn.deleteCharAt(orderProdNameCn.length() - 1);
        }
        if (orderProdNameEn.lastIndexOf(StrUtil.COMMA) == orderProdNameEn.length() - 1) {
            orderProdNameEn.deleteCharAt(orderProdNameEn.length() - 1);
        }

        // 订单信息
        com.yami.shop.bean.model.Order order = new com.yami.shop.bean.model.Order();

        order.setShopId(shopId);
        order.setOrderNumber(orderNumber);
        order.setProductNums(shopCartOrderDto.getTotalCount());
        // 订单商品名称
        order.setProdName(orderProdName.toString());
        order.setProdNameCn(orderProdNameCn.toString());
        order.setProdNameEn(orderProdNameEn.toString());
        // 用户id
        order.setUserId(userId);
        // 商品总额
        order.setTotal(shopCartOrderDto.getTotal());
        // 实际总额
        order.setActualTotal(shopCartOrderDto.getActualTotal());
        order.setStatus(OrderStatus.UNPAY.value());
        order.setUpdateTime(now);
        order.setCreateTime(now);
        order.setIsPayed(0);
        order.setDeleteStatus(0);
        order.setReduceAmount(shopCartOrderDto.getShopReduce());
        order.setFreightAmount(shopCartOrderDto.getTransFee());
        order.setRemarks(shopCartOrderDto.getRemarks());
        order.setOrderType(mergerOrder.getOrderType().value());
        order.setPlatformAmount(shopCartOrderDto.getPlatformAmount());
        order.setOrderItems(orderItems);
        order.setScore(shopCartOrderDto.getUseScore());
        order.setDvyType(mergerOrder.getDvyType());
        order.setDvyId(mergerOrder.getStationId());
        order.setPlatformFreeFreightAmount(shopCartOrderDto.getPlatformFreeFreightAmount());
        order.setFreeTransfee(-shopCartOrderDto.getFreeTransFee());
        // 平台佣金
        order.setPlatformCommission(shopCartOrderDto.getPlatformCommission());
        // 预售
        order.setPreSaleTime(getPreSellTime(shopCartOrderDto.getShopCartItemDiscounts()));
        order.setChangeAmountVersion(0);
        order.setDiscountAmount(shopCartOrderDto.getDiscountReduce());
        order.setShopCouponAmount(shopCartOrderDto.getCouponReduce());
        order.setPlatformCouponAmount(shopCartOrderDto.getPlatformCouponReduce());
        order.setMemberAmount(shopCartOrderDto.getLevelReduce());
        order.setScoreAmount(shopCartOrderDto.getScoreReduce());
        order.setIsSettled(0);
        order.setShopComboAmount(shopCartOrderDto.getShopComboAmount());

        // 处理下虚拟商品订单
        handlerVirtualProdOder(mergerOrder, shopCartOrderDto, order);

        event.getOrders().add(order);
        // 插入订单结算表
        insertOrderSettlement(mergerOrder, shopCartOrderDto, now, userId, orderNumber, order);
        //发票信息
        insertOrderInvoice(mergerOrder, orderNumber, shopId);
    }

    private void insertOrderInvoice(ShopCartOrderMergerDto mergerOrder, String orderNumber, Long shopId) {
        if (CollectionUtils.isNotEmpty(mergerOrder.getOrderInvoiceList())){
            for (OrderInvoice orderInvoice : mergerOrder.getOrderInvoiceList()) {
                if (Objects.equals(shopId, orderInvoice.getShopId())) {
                    orderInvoice.setInvoiceState(OrderInvoiceState.APPLICATION.value());
                    orderInvoice.setApplicationTime(new Date());
                    orderInvoice.setOrderNumber(orderNumber);
                    orderInvoice.setShopId(shopId);
                    orderInvoiceService.save(orderInvoice);
                }
            }
        }
    }

    private void insertOrderSettlement(ShopCartOrderMergerDto mergerOrder, ShopCartOrderDto shopCartOrderDto, Date now, String userId, String orderNumber, com.yami.shop.bean.model.Order order) {
        OrderSettlement orderSettlement = new OrderSettlement();
        orderSettlement.setUserId(userId);
        orderSettlement.setCreateTime(now);
        orderSettlement.setOrderNumber(orderNumber);
        orderSettlement.setPayAmount(order.getActualTotal());
        orderSettlement.setPayStatus(0);
        orderSettlement.setVersion(0);
        orderSettlement.setPayScore(0L);
        //如果用使用积分，结算表将积分价格插入
        if(mergerOrder.getIsScorePay() != null && mergerOrder.getIsScorePay() == 1){
            orderSettlement.setPayScore(shopCartOrderDto.getUseScore());
        }
        orderSettlementMapper.insert(orderSettlement);
    }

    /**
     * 处理下虚拟商品操作
     * @param mergerOrder 聚合订单信息
     * @param shopCartOrderDto 店铺订单信息
     * @param order 订单信息
     */
    private void handlerVirtualProdOder(ShopCartOrderMergerDto mergerOrder, ShopCartOrderDto shopCartOrderDto, com.yami.shop.bean.model.Order order) {
        OrderItem orderItem = order.getOrderItems().get(0);
        order.setOrderMold(orderItem.getMold());
        order.setIsRefund(shopCartOrderDto.getIsRefund());
        Map<Long, List<VirtualRemarkVO>> map = new HashMap<>(8);
        Long prodId = shopCartOrderDto.getShopCartItemDiscounts().get(0).getShopCartItems().get(0).getProdId();
        // 虚拟商品备注信息列表
        if(CollUtil.isNotEmpty(mergerOrder.getVirtualRemarkList())) {
            for (VirtualRemarkVO virtualRemarkVO : mergerOrder.getVirtualRemarkList()) {
                if (Objects.isNull(virtualRemarkVO.getProdId())) {
                    virtualRemarkVO.setProdId(prodId);
                }
            }
            map = mergerOrder.getVirtualRemarkList().stream().collect(Collectors.groupingBy(VirtualRemarkVO::getProdId));
        }
        if(Objects.equals(order.getOrderMold(),1)){
            // 存入留言
            String virtualRemarkStr = Json.toJsonString(map.get(prodId));
            order.setVirtualRemark(virtualRemarkStr);
            // 虚拟商品默认无需快递
            order.setDvyType(DeliveryType.NO_EXPRESS.getValue());
            // 存入核销信息
            order.setWriteOffStart(shopCartOrderDto.getWriteOffStart());
            order.setWriteOffEnd(shopCartOrderDto.getWriteOffEnd());
            if(Objects.nonNull(shopCartOrderDto.getWriteOffEnd()) && DateUtil.compare(shopCartOrderDto.getWriteOffEnd(),new Date()) < 0){
                throw new YamiShopBindException("yami.order.coupon.expired");
            }
            order.setWriteOffNum(shopCartOrderDto.getWriteOffNum());
            order.setWriteOffMultipleCount(shopCartOrderDto.getWriteOffMultipleCount());
            order.setIsRefund(shopCartOrderDto.getIsRefund());
            order.setWriteOffStatus(0);
            if(Objects.equals(order.getWriteOffNum(),0)){
                return;
            }
            // 获取最新的券码
            String code = orderVirtualInfoService.getNewCodeInfo(order.getShopId());
            List<OrderVirtualInfo> orderVirtualInfos = new ArrayList<>();
            for (int i = 0; i < order.getProductNums(); i++) {
                code = WriteOffCodeUtil.getCode(code);
                OrderVirtualInfo orderVirtualInfo = new OrderVirtualInfo();
                orderVirtualInfo.setOrderNumber(order.getOrderNumber());
                orderVirtualInfo.setShopId(order.getShopId());
                orderVirtualInfo.setIsWriteOff(0);
                orderVirtualInfo.setWriteOffMultipleCount(order.getWriteOffMultipleCount());
                orderVirtualInfo.setWriteOffCode(code);
                orderVirtualInfo.setCreateTime(new Date());
                orderVirtualInfos.add(orderVirtualInfo);
            }
            orderVirtualInfoService.saveBatch(orderVirtualInfos);
        }
    }


    private Date getPreSellTime(List<ShopCartItemDiscountDto> shopCartItemDiscounts) {
        for (ShopCartItemDiscountDto shopCartItemDiscount : shopCartItemDiscounts) {
            List<ShopCartItemDto> shopCartItems = shopCartItemDiscount.getShopCartItems();
            for (ShopCartItemDto shopCartItem : shopCartItems) {
                // 是否预售商品
                if(Objects.nonNull(shopCartItem.getPreSellStatus()) &&Objects.equals(shopCartItem.getPreSellStatus(),1)){
                    return shopCartItem.getPreSellTime();
                }
            }
        }
        return null;
    }


    private void buildOrderItem(List<Long> basketIds, ShopCartOrderDto shopCartOrderDto, String orderNumber, Long shopId, StringBuilder orderProdName, StringBuilder orderProdNameCn, StringBuilder orderProdNameEn, List<OrderItem> orderItems, String userId) {
        Date now = new Date();

        List<ShopCartItemDiscountDto> shopCartItemDiscounts = shopCartOrderDto.getShopCartItemDiscounts();
        for (ShopCartItemDiscountDto shopCartItemDiscount : shopCartItemDiscounts) {
            List<ShopCartItemDto> shopCartItems = shopCartItemDiscount.getShopCartItems();
            for (ShopCartItemDto shopCartItem : shopCartItems) {
                loadOrderItem(basketIds, shopCartOrderDto, orderNumber, shopId, orderProdName, orderProdNameCn, orderProdNameEn, orderItems, now, userId, shopCartItem);
            }

        }
    }

    private void loadOrderItem(List<Long> basketIds, ShopCartOrderDto shopCartOrderDto, String orderNumber, Long shopId, StringBuilder orderProdName, StringBuilder orderProdNameCn, StringBuilder orderProdNameEn, List<OrderItem> orderItems, Date now, String userId, ShopCartItemDto shopCartItem) {
        Integer lang = I18nMessage.getDbLang();
        OrderItem orderItem = new OrderItem();
        Sku sku = skuService.getSkuListBySkuId(shopCartItem.getSkuId());
        Product product = productService.getProductByProdId(shopCartItem.getProdId(), I18nMessage.getDbLang());
        shopCartOrderDto.setWriteOffNum(product.getWriteOffNum());
        shopCartOrderDto.setIsRefund(product.getIsRefund());
        // 如果是虚拟商品且需要核销，放进去
        if(Objects.equals(product.getMold(), 1) && !Objects.equals(product.getWriteOffNum(),0)){
            Date startTime = new Date();
            Date endTime;
            // 判断有效期
            switch (product.getWriteOffTime()){
                case -1:
                    endTime = null;
                    break;
                case 0:
                    startTime = product.getWriteOffStart();
                    endTime = product.getWriteOffEnd();
                    break;
                case 1:
                    endTime = DateUtil.endOfDay(startTime);
                    break;
                default:
                    endTime = DateUtil.offsetDay(startTime,product.getWriteOffTime());
                    break;
            }
            shopCartOrderDto.setWriteOffStart(startTime);
            shopCartOrderDto.setWriteOffEnd(endTime);
            if (Objects.equals(product.getWriteOffNum(), -1)) {
                shopCartOrderDto.setWriteOffMultipleCount(product.getWriteOffMultipleCount());
            }
            orderItem.setStatus(0);
        }
        orderItem.setShopId(shopId);
        orderItem.setCategoryId(product.getCategoryId());
        orderItem.setOrderNumber(orderNumber);

        // sku信息
        orderItem.setProdId(sku.getProdId());
        orderItem.setSkuId(sku.getSkuId());
        for (SkuLang skuLang : sku.getSkuLangList()) {
            if (skuLang.getLang() == 0){
                orderItem.setSkuNameCn(skuLang.getSkuName());
            } else if(skuLang.getLang() == 1){
                orderItem.setSkuNameEn(skuLang.getSkuName());
            }
        }
        orderItem.setSkuName(sku.getSkuName());
        orderItem.setProdName(product.getProdName());

        orderItem.setPic(StrUtil.isBlank(sku.getPic()) ? product.getPic() : sku.getPic());
        orderItem.setProdCount(shopCartItem.getProdCount());
        // 保存中英文名称
        orderItem.setProdNameCn(product.getProdNameCn());
        orderItem.setProdNameEn(product.getProdNameEn());
        orderItem.setPrice(shopCartItem.getPrice());
        orderItem.setUserId(userId);
        orderItem.setProductTotalAmount(shopCartItem.getProductTotalAmount());
        orderItem.setRecTime(now);
        orderItem.setCommSts(0);
        orderItem.setBasketDate(shopCartItem.getBasketDate());
        //平台的补贴优惠金额
        orderItem.setPlatformShareReduce(shopCartItem.getPlatformShareReduce());
        // 实际订单项支付金额
        orderItem.setActualTotal(shopCartItem.getActualTotal());
        // 分摊优惠金额
        orderItem.setShareReduce(shopCartItem.getShareReduce());
        orderItem.setRate(shopCartItem.getRate());
        // 平台佣金
        orderItem.setPlatformCommission(shopCartItem.getPlatformCommission());
        if (Objects.equals(lang, LanguageEnum.LANGUAGE_EN.getLang())) {
            orderProdName.append(orderItem.getProdNameEn()).append(StrUtil.COMMA);
        } else {
            orderProdName.append(orderItem.getProdNameCn()).append(StrUtil.COMMA);
        }
        orderProdNameCn.append(orderItem.getProdNameCn()).append(StrUtil.COMMA);
        orderProdNameEn.append(orderItem.getProdNameEn()).append(StrUtil.COMMA);
        //推广员卡号
        orderItem.setDistributionCardNo(shopCartItem.getDistributionCardNo());
        orderItem.setDiscountAmount(shopCartItem.getDiscountAmount());
        orderItem.setShopCouponAmount(shopCartItem.getShopCouponAmount());
        orderItem.setPlatformCouponAmount(shopCartItem.getPlatformCouponAmount());
        orderItem.setMemberAmount(shopCartItem.getLevelReduce());
        orderItem.setScoreAmount(shopCartItem.getScorePayReduce());
        orderItem.setComboAmount(shopCartItem.getComboAmount());
        //使用积分价格
        orderItem.setUseScore(shopCartItem.getScorePrice());
        orderItem.setChangeAmountVersion(0);
        orderItem.setMold(Objects.isNull(product.getMold()) ? 0 : product.getMold());
        orderItem.setGiveaway(shopCartItem.getGiveaway());
        orderItems.add(orderItem);
        if (shopCartItem.getBasketId() != null && !Objects.equals(shopCartItem.getBasketId(), 0L) && !Objects.equals(shopCartItem.getBasketId(), -1L)) {
            basketIds.add(shopCartItem.getBasketId());
        }
    }

}
