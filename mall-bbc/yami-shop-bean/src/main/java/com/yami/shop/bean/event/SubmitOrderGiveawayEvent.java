/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.bean.event;

import com.yami.shop.bean.model.Order;
import com.yami.shop.bean.model.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 提交订单赠品的事件
 * @author LGH
 */
@Data
@AllArgsConstructor
public class SubmitOrderGiveawayEvent {
    /**
     * 完整的订单信息
     */
    private List<Order> orders;

    private List<OrderItem> orderItems;

}
