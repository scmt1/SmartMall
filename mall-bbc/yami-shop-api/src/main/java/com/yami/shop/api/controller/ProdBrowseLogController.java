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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yami.shop.bean.app.dto.ProductDto;
import com.yami.shop.bean.model.ProdBrowseLog;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.security.api.util.SecurityUtils;
import com.yami.shop.service.ProdBrowseLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;


/**
 * 商品浏览记录表
 *
 * @author LGH
 * @date 2021-11-01 10:43:09
 */
@RestController
@AllArgsConstructor
@RequestMapping("/p/prodBrowseLog" )
@Api(tags = "商品浏览记录表")
public class ProdBrowseLogController {

    private final ProdBrowseLogService prodBrowseLogService;

    @GetMapping("/page")
    @ApiOperation(value = "获取商品浏览记录表列表", notes = "分页获取商品浏览记录表列表")
    public ServerResponseEntity<IPage<ProdBrowseLog>> page(PageParam<ProductDto> page) {
        String userId = SecurityUtils.getUser().getUserId();
        IPage<ProdBrowseLog> pageRes = prodBrowseLogService.pageByUserId(page, userId);
        return ServerResponseEntity.success(pageRes);
    }

    @PostMapping
    @ApiOperation(value = "保存商品浏览记录表", notes = "保存商品浏览记录表")
    public ServerResponseEntity<Void> save(@Valid @RequestBody ProdBrowseLog prodBrowseLog) {
        String userId = SecurityUtils.getUser().getUserId();
        prodBrowseLog.setUserId(userId);
        prodBrowseLogService.saveInfo(prodBrowseLog);
        return ServerResponseEntity.success();
    }

    @DeleteMapping
    @ApiOperation(value = "根据prodBrowseLogIds批量删除商品浏览记录", notes = "根据prodBrowseLogIds批量删除商品浏览记录")
    public ServerResponseEntity<Void> deleteByIds(@RequestBody List<Long> prodBrowseLogIds) {
        prodBrowseLogService.deleteByIds(prodBrowseLogIds, SecurityUtils.getUser().getUserId());
        return ServerResponseEntity.success();
    }

}
