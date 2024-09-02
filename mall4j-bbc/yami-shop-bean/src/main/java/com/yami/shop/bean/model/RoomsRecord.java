package com.yami.shop.bean.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.Date;

@Data
@TableName("tz_rooms_record")
public class RoomsRecord {
    @TableId
    @ApiModelProperty("id")
    private Long roomsRecordId;
    private Long roomsId;
    private String orderNumber;
    private Date startTime;
    private Date endTime;
    private Date createTime;
    private Integer personCount;
}
