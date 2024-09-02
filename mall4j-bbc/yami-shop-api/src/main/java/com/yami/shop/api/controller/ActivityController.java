/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.api.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yami.shop.bean.model.Activity;
import com.yami.shop.bean.model.ActivityUser;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.security.api.util.SecurityUtils;
import com.yami.shop.service.ActivityService;
import com.yami.shop.service.ActivityUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author lgh on 2018/10/26.
 */
@RestController
@Api(tags = "活动")
@RequestMapping("/p/activity")
public class ActivityController {

    @Autowired
    private ActivityService activityService;
    @Autowired
    private ActivityUserService activityUserService;

    @GetMapping("/activityList")
    @ApiOperation(value = "活动查询", notes = "活动查询")
    public ServerResponseEntity<List<Activity>> activityList(@RequestParam(value = "isShop") Integer isShop, @RequestParam(value = "shopId",required = false) Long shopId) {
        List<Activity> activityList = activityService.selectActivityList(isShop,shopId);
        return ServerResponseEntity.success(activityList);
    }

    @GetMapping("/info/{id}")
    @ApiOperation(value = "获取信息", notes = "获取信息")
    public ServerResponseEntity<Activity> info(@PathVariable("id") Long id) {
        String userId = SecurityUtils.getUser().getUserId();
        Activity activity = activityService.getById(id);
        //查询参与人数
        QueryWrapper<ActivityUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("activity_id",id);
        int count = activityUserService.count(queryWrapper);
        activity.setTotalCount(count);
        //查询当前用户活动报名签到
        QueryWrapper<ActivityUser> activityUserQueryWrapper = new QueryWrapper<>();
        activityUserQueryWrapper.lambda().eq(ActivityUser::getActivityId,activity.getId());
        activityUserQueryWrapper.lambda().eq(ActivityUser::getUserId,userId);
        ActivityUser activityUser = activityUserService.getOne(activityUserQueryWrapper);
        activity.setIsUserSign(0);
        activity.setIsUserCheckIn(0);
        if(activityUser != null){
            activity.setIsUserSign(1);
            if(activityUser.getStatus() != null && activityUser.getStatus() == 1){
                activity.setIsUserCheckIn(1);
            }
        }
        if(activity == null) {
            return ServerResponseEntity.showFailMsg("活动信息不存在！");
        }
        return ServerResponseEntity.success(activity);
    }

    @GetMapping("/getShopActivityInfo")
    @ApiOperation(value = "获取店铺活动信息", notes = "获取店铺活动信息")
    public ServerResponseEntity<Activity> getShopActivityInfo(@RequestParam(value = "shopId") Long shopId) {
        QueryWrapper<Activity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(Activity::getIsShop,1);
        queryWrapper.lambda().eq(Activity::getShopId,shopId);
        queryWrapper.lambda().eq(Activity::getIsRelease,1);
        queryWrapper.lambda().eq(Activity::getDelFlag,0);
        Activity activity = activityService.getOne(queryWrapper);
        return ServerResponseEntity.success(activity);
    }
}
