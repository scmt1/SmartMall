/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.bean.param;

import lombok.Data;

import java.util.List;

/**
 * @author Yami
 */
@Data
public class CustomerDealRespParam {

    /**
     * 全部成交客户
     */
    private CustomerDealParam allDeal;
    /**
     * 新成交客户数
     */
    private CustomerDealParam newDeal;
    /**
     * 老成交客户数
     */
    private CustomerDealParam oldDeal;

    private List<CustomerDealTrendParam> listDeal;
}
