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

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 套餐
 *
 * @author LGH
 * @date 2021-11-02 10:32:48
 */
@Data
@TableName("tz_combo")
public class Combo implements Serializable{
    private static final long serialVersionUID = 1L;

    @TableId
    @ApiModelProperty(value = "套餐id")
    private Long comboId;

    @ApiModelProperty(value = "套餐名称")
    @NotBlank(message = "套餐名称不能为空")
    private String name;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "活动开始时间")
    @NotNull(message = "活动开始时间不能为空")
    private Date startTime;

    @ApiModelProperty(value = "活动结束时间")
    @NotNull(message = "活动结束时间不能为空")
    private Date endTime;

    @ApiModelProperty(value = "状态， -1：已删除 0：已失效 1：开启")
    private Integer status;

    @ApiModelProperty(value = "销量")
    private Integer soldNum;

    @ApiModelProperty(value = "套餐价格")
    private Double price;

    @ApiModelProperty(value = "店铺id")
    private Long shopId;

    @ApiModelProperty(value = "主商品id")
    private Long mainProdId;

    @ApiModelProperty(value = "主商品")
    @Valid
    @TableField(exist = false)
    private ComboProd mainProd;

    @ApiModelProperty(value = "搭配商品列表")
    @Valid
    @TableField(exist = false)
    private List<ComboProd> matchingProds;

    @ApiModelProperty(value = "商品数量")
    @TableField(exist = false)
    private Integer prodCount;

    @ApiModelProperty(value = "套餐价")
    @TableField(exist = false)
    private Double matchingPrice;

    @ApiModelProperty("主商品名称")
    @TableField(exist = false)
    private String mainProdName;

    @ApiModelProperty("主商品图片")
    @TableField(exist = false)
    private String mainProdPic;

    @ApiModelProperty("搭配商品名称")
    @TableField(exist = false)
    private String matchProdName;

    @ApiModelProperty("语言")
    @TableField(exist = false)
    private Integer lang;

    @ApiModelProperty("主商品id列表")
    @TableField(exist = false)
    private List<Long> mainProdIdList;

    @ApiModelProperty("搭配商品id列表")
    @TableField(exist = false)
    private List<Long> matchProdIdList;
}
