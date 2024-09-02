/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.card.common.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yami.shop.card.common.dto.CardUserDto;
import com.yami.shop.card.common.model.CardUser;
import com.yami.shop.common.util.PageParam;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 优惠券
 *
 * @author lanhai
 */
public interface CardUserMapper extends BaseMapper<CardUser> {

    List<CardUserDto> getCardUserList(@Param("cardUser") CardUser cardUser);

    int updateBalance(@Param("cardUserId") Long cardUserId,@Param("reduceAmount") Double reduceAmount);

    IPage<CardUser> getCardUserPage(PageParam<CardUser> page,@Param("cardUser") CardUser cardUser);

    List<CardUser> downloadBuyRecord(@Param("cardUser") CardUser cardUser);

    /**
     * 设置用户的过期提货卡为失效状态
     *
     * @param now 时间
     */
    void updateCardUserStatusByTime(@Param("now") Date now);

    /**
     * 推送即将失效的提货卡短信给用户
     *
     */
    List<CardUser> cardSendMessage();
}
