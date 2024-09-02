package com.yami.shop.bean.pay;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class CarPayInfoDto {

    @ApiModelProperty("车辆支付单号")
    private String carPayNo;

    @ApiModelProperty("车辆订单号")
    private String tradeNo;

    @ApiModelProperty("支付类型(微信支付：WECHAT 支付宝:ALIPAY)")
    private String payType;

    @ApiModelProperty("版本号")
    private Integer version;

    @ApiModelProperty("支付时间")
    private String payTime;

    @ApiModelProperty("支付金额")
    private Integer totalAmount;

    @ApiModelProperty("停车支付金额")
    private Double payTotalAmount;

    @ApiModelProperty("返回码(0 成功，非0失败)")
    private Integer result;

    @ApiModelProperty("返回错误信息")
    private String msg;
}
