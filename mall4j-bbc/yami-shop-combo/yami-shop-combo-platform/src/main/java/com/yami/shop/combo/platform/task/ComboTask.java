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

import com.xxl.job.core.handler.annotation.XxlJob;
import com.yami.shop.combo.multishop.service.ComboService;
import com.yami.shop.service.ProductService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * @Author lth
 * @Date 2021/11/12 15:07
 */
@Slf4j
@Component("comboTask")
@AllArgsConstructor
public class ComboTask {

    private final ComboService comboService;
    private final ProductService productService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 关闭已经结束的套餐活动
     */
    @XxlJob("closeCombo")
    public void closeCombo() {
        comboService.closeComboByEndDate();
    }

    /**
     * 开启到了活动时间未开启的套餐活动
     */
    @XxlJob("enableCombo")
    public void enableCombo(){
        comboService.enableCombo();
    }
}
