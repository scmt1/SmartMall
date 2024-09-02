package com.yami.shop.bean.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@TableName("tz_activity_sign")
public class ActivitySign {
    @TableId
    private Long activitySignId;
    /**
     * 活动ID
     */
    private Long activityId;
    /**
     * 报名开始时间
     */
    @ApiModelProperty(value = "报名开始时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    /**
     * 报名结束时间
     */
    @ApiModelProperty(value = "报名结束时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    /**
     * 报名总数
     */
    private Integer signTotal;

    /**
     * 报名剩余数量
     */
    private Integer stocks;
}
