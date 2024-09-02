/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.combo.multishop.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.shop.bean.model.GiveawayProd;
import com.yami.shop.bean.model.Product;
import com.yami.shop.bean.vo.GiveawayVO;
import com.yami.shop.combo.multishop.dao.GiveawayProdMapper;
import com.yami.shop.combo.multishop.service.GiveawayProdService;
import com.yami.shop.common.config.Constant;
import com.yami.shop.common.i18n.I18nMessage;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.common.util.RedisUtil;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 套装商品项
 *
 * @author LGH
 * @date 2021-11-08 13:29:16
 */
@Service
@AllArgsConstructor
public class GiveawayProdServiceImpl extends ServiceImpl<GiveawayProdMapper, GiveawayProd> implements GiveawayProdService {

    private final GiveawayProdMapper giveawayProdMapper;

    @Override
    public void insertBatch(List<GiveawayProd> giveawayProds, Long giveawayId) {
        if (CollUtil.isEmpty(giveawayProds) || Objects.isNull(giveawayId)) {
            return;
        }
        giveawayProdMapper.insertBatch(giveawayProds, giveawayId);
    }

    @Override
    @Cacheable(cacheNames = "getGiveawayByProdId", key = "#prodId")
    public GiveawayVO getGiveawayByProdId(Long prodId) {
        return giveawayProdMapper.getGiveawayByProdId(prodId);
    }

    @Override
    @CacheEvict(cacheNames = "getGiveawayByProdId", key = "#prodId")
    public void removeGiveawayCacheByProdId(Long prodId) {
    }

    @Override
    public void removeGiveawayCacheBatch(List<Long> prodIds) {
        if (CollUtil.isEmpty(prodIds)) {
            return;
        }
        List<String> key = new ArrayList<>();
        for (Long prodId : prodIds) {
            key.add("getGiveawayByProdId" + Constant.UNION + prodId);
        }
        RedisUtil.del(key);
    }

    @Override
    public IPage<Product> getMainProdPage(PageParam<Product> page, GiveawayProd giveawayProd) {
        IPage<Product> productPage = giveawayProdMapper.getMainProdPage(page, giveawayProd, I18nMessage.getLang());
        return productPage;
    }
}
