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
import com.yami.shop.common.util.PageParam;
import com.yami.shop.coupon.common.model.Coupon;
import com.yami.shop.coupon.common.model.CouponOnly;

import java.util.List;

/**
 * @author lgh on 2018/12/27.
 */
public interface CouponOnlyService extends IService<CouponOnly> {

    IPage<CouponOnly> getPlatformPage(PageParam<CouponOnly> page);

    List<CouponOnly> queryGroupListByCouponId(Long couponId);

    List<Coupon> getCouponByGroupNum(String groupNum);
}
