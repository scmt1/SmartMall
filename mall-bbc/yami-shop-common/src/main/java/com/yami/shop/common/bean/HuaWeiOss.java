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
import lombok.EqualsAndHashCode;

/**
 * OBS配置信息
 * @author LGH
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class HuaWeiOss extends SwitchBaseModel {

    private String  bucketName;

    private String  accessKeyId;

    private String  secretAccessKey;

    private String  endpoint;
}
