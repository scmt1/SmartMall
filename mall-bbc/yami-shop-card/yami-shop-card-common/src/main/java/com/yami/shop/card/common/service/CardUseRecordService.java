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
import com.yami.shop.card.common.model.CardUseRecord;
import com.yami.shop.common.util.PageParam;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author lgh on 2018/12/27.
 */
public interface CardUseRecordService extends IService<CardUseRecord> {

    List<CardUseRecord> getCardUseRecordList(String cardNumber, String userId);

    IPage<CardUseRecord> queryWriteOffCardList(CardUseRecord cardUseRecord, PageParam<Card> page);

    /**
     * 功能描述： 导出
     * @param cardUseRecord 查询参数
     * @param response response参数
     */
    public void download(CardUseRecord cardUseRecord, HttpServletResponse response) ;

    /**
     * 功能描述： 导出
     * @param cardUseRecord 查询参数
     * @param response response参数
     */
    public void shopDownload(CardUseRecord cardUseRecord, HttpServletResponse response) ;

    CardUseRecord queryCardUseTotalBalance(String cardCode);

    /**
     * 统计提货卡(券)消费信息
     * @param cardUseRecord
     * @return
     */
    CardUseRecord statisticCardUseRecord(CardUseRecord cardUseRecord);

    List<CardUseRecord> writeOffDetailStatistic(CardUseRecord cardUseRecord);

    IPage<CardUseRecord> shopWriteOffDetail(PageParam<CardUseRecord> page,CardUseRecord cardUseRecord);

    /**
     * 功能描述： 导出店铺核销提货卡(券)数据
     * @param cardUseRecord 查询参数
     * @param response response参数
     */
    public void shopDownloadByShopId(CardUseRecord cardUseRecord, HttpServletResponse response);

    void updateCardUseRecordSettlementStatusByIds(List<Long> cardUseRecordIds);
}
