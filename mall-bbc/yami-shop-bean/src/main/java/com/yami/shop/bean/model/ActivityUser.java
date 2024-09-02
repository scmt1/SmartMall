package com.yami.shop.bean.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@TableName("tz_activity_user")
public class ActivityUser {
    @TableId
    private Long activityUserId;
    /**
     * 活动ID
     */
    private Long activityId;
    /**
     * 用户ID
     */
    private String userId;
    /**
     * 报名时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date receiveTime;
    /**
     * 是否删除
     */
    private Integer isDelete;
    /**
     * 签到时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date signTime;

    /**
     * 签到状态
     */
    private Integer status;

    /**
     * 分段报名ID
     */
    private Long activitySignId;
}
