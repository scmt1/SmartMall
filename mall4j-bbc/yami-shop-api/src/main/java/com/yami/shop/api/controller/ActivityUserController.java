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

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yami.shop.bean.model.Activity;
import com.yami.shop.bean.model.ActivitySign;
import com.yami.shop.bean.model.ActivityUser;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.security.api.util.SecurityUtils;
import com.yami.shop.service.ActivityService;
import com.yami.shop.service.ActivitySignService;
import com.yami.shop.service.ActivityUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author lgh on 2018/10/26.
 */
@RestController
@Api(tags = "活动报名")
@RequestMapping("/p/activityUser")
@Slf4j
public class ActivityUserController {
    @Autowired
    private ActivityUserService activityUserService;

    @Autowired
    private ActivityService activityService;
    @Autowired
    private ActivitySignService activitySignService;

//    @PostMapping("/addActivityUser")
//    @ApiOperation(value = "活动报名", notes = "活动报名")
//    @Transactional(rollbackFor = Exception.class)
//    public ServerResponseEntity<Object> addActivityUser(@Valid @RequestBody ActivityUser activityUser) throws Exception {
//        SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd");
//        String userId = SecurityUtils.getUser().getUserId();
//        Activity activity = activityService.getById(activityUser.getActivityId());
//        //负数，前面小于后面，0相等，正数 前面大于后面
//        if(DateUtil.compare(activity.getStartTime(), new Date()) > 0){
//            throw new YamiShopBindException("活动还未开始！");
//        }
//        if(DateUtil.compare(activity.getEndTime(), new Date()) < 0){
//            throw new YamiShopBindException("活动已结束！");
//        }
//        if(activity.getStocks() == 0) {
//            return ServerResponseEntity.showFailMsg("活动报名人数已满，无法报名！");
//        }
//        //分段报名
//        if(activity.getIsTimeSign() == 1){
//            int flag = 0;
//            List<ActivitySign> activitySignList = activitySignService.getActivitySignList(activity.getId());
//            for (ActivitySign activitySign:activitySignList) {
//                int startNum = DateUtil.compare(new Date(),activitySign.getStartTime());
//                int endNum = DateUtil.compare(new Date(),activitySign.getEndTime());
//                //当前时间比报名开始时间大，比报名结束时间小
//                if(startNum > 0 && endNum < 0){
//                    flag += 1;
//                    if(activitySign.getStocks() < 1){
//                        throw new YamiShopBindException(dateFormat.format(activitySign.getStartTime()) + "~" + dateFormat.format(activitySign.getEndTime()) + "报名人数已满，无法报名！");
//                    }
//                    activityUser.setActivitySignId(activitySign.getActivitySignId());
//                    break;
//                }
//            }
//            if(flag < 1){
//                throw new YamiShopBindException("活动报名时间未到，无法报名！");
//            }
//        }
//        if(activity.getIsTimeSign() == 1){
//            if(activitySignService.updateActivitySignStocks(activityUser.getActivitySignId()) < 1){
//                throw new YamiShopBindException("活动报名人数已满，无法报名！");
//            }
//        }
//        QueryWrapper<ActivityUser> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("user_id", userId);
//        queryWrapper.lambda().eq(ActivityUser::getActivityId, activityUser.getActivityId());
//        ActivityUser activityUser1 = activityUserService.getOne(queryWrapper);
//        if (activityUser1 != null) {
//            return ServerResponseEntity.showFailMsg("已报名成功，无法再次报名");
//        }
//        activityUser.setUserId(userId);
//        activityUser.setReceiveTime(new Date());
//        activityUser.setIsDelete(0);
//        activityUserService.save(activityUser);
//
//        if(activityService.updateActivityStocks(activityUser.getActivityId()) < 1) {
//            throw new YamiShopBindException("活动报名人数已满，无法报名！");
//        }
//        return ServerResponseEntity.success("活动报名成功");
//    }


    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @PostMapping("/addActivityUser")
    @ApiOperation(value = "活动报名", notes = "活动报名")
    @Transactional(rollbackFor = Exception.class)
    public ServerResponseEntity<Object> addActivityUser(@Valid @RequestBody ActivityUser activityUser) throws Exception {
        String insertKey = "insert:activityId" + activityUser.getActivityId();
        String saveCountKey = "saveCount:activityId" + activityUser.getActivityId();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String userId = SecurityUtils.getUser().getUserId();
        Activity activity = activityService.getById(activityUser.getActivityId());
        //负数，前面小于后面，0相等，正数 前面大于后面
        if (DateUtil.compare(activity.getStartTime(), new Date()) > 0) {
            throw new YamiShopBindException("活动还未开始！");
        }
        if (DateUtil.compare(activity.getEndTime(), new Date()) < 0) {
            throw new YamiShopBindException("活动已结束！");
        }
        if (activity.getStocks() == 0) {
            throw new YamiShopBindException("活动报名人数已满，无法报名！");
        }

        Integer total = activity.getPersonCount();
        int flag = 0;
        //分段报名
        if (activity.getIsTimeSign() == 1) {
            List<ActivitySign> activitySignList = activitySignService.getActivitySignList(activity.getId());
            for (ActivitySign activitySign : activitySignList) {
                int startNum = DateUtil.compare(new Date(), activitySign.getStartTime());
                int endNum = DateUtil.compare(new Date(), activitySign.getEndTime());
                //当前时间比报名开始时间大，比报名结束时间小
                if (startNum > 0 && endNum < 0) {
                    flag += 1;
                    if (activitySign.getStocks() < 1) {
                        throw new YamiShopBindException(dateFormat.format(activitySign.getStartTime()) + "~" + dateFormat.format(activitySign.getEndTime()) + "报名人数已满，无法报名！");
                    }
                    activityUser.setActivitySignId(activitySign.getActivitySignId());
                    insertKey += ":" + activitySign.getActivitySignId();
                    saveCountKey += ":" + activitySign.getActivitySignId();

                    total = activitySign.getSignTotal();
                    break;
                }
            }
            if (flag < 1) {
                throw new YamiShopBindException("活动报名时间未到，无法报名！");
            }
        }
        insertKey += ":" + userId;
        //1.查询redis 是否存在  避免一个人多次提交
        String s = stringRedisTemplate.opsForValue().get(insertKey);
        if (StringUtils.isNotBlank(s)) {
            throw new YamiShopBindException("已报名成功，无法再次报名");
        }
        //2.存入redis缓存
        Boolean aBoolean = setRedisIfAbsent(insertKey, userId);
        if (!aBoolean) {
            throw new YamiShopBindException("当前提交人数过多，请稍后再试！");
        }
        Long increment = stringRedisTemplate.opsForValue().increment(saveCountKey, 1);
        log.info("increment:{}", increment);
        if (increment > total) {
            deleteRedisKey(insertKey);
            stringRedisTemplate.opsForValue().decrement(saveCountKey, 1);
            throw new YamiShopBindException("报名人数已满，无法报名！");
        }

        QueryWrapper<ActivityUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.lambda().eq(ActivityUser::getActivityId, activityUser.getActivityId());
        ActivityUser activityUser1 = activityUserService.getOne(queryWrapper);
        if (activityUser1 != null) {
            return ServerResponseEntity.showFailMsg("已报名成功，无法再次报名");
        }
        activityUser.setUserId(userId);
        activityUser.setReceiveTime(new Date());
        activityUser.setIsDelete(0);
        boolean save = activityUserService.save(activityUser);
        if (!save) {
            stringRedisTemplate.opsForValue().decrement(saveCountKey, 1);
            deleteRedisKey(insertKey);
            throw new YamiShopBindException("活动报名失败！请重试");
        }
        if (activity.getIsTimeSign() == 1 && flag > 0) {
            if (activitySignService.updateActivitySignStocks(activityUser.getActivitySignId()) < 1) {
                throw new YamiShopBindException("活动报名人数已满，无法报名！");
            }
        }
        if (activityService.updateActivityStocks(activityUser.getActivityId()) < 1) {
            throw new YamiShopBindException("活动报名人数已满，无法报名！");
        }
        return ServerResponseEntity.success("活动报名成功");

    }


