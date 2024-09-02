package com.yami.shop.bean.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

@Data
@TableName("tz_activity")
public class Activity {
    @TableId
    @ApiModelProperty("id")
    private Long id;

    private String title;

    private String address;

    private String sponsor;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    private String content;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    private Integer delFlag;

    private String type;

    private String qrCode;

    private String logo;

    private Integer personCount;

    private Integer stocks;

    private Integer isShop;

    private Long shopId;

    private Integer isRelease;

    private Integer activityType;

    private Integer isTimeSign;

    @TableField(exist = false)
    private Integer totalCount;

    @TableField(exist = false)
    private Integer signCount;

    @TableField(exist = false)
    private Integer isUserSign;

    @TableField(exist = false)
    private Integer isUserCheckIn;

    @TableField(exist = false)
    private String shopName;

    @TableField(exist = false)
    private List<ActivitySign> activitySigns;

}
