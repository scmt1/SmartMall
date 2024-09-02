/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.card.common.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yami.shop.card.common.model.Card;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.card.common.dto.CardDto;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

/**
 * @author lgh on 2018/12/27.
 */
public interface CardService extends IService<Card> {


    IPage<Card> getPlatformPage(PageParam<Card> page, Card card);

    IPage<Card> getPlatformBatchPage(PageParam<Card> page, Card card);

    IPage<Card> batchCardDetailsPage(PageParam<Card> page, Card card);

    void deleteByCardId(Long cardId);

    void deleteUserCardByCardId(String userId, Long cardId);

    CardDto getCardUserInfo(Long cardUserId);

    List<String> queryBatchNumList(Long shopId);

    void updateBatchByCardCode(List<String> readData);

    void makeCardByNumAndBatchNumber(List<String> batchNumbers,Long shopId);

    void sellCardByNumAndBatchNumber(String batchNumber, String number, Long shopId,Double balance,String buyUnit,String buyReason);

    void rechargeCardByNumAndBatchNumber(String batchNumber, String number, Double balance,Long shopId);

    void freezeCardByNumAndBatchNumber(String batchNumber, String number,Long shopId);

    void soldToUnsoldCardByNumAndBatchNumber(String batchNumber, String number,Long shopId);

    void delBatchCard(String batchNumber,Long shopId);

    void freezeCardByCardIds(List<Long> cardIds,Long shopId);

    void updateCardBybatchNumber(Card card);

    List<Card> getCardList(Card card);

    List<Card> queryCardSellRecord(String batchNumber);

    Card getMinNotSoldCardNumber(String batchNumber);

    Integer getSellCardNum(String batchNumber,String startNumber,String endNumber);

    Integer getCardNumByNumber(String batchNumber,String startNumber,String endNumber);

    Card getCardInfoByCardCode(String cardCode);

    Card batchInfoStatistic(Card card);

    Card getBuyCardMaxInfo();

    /**
     * 定时任务，改变提货卡的状态为已失效
     *
     * @param now 时间
     */
    void changeCardStatus(Date now);

    /**
     * 功能描述： 导出店铺核销提货卡(券)数据
     * @param card 查询参数
     * @param response response参数
     */
    public void downLoadCardRecord(Card card, HttpServletResponse response);

    /**
     * 功能描述： 导出批次信息数据
     * @param card 查询参数
     * @param response response参数
     */
    public void downloadBatchInfo(Card card, HttpServletResponse response);
}
