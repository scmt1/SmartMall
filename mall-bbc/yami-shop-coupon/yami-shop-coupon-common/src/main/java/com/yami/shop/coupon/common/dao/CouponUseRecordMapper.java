/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.coupon.common.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yami.shop.common.util.PageAdapter;
import com.yami.shop.coupon.common.model.CouponUseRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 优惠券使用记录
 *
 * @author yami code generator
 * @date 2019-05-15 09:04:57
 */
public interface CouponUseRecordMapper extends BaseMapper<CouponUseRecord> {

    /**
     * 批量插入用户优惠券使用记录
     *
     * @param couponUseRecordsList 用户优惠券使用记录集合
     */
    void addCouponUseRecode(@Param("couponUseRecordsList") List<CouponUseRecord> couponUseRecordsList);

    /**
     * 批量更新商家券、平台券
     *
     * @param status          状态
     * @param orderNumberList 订单编号list
     */
    void batchUpdateRecordByStatusAndOrderNums(@Param("status") int status, @Param("orderNumberList") List<String> orderNumberList);

    /**
     * 获取订单中使用的优惠券
     *
     * @param orderNumber 订单编号
     * @return
     */
    List<CouponUseRecord> listByOrderNumber(@Param("orderNumber") String orderNumber);

    /**
     * 获取全部订单号字符串
     * @param orderNumber
     * @return
     */
    List<CouponUseRecord> getOrderNumberContact(@Param("orderNumber") String orderNumber);

    /**
     * 根据id, 批量更新商家券、平台券
     *
     * @param status
     * @param ids
     */
    void batchUpdateRecordStatusByStatusAndIds(@Param("status") int status, @Param("ids") List<Long> ids);

    /**
     * 获取需要解锁的、用户所领取的优惠券
     *
     * @param orderNumber 订单id
     * @return 用户所领取的优惠券id
     */
    List<Long> listCouponUserIdsByOrderNumber(@Param("orderNumber") String orderNumber);

    /**
     * 将锁定状态标记为已解锁
     *
     * @param couponUserIds 用户所领取的优惠券id
     * @return
     */
    int unLockByIds(@Param("couponUserIds") List<Long> couponUserIds);

    List<CouponUseRecord> writeOffRecordPage(@Param("adapter") PageAdapter adapter,@Param("couponUseRecord") CouponUseRecord couponUseRecord);

    List<CouponUseRecord> shopWriteOffDetail(@Param("adapter") PageAdapter adapter,@Param("couponUseRecord") CouponUseRecord couponUseRecord);

    List<CouponUseRecord> getWriteOffRecordList(@Param("couponUseRecord") CouponUseRecord couponUseRecord);

    List<CouponUseRecord> getShopWriteOffRecordList(@Param("couponUseRecord") CouponUseRecord couponUseRecord);

    /**
     * 根据参数，统计查询到的使用记录总数
     *
     * @param adapter 分页参数
     * @param couponUseRecord   条件查询参数
     * @return 数量
     */
    Integer countGetWriteOffRecordPageByParam(@Param("adapter") PageAdapter adapter, @Param("couponUseRecord") CouponUseRecord couponUseRecord);

    /**
     * 根据参数，统计查询到的使用记录总数
     * @param couponUseRecord   条件查询参数
     * @return 数量
     */
    Integer countGetShopWriteOffRecord(@Param("couponUseRecord") CouponUseRecord couponUseRecord);

    List<CouponUseRecord> writeOffDetail(@Param("couponUseRecord") CouponUseRecord couponUseRecord);

    /**
     * 统计会员卡消费信息
     *
     * @param couponUseRecord
     * @return
     */
    CouponUseRecord statisticCouponUse(@Param("couponUseRecord") CouponUseRecord couponUseRecord);
}
