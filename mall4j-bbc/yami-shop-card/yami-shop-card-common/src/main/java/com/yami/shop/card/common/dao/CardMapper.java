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

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.yami.shop.card.common.model.Card;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.card.common.dto.CardDto;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 优惠券
 *
 * @author lanhai
 */
public interface CardMapper extends BaseMapper<Card> {


    IPage<Card> getPlatformPage(@Param("page") PageParam<Card> page, @Param(Constants.WRAPPER) QueryWrapper<Card> cardQueryWrapper);

    IPage<Card> getPlatformBatchPage(@Param("page") PageParam<Card> page,@Param("card") Card card);

    IPage<Card> batchCardDetailsPage(@Param("page") PageParam<Card> page,@Param("card") Card card);

    List<Card> batchCardDetailsPage(@Param("card") Card card);

    void deleteUserCardByCardId(@Param("userId") String userId, @Param("cardId") Long cardId);

    CardDto getCardUserInfo(Long cardUserId);

    List<String> queryBatchNumList(Long shopId);

    void updateBatchByCardCode(@Param("readData") List<String> readData);

    void makeCardByNumAndBatchNumber(@Param("batchNumbers") List<String> batchNumbers,@Param("shopId") Long shopId);

    void sellCardByNumAndBatchNumber(@Param("batchNumber") String batchNumber,@Param("number") String number,@Param("shopId") Long shopId,@Param("balance") Double balance,@Param("buyUnit") String buyUnit,@Param("buyReason") String buyReason);

    void rechargeCardByNumAndBatchNumber(@Param("batchNumber") String batchNumber,@Param("number") String number, @Param("balance") Double balance,@Param("shopId") Long shopId);

    void freezeCardByNumAndBatchNumber(@Param("batchNumber") String batchNumber,@Param("number") String number,@Param("shopId") Long shopId);

    void soldToUnsoldCardByNumAndBatchNumber(@Param("batchNumber") String batchNumber,@Param("number") String number,@Param("shopId") Long shopId);

    void delBatchCard(@Param("batchNumber") String batchNumber,@Param("shopId") Long shopId);

    void freezeCardByCardIds(@Param("cardIds") List<Long> cardIds,@Param("shopId") Long shopId);

    void updateCardBybatchNumber(@Param("card") Card card);

    List<Card> queryCardSellRecord(@Param("batchNumber") String batchNumber);

    Card getMinNotSoldCardNumber(@Param("batchNumber") String batchNumber);

    Integer getSellCardNum(@Param("batchNumber") String batchNumber,@Param("startNumber") String startNumber,@Param("endNumber") String endNumber);

    Integer getCardNumByNumber(@Param("batchNumber") String batchNumber,@Param("startNumber") String startNumber,@Param("endNumber") String endNumber);

    Card getCardInfoByCardCode(@Param("cardCode") String cardCode);

    Card batchInfoStatistic(@Param("card") Card card);

    Card getBuyCardMaxInfo();

    /**
     * 改变提货卡的状态为已失效
     *
     * @param now 时间
     */
    void changeCardStatus(@Param("now") Date now);
}
