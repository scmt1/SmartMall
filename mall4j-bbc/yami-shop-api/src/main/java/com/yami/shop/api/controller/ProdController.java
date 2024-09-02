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
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yami.shop.bean.app.dto.*;
import com.yami.shop.bean.app.vo.ProductVO;
import com.yami.shop.bean.dto.DerivativeCategoryDto;
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

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author LGH
 */
@RestController
@RequestMapping("/prod")
@Api(tags = "商品接口")
public class ProdController {

    @Autowired
    private ProductService prodService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private MapperFacade mapperFacade;
    @Autowired
    private SameCityService sameCityService;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private SkuService skuService;
    @Autowired
    private ProdCommService prodCommService;
    @Autowired
    private ShopDetailService shopDetailService;
    @Autowired
    private ProdParameterService prodParameterService;
    @Autowired
    private SearchProductService searchProductService;
    @Autowired
    private ProductService productService;
    @Autowired
    private ClothingProdReductionService clothingProdReductionService;

    @GetMapping("/prodInfo")
    @ApiOperation(value = "商品详情信息", notes = "根据商品ID（prodId）获取商品信息")
    @ApiImplicitParam(name = "prodId", value = "商品ID", required = true, dataType = "Long")
    public ServerResponseEntity<ProductVO> prodInfo(Long prodId) {
        Integer dbLang = I18nMessage.getDbLang();
        Product product = prodService.getProductInfo(prodId, dbLang);
        if (product == null || product.getStatus() != 1) {
            // 商品已下线
            throw new YamiShopBindException("yami.product.off.shelves");
        }
        // 检查店铺是否处于营业状态
        checkShopStatusIsOpen(product);
        // 启用的sku列表
        List<Sku> skuList = skuService.listPutOnSkuAndSkuStock(prodId, dbLang);
        product.setSkuList(skuList);
        ProductVO productVO = mapperFacade.map(product, ProductVO.class);
        // 如果是积分商品， 所有数据已经获取完成了
        if (Objects.equals(productVO.getProdType(), ProdType.PROD_TYPE_SCORE.value())) {
            return ServerResponseEntity.success(productVO);
        }
        // 发送事件，获取商品可用的正在开播直播间、商品套餐、秒杀、团购、积分商品信息
        applicationContext.publishEvent(new LoadProdActivistEvent(prodId, productVO, product.getProdType()));
        // 普通商品有多种物流，需要加载物流模板信息
//        if (Objects.equals(productVO.getProdType(), ProdType.PROD_TYPE_NORMAL.value())) {
        loadDeliveryMode(product.getDeliveryMode(), productVO);
//        }
        // 商品参数列表
        List<ProdParameter> prodParameters = prodParameterService.listParameter(prodId, I18nMessage.getDbLang());
        productVO.setProdParameterList(prodParameters);
        return ServerResponseEntity.success(productVO);
    }

    @GetMapping("/skuList")
    @ApiOperation(value = "sku信息", notes = "根据商品ID（prodId）单独获取sku信息")
    @ApiImplicitParam(name = "prodId", value = "商品ID", required = true, dataType = "Long")
    public ServerResponseEntity<List<SkuDto>> skuList(@RequestParam("prodId") Long prodId) {
        return ServerResponseEntity.success(skuService.getProdDetailSkuInfo(prodId));
    }

    @GetMapping("/isStatus")
    @ApiOperation(value = "校验商品是否下架", notes = "根据商品ID（prodId）校验商品是否下架")
    @ApiImplicitParam(name = "prodId", value = "商品ID", required = true)
    public ServerResponseEntity<Boolean> isStatus(Long prodId) {
        Product product = prodService.getProductByProdId(prodId, I18nMessage.getDbLang());
        if (product == null || product.getStatus() != 1) {
            return ServerResponseEntity.success(false);
        }
        return ServerResponseEntity.success(true);
    }

