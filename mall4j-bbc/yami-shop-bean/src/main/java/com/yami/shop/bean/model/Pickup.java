package com.yami.shop.bean.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@TableName("tz_pickup")
public class Pickup {
    @TableId
    @ApiModelProperty("id")
    private Long id;

    private String orderNumber;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    private Date pickupTime;
    private Integer status;
    private Long shopId;
    private String prodPic;
    private String pickupCode;
}
