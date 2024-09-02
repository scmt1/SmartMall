/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.combo.multishop.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.shop.bean.app.dto.SkuDto;
import com.yami.shop.bean.enums.ComboProdType;
import com.yami.shop.bean.enums.IsRequired;
import com.yami.shop.bean.enums.ProdStatusEnums;
import com.yami.shop.bean.model.Product;
import com.yami.shop.bean.model.Sku;
import com.yami.shop.bean.param.ProductParam;
import com.yami.shop.bean.vo.ComboProdSkuVO;
import com.yami.shop.bean.vo.ComboProdVO;
import com.yami.shop.bean.vo.ComboVO;
import com.yami.shop.combo.multishop.dao.ComboMapper;
import com.yami.shop.combo.multishop.dto.ComboSkuDto;
import com.yami.shop.combo.multishop.model.Combo;
import com.yami.shop.combo.multishop.model.ComboProd;
import com.yami.shop.combo.multishop.model.ComboProdSku;
import com.yami.shop.combo.multishop.service.ComboProdService;
import com.yami.shop.combo.multishop.service.ComboProdSkuService;
import com.yami.shop.combo.multishop.service.ComboService;
import com.yami.shop.common.enums.StatusEnum;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.i18n.I18nMessage;
import com.yami.shop.common.util.Arith;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.common.util.RedisUtil;
import com.yami.shop.dao.BasketMapper;
import com.yami.shop.service.BasketService;
import com.yami.shop.service.ProductService;
import com.yami.shop.service.SkuService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 套餐
 *
 * @author LGH
 * @date 2021-11-02 10:32:48
 */
@Service
public class ComboServiceImpl extends ServiceImpl<ComboMapper, Combo> implements ComboService {

    @Autowired
    private ComboMapper comboMapper;
    @Autowired
    private BasketMapper basketMapper;
    @Autowired
    private ComboProdService comboProdService;
    @Autowired
    private ProductService productService;
    @Autowired
    private SkuService skuService;
    @Autowired
    private ComboProdSkuService comboProdSkuService;
    @Autowired
    private BasketService basketService;

    private static final int MATCHING_PROD_LIMIT = 4;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveInfo(Combo combo) {
        Date now = new Date();
        // 检查信息
        this.checkComboInfo(combo);
        combo.setCreateTime(now);
        combo.setUpdateTime(now);
        combo.setStatus(StatusEnum.ENABLE.value());
        combo.setSoldNum(0);
        combo.setMainProdId(combo.getMainProd().getProdId());
        combo.setPrice(loadComboPrice(combo));
        Date date = new Date();
        Date startTime = combo.getStartTime();
        if (startTime.compareTo(date) > 0){
            combo.setStatus(StatusEnum.OFFLINE.value());
        }else {
            combo.setStatus(StatusEnum.ENABLE.value());
        }
        // 保存套餐
        comboMapper.insert(combo);
        // 保存主商品
        ComboProd mainProd = combo.getMainProd();
        mainProd.setComboId(combo.getComboId());
        mainProd.setStatus(combo.getStatus());
        comboProdService.save(mainProd);
        // 保存主商品关联的规格信息
        comboProdSkuService.insertBatch(mainProd.getSkuList(), mainProd.getComboProdId());
        // 保存搭配商品
        for (ComboProd matchingProd : combo.getMatchingProds()) {
            matchingProd.setStatus(combo.getStatus());
        }
        comboProdService.insertBatch(combo.getMatchingProds(), combo.getComboId());
        // 保存搭配商品关联的规格信息
        combo.getMatchingProds().forEach(matchingProd -> comboProdSkuService.insertBatch(matchingProd.getSkuList(), matchingProd.getComboProdId()));
        //清除缓存
        comboProdService.removeComboListCache(mainProd.getProdId());
    }

    private void loadMatchingPrice(ComboProd comboProd) {
        comboProd.setComboPrice(comboProd.getSkuList().get(0).getMatchingPrice());
        for (ComboProdSku comboProdSku : comboProd.getSkuList()) {
            if (comboProdSku.getMatchingPrice() < comboProd.getComboPrice()) {
                comboProd.setComboPrice(comboProdSku.getMatchingPrice());
            }
        }
    }

