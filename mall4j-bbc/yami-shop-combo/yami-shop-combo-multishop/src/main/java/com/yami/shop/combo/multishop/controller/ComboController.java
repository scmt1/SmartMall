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
import com.yami.shop.combo.multishop.model.Combo;
import com.yami.shop.combo.multishop.service.ComboProdService;
import com.yami.shop.combo.multishop.service.ComboService;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.i18n.I18nMessage;
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
 * 套餐
 *
 * @author LGH
 * @date 2021-11-02 10:32:48
 */
@RestController
@AllArgsConstructor
@RequestMapping("/shop/combo" )
@Api(tags="套餐接口")
public class ComboController {

    private final ComboService comboService;
    private final ComboProdService comboProdService;

    @GetMapping("/page" )
    @ApiOperation(value = "分页查询套餐", notes = "分页查询套餐")
    public ServerResponseEntity<IPage<Combo>> getComboPage(PageParam<Combo> page, Combo combo) {
        combo.setLang(I18nMessage.getDbLang());
        combo.setShopId(SecurityUtils.getShopUser().getShopId());
        IPage<Combo> pageRes = comboService.pageByParam(page, combo);
        return ServerResponseEntity.success(pageRes);
    }

    @GetMapping("/info/{comboId}" )
    @ApiOperation(value = "通过id查询套餐", notes = "通过id查询套餐")
    public ServerResponseEntity<Combo> getById(@PathVariable("comboId") Long comboId) {
        Combo combo = comboService.getComboWithProdInfoById(comboId);
        if (!Objects.equals(combo.getShopId(), SecurityUtils.getShopUser().getShopId())) {
            // 当前套餐shopId与用户shopId不相等
            throw new YamiShopBindException("yami.combo.not.shop");
        }
        return ServerResponseEntity.success(combo);
    }

    @PostMapping
    @ApiOperation(value = "新增套餐", notes = "新增套餐")
    public ServerResponseEntity<Void> save(@RequestBody @Valid Combo combo) {
        Long shopId = SecurityUtils.getShopUser().getShopId();
        combo.setShopId(shopId);
        comboService.saveInfo(combo);
        comboProdService.removeComboListCache(combo.getMainProdId());
        return ServerResponseEntity.success();
    }

    @PutMapping
    @ApiOperation(value = "修改套餐", notes = "修改套餐Issued")
    public ServerResponseEntity<Void> updateById(@RequestBody @Valid Combo combo) {
        Long shopId = SecurityUtils.getShopUser().getShopId();
        combo.setShopId(shopId);
        comboService.updateInfo(combo);
        return ServerResponseEntity.success();
    }

    @PutMapping("/changeStatus")
    @ApiOperation(value = "修改套餐状态", notes = "修改套餐状态")
    public ServerResponseEntity<Void> changeStatus(@RequestParam(value = "status") Integer status, @RequestParam(value = "comboId") Long comboId) {
        Long shopId = SecurityUtils.getShopUser().getShopId();
        comboService.changeStatus(comboId, shopId, status);
        return ServerResponseEntity.success();
    }

}
