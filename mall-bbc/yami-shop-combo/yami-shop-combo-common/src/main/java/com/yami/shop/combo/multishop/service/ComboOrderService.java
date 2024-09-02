/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.combo.multishop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yami.shop.combo.multishop.model.ComboOrder;

import java.util.List;

/**
 * 套餐订单表
 *
 * @author LGH
 * @date 2021-12-07 09:45:48
 */
public interface ComboOrderService extends IService<ComboOrder> {

    /**
     * 根据订单编号列表获取套餐订单
     * @param orderNumberList
     * @return
     */
    List<ComboOrder> listByOrderNumberList(List<String> orderNumberList);
}
