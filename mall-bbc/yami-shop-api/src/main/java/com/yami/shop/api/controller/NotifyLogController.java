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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yami.shop.bean.model.NotifyLog;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.security.api.util.SecurityUtils;
import com.yami.shop.service.NotifyLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;


/**
 * @author lhd
 * @date 2020-05-12 08:21:24
 */
@RestController
@RequestMapping("/p/myNotifyLog")
@Api(tags = "消息接口")
public class NotifyLogController {

    @Autowired
    private NotifyLogService notifyLogService;

    @GetMapping("/unReadCount")
    @ApiOperation(value = "查询未读消息数量", notes = "查询未读消息数量")
    public ServerResponseEntity<Integer> getNotifyCount() {
        String userId = SecurityUtils.getUser().getUserId();
        return ServerResponseEntity.success(notifyLogService.countUnreadMsg(userId));
    }

    @GetMapping("/unReadCountList")
    @ApiOperation(value = "查询消息列表", notes = "查询消息列表")
    @ApiImplicitParam(name = "status", value = "状态 0未读 1已读,默认为0", dataType = "Integer")
    public ServerResponseEntity<IPage<NotifyLog>> getUnReadCountList(PageParam<NotifyLog> page, Integer status) {
        String userId = SecurityUtils.getUser().getUserId();
        status = Objects.isNull(status) ? 0 : status;
        IPage<NotifyLog> notifyReminds = notifyLogService.page(page,
                new LambdaQueryWrapper<NotifyLog>()
                        .eq(NotifyLog::getRemindId, userId)
                        .eq(NotifyLog::getStatus, status)
                        .lt(NotifyLog::getSendType, 100)
                        .eq(NotifyLog::getRemindType, 3)
                        .orderByDesc(NotifyLog::getCreateTime));
        List<NotifyLog> records = notifyReminds.getRecords();
        records.forEach(record -> record.setStatus(1));
        if (CollectionUtils.isNotEmpty(records)) {
            notifyLogService.updateBatchById(records);
        }
        return ServerResponseEntity.success(notifyReminds);
    }


}
