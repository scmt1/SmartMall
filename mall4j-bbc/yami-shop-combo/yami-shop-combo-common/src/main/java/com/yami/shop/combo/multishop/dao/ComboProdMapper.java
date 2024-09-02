/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.combo.multishop.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yami.shop.bean.vo.ComboProdVO;
import com.yami.shop.combo.multishop.model.ComboProd;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 套餐商品项
 *
 * @author LGH
 * @date 2021-11-02 10:35:08
 */
public interface ComboProdMapper extends BaseMapper<ComboProd> {

    /**
     * 批量保存
     * @param comboProdList
     * @param comboId
     */
    void insertBatch(@Param("comboProdList") List<ComboProd> comboProdList, @Param("comboId") Long comboId);

    /**
     * 根据商品id获取商品套餐信息
     * @param prodId
     * @return
     */
    List<ComboProdVO> listComboProdByProdId(@Param("prodId") Long prodId);
}
