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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yami.shop.bean.enums.RenovationType;
import com.yami.shop.bean.model.ShopRenovation;
import com.yami.shop.common.config.Constant;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.service.ShopRenovationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;


/**
 * @author lhd
 * @date 2021-01-05 11:03:38
 */
@RestController
@Api(tags = "店铺装修信息")
@RequestMapping("/shopRenovation")
public class ShopRenovationController {

    @Autowired
    private ShopRenovationService shopRenovationService;

    @GetMapping("/info")
    @ApiOperation(value = "店铺装修信息", notes = "根据店铺id,模板id,店铺装修信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shopId", value = "店铺id", required = true, dataType = "Long"),
            @ApiImplicitParam(name = "renovationId", value = "模板id", required = true, dataType = "Integer")
    })
    public ServerResponseEntity<ShopRenovation> getById(Long shopId, Long renovationId) {
        ShopRenovation shopRenovation = shopRenovationService.getOne(new LambdaQueryWrapper<ShopRenovation>()
                .eq(ShopRenovation::getShopId, shopId)
                .eq(ShopRenovation::getRenovationId, renovationId)
        );
        return ServerResponseEntity.success(shopRenovation);
    }

    @GetMapping("/getHomePage")
    @ApiOperation(value = "获取店铺装修信息", notes = "根据店铺id,模板id,返回店铺装修信息")
    public ServerResponseEntity<ShopRenovation> getHomePage() {
        ShopRenovation shopRenovation = shopRenovationService.homeShopRenovation(Constant.PLATFORM_SHOP_ID, RenovationType.H5.value());
        return ServerResponseEntity.success(shopRenovation);
    }

    @GetMapping("/homepage")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "shopId", value = "店铺id(默认是平台)", dataType = "Long"),
            @ApiImplicitParam(name = "renovationId", value = "模板id(默认是设为主页的模板)", dataType = "Long"),
            @ApiImplicitParam(name = "renovationType", value = "装修类型 1Pc 2移动端", required = true, dataType = "Integer")
    })
    @ApiOperation(value = "获取平台、店铺装修信息", notes = "根据店铺id,模板id,返回店铺装修信息")
    public ServerResponseEntity<ShopRenovation> getHomePage(Long shopId, Long renovationId, @RequestParam("renovationType") Integer renovationType) {
        if (Objects.isNull(shopId)) {
            shopId = Constant.PLATFORM_SHOP_ID;
        }
        ShopRenovation shopRenovation = null;
        // 没有指定模板，获取设为主页的模板
        if (Objects.isNull(renovationId)) {
            shopRenovation = shopRenovationService.homeShopRenovation(shopId, renovationType);
        }
        // 获取指定模板
        else {
            shopRenovation = shopRenovationService.getShopRenovationById(renovationId);
        }
        return ServerResponseEntity.success(shopRenovation);
    }
}
