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
import com.yami.shop.combo.multishop.dao.ComboProdSkuMapper;
import com.yami.shop.combo.multishop.model.ComboProdSku;
import com.yami.shop.combo.multishop.service.ComboProdSkuService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 套装商品sku项
 *
 * @author LGH
 * @date 2021-11-05 09:23:32
 */
@Service
@AllArgsConstructor
public class ComboProdSkuServiceImpl extends ServiceImpl<ComboProdSkuMapper, ComboProdSku> implements ComboProdSkuService {

    private final ComboProdSkuMapper comboProdSkuMapper;

    @Override
    public void insertBatch(List<ComboProdSku> skuList, Long comboProdId) {
        if (CollUtil.isEmpty(skuList) || Objects.isNull(comboProdId)) {
            return;
        }
        comboProdSkuMapper.insertBatch(skuList, comboProdId);
    }

    @Override
    public List<ComboProdSku> listByProdIdAndComboId(Long prodId, Long comboId) {
        return comboProdSkuMapper.listByProdIdAndComboId(prodId, comboId);
    }
}
