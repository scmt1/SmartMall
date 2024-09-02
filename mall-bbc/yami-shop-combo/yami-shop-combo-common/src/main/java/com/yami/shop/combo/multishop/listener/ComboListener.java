/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.combo.multishop.listener;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yami.shop.bean.enums.ProdStatusEnums;
import com.yami.shop.bean.enums.ShopStatus;
import com.yami.shop.bean.event.ComboEvent;
import com.yami.shop.bean.event.GetComboProdCountEvent;
import com.yami.shop.bean.event.ProdChangeStatusEvent;
import com.yami.shop.bean.event.ShopChangeStatusEvent;
import com.yami.shop.bean.model.Product;
import com.yami.shop.combo.multishop.model.Combo;
import com.yami.shop.combo.multishop.model.ComboProd;
import com.yami.shop.combo.multishop.service.ComboProdService;
import com.yami.shop.combo.multishop.service.ComboService;
import com.yami.shop.common.enums.StatusEnum;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;


/**
 * @author yami
 */
@Slf4j
@Component("comboListener")
@AllArgsConstructor
public class ComboListener {

    private final ComboProdService comboProdService;

    private final ComboService comboService;

    /**
     * 同步商品状态
     */
    @EventListener(ComboEvent.class)
    public void comboProdChangeStatusListener(ComboEvent event) {
        // 主商品
        Product product = event.getProduct();
        // 套餐搭配商品
        List<ComboProd> comboProdList = comboProdService.list(new LambdaQueryWrapper<ComboProd>()
                .eq(ComboProd::getProdId, product.getProdId())
                .eq(ComboProd::getStatus, 1)
        );
        if (CollUtil.isEmpty(comboProdList)) {
            return;
        }
        Set<Long> comboIds = comboProdList.stream().map(ComboProd::getComboId).collect(Collectors.toSet());
        removeComboCacheByComboIds(comboIds);
    }

    /**
     * 根据商品id获取正在参数套餐的数量
     */
    @EventListener(GetComboProdCountEvent.class)
    public void getComboProdCount(GetComboProdCountEvent event) {
        int count = comboProdService.count(Wrappers.lambdaQuery(ComboProd.class)
                .eq(ComboProd::getProdId, event.getProdId())
                .eq(ComboProd::getStatus, StatusEnum.ENABLE.value())
        );
        event.setCount(count);
    }

    /**
     * 店铺状态变化监听
     * @param event
     */
    @EventListener(ShopChangeStatusEvent.class)
    public void comboShopChangeStatusListener(ShopChangeStatusEvent event) {
        Long shopId = event.getShopId();
        ShopStatus shopStatus = event.getShopStatus();
        if (Objects.isNull(shopId) || Objects.isNull(shopStatus)) {
            return;
        }
        if (Objects.equals(shopStatus, ShopStatus.OFFLINE)) {
            // 店铺下线时，把所有套餐活动失效
            comboService.closeComboByShopId(shopId);
        }
    }

    /**
     * 商品状态变化监听
     */
    @EventListener(ProdChangeStatusEvent.class)
    public void comboProdChangeStatusEvent(ProdChangeStatusEvent event) {
        if (!Objects.equals(ProdStatusEnums.SHOP_OFFLINE.getValue(), event.getStatus()) && !Objects.equals(ProdStatusEnums.PLATFORM_OFFLINE.getValue(), event.getStatus())) {
            return;
        }
        Product product = event.getProduct();
        List<ComboProd> comboProdList = comboProdService.list(new LambdaQueryWrapper<ComboProd>()
                .eq(ComboProd::getProdId, product.getProdId())
                .eq(ComboProd::getStatus, 1)
        );
        if (CollUtil.isEmpty(comboProdList)) {
            return;
        }
        Boolean isMainProd = false;
        List<Long> comboIds = new ArrayList<>();
        for (ComboProd comboProd : comboProdList) {
            if (comboProd.getType() == 1) {
                isMainProd = true;
            }
            comboIds.add(comboProd.getComboId());
        }
        // 主商品下线， 就失效活动
        if (isMainProd) {
            // 下架商品，且不是主商品则清除缓存
            comboService.closeComboByMainProdId(event.getProduct().getProdId());
        }
        if (CollUtil.isEmpty(comboIds)) {
            return;
        }
        removeComboCacheByComboIds(comboIds);
    }

    private void removeComboCacheByComboIds(Collection<Long> comboIds) {
        if (CollUtil.isEmpty(comboIds)) {
            return;
        }
        // 搭配商品下线, 清除套餐缓存
        List<Combo> list = comboService.list(new LambdaQueryWrapper<Combo>().in(Combo::getComboId, comboIds));
        List<Long> prodIds = list.stream().map(Combo::getMainProdId).collect(Collectors.toList());
        comboProdService.removeComboListCacheByProdIds(prodIds);
    }
}
