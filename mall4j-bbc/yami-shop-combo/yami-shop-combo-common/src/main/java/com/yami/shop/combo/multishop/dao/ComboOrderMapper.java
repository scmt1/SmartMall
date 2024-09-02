/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.combo.multishop.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yami.shop.combo.multishop.model.ComboOrder;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 套餐订单表
 *
 * @author LGH
 * @date 2021-12-07 09:45:48
 */
public interface ComboOrderMapper extends BaseMapper<ComboOrder> {

    /**
     * 根据订单编号获取信息
     * @param orderNumberList 订单编号
     * @return
    */
    List<ComboOrder> listByOrderNumberList(@Param("orderNumberList") List<String> orderNumberList);
}
