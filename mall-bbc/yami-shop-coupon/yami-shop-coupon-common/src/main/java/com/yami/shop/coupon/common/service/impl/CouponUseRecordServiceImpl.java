/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.coupon.common.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.yami.shop.bean.enums.ShopStatus;
import com.yami.shop.bean.enums.ShopType;
import com.yami.shop.bean.model.Order;
import com.yami.shop.bean.param.ShopExportParam;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.i18n.I18nMessage;
import com.yami.shop.common.i18n.LanguageEnum;
import com.yami.shop.common.response.ResponseEnum;
import com.yami.shop.common.util.DateUtils;
import com.yami.shop.common.util.PageAdapter;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.common.util.PoiExcelUtil;
import com.yami.shop.coupon.common.constants.UserCouponRecordStatus;
import com.yami.shop.coupon.common.constants.UserCouponStatus;
import com.yami.shop.coupon.common.dao.CouponUseRecordMapper;
import com.yami.shop.coupon.common.dao.CouponUserMapper;
import com.yami.shop.coupon.common.dto.CouponRecordDTO;
import com.yami.shop.coupon.common.model.CouponUseRecord;
import com.yami.shop.coupon.common.service.CouponUseRecordService;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * 优惠券使用情况
 *
 * @author yami code generator
 * @date 2019-05-15 09:04:57
 */
@Service
@AllArgsConstructor
public class CouponUseRecordServiceImpl extends ServiceImpl<CouponUseRecordMapper, CouponUseRecord> implements CouponUseRecordService {

    private final CouponUseRecordMapper couponUseRecordMapper;
    private final CouponUserMapper couponUserMapper;

    @Override
    public void addCouponUseRecode(List<CouponUseRecord> couponUseList) {
        couponUseRecordMapper.addCouponUseRecode(couponUseList);
    }

    @Override
    public void batchUpdateRecordByStatusAndOrderNums(int status, List<String> orderNumberList) {
        // 更新商家券、平台券
        couponUseRecordMapper.batchUpdateRecordByStatusAndOrderNums(status, orderNumberList);
    }

    @Override
    public void lockCoupon(List<CouponRecordDTO> lockCouponParams, String userId) {
        List<CouponUseRecord> couponLocks = Lists.newArrayList();
        List<Long> userCouponIds = new ArrayList<>();
        Set<Long> orderIds = new HashSet<>();
        Date now = new Date();
        for (CouponRecordDTO lockCouponParam : lockCouponParams) {
            CouponUseRecord couponLock = new CouponUseRecord();
            couponLock.setAmount(lockCouponParam.getReduceAmount());
            // 获取用户优惠券id
            couponLock.setCouponUserId(lockCouponParam.getCouponUserId());
            userCouponIds.add(lockCouponParam.getCouponUserId());
            couponLock.setUserId(userId);
            couponLock.setOrderNumber(lockCouponParam.getOrderNumbers());
            couponLock.setWriteOffShopId(lockCouponParam.getWriteOffShopId());
            if (couponLock.getOrderNumber().contains(StrUtil.COMMA)) {
                for (String orderId : couponLock.getOrderNumber().split(StrUtil.COMMA)) {
                    orderIds.add(Long.valueOf(orderId));
                }
            } else {
                orderIds.add(Long.valueOf(couponLock.getOrderNumber()));
            }
            //优惠券记录为冻结状态
            couponLock.setStatus(UserCouponRecordStatus.FREEZE.getValue());
            couponLock.setUseTime(now);
            couponLocks.add(couponLock);
        }
        // 批量保存使用记录
        saveBatch(couponLocks);
        // 批量将用户的优惠券变成已使用状态
        couponUserMapper.batchUpdateUserCouponStatus(UserCouponStatus.USED.getValue(), userCouponIds);

//        // 发送消息一个小时后解锁优惠券(包括哪些订单)
//        SendStatus sendStatus = couponMqTemplate.syncSend(RocketMqConstant.COUPON_UNLOCK_TOPIC, new GenericMessage<>(orderIds), RocketMqConstant.TIMEOUT, RocketMqConstant.CANCEL_ORDER_DELAY_LEVEL + 1).getSendStatus();
//        if (!Objects.equals(sendStatus,SendStatus.SEND_OK)) {
//            // 消息发不出去就抛异常，发的出去无所谓
//            throw new YamiShopBindException(ResponseEnum.EXCEPTION);
//        }
    }

