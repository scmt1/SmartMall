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

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yami.shop.bean.app.dto.*;
import com.yami.shop.bean.enums.*;
import com.yami.shop.bean.model.*;
import com.yami.shop.bean.vo.OrderItemVO;
import com.yami.shop.common.config.Constant;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.i18n.I18nMessage;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.common.util.Arith;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.config.ShopConfig;
import com.yami.shop.security.api.util.SecurityUtils;
import com.yami.shop.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import ma.glasnost.orika.MapperFacade;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author LGH
 */
@RestController
@RequestMapping("/p/myOrder")
@Api(tags = "我的订单接口")
@AllArgsConstructor
public class MyOrderController {

    private final OrderService orderService;

    private final MapperFacade mapperFacade;

    private final UserAddrOrderService userAddrOrderService;

    private final ProductService productService;

    private final SkuService skuService;

    private final ShopConfig shopConfig;

    private final MyOrderService myOrderService;

    private final ShopDetailService shopDetailService;

    private final OrderItemService orderItemService;

    private final OrderRefundService orderRefundService;

    private final OrderVirtualInfoService orderVirtualInfoService;

    private final OrderInvoiceService orderInvoiceService;

    @GetMapping("/orderDetail")
    @ApiOperation(value = "订单详情信息", notes = "根据订单号获取订单详情信息")
    @ApiImplicitParam(name = "orderNumber", value = "订单号", required = true, dataType = "String")
    public ServerResponseEntity<OrderShopDto> orderDetail(@RequestParam(value = "orderNumber") String orderNumber) {
        String userId = SecurityUtils.getUser().getUserId();
        Long stationId = SecurityUtils.getUser().getStationId();
        OrderShopDto orderShopDto = new OrderShopDto();
        Order order;
        if (Objects.nonNull(userId) && Objects.isNull(stationId)) {
            order = orderService.getOrderByOrderNumberAndUserId(orderNumber, userId, false);
        } else {
            order = orderService.getOne(new LambdaQueryWrapper<Order>()
                    .eq(Order::getOrderNumber, orderNumber)
                    .eq(Order::getDvyType, 2)
                    .eq(Order::getDvyId, stationId));
        }
        // 再去找找是否为虚拟商品
        if(Objects.isNull(order)){
            order = orderService.getOne(new LambdaQueryWrapper<Order>()
                    .eq(Order::getOrderNumber, orderNumber)
                    .eq(Order::getOrderMold, 1));
        }
        if (Objects.isNull(order)) {
            // 订单不存在
            throw new YamiShopBindException("yami.order.no.exist");
        }
        ShopDetail shopDetail;
        if (Objects.equals(order.getShopId(), Constant.PLATFORM_SHOP_ID)) {
            shopDetail = new ShopDetail();
            shopDetail.setShopName(Constant.PLATFORM_SHOP_NAME);
        } else {
            shopDetail = shopDetailService.getShopDetailByShopId(order.getShopId());
        }
        UserAddrDto userAddrDto = null;
        if (!Objects.equals(order.getDvyType(), DvyType.STATION.value())) {
            UserAddrOrder userAddrOrder = userAddrOrderService.getById(order.getAddrOrderId());
            userAddrDto = mapperFacade.map(userAddrOrder, UserAddrDto.class);
        }
        // 发票id
        Long orderInvoiceId = orderInvoiceService.getByorderNumber(orderNumber);
        orderShopDto.setOrderInvoiceId(orderInvoiceId);
        orderShopDto.setShopId(shopDetail.getShopId());
        orderShopDto.setDvyType(order.getDvyType());
        orderShopDto.setShopName(shopDetail.getShopName());
        orderShopDto.setActualTotal(order.getActualTotal());
        orderShopDto.setUserAddrDto(userAddrDto);
        orderShopDto.setPayType(order.getPayType());
        orderShopDto.setRefundStatus(order.getRefundStatus());
//        // 如果存在用户等级免运费金额，说明是包邮的，此时运费金额等于免运费金额，否则默认是订单运费
//        if (order.getFreeTransfee() != null && order.getFreeTransfee() > 0) {
//            orderShopDto.setTransfee(order.getFreeTransfee());
//        } else {
//            orderShopDto.setTransfee(order.getFreightAmount());
//        }
        orderShopDto.setTransfee(order.getFreightAmount());
        // 返回满减优惠金额，优惠券优惠金额和店铺优惠总额
        orderShopDto.setDiscountMoney(order.getDiscountAmount());
        orderShopDto.setShopCouponMoney(order.getShopCouponAmount());
        // 返回拼团/秒杀优惠扣除店铺改价金额和平台优惠金额
        orderShopDto.setShopAmount(Arith.sub(Arith.sub(order.getReduceAmount(), order.getPlatformAmount()), order.getShopChangeFreeAmount()));
        // 返回平台优惠券，平台等级，平台积分优惠金额和平台免运费金额
        orderShopDto.setPlatformCouponAmount(order.getPlatformCouponAmount());
        orderShopDto.setMemberAmount(order.getMemberAmount());
        orderShopDto.setScoreAmount(order.getScoreAmount());
        orderShopDto.setPlatformFreeFreightAmount(order.getPlatformFreeFreightAmount());
        orderShopDto.setShopChangeFreeAmount(order.getShopChangeFreeAmount());
        orderShopDto.setFreeTransfee(order.getFreeTransfee());
        orderShopDto.setShopComboAmount(order.getShopComboAmount());
        // Math.abs
        orderShopDto.setReduceAmount(Math.abs(order.getReduceAmount()));
        orderShopDto.setCreateTime(order.getCreateTime());
        orderShopDto.setRemarks(order.getRemarks());
        orderShopDto.setOrderType(order.getOrderType());
        orderShopDto.setStatus(order.getStatus());
        orderShopDto.setOrderMold(order.getOrderMold());
        // 付款时间
        orderShopDto.setPayTime(order.getPayTime());
        // 发货时间
        orderShopDto.setDvyTime(order.getDvyTime());
        // 完成时间
        orderShopDto.setFianllyTime(order.getFinallyTime());
        // 取消时间
        orderShopDto.setCancelTime(order.getCancelTime());
        // 更新时间
        orderShopDto.setUpdateTime(order.getUpdateTime());
        // 桌号
        orderShopDto.setRoomsId(order.getRoomsId());

        refundOrderItemInfo(orderNumber, orderShopDto, order);
        // 如果是虚拟商品订单，处理下虚拟商品订单相关数据
        if(Objects.equals(order.getOrderMold(),1)) {
            handlerVirtualProdOrder(order, orderShopDto,stationId);
        }
        return ServerResponseEntity.success(orderShopDto);
    }

