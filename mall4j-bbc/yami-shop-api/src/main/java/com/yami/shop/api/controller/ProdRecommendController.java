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

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yami.shop.bean.app.dto.ProdCommDataDto;
import com.yami.shop.bean.app.dto.ProdCommDto;
import com.yami.shop.bean.app.dto.ProductDto;
import com.yami.shop.bean.app.dto.SkuDto;
import com.yami.shop.bean.app.vo.ProductVO;
import com.yami.shop.bean.enums.ProdStatusEnums;
import com.yami.shop.bean.enums.ProdType;
import com.yami.shop.bean.enums.ShopStatus;
import com.yami.shop.bean.event.LoadProdActivistEvent;
import com.yami.shop.bean.event.ProcessActivityProdPriceEvent;
import com.yami.shop.bean.model.*;
import com.yami.shop.bean.param.EsProductParam;
import com.yami.shop.bean.param.ProductParam;
import com.yami.shop.bean.vo.search.EsProductSearchVO;
import com.yami.shop.bean.vo.search.ProductSearchVO;
import com.yami.shop.common.config.Constant;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.i18n.I18nMessage;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.common.util.Json;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.delivery.common.model.SameCity;
import com.yami.shop.delivery.common.service.SameCityService;
import com.yami.shop.search.common.param.EsPageParam;
import com.yami.shop.search.common.service.SearchProductService;
import com.yami.shop.search.common.vo.EsPageVO;
import com.yami.shop.security.api.model.YamiUser;
import com.yami.shop.security.api.util.SecurityUtils;
import com.yami.shop.security.common.bo.UserInfoInTokenBO;
import com.yami.shop.security.common.util.AuthUserContext;
import com.yami.shop.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import ma.glasnost.orika.MapperFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 登录情况下的商品推荐接口
 *
 * @author chiley
 * @date 2022/9/21 10:33
 */
@RestController
@RequestMapping("/p/prod")
@Api(tags = "商品推荐接口")
public class ProdRecommendController {

    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SearchProductService searchProductService;
    @Autowired
    private ProdBrowseLogService prodBrowseLogService;


    @GetMapping("/recommendList")
    @ApiOperation(value = "推荐商品列表", notes = "根据商品ID（prodId）获取商品信息")
    public ServerResponseEntity<EsPageVO<EsProductSearchVO>> recommendList(PageParam page, ProductParam productParam) {
        YamiUser user = SecurityUtils.getUser();
        EsProductParam esProductParam = new EsProductParam();
        if (Objects.isNull(productParam.getProdType())) {
            esProductParam.setProdType(ProdType.PROD_TYPE_NORMAL.value());
        }
        Long primaryCategoryId;
        primaryCategoryId = prodBrowseLogService.recommendCategoryId(productParam.getProdType(), user.getUserId());

        // 已登陆但还没有数据，或未登陆
        boolean subCategoryId = (!Objects.equals(productParam.getProdType(), ProdType.PROD_TYPE_SCORE.value()) && Objects.isNull(primaryCategoryId)) && Objects.nonNull(productParam.getCategoryId());
        if (subCategoryId) {
            List<Category> categoryList = categoryService.getCategoryAndParent(productParam.getCategoryId());
            if (CollUtil.isNotEmpty(categoryList)) {
                primaryCategoryId = categoryList.get(0).getCategoryId();
            }
        }
        esProductParam.setPrimaryCategoryId(primaryCategoryId);
        //如果有商品id则过滤掉
        if (Objects.nonNull(productParam.getProdId())) {
            List<Long> prodIds = new ArrayList<>();
            prodIds.add(productParam.getProdId());
            esProductParam.setSpuIdsExclude(prodIds);
        }
        EsPageParam esPageParam = new EsPageParam();
        esPageParam.setCurrent((int) page.getCurrent());
        esPageParam.setSize((int) page.getSize());
        EsPageVO<EsProductSearchVO> productPage = searchProductService.page(esPageParam, esProductParam, Boolean.FALSE);
        List<ProductSearchVO> products = productPage.getRecords().get(0).getProducts();
        long current = page.getCurrent();
        long size = page.getSize();
        int spuNum = products.size();
        // 推荐商品的数量不足时，查询额外的商品进行填充
        if (Objects.equals(current, 1L) && size > spuNum) {
            esPageParam.setSize(Math.toIntExact(size - spuNum));
            esPageParam.setCurrent(1);
            // 查询该分类以外的商品
            esProductParam.setPrimaryCategoryId(null);
            esProductParam.setNotPrimaryCategoryId(primaryCategoryId);
            EsPageVO<EsProductSearchVO> subProductPage = searchProductService.page(esPageParam, esProductParam, Boolean.FALSE);
            for (EsProductSearchVO productSearchVO : subProductPage.getRecords()) {
                if (CollUtil.isNotEmpty(productSearchVO.getProducts())) {
                    products.addAll(productSearchVO.getProducts());
                }
            }
            productPage.setTotal((long) products.size());
            productPage.setPages(productPage.getTotal() > 0 ? (int) page.getCurrent() : 0);
        }
        return ServerResponseEntity.success(productPage);
    }
}
