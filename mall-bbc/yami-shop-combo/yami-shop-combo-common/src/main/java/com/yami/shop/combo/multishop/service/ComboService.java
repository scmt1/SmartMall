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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yami.shop.bean.app.dto.SkuDto;
import com.yami.shop.bean.vo.ComboVO;
import com.yami.shop.combo.multishop.dto.ComboSkuDto;
import com.yami.shop.combo.multishop.model.Combo;
import com.yami.shop.common.util.PageParam;

import java.util.List;
import java.util.Set;

/**
 * 套餐
 *
 * @author LGH
 * @date 2021-11-02 10:32:48
 */
public interface ComboService extends IService<Combo> {

    /**
     * 新增套餐
     * @param combo
     */
    void saveInfo(Combo combo);

    /**
     * 通过套餐id获取套餐与套餐下的商品信息
     * @param comboId
     * @return
     */
    Combo getComboWithProdInfoById(Long comboId);

    /**
     * 更新套餐
     * @param combo
     */
    void updateInfo(Combo combo);

    /**
     * 分页获取套餐信息
     * @param page
     * @param combo
     * @return
     */
    IPage<Combo> pageByParam(PageParam<Combo> page, Combo combo);

    /**
     * 改变套餐状态
     * @param comboId
     * @param shopId
     * @param status
     */
    void changeStatus(Long comboId, Long shopId, Integer status);

    /**
     * 补充商品信息，返回套餐列表
     * @param prodId
     * @return
     */
    List<ComboVO> listComboByProdId(Long prodId);

    /**
     * 查询指定套餐id
     * @param comboId
     * @return
     */
    ComboVO getComboInfoByComboId(Long comboId);

    /**
     * 查询指定套餐id
     * @param comboVO
     * @return
     */
    ComboVO getComboInfo(ComboVO comboVO);

    /**
     * 关闭已经结束的套餐
     */
    void closeComboByEndDate();

    /**
     * 开启到了活动时间未开启的套餐活动
     */
    void enableCombo();

    /**
     * 获取套餐信息（套餐、套餐商品、套餐商品Sku列表）
     *
     * @param comboId
     * @return
     */
    ComboVO getComboVO(Long comboId);

    /**
     * 根据套餐id批量失效套餐活动
     * @param comboList
     */
    void closeComboByComboList(List<Combo> comboList);

    /**
     * 获取指定套餐信息列表
     * @param comboIds
     * @param skuIds
     * @return
     */
    List<ComboSkuDto> listComboByComboIds(Set<Long> comboIds, List<Long> skuIds);

    /**
     * 根据店铺id失效套餐活动
     * @param shopId
     */
    void closeComboByShopId(Long shopId);

    /**
     * 根据主商品id失效套餐活动
     * @param prodId
     */
    void closeComboByMainProdId(Long prodId);

    /**
     * 更新套餐销量
     * @param comboIds
     */
    void updateSoldNum(Set<Long> comboIds);

    /**
     * 获取指定套餐商品的sku列表
     * @param prodId
     * @param comboId
     * @return
     */
    List<SkuDto> getComboSkuInfo(Long prodId, Long comboId);

}
