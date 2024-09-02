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
import com.yami.shop.coupon.common.dao.CouponQrcodeMapper;
import com.yami.shop.coupon.common.model.CouponOnly;
import com.yami.shop.coupon.common.model.CouponQrcode;
import com.yami.shop.coupon.common.service.CouponOnlyService;
import com.yami.shop.coupon.common.service.CouponQrcodeService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author lgh on 2018/12/27.
 */
@Service
@AllArgsConstructor
public class CouponQrcodeServiceImpl extends ServiceImpl<CouponQrcodeMapper, CouponQrcode> implements CouponQrcodeService {

    private final CouponQrcodeMapper couponQrcodeMapper;

    @Override
    public CouponQrcode getCouponQrcodeByQrCode(String qrCode) {
        return couponQrcodeMapper.getCouponQrcodeByQrCode(qrCode);
    }
}
