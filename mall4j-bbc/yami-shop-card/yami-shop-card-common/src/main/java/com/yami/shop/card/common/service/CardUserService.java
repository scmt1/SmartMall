/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.card.common.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yami.shop.card.common.model.CardUser;
import com.yami.shop.card.common.dto.CardUserDto;
import com.yami.shop.common.util.PageParam;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

/**
 * @author lgh on 2018/12/27.
 */
public interface CardUserService extends IService<CardUser> {

    List<CardUserDto> getCardUserList(CardUser cardUser);

    int updateBalance(Long cardUserId, Double reduceAmount);

    IPage<CardUser> getCardUserPage(PageParam<CardUser> page, CardUser cardUser);

    /**
     * 功能描述： 导出店铺核销提货卡(券)数据
     * @param cardUser 查询参数
     * @param response response参数
     */
    public void downloadBuyRecord(CardUser cardUser, HttpServletResponse response);

    /**
     * 设置用户的过期提货卡为失效状态
     * @param now 时间
     */
    void updateCardUserStatusByTime(Date now);

    /**
     * 推送即将失效的提货卡短信给用户
     */
    List<CardUser> cardSendMessage();
}
