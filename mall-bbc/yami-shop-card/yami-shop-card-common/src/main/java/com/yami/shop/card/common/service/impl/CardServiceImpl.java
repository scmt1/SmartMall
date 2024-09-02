/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.card.common.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.shop.card.common.model.Card;
import com.yami.shop.card.common.model.CardUseRecord;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.i18n.I18nMessage;
import com.yami.shop.common.i18n.LanguageEnum;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.card.common.dao.CardMapper;
import com.yami.shop.card.common.dao.CardUserMapper;
import com.yami.shop.card.common.dto.CardDto;
import com.yami.shop.card.common.model.CardUser;
import com.yami.shop.card.common.service.CardService;
import com.yami.shop.common.util.PoiExcelUtil;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author lgh on 2018/12/27.
 */
@Service
@AllArgsConstructor
public class CardServiceImpl extends ServiceImpl<CardMapper, Card> implements CardService {

    @Autowired
    private CardMapper cardMapper;

    @Autowired
    private CardUserMapper cardUserMapper;

    @Override
    public IPage<Card> getPlatformPage(PageParam<Card> page, Card card) {
        QueryWrapper<Card> cardQueryWrapper = buildQueryWrapper(card);
        cardQueryWrapper.lambda().orderByDesc(Card::getCreateTime);
        cardQueryWrapper.lambda().orderByDesc(Card::getCardNumber);
        return cardMapper.getPlatformPage(page, cardQueryWrapper);
    }

    @Override
    public IPage<Card> getPlatformBatchPage(PageParam<Card> page, Card card) {
        return cardMapper.getPlatformBatchPage(page, card);
    }

