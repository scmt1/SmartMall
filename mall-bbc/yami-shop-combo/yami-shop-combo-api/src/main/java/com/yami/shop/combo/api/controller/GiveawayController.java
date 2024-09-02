/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.combo.api.controller;

import com.yami.shop.bean.vo.GiveawayVO;
import com.yami.shop.combo.multishop.service.GiveawayService;
import com.yami.shop.common.response.ServerResponseEntity;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Citrus
 * @date 2021/11/12 9:19
 */
@RestController
@RequestMapping("/p/giveaway" )
@Api(tags="用户端赠品接口")
public class GiveawayController {

    @Autowired
    private GiveawayService giveawayService;

    @GetMapping("/info/{prodId}")
    @ApiOperation(value = "通过商品id查询赠品信息", notes = "通过商品id查询赠品")
    public ServerResponseEntity<GiveawayVO> getById(@PathVariable("prodId") Long prodId) {
        GiveawayVO giveawayVO = giveawayService.getGiveawayProdByProdId(prodId);
        return ServerResponseEntity.success(giveawayVO);
    }
}
