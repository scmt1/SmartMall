/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.combo.multishop.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yami.shop.bean.model.GiveawayProd;
import com.yami.shop.bean.model.Product;
import com.yami.shop.combo.multishop.service.GiveawayProdService;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.security.multishop.util.SecurityUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * 套装商品项
 *
 * @author LGH
 * @date 2021-11-08 13:29:16
 */
@RestController
@AllArgsConstructor
@RequestMapping("/shop/giveawayProd" )
@Api(tags="赠品商品项接口")
public class GiveawayProdController {

    private final GiveawayProdService giveawayProdService;

    /**
     * 分页查询
     * @param page 分页对象
     * @param giveawayProd 套装商品项
     * @return 分页数据
     */
    @GetMapping("/page" )
    @ApiOperation(value = "分页获取赠品信息", notes = "分页获取赠品信息")
    public ServerResponseEntity<IPage<GiveawayProd>> getGiveawayProdPage(PageParam<GiveawayProd> page, GiveawayProd giveawayProd) {
        return ServerResponseEntity.success(giveawayProdService.page(page, new LambdaQueryWrapper<GiveawayProd>()));
    }

    /**
     * 主赠送商品分页查询
     * @param page 分页对象
     * @param giveawayProd 套装商品项
     * @return 分页数据
     */
    @GetMapping("/mainProdPage" )
    @ApiOperation(value = "分页获取主赠送商品", notes = "分页获取赠品信息")
    public ServerResponseEntity<IPage<Product>> getMainProdPage(PageParam<Product> page, GiveawayProd giveawayProd) {
        Long shopId = SecurityUtils.getShopUser().getShopId();
        giveawayProd.setShopId(shopId);
        return ServerResponseEntity.success(giveawayProdService.getMainProdPage(page, giveawayProd));
    }

}
