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

import com.yami.shop.bean.app.dto.DeliveryDto;
import com.yami.shop.bean.app.dto.SimpleDeliveryDto;
import com.yami.shop.bean.model.Delivery;
import com.yami.shop.bean.model.Order;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.security.api.util.SecurityUtils;
import com.yami.shop.service.DeliveryService;
import com.yami.shop.service.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author LGH
 */
@Slf4j
@RestController
@RequestMapping("/p/delivery")
@Api(tags = "查看物流接口")
public class DeliveryController {

    @Autowired
    private DeliveryService deliveryService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private MapperFacade mapperFacade;

    /**
     * 查看物流接口
     */
    @GetMapping("/check")
    @ApiOperation(value = "查看物流", notes = "根据订单号查看物流")
    @ApiImplicitParam(name = "orderNumber", value = "订单号", required = true, dataType = "String")
    public ServerResponseEntity<DeliveryDto> checkDelivery(String orderNumber) {
        // 获取用户id
        String userId = SecurityUtils.getUser().getUserId();
        // 查询订单
        Order order = orderService.getOrderByOrderNumberAndUserId(orderNumber, userId, true);
        // 查询交易单
        Delivery delivery = deliveryService.getById(order.getDvyId());
        if (null == delivery) {
            // 交易单号不存在
            throw new YamiShopBindException("yami.transaction.not.exists");
        }
        // 查询物流详情信息
        // 物流公司名称、官网、订单号、物流详情信息
        DeliveryDto deliveryDto;
        try {
            // 解析的物流详情明细
            deliveryDto = deliveryService.query(delivery.getDvyId(), order.getDvyFlowId(), order.getReceiverMobile());
            deliveryDto.setDvyFlowId(order.getDvyFlowId());
            deliveryDto.setCompanyHomeUrl(delivery.getCompanyHomeUrl());
            deliveryDto.setCompanyName(delivery.getDvyName());
        } catch (Exception e) {
            log.error("物流详情查询出错:", e);
            // 查询出错
            throw new YamiShopBindException("yami.query.error");
        }
        return ServerResponseEntity.success(deliveryDto);
    }

    @GetMapping("/list")
    @ApiOperation(value = "查看物流列表", notes = "查看物流列表")
    public ServerResponseEntity<List<SimpleDeliveryDto>> checkDelivery() {
        List<Delivery> list = deliveryService.list();
        List<SimpleDeliveryDto> deliveryDtos = mapperFacade.mapAsList(list, SimpleDeliveryDto.class);
        return ServerResponseEntity.success(deliveryDtos);
    }
}
