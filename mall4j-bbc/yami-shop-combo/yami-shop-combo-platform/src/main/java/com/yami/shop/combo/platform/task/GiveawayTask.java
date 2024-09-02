/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.combo.platform.task;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.yami.shop.combo.multishop.service.GiveawayService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @Author lth
 * @Date 2021/11/12 15:07
 */
@Slf4j
@Component("giveawayTask")
@AllArgsConstructor
public class GiveawayTask {

    private final GiveawayService giveawayService;

    /**
     * 关闭已经结束的赠品活动
     */
    @XxlJob("closeGiveaway")
    public void closeGiveaway() {
        giveawayService.closeGiveawayByEndDate();
    }

    /**
     * 启用赠品活动
     */
    @XxlJob("enableGiveaway")
    public void activityStartAndProdChange(){
        XxlJobHelper.log("赠品活动开启");
        giveawayService.startGiveawayActivity();
    }



}
