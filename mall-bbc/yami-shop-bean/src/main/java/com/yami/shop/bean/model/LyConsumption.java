package com.yami.shop.bean.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@TableName("tz_ly_consumption")
public class LyConsumption {
    @TableId
    @ApiModelProperty("id")
    private Long id;

    @ApiModelProperty("订单编号")
    private String orderNumber;

    @ApiModelProperty("手机号")
    private String mobile;

    @ApiModelProperty("消费时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date consumptionTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @ApiModelProperty("消费金额")
    private Double money;

}
