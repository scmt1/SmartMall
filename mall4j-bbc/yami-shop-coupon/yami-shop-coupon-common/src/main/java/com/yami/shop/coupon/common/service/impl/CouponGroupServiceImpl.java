/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.coupon.common.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yami.shop.bean.app.dto.CouponDto;
import com.yami.shop.bean.app.dto.CouponOrderDto;
import com.yami.shop.bean.app.dto.ProductDto;
import com.yami.shop.bean.enums.CouponProdType;
import com.yami.shop.bean.enums.CouponType;
import com.yami.shop.bean.enums.OfflineHandleEventStatus;
import com.yami.shop.bean.enums.OfflineHandleEventType;
import com.yami.shop.bean.event.BalanceCouponEvent;
import com.yami.shop.bean.event.ScoreOrderEvent;
import com.yami.shop.bean.model.OfflineHandleEvent;
import com.yami.shop.bean.param.OfflineHandleEventAuditParam;
import com.yami.shop.common.config.Constant;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.i18n.I18nMessage;
import com.yami.shop.common.util.PageAdapter;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.coupon.common.constants.CouponStatusEnum;
import com.yami.shop.coupon.common.constants.ValidTimeTypeEnum;
import com.yami.shop.coupon.common.dao.*;
import com.yami.shop.coupon.common.model.*;
import com.yami.shop.coupon.common.service.CouponGroupService;
import com.yami.shop.coupon.common.service.CouponService;
import com.yami.shop.coupon.common.service.CouponUserService;
import com.yami.shop.dao.ProductMapper;
import com.yami.shop.service.OfflineHandleEventService;
import lombok.AllArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lgh on 2018/12/27.
 */
@Service
@AllArgsConstructor
public class CouponGroupServiceImpl extends ServiceImpl<CouponGroupMapper, CouponGroup> implements CouponGroupService {


}
