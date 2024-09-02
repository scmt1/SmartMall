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

import com.yami.shop.bean.app.dto.CategoryDto;
import com.yami.shop.bean.model.Category;
import com.yami.shop.common.i18n.I18nMessage;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author LGH
 */
@RestController
@RequestMapping("/category")
@Api(tags = "分类接口")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private MapperFacade mapperFacade;

    @GetMapping("/categoryInfo")
    @ApiOperation(value = "分类信息列表", notes = "获取所有的产品分类信息，顶级分类的parentId为0,默认为顶级分类")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "parentId", value = "分类ID", dataType = "Long"),
            @ApiImplicitParam(name = "shopId", value = "店铺id", dataType = "Long")
    })
    public ServerResponseEntity<List<CategoryDto>> categoryInfo(@RequestParam(value = "parentId", defaultValue = "0") Long parentId,
                                                          @RequestParam(value = "shopId", defaultValue = "0") Long shopId) {
        List<Category> categories = categoryService.listByParentIdAndShopId(parentId, shopId, I18nMessage.getDbLang());
        List<CategoryDto> categoryDtos = mapperFacade.mapAsList(categories, CategoryDto.class);
        return ServerResponseEntity.success(categoryDtos);
    }

    @GetMapping("/getCategoryInfoByParentId")
    @ApiOperation(value = "获取三级分类", notes = "获取三级分类")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "parentId", value = "分类ID", dataType = "Long")
    })
    public ServerResponseEntity<List<Category>> getCategoryInfoByParentId(@RequestParam(value = "parentId", defaultValue = "0") Long parentId) {
        List<Category> categories = categoryService.getCategoryInfoByParentId(parentId);
        return ServerResponseEntity.success(categories);
    }
}
