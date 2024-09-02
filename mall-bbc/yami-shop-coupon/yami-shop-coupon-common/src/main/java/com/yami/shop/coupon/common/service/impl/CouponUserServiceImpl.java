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
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.shop.bean.model.CouponAppConnect;
import com.yami.shop.bean.param.*;
import com.yami.shop.common.i18n.I18nMessage;
import com.yami.shop.common.i18n.LanguageEnum;
import com.yami.shop.common.util.*;
import com.yami.shop.coupon.common.dao.CouponUseRecordMapper;
import com.yami.shop.coupon.common.dao.CouponUserMapper;
import com.yami.shop.coupon.common.model.CouponUseRecord;
import com.yami.shop.coupon.common.model.CouponUser;
import com.yami.shop.coupon.common.service.CouponUserService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.util.*;


/**
 * @author lgh on 2018/12/27.
 */
@Service
public class CouponUserServiceImpl extends ServiceImpl<CouponUserMapper, CouponUser> implements CouponUserService {

    @Autowired
    private CouponUserMapper couponUserMapper;

    @Autowired
    private CouponUseRecordMapper couponUseRecordMapper;

//    @Override
//    public List<CouponUser> getCouponAndCouponUserByCouponUserIds(List<Long> couponUserIds) {
//        if (CollectionUtil.isEmpty(couponUserIds)) {
//            return Collections.emptyList();
//        }
//        return couponUserMapper.getCouponAndCouponUserByCouponUserIds(couponUserIds);
//    }

    @Override
    public void deleteUnValidTimeCoupons(Date date) {
        couponUserMapper.deleteUnValidTimeCoupons(date);
    }

