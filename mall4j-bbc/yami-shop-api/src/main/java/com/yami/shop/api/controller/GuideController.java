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
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yami.shop.bean.model.Guide;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.service.GuideService;
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
@Api(tags = "引导页")
@RequestMapping("/admin/guide")
public class GuideController {

    @Autowired
    private GuideService guideService;


    @GetMapping("/info/{id}")
    @ApiOperation(value = "获取信息", notes = "获取信息")
    public ServerResponseEntity<Guide> info(@PathVariable("id") Long id) {
        Guide area = guideService.getById(id);
        return ServerResponseEntity.success(area);
    }

    @GetMapping("/list")
    @ApiOperation(value = "查询list", notes = "查询list")
    public ServerResponseEntity<List<Guide>> list(Guide guide) {
        List<Guide> sysUserPage = guideService.list( new LambdaQueryWrapper<Guide>()
                .like(StrUtil.isNotBlank(guide.getGuideTitle()), Guide::getGuideTitle, guide.getGuideTitle())
                .eq(Guide::getType, guide.getType()).eq(Guide::getDelFlag, guide.getDelFlag())
                .orderByDesc(Guide::getCreateTime)
        );
        return ServerResponseEntity.success(sysUserPage);
    }
}
