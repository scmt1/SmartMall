/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.bean.vo.statistics;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 热卖信息统计
 * @author Yami
 */
@Data
public class OrderStatisticsVO {

    @ApiModelProperty("线上支付订单数")
    private Long onlineOrderNum;

    @ApiModelProperty("线下支付订单数")
    private Long offlineOrderNum;

    @ApiModelProperty("红云支付订单数")
    private Long hyOrderNum;

    @ApiModelProperty("停车订单数")
    private Long carOrderNum;
}
