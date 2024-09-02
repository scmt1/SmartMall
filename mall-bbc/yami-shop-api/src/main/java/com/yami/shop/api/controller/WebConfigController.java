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


import com.yami.shop.bean.enums.WebConfigTypeEnum;
import com.yami.shop.bean.model.WebConfig;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.service.SysConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author SJL
 * @date 2021-02-20 09:44:42
 */
@RestController
@AllArgsConstructor
@RequestMapping("/webConfig")
@Api(tags = "网站配置接口")
public class WebConfigController {

    private final SysConfigService sysConfigService;

    @GetMapping("/getPcWebConfig")
    @ApiOperation(value = "获取当前激活的PC端网站配置")
    public ServerResponseEntity<WebConfig> getWebConfig() {
        return ServerResponseEntity.success(sysConfigService.getSysConfigObject(WebConfigTypeEnum.PC.value(), WebConfig.class));
    }

    @GetMapping("/getUniWebConfig")
    @ApiOperation(value = "获取当前激活的H5端网站配置")
    public ServerResponseEntity<WebConfig> getUniWebConfig() {
        return ServerResponseEntity.success(sysConfigService.getSysConfigObject(WebConfigTypeEnum.H5.value(), WebConfig.class));
    }

    @GetMapping("/getStationWebConfig")
    @ApiOperation(value = "获取自提点端网站配置")
    public ServerResponseEntity<WebConfig> getStationWebConfig() {
        return ServerResponseEntity.success(sysConfigService.getSysConfigObject(WebConfigTypeEnum.STATION.value(), WebConfig.class));

    }
}
