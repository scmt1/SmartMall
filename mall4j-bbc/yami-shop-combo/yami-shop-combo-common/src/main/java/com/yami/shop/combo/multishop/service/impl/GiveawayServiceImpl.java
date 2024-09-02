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
import com.yami.shop.bean.model.Giveaway;
import com.yami.shop.bean.model.GiveawayProd;
import com.yami.shop.bean.model.Product;
import com.yami.shop.bean.model.Sku;
import com.yami.shop.bean.vo.GiveawayProdVO;
import com.yami.shop.bean.vo.GiveawayVO;
import com.yami.shop.combo.multishop.dao.GiveawayMapper;
import com.yami.shop.combo.multishop.service.GiveawayProdService;
import com.yami.shop.combo.multishop.service.GiveawayService;
import com.yami.shop.common.constants.CacheNames;
import com.yami.shop.common.enums.StatusEnum;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.i18n.I18nMessage;
import com.yami.shop.common.util.Arith;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.common.util.RedisUtil;
import com.yami.shop.service.ProductService;
import com.yami.shop.service.SkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 赠品
 *
 * @author LGH
 * @date 2021-11-08 13:29:16
 */
@Service
public class GiveawayServiceImpl extends ServiceImpl<GiveawayMapper, Giveaway> implements GiveawayService {

    @Autowired
    private GiveawayMapper giveawayMapper;
    @Autowired
    private ProductService productService;
    @Autowired
    private SkuService skuService;
    @Autowired
    private GiveawayProdService giveawayProdService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveInfo(Giveaway giveaway) {
        Date now = new Date();
        this.checkGiveawayInfo(giveaway);
        giveaway.setCreateTime(now);
        giveaway.setUpdateTime(now);
        giveaway.setStatus(StatusEnum.ENABLE.value());
        if(giveaway.getStartTime().getTime() > now.getTime()){
            giveaway.setStatus(StatusEnum.OFFLINE.value());
        }
        // 保存赠品信息
        giveawayMapper.insert(giveaway);
        // 保存赠品赠送商品信息
        giveawayProdService.insertBatch(giveaway.getGiveawayProds(), giveaway.getGiveawayId());
        //清除缓存
        giveawayProdService.removeGiveawayCacheByProdId(giveaway.getProdId());
    }

    @Override
    public IPage<Giveaway> pageByParam(PageParam<Giveaway> page, Giveaway giveaway) {
        return giveawayMapper.pageByParam(page, giveaway, I18nMessage.getLang());
    }

    @Override
    public Giveaway getInfoById(Long giveawayId) {
        Giveaway giveaway = giveawayMapper.getInfoById(giveawayId, I18nMessage.getLang());
        List<GiveawayProd> giveawayProds = giveawayProdService.list(Wrappers.lambdaQuery(GiveawayProd.class)
                .eq(GiveawayProd::getGiveawayId, giveawayId)
                .gt(GiveawayProd::getStatus, StatusEnum.DELETE.value())
        );
        giveawayProds.forEach(this::addProdAndSkuInfoHandler);
        giveaway.setGiveawayProds(giveawayProds);
        return giveaway;
    }