    @GetMapping("/listProdByIdsAndType")
    @ApiOperation(value = "获取商品信息", notes = "根据商品ids获取商品信息")
    public ServerResponseEntity<List<ProductDto>> listProdByIdsAndType(ProductParam product) {
        product.setLang(I18nMessage.getDbLang());
        product.setStatus(ProdStatusEnums.NORMAL.getValue());
        List<Product> products = prodService.listProdByIdsAndType(product);
        processActivityProdPrice(product, products);
        List<ProductDto> productDtos = mapperFacade.mapAsList(products, ProductDto.class);
        return ServerResponseEntity.success(productDtos);
    }

    /**
     * 处理下活动商品的价格
     *
     * @param product  筛选参数
     * @param products 商品列表
     */
    private void processActivityProdPrice(ProductParam product, List<Product> products) {
        Map<Integer, List<Product>> prodMap = products.stream().collect(Collectors.groupingBy(Product::getProdType));
        if (prodMap.containsKey(ProdType.PROD_TYPE_SECKILL.value())) {
            applicationContext.publishEvent(new ProcessActivityProdPriceEvent(product, prodMap.get(ProdType.PROD_TYPE_SECKILL.value())));
        }

        if (prodMap.containsKey(ProdType.PROD_TYPE_GROUP.value())) {
            applicationContext.publishEvent(new ProcessActivityProdPriceEvent(product, prodMap.get(ProdType.PROD_TYPE_GROUP.value())));
        }
    }

    @GetMapping("/prodCommData")
    @ApiOperation(value = "返回商品评论数据(好评率 好评数量 中评数 差评数)", notes = "根据商品id获取")
    @ApiImplicitParam(name = "prodId", value = "商品id", required = true, dataType = "Long")
    public ServerResponseEntity<ProdCommDataDto> getProdCommData(Long prodId) {
        return ServerResponseEntity.success(prodCommService.getProdCommDataByProdId(prodId));
    }

