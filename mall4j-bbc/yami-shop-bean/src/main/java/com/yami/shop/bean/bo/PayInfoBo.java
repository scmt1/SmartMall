/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.bean.bo;

import lombok.Data;

/**
 * @author Yami
 */
@Data
public class PayInfoBo {

    /**
     * 商城支付单号
     */
    private String payNo;

    /**
     * 第三方订单流水号
     */
    private String bizPayNo;

    private Boolean isPaySuccess;

    private String successString;

    /**
     * 第三方订单订单号
     */
    private String bizOrderNo;

}
