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
import com.yami.shop.bean.model.Giveaway;
import com.yami.shop.bean.vo.GiveawayVO;
import com.yami.shop.common.util.PageParam;

import java.util.List;

/**
 * 赠品
 *
 * @author LGH
 * @date 2021-11-08 13:29:16
 */
public interface GiveawayService extends IService<Giveaway> {

    /**
     * 保存赠品信息
     * @param giveaway
     */
    void saveInfo(Giveaway giveaway);

    /**
     * 分页获取赠品信息
     * @param page
     * @param giveaway
     * @return
     */
    IPage<Giveaway> pageByParam(PageParam<Giveaway> page, Giveaway giveaway);

    /**
     * 根据id查询赠品信息
     * @param giveawayId
     * @return
     */
    Giveaway getInfoById(Long giveawayId);

    /**
     * 更新赠品信息
     * @param giveaway
     */
    void updateInfo(Giveaway giveaway);

    /**
     * 修改赠品状态
     * @param giveawayId
     * @param shopId
     * @param status
     */
    void changeStatus(Long giveawayId, Long shopId, Integer status);

    /**
     * 根据商品id获取所有赠品商品信息
     * @param prodId
     * @return
     */
    GiveawayVO getGiveawayProdByProdId(Long prodId);

    /**
     * 关闭失效的赠品活动
     */
    void closeGiveawayByEndDate();

    /**
     * 启动未开启的赠品活动
     */
    void startGiveawayActivity();

    /**
     * 根据店铺id关闭赠品活动
     * @param shopId
     */
    void closeGiveawayByShopId(Long shopId);

    /**
     * 根据主商品id关闭赠品活动
     * @param prodId
     */
    void closeGiveawayByMainProdId(Long prodId);

    /**
     * 根据赠送商品id，获取主商品id列表
     * @param prodId
     * @return
     */
    List<Long> listMainProdIdByGiveawayProdId(Long prodId);

    /**
     * 根据商品id获取所有赠品商品及库存信息
     * @param prodId
     * @return
     */
    GiveawayVO getGiveawayProdAndStockByProdId(Long prodId);

    /**
     * 根据赠品批量失效活动
     * @param giveawayList
     */
    void closeGiveawayByGiveawayList(List<Giveaway> giveawayList);
}
