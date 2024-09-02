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
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.shop.bean.vo.ComboProdVO;
import com.yami.shop.bean.vo.ComboVO;
import com.yami.shop.combo.multishop.dao.ComboMapper;
import com.yami.shop.combo.multishop.dao.ComboProdMapper;
import com.yami.shop.combo.multishop.model.ComboProd;
import com.yami.shop.combo.multishop.service.ComboProdService;
import com.yami.shop.common.config.Constant;
import com.yami.shop.common.i18n.LanguageEnum;
import com.yami.shop.common.util.Arith;
import com.yami.shop.common.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * 套餐商品项
 *
 * @author LGH
 * @date 2021-11-02 10:35:08
 */
@Service
public class ComboProdServiceImpl extends ServiceImpl<ComboProdMapper, ComboProd> implements ComboProdService {

    @Autowired
    private ComboProdMapper comboProdMapper;
    @Autowired
    private ComboMapper comboMapper;

    @Override
    public void insertBatch(List<ComboProd> comboProdList, Long comboId) {
        if (CollUtil.isEmpty(comboProdList)) {
            return;
        }
        comboProdMapper.insertBatch(comboProdList, comboId);
    }

    @Override
    @Cacheable(cacheNames = "listCombo", key = "#prodId + ':' + #lang")
    public List<ComboVO> listCombo(Long prodId, Integer lang) {
        if (Objects.isNull(prodId)) {
            return null;
        }
        List<ComboVO> comboList = comboMapper.listComboByProdId(prodId, lang);
        Iterator<ComboVO> iterator = comboList.iterator();
        while (iterator.hasNext()) {
            ComboVO comboVO = iterator.next();
             double minPrice = 0D;
            Iterator<ComboProdVO> prodIterator = comboVO.getMatchingProds().iterator();
            while (prodIterator.hasNext()) {
                ComboProdVO comboProdVO = prodIterator.next();
                if (!Objects.equals(comboProdVO.getProdStatus(), 1)) {
                    prodIterator.remove();
                    continue;
                }
                if (comboProdVO.getRequired() == 1 || comboProdVO.getType() == 1) {
                    minPrice = Arith.add(minPrice, Arith.mul(comboProdVO.getComboPrice(), comboProdVO.getLeastNum()));
                }
            }
            comboVO.setComboAmount(minPrice);
            if (CollUtil.isEmpty(comboVO.getMatchingProds())) {
                iterator.remove();
            }
        }
        return comboList;
    }

    @Override
    public void removeComboListCache(Long prodId) {
        List<String> key = new ArrayList<>();
        key.add("listCombo" + Constant.UNION + prodId + Constant.COLON + LanguageEnum.LANGUAGE_ZH_CN.getLang());
        key.add("listCombo" + Constant.UNION + prodId + Constant.COLON + LanguageEnum.LANGUAGE_EN.getLang());
        RedisUtil.del(key);
    }

    @Override
    public void removeComboListCacheByProdIds(List<Long> prodIds) {
        if (CollUtil.isEmpty(prodIds)) {
            return;
        }
        List<String> key = new ArrayList<>();
        for (Long prodId : prodIds) {
            key.add("listCombo" + Constant.UNION + prodId + Constant.COLON + LanguageEnum.LANGUAGE_ZH_CN.getLang());
            key.add("listCombo" + Constant.UNION + prodId + Constant.COLON + LanguageEnum.LANGUAGE_EN.getLang());
        }
        RedisUtil.del(key);
    }

    @Override
    public List<ComboProdVO> listComboProdByProdId(Long prodId) {
        return comboProdMapper.listComboProdByProdId(prodId);
    }
}
