/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.combo.multishop.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yami.shop.bean.model.GiveawayProd;
import com.yami.shop.bean.model.Product;
import com.yami.shop.bean.vo.GiveawayVO;
import com.yami.shop.common.util.PageParam;

import java.util.List;

/**
 * 套装商品项
 *
 * @author LGH
 * @date 2021-11-08 13:29:16
 */
public interface GiveawayProdService extends IService<GiveawayProd> {

    /**
     * 保存赠品商品项信息
     * @param giveawayProds
     * @param giveawayId
     */
    void insertBatch(List<GiveawayProd> giveawayProds, Long giveawayId);

    /**
     * 根据商品id获取赠品信息[有缓存]
     * @param prodId
     * @return
     */
    GiveawayVO getGiveawayByProdId(Long prodId);

    /**
     * 根据商品id删除缓存
     * @param prodId
     */
    void removeGiveawayCacheByProdId(Long prodId);

    /**
     * 根据商品id删除缓存
     * @param prodIds
     */
    void removeGiveawayCacheBatch(List<Long> prodIds);

    /**
     * 分页获取主商品
     * @param page
     * @param giveawayProd
     * @return
     */
    IPage<Product> getMainProdPage(PageParam<Product> page, GiveawayProd giveawayProd);

//    /**
//     *
//     * @param prodId
//     * @return
//     */
//    GiveawayVO getGiveawayAndStockByProdId(Long prodId);
}