    private void refundOrderItemInfo(String orderNumber, OrderShopDto orderShopDto, Order order) {
        List<OrderItem> orderItemDbList = orderItemService.getOrderItemsByOrderNumber(orderNumber, I18nMessage.getDbLang());
        List<OrderItem> orderItemList = orderItemDbList.stream().filter(item -> Objects.isNull(item.getGiveawayOrderItemId())).collect(Collectors.toList());
        Map<Long, List<OrderItem>> giveawayMap = orderItemDbList.stream().filter(item -> Objects.nonNull(item.getGiveawayOrderItemId()))
                .collect(Collectors.groupingBy(OrderItem::getGiveawayOrderItemId));
        //计算订单使用积分
        Long score = 0L;
        for (OrderItem orderItem : orderItemDbList) {
            score += orderItem.getUseScore();
        }
        orderShopDto.setOrderScore(score);
        // 查询第一个商品是否为秒杀商品
        Product prodDb = productService.getById(orderItemDbList.get(0).getProdId());
        if (Objects.equals(order.getOrderType(), OrderType.SECKILL.value()) && Objects.equals(prodDb.getProdType(), ProdType.PROD_TYPE_SECKILL.value())) {
            orderShopDto.setSeckillId(prodDb.getActivityId());
        }
        List<OrderItemDto> orderItemDtoList = mapperFacade.mapAsList(orderItemList, OrderItemDto.class);
        List<OrderRefund> orderRefunds = orderRefundService.getProcessingOrderRefundByOrderId(order.getOrderId());

        // 可以退款的状态，并在退款时间内
        if (order.getStatus() > OrderStatus.UNPAY.value() && order.getStatus() < OrderStatus.CLOSE.value() && orderRefundService.checkRefundDate(order)) {
            orderShopDto.setCanRefund(true);
            // 有没有正在退款中的订单
            if (CollectionUtil.isEmpty(orderRefunds)) {
                orderShopDto.setCanAllRefund(true);
            }
        }
        double alreadyRefundAmount = 0.0;
        for (OrderRefund orderRefund : orderRefunds) {
            alreadyRefundAmount = Arith.add(alreadyRefundAmount, orderRefund.getRefundAmount());
            // 整单退款
            if (Objects.equals(RefundType.ALL.value(), orderRefund.getRefundType())) {
                orderShopDto.setCanRefund(false);
                // 统一的退款单号
                for (OrderItemDto orderItemDto : orderItemDtoList) {
                    orderItemDto.setRefundSn(orderRefund.getRefundSn());
                }
                break;
            }
            // 单项退款，每个单号都不一样
            for (OrderItemDto orderItemDto : orderItemDtoList) {
                if (Objects.equals(orderItemDto.getOrderItemId(), orderRefund.getOrderItemId())) {
                    orderItemDto.setRefundSn(orderRefund.getRefundSn());
                }
            }

        }
        orderShopDto.setCanRefundAmount(Arith.sub(order.getActualTotal(), alreadyRefundAmount));
        orderShopDto.setOrderItemDtos(orderItemDtoList);
        double total = 0.0;
        Integer totalNum = 0;
        for (OrderItemDto orderItem : orderShopDto.getOrderItemDtos()) {
            total = Arith.add(total, orderItem.getProductTotalAmount());
            totalNum += orderItem.getProdCount();
            // 放入赠品
            if(giveawayMap.containsKey(orderItem.getOrderItemId())){
                orderItem.setGiveawayList(giveawayMap.get(orderItem.getOrderItemId()));
            }
        }
        orderShopDto.setTotal(total);
        orderShopDto.setTotalNum(totalNum);
    }