    @Override
    public Combo getComboWithProdInfoById(Long comboId) {
        Combo combo = comboMapper.getComboWithProdInfoById(comboId);
        loadMainProd(combo);
        if (Objects.equals(combo.getStatus(), StatusEnum.DELETE.value())) {
            // 当前套餐已被删除
            throw new YamiShopBindException("yami.combo.already.delete");
        }
        // 获取主商品关联的商品信息
        ComboProd mainProd = combo.getMainProd();
        this.getProdAndSkuInfo(mainProd);
        // 获取搭配商品关联的商品信息
        List<ComboProd> matchingProds = combo.getMatchingProds();
        matchingProds.forEach(this::getProdAndSkuInfo);
        return combo;
    }

    /**
     * 获取关联的prod与sku信息
     *
     * @param comboProd
     */
    private void getProdAndSkuInfo(ComboProd comboProd) {
        Product prod = productService.getProductByProdId(comboProd.getProdId(), I18nMessage.getLang());
        if (Objects.nonNull(prod)) {
            comboProd.setProdName(prod.getProdName());
            comboProd.setPic(prod.getPic());
            comboProd.setProdStatus(prod.getStatus());
            List<ComboProdSku> comboProdSkuList = comboProdSkuService.list(Wrappers.lambdaQuery(ComboProdSku.class)
                    .eq(ComboProdSku::getComboProdId, comboProd.getComboProdId())
            );
            for (ComboProdSku comboProdSku : comboProdSkuList) {
                Sku sku = skuService.getSkuBySkuId(comboProdSku.getSkuId(), I18nMessage.getLang());
                if (Objects.nonNull(sku)) {
                    comboProdSku.setSkuName(sku.getSkuName());
                    comboProdSku.setPrice(sku.getPrice());
                    comboProdSku.setSkuStatus(Objects.equals(sku.getIsDelete(), 1) ? StatusEnum.DELETE.value() : sku.getStatus());
                    comboProdSku.setProdRequired(comboProd.getRequired());
                    comboProdSku.setStocks(sku.getStocks());
                } else {
                    comboProdSku.setSkuStatus(StatusEnum.DELETE.value());
                }
            }
            comboProd.setSkuList(comboProdSkuList);
        } else {
            comboProd.setProdStatus(StatusEnum.DELETE.value());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInfo(Combo combo) {
        // 从套餐中移除的商品id集合
        List<Long> deleteComboProdIds = new ArrayList<>();
        // 数据发生变化的主商品prodId集合
        List<Long> changeDateProdIds = new ArrayList<>();
        // 更新前的准备工作
        this.updatePrepare(combo, deleteComboProdIds, changeDateProdIds);
        Combo comboDb = getComboWithProdInfoById(combo.getComboId());
        combo.setPrice(loadComboPrice(combo));
        comboStatus(combo);
        // 更新套餐数据
        int comboUpdateCount = comboMapper.update(combo, Wrappers.lambdaUpdate(Combo.class)
                .eq(Combo::getComboId, combo.getComboId())
                .ne(Combo::getStatus, StatusEnum.DELETE.value())
                .ne(Combo::getStatus, StatusEnum.DISABLE.value())
        );
        if (comboUpdateCount < 1) {
            throw new YamiShopBindException("yami.combo.update.fail");
        }
        // 更新主商品数据
        ComboProd mainProd = combo.getMainProd();
        mainProd.setStatus(combo.getStatus());
        comboProdService.saveOrUpdate(mainProd);
        comboProdSkuService.remove(Wrappers.lambdaQuery(ComboProdSku.class)
                .eq(ComboProdSku::getComboProdId, mainProd.getComboProdId())
        );
        comboProdSkuService.insertBatch(mainProd.getSkuList(), mainProd.getComboProdId());
        if (CollUtil.isNotEmpty(deleteComboProdIds)) {
            comboProdService.update(Wrappers.lambdaUpdate(ComboProd.class)
                    .set(ComboProd::getStatus, StatusEnum.DELETE.value())
                    .in(ComboProd::getComboProdId, deleteComboProdIds)
            );
        }
        // 更新搭配商品数据
        List<ComboProd> matchingProds = combo.getMatchingProds();
        for (ComboProd matchingProd : matchingProds) {
            matchingProd.setStatus(combo.getStatus());
        }
        if (CollUtil.isNotEmpty(matchingProds)) {
            comboProdService.saveOrUpdateBatch(matchingProds);
        }
        matchingProds.forEach(matchingProd -> {
            comboProdSkuService.remove(Wrappers.lambdaQuery(ComboProdSku.class)
                    .eq(ComboProdSku::getComboProdId, matchingProd.getComboProdId())
            );
            comboProdSkuService.insertBatch(matchingProd.getSkuList(), matchingProd.getComboProdId());
        });
        //清除缓存
        changeDateProdIds.forEach(prodId -> comboProdService.removeComboListCache(prodId));

        //删除有相关套餐的购物车缓存
        if(changeDateProdIds.size()>0){
            List<String> userIds = basketMapper.removeByComboIdAndSkuIds(combo.getComboId(), null);
            basketService.removeCacheByUserIds(userIds);
        }

        // 购物车
        removeBasketCombo(combo, comboDb);
    }

    /**
     * 根据修改时间判断活动状态
     * @param combo
     */
    private void comboStatus(Combo combo){
        Date now = new Date();
        Date startTime = combo.getStartTime();
        if (startTime.compareTo(now) > 0){
            //活动开始时间在当前时间后面
            combo.setStatus(StatusEnum.OFFLINE.value());
        }else {
            combo.setStatus(StatusEnum.ENABLE.value());
        }
    }

    private Double loadComboPrice(Combo combo) {
        double price = 0D;
        ComboProd mainProd = combo.getMainProd();
        loadMatchingPrice(mainProd);
        price = Arith.add(price, Arith.mul(mainProd.getComboPrice(), mainProd.getLeastNum()));
        List<ComboProd> matchingProds = combo.getMatchingProds();
        for (ComboProd matchingProd : matchingProds) {
            loadMatchingPrice(matchingProd);
            price = Arith.add(price, Arith.mul(matchingProd.getComboPrice(), matchingProd.getLeastNum()));
        }
        return price;
    }

    private void removeBasketCombo(Combo combo, Combo comboDb) {
        // 主商品
        ComboProd mainProd = combo.getMainProd();
        ComboProd mainProdDb = comboDb.getMainProd();
        List<Long> skuIdList = mainProd.getSkuList().stream().map(ComboProdSku::getSkuId).collect(Collectors.toList());
        List<Long> skuIdListDb = mainProdDb.getSkuList().stream().filter(prod -> !skuIdList.contains(prod.getSkuId())).map(ComboProdSku::getSkuId).collect(Collectors.toList());
        boolean skuChange = skuIdList.size() != mainProdDb.getSkuList().size() || CollUtil.isNotEmpty(skuIdListDb);

        // 主商品的sku删除，清除购物车该套餐的信息
        if (skuChange) {
            basketService.removeByComboIdAndSkuIds(combo.getComboId(), null);
            return;
        }
        // 搭配商品
        List<Long> skuIds = new ArrayList<>();
        for (ComboProd matchingProd : comboDb.getMatchingProds()) {
            for (ComboProdSku comboProdSku : matchingProd.getSkuList()) {
                skuIds.add(comboProdSku.getSkuId());
            }
        }
        // 已删除的
        for (ComboProd matchingProd : combo.getMatchingProds()) {
            for (ComboProdSku comboProdSku : matchingProd.getSkuList()) {
                skuIds.remove(comboProdSku.getSkuId());
            }
        }
        if (CollUtil.isEmpty(skuIds)) {
            return;
        }
        // 清除购物车中已不存在的套餐商品
        basketService.removeByComboIdAndSkuIds(combo.getComboId(), skuIds);
    }

    /**
     * 更新套餐前的准备工作 检查套餐信息，设置默认值，更新商品类型
     *
     * @param combo
     * @param deleteComboProdIds
     */
    private void updatePrepare(Combo combo, List<Long> deleteComboProdIds, List<Long> changeDateProdIds) {
        // 检查套餐数据
        this.checkComboInfo(combo);
        // 获取数据库中的套餐数据
        Combo dbCombo = comboMapper.selectById(combo.getComboId());
        if (Objects.isNull(dbCombo) || Objects.equals(dbCombo.getStatus(), StatusEnum.DELETE.value())) {
            // 当前套餐已被删除
            throw new YamiShopBindException("yami.combo.already.delete");
        }
        if (Objects.equals(dbCombo.getStatus(), StatusEnum.DISABLE.value())) {
            // 当前套餐已失效
            throw new YamiShopBindException("yami.combo.already.invalid");
        }
        if (!Objects.equals(dbCombo.getShopId(), combo.getShopId())) {
            // 店铺id不相等
            throw new YamiShopBindException("yami.combo.not.shop");
        }
        combo.setStatus(StatusEnum.ENABLE.value());
        combo.setUpdateTime(new Date());
        ComboProd mainProd = combo.getMainProd();
        mainProd.setComboId(combo.getComboId());
        List<ComboProd> matchingProds = combo.getMatchingProds();
        // 获取旧的主商品数据
        ComboProd dbMainProd = comboProdService.getOne(Wrappers.lambdaQuery(ComboProd.class)
                .select(ComboProd::getComboProdId, ComboProd::getProdId)
                .eq(ComboProd::getComboId, combo.getComboId())
                .eq(ComboProd::getType, ComboProdType.MAIN_PROD.value())
                .ne(ComboProd::getStatus, StatusEnum.DELETE.value())
                .ne(ComboProd::getStatus,StatusEnum.DISABLE.value())
        );
        changeDateProdIds.add(mainProd.getProdId());
        if (Objects.equals(mainProd.getProdId(), dbMainProd.getProdId())) {
            // 主商品更新
            mainProd.setComboProdId(dbMainProd.getComboProdId());
        } else {
            // 删除旧的主商品，更新商品类型
            deleteComboProdIds.add(dbMainProd.getComboProdId());
            changeDateProdIds.add(dbMainProd.getProdId());
        }
        combo.setMainProdId(mainProd.getProdId());
        // 获取旧的搭配商品数据
        List<ComboProd> dbMatchingProds = comboProdService.list(Wrappers.lambdaQuery(ComboProd.class)
                .select(ComboProd::getComboProdId, ComboProd::getProdId)
                .eq(ComboProd::getComboId, combo.getComboId())
                .eq(ComboProd::getType, ComboProdType.MATCHING_PROD.value())
                .eq(ComboProd::getStatus, StatusEnum.ENABLE.value())
        );
        for (ComboProd comboProd : matchingProds) {
            comboProd.setComboId(combo.getComboId());
            int index = 0;
            for (; index < dbMatchingProds.size(); index++) {
                if (Objects.equals(comboProd.getProdId(), dbMatchingProds.get(index).getProdId())) {
                    comboProd.setComboProdId(dbMatchingProds.get(index).getComboProdId());
                    break;
                }
            }
            if (index < dbMatchingProds.size()) {
                dbMatchingProds.remove(index);
            }
        }
        dbMatchingProds.forEach(comboProd -> deleteComboProdIds.add(comboProd.getComboProdId()));
    }

    @Override
    public IPage<Combo> pageByParam(PageParam<Combo> page, Combo combo) {

        //如果是主商品名称查询
        if(Objects.nonNull(combo.getMainProdName()) && !Objects.equals(combo.getMainProdName(), "")){
            List<Long> ids = getProdIdByName(combo, combo.getMainProdName());
            if(CollectionUtils.isEmpty(ids)){
                ids.add(-1L);
            }
            combo.setMainProdIdList(ids);
        }
        //如果是搭配商品名称查询
        if(Objects.nonNull(combo.getMatchProdName())  && !Objects.equals(combo.getMatchProdName(), "")){
            List<Long> ids = getProdIdByName(combo, combo.getMatchProdName());
            if(CollectionUtils.isEmpty(ids)){
                ids.add(-1L);
            }
            combo.setMatchProdIdList(ids);
        }
        return comboMapper.pageByParam(page, combo);
    }

    private List<Long> getProdIdByName(Combo combo, String prodName){
        ProductParam productParam = new ProductParam();
        productParam.setShopId(combo.getShopId());
        productParam.setLang(I18nMessage.getDbLang());
        productParam.setProdName(prodName);
        List<Long> ids = new ArrayList<>();
        IPage<Product> productIPage = productService.prodSkuPage(new PageParam<Product>(), productParam);
        productIPage.getRecords().forEach(s-> ids.add(s.getProdId()));
        return ids;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long comboId, Long shopId, Integer status) {
        StatusEnum statusEnum = StatusEnum.instance(status);
        if (Objects.isNull(statusEnum)) {
            return;
        }
        boolean comboUpdateRes = false;
        switch (statusEnum) {
            case DISABLE:
            case DELETE:
                Combo combo = comboMapper.getComboWithProdInfoById(comboId);
                loadMainProd(combo);
                if (Objects.equals(combo.getStatus(), StatusEnum.DELETE.value())) {
                    // 当前套餐已被删除
                    throw new YamiShopBindException("yami.combo.already.delete");
                }
                Long prodId = combo.getMainProd().getProdId();
                comboUpdateRes = this.update(Wrappers.lambdaUpdate(Combo.class)
                        .set(Combo::getStatus, status)
                        .eq(Combo::getComboId, comboId)
                        .eq(Combo::getShopId, shopId)
                        .gt(Combo::getStatus, StatusEnum.DELETE.value())
                );
                //清除缓存
                if (Objects.nonNull(prodId)) {
                    comboProdService.removeComboListCache(prodId);
                }
                basketService.removeByComboIdAndSkuIds(combo.getComboId(), null);
                break;
            default:
                break;
        }
        if (comboUpdateRes) {
            boolean comboProdUpdateRes = comboProdService.update(Wrappers.lambdaUpdate(ComboProd.class)
                    .set(ComboProd::getStatus, status)
                    .eq(ComboProd::getComboId, comboId)
                    .gt(ComboProd::getStatus, status)
            );
            if (!comboProdUpdateRes) {
                throw new YamiShopBindException("yami.combo.delete.fail");
            }
        }
    }

    @Override
    public List<ComboVO> listComboByProdId(Long prodId) {
        long currentTimeMillis = System.currentTimeMillis();
        List<ComboVO> comboList = comboProdService.listCombo(prodId, I18nMessage.getDbLang());
        Iterator<ComboVO> iterator = comboList.iterator();
        while (iterator.hasNext()) {
            ComboVO comboVO = iterator.next();
            // 未到开始时间或已超过结束时间
            if (comboVO.getStartTime().getTime() > currentTimeMillis || comboVO.getEndTime().getTime() <= currentTimeMillis) {
                iterator.remove();
            }
            loadMainComboProdAndProdNum(comboVO, null);
        }
        return comboList;
    }

    private void loadMainComboProdAndProdNum(ComboVO comboVO, Integer required) {
        int prodCount = 0;
        Iterator<ComboProdVO> iterator = comboVO.getMatchingProds().iterator();
        while (iterator.hasNext()) {
            ComboProdVO comboProd = iterator.next();
            if (Objects.isNull(required)) {
                ++prodCount;
            } else if (Objects.equals(comboProd.getRequired(), required)){
                ++prodCount;
            }
            if (Objects.equals(comboProd.getType(), 1)) {
                iterator.remove();
                comboVO.setMainProd(comboProd);
            }
        }
        comboVO.setProdCount(prodCount);
    }

    @Override
    public ComboVO getComboInfoByComboId(Long comboId) {
        ComboVO comboVO = getComboVO(comboId);
        List<Long> prodIds = comboVO.getMatchingProds().stream().map(ComboProdVO::getProdId).collect(Collectors.toList());
        List<Product> productList = productService.listProdAndSku(prodIds);
        Map<Long, Product> productMap = productList.stream().collect(Collectors.toMap(Product::getProdId, p -> p));
        Iterator<ComboProdVO> iterator = comboVO.getMatchingProds().iterator();
        while (iterator.hasNext()) {
            ComboProdVO matchingProd = iterator.next();
            Product product = productMap.get(matchingProd.getProdId());
            if (!Objects.equals(product.getStatus(), ProdStatusEnums.NORMAL.getValue())) {
                iterator.remove();
                continue;
            }
            matchingProd.setProdName(product.getProdName());
            matchingProd.setPic(product.getPic());
            matchingProd.setProdStatus(product.getStatus());
            matchingProd.setPrice(product.getPrice());
            Map<Long, Sku> skuMap = product.getSkuList().stream().collect(Collectors.toMap(Sku::getSkuId, s -> s));
            double skuPrice = 0D;
            for (ComboProdSkuVO comboProdSku : matchingProd.getSkuList()) {
                Sku sku = skuMap.get(comboProdSku.getSkuId());
                comboProdSku.setComboProdId(matchingProd.getComboProdId());
                comboProdSku.setSkuName(sku.getSkuName());
                comboProdSku.setStocks(sku.getStocks());
                comboProdSku.setPic(sku.getPic());
                comboProdSku.setPrice(sku.getPrice());
                comboProdSku.setProperties(sku.getProperties());
                comboProdSku.setProdRequired(matchingProd.getRequired());
                if(skuPrice > comboProdSku.getMatchingPrice() || skuPrice == 0) {
                    skuPrice = comboProdSku.getMatchingPrice();
                }
            }
            matchingProd.setComboPrice(skuPrice);
        }
        loadMainComboProdAndProdNum(comboVO, 1);
        return comboVO;
    }

    @Override
    public ComboVO getComboInfo(ComboVO comboVO) {
        if (Objects.isNull(comboVO.getComboId())) {
            throw new YamiShopBindException("comboId not null");
        }
        if (Objects.isNull(comboVO.getMatchingProds())) {
            comboVO.setMatchingProds(new ArrayList<>());
        }
        ComboVO combo = getComboInfoByComboId(comboVO.getComboId());
        if (CollUtil.isEmpty(combo.getMatchingProds())) {
            return combo;
        }
        combo.getMatchingProds().removeIf(comboProd -> !comboVO.getMatchingProdIds().contains(comboProd.getProdId()));
        return combo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeComboByShopId(Long shopId) {
        // 获取状态为正在进行中的套餐活动
        List<Combo> comboList = comboMapper.selectList(Wrappers.lambdaQuery(Combo.class)
                .select(Combo::getComboId, Combo::getMainProdId)
                .eq(Combo::getShopId, shopId)
                .eq(Combo::getStatus, StatusEnum.ENABLE.value())
        );
        this.closeComboByComboList(comboList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeComboByMainProdId(Long mainProdId) {
        // 获取该主商品下正在进行中的套餐活动
        List<Combo> comboList = comboMapper.selectList(Wrappers.lambdaQuery(Combo.class)
                .eq(Combo::getMainProdId, mainProdId)
                .eq(Combo::getStatus, StatusEnum.ENABLE.value())
        );
        this.closeComboByComboList(comboList);
    }

    @Override
    public void updateSoldNum(Set<Long> comboIds) {
        if (CollUtil.isEmpty(comboIds)) {
            return;
        }
        comboMapper.updateSoldNum(comboIds);
    }

    @Override
    public List<SkuDto> getComboSkuInfo(Long prodId, Long comboId) {
        List<SkuDto> skuList = skuService.getProdDetailSkuInfo(prodId);
        if (CollUtil.isEmpty(skuList)) {
            return new ArrayList<>();
        }
        List<ComboProdSku> comboProdSkus = comboProdSkuService.listByProdIdAndComboId(prodId, comboId);
        if(CollUtil.isEmpty(comboProdSkus)) {
            return new ArrayList<>();
        }
        Map<Long, ComboProdSku> skuMap = comboProdSkus.stream().collect(Collectors.toMap(ComboProdSku::getSkuId, c -> c));
        Iterator<SkuDto> iterator = skuList.iterator();
        while (iterator.hasNext()) {
            SkuDto skuDto = iterator.next();
            ComboProdSku comboProdSku = skuMap.get(skuDto.getSkuId());
            if (Objects.isNull(comboProdSku)) {
                iterator.remove();
                continue;
            }
            skuDto.setOriPrice(skuDto.getPrice());
            skuDto.setPrice(comboProdSku.getMatchingPrice());
        }
        return skuList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeComboByEndDate() {
        Date now = new Date();
        // 获取失效的套餐数据，只失效此时获取到的数据
        List<Combo> comboList = comboMapper.selectList(Wrappers.lambdaQuery(Combo.class)
                .select(Combo::getComboId, Combo::getMainProdId)
                .eq(Combo::getStatus, StatusEnum.ENABLE.value())
                .lt(Combo::getEndTime, now)
        );
        this.closeComboByComboList(comboList);
    }

    @Override
    public void enableCombo() {
        //获取符合条件的套餐id
        List<Combo> comboList = comboMapper.notStartComboIdButStart();
        List<Long> comboIdList = new ArrayList<>();
        for (Combo combo : comboList) {
            comboIdList.add(combo.getComboId());
            comboProdService.removeComboListCache(combo.getMainProdId());
        }
        if (comboIdList.size() == 0){
            return;
        }
        //开启
        update(Wrappers.lambdaUpdate(Combo.class)
                .set(Combo::getStatus, StatusEnum.ENABLE.value())
                .in(Combo::getComboId, comboIdList)
                .eq(Combo::getStatus, StatusEnum.OFFLINE.value())
        );
        //开启套餐商品
        comboProdService.update(Wrappers.lambdaUpdate(ComboProd.class)
                .set(ComboProd::getStatus, StatusEnum.ENABLE.value())
                .in(ComboProd::getComboId, comboIdList)
                .eq(ComboProd::getStatus, StatusEnum.OFFLINE.value())
        );
    }

    @Override
    public ComboVO getComboVO(Long comboId) {
        ComboVO comboVO = comboMapper.getComboInfoByComboId(comboId);
        if (Objects.isNull(comboVO)) {
            // 套餐不存在，或已失效
            throw new YamiShopBindException("yami.prod.combo.expired");
        }
        return comboVO;
    }

    @Override
    public void closeComboByComboList(List<Combo> comboList) {
        if (CollUtil.isEmpty(comboList)) {
            return;
        }
        List<Long> comboIds = new ArrayList<>();
        List<String> comboProdCacheKeys = new ArrayList<>();
        for (Combo combo : comboList) {
            comboIds.add(combo.getComboId());
            comboProdService.removeComboListCache(combo.getMainProdId());
        }
        // 失效套餐
        update(Wrappers.lambdaUpdate(Combo.class)
                .set(Combo::getStatus, StatusEnum.DISABLE.value())
                .in(Combo::getComboId, comboIds)
                .eq(Combo::getStatus, StatusEnum.ENABLE.value())
        );
        // 失效套餐商品
        comboProdService.update(Wrappers.lambdaUpdate(ComboProd.class)
                .set(ComboProd::getStatus, StatusEnum.DISABLE.value())
                .in(ComboProd::getComboId, comboIds)
                .eq(ComboProd::getStatus, StatusEnum.ENABLE.value())
        );
        // 删除失效缓存
        RedisUtil.deleteBatch(comboProdCacheKeys);
        // 删除购物车中的套餐
        for (Combo combo : comboList) {
            basketService.removeByComboIdAndSkuIds(combo.getComboId(), null);
        }
    }

    @Override
    public List<ComboSkuDto> listComboByComboIds(Set<Long> comboIds, List<Long> skuIds) {
        if (CollUtil.isEmpty(comboIds) || CollUtil.isEmpty(skuIds)) {
            return new ArrayList<>();
        }
        return comboMapper.listComboByComboIds(comboIds, skuIds, I18nMessage.getDbLang());
    }

    /**
     * 检查套餐信息
     *
     * @param combo
     */
    private void checkComboInfo(Combo combo) {
        if (DateUtil.compare(combo.getStartTime(), combo.getEndTime()) > 0) {
            // 活动开始时间不能大于结束时间
            throw new YamiShopBindException("yami.live.time.check");
        }
        if (Objects.isNull(combo.getComboId()) && DateUtil.compare(combo.getEndTime(), new Date()) < 0) {
            // 新增套餐时活动结束时间不能小于当前时间
            throw new YamiShopBindException("yami.combo.end.time.error");
        }
        if (Objects.isNull(combo.getMainProd())) {
            // 套餐主商品不能为空
            throw new YamiShopBindException("yami.combo.main.prod.not.empty");
        }
        if (Objects.nonNull(combo.getMatchingProds()) && combo.getMatchingProds().size() > MATCHING_PROD_LIMIT) {
            // 搭配商品数量错误
            throw new YamiShopBindException("yami.combo.matching.prod.count.error");
        }
        ComboProd mainProd = combo.getMainProd();
        List<ComboProd> matchingProds = combo.getMatchingProds();
        // 套餐价格
        combo.setPrice(0.0);
        // 校验主商品
        this.checkComboProdInfo(combo, mainProd, combo.getShopId(), ComboProdType.MAIN_PROD.value());
        // 校验搭配商品
        for (ComboProd matchingProd : matchingProds) {
            this.checkComboProdInfo(combo, matchingProd, combo.getShopId(), ComboProdType.MATCHING_PROD.value());
            if (!Objects.equals(matchingProd.getRequired(), IsRequired.YES.value())) {
                matchingProd.setRequired(IsRequired.NO.value());
            }
        }
    }

    /**
     * 校验商品项信息
     *
     * @param combo
     * @param comboProd
     * @param shopId
     * @param comboProdType
     */
    private void checkComboProdInfo(Combo combo, ComboProd comboProd, Long shopId, Integer comboProdType) {
        if (Objects.isNull(comboProd.getLeastNum()) || comboProd.getLeastNum() == 0) {
            throw new YamiShopBindException("yami.combo.least.num.not.empty");
        }
        Product product = productService.getProductByProdId(comboProd.getProdId(), I18nMessage.getLang());
        if (Objects.isNull(product) || !Objects.equals(product.getShopId(), shopId)) {
            // 找不到商品或者商品的shopId与当前用户的shopId不相等
            throw new YamiShopBindException("yami.combo.not.find.prod");
        }
        if (Objects.equals(product.getStatus(), StatusEnum.DELETE.value())) {
            // 商品已被删除
            throw new YamiShopBindException(product.getProdName() + I18nMessage.getMessage("yami.combo.product.already.delete.rear.tips"));
        }
        Double minPrice = 0.0;
        for (ComboProdSku comboProdSku : comboProd.getSkuList()) {
            Sku sku = skuService.getSkuBySkuId(comboProdSku.getSkuId(), I18nMessage.getLang());
            if (Objects.isNull(sku)) {
                // 存在错误的商品，请刷新后重新选择
                throw new YamiShopBindException("yami.giveaway.prod.error");
            }
            if (!Objects.equals(comboProd.getProdId(), sku.getProdId())) {
                // skuId与prodId不匹配
                throw new YamiShopBindException(sku.getSkuName() + I18nMessage.getMessage("yami.combo.info.error"));
            }
            if (sku.getIsDelete() ==1) {
                // 规格已被删除
                throw new YamiShopBindException(product.getProdName() + I18nMessage.getMessage("yami.combo.spec.already") + sku.getSkuName() + I18nMessage.getMessage("yami.combo.product.already.delete.rear.tips"));
            }
            if (Objects.equals(sku.getStatus(), StatusEnum.DISABLE.value())) {
                // 规格已被禁用
                throw new YamiShopBindException(product.getProdName() + I18nMessage.getMessage("yami.combo.spec.already") + sku.getSkuName() + I18nMessage.getMessage("yami.combo.product.already.disable.rear.tips"));
            }
            if (sku.getPrice() < comboProdSku.getMatchingPrice()) {
                // sku搭配价格不能超过商品原价
                throw new YamiShopBindException(product.getProdName() + " " + sku.getSkuName() + I18nMessage.getMessage("yami.combo.prod.matching.price.after.tips"));
            }
            minPrice = minPrice == 0.0 ? sku.getPrice() : (sku.getPrice() < minPrice ? sku.getPrice() : minPrice);
        }
        // 设置商品项默认值
        if (Objects.equals(comboProdType, ComboProdType.MAIN_PROD.value())) {
            // 主商品
            comboProd.setType(ComboProdType.MAIN_PROD.value());
            comboProd.setRequired(IsRequired.YES.value());
        } else {
            // 搭配商品
            comboProd.setType(ComboProdType.MATCHING_PROD.value());
        }
        if (Objects.equals(comboProd.getRequired(), IsRequired.YES.value())) {
            combo.setPrice(combo.getPrice() + (minPrice * comboProd.getLeastNum()));
        }
        comboProd.setStatus(StatusEnum.ENABLE.value());
    }


    private void loadMainProd(Combo combo) {
        Iterator<ComboProd> iterator = combo.getMatchingProds().iterator();
        while (iterator.hasNext()) {
            ComboProd comboProd = iterator.next();
            if(comboProd.getType() == 1) {
                iterator.remove();
                combo.setMainProd(comboProd);
                return;
            }
        }
    }
}
