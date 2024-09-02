/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.card.common.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yami.shop.card.common.model.CardShop;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 优惠券
 *
 * @author lanhai
 */
public interface CardShopMapper extends BaseMapper<CardShop> {


    List<CardShop> queryCardShopList(@RequestParam("cardId") Long cardId);

    List<CardShop> getConfigNoUseCardShopList(@Param("shopIds") List<Long> shopIds);

    List<CardShop> getNoUseCardShopList(@Param("shopIds") List<Long> shopIds);
}