    /**
     * 添加下虚拟商品的信息
     *
     * @param order        订单信息
     * @param orderShopDto 用于前端展示的订单信息
     * @param stationId 门店id
     */
    private void handlerVirtualProdOrder(Order order, OrderShopDto orderShopDto,Long stationId) {
        orderShopDto.setOrderMold(order.getOrderMold());
        // 虚拟商品留言信息
        orderShopDto.setVirtualRemark(order.getVirtualRemark());
        orderShopDto.setWriteOffStart(order.getWriteOffStart());
        orderShopDto.setWriteOffEnd(order.getWriteOffEnd());
        orderShopDto.setWriteOffNum(order.getWriteOffNum());
        orderShopDto.setWriteOffMultipleCount(order.getWriteOffMultipleCount());
        if (Objects.equals(order.getIsRefund(), 0)) {
            orderShopDto.setCanAllRefund(false);
            orderShopDto.setCanRefund(false);
        }

        // 卡券信息
        List<OrderVirtualInfo> virtualInfoList = orderVirtualInfoService.list(new LambdaQueryWrapper<OrderVirtualInfo>()
                .eq(OrderVirtualInfo::getOrderNumber, order.getOrderNumber())
                .eq(Objects.nonNull(stationId),OrderVirtualInfo::getIsWriteOff, 1)
                .eq(Objects.nonNull(stationId),OrderVirtualInfo::getStationId, stationId));
        boolean flag = Objects.equals(order.getStatus(), OrderStatus.WAIT_GROUP.value()) || Objects.equals(order.getStatus(), OrderStatus.UNPAY.value()) ;
        if (CollectionUtil.isNotEmpty(virtualInfoList) && !flag) {
            orderShopDto.setVirtualInfoList(virtualInfoList);
            orderShopDto.setTotalNum(virtualInfoList.size());
        }
    }

    @GetMapping("/myOrder")
    @ApiOperation(value = "订单列表信息", notes = "根据订单状态获取订单列表信息，状态为0时获取所有订单")
    @ApiImplicitParam(name = "status", value = "订单状态 1:待付款 2:待发货 3:待收货 4:待评价 5:成功 6:失败 7待成团", dataType = "Integer")
    public ServerResponseEntity<IPage<MyOrderDto>> myOrder(@RequestParam(value = "status") Integer status, PageParam<MyOrderDto> page) {
        String userId = SecurityUtils.getUser().getUserId();
        IPage<MyOrderDto> myOrderDtoIpage = myOrderService.pageMyOrderByUserIdAndStatus(page, userId, status, I18nMessage.getDbLang());
        return ServerResponseEntity.success(myOrderDtoIpage);
    }

