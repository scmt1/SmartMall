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

import com.yami.shop.bean.app.dto.ProdTagDto;
import com.yami.shop.bean.model.ProdTag;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.service.ProdTagService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author LGH
 */
@RestController
@RequestMapping("/prod/tag")
@Api(tags = "商品分组标签接口")
public class ProdTagController {

    @Autowired
    private ProdTagService prodTagService;

    @Autowired
    private MapperFacade mapperFacade;

    @GetMapping("/prodTagList")
    @ApiOperation(value = "商品分组标签列表", notes = "获取所有的商品分组列表")
    public ServerResponseEntity<List<ProdTagDto>> getProdTagList() {
        List<ProdTag> prodTagList = prodTagService.listProdTag();
        List<ProdTagDto> prodTagDtoList = mapperFacade.mapAsList(prodTagList, ProdTagDto.class);
        return ServerResponseEntity.success(prodTagDtoList);
    }
}
