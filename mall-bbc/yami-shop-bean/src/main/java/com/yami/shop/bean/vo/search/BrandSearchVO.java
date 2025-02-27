/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.bean.vo.search;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author FrozenWatermelon
 */
@Data
public class BrandSearchVO {

    @ApiModelProperty(value = "品牌名称")
    private String brandName;

    @ApiModelProperty(value = "品牌id")
    private Long brandId;

    @ApiModelProperty(value = "品牌图片")
    private String brandImg;
}