    @GetMapping("/myOrderSearch")
    @ApiOperation(value = "订单列表信息查询", notes = "根据订单编号或者订单中商品名称搜索")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "status", value = "订单状态 1:待付款 2:待发货 3:待收货 4:待评价 5:成功 6:失败", dataType = "Integer"),
            @ApiImplicitParam(name = "orderMold", value = "订单类别 0.实物商品订单 1. 虚拟商品订单", dataType = "Integer"),
            @ApiImplicitParam(name = "orderName", value = "订单编号或者订单中商品名称", dataType = "String"),
            @ApiImplicitParam(name = "orderTimeStatus", value = "0全部订单 1最近七天 2最近三个月 3三个月之前 订单", dataType = "Integer"),
            @ApiImplicitParam(name = "orderType", value = "0全部订单 1拼团订单 2秒杀订单 3积分订单", dataType = "Integer"),
            @ApiImplicitParam(name = "orderNumber", value = "订单编号", dataType = "String"),
            @ApiImplicitParam(name = "shopId", value = "店铺id", dataType = "Long")
    })
    public ServerResponseEntity<IPage<MyOrderDto>> myOrderSearch(@RequestParam(value = "status") Integer status,
                                                           @RequestParam(value = "orderName") String orderName,
                                                           @RequestParam(value = "orderTimeStatus", required = false) Integer orderTimeStatus,
                                                           @RequestParam(value = "orderType", required = false) Integer orderType,
                                                           @RequestParam(value = "orderMold") Integer orderMold,
                                                           @RequestParam(value = "orderNumber") String orderNumber,
                                                           @RequestParam(value = "shopId", required = false) Long shopId,
                                                           PageParam<MyOrderDto> page) {
        String userId = SecurityUtils.getUser().getUserId();
        IPage<MyOrderDto> myOrderDtoIpage = myOrderService.pageMyOrderByParams(page, userId, status, orderName, orderTimeStatus, orderType, orderNumber, I18nMessage.getDbLang(), shopId,orderMold);
        return ServerResponseEntity.success(myOrderDtoIpage);
    }

    @GetMapping("/getOrderItem")
    @ApiOperation(value = "获取订单项信息", notes = "根据订单项Id获取订单项信息")
    @ApiImplicitParam(name = "orderItemId", value = "订单项Id", dataType = "Long")
    public ServerResponseEntity<OrderItemVO> getOrderItem(@RequestParam(value = "orderItemId") Long orderItemId) {
        String imgDomainName = shopConfig.getDomain().getResourcesDomainName();
        OrderItem orderItem = orderItemService.getByIdI18n(orderItemId);
        orderItem.setPic(orderItem.getPic() != null ? imgDomainName + "/" + orderItem.getPic() : null);
        Order one = orderService.getOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderNumber,orderItem.getOrderNumber()));
        OrderItemVO orderItemVO = new OrderItemVO();
        BeanUtil.copyProperties(orderItem, orderItemVO);
        orderItemVO.setPayDate(one.getPayTime());
        return ServerResponseEntity.success(orderItemVO);
    }

    @GetMapping("/getOrderItems")
    @ApiOperation(value = "获取订单项信息", notes = "根据订单编号orderNumber获取订单项信息")
    @ApiImplicitParam(name = "orderNumber", value = "订单编号", dataType = "String")
    public ServerResponseEntity<List<OrderItem>> getOrderItems(@RequestParam(value = "orderNumber") String orderNumber) {
        String imgDomainName = shopConfig.getDomain().getResourcesDomainName();
//        List<OrderItem> orderItems = orderItemService.list(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderNumber, orderNumber));
//        for (OrderItem orderItem : orderItems) {
//            orderItem.setPic(StrUtil.isNotBlank(orderItem.getPic()) ? imgDomainName + "/" + orderItem.getPic() : null);
//            OrderItem langInfo = orderItemService.getByOrderItemId(orderItem.getOrderItemId(), I18nMessage.getDbLang());
//            orderItem.setProdName(langInfo.getProdName());
//            orderItem.setSkuName(langInfo.getSkuName());
//        }
        List<OrderItem> orderItems = orderItemService.listAndPayTimeByOrderNumber(orderNumber);
        for (OrderItem orderItem : orderItems) {
            orderItem.setPic(orderItem.getPic() !=null ? imgDomainName + "/" + orderItem.getPic() :null);
        }
        return ServerResponseEntity.success(orderItems);
    }

    @GetMapping("/myOrderComment")
    @ApiOperation(value = "订单评价列表接口", notes = "根据订单评价状态获取订单列表信息")
    @ApiImplicitParam(name = "commStatus", value = "订单状态 0:待评价 1已评价", dataType = "Integer")
    public ServerResponseEntity<IPage<MyOrderDto>> myOrderComment(@RequestParam(value = "commStatus") Integer commStatus,
                                                            PageParam<MyOrderDto> page) {
        String userId = SecurityUtils.getUser().getUserId();
        IPage<MyOrderDto> myOrderDtoIpage = myOrderService.myOrderComment(page, userId, commStatus);
        return ServerResponseEntity.success(myOrderDtoIpage);
    }

    @GetMapping("/myOrderItemsComment")
    @ApiOperation(value = "订单项评价列表接口", notes = "根据订单评价状态获取订单列表信息")
    @ApiImplicitParam(name = "commStatus", value = "订单状态 0:待评价 1已评价", dataType = "Integer")
    public ServerResponseEntity<IPage<MyOrderItemDto>> myOrderItemsComment(@RequestParam(value = "commStatus") Integer commStatus,
                                                                     PageParam<MyOrderItemDto> page) {
        String userId = SecurityUtils.getUser().getUserId();
        IPage<MyOrderItemDto> myOrderDtoIpage = myOrderService.myOrderItemsComment(page, userId, commStatus);
        return ServerResponseEntity.success(myOrderDtoIpage);
    }

    @PutMapping("/cancel/{orderNumber}")
    @ApiOperation(value = "根据订单编号取消订单", notes = "根据订单编号取消订单")
    @ApiImplicitParam(name = "orderNumber", value = "订单编号", required = true, dataType = "String")
    public ServerResponseEntity<String> cancel(@PathVariable("orderNumber") String orderNumber) {
        String userId = SecurityUtils.getUser().getUserId();
        Order order = orderService.getOrderByOrderNumberAndUserId(orderNumber, userId, true);
        if (!Objects.equals(order.getStatus(), OrderStatus.UNPAY.value())) {
            // 订单已支付，无法取消订单
            throw new YamiShopBindException("yami.order.status.change");
        }

        List<OrderItem> orderItems = orderItemService.getOrderItemsByOrderNumber(orderNumber, I18nMessage.getDbLang());
        order.setOrderItems(orderItems);
        // 取消订单
        orderService.cancelOrders(Collections.singletonList(order));
        // 清除缓存
        for (OrderItem orderItem : orderItems) {
            productService.removeProdCacheByProdId(orderItem.getProdId());
            skuService.removeSkuCacheBySkuId(orderItem.getSkuId(), orderItem.getProdId());
        }
        return ServerResponseEntity.success();
    }

    @PutMapping("/receipt/{orderNumber}")
    @ApiOperation(value = "根据订单号确认收货", notes = "根据订单号确认收货")
    @ApiImplicitParam(name = "orderNumber", value = "订单编号", required = true, dataType = "String")
    public ServerResponseEntity<String> receipt(@PathVariable("orderNumber") String orderNumber) {
        String userId = SecurityUtils.getUser().getUserId();
        Order order = orderService.getOrderByOrderNumberAndUserId(orderNumber, userId, true);

        if (!Objects.equals(order.getStatus(), OrderStatus.CONSIGNMENT.value())) {
            // 订单不处于待收货状态，无法确认收货
            throw new YamiShopBindException("yami.order.no.delivery");
        }
        if (Objects.equals(order.getRefundStatus(), RefundStatusEnum.APPLY.value())) {
            // 订单退款中，无法确认收货
            throw new YamiShopBindException("yami.order.receipt.refund");
        }
        List<OrderItem> orderItems = orderItemService.getOrderItemsByOrderNumber(orderNumber, I18nMessage.getDbLang());
        order.setOrderItems(orderItems);
        // 确认收货
        orderService.receiptOrder(Collections.singletonList(order));

        for (OrderItem orderItem : orderItems) {
            productService.removeProdCacheByProdId(orderItem.getProdId());
            skuService.removeSkuCacheBySkuId(orderItem.getSkuId(), orderItem.getProdId());
        }
        return ServerResponseEntity.success();
    }

    @DeleteMapping("/{orderNumber}")
    @ApiOperation(value = "根据订单号删除订单", notes = "根据订单号删除订单")
    @ApiImplicitParam(name = "orderNumber", value = "订单号", required = true, dataType = "String")
    public ServerResponseEntity<String> delete(@PathVariable("orderNumber") String orderNumber) {
        String userId = SecurityUtils.getUser().getUserId();
        Order order = orderService.getOrderByOrderNumberAndUserId(orderNumber, userId, true);
        if (!Objects.equals(order.getStatus(), OrderStatus.SUCCESS.value()) && !Objects.equals(order.getStatus(), OrderStatus.CLOSE.value())) {
            // 订单未完成或未关闭，无法删除订单
            throw new YamiShopBindException("yami.order.no.success");
        }
        // 删除订单
        orderService.deleteOrders(Collections.singletonList(order));
        // 删除成功
        return ServerResponseEntity.success(I18nMessage.getMessage("yami.delete.successfully"));
    }


//    /**
//     * 获取我的订单订单数量
//     */
//    @GetMapping("/orderCount")
//    @ApiOperation(value = "获取我的订单订单数量", notes = "获取我的订单订单数量")
//    public ServerResponseEntity<OrderCountData> getOrderCount() {
//        String userId = SecurityUtils.getUser().getUserId();
//        OrderCountData orderCountMap = orderService.getOrderCount(userId);
//        return ServerResponseEntity.success(orderCountMap);
//    }


}
