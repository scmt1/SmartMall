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


import com.yami.shop.bean.dto.FlowLogDto;
import com.yami.shop.bean.model.FlowLog;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.service.FlowLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;


/**
 * 用户流量记录
 *
 * @author YXF
 * @date 2020-07-13 13:18:33
 */
@RestController
@RequestMapping("/flowAnalysisLog")
@Api(tags = "用户流量记录接口")
public class FlowLogController {

    @Autowired
    private FlowLogService flowLogService;
    @Autowired
    private MapperFacade mapperFacade;


    @PostMapping
    @ApiOperation(value = "新增用户流量记录")
    public ServerResponseEntity<Boolean> save(@RequestBody @Valid FlowLogDto flowLogDto, HttpServletRequest request) {
        FlowLog flowLog = mapperFacade.map(flowLogDto, FlowLog.class);
        flowLogService.saveUserFlowLog(flowLog, request);
        return ServerResponseEntity.success();
    }
}
