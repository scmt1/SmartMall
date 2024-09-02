/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.api.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.google.common.collect.Lists;
import com.yami.shop.bean.app.dto.*;
import com.yami.shop.bean.app.param.OrderParam;
import com.yami.shop.bean.app.param.OrderPayInfoParam;
import com.yami.shop.bean.app.param.SubmitOrderParam;
import com.yami.shop.bean.enums.DvyType;
import com.yami.shop.bean.enums.EsOperationType;
import com.yami.shop.bean.enums.OrderStatus;
import com.yami.shop.bean.enums.OrderType;
import com.yami.shop.bean.event.EsProductUpdateEvent;
import com.yami.shop.bean.event.SubmitSeckillOrderEvent;
import com.yami.shop.bean.model.*;
import com.yami.shop.bean.param.ChooseCouponParam;
import com.yami.shop.bean.param.PlatformChooseCouponParam;
import com.yami.shop.bean.vo.UserDeliveryInfoVO;
import com.yami.shop.common.response.ResponseEnum;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.common.util.Arith;
import com.yami.shop.delivery.api.manager.DeliveryOrderManager;
import com.yami.shop.manager.*;
import com.yami.shop.manager.impl.ConfirmOrderManager;
import com.yami.shop.manager.impl.ShopCartAdapter;
import com.yami.shop.manager.impl.ShopCartItemAdapter;
import com.yami.shop.security.api.util.SecurityUtils;
import com.yami.shop.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author LGH
 */
