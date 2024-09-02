/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.common.bean;

import lombok.Data;

/**
 * 系统支付开关配置
 * @author cl
 */
@Data
public class SysPayConfig {

    /**
     *  是否开启支付宝支付
     */
    private Boolean aliPaySwitch;

    /**
     *  是否开启微信支付
     */
    private Boolean wxPaySwitch;

    /**
     *  是否开启余额支付
     */
    private Boolean balancePaySwitch;

    /**
     *  是否开启payPal支付
     */
    private Boolean payPalSwitch;

    /**
     *  是否开启提货卡支付
     */
    private Boolean cardPaySwitch;

}
