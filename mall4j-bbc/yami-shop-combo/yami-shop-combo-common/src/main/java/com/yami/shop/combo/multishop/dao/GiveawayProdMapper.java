/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.combo.multishop.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yami.shop.bean.model.GiveawayProd;
import com.yami.shop.bean.model.Product;
import com.yami.shop.bean.vo.GiveawayVO;
import com.yami.shop.common.util.PageParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 套装商品项
 *
 * @author LGH
 * @date 2021-11-08 13:29:16
 */
public interface GiveawayProdMapper extends BaseMapper<GiveawayProd> {

    /**
     * 批量保存赠品商品项信息
     *
     * @param giveawayProds
     * @param giveawayId
     */
    void insertBatch(@Param("giveawayProds") List<GiveawayProd> giveawayProds, @Param("giveawayId") Long giveawayId);


    /**
     * 根据商品id获取赠品信息
     *
     * @param prodId
     * @return
     */
    GiveawayVO getGiveawayByProdId(@Param("prodId") Long prodId);

    /**
     * 获取主赠送商品
     * @param page
     * @param giveawayProd
     * @param lang
     * @return
     */
    IPage<Product> getMainProdPage(@Param("page") PageParam<Product> page, @Param("giveawayProd") GiveawayProd giveawayProd, @Param("lang") Integer lang);
}