    /**
     * 单线程设置值
     *
     * @param key
     * @param value
     * @return
     */
    private Boolean setRedisIfAbsent(String key, String value) {
        Boolean aBoolean = stringRedisTemplate.opsForValue().setIfAbsent(key, value, 30, TimeUnit.DAYS);//设置10天过期
        return aBoolean;
    }

    private void deleteRedisKey(String key) {
        stringRedisTemplate.delete(key);
    }


    @GetMapping("/signActivity")
    @ApiOperation(value = "活动签到 用户扫码", notes = "活动签到")
    @Transactional(rollbackFor = Exception.class)
    public ServerResponseEntity<Object> signActivity(Long activityId) {
        String userId = SecurityUtils.getUser().getUserId();
        QueryWrapper<ActivityUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ActivityUser::getUserId, userId);
        queryWrapper.lambda().eq(ActivityUser::getActivityId, activityId);
        ActivityUser activityUser = activityUserService.getOne(queryWrapper);
        if (activityUser != null) {
            Activity activity = activityService.getById(activityUser.getActivityId());
            if(activityUser.getStatus() != null && activityUser.getStatus() == 1){
                return ServerResponseEntity.showFailMsg("您已签到！");
            }
            //负数，前面小于后面，0相等，正数 前面大于后面
            if (DateUtil.compare(activity.getEndTime(), new Date()) < 0) {
                throw new YamiShopBindException("活动已结束，无法签到！");
            }
            activityUser.setStatus(1);
            activityUser.setSignTime(new Date());
            activityUserService.updateById(activityUser);
            return ServerResponseEntity.success("活动签到成功");
        } else {
            return ServerResponseEntity.showFailMsg("您还未报名，请前往活动报名页面报名！");
        }
    }
}
