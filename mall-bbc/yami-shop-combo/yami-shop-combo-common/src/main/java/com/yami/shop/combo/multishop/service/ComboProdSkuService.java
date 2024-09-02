/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.combo.multishop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yami.shop.combo.multishop.model.ComboProdSku;

import java.util.List;

/**
 * 套装商品sku项
 *
 * @author LGH
 * @date 2021-11-05 09:23:32
 */
public interface ComboProdSkuService extends IService<ComboProdSku> {

    /**
     * 保存商品sku项
     * @param skuList
     * @param comboProdId
     */
    void insertBatch(List<ComboProdSku> skuList, Long comboProdId);

    /**
     * 根据商品、套餐id获取套餐sku信息
     * @param prodId
     * @param comboId
     * @return
     */
    List<ComboProdSku> listByProdIdAndComboId(Long prodId, Long comboId);
}