    @Override
    public IPage<Card> batchCardDetailsPage(PageParam<Card> page, Card card) {
        return cardMapper.batchCardDetailsPage(page, card);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByCardId(Long cardId) {
        Integer count = cardUserMapper.selectCount(new LambdaQueryWrapper<CardUser>().eq(CardUser::getCardId, cardId).eq(CardUser::getIsDelete, 0));
        if (count > 0) {
            // 该卡已被领取，删除失败
            throw new YamiShopBindException("该会员卡已被用户领取，删除失败！");
        }

        Card card = cardMapper.selectById(cardId);
        if (Objects.isNull(card)) {
            // 不存在
            throw new YamiShopBindException("该会员卡不存在！");
        }
        cardMapper.deleteById(cardId);
    }

    @Override
    public void deleteUserCardByCardId(String userId, Long cardId) {

        cardMapper.deleteUserCardByCardId(userId, cardId);
    }

    @Override
    public CardDto getCardUserInfo(Long cardUserId) {
        return cardMapper.getCardUserInfo(cardUserId);
    }

    @Override
    public List<String> queryBatchNumList(Long shopId) {
        return cardMapper.queryBatchNumList(shopId);
    }

    @Override
    public void updateBatchByCardCode(List<String> readData) {
        cardMapper.updateBatchByCardCode(readData);
    }

    @Override
    public void makeCardByNumAndBatchNumber(List<String> batchNumbers, Long shopId) {
        cardMapper.makeCardByNumAndBatchNumber(batchNumbers, shopId);
    }

    @Override
    public void sellCardByNumAndBatchNumber(String batchNumber, String number, Long shopId,Double balance,String buyUnit,String buyReason) {
        cardMapper.sellCardByNumAndBatchNumber(batchNumber, number, shopId,balance,buyUnit,buyReason);
    }

    @Override
    public void rechargeCardByNumAndBatchNumber(String batchNumber, String number, Double balance, Long shopId) {
        cardMapper.rechargeCardByNumAndBatchNumber(batchNumber, number, balance, shopId);
    }

    @Override
    public void freezeCardByNumAndBatchNumber(String batchNumber, String number, Long shopId) {
        cardMapper.freezeCardByNumAndBatchNumber(batchNumber,number, shopId);
    }

    @Override
    public void soldToUnsoldCardByNumAndBatchNumber(String batchNumber, String number, Long shopId) {
        cardMapper.soldToUnsoldCardByNumAndBatchNumber(batchNumber,number, shopId);
    }

    @Override
    public void delBatchCard(String batchNumber, Long shopId) {
        cardMapper.delBatchCard(batchNumber, shopId);
    }

    @Override
    public void freezeCardByCardIds(List<Long> cardIds, Long shopId) {
        cardMapper.freezeCardByCardIds(cardIds, shopId);
    }

    @Override
    public void updateCardBybatchNumber(Card card) {
        cardMapper.updateCardBybatchNumber(card);
    }

    @Override
    public List<Card> getCardList(Card card) {
        QueryWrapper<Card> queryWrapper = buildQueryWrapper(card);
        return cardMapper.selectList(queryWrapper);
    }

    @Override
    public List<Card> queryCardSellRecord(String batchNumber) {
        return cardMapper.queryCardSellRecord(batchNumber);
    }

    @Override
    public Card getMinNotSoldCardNumber(String batchNumber) {
        return cardMapper.getMinNotSoldCardNumber(batchNumber);
    }

    @Override
    public Integer getSellCardNum(String batchNumber, String startNumber, String endNumber) {
        return cardMapper.getSellCardNum(batchNumber,startNumber,endNumber);
    }

    @Override
    public Integer getCardNumByNumber(String batchNumber, String startNumber, String endNumber) {
        return cardMapper.getCardNumByNumber(batchNumber,startNumber,endNumber);
    }

    @Override
    public Card getCardInfoByCardCode(String cardCode) {
        return cardMapper.getCardInfoByCardCode(cardCode);
    }

    @Override
    public Card batchInfoStatistic(Card card) {
        return cardMapper.batchInfoStatistic(card);
    }

    @Override
    public Card getBuyCardMaxInfo() {
        return cardMapper.getBuyCardMaxInfo();
    }

    @Override
    public void changeCardStatus(Date now) {
        cardMapper.changeCardStatus(now);
    }

    @Override
    public void downLoadCardRecord(Card card, HttpServletResponse response) {
        QueryWrapper<Card> queryWrapper = buildQueryWrapper(card);
        List<Card> list = cardMapper.selectList(queryWrapper);

        if (CollUtil.isEmpty(list)) {
            return;
        }
        ExcelWriter writer = ExcelUtil.getBigWriter();
        final List<String> exportCol = Arrays.asList(EXPORT_COL_CN);
        // 最大列数量
        int maxColNum = exportCol.size();

        Sheet sheet = writer.getSheet();
        // 设置列宽
        this.setColumnWidth(sheet);
        // 标题
        writer.merge(maxColNum - 1, "提货卡/券记录数据");
        // 列名
        writer.writeRow(exportCol);
        int rowIndex = 2;
        for (Card re : list) {
            int col = -1;
            // 卡/券名称
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getCardTitle());
            // 卡/券号
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getCardCode());
            // 卡/券编号
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getCardNumber());
            // 状态
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col,
                    re.getStatus() == -1 ? "已失效" : re.getStatus() == 0 ? "未制卡(券) " : re.getStatus() == 1 ? "未出售" :
                    re.getStatus() == 2 ? "已出售" : re.getStatus() == 3 ? "已绑定" : re.getStatus() == 4 ? "已冻结" :
                    re.getStatus() == 5 ? "已置换为卡" : re.getStatus() == 6 ? "已核销" : re.getStatus() == 7 ? "已作废" : ""
            );
            // 卡/券金额
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getBalance());
            // 卡类别
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getCardType() == 1 ? "提货卡" : re.getCardType() == 2 ? "实物券" : "");
            // 团卡/券类型
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getBuyCardType() == 0 ? "工会团卡(券)" : re.getBuyCardType() == 1 ? "个人团卡" : "");
            // 有效期
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, DateUtil.format(re.getUserStartTime(), "yyyy-MM-dd") + "到" + DateUtil.format(re.getUserEndTime(), "yyyy-MM-dd"));
            // 购买单位
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getBuyUnit());
            // 出售时间
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getSellTime());
            // 购买事由
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getBuyReason());
            ++rowIndex;
        }
        PoiExcelUtil.writeExcel(response, writer);
    }

    @Override
    public void downloadBatchInfo(Card card, HttpServletResponse response) {
        List<Card> list = cardMapper.batchCardDetailsPage(card);

        if (CollUtil.isEmpty(list)) {
            return;
        }
        ExcelWriter writer = ExcelUtil.getBigWriter();
        final List<String> exportCol = Arrays.asList(EXPORT_COL_BATCH_INFO);
        // 最大列数量
        int maxColNum = exportCol.size();

        Sheet sheet = writer.getSheet();
        // 设置列宽
        this.setBatchInfoColumnWidth(sheet);
        // 标题
        writer.merge(maxColNum - 1, "批次信息记录数据");
        // 列名
        writer.writeRow(exportCol);
        int rowIndex = 2;
        for (Card re : list) {
            int col = -1;
            // 批次号
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getBatchNumber());
            // 卡(券)名称
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getCardTitle());
            // 卡类型
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getCardType() == 1 ? "提货卡" : re.getCardType() == 2 ? "实物券" : "");
            // 购买单位
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getBuyUnit());
            // 状态
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col,
                    re.getStatus() == -1 ? "已失效" : re.getStatus() == 0 ? "未制卡(券) " : re.getStatus() == 1 ? "未出售" :
                            re.getStatus() == 2 ? "已出售" : re.getStatus() == 3 ? "已绑定" : re.getStatus() == 4 ? "已冻结" :
                                    re.getStatus() == 5 ? "已置换为卡" : re.getStatus() == 6 ? "已核销" : ""
            );
            // 批次时间
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getBatchTime());
            // 有效期
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, DateUtil.format(re.getUserStartTime(), "yyyy-MM-dd") + "到" + DateUtil.format(re.getUserEndTime(), "yyyy-MM-dd"));
            // 数量
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getCardNum());
            // 卖出编号
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getSellRecordNum());
            // 卡/券金额
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getBalance());
            // 总金额
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getTotalAmount());
            ++rowIndex;
        }
        PoiExcelUtil.writeExcel(response, writer);
    }


    private QueryWrapper<Card> buildQueryWrapper(Card card) {
        QueryWrapper<Card> cardQueryWrapper = new QueryWrapper<>();
        if (card.getShopId() != null) {
            cardQueryWrapper.lambda().eq(Card::getShopId, card.getShopId());
        }
        if (StringUtils.isNotBlank(card.getCardTitle())) {
            cardQueryWrapper.lambda().like(Card::getCardTitle, card.getCardTitle());
        }
        if (StringUtils.isNotBlank(card.getMobile())) {
            cardQueryWrapper.lambda().like(Card::getMobile, card.getMobile());
        }
        if (StringUtils.isNotBlank(card.getCardCode())) {
            cardQueryWrapper.lambda().like(Card::getCardCode, card.getCardCode());
        }
        if (StringUtils.isNotBlank(card.getCardNumber())) {
            cardQueryWrapper.lambda().eq(Card::getCardNumber, card.getCardNumber());
        }
        if (!Objects.isNull(card.getStatus())) {
            cardQueryWrapper.lambda().eq(Card::getStatus, card.getStatus());
        }
        if (!Objects.isNull(card.getCardType())) {
            cardQueryWrapper.lambda().eq(Card::getCardType, card.getCardType());
        }
        if (StringUtils.isNotBlank(card.getBatchNumber())) {
            cardQueryWrapper.lambda().like(Card::getBatchNumber, card.getBatchNumber());
        }
        if (!Objects.isNull(card.getStartTime())) {
            cardQueryWrapper.lambda().ge(Card::getCreateTime, card.getStartTime());
        }
        if (!Objects.isNull(card.getEndTime())) {
            cardQueryWrapper.lambda().le(Card::getCreateTime, card.getEndTime());
        }
        if (!Objects.isNull(card.getSellStartTime())) {
            cardQueryWrapper.lambda().ge(Card::getSellTime, card.getSellStartTime());
        }
        if (!Objects.isNull(card.getSellEndTime())) {
            cardQueryWrapper.lambda().le(Card::getSellTime, card.getSellEndTime());
        }
        if (!Objects.isNull(card.getBuyCardType())) {
            cardQueryWrapper.lambda().eq(Card::getBuyCardType, card.getBuyCardType());
        }
        if (StringUtils.isNotBlank(card.getBuyUnit())) {
            cardQueryWrapper.lambda().like(Card::getBuyUnit, card.getBuyUnit());
        }

        cardQueryWrapper.lambda().eq(Card::getIsDelete, 0);
        return cardQueryWrapper;
    }

    private void setColumnWidth(Sheet sheet) {
        // 卡/券名称
        sheet.setColumnWidth(0, 20 * 256);
        // 卡/券号
        sheet.setColumnWidth(1, 20 * 256);
        // 卡/券编号
        sheet.setColumnWidth(2, 20 * 256);
        // 状态
        sheet.setColumnWidth(3, 20 * 256);
        // 卡/券金额
        sheet.setColumnWidth(4, 20 * 256);
        // 卡类别
        sheet.setColumnWidth(5, 20 * 256);
        // 团卡/券类型
        sheet.setColumnWidth(6, 20 * 256);
        // 有效期
        sheet.setColumnWidth(7, 40 * 256);
        // 购买单位
        sheet.setColumnWidth(8, 40 * 256);
        // 出售时间
        sheet.setColumnWidth(9, 20 * 256);
        // 购买事由
        sheet.setColumnWidth(10, 20 * 256);
    }

    private void setBatchInfoColumnWidth(Sheet sheet) {
        // 批次号
        sheet.setColumnWidth(0, 20 * 256);
        // 卡(券)名称
        sheet.setColumnWidth(1, 20 * 256);
        // 卡类型
        sheet.setColumnWidth(2, 20 * 256);
        // 购买单位
        sheet.setColumnWidth(3, 20 * 256);
        // 状态
        sheet.setColumnWidth(4, 20 * 256);
        // 批次时间
        sheet.setColumnWidth(5, 40 * 256);
        // 有效期
        sheet.setColumnWidth(6, 40 * 256);
        // 数量
        sheet.setColumnWidth(7, 20 * 256);
        // 卖出编号
        sheet.setColumnWidth(8, 20 * 256);
        // 金额
        sheet.setColumnWidth(9, 20 * 256);
        // 总金额
        sheet.setColumnWidth(10, 20 * 256);
    }

    /**
     * 导出excel列（中文）
     */
    private final static String[] EXPORT_COL_CN = {
            "卡/券名称","卡/券号","卡/券编号","状态", "卡/券金额", "卡类别", "团卡/券类型", "有效期",  "购买单位",  "出售时间",  "购买事由"
    };
    private final static String[] EXPORT_COL_BATCH_INFO = {
            "批次号","卡(券)名称","卡类型","购买单位","状态","批次时间","有效期","数量","卖出编号","金额","总金额"
    };
}

