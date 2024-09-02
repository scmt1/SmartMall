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


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yami.shop.bean.app.dto.ProdCommDto;
import com.yami.shop.bean.app.param.ProdCommParam;
import com.yami.shop.bean.enums.OrderStatus;
import com.yami.shop.bean.enums.ReturnMoneyStsType;
import com.yami.shop.bean.model.Order;
import com.yami.shop.bean.model.OrderItem;
import com.yami.shop.bean.model.ProdComm;
import com.yami.shop.bean.model.Product;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.i18n.I18nMessage;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.security.api.util.SecurityUtils;
import com.yami.shop.service.OrderItemService;
import com.yami.shop.service.OrderService;
import com.yami.shop.service.ProdCommService;
import com.yami.shop.service.ProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Objects;

/**
 * @author LGH
 */
@RestController
@RequestMapping("/p/prodComm")
@Api(tags = "商品评论接口")
@AllArgsConstructor
public class ProdCommController {

    private final ProdCommService prodCommService;

    private final OrderItemService orderItemService;

    private final OrderService orderService;

    private final ProductService productService;

    @GetMapping("/prodCommPageByUser")
    @ApiOperation(value = "根据用户返回评论分页数据", notes = "传入页码")
    public ServerResponseEntity<IPage<ProdCommDto>> getProdCommPage(PageParam page) {
        return ServerResponseEntity.success(prodCommService.getProdCommDtoPageByUserId(page, SecurityUtils.getUser().getUserId()));
    }

    @PostMapping
    @ApiOperation(value = "添加评论")
    public ServerResponseEntity<Void> saveProdCommPage(@Valid @RequestBody ProdCommParam prodCommParam) {
        Long orderItemId = prodCommParam.getOrderItemId();
        OrderItem orderItem = orderItemService.getByOrderItemId(orderItemId);
        if (orderItem == null) {
            // 订单项不存在
            throw new YamiShopBindException("yami.address.deleted");
        }
        if (Objects.equals(orderItem.getCommSts(), 1)) {
            // 改订单项已评论，请勿重复评论
            throw new YamiShopBindException("yami.item.has.commented");
        }
        String userId = SecurityUtils.getUser().getUserId();
        Order order = orderService.getOrderByOrderNumberAndUserId(orderItem.getOrderNumber(), userId, true);
        if (!Objects.equals(order.getStatus(), OrderStatus.SUCCESS.value()) && !Objects.equals(orderItem.getReturnMoneySts(), ReturnMoneyStsType.SUCCESS.value())) {
            // 请确认收货后再进行评论
            throw new YamiShopBindException("yami.confirm.receipt");
        }
        Product product = productService.getProductByProdId(orderItem.getProdId(), I18nMessage.getDbLang());
        if (Objects.isNull(product)) {
            String takeOff = I18nMessage.getMessage("yami.product.no.exist");
            // 商品不存在
            throw new YamiShopBindException(takeOff);
        }
        if (StrUtil.isBlank(prodCommParam.getPics())) {
            prodCommParam.setPics(null);
        }
        prodCommService.comm(orderItem, prodCommParam);
        return ServerResponseEntity.success();
    }

    @GetMapping("/getProdComment")
    @ApiOperation(value = "根据订单项Id获取评论")
    @ApiImplicitParam(name = "orderItemId", value = "订单项Id", dataType = "Long")
    public ServerResponseEntity<ProdComm> getProdComment(Long orderItemId) {
        ProdComm prodComm = prodCommService.getOne(new LambdaUpdateWrapper<ProdComm>()
                .eq(ProdComm::getOrderItemId, orderItemId)
                .eq(ProdComm::getUserId, SecurityUtils.getUser().getUserId()));
        if (Objects.isNull(prodComm)) {
            throw new YamiShopBindException("yami.comment.is.delete");
        }
        return ServerResponseEntity.success(prodComm);
    }
}
