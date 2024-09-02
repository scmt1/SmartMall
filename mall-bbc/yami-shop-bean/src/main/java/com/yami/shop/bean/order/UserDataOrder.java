/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.bean.order;
/**
 * 取消订单先后顺序
 * @author LGH
 */
public interface UserDataOrder {

    /**
     * 没有任何的顺序
     */
    int DEFAULT = 0;

    /**
     * 统计用户的一些数据，排在DEFAULT后面
     */
    int USER = 100;
}
