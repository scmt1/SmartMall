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
import com.yami.shop.bean.model.Giveaway;
import com.yami.shop.common.util.PageParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * 赠品
 *
 * @author LGH
 * @date 2021-11-08 13:29:16
 */
public interface GiveawayMapper extends BaseMapper<Giveaway> {

    /**
     * 分页获取赠品信息
     *
     * @param page
     * @param giveaway
     * @param lang
     * @return
     */
    IPage<Giveaway> pageByParam(@Param("page") PageParam<Giveaway> page, @Param("giveaway") Giveaway giveaway, @Param("lang") Integer lang);

    /**
     * 根据id获取赠品信息
     *
     * @param giveawayId
     * @param lang
     * @return
     */
    Giveaway getInfoById(@Param("giveawayId") Long giveawayId, @Param("lang") Integer lang);

    /**
     * 根据赠送商品id，获取主商品id列表
     *
     * @param prodId
     * @return
     */
    List<Long> listMainProdIdByGiveawayProdId(@Param("prodId") Long prodId);

    /**
     * 获取正常的活动数目
     * @param giveawayIds
     * @return
     */
    int countNormalGiveaway(@Param("giveawayIds") Set<Long> giveawayIds);
}