    @GetMapping("/prodCommPageByProd")
    @ApiOperation(value = "根据商品返回评论分页数据", notes = "传入商品id和页码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "prodId", value = "商品id", required = true, dataType = "Long"),
            @ApiImplicitParam(name = "evaluate", value = "-1或null 全部，0好评 1中评 2差评 3有图", required = true, dataType = "Long"),
    })
    public ServerResponseEntity<IPage<ProdCommDto>> getProdCommPageByProdId(PageParam page, Long prodId, Integer evaluate) {
        return ServerResponseEntity.success(prodCommService.getProdCommDtoPageByProdId(page, prodId, evaluate));
    }

    /**
     *  用户未登录情况下的商品推荐
     * @param page
     * @param productParam
     * @return
     */
    @GetMapping("/recommendList")
    @ApiOperation(value = "推荐商品列表", notes = "根据商品ID（prodId）获取商品信息")
    public ServerResponseEntity<EsPageVO<EsProductSearchVO>> recommendList(PageParam page, ProductParam productParam) {
        EsProductParam esProductParam = new EsProductParam();
        if (Objects.isNull(productParam.getProdType())) {
            esProductParam.setProdType(ProdType.PROD_TYPE_NORMAL.value());
        }
        Long primaryCategoryId = null;
        List<Category> categoryList = categoryService.getCategoryAndParent(productParam.getCategoryId());
        if (CollUtil.isNotEmpty(categoryList)) {
            primaryCategoryId = categoryList.get(0).getCategoryId();
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
        if (Objects.equals(current , 1L) && size > spuNum) {
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
            productPage.setPages(productPage.getTotal() > 0 ? (int)page.getCurrent() : 0);
        }
        return ServerResponseEntity.success(productPage);
    }

    private void checkShopStatusIsOpen(Product product) {
        // 积分商品的平台店铺不需要检验
        if (Objects.equals(product.getShopId(), Constant.PLATFORM_SHOP_ID)) {
            return;
        }
        Date now = new Date();
        ShopDetail shopDetail = shopDetailService.getShopDetailByShopId(product.getShopId());
        if (Objects.equals(shopDetail.getShopStatus(), ShopStatus.OPEN.value())) {
            return;
        }
        product.setShopName(shopDetail.getShopName());
        if (Objects.equals(shopDetail.getShopStatus(), ShopStatus.OFFLINE.value()) || Objects.equals(shopDetail.getShopStatus(), ShopStatus.OFFLINE_AUDIT.value())) {
            throw new YamiShopBindException("店铺已下线");
        }
        if (Objects.equals(shopDetail.getShopStatus(), ShopStatus.STOP.value())) {
            if (now.compareTo(shopDetail.getContractStartTime()) < 0) {
                throw new YamiShopBindException("店铺未开始营业");
            } else {
                throw new YamiShopBindException("店铺已停止营业");
            }
        }
        throw new YamiShopBindException("店铺状态异常");
    }

    /**
     * 加载商品物流模板
     * @param deliveryMode
     * @param productVO
     */
    private void loadDeliveryMode(String deliveryMode, ProductVO productVO) {
        // 物流模板
        Product.DeliveryModeVO deliveryModeVO = Json.parseObject(deliveryMode, Product.DeliveryModeVO.class);
        SameCity sameCity = sameCityService.getSameCityByShopId(productVO.getShopId());
        // 如果同城配送是关闭了的，前端就不需要显示同城配送了
        if (Objects.isNull(sameCity) || !Objects.equals(1, sameCity.getStatus()) || Objects.isNull(deliveryModeVO.getHasCityDelivery())) {
            deliveryModeVO.setHasCityDelivery(false);
        }
        if (deliveryModeVO.getHasCityDelivery() != null && deliveryModeVO.getHasCityDelivery() && Objects.nonNull(sameCity)) {
            productVO.setStartDeliveryFee(sameCity.getStartingFee());
        }
        productVO.setDeliveryModeVO(deliveryModeVO);
    }

    @GetMapping("/getProdByCategory")
    @ApiOperation(value = "通过分类查询商品", notes = "商品查询接口,通过分类获取")
    public ServerResponseEntity<List<Category>> getProdByCategory(@RequestParam(value = "parentId", defaultValue = "0") Long parentId,
                                                                  @RequestParam(value = "shopId", defaultValue = "0") Long shopId) {
        List<Category> categories = categoryService.listByParentIdAndShopId(parentId, shopId, I18nMessage.getDbLang());
        List<Long> parentIds;
        List<Category> categoryList = new ArrayList<>();
        for (Category category:categories) {
            parentIds = new ArrayList<>();
            parentIds.add(category.getCategoryId());
            List<Long> categoryIds = categoryService.getCategoryIdByParentId(parentIds);
            List<Product> prodList = productService.getProdCategoryByCategoryId(categoryIds);
            if(prodList.size() > 0){
                category.setProducts(prodList);
                categoryList.add(category);
            }
        }
        return ServerResponseEntity.success(categoryList);
    }

    @GetMapping("/getProdByCategoryAndShop")
    @ApiOperation(value = "通过分类和店铺查询商品", notes = "商品查询接口,通过分类和店铺获取")
    public ServerResponseEntity<IPage<Product>> getProdByCategoryAndShop(PageParam page,Product product) {
        IPage<Product> prodList = productService.getProdByCategoryAndShop(page,product.getCategoryId(),product.getShopIds());
        return ServerResponseEntity.success(prodList);
    }

    @GetMapping("/getReductionProdByShopId")
    @ApiOperation(value = "通过店铺id查询商品", notes = "商品查询接口,通过店铺id获取")
    public ServerResponseEntity<IPage<Product>> getReductionProdByShopId(PageParam page,Product product) {
        LambdaQueryWrapper<ClothingProdReduction> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ClothingProdReduction::getShopId, product.getShopId());
        queryWrapper.last("LIMIT 1");
        ClothingProdReduction one = clothingProdReductionService.getOne(queryWrapper);
        if(one == null){
            IPage<Product> prodList = productService.getReductionProdByShopId(page,product.getShopId());
            return ServerResponseEntity.success(prodList);
        }else{
            List<ClothingProdReduction> clothingProdByShopId = clothingProdReductionService.getClothingProdByShopId(product.getShopId());
            List<Long> prodIds = clothingProdByShopId.stream().map(o -> o.getProdId()).collect(Collectors.toList());
            IPage<Product> prodByProdIds = productService.getProdByProdIds(page,prodIds);
            return ServerResponseEntity.success(prodByProdIds);
        }
    }
}
