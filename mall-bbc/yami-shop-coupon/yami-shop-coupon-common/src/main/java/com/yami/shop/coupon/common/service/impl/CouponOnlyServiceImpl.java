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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.shop.common.util.PageAdapter;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.coupon.common.dao.CouponOnlyMapper;
import com.yami.shop.coupon.common.model.Coupon;
import com.yami.shop.coupon.common.model.CouponOnly;
import com.yami.shop.coupon.common.service.CouponOnlyService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author lgh on 2018/12/27.
 */
@Service
@AllArgsConstructor
public class CouponOnlyServiceImpl extends ServiceImpl<CouponOnlyMapper, CouponOnly> implements CouponOnlyService {

    @Autowired
    private CouponOnlyMapper couponOnlyMapper;

    @Override
    public IPage<CouponOnly> getPlatformPage(PageParam<CouponOnly> page) {
        List<CouponOnly> platformPage = couponOnlyMapper.getPlatformPage(new PageAdapter(page));
        page.setRecords(platformPage);
        page.setTotal(couponOnlyMapper.countPlatform());
        return page;
    }

    @Override
    public List<CouponOnly> queryGroupListByCouponId(Long couponId) {
        return couponOnlyMapper.queryGroupListByCouponId(couponId);
    }

    @Override
    public List<Coupon> getCouponByGroupNum(String groupNum) {
        return couponOnlyMapper.getCouponByGroupNum(groupNum);
    }
}
