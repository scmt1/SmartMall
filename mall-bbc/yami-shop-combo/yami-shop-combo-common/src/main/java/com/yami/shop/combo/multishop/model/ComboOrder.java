/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.combo.multishop.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 套餐订单表
 *
 * @author LGH
 * @date 2021-12-07 09:45:48
 */
@Data
@TableName("tz_combo_order")
public class ComboOrder implements Serializable{
    private static final long serialVersionUID = 1L;

    /**
     * 拼团订单id
     */
    @TableId
    private Long comboOrderId;
    /**
     * 店铺id
     */
    private Long shopId;
    /**
     * 套餐id
     */
    private Long comboId;
    /**
     * user_id
     */
    private String userId;
    /**
     * 套餐数量
     */
    private Integer comboNum;
    /**
     * 支付金额
     */
    private Double payPrice;
    /**
     * 订单编号
     */
    private String orderNumber;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 更新时间
     */
    private Date updateTime;
    /**
     * 状态(0:待支付、1:支付成功)
     */
    private Integer status;

}
