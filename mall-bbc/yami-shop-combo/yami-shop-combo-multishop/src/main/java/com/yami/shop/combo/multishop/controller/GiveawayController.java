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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yami.shop.bean.model.Giveaway;
import com.yami.shop.combo.multishop.service.GiveawayService;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.security.multishop.util.SecurityUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Objects;


/**
 * 赠品
 *
 * @author LGH
 * @date 2021-11-08 13:29:16
 */
@RestController
@AllArgsConstructor
@RequestMapping("/shop/giveaway" )
@Api(tags="赠品接口")
public class GiveawayController {

    private final GiveawayService giveawayService;

    @GetMapping("/page" )
    @ApiOperation(value = "分页获取赠品信息", notes = "分页获取赠品信息")
    public ServerResponseEntity<IPage<Giveaway>> getGiveawayPage(PageParam<Giveaway> page, Giveaway giveaway) {
        giveaway.setShopId(SecurityUtils.getShopUser().getShopId());
        IPage<Giveaway> giveawayPage = giveawayService.pageByParam(page, giveaway);
        return ServerResponseEntity.success(giveawayPage);
    }

    @GetMapping("/info/{giveawayId}" )
    @ApiOperation(value = "通过id查询赠品", notes = "通过id查询赠品")
    public ServerResponseEntity<Giveaway> getById(@PathVariable("giveawayId") Long giveawayId) {
        Long shopId = SecurityUtils.getShopUser().getShopId();
        Giveaway giveaway = giveawayService.getInfoById(giveawayId);
        if (!Objects.equals(shopId, giveaway.getShopId())) {
            throw new YamiShopBindException("yami.giveaway.not.on.shop");
        }
        return ServerResponseEntity.success(giveaway);
    }

    @PostMapping
    @ApiOperation(value = "新增赠品", notes = "新增赠品")
    public ServerResponseEntity<Void> save(@RequestBody @Valid Giveaway giveaway) {
        giveaway.setShopId(SecurityUtils.getShopUser().getShopId());
        giveawayService.saveInfo(giveaway);
        return ServerResponseEntity.success();
    }

    @PutMapping
    @ApiOperation(value = "修改赠品", notes = "修改赠品")
    public ServerResponseEntity<Void> updateById(@RequestBody @Valid Giveaway giveaway) {
        giveaway.setShopId(SecurityUtils.getShopUser().getShopId());
        giveawayService.updateInfo(giveaway);
        return ServerResponseEntity.success();
    }

    @PutMapping("/changeStatus")
    @ApiOperation(value = "修改赠品状态", notes = "修改赠品状态")
    public ServerResponseEntity<Void> changeStatus(@RequestParam(value = "status") Integer status, @RequestParam(value = "giveawayId") Long giveawayId) {
        Long shopId = SecurityUtils.getShopUser().getShopId();
        giveawayService.changeStatus(giveawayId, shopId, status);
        return ServerResponseEntity.success();
    }
}