    private void addProdAndSkuInfoHandler(GiveawayProd giveawayProd) {
        Integer lang = I18nMessage.getDbLang();
        Product product = productService.getProductByProdId(giveawayProd.getProdId(), lang);
        if (Objects.nonNull(product)) {
            giveawayProd.setProdName(product.getProdName());
            giveawayProd.setPic(product.getPic());
            Sku sku = skuService.getSkuBySkuId(giveawayProd.getSkuId(), lang);
            if (Objects.nonNull(sku)) {
                giveawayProd.setSkuName(sku.getSkuName());
                giveawayProd.setPrice(sku.getPrice());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInfo(Giveaway giveaway) {
        Date now = new Date();
        this.checkGiveawayInfo(giveaway);
        Giveaway dbGiveaway = giveawayMapper.selectById(giveaway.getGiveawayId());
        if (Objects.isNull(dbGiveaway) || Objects.equals(dbGiveaway.getStatus(), StatusEnum.DELETE.value())) {
            // 找不到赠品或赠品已被删除
            throw new YamiShopBindException("yami.giveaway.already.delete");
        }
        if (Objects.equals(dbGiveaway.getStatus(), StatusEnum.DISABLE.value())) {
            // 当前赠品已失效
            throw new YamiShopBindException("yami.giveaway.already.invalid");
        }
        if (!Objects.equals(dbGiveaway.getShopId(), giveaway.getShopId())) {
            // 店铺id不相等
            throw new YamiShopBindException("yami.giveaway.not.on.shop");
        }
        giveaway.setStatus(StatusEnum.ENABLE.value());
        if(giveaway.getStartTime().getTime() > now.getTime()){
            giveaway.setStatus(StatusEnum.OFFLINE.value());
        }
        giveaway.setUpdateTime(now);
        List<Long> deleteGiveawayProdIds = new ArrayList<>();
        // 获取旧的赠送商品数据
        List<GiveawayProd> dbGiveawayProds = giveawayProdService.list(Wrappers.lambdaQuery(GiveawayProd.class)
                .select(GiveawayProd::getGiveawayProdId, GiveawayProd::getSkuId)
                .eq(GiveawayProd::getGiveawayId, giveaway.getGiveawayId())
                .eq(GiveawayProd::getStatus, StatusEnum.ENABLE.value())
        );
        for (GiveawayProd giveawayProd : giveaway.getGiveawayProds()) {
            giveawayProd.setGiveawayId(giveaway.getGiveawayId());
            int index = 0;
            for (; index < dbGiveawayProds.size(); index++) {
                if (Objects.equals(giveawayProd.getSkuId(), dbGiveawayProds.get(index).getSkuId())) {
                    giveawayProd.setGiveawayProdId(dbGiveawayProds.get(index).getGiveawayProdId());
                    break;
                }
            }
            if (index < dbGiveawayProds.size()) {
                dbGiveawayProds.remove(index);
            }
        }
        dbGiveawayProds.forEach(giveawayProd -> {
            deleteGiveawayProdIds.add(giveawayProd.getGiveawayProdId());
        });
        int giveawayUpdateCount = giveawayMapper.update(giveaway, Wrappers.lambdaUpdate(Giveaway.class)
                .eq(Giveaway::getGiveawayId, giveaway.getGiveawayId())
                .in(Giveaway::getStatus, StatusEnum.ENABLE.value(), StatusEnum.OFFLINE.value())
        );
        if (giveawayUpdateCount < 1) {
            throw new YamiShopBindException("yami.giveaway.update.fail");
        }
        giveawayProdService.saveOrUpdateBatch(giveaway.getGiveawayProds());
        if (CollUtil.isNotEmpty(deleteGiveawayProdIds)) {
            giveawayProdService.update(Wrappers.lambdaUpdate(GiveawayProd.class)
                    .set(GiveawayProd::getStatus, StatusEnum.DELETE.value())
                    .in(GiveawayProd::getGiveawayProdId, deleteGiveawayProdIds)
            );
        }
        //清除缓存
        giveawayProdService.removeGiveawayCacheByProdId(giveaway.getProdId());
        giveawayProdService.removeGiveawayCacheByProdId(dbGiveaway.getProdId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long giveawayId, Long shopId, Integer status) {
        StatusEnum statusEnum = StatusEnum.instance(status);
        if (Objects.isNull(statusEnum)) {
            return;
        }
        boolean giveawayUpdateRes = false;
        switch (statusEnum) {
            case DISABLE:
            case DELETE:
                Giveaway giveaway = giveawayMapper.selectById(giveawayId);
                if (Objects.isNull(giveaway) || Objects.equals(giveaway.getStatus(), StatusEnum.DELETE.value())) {
                    // 当前赠品已被删除
                    throw new YamiShopBindException("yami.giveaway.already.delete");
                }
                giveawayUpdateRes = this.update(Wrappers.lambdaUpdate(Giveaway.class)
                        .set(Giveaway::getStatus, status)
                        .eq(Giveaway::getGiveawayId, giveawayId)
                        .eq(Giveaway::getShopId, shopId)
                        .gt(Giveaway::getStatus, StatusEnum.DELETE.value())
                );
                //清除缓存
                giveawayProdService.removeGiveawayCacheByProdId(giveaway.getProdId());
                break;
            default:
                break;
        }
        if (giveawayUpdateRes) {
            boolean giveawayProdUpdateRes = giveawayProdService.update(Wrappers.lambdaUpdate(GiveawayProd.class)
                    .set(GiveawayProd::getStatus, status)
                    .eq(GiveawayProd::getGiveawayId, giveawayId)
                    .gt(GiveawayProd::getStatus, status)
            );
            if (!giveawayProdUpdateRes) {
                throw new YamiShopBindException("yami.combo.delete.fail");
            }
        }
    }

    @Override
    public GiveawayVO getGiveawayProdByProdId(Long prodId) {
        GiveawayVO giveaway = giveawayProdService.getGiveawayByProdId(prodId);
        if (Objects.isNull(giveaway)) {
            return null;
        }
        long currentTimeMillis = System.currentTimeMillis();
        // 未到开始时间或已超过结束时间
        if (giveaway.getStartTime().getTime() > currentTimeMillis || giveaway.getEndTime().getTime() <= currentTimeMillis) {
            return null;
        }
        List<GiveawayProdVO> giveawayProdList = giveaway.getGiveawayProds();
        List<GiveawayProdVO> resGiveawayProds = new ArrayList<>();
        if (CollUtil.isNotEmpty(giveawayProdList)) {
            giveawayProdList.forEach(giveawayProd -> {
                boolean flag = addProdAndSkuInfoHandler(giveawayProd);
                if (flag) {
                    resGiveawayProds.add(giveawayProd);
                }
            });
        }

        giveaway.setGiveawayProds(resGiveawayProds);
        return giveaway;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeGiveawayByEndDate() {
        Date now = new Date();
        // 获取失效的数据，只失效此时获取到的赠品数据
        List<Giveaway> giveawayList = giveawayMapper.selectList(Wrappers.lambdaQuery(Giveaway.class)
                .select(Giveaway::getGiveawayId, Giveaway::getProdId)
                .eq(Giveaway::getStatus, StatusEnum.ENABLE.value())
                .le(Giveaway::getEndTime, now)
        );
        this.closeGiveawayByGiveawayList(giveawayList);
    }

    @Override
    public void startGiveawayActivity(){
        Date now = new Date();
        // 获取未启动的数据，只启动此时获取到的赠品数据
        List<Giveaway> giveawayList = giveawayMapper.selectList(Wrappers.lambdaQuery(Giveaway.class)
                .select(Giveaway::getGiveawayId, Giveaway::getProdId)
                .eq(Giveaway::getStatus, StatusEnum.OFFLINE.value())
                .le(Giveaway::getStartTime, now)
        );
        List<Long> idList = new ArrayList<>();
        giveawayList.forEach(s->idList.add(s.getProdId()));
        this.update(Wrappers.lambdaUpdate(Giveaway.class)
                        .set(Giveaway::getStatus, StatusEnum.ENABLE.value())
                        .eq(Giveaway::getStatus, StatusEnum.OFFLINE.value())
                        .le(Giveaway::getStartTime, now)
        );
        giveawayProdService.removeGiveawayCacheBatch(idList);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeGiveawayByShopId(Long shopId) {
        // 获取店铺中状态为进行中的赠品活动
        List<Giveaway> giveawayList = giveawayMapper.selectList(Wrappers.lambdaQuery(Giveaway.class)
                .select(Giveaway::getGiveawayId, Giveaway::getProdId)
                .eq(Giveaway::getShopId, shopId)
                .eq(Giveaway::getStatus, StatusEnum.ENABLE.value())
        );
        this.closeGiveawayByGiveawayList(giveawayList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeGiveawayByMainProdId(Long prodId) {
        List<Giveaway> giveawayList = giveawayMapper.selectList(Wrappers.lambdaQuery(Giveaway.class)
                .select(Giveaway::getGiveawayId, Giveaway::getProdId)
                .eq(Giveaway::getProdId, prodId)
                .eq(Giveaway::getStatus, StatusEnum.ENABLE.value())
        );
        this.closeGiveawayByGiveawayList(giveawayList);
    }

    @Override
    public List<Long> listMainProdIdByGiveawayProdId(Long prodId) {
        return giveawayMapper.listMainProdIdByGiveawayProdId(prodId);
    }

    @Override
    public GiveawayVO getGiveawayProdAndStockByProdId(Long prodId) {
        GiveawayVO giveawayVO = getGiveawayProdByProdId(prodId);
        if (Objects.isNull(giveawayVO)) {
            return giveawayVO;
        }
        List<Long> skuIds = giveawayVO.getGiveawayProds().stream().map(GiveawayProdVO::getSkuId).collect(Collectors.toList());
        List<Sku> skuList;
        if (CollUtil.isEmpty(skuIds)) {
            skuList = new ArrayList<>();
        } else {
            skuList = skuService.getSkuBySkuIds(skuIds);
        }
        Map<Long, Integer> skuMap = skuList.stream().collect(Collectors.toMap(Sku::getSkuId, Sku::getStocks));
        for (GiveawayProdVO giveawayProd : giveawayVO.getGiveawayProds()) {
            Integer stock = skuMap.get(giveawayProd.getSkuId());
            giveawayProd.setSkuStock(Objects.isNull(stock) ? 0 : stock);
        }
        return giveawayVO;
    }

    public void closeGiveawayByGiveawayList(List<Giveaway> giveawayList) {
        if (CollUtil.isEmpty(giveawayList)) {
            return;
        }
        List<Long> giveawayIds = new ArrayList<>();
        List<String> giveawayProdCacheKeys = new ArrayList<>();
        for (Giveaway giveaway : giveawayList) {
            giveawayIds.add(giveaway.getGiveawayId());
            giveawayProdCacheKeys.add("getGiveawayByProdId" + CacheNames.UNION + giveaway.getProdId());
        }
        // 失效赠品
        update(Wrappers.lambdaUpdate(Giveaway.class)
                .set(Giveaway::getStatus, StatusEnum.DISABLE.value())
                .in(Giveaway::getGiveawayId, giveawayIds)
                .eq(Giveaway::getStatus, StatusEnum.ENABLE.value())
        );
        // 失效赠送商品
        giveawayProdService.update(Wrappers.lambdaUpdate(GiveawayProd.class)
                .set(GiveawayProd::getStatus, StatusEnum.DISABLE.value())
                .in(GiveawayProd::getGiveawayId, giveawayIds)
                .eq(GiveawayProd::getStatus, StatusEnum.ENABLE.value())
        );
        // 删除失效缓存
        RedisUtil.deleteBatch(giveawayProdCacheKeys);
    }

    private boolean addProdAndSkuInfoHandler(GiveawayProdVO giveawayProd) {
        Integer lang = I18nMessage.getDbLang();
        Product product = productService.getProductByProdId(giveawayProd.getProdId(), lang);
        if (Objects.isNull(product)) {
            return false;
        } else {
            giveawayProd.setProdName(product.getProdName());
            Sku sku = skuService.getSkuBySkuId(giveawayProd.getSkuId(), lang);
            if (Objects.isNull(sku) || sku.getStocks() <= 0) {
                return false;
            } else {
                giveawayProd.setSkuName(sku.getSkuName());
            }
        }
        return true;
    }

    /**
     * 检查赠品信息
     *
     * @param giveaway
     */
    private void checkGiveawayInfo(Giveaway giveaway) {
        if (DateUtil.compare(giveaway.getStartTime(), giveaway.getEndTime()) > 0) {
            // 活动开始时间不能大于结束时间
            throw new YamiShopBindException("yami.live.time.check");
        }
        if (Objects.isNull(giveaway.getGiveawayId()) && DateUtil.compare(giveaway.getEndTime(), new Date()) < 0) {
            // 新增时活动结束时间不能小于当前时间
            throw new YamiShopBindException("yami.combo.end.time.error");
        }
        // 主商品
        Product mainProduct = productService.getProductByProdId(giveaway.getProdId(), I18nMessage.getLang());
        if (Objects.isNull(mainProduct) || !Objects.equals(mainProduct.getShopId(), giveaway.getShopId())) {
            // 商品找不到，或店铺id不相等
            throw new YamiShopBindException("yami.giveaway.prod.error");
        }
        if (Objects.equals(mainProduct.getStatus(), StatusEnum.DELETE.value())) {
            throw new YamiShopBindException(mainProduct.getProdName() + I18nMessage.getMessage("yami.combo.product.already.delete.rear.tips"));
        }
        int count;
        if (Objects.isNull(giveaway.getGiveawayId())) {
            count = count(Wrappers.lambdaQuery(Giveaway.class)
                    .eq(Giveaway::getProdId, giveaway.getProdId())
                    .in(Giveaway::getStatus, StatusEnum.ENABLE.value(), StatusEnum.OFFLINE.value()));
        } else {
            count = count(Wrappers.lambdaQuery(Giveaway.class).eq(Giveaway::getProdId, giveaway.getProdId())
                    .ne(Giveaway::getGiveawayId, giveaway.getGiveawayId())
                    .in(Giveaway::getStatus, StatusEnum.ENABLE.value(), StatusEnum.OFFLINE.value()));
        }
        if (count > 0) {
            // 一个主商品不能设置多个赠品活动
            throw new YamiShopBindException("yami.giveaway.activity.time.error.tips5");
        }
        // 检查时间段是否冲突
        this.checkTimeIsRepeat(giveaway.getProdId(), giveaway.getStartTime(), giveaway.getEndTime(), giveaway.getGiveawayId());
        Map<Long, Product> productMap = new HashMap<>(16);
        productMap.put(mainProduct.getProdId(), mainProduct);
        List<GiveawayProd> giveawayProds = giveaway.getGiveawayProds();
        double totalRefundPrice = 0.0;
        // 赠送商品
        for (GiveawayProd giveawayProd : giveawayProds) {
            Product product = productMap.getOrDefault(giveawayProd.getProdId(), productService.getProductByProdId(giveawayProd.getProdId(), I18nMessage.getLang()));
            if (Objects.isNull(product) || !Objects.equals(product.getShopId(), giveaway.getShopId())) {
                // 商品找不到，或店铺id不相等
                throw new YamiShopBindException("yami.giveaway.prod.error");
            }
            if (Objects.equals(product.getStatus(), StatusEnum.DELETE.value())) {
                // 商品已被删除
                throw new YamiShopBindException(product.getProdName() + I18nMessage.getMessage("yami.combo.product.already.delete.rear.tips"));
            }
            if (Objects.equals(product.getMold(), 1)) {
                // 赠送的商品不能是虚拟商品
                throw new YamiShopBindException(product.getProdName() + I18nMessage.getMessage("yami.giveaway.virtual.prod.error"));
            }
            Sku sku = skuService.getSkuBySkuId(giveawayProd.getSkuId(), I18nMessage.getLang());
            if (Objects.isNull(sku) || !Objects.equals(sku.getProdId(), product.getProdId())) {
                // sku找不到，或商品id不相等
                throw new YamiShopBindException("yami.giveaway.prod.error");
            }
            if (Objects.equals(sku.getIsDelete(), 1)) {
                // 规格已被删除
                throw new YamiShopBindException(product.getProdName() + I18nMessage.getMessage("yami.combo.spec.already") + sku.getSkuName() + I18nMessage.getMessage("yami.combo.product.already.delete.rear.tips"));
            }
            if (Objects.equals(sku.getStatus(), StatusEnum.DISABLE.value())) {
                // 规格已被禁用
                throw new YamiShopBindException(product.getProdName() + I18nMessage.getMessage("yami.combo.spec.already") + sku.getSkuName() + I18nMessage.getMessage("yami.combo.product.already.disable.rear.tips"));
            }
            if (Objects.isNull(giveawayProd.getGiveawayNum()) || giveawayProd.getGiveawayNum() <= 0) {
                giveawayProd.setGiveawayNum(1);
            }
            if (Objects.isNull(giveawayProd.getRefundPrice()) || giveawayProd.getRefundPrice() < 0) {
                giveawayProd.setRefundPrice(0.0);
            }
            totalRefundPrice = Arith.add(totalRefundPrice, Arith.mul(giveawayProd.getRefundPrice(), giveawayProd.getGiveawayNum()));
            productMap.put(product.getProdId(), product);
        }
        double totalMainProductPrice = Arith.mul(mainProduct.getPrice(), giveaway.getBuyNum());
        if (totalMainProductPrice < totalRefundPrice) {
            // 赠品的总售后价不能超过主商品价格
            throw new YamiShopBindException("yami.giveaway.refund.price.error");
        }
    }

    /**
     * 检查时间段是否重复
     *
     * @param prodId
     * @param startTime
     * @param endTime
     */
    private void checkTimeIsRepeat(Long prodId, Date startTime, Date endTime, Long giveawayId) {
        List<Giveaway> dbGiveawayList = giveawayMapper.selectList(Wrappers.lambdaQuery(Giveaway.class)
                .select(Giveaway::getName, Giveaway::getStartTime, Giveaway::getEndTime)
                .eq(Giveaway::getProdId, prodId)
                .eq(Giveaway::getStatus, StatusEnum.ENABLE.value())
                .ne(Objects.nonNull(giveawayId), Giveaway::getGiveawayId, giveawayId)
        );
        for (Giveaway giveaway : dbGiveawayList) {
            if (DateUtil.compare(startTime, giveaway.getEndTime()) > 0 || DateUtil.compare(endTime, giveaway.getStartTime()) < 0) {
                continue;
            }
            // 时间冲突，一个主商品同一时间不能设置多个赠品活动
            throw new YamiShopBindException(I18nMessage.getMessage("yami.giveaway.activity.time.error.tips1") + giveaway.getName()
                    + I18nMessage.getMessage("yami.giveaway.activity.time.error.tips2")
                    + giveaway.getName() + I18nMessage.getMessage("yami.giveaway.activity.time.error.tips3")
                    + DateUtil.formatDateTime(giveaway.getStartTime())
                    + I18nMessage.getMessage("yami.giveaway.time.to")
                    + DateUtil.formatDateTime(giveaway.getEndTime()));
        }
    }
}
