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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yami.shop.bean.enums.InvoiceHeaderType;
import com.yami.shop.bean.enums.OrderInvoiceState;
import com.yami.shop.bean.model.OrderInvoice;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.security.api.util.SecurityUtils;
import com.yami.shop.service.OrderInvoiceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;
import java.util.Objects;


/**
 * @author Citrus
 * @date 2021-08-16 14:22:47
 */
@RestController("appOrderInvoiceController")
@RequestMapping("/p/orderInvoice")
@Api(tags = "app订单发票接口")
public class OrderInvoiceController {

    @Autowired
    private OrderInvoiceService orderInvoiceService;

    @GetMapping("/page")
    @ApiOperation(value = "分页查询订单发票列表信息")
    public ServerResponseEntity<IPage<OrderInvoice>> getOrderInvoicePage(PageParam<OrderInvoice> page) {
        IPage<OrderInvoice> userInvoicePage = orderInvoiceService.pageUserInvoice(page, SecurityUtils.getUser().getUserId());
        return ServerResponseEntity.success(userInvoicePage);
    }

    @GetMapping("/info/{orderInvoiceId}")
    @ApiOperation(value = "通过订单发票id查询订单发票信息")
    @ApiImplicitParam(name = "orderInvoiceId", value = "订单发票id", required = true, dataType = "Long")
    public ServerResponseEntity<OrderInvoice> getById(@PathVariable("orderInvoiceId") Long orderInvoiceId) {
        OrderInvoice orderInvoice = orderInvoiceService.getById(orderInvoiceId);
        if (Objects.nonNull(orderInvoice.getUserId()) && !Objects.equals(orderInvoice.getUserId(), SecurityUtils.getUser().getUserId())) {
            //非当前用户的发票信息
            throw new YamiShopBindException("yami.invoice.not.user");
        }
        return ServerResponseEntity.success(orderInvoice);
    }

    @PostMapping
    @ApiOperation(value = "申请开票")
    public ServerResponseEntity<Boolean> save(@RequestBody @Valid OrderInvoice orderInvoice) {
        if (Objects.isNull(orderInvoice.getShopId())) {
            //店铺id不能为空
            throw new YamiShopBindException("yami.invoice.shopId.notNull");
        }
        Long orderId = orderInvoiceService.getByorderNumber(orderInvoice.getOrderNumber());
        if (Objects.nonNull(orderId)) {
            //该订单已经申请发票，请勿重复申请！
            throw new YamiShopBindException("yami.invoice.invoiceId.duplicate");
        }
        if (Objects.equals(InvoiceHeaderType.PERSONAL.value(), orderInvoice.getHeaderType())) {
            orderInvoice.setInvoiceTaxNumber(null);
        }
        orderInvoice.setInvoiceState(OrderInvoiceState.APPLICATION.value());
        orderInvoice.setApplicationTime(new Date());
        return ServerResponseEntity.success(orderInvoiceService.save(orderInvoice));
    }

    @PutMapping
    @ApiOperation(value = "申请换开")
    public ServerResponseEntity<Boolean> updateById(@RequestBody @Valid OrderInvoice orderInvoice) {
        orderInvoice.setInvoiceState(OrderInvoiceState.APPLICATION.value());
        orderInvoice.setApplicationTime(new Date());
        if (Objects.equals(InvoiceHeaderType.PERSONAL.value(), orderInvoice.getHeaderType())) {
            orderInvoice.setInvoiceTaxNumber(null);
        }
        return ServerResponseEntity.success(orderInvoiceService.updateById(orderInvoice));
    }
}
