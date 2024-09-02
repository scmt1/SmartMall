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
import com.yami.shop.bean.vo.ComboProdVO;
import com.yami.shop.bean.vo.ComboVO;
import com.yami.shop.combo.multishop.model.ComboProd;

import java.util.List;

/**
 * 套餐商品项
 *
 * @author LGH
 * @date 2021-11-02 10:35:08
 */
public interface ComboProdService extends IService<ComboProd> {

    /**
     * 批量保存
     * @param skuList
     * @param comboId
     */
    void insertBatch(List<ComboProd> skuList, Long comboId);

    /**
     * 根据商品id获取套餐列表[有缓存]
     * @param prodId
     * @param lang
     * @return
     */
    List<ComboVO> listCombo(Long prodId, Integer lang);

    /**
     * 根据商品id删除缓存
     * @param prodId
     */
    void removeComboListCache(Long prodId);

    /**
     * 根据商品id列表删除缓存
     * @param prodIds
     */
    void removeComboListCacheByProdIds(List<Long> prodIds);

    /**
     * 根据商品id获取商品套餐信息
     * @param prodId 商品id
     * @return
     */
    List<ComboProdVO> listComboProdByProdId(Long prodId);
}
