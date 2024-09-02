/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.combo.multishop.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.shop.combo.multishop.dao.ComboOrderMapper;
import com.yami.shop.combo.multishop.model.ComboOrder;
import com.yami.shop.combo.multishop.service.ComboOrderService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 套餐订单表
 *
 * @author LGH
 * @date 2021-12-07 09:45:48
 */
@Service
@AllArgsConstructor
public class ComboOrderServiceImpl extends ServiceImpl<ComboOrderMapper, ComboOrder> implements ComboOrderService {

    private final ComboOrderMapper comboOrderMapper;

    @Override
    public List<ComboOrder> listByOrderNumberList(List<String> orderNumberList) {
        if (CollUtil.isEmpty(orderNumberList)) {
            return null;
        }
        return comboOrderMapper.listByOrderNumberList(orderNumberList);
    }
}