    @Override
    public void unlockCoupon(Order order) {
        // 该订单没有下单成功，或订单已取消，赶紧解锁库存
        //查询订单所使用的优惠券列表
        List<CouponUseRecord> couponUseRecordList = couponUseRecordMapper.selectList(new LambdaQueryWrapper<CouponUseRecord>()
                .eq(CouponUseRecord::getUserId, order.getUserId())
                .like(CouponUseRecord::getOrderNumber, order.getOrderNumber()));
        List<Long> couponUserIds = Lists.newArrayList();
        for (CouponUseRecord couponUseRecord : couponUseRecordList) {
            couponUserIds.add(couponUseRecord.getCouponUserId());
        }
        if (CollectionUtil.isEmpty(couponUserIds)) {
            return;
        }
        // 批量将用户的优惠券变成未使用状态
        couponUserMapper.batchUpdateUserCouponStatus(UserCouponStatus.EFFECTIVE.getValue(), couponUserIds);

        // 将锁定状态标记为已解锁
        int updateStatus = couponUseRecordMapper.unLockByIds(couponUserIds);
        if (updateStatus == 0) {
            throw new YamiShopBindException(ResponseEnum.EXCEPTION);
        }
    }

    @Override
    public IPage<CouponUseRecord> shopWriteOffDetail(PageParam<CouponUseRecord> page,CouponUseRecord couponUseRecord) {
        Page<CouponUseRecord> pages = new Page<>();
        pages.setCurrent(page.getCurrent());
        pages.setSize(page.getSize());
        List<CouponUseRecord> couponUseRecords = couponUseRecordMapper.shopWriteOffDetail(new PageAdapter(page), couponUseRecord);
        pages.setRecords(couponUseRecords);
        Integer total = couponUseRecordMapper.countGetShopWriteOffRecord(couponUseRecord);
        pages.setTotal(total);
        return pages;
    }

    @Override
    public IPage<CouponUseRecord> writeOffRecordPage(PageParam<CouponUseRecord> page,CouponUseRecord couponUseRecord) {
        Page<CouponUseRecord> pages = new Page<>();
        pages.setCurrent(page.getCurrent());
        pages.setSize(page.getSize());
        List<CouponUseRecord> couponUseRecords = couponUseRecordMapper.writeOffRecordPage(new PageAdapter(page), couponUseRecord);
        pages.setRecords(couponUseRecords);
        Integer total = couponUseRecordMapper.countGetWriteOffRecordPageByParam(new PageAdapter(page), couponUseRecord);
        pages.setTotal(total);
        return pages;
    }

    @Override
    public void shopDownload(CouponUseRecord couponUseRecord, HttpServletResponse response) {
        List<CouponUseRecord> list = couponUseRecordMapper.getShopWriteOffRecordList(couponUseRecord);

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
        for (CouponUseRecord re : list) {
            int col = -1;
            // 优惠券名称
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getCouponName());
            // 使用人
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getNickName());
            // 订单编号
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getOrderNumber());
            // 减免金额
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getAmount());
            // 使用时间
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getUseTime());
            // 核销店铺
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getShopName());
            ++rowIndex;
        }
        PoiExcelUtil.writeExcel(response, writer);
    }

    @Override
    public void download(CouponUseRecord couponUseRecord, HttpServletResponse response) {
        List<CouponUseRecord> list = couponUseRecordMapper.getWriteOffRecordList(couponUseRecord);

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
        for (CouponUseRecord re : list) {
            int col = -1;
            // 优惠券名称
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getCouponName());
            // 使用人
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getNickName());
            // 订单编号
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getOrderNumber());
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
            ++rowIndex;
        }
        PoiExcelUtil.writeExcel(response, writer);
    }

    @Override
    public List<CouponUseRecord> writeOffDetail(CouponUseRecord couponUseRecord) {
        return couponUseRecordMapper.writeOffDetail(couponUseRecord);
    }

    @Override
    public CouponUseRecord statisticCouponUse(CouponUseRecord couponUseRecord) {
        return couponUseRecordMapper.statisticCouponUse(couponUseRecord);
    }

    private void setColumnWidth(Sheet sheet) {
        // 优惠券名称
        sheet.setColumnWidth(0, 20 * 256);
        // 使用人
        sheet.setColumnWidth(1, 20 * 256);
        // 订单编号
        sheet.setColumnWidth(2, 20 * 256);
        // 减免金额
        sheet.setColumnWidth(3, 20 * 256);
        // 店铺承担金额
        sheet.setColumnWidth(4, 20 * 256);
        // 物业承担金额
        sheet.setColumnWidth(5, 20 * 256);
        // 使用时间
        sheet.setColumnWidth(6, 20 * 256);
        // 核销店铺
        sheet.setColumnWidth(7, 20 * 256);
    }

    /**
     * 导出excel列（中文）
     */
    private final static String[] EXPORT_COL_CN = {
            "优惠券名称", "使用人", "订单编号", "减免金额(元)","店铺承担金额(元)","物业承担金额(元)", "使用时间", "核销店铺"
    };
    /**
     * 导出excel列（英文）
     */
    private final static String[] EXPORT_COL_EN = {
            "Coupon Name", "User", "Order Number", "Deduction amount","shop amount","wy amount", "Use Time", "Verification store"
    };

}
