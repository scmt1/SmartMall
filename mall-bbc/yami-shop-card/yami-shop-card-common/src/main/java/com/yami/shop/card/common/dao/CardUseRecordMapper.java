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
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yami.shop.card.common.model.Card;
import com.yami.shop.card.common.model.CardUseRecord;
import com.yami.shop.common.util.PageParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 优惠券使用记录
 *
 * @author yami code generator
 * @date 2019-05-15 09:04:57
 */
public interface CardUseRecordMapper extends BaseMapper<CardUseRecord> {


    List<CardUseRecord> getCardUseRecordList(@Param("cardNumber") String cardNumber, @Param("userId") String userId);

    IPage<CardUseRecord> queryWriteOffCardList(@Param("cardUseRecord") CardUseRecord cardUseRecord, @Param("page") PageParam<Card> page);

    List<CardUseRecord> downloadWriteOffCardList(@Param("cardUseRecord") CardUseRecord cardUseRecord);

    CardUseRecord queryCardUseTotalBalance(String cardCode);

    /**
     * 统计提货卡(券)消费信息
     * @param cardUseRecord
     * @return
     */
    CardUseRecord statisticCardUseRecord(@Param("cardUseRecord") CardUseRecord cardUseRecord);

    List<CardUseRecord> writeOffDetailStatistic(@Param("cardUseRecord") CardUseRecord cardUseRecord);

    IPage<CardUseRecord> shopWriteOffDetail(PageParam<CardUseRecord> page,@Param("cardUseRecord") CardUseRecord cardUseRecord);

    List<CardUseRecord> getWriteOffDetail(@Param("cardUseRecord") CardUseRecord cardUseRecord);

    void updateCardUseRecordSettlementStatusByIds(@Param("cardUseRecordIds") List<Long> cardUseRecordIds);
}
