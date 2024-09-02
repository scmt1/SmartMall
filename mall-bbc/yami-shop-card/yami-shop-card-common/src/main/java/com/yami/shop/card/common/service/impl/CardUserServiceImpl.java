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
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.shop.card.common.dao.CardUserMapper;
import com.yami.shop.card.common.dto.CardUserDto;
import com.yami.shop.card.common.model.CardUser;
import com.yami.shop.card.common.service.CardUserService;
import com.yami.shop.common.i18n.I18nMessage;
import com.yami.shop.common.i18n.LanguageEnum;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.common.util.PoiExcelUtil;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author lgh on 2018/12/27.
 */
@Service
@AllArgsConstructor
public class CardUserServiceImpl extends ServiceImpl<CardUserMapper, CardUser> implements CardUserService {

    @Autowired
    private CardUserMapper cardUserMapper;

    @Override
    public List<CardUserDto> getCardUserList(CardUser cardUser) {
        return cardUserMapper.getCardUserList(cardUser);
    }

    @Override
    public int updateBalance(Long cardUserId, Double reduceAmount) {
        return cardUserMapper.updateBalance(cardUserId, reduceAmount);
    }

    @Override
    public IPage<CardUser> getCardUserPage(PageParam<CardUser> page, CardUser cardUser) {
        return cardUserMapper.getCardUserPage(page,cardUser);
    }

    @Override
    public void downloadBuyRecord(CardUser cardUser, HttpServletResponse response) {
        List<CardUser> cardUsers = cardUserMapper.downloadBuyRecord(cardUser);
        if (CollUtil.isEmpty(cardUsers)) {
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
        for (CardUser re : cardUsers) {
            int col = -1;
            // 卡名称
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getCardTitle());
            // 卡号
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getCardNumber());
            // 购买人手机号
            if(StringUtils.isNotBlank(re.getUserMobile())){
                PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getUserMobile().replaceAll("(\\d{3})\\d{4}(\\d{4})","$1****$2"));
            }else{
                PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getUserMobile());
            }
            // 原始金额
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getCardAmount());
            // 剩余金额
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getBalance());
            // 购买时间
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getReceiveTime());
            // 有效期
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            String userStartTime = formatter.format(re.getUserStartTime());
            String userEndTime = formatter.format(re.getUserEndTime());
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, userStartTime + "至" + userEndTime);
            // 状态
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getStatus() == 0 ? "失效" : "有效");
            ++rowIndex;
        }
        PoiExcelUtil.writeExcel(response, writer);
    }

    @Override
    public void updateCardUserStatusByTime(Date now) {
        cardUserMapper.updateCardUserStatusByTime(now);
    }

    @Override
    public List<CardUser> cardSendMessage() {
        return cardUserMapper.cardSendMessage();
    }

    private void setColumnWidth(Sheet sheet) {
        // 卡名称
        sheet.setColumnWidth(0, 20 * 256);
        // 卡号
        sheet.setColumnWidth(1, 20 * 256);
        //购买人手机号
        sheet.setColumnWidth(2, 20 * 256);
        // 原始金额
        sheet.setColumnWidth(3, 20 * 256);
        // 剩余金额
        sheet.setColumnWidth(4, 20 * 256);
        // 购买时间
        sheet.setColumnWidth(5, 20 * 256);
        // 有效期
        sheet.setColumnWidth(6, 50 * 256);
        // 状态
        sheet.setColumnWidth(7, 20 * 256);
    }

    /**
     * 导出excel列（中文）
     */
    private final static String[] EXPORT_COL_CN = {
            "卡名称","卡号","购买人手机号", "原始金额", "剩余金额", "购买时间", "有效期", "状态"
    };
    /**
     * 导出excel列（英文）
     */
    private final static String[] EXPORT_COL_EN = {
            "Card Name","Card Number","Buy Mobile", "Original Amount", "Remaining Amount", "Buy Time", "Expiration Date", "Status"
    };
}

