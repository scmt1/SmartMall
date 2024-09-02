package com.yami.shop.combo.multishop.listener;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yami.shop.bean.event.CategoryWordEvent;
import com.yami.shop.bean.model.Giveaway;
import com.yami.shop.combo.multishop.dao.ComboMapper;
import com.yami.shop.combo.multishop.dao.GiveawayMapper;
import com.yami.shop.combo.multishop.model.Combo;
import com.yami.shop.combo.multishop.service.ComboService;
import com.yami.shop.combo.multishop.service.GiveawayService;
import com.yami.shop.common.enums.StatusEnum;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("CategoryComboListener")
@Slf4j
@AllArgsConstructor
public class CategoryComboListener {

    private final ComboService comboService;
    private final ComboMapper comboMapper;
    private final GiveawayService giveawayService;
    private final GiveawayMapper giveawayMapper;

    @EventListener(CategoryWordEvent.class)
    public void CategoryComboListener(CategoryWordEvent event) {
        List<Long> prodIds = event.getProdIdList();
        //失效套餐活动
        if(CollUtil.isNotEmpty(prodIds)){
            List<Combo> comboList = comboMapper.selectList(Wrappers.lambdaUpdate(Combo.class)
                    .in(Combo::getMainProdId, prodIds)
                    .in(Combo::getStatus, StatusEnum.ENABLE.value(), StatusEnum.OFFLINE.value())
            );
            comboService.closeComboByComboList(comboList);

            // 失效赠品活动
            List<Giveaway> giveawayList = giveawayMapper.selectList(Wrappers.lambdaUpdate(Giveaway.class)
                    .in(Giveaway::getProdId, prodIds)
                    .in(Giveaway::getStatus, StatusEnum.ENABLE.value(), StatusEnum.OFFLINE.value())
            );
            giveawayService.closeGiveawayByGiveawayList(giveawayList);
        }
    }
}
