package com.yami.shop.api.controller;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yami.shop.bean.bo.PayInfoResultBO;
import com.yami.shop.bean.enums.OrderStatus;
import com.yami.shop.bean.enums.PayEntry;
import com.yami.shop.bean.enums.PayStatus;
import com.yami.shop.bean.enums.RefundStatusEnum;
import com.yami.shop.bean.model.Order;
import com.yami.shop.bean.model.OrderItem;
import com.yami.shop.bean.model.PayInfo;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.i18n.I18nMessage;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.common.util.Arith;
import com.yami.shop.common.util.RequestKitBean;
import com.yami.shop.dao.PayInfoMapper;
import com.yami.shop.service.*;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@ApiIgnore
@RestController
@RequestMapping("/hySystem")
@AllArgsConstructor
@Slf4j
public class HyController {

    private final OrderService orderService;
    private final PayInfoService payInfoService;
    private final OrderItemService orderItemService;
    private final ProductService productService;
    private final SkuService skuService;

    @Autowired
    private RequestKitBean requestKitBean;

    @Autowired
    private PayInfoMapper payInfoMapper;

    @RequestMapping("/order/HyPayNotice")
    public Object HyPayNotice() {
        JSONObject params = getReqParamJSON();
        log.info("接收红云平台回调参数=========={}", params);

        if (params == null || StrUtil.isBlank(params.toJSONString())) {
            return "false";
        }

        // 支付单号mchOrderNo 第三方支付单号channelOrderNo   支付状态state 支付金额amount  红云系统订单号payOrderId

        //根据红云平台的订单号 找出我们系统的订单号
        Order order = orderService.getOne(new LambdaQueryWrapper<Order>().eq(Order::getHyOrderNumber, params.getString("payOrderId")));
        PayInfoResultBO payInfoResultBO = new PayInfoResultBO();
        payInfoResultBO.setPayNo(params.getString("mchOrderNo"));
        payInfoResultBO.setBizPayNo(params.getString("channelOrderNo"));
        payInfoResultBO.setBizOrderNo(params.getString("payOrderId"));
        payInfoResultBO.setIsPaySuccess("2".equals(params.getString("state")));
        payInfoResultBO.setSuccessString("true");
        payInfoResultBO.setPayAmount(Arith.div(Double.parseDouble(params.getString("amount")), 100));
        payInfoResultBO.setCallbackContent(params.toJSONString());


        PayInfo payInfo = new PayInfo();
        payInfo.setPayNo(params.getString("mchOrderNo"));
        payInfo.setBizPayNo(params.getString("channelOrderNo"));
        payInfo.setBizOrderNo(params.getString("payOrderId"));
        payInfo.setUserId(order.getUserId());
        payInfo.setPayAmount(Double.parseDouble(params.getString("amount")));
        payInfo.setPayScore(0L);
        payInfo.setPayStatus(PayStatus.UNPAY.value());
        payInfo.setPayType(7);//红云平台支付
        payInfo.setVersion(0);
        // 保存多个支付订单号
        payInfo.setOrderNumbers(order.getOrderNumber());
        payInfo.setPayEntry(PayEntry.ORDER.value());
        payInfo.setCreateTime(new Date());
        payInfo.setCallbackTime(new Date());
        payInfo.setCallbackContent(params.toJSONString());
        // 保存预支付信息
        payInfoMapper.insert(payInfo);
        payInfoService.noticeOrder(payInfoResultBO, payInfo);
        return "true";
    }


    @PostMapping("/receipt")
    @ApiOperation(value = "根据订单号确认收货", notes = "根据订单号确认收货")
    public ServerResponseEntity<String> receipt(String hyOrderNumber) {
        Order order = orderService.getOne(new LambdaQueryWrapper<Order>().eq(Order::getHyOrderNumber, hyOrderNumber));
        String orderNumber = order.getOrderNumber();

        String userId = order.getUserId();
        order = orderService.getOrderByOrderNumberAndUserId(orderNumber, userId, true);

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

    public JSONObject getReqParamJSON() {
        return requestKitBean.getReqParamJSON();
    }
}
