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

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;


/**
 * 店铺的头信息
 * @author LGH
 */
@Data
@ApiModel("店铺搜索信息")
public class ShopSearchParam {

    @ApiModelProperty(value = "店铺名称")
    private String shopName;

    @ApiModelProperty(value = "店铺id列表-根据店铺id获取店铺列表")
    private List<Long> shopIds;

    @ApiModelProperty(value = "楼层")
    private String floor;

    @ApiModelProperty(value = "行业类型")
    private String industryType;

    @ApiModelProperty(value = "是否开启小票积分(0：不开启 1：开启)")
    private Integer isTicketScore;
}
