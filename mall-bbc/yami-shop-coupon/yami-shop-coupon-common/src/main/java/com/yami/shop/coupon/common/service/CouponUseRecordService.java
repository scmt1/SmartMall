/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.coupon.common.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yami.shop.bean.model.Order;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.coupon.common.dto.CouponRecordDTO;
import com.yami.shop.coupon.common.model.CouponUseRecord;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 优惠券使用记录
 *
 * @author yami code generator
 * @date 2019-05-15 09:04:57
 */
public interface CouponUseRecordService extends IService<CouponUseRecord> {

    /**
     * 添加优惠券使用记录
     *
     * @param couponUseList
     */
    void addCouponUseRecode(List<CouponUseRecord> couponUseList);

    /**
     * 通过订单列表和批量修改记录状态
     * @param status 状态
     * @param orderNumberList 订单编号列表
     */
    void batchUpdateRecordByStatusAndOrderNums(int status, List<String> orderNumberList);

    /**
     * 锁定订优惠券状态
     * @param lockCouponParams 订单id和优惠券id关联信息
     * @return void
     */
    void lockCoupon(List<CouponRecordDTO> lockCouponParams, String userId);

    /**
     * 根据订单号进行优惠券解锁
     * @param order 订单信息
     */
    void unlockCoupon(Order order);

    IPage<CouponUseRecord> writeOffRecordPage(PageParam<CouponUseRecord> page,CouponUseRecord couponUseRecord);

    IPage<CouponUseRecord> shopWriteOffDetail(PageParam<CouponUseRecord> page,CouponUseRecord couponUseRecord);

    /**
     * 功能描述： 导出
     * @param couponUseRecord 查询参数
     * @param response response参数
     */
    public void download(CouponUseRecord couponUseRecord, HttpServletResponse response) ;

    /**
     * 功能描述： 店铺核销平台优惠券记录导出
     * @param couponUseRecord 查询参数
     * @param response response参数
     */
    public void shopDownload(CouponUseRecord couponUseRecord, HttpServletResponse response) ;

    List<CouponUseRecord> writeOffDetail(CouponUseRecord couponUseRecord);

    /**
     * 统计优惠券核销信息
     * @param couponUseRecord
     * @return
     */
    CouponUseRecord statisticCouponUse(CouponUseRecord couponUseRecord);
}