@RestController
@RequestMapping("/p/order")
@Api(tags = "订单接口")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private SubmitOrderManager submitOrderManager;
    @Autowired
    private SkuService skuService;
    @Autowired
    private ProductService productService;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private UserAddrOrderService userAddrOrderService;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    private ShopCartAdapter shopCartAdapter;
    @Autowired
    private ShopCartItemAdapter shopCartItemAdapter;
    @Autowired
    private ConfirmOrderManager confirmOrderManager;
    @Autowired
    private ThreadPoolExecutor orderThreadPoolExecutor;
    @Autowired
    private DeliveryOrderManager deliveryOrderManager;
    @Autowired
    private DiscountShopCartManager discountShopCartManager;
    @Autowired
    private ComboShopCartManager comboShopCartManager;
    @Autowired
    private CouponConfirmOrderManager couponConfirmOrderManager;
    @Autowired
    private UserLevelOrderManager userLevelOrderManager;
    @Autowired
    private OrderUseScoreManager orderUseScoreManager;
    @Autowired
    private ShopDetailService shopDetailService;
    @Autowired
    private RoomsService roomsService;
    @Autowired
    private RoomsRecordService roomsRecordService;


    @PostMapping("/confirm")
    @ApiOperation(value = "结算，生成普通订单信息", notes = "传入下单所需要的参数进行下单")
    public ServerResponseEntity<ShopCartOrderMergerDto> confirm(@Valid @RequestBody OrderParam orderParam) throws ExecutionException, InterruptedException {
        String userId = SecurityUtils.getUser().getUserId();
        // 将要返回给前端的完整的订单信息
        ShopCartOrderMergerDto shopCartOrderMerger = new ShopCartOrderMergerDto();
        shopCartOrderMerger.setIsScorePay(orderParam.getIsScorePay());
        shopCartOrderMerger.setDvyType(orderParam.getDvyType());
        shopCartOrderMerger.setUsableScore(orderParam.getUserUseScore());
        shopCartOrderMerger.setOrderType(OrderType.ORDINARY);
        // 组装获取用户提交的购物车商品项
        List<ShopCartItemDto> shopCartItemsDb = shopCartItemAdapter.getShopCartItemsByOrderItems(orderParam.getOrderItem(), userId,orderParam.getIsMall());
        // 筛选过滤掉不同配送的商品
        List<ShopCartItemDto> shopCartItems = confirmOrderManager.filterShopItemsByType(shopCartOrderMerger, shopCartItemsDb);
        // 该商品不满足任何的配送方式
        if (CollectionUtil.isEmpty(shopCartItems)) {
            return ServerResponseEntity.fail(ResponseEnum.ORDER_DELIVERY_NOT_SUPPORTED, shopCartOrderMerger);
        }
        ShopCartItemDto firstShopCartItem = shopCartItems.get(0);
        // 是否为预售订单
        orderParam.setPreSellStatus(firstShopCartItem.getPreSellStatus());
        shopCartOrderMerger.setPreSellStatus(firstShopCartItem.getPreSellStatus());
        // 商品类别 0.实物商品 1. 虚拟商品
        int mold = 0;
        if (shopCartItems.stream().filter(shopCartItemDto -> shopCartItemDto.getMold() == 1).count() == shopCartItems.size()) {
            // 订单项中的所有商品都为虚拟商品时，才是虚拟订单
            mold = 1;
        }
        shopCartOrderMerger.setMold(mold);

        // 购物车
        List<ShopCartDto> shopCarts = shopCartAdapter.getShopCarts(shopCartItems);

        // 计算满减，并重新组合购物车
        if (discountShopCartManager != null) {
            shopCarts = discountShopCartManager.calculateDiscountAndMakeUpShopCart(shopCarts);
        }

        // 计算套餐，并重新组合购物车
        if (comboShopCartManager != null) {
            shopCarts = comboShopCartManager.calculateDiscountAndMakeUpShopCart(shopCarts);
        }

        // 异步计算运费，运费暂时和优惠券没啥关联，可以与优惠券异步计算，获取用户地址，自提信息
        CompletableFuture<UserDeliveryInfoVO> deliveryFuture = null;
        if (Objects.equals(mold, 0)) {
            deliveryFuture = CompletableFuture.supplyAsync(
                    () -> deliveryOrderManager.calculateAndGetDeliverInfo(userId, orderParam.getAddrId(), orderParam.getDvyType(), shopCartItems),
                    orderThreadPoolExecutor);
        }


        // 计算优惠券，并返回优惠券信息
        if (couponConfirmOrderManager != null) {
            shopCarts = couponConfirmOrderManager.chooseShopCoupon(new ChooseCouponParam(orderParam.getUserChangeCoupon(), orderParam.getCouponUserIds(), shopCarts, ""));
        }

        // 运费用异步计算，最后要等运费出结果
        UserDeliveryInfoVO userDeliveryInfo = new UserDeliveryInfoVO();
        if (Objects.nonNull(deliveryFuture)) {
            userDeliveryInfo = deliveryFuture.get();
        }

        // 当算完一遍店铺的各种满减活动时，重算一遍订单金额
        confirmOrderManager.recalculateAmountWhenFinishingCalculateShop(shopCartOrderMerger, shopCarts, userDeliveryInfo);

        double orderShopReduce = shopCartOrderMerger.getOrderReduce();

        // ===============================================开始平台优惠的计算==================================================

        // 计算平台优惠券，并返回平台优惠券信息
        if (couponConfirmOrderManager != null) {
            shopCartOrderMerger = couponConfirmOrderManager.choosePlatformCoupon(new PlatformChooseCouponParam(orderParam.getUserChangeCoupon(),
                    orderParam.getCouponUserIds(), shopCartOrderMerger, ""));
        }

        // 等级折扣
        if (userLevelOrderManager != null) {
            userLevelOrderManager.calculateLevelDiscount(shopCartOrderMerger);
        }

        // ===============================================结束平台优惠的计算==================================================
        // 结束平台优惠的计算之后，还要重算一遍金额
        confirmOrderManager.recalculateAmountWhenFinishingCalculatePlatform(shopCartOrderMerger);

        // 计算订单积分抵扣金额
        if (orderUseScoreManager != null) {
            orderUseScoreManager.orderUseScore(shopCartOrderMerger, orderParam, shopCartItems);
        }
        shopCartOrderMerger.setOrderShopReduce(orderShopReduce);
        // 计算平台佣金
        confirmOrderManager.calculatePlatformCommission(shopCartOrderMerger);

        // 缓存计算
        confirmOrderManager.cacheCalculatedInfo(userId, shopCartOrderMerger);
        return ServerResponseEntity.success(shopCartOrderMerger);
    }

    @PostMapping("/submit")
    @ApiOperation(value = "提交订单，返回支付流水号", notes = "根据传入的参数判断是否为购物车提交订单，同时对购物车进行删除，用户开始进行支付，根据店铺进行拆单")
    public ServerResponseEntity<OrderNumbersDto> submitOrders(@Valid @RequestBody SubmitOrderParam submitOrderParam) {
        String userId = SecurityUtils.getUser().getUserId();
        ServerResponseEntity<ShopCartOrderMergerDto> orderCheckResult = submitOrderManager.checkSubmitInfo(submitOrderParam, userId);
        if (!orderCheckResult.isSuccess()) {
            if(StrUtil.equals(ResponseEnum.REPEAT_ORDER.value(),orderCheckResult.getCode())){
                OrderNumbersDto orderNumbersDto = new OrderNumbersDto(null);
                orderNumbersDto.setDuplicateError(1);
                return ServerResponseEntity.success(orderNumbersDto);
            }
        }
        ShopCartOrderMergerDto mergerOrder = orderCheckResult.getData();

        List<ShopCartOrderDto> shopCartOrders = mergerOrder.getShopCartOrders();

        List<Order> orders = orderService.submit(mergerOrder);

        // 这里需要判断店铺类型，是堂食的，需要增加餐桌使用记录 和修改餐桌状态
        try {
            if (submitOrderParam.getRoomsId() != null) {
                for (Order order : orders) {
                    ShopDetail shopDetail = shopDetailService.getById(order.getShopId());
                    if (StringUtils.isNotBlank(shopDetail.getStoreType()) && shopDetail.getStoreType().contains("堂食")) {
                        RoomsRecord roomsRecord = new RoomsRecord();
                        roomsRecord.setRoomsId(submitOrderParam.getRoomsId());
                        roomsRecord.setOrderNumber(order.getOrderNumber());
                        roomsRecord.setStartTime(new Date());
                        roomsRecord.setCreateTime(new Date());
                        roomsRecordService.save(roomsRecord);

                        Rooms rooms = new Rooms();
                        rooms.setRoomsId(submitOrderParam.getRoomsId());
                        rooms.setRoomsStatus(1);
                        roomsService.updateById(rooms);
                    }
                }
            }
        }catch (Exception e) {
            log.info("保存餐桌使用记录异常：{}", e.getMessage());
        }


        StringBuilder orderNumbers = new StringBuilder();
        Set<Long> prodIds = new HashSet<>();
        for (Order order : orders) {
            orderNumbers.append(order.getOrderNumber()).append(StrUtil.COMMA);
            prodIds.addAll(order.getOrderItems().stream().map(OrderItem::getProdId).collect(Collectors.toSet()));
        }
        orderNumbers.deleteCharAt(orderNumbers.length() - 1);

        // 更新es中的商品库存
        eventPublisher.publishEvent(new EsProductUpdateEvent(null, new ArrayList<>(prodIds), EsOperationType.UPDATE_BATCH));
        // 移除缓存
        for (ShopCartOrderDto shopCartOrder : shopCartOrders) {
            for (ShopCartItemDiscountDto shopCartItemDiscount : shopCartOrder.getShopCartItemDiscounts()) {
                for (ShopCartItemDto shopCartItem : shopCartItemDiscount.getShopCartItems()) {
                    skuService.removeSkuCacheBySkuId(shopCartItem.getSkuId(), shopCartItem.getProdId());
                    productService.removeProdCacheByProdId(shopCartItem.getProdId());
                }
            }
        }
        orderService.removeConfirmOrderCache(userId + submitOrderParam.getUuid());
        return ServerResponseEntity.success(new OrderNumbersDto(orderNumbers.toString()));
    }


    @GetMapping("/getOrderPayInfoByOrderNumber")
    @ApiOperation(value = "获取订单支付信息", notes = "获取订单支付的商品/地址信息")
    @ApiImplicitParam(name = "orderNumbers", value = "订单流水号", required = true, dataType = "String")
    public ServerResponseEntity<OrderPayInfoParam> getOrderPayInfoByOrderNumber(@RequestParam("orderNumbers") String orderNumbers) {
        List<String> orderNumberList = Arrays.asList(orderNumbers.split(StrUtil.COMMA));
        //获取订单信息
        List<Order> orderList = orderService.getOrderPayInfoByOrderNumber(orderNumberList);
        List<String> prodNameList = Lists.newArrayList();
        Long addrOrderId = null;
        Date endTime = null;
        int totalScore = 0;
        double totalFee = 0.0;
        boolean isStation = false;
        boolean hasAddr = false;
        Integer status = 1;
        Order orderDb = new Order();
        //获取商品名集合
        for (Order order : orderList) {
            for (OrderItem orderItem : order.getOrderItems()) {
                prodNameList.add(orderItem.getProdName());
                totalScore += orderItem.getUseScore() != null ? orderItem.getUseScore() : 0;
            }
            //第一次循环，获取订单地址id，订单过期时间
            if (Objects.isNull(addrOrderId)) {
                addrOrderId = order.getAddrOrderId();
                if (Objects.equals(2, order.getOrderType())) {
                    // 获取秒杀订单的取消订单时间
                    Integer maxCancelTime = 0;
                    SubmitSeckillOrderEvent event = new SubmitSeckillOrderEvent(order, maxCancelTime);
                    applicationContext.publishEvent(event);
                    maxCancelTime = event.getMaxCancelTime();
                    if (maxCancelTime <= 0) {
                        maxCancelTime = 30;
                    }
                    endTime = DateUtil.offsetMinute(order.getCreateTime(), maxCancelTime);
                } else {
                    endTime = DateUtil.offsetMinute(order.getCreateTime(), 30);
                }
            }
            totalFee = Arith.add(totalFee, order.getActualTotal());
            if (Objects.equals(order.getDvyType(), DvyType.STATION.value())) {
                isStation = true;
                orderDb = order;
            }
            if (!Objects.equals(order.getStatus(), OrderStatus.UNPAY.value())) {
                status = order.getStatus();
            }
            if (!hasAddr && !Objects.equals(order.getOrderMold(), 1)) {
                addrOrderId = order.getAddrOrderId();
                hasAddr = true;
            }
        }
        OrderPayInfoParam orderPayInfoParam = new OrderPayInfoParam();
        orderPayInfoParam.setStatus(status);
        orderPayInfoParam.setProdNameList(prodNameList);
        orderPayInfoParam.setEndTime(endTime);
        orderPayInfoParam.setTotalFee(totalFee);
        orderPayInfoParam.setTotalScore(totalScore);
        if (isStation) {
            orderPayInfoParam.setUserAddr("");
            orderPayInfoParam.setReceiver(orderDb.getReceiverName());
            orderPayInfoParam.setMobile(orderDb.getReceiverMobile());
        } else if (hasAddr) {
            //写入商品名、收货地址/电话
            UserAddrOrder userAddrOrder = userAddrOrderService.getById(addrOrderId);
            String addr = userAddrOrder.getProvince() + userAddrOrder.getCity() + userAddrOrder.getArea() + userAddrOrder.getAddr();
            orderPayInfoParam.setUserAddr(addr);
            orderPayInfoParam.setReceiver(userAddrOrder.getReceiver());
            orderPayInfoParam.setMobile(userAddrOrder.getMobile());
        }
        return ServerResponseEntity.success(orderPayInfoParam);
    }
}
