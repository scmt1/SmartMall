/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.bean.app.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


/**
 * @author Yami
 */
@ApiModel("商品评论数据")
@Data
public class ProdCommDataDto {

    @ApiModelProperty(value = "好评率")
    private Double positiveRating;

    @ApiModelProperty(value = "评论数量")
    private Integer number;

    @ApiModelProperty(value = "好评数")
    private Integer praiseNumber;

    @ApiModelProperty(value = "中评数")
    private Integer secondaryNumber;

    @ApiModelProperty(value = "差评数")
    private Integer negativeNumber;

    @ApiModelProperty(value = "有图数")
    private Integer picNumber;

    @ApiModelProperty(value = "1分评论数")
    private Integer scoreNumber1;

    @ApiModelProperty(value = "2分评论数")
    private Integer scoreNumber2;

    @ApiModelProperty(value = "3分评论数")
    private Integer scoreNumber3;

    @ApiModelProperty(value = "4分评论数")
    private Integer scoreNumber4;

    @ApiModelProperty(value = "5分评论数")
    private Integer scoreNumber5;

}
