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

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * 商品分析概况参数
 */
/**
 * @author Yami
 */
@Data
public class ProdAnalysisSurveyParam {

    /**
     * 时间类型 1今日实时 2 近7天 3 近30天 4自然日 5自然月
     */
    @ApiModelProperty("时间类型 1今日实时 2 近7天 3 近30天 4自然日 5自然月")
    private Integer dateType;
    /**
     * 开始时间
     */
    @ApiModelProperty("开始时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;
    /**
     * 结束时间
     */
    @ApiModelProperty("结束时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;
    /**
     * 时间
     */
    @ApiModelProperty("前端不传，后台使用字段")
    private Date dateTime;

    /**
     * 店铺id
     */
    @ApiModelProperty("前端不传，后台使用字段")
    private Long shopId;
}
