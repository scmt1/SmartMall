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

import com.yami.shop.bean.dto.AreaDto;
import com.yami.shop.bean.model.Area;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.service.AreaService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author lgh on 2018/10/26.
 */
@RestController
@RequestMapping("/p/area")
@Api(tags = "省市区接口")
public class AreaController {

    @Autowired
    private AreaService areaService;

    /**
     * 分页获取
     */
    @GetMapping("/listByPid")
    @ApiOperation(value = "获取省市区信息", notes = "根据省市区的pid获取地址信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "level", value = "省市区的level(level为1获取所有省份)", required = true, dataType = "String"),
            @ApiImplicitParam(name = "pid", value = "省市区的pid", required = true, dataType = "String")
    })
    public ServerResponseEntity<List<Area>> listByPid(Long pid, Integer level) {
        List<Area> list = areaService.listByPid(pid, level);
        return ServerResponseEntity.success(list);
    }

    @GetMapping("/getAreaListInfo")
    @ApiOperation(value = "获取省市区信息", notes = "获取完整层级的地址信息")
    public ServerResponseEntity<List<AreaDto>> getAreaListInfo() {
        List<AreaDto> areaDtoList = areaService.getAreaListInfo();
        return ServerResponseEntity.success(areaDtoList);
    }

}