    @Override
    public void updateStatusByTime(Date now) {
        couponUserMapper.updateStatusByTime(now);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(String userId, String orderNumber) {

        //查询订单所使用的优惠券列表
        List<CouponUseRecord> couponUseRecordList = couponUseRecordMapper.selectList(new LambdaQueryWrapper<CouponUseRecord>()
                .eq(CouponUseRecord::getUserId, userId)
                .like(CouponUseRecord::getOrderNumber, orderNumber));
        if (CollectionUtils.isEmpty(couponUseRecordList)) {
            return;
        }
        List<Long> couponUseRecordIdList = Lists.newArrayList();
        List<Long> couponUserIds = Lists.newArrayList();
        for (CouponUseRecord couponUseRecord : couponUseRecordList) {
            String[] split = couponUseRecord.getOrderNumber().split(StrUtil.COMMA);
            // 订单号集合
            StringBuilder orderNumbers = new StringBuilder(100);
            // 如果大于1则是平台优惠券，并且在多个店铺使用时
            if(split.length > 1){
                for (String orderNumStr : split) {
                    // 判断是不是当前的订单编号
                    if(StrUtil.equals(orderNumStr,orderNumber)){
                        continue;
                    }
                    orderNumbers.append(orderNumStr).append(StrUtil.COMMA);
                }
                orderNumbers.deleteCharAt(orderNumbers.length() - 1);
                // 修改订单编号信息，在进行修改后结束本次循环
                couponUseRecord.setOrderNumber(orderNumbers.toString());
                couponUseRecordMapper.updateById(couponUseRecord);
                continue;
            }
            couponUseRecordIdList.add(couponUseRecord.getCouponUseRecordId());
            couponUserIds.add(couponUseRecord.getCouponUserId());
        }
        if(CollectionUtil.isNotEmpty(couponUseRecordIdList)) {
            //删除优惠券使用记录
            couponUseRecordMapper.deleteBatchIds(couponUseRecordIdList);
        }
        if(CollectionUtil.isNotEmpty(couponUserIds)) {
            //修改用户优惠券状态为可用状态
            couponUserMapper.batchUpdateUserCouponStatus(1, couponUserIds);
        }
    }

    @Override
    public List<CouponAnalysisParam> getCouponAnalysis(ProdEffectParam param) {
        List<CouponAnalysisParam> resList = new ArrayList<>();
        List<DateParam> everyDays = DateUtils.findEveryDays(param.getStartTime(), param.getEndTime());
        for (DateParam everyDay : everyDays) {
            CouponAnalysisParam res = new CouponAnalysisParam();
            param.setStartTime(everyDay.getStartTime());
            param.setEndTime(everyDay.getEndTime());
            res.setCurrentDay(DateUtils.dateToStrYmd(param.getStartTime()));
            // 领取次数
            res.setTakeNum(couponUserMapper.countTakeNum(null,null, param));
            // TODO 验证次数
            // 微商城使用次数
            res.setUseNum(couponUserMapper.countTakeNum(null,2, param));
            // TODO 分享次数
            resList.add(res);
        }
        return resList;
    }

    @Override
    public Integer countMemberGetCoupon(CustomerReqParam param) {
        return couponUserMapper.countMemberGetCoupon(param);
    }

    @Override
    public Integer countMemberCouponByParam(MemberReqParam param) {
        return couponUserMapper.countMemberCouponByParam(param);
    }

    @Override
    public CouponUserParam getCouponCountByUserId(String userId) {
        // 用户未使用的并且是未过期的优惠券
        Date now = new Date();
        Integer usables = couponUserMapper.countCouponUsableNums(userId,now);
        // 已使用
        Integer usedNums = couponUserMapper.countCouponUsedNums(userId);
        // 失效(已过期)
        Integer couponExpiredNums = couponUserMapper.countCouponExpiredNums(userId);
        CouponUserParam param = new CouponUserParam();
        param.setCouponUsableNums(getIntegerNum(usables));
        param.setCouponUsedNums(getIntegerNum(usedNums));
        param.setCouponExpiredNums(getIntegerNum(couponExpiredNums));
        return param;
    }

    @Override
    public Page<CouponAnalysisParam> getCouponAnalysisParamByDate(Page page, ProdEffectParam param) {
        Page<CouponAnalysisParam> paramByDate = couponUserMapper.getCouponAnalysisParamByDate(page, param);
        for (CouponAnalysisParam couponAnalysisParam : paramByDate.getRecords()) {
            couponAnalysisParam.setUseNum(couponUserMapper.countUseNum(couponAnalysisParam.getCouponId(),param));
        }
        return paramByDate;
    }

    @Override
    public CouponUser getCouponUserByQrCode(String couponUserNumber) {
        return couponUserMapper.getCouponUserByQrCode(couponUserNumber);
    }

    @Override
    public IPage<CouponUser> getCouponUserPage(PageParam<CouponUser> page, CouponUser couponUser) {
        Page<CouponUser> pages = new Page<>();
        pages.setCurrent(page.getCurrent());
        pages.setSize(page.getSize());
        List<CouponUser> couponUsers = couponUserMapper.getCouponUserPage(new PageAdapter(page), couponUser);
        pages.setRecords(couponUsers);
        Integer total = couponUserMapper.countGetCouponUserPageByParam(couponUser);
        pages.setTotal(total);
        return pages;
    }

    @Override
    public List<CouponAppConnect> queryNotUseCouponUserData() {
        return couponUserMapper.queryNotUseCouponUserData();
    }

    @Override
    public List<CouponUser> couponSendMessage() {
        return couponUserMapper.couponSendMessage();
    }

    @Override
    public void downloadCouponUser(CouponUser couponUser, HttpServletResponse response) {
        List<CouponUser> couponUsers = couponUserMapper.getCouponUserList(couponUser);
        if (CollUtil.isEmpty(couponUsers)) {
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
        writer.merge(maxColNum - 1, Objects.equals(I18nMessage.getLang(), LanguageEnum.LANGUAGE_ZH_CN.getLang()) ? "优惠券领取记录" : "Coupon verification record");
        // 列名
        writer.writeRow(exportCol);
        int rowIndex = 2;
        for (CouponUser re : couponUsers) {
            int col = -1;
            // 优惠券名称
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getCouponName());
            // 领取人
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getNickName());
            // 领取人手机号
            if(StringUtils.isNotBlank(re.getUserMobile())){
                PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getUserMobile().replaceAll("(\\d{3})\\d{4}(\\d{4})","$1****$2"));
            }else{
                PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getUserMobile());
            }
            // 领取时间
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getReceiveTime());
            // 状态
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getStatus() == 0 ? "失效" : re.getStatus() == 1 ? "有效" : "使用过");
            // 用券开始时间
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getUserStartTime());
            // 用券结束时间
            PoiExcelUtil.mergeIfNeed(writer, rowIndex, rowIndex, ++col, col, re.getUserEndTime());
            ++rowIndex;
        }
        PoiExcelUtil.writeExcel(response, writer);
    }

    private Integer getIntegerNum(Integer value) {
        if (Objects.isNull(value)){
            value = 0;
        }
        return value;
    }

    private void setColumnWidth(Sheet sheet) {
        // 优惠券名称
        sheet.setColumnWidth(0, 20 * 256);
        // 领取人
        sheet.setColumnWidth(1, 20 * 256);
        // 领取人手机号
        sheet.setColumnWidth(2, 20 * 256);
        // 领取时间
        sheet.setColumnWidth(3, 30 * 256);
        // 状态
        sheet.setColumnWidth(4, 20 * 256);
        // 用券开始时间
        sheet.setColumnWidth(5, 30 * 256);
        // 用券结束时间
        sheet.setColumnWidth(6, 30 * 256);
    }

    /**
     * 导出excel列（中文）
     */
    private final static String[] EXPORT_COL_CN = {
            "优惠券名称", "领取人", "领取人手机号", "领取时间", "状态", "用券开始时间", "用券结束时间"
    };
    /**
     * 导出excel列（英文）
     */
    private final static String[] EXPORT_COL_EN = {
            "Coupon Name", "User", "User Mobile", "Collection time", "Status", "Start time", "End time"
    };
}
