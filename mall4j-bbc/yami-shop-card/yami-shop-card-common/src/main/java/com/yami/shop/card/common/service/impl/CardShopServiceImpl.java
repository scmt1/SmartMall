/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.card.common.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.shop.card.common.dao.CardShopMapper;
import com.yami.shop.card.common.service.CardShopService;
import com.yami.shop.card.common.model.CardShop;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author lgh on 2018/12/27.
 */
@Service
@AllArgsConstructor
public class CardShopServiceImpl extends ServiceImpl<CardShopMapper, CardShop> implements CardShopService {

    @Autowired
    private CardShopMapper cardShopMapper;

    @Override
    public List<CardShop> queryCardShopList(Long id) {
        return cardShopMapper.queryCardShopList(id);
    }

    @Override
    public List<CardShop> getConfigNoUseCardShopList(List<Long> shopIds) {
        return cardShopMapper.getConfigNoUseCardShopList(shopIds);
    }

    @Override
    public List<CardShop> getNoUseCardShopList(List<Long> shopIds) {
        return cardShopMapper.getNoUseCardShopList(shopIds);
    }
}

