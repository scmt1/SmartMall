package com.yami.shop.bean.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@TableName("tz_car_order")
public class CarOrder {

    @ApiModelProperty("订单号")
    @TableId
    private String carId;

    @ApiModelProperty("订单号")
    private String carNumber;

    @ApiModelProperty("车牌号")
    private String carno;

    @ApiModelProperty("价格(单位：元)")
    private Double price;

    @ApiModelProperty("停车时长")
    private Integer parktime;

    @ApiModelProperty("如果使用优惠券，优惠的时间")
    private Integer deductTime;

    @ApiModelProperty("如果使用优惠券，优惠的金额")
    private Integer deductPrice;

    @ApiModelProperty("用户id")
    private String userId;

    @ApiModelProperty("订单号")
    @TableField(exist=false)
    private String tradeno;

    /**
     * 订购时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 是否已经支付，1：已经支付过，0：，没有支付过
     */
    private Integer isPayed;

    @ApiModelProperty("车辆类型(1蓝牌车2黄牌车4新能源小车8新能源黄牌车101警车102救护车103消防车)")
    @TableField(exist=false)
    private Integer cartype;

    @ApiModelProperty("车辆注册类型(0 临时车1 注册车2 月卡车3 Vip车4 黑名单车 5共组车 6储值车 11限时车)")
    @TableField(exist=false)
    private Integer regtype;

    @ApiModelProperty("开始生效时间戳")
    @TableField(exist=false)
    private Integer starttime;

    @ApiModelProperty("过期时间戳")
    @TableField(exist=false)
    private Integer endtime;

    @ApiModelProperty("车主电话")
    @TableField(exist=false)
    private String phone;
}
