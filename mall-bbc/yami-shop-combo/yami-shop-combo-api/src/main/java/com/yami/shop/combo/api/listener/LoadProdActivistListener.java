/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.combo.api.listener;

import com.yami.shop.bean.app.vo.ProductVO;
import com.yami.shop.bean.enums.ProdType;
import com.yami.shop.bean.event.LoadProdActivistEvent;
import com.yami.shop.bean.order.LoadProdActivistOrder;
import com.yami.shop.bean.vo.ComboVO;
import com.yami.shop.bean.vo.GiveawayVO;
import com.yami.shop.combo.multishop.service.ComboService;
import com.yami.shop.combo.multishop.service.GiveawayService;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * 加载商品套餐信息
 * @author yami
 */
@Component("comboLoadProdActivistListener")
@AllArgsConstructor
public class LoadProdActivistListener {

    private final ComboService comboService;
    private final GiveawayService giveawayService;

    @EventListener(LoadProdActivistEvent.class)
    @Order(LoadProdActivistOrder.COMBO)
    public void loadProdComboHandle(LoadProdActivistEvent event) {
        ProductVO productVO = event.getProductVO();
        // 秒杀或者团购活动页面，不显示套餐
        if (Objects.nonNull(productVO.getSeckillVO()) || Objects.nonNull(productVO.getGroupActivityVO())) {
            return;
        }
        List<ComboVO> comboList = comboService.listComboByProdId(productVO.getProdId());
        productVO.setComboList(comboList);

        GiveawayVO giveawayVO = giveawayService.getGiveawayProdByProdId(productVO.getProdId());
        productVO.setGiveaway(giveawayVO);
    }

}
