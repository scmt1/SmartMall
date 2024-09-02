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

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yami.shop.bean.enums.GiveawayProdTypeEnum;
import com.yami.shop.bean.enums.ProdStatusEnums;
import com.yami.shop.bean.enums.ShopStatus;
import com.yami.shop.bean.event.GetGiveawayProdCountEvent;
import com.yami.shop.bean.event.ProdChangeStatusEvent;
import com.yami.shop.bean.event.ShopChangeStatusEvent;
import com.yami.shop.bean.model.Giveaway;
import com.yami.shop.bean.model.GiveawayProd;
import com.yami.shop.bean.model.Product;
import com.yami.shop.combo.multishop.service.GiveawayProdService;
import com.yami.shop.combo.multishop.service.GiveawayService;
import com.yami.shop.common.enums.StatusEnum;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * @Author lth
 * @Date 2021/11/12 9:26
 */
@Slf4j
@Component("giveawayListener")
@AllArgsConstructor
public class GiveawayListener {

    private final GiveawayService giveawayService;
    private final GiveawayProdService giveawayProdService;

    /**
     * 获取赠品数量
     */
    @EventListener(GetGiveawayProdCountEvent.class)
    public void getGiveawayProdCountByProdId(GetGiveawayProdCountEvent event) {
        int count = 0;
        if (Objects.isNull(event.getGiveawayProdType()) || Objects.equals(event.getGiveawayProdType(), GiveawayProdTypeEnum.MAIN_PROD.value())) {
            count += giveawayService.count(Wrappers.lambdaQuery(Giveaway.class)
                    .eq(Giveaway::getProdId, event.getProdId())
                    .eq(Giveaway::getStatus, StatusEnum.ENABLE.value())
            );
        }
        if (Objects.isNull(event.getGiveawayProdType()) || Objects.equals(event.getGiveawayProdType(), GiveawayProdTypeEnum.GIVE_PROD.value())) {
            count += giveawayProdService.count(Wrappers.lambdaQuery(GiveawayProd.class)
                    .eq(GiveawayProd::getProdId, event.getProdId())
                    .eq(GiveawayProd::getStatus, StatusEnum.ENABLE.value())
            );
        }
        event.setCount(count);
    }

    /**
     * 店铺状态变化监听
     * @param event
     */
    @EventListener(ShopChangeStatusEvent.class)
    public void giveawayShopChangeStatusListener(ShopChangeStatusEvent event) {
        Long shopId = event.getShopId();
        ShopStatus shopStatus = event.getShopStatus();
        if (Objects.isNull(shopId) || Objects.isNull(shopStatus)) {
            return;
        }
        if (Objects.equals(shopStatus, ShopStatus.OFFLINE)) {
            // 店铺下线时，把所有赠品活动失效
            giveawayService.closeGiveawayByShopId(shopId);
        }
    }

    /**
     * 商品状态变化监听
     */
    @EventListener(ProdChangeStatusEvent.class)
    public void giveawayProdChangeStatusEvent(ProdChangeStatusEvent event) {
//        if (Objects.equals(ProdStatusEnums.DELETE.getValue(), event.getStatus())) {
//            // 删除时检查是否有赠品活动，有的话使之全部失效
//            giveawayService.closeGiveawayByMainProdId(event.getProduct().getProdId());
//            return;
//        }
        if (Objects.equals(ProdStatusEnums.PLATFORM_AUDIT.getValue(), event.getStatus()) || Objects.equals(ProdStatusEnums.AUDIT.getValue(), event.getStatus()) ||
                Objects.equals(ProdStatusEnums.DELETE.getValue(), event.getStatus())) {
            return;
        }
        Product product = event.getProduct();
        // 赠送主商品下线，失效赠品活动
        giveawayService.closeGiveawayByMainProdId(product.getProdId());
        // 赠送商品上下线，清除缓存
        List<Long> prodIds = giveawayService.listMainProdIdByGiveawayProdId(product.getProdId());
        giveawayProdService.removeGiveawayCacheBatch(prodIds);
    }
}
