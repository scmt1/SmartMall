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

import cn.hutool.core.collection.CollUtil;
import com.yami.shop.bean.app.dto.SkuDto;
import com.yami.shop.bean.vo.ComboVO;
import com.yami.shop.combo.multishop.model.Combo;
import com.yami.shop.combo.multishop.model.ComboProd;
import com.yami.shop.combo.multishop.model.ComboProdSku;
import com.yami.shop.combo.multishop.param.CalculateComboParam;
import com.yami.shop.combo.multishop.service.ComboService;
import com.yami.shop.combo.multishop.vo.CalculateComboVO;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.common.util.Arith;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * 套餐
 *
 * @author LGH
 * @date 2021-11-02 10:32:48
 */
@RestController
@AllArgsConstructor
@RequestMapping("/combo" )
@Api(tags="套餐接口")
public class ComboController {

    private final ComboService comboService;

    @GetMapping("/getComboByComboId" )
    @ApiOperation(value = "获取套餐信息", notes = "获取套餐信息")
    @ApiImplicitParam(name = "comboId", value = "套餐id")
    public ServerResponseEntity<ComboVO> getComboByComboId(@RequestParam("comboId") Long comboId) {
        ComboVO comboVO = comboService.getComboInfoByComboId(comboId);
        return ServerResponseEntity.success(comboVO);
    }

    @GetMapping("/skuList")
    @ApiOperation(value = "获取套餐商品sku信息", notes = "根据商品ID（prodId）单独获取sku信息")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(name = "prodId", value = "商品ID", required = true, dataType = "Long"),
            @ApiImplicitParam(name = "comboId", value = "套餐ID", required = true, dataType = "Long")
    })
    public ServerResponseEntity<List<SkuDto>> skuList(@RequestParam("prodId") Long prodId, @RequestParam("comboId") Long comboId) {
        return ServerResponseEntity.success(comboService.getComboSkuInfo(prodId, comboId));
    }

    @PostMapping("/getCombo" )
    @ApiOperation(value = "pc端-获取套餐信息", notes = "获取套餐信息")
    public ServerResponseEntity<ComboVO> getComboByComboId(@RequestBody ComboVO comboVO) {
        ComboVO combo = comboService.getComboInfo(comboVO);
        return ServerResponseEntity.success(combo);
    }

    @PutMapping("/calculateComboAmount" )
    @ApiOperation(value = "计算套餐金额", notes = "计算套餐金额")
    public ServerResponseEntity<CalculateComboVO> calculateComboAmount(@RequestBody CalculateComboParam calculateComboParam) {
        double price = 0D;
        double matchingPrice = 0D;
        Combo combo = comboService.getComboWithProdInfoById(calculateComboParam.getComboId());
        if (Objects.isNull(combo) || !Objects.equals(combo.getStatus(), 1)) {
            // 套餐不存在，或已失效
            throw new YamiShopBindException("yami.prod.combo.expired");
        }
        // 计算主商品金额
        ComboProd comboProd = combo.getMainProd();
        for (ComboProdSku comboProdSku : comboProd.getSkuList()) {
            if (Objects.equals(calculateComboParam.getSkuId(), comboProdSku.getSkuId())) {
                price = Arith.add(price, Arith.mul(comboProdSku.getPrice(), comboProd.getLeastNum()));
                matchingPrice = Arith.add(matchingPrice, Arith.mul(comboProdSku.getMatchingPrice(), comboProd.getLeastNum()));
                break;
            }
        }
        if (price == 0) {
            // 主商品信息错误，请刷新后重试
            throw new YamiShopBindException("yami.main.prod.wrong");
        }
        List<Long> matchingSkuIds = calculateComboParam.getMatchingSkuIds();
        // 计算搭配商品金额
        if (CollUtil.isNotEmpty(combo.getMatchingProds())) {
            for (ComboProd matchingProd : combo.getMatchingProds()) {
                ComboProdSku comboProdSku = getComboProdSku(matchingProd, matchingSkuIds);
                if (Objects.equals(matchingProd.getRequired(), 1) && Objects.isNull(comboProdSku)) {
                    // 请选择必选搭配商品
                    throw new YamiShopBindException("yami.select.required.matching.items");
                } else if (Objects.isNull(comboProdSku)) {
                    continue;
                }
                price = Arith.add(price, Arith.mul(comboProdSku.getPrice(), matchingProd.getLeastNum()));
                matchingPrice = Arith.add(matchingPrice, Arith.mul(comboProdSku.getMatchingPrice(), matchingProd.getLeastNum()));
            }
        }
        if (calculateComboParam.getComboNum() < 1) {
            calculateComboParam.setComboNum(1);
        }
        // 计算套餐数量的金额
        price = Arith.mul(price, calculateComboParam.getComboNum());
        matchingPrice = Arith.mul(matchingPrice, calculateComboParam.getComboNum());
        CalculateComboVO calculateComboVO = new CalculateComboVO();
        calculateComboVO.setPrice(price);
        calculateComboVO.setMatchingPrice(matchingPrice);
        calculateComboVO.setReduceAmount(Arith.sub(price, matchingPrice));
        return ServerResponseEntity.success(calculateComboVO);
    }

    private ComboProdSku getComboProdSku(ComboProd matchingProd, List<Long> matchingSkuIds) {
        if(CollUtil.isEmpty(matchingSkuIds)) {
            return null;
        }
        for (ComboProdSku comboProdSku : matchingProd.getSkuList()) {
            if (matchingSkuIds.contains(comboProdSku.getSkuId())) {
                return comboProdSku;
            }
        }
        return null;
    }
}
