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
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yami.shop.bean.vo.ComboVO;
import com.yami.shop.combo.multishop.dto.ComboSkuDto;
import com.yami.shop.combo.multishop.model.Combo;
import com.yami.shop.common.util.PageParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * 套餐
 *
 * @author LGH
 * @date 2021-11-02 10:32:48
 */
public interface ComboMapper extends BaseMapper<Combo> {

    /**
     * 通过套餐id获取套餐与套餐下的商品信息
     *
     * @param comboId
     * @return
     */
    Combo getComboWithProdInfoById(@Param("comboId") Long comboId);

    /**
     * 分页获取套餐信息
     *
     * @param page
     * @param combo
     * @return
     */
    IPage<Combo> pageByParam(@Param("page") PageParam<Combo> page, @Param("combo") Combo combo);

    /**
     * 根据套餐id列表获取套餐信息
     *
     * @param prodId
     * @param lang
     * @return
     */
    List<ComboVO> listComboByProdId(@Param("prodId") Long prodId, @Param("lang") Integer lang);

    /**
     * 获取套餐信息（套餐、套餐商品、套餐商品Sku列表）
     *
     * @param comboId
     * @return
     */
    ComboVO getComboInfoByComboId(@Param("comboId") Long comboId);

    /**
     * 获取指定套餐信息列表
     *
     * @param comboIds
     * @param skuIds
     * @param lang
     * @return
     */
    List<ComboSkuDto> listComboByComboIds(@Param("comboIds") Set<Long> comboIds, @Param("skuIds") List<Long> skuIds, @Param("lang") Integer lang);

    /**
     * 更新套餐销量
     * @param comboIds
     */
    void updateSoldNum(@Param("comboIds") Set<Long> comboIds);

    /**
     * 获取正常的活动数目
     * @param comboIds
     * @return
     */
    int countNormalCombo(@Param("comboIds")Set<Long> comboIds);

    /**
     * 获取本该开始的而未开始的套餐id
     * @return
     */
    List<Combo> notStartComboIdButStart();

//    /**
//     * 批量修改套餐状态
//     * @param comboId
//     * @param status
//     */
//    void batchUpdateComboStatus(List<Long> comboId,Integer status);
}
