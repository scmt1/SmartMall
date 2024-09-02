package com.yami.shop.bean.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;
import java.util.List;

@Data
@TableName("tz_lucky_draw")
public class LuckyDraw {
    @TableId
    @ApiModelProperty("id")
    private Long drawId;

    @ApiModelProperty(value = "抽奖名称")
    private String drawName;

    @ApiModelProperty(value = "活动说明")
    private String activityDescription;

    @ApiModelProperty(value = "抽奖类型(1：积分抽奖)")
    private Integer drawType;

    @ApiModelProperty(value = "参与次数")
    private Integer joinFrequency;

    @ApiModelProperty(value = "开始时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @ApiModelProperty(value = "结束时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    @ApiModelProperty(value = "消耗积分")
    private Long points;

    @ApiModelProperty(value = "参与条件( 0：全部会员 1：普通会员 2：付费会员)")
    private Integer joinType;

    @ApiModelProperty(value = "中奖概率")
    private Double winningRate;

    @ApiModelProperty(value = "未中奖提示")
    private String noWinningDesc;

    @ApiModelProperty(value = "是否显示中奖名单(0：显示，1：不显示)")
    private Integer isShowWinner;

    @ApiModelProperty(value = "状态(0：未开始，1：开始，2：结束)")
    private Integer status;

    @ApiModelProperty(value = "奖品列表")
    @TableField(exist = false)
    private List<LuckyPrize> prizes;
}
