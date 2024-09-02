/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.bean.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author Yami
 */
@Data
@TableName("tz_registr")
public class Registr implements Serializable {

    @TableId
    @ApiModelProperty("挂单id")
    private Long registrId;

    @ApiModelProperty(value = "店铺id")
    private Long shopId;

    @ApiModelProperty(value = "挂单流水号")
    private String registrNumber;

    @ApiModelProperty(value = "用户ID")
    private String userId;

    @ApiModelProperty(value = "挂单时间")
    private Date registrDate;

    @ApiModelProperty(value = "总价")
    private Double actualTotal;

    @ApiModelProperty(value = "挂单项")
    @TableField(exist = false)
    private List<RegistrItem> registrItems;
}
