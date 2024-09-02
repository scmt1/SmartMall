package com.yami.shop.bean.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.Date;

@Data
@TableName("tz_rooms")
public class Rooms {
    @TableId
    @ApiModelProperty("id")
    private Long roomsId;
    private String roomsName;
    private Integer roomsStatus;
    private Date createTime;
    private Long shopId;
    private String remark;
    private Integer personCount;

    private String qrCode;
}
