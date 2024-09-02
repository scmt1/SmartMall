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
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.shop.card.common.dao.CardUseRecordMapper;
import com.yami.shop.card.common.model.Card;
import com.yami.shop.card.common.model.CardUseRecord;
import com.yami.shop.card.common.service.CardUseRecordService;
import com.yami.shop.common.i18n.I18nMessage;
import com.yami.shop.common.i18n.LanguageEnum;
import com.yami.shop.common.util.PageAdapter;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.common.util.PoiExcelUtil;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author lgh on 2018/12/27.
 */
@Service
@AllArgsConstructor
public class CardUseRecordServiceImpl extends ServiceImpl<CardUseRecordMapper, CardUseRecord> implements CardUseRecordService {

    @Autowired
    private CardUseRecordMapper cardUseRecordMapper;


    @Override
    public List<CardUseRecord> getCardUseRecordList(String cardNumber, String userId) {
        return cardUseRecordMapper.getCardUseRecordList(cardNumber, userId);
    }

    @Override
    public IPage<CardUseRecord> queryWriteOffCardList(CardUseRecord cardUseRecord, PageParam<Card> page) {
        return cardUseRecordMapper.queryWriteOffCardList(cardUseRecord, page);
    }

    @Override
    public void download(CardUseRecord cardUseRecord, HttpServletResponse response) {
        List<CardUseRecord> list = cardUseRecordMapper.downloadWriteOffCardList(cardUseRecord);

        if (CollUtil.isEmpty(list)) {
            return;
        }
        ExcelWriter writer = ExcelUtil.getBigWriter();
        final List<String> exportCol = Arrays.asList(Objects.equals(I18nMessage.getLang(), LanguageEnum.LANGUAGE_ZH_CN.getLang()) ? EXPORT_COL_CN : EXPORT_COL_EN);
        // 最大列数量
        int maxColNum = exportCol.size();

        Sheet sheet = writer.getSheet();
        // 设置列宽
        this.setColumnWidth(sheet);
        // 标题
        writer.merge(maxColNum - 1, Objects.equals(I18nMessage.getLang(), LanguageEnum.LANGUAGE_ZH_CN.getLang()) ? "提货卡/券核销记录" : "Card verification record");
        // 列名
        writer.writeRow(exportCol);
        int rowIndex = 2;
        for (CardUseRecord re : list) {
            int col = -1;
            // 卡名称
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getCardTitle());
            // 卡号
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getCardCode());
            // 订单号
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getOrderNumber());
            // 使用人
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getNickName());
            // 使用人手机号
            if(StringUtils.isNotBlank(re.getUserMobile())){
                PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getUserMobile().replaceAll("(\\d{3})\\d{4}(\\d{4})","$1****$2"));
            }else{
                PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getUserMobile());
            }
            // 减免金额
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getAmount());
            // 店铺承担金额
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getShopAmount() != null ? re.getShopAmount() : 0);
            // 物业承担金额
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getWyAmount() != null ? re.getWyAmount() : 0);
            // 使用时间
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getUseTime());
            // 核销店铺
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getShopName());
            // 团卡类型
            if(re.getBuyCardType() == 0){
                PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, "工会团卡(券)");
            }else if(re.getBuyCardType() == 1){
                PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, "个人团卡");
            }
            // 结算状态
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getSettlement() == 0 ? "未结算" : "已结算");
            ++rowIndex;
        }
        PoiExcelUtil.writeExcel(response, writer);
    }

    @Override
    public void shopDownload(CardUseRecord cardUseRecord, HttpServletResponse response) {
        List<CardUseRecord> list = cardUseRecordMapper.downloadWriteOffCardList(cardUseRecord);

        if (CollUtil.isEmpty(list)) {
            return;
        }
        ExcelWriter writer = ExcelUtil.getBigWriter();
        final List<String> exportCol = Arrays.asList(Objects.equals(I18nMessage.getLang(), LanguageEnum.LANGUAGE_ZH_CN.getLang()) ? SHOP_EXPORT_COL_CN : SHOP_EXPORT_COL_EN);
        // 最大列数量
        int maxColNum = exportCol.size();

        Sheet sheet = writer.getSheet();
        // 设置列宽
        this.setShopColumnWidth(sheet);
        // 标题
        writer.merge(maxColNum - 1, Objects.equals(I18nMessage.getLang(), LanguageEnum.LANGUAGE_ZH_CN.getLang()) ? "提货卡(券)核销记录" : "Card verification record");
        // 列名
        writer.writeRow(exportCol);
        int rowIndex = 2;
        for (CardUseRecord re : list) {
            int col = -1;
            // 卡名称
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getCardTitle());
            // 订单号
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getOrderNumber());
            // 使用人
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getNickName());
            // 使用人手机号
            if(StringUtils.isNotBlank(re.getUserMobile())){
                PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getUserMobile().replaceAll("(\\d{3})\\d{4}(\\d{4})","$1****$2"));
            }else{
                PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getUserMobile());
            }
            // 减免金额
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getAmount());
            // 店铺承担金额
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getShopAmount() != null ? re.getShopAmount() : 0);
            // 物业承担金额
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getWyAmount() != null ? re.getWyAmount() : 0);
            // 使用时间
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getUseTime());
            // 核销店铺
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getShopName());
            // 团卡类型
            if(re.getBuyCardType() == 0){
                PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, "工会团卡(券)");
            }else if(re.getBuyCardType() == 1){
                PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, "个人团卡");
            }
            // 结算状态
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getSettlement() == 0 ? "未结算" : "已结算");
            // 核销人员
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getEmployeeNickName());
            ++rowIndex;
        }
        PoiExcelUtil.writeExcel(response, writer);
    }

    @Override
    public CardUseRecord queryCardUseTotalBalance(String cardCode) {
        return cardUseRecordMapper.queryCardUseTotalBalance(cardCode);
    }

    @Override
    public CardUseRecord statisticCardUseRecord(CardUseRecord cardUseRecord) {
        return cardUseRecordMapper.statisticCardUseRecord(cardUseRecord);
    }

    @Override
    public List<CardUseRecord> writeOffDetailStatistic(CardUseRecord cardUseRecord) {
        return cardUseRecordMapper.writeOffDetailStatistic(cardUseRecord);
    }

    @Override
    public IPage<CardUseRecord> shopWriteOffDetail(PageParam<CardUseRecord> page, CardUseRecord cardUseRecord) {
        return cardUseRecordMapper.shopWriteOffDetail(page,cardUseRecord);
    }

    @Override
    public void shopDownloadByShopId(CardUseRecord cardUseRecord, HttpServletResponse response) {
        List<CardUseRecord> list = cardUseRecordMapper.getWriteOffDetail(cardUseRecord);
        if (CollUtil.isEmpty(list)) {
            return;
        }
        ExcelWriter writer = ExcelUtil.getBigWriter();
        final List<String> exportCol = Arrays.asList(Objects.equals(I18nMessage.getLang(), LanguageEnum.LANGUAGE_ZH_CN.getLang()) ? EXPORT_COL_CN : EXPORT_COL_EN);
        // 最大列数量
        int maxColNum = exportCol.size();

        Sheet sheet = writer.getSheet();
        // 设置列宽
        this.setColumnWidth(sheet);
        // 标题
        writer.merge(maxColNum - 1, Objects.equals(I18nMessage.getLang(), LanguageEnum.LANGUAGE_ZH_CN.getLang()) ? "优惠券核销记录" : "Coupon verification record");
        // 列名
        writer.writeRow(exportCol);
        int rowIndex = 2;
        for (CardUseRecord re : list) {
            int col = -1;
            // 卡名称
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getCardTitle());
            // 卡号
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getCardCode());
            // 订单号
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getOrderNumber());
            // 使用人
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getNickName());
            // 使用人手机号
            if(StringUtils.isNotBlank(re.getUserMobile())){
                PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getUserMobile().replaceAll("(\\d{3})\\d{4}(\\d{4})","$1****$2"));
            }else{
                PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getUserMobile());
            }
            // 减免金额
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getAmount());
            // 店铺承担金额
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getShopAmount() != null ? re.getShopAmount() : 0);
            // 物业承担金额
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getWyAmount() != null ? re.getWyAmount() : 0);
            // 使用时间
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getUseTime());
            // 核销店铺
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getShopName());
            // 团卡类型
            if(re.getBuyCardType() == 0){
                PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, "工会团卡(券)");
            }else if(re.getBuyCardType() == 1){
                PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, "个人团卡");
            }
            // 结算状态
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getSettlement() == 0 ? "未结算" : "已结算");
            ++rowIndex;
        }
        PoiExcelUtil.writeExcel(response, writer);
    }

    @Override
    public void updateCardUseRecordSettlementStatusByIds(List<Long> cardUseRecordIds) {
        cardUseRecordMapper.updateCardUseRecordSettlementStatusByIds(cardUseRecordIds);
    }

    private void setColumnWidth(Sheet sheet) {
        // 卡名称
        sheet.setColumnWidth(0, 20 * 256);
        // 卡号
        sheet.setColumnWidth(1, 20 * 256);
        // 订单号
        sheet.setColumnWidth(2, 20 * 256);
        // 使用人
        sheet.setColumnWidth(3, 20 * 256);
        //使用人手机号
        sheet.setColumnWidth(4, 20 * 256);
        // 金额
        sheet.setColumnWidth(5, 20 * 256);
        // 店铺承担金额
        sheet.setColumnWidth(6, 20 * 256);
        // 物业承担金额
        sheet.setColumnWidth(7, 20 * 256);
        // 使用时间
        sheet.setColumnWidth(8, 20 * 256);
        // 核销店铺
        sheet.setColumnWidth(9, 20 * 256);
        // 团卡类型
        sheet.setColumnWidth(10, 20 * 256);
        // 结算状态
        sheet.setColumnWidth(11, 20 * 256);
    }

    private void setShopColumnWidth(Sheet sheet) {
        // 卡名称
        sheet.setColumnWidth(0, 20 * 256);
        // 订单号
        sheet.setColumnWidth(1, 20 * 256);
        // 使用人
        sheet.setColumnWidth(2, 20 * 256);
        //使用人手机号
        sheet.setColumnWidth(3, 20 * 256);
        // 金额
        sheet.setColumnWidth(4, 20 * 256);
        // 店铺承担金额
        sheet.setColumnWidth(5, 20 * 256);
        // 物业承担金额
        sheet.setColumnWidth(6, 20 * 256);
        // 使用时间
        sheet.setColumnWidth(7, 20 * 256);
        // 核销店铺
        sheet.setColumnWidth(8, 20 * 256);
        // 团卡类型
        sheet.setColumnWidth(9, 20 * 256);
        // 结算状态
        sheet.setColumnWidth(10, 20 * 256);
        // 核销人员
        sheet.setColumnWidth(11, 20 * 256);
    }

    /**
     * 导出excel列（中文）
     */
    private final static String[] EXPORT_COL_CN = {
            "卡名称","卡号","订单号","使用人", "使用人手机号", "金额(元)","店铺承担金额(元)","物业承担金额(元)", "使用时间", "核销店铺", "团卡类型", "结算状态"
    };
    /**
     * 导出excel列（英文）
     */
    private final static String[] EXPORT_COL_EN = {
            "Card Name","Card Number","Order number", "Use Name", "Use Mobile", "Amount","shop amount","wy amount", "Use Time", "Verification store", "Group Card Type", "Settlement status"
    };

    /**
     * 导出excel列（中文）
     */
    private final static String[] SHOP_EXPORT_COL_CN = {
            "卡名称","订单号", "使用人", "使用人手机号", "金额","店铺承担金额(元)","物业承担金额(元)", "使用时间", "核销店铺", "团卡类型", "结算状态", "核销人员"
    };
    /**
     * 导出excel列（英文）
     */
    private final static String[] SHOP_EXPORT_COL_EN = {
            "Card Name","Order number", "Use Name", "Use Mobile", "Amount","shop amount","wy amount", "Use Time", "Verification store", "Group Card Type", "Settlement status","Verification personnel"
    };
}

