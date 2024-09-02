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

import java.io.Serializable;

/**
 * @author Yami
 */
@Data
public class TransportParam implements Serializable {
    private static final long serialVersionUID = 1876655654053364580L;
    /**
     * 运费模板id
     */
    private Long transportId;

    /**
     * 运费模板名称
     */
    private String transName;

}
