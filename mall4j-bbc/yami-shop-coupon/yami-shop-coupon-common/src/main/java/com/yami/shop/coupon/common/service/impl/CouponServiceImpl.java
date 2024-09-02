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
import cn.hutool.core.lang.Snowflake;
import com.alibaba.fastjson.JSONObject;
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
import com.yami.shop.bean.event.CouponReceiveEvent;
import com.yami.shop.bean.event.ScoreOrderEvent;
import com.yami.shop.bean.model.CouponOrder;
import com.yami.shop.bean.model.Dict;
import com.yami.shop.bean.model.DictData;
import com.yami.shop.bean.model.OfflineHandleEvent;
import com.yami.shop.bean.param.OfflineHandleEventAuditParam;
import com.yami.shop.common.config.Constant;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.i18n.I18nMessage;
import com.yami.shop.common.util.PageAdapter;
import com.yami.shop.common.util.PageParam;
import com.yami.shop.coupon.common.constants.CouponStatusEnum;
import com.yami.shop.coupon.common.constants.ValidTimeTypeEnum;
import com.yami.shop.coupon.common.dao.CouponMapper;
import com.yami.shop.coupon.common.dao.CouponProdMapper;
import com.yami.shop.coupon.common.dao.CouponShopMapper;
import com.yami.shop.coupon.common.dao.CouponUserMapper;
import com.yami.shop.coupon.common.model.*;
import com.yami.shop.coupon.common.service.CouponOnlyService;
import com.yami.shop.coupon.common.service.CouponService;
import com.yami.shop.coupon.common.service.CouponUserService;
import com.yami.shop.coupon.common.utils.PolygonUtil;
import com.yami.shop.dao.ProductMapper;
import com.yami.shop.service.DictDataService;
import com.yami.shop.service.DictService;
import com.yami.shop.service.OfflineHandleEventService;
import lombok.AllArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author lgh on 2018/12/27.
 */
@Service
@AllArgsConstructor
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements CouponService {

    private final CouponMapper couponMapper;
    private final CouponProdMapper couponProdMapper;
    private final CouponShopMapper couponShopMapper;
    private final CouponUserMapper couponUserMapper;
    private final CouponUserService couponUserService;
    private final ProductMapper productMapper;
    private final OfflineHandleEventService offlineHandleEventService;
    private final ApplicationContext applicationContext;
    private final DictService dictService;
    private final DictDataService dictDataService;
    private final CouponOnlyService couponOnlyService;
    @Autowired
    private Snowflake snowflake;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = "couponAndCouponProds", key = "#coupon.shopId")
    public void updateCouponAndCouponProds(@Valid Coupon coupon) {
        Integer suitableProdType = coupon.getSuitableProdType();
        Coupon couponDb = couponMapper.selectById(coupon.getCouponId());
        checkInfo(coupon, false);
        boolean isPutOn = Objects.equals(couponDb.getPutonStatus(), CouponStatusEnum.NOT_LAUNCH.getValue()) && Objects.equals(coupon.getPutonStatus(), CouponStatusEnum.PUT_ON.getValue());
        if (isPutOn) {
            coupon.setLaunchTime(new Date());
        }
        couponMapper.updateCoupon(coupon);
        // 删除该优惠券下所有商品
        couponProdMapper.deleteCouponProdsByCouponId(coupon.getCouponId());

        couponShopMapper.deleteCouponShopsByCouponId(coupon.getCouponId());

        if (suitableProdType == 1 || suitableProdType == 2) {
            List<Long> prodIds = coupon.getCouponProds().stream().map(CouponProd::getProdId).collect(Collectors.toList());
            // 插入所有商品
            couponProdMapper.insertCouponProdsBatch(coupon.getCouponId(), prodIds);
        }

        if (suitableProdType == 3) {
            List<Long> shopIds = coupon.getCouponShops().stream().map(CouponShop::getShopId).collect(Collectors.toList());
            // 插入所有店铺
            couponShopMapper.insertCouponShopsBatch(coupon.getCouponId(), shopIds);
        }

        // 不是平台端的优惠券、修改后的领取类型为平台发放、没有发生领取类型变更的，直接返回
        if (!Objects.equals(coupon.getShopId(), Constant.PLATFORM_SHOP_ID)
                || Objects.equals(coupon.getGetWay(), 1)
                || Objects.equals(coupon.getGetWay(), couponDb.getGetWay())) {
            return;
        }
        // 处理优惠券类型从系统发放变更为用户领取时，移除用户等级绑定的优惠券（目前用户等级绑定的优惠券只支持系统发放）
        applicationContext.publishEvent(new BalanceCouponEvent(coupon.getCouponId()));
    }

    @Override
    @CacheEvict(cacheNames = "couponAndCouponProds", key = "#shopId")
    public void removeCouponAndCouponProdsCache(Long shopId) {
    }

    @Override
    public IPage<CouponDto> getCouponList(Page<CouponDto> page, String userId) {
        //获取优惠券列表
        List<Long> couponIds = couponMapper.getShopAvailableCouponIds(new PageAdapter(page));
        page.setTotal(couponMapper.countCouponPageByCouponDto());
        if (CollUtil.isEmpty(couponIds)) {
            page.setRecords(new ArrayList<>());
            return page;
        }
        List<CouponDto> couponDtoList = couponMapper.getCouponList(couponIds, I18nMessage.getDbLang());
        List<CouponUser> couponUsers = couponUserMapper.listByCouponIdsAndUserId(couponIds, userId);
        Map<Long, Integer> couponUserMap = couponUsers.stream().collect(Collectors.toMap(CouponUser::getCouponId, CouponUser::getCurUserReceiveCount));
        for (CouponDto couponDto : couponDtoList) {
            if (couponUserMap.containsKey(couponDto.getCouponId())) {
                couponDto.setCurUserReceiveCount(couponUserMap.get(couponDto.getCouponId()));
                continue;
            }
            couponDto.setCurUserReceiveCount(0);
        }
        page.setRecords(couponDtoList);
        return page;
    }

    @Override
    public IPage<CouponDto> getCouponList(Page<CouponDto> page) {
        //获取优惠券列表
        page.setTotal(couponMapper.countCouponPageByCouponDto());
        List<Long> couponIds = couponMapper.getShopAvailableCouponIds(new PageAdapter(page));
        if (CollUtil.isEmpty(couponIds)) {
            page.setRecords(new ArrayList<>());
            return page;
        }
        List<CouponDto> couponDtoList = couponMapper.getCouponList(couponIds, I18nMessage.getDbLang());
        page.setRecords(couponDtoList);
        return page;
    }

    @Override
    public IPage<CouponUser> getPageByUserId(PageParam<CouponUser> page, String userId, Integer status) {
        return couponUserMapper.getPageByUserId(page, userId, status);
    }

    @Override
    public CouponDto getCouponUserInfo(Long couponUserId) {
        return couponMapper.getCouponUserInfo(couponUserId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = "couponAndCouponProds", key = "#shopId")
    public void batchBindCouponByIds(List<Long> couponIds, String userId, Long shopId) {
        List<CouponUser> couponUsers = new ArrayList<>();
        Collection<Coupon> coupons = listByIds(couponIds);
        if (CollectionUtils.isEmpty(coupons)) {
            return;
        }
        for (Coupon coupon : coupons) {
            Date nowTime = new Date();
            // 当优惠券状态不为投放时
            if (coupon.getOverdueStatus() == 0 || coupon.getPutonStatus() != 1 || coupon.getStocks() == 0) {
                // 该券无法被领取或者该券领完了!
                log.warn("yami.coupon.receive.finish");
                continue;
            }
            int count = couponUserMapper.selectCount(new LambdaQueryWrapper<CouponUser>().eq(CouponUser::getUserId, userId).eq(CouponUser::getCouponId, coupon.getCouponId()));
            if (count >= coupon.getLimitNum()) {
                // 该券已达个人领取上限，无法继续领取！
                log.warn("yami.coupon.user.limit");
                continue;
            }
            CouponUser couponUser = new CouponUser();
            couponUser.setUserId(userId);
            couponUser.setStatus(1);
            couponUser.setCouponId(coupon.getCouponId());
            couponUser.setReceiveTime(nowTime);
            // 生效时间类型为固定时间
            if (coupon.getValidTimeType() == 1) {
                couponUser.setUserStartTime(coupon.getStartTime());
                couponUser.setUserEndTime(coupon.getEndTime());
            }
            // 生效时间类型为领取后生效
            if (coupon.getValidTimeType() == 2) {
                if (coupon.getAfterReceiveDays() == null) {
                    coupon.setAfterReceiveDays(0);
                }
                if (coupon.getValidDays() == null) {
                    coupon.setValidDays(0);
                }
                couponUser.setUserStartTime(DateUtils.addDays(DateUtil.beginOfDay(nowTime), coupon.getAfterReceiveDays()));
                couponUser.setUserEndTime(DateUtils.addDays(couponUser.getUserStartTime(), coupon.getValidDays() + 1));
            }
            couponUser.setIsDelete(0);
            couponUsers.add(couponUser);
            // 更新库存
            if (couponMapper.updateCouponStocksAndVersion(coupon.getCouponId()) < 1) {
                // 优惠券库存不足
                throw new YamiShopBindException("yami.coupon.stock.enough");
            }
        }
        couponUserService.saveBatch(couponUsers);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = "couponAndCouponProds", key = "#shopId")
    public void bindCouponById(List<Long> couponIds, String userId, Long shopId) {
        List<CouponUser> couponUsers = new ArrayList<>();
        Collection<Coupon> coupons = listByIds(couponIds);
        if (CollectionUtils.isEmpty(coupons)) {
            return;
        }
        for (Coupon coupon : coupons) {
            Date nowTime = new Date();
            CouponUser couponUser = new CouponUser();
            couponUser.setUserId(userId);
            couponUser.setStatus(1);
            couponUser.setCouponId(coupon.getCouponId());
            couponUser.setReceiveTime(nowTime);
            // 生效时间类型为固定时间
            if (coupon.getValidTimeType() == 1) {
                couponUser.setUserStartTime(coupon.getStartTime());
                couponUser.setUserEndTime(coupon.getEndTime());
            }
            // 生效时间类型为领取后生效
            if (coupon.getValidTimeType() == 2) {
                if (coupon.getAfterReceiveDays() == null) {
                    coupon.setAfterReceiveDays(0);
                }
                if (coupon.getValidDays() == null) {
                    coupon.setValidDays(0);
                }
                couponUser.setUserStartTime(DateUtils.addDays(DateUtil.beginOfDay(nowTime), coupon.getAfterReceiveDays()));
                couponUser.setUserEndTime(DateUtils.addDays(couponUser.getUserStartTime(), coupon.getValidDays() + 1));
            }
            couponUser.setIsDelete(0);
            couponUsers.add(couponUser);
        }
        couponUserService.saveBatch(couponUsers);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = "couponAndCouponProds", key = "#coupon.shopId")
    public void insertCouponAndCouponProds(@Valid Coupon coupon) {
        Integer suitableProdType = coupon.getSuitableProdType();
        checkInfo(coupon, true);
        if (Objects.equals(coupon.getPutonStatus(), CouponStatusEnum.PUT_ON.getValue())) {
            coupon.setLaunchTime(new Date());
        }
        couponMapper.insert(coupon);
        if (suitableProdType == 2 || suitableProdType == 1) {
            List<Long> prodIds = coupon.getCouponProds().stream().map(CouponProd::getProdId).collect(Collectors.toList());
            // 插入所有商品
            couponProdMapper.insertCouponProdsBatch(coupon.getCouponId(), prodIds);
        } else if (suitableProdType == 3) {
            List<Long> shopIds = coupon.getCouponShops().stream().map(CouponShop::getShopId).collect(Collectors.toList());
            // 插入所有商品
            couponShopMapper.insertCouponShopsBatch(coupon.getCouponId(), shopIds);
        }
    }

    @Override
    public Coupon getCouponAndCouponProdsByCouponId(Long id) {
        return couponMapper.getCouponAndCouponProdsByCouponId(id);
    }


    private boolean checkAddress(Double lat, Double lng) {
        //先判断位置是否在江安县范围内
        JSONObject jsonObject = PolygonUtil.queryAreaName(lat, lng);
        if (jsonObject.getInteger("status") == 0) {
            JSONObject addressObject = jsonObject.getJSONObject("result").getJSONObject("address_component");
            String city = addressObject.getString("city");
            String district = addressObject.getString("district");

            Dict dict = dictService.findByType("coupon_address");
            List<DictData> dictDataList = dictDataService.findByDictId(dict.getId());

            boolean flag = false;
            for (DictData dictData : dictDataList) {
                String[] split = dictData.getValue().split(",");
                for (String s : split) {
                    String[] strings = s.split("-");
                    if (Objects.equals(city, strings[0]) && Objects.equals(district, strings[1])) {
                        flag = true;
                        break;
                    }
                }
                if (flag == true) {
                    break;
                }
            }
            return flag;
        }
        return false;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = "couponAndCouponProds", key = "#coupon.shopId")
//    @RedisLock(lockName = "userReceiveCoupon", key = "#coupon.couponId + ':' + #userId")
    public void receive(Coupon coupon, String userId, Double lat, Double lng) {

        String insertKey = "insert:couponId" + coupon.getCouponId() + ":" + userId;
        String saveCountKey = "saveCount:couponId" + coupon.getCouponId();

        Integer total = coupon.getSourceStock();

        //先判断位置是否在江安县范围内
        boolean flag = checkAddress(lat, lng);
        if (flag) {
            List<CouponOnly> groupList = couponOnlyService.queryGroupListByCouponId(coupon.getCouponId());
            //如果groupList==0 说明领取的优惠券不在分组里面，不管 ，否则需要判断已领取的优惠券里面是否包含了分组里面的
            if (groupList != null && groupList.size() > 0) {
                //查询当前用户已领取的优惠券
                List<CouponUser> list = couponUserService.list(new LambdaQueryWrapper<CouponUser>()
                        .eq(CouponUser::getUserId, userId)
                        .eq(CouponUser::getIsDelete, 0)
                        .select(CouponUser::getCouponId)
                );
                List<Long> userCouponIdList = list.stream().map(item -> item.getCouponId()).collect(Collectors.toList());
                //判断已领取的优惠券里面是否有存在于分组里面的优惠券，有则提示不能重复领取
                boolean isContains = false;
                for (CouponOnly couponOnly : groupList) {
                    for (Long aLong : userCouponIdList) {
                        if (Objects.equals(aLong, couponOnly.getCouponId())) {
                            isContains = true;
                            break;
                        }
                    }
                    if (isContains)
                        break;
                }

                if (isContains) {
                    throw new YamiShopBindException("您已经领取过该类型的优惠券，无法继续领取！");
                }
            }
        } else {
            throw new YamiShopBindException("您当前没有在可领取的范围内，不能领取优惠券");
        }
        //如果是积分商品，就扣除用户相应的积分
        if (Objects.equals(coupon.getCouponType(), CouponType.C2P.value())) {
            applicationContext.publishEvent(new ScoreOrderEvent(coupon.getCouponId(), null, coupon.getScorePrice()));
        }

        //1.查询redis 是否存在  避免一个人多次提交
        String s = stringRedisTemplate.opsForValue().get(insertKey);
        if (StringUtils.isNotBlank(s)) {
            throw new YamiShopBindException("yami.coupon.user.limit");
        }
        //2.存入redis缓存
        Boolean aBoolean = setRedisIfAbsent(insertKey, userId);
        if (!aBoolean) {
            throw new YamiShopBindException("当前领取人数过多，请稍后再试！");
        }
        Long increment = stringRedisTemplate.opsForValue().increment(saveCountKey, 1);
        if (increment > total) {
            deleteRedisKey(insertKey);
            stringRedisTemplate.opsForValue().decrement(saveCountKey, 1);
            throw new YamiShopBindException("yami.coupon.stock.enough");
        }

        CouponUser couponUser = getCouponUser(coupon, userId);
        couponUserMapper.insert(couponUser);
        // 更新库存
        if (couponMapper.updateCouponStocksAndVersion(coupon.getCouponId()) < 1) {
            // 优惠券库存不足
            throw new YamiShopBindException("yami.coupon.stock.enough");
        }

        //领取成功 推送服务通知
        applicationContext.publishEvent(new CouponReceiveEvent(coupon.getCouponName()
                , "消费金额满" + coupon.getCashCondition() + "减" + coupon.getReduceAmount(), coupon.getEndTime(), "您有新的优惠券到账！", userId));
    }

    private CouponUser getCouponUser(Coupon coupon, String userId) {
        Date nowTime = new Date();
        // 当优惠券状态不为投放时
        if (coupon.getOverdueStatus() == 0 || coupon.getPutonStatus() != 1) {
            // 该券无法被领取
            throw new YamiShopBindException("yami.coupon.receive.finish");
        }
        // 当优惠券不在规定时间内(类型为固定时间) 优惠券使用时间，不是领取时间，一般优惠券都是可以领，还不能用的
//        if (coupon.getValidTimeType() == 1 && (coupon.getEndTime().getTime() < nowTime.getTime() || coupon.getStartTime().getTime() > nowTime.getTime())) {
//            throw new YamiShopBindException("不在可领取的时间范围内");
//        }
        // 当优惠券无库存时
        if (coupon.getStocks() == 0) {
            // 该券领完了
            throw new YamiShopBindException("yami.coupon.receive.finish");
        }
        int count = couponUserMapper.selectCount(new LambdaQueryWrapper<CouponUser>().eq(CouponUser::getUserId, userId).eq(CouponUser::getCouponId, coupon.getCouponId()));
        if (count >= coupon.getLimitNum()) {
            // 该券已达个人领取上限，无法继续领取
            throw new YamiShopBindException("yami.coupon.user.limit");
        }
        CouponUser couponUser = new CouponUser();
        couponUser.setUserId(userId);
        couponUser.setCouponId(coupon.getCouponId());
        couponUser.setStatus(1);
        couponUser.setReceiveTime(nowTime);
        // 生效时间类型为固定时间
        if (coupon.getValidTimeType() == 1) {
            couponUser.setUserStartTime(coupon.getStartTime());
            couponUser.setUserEndTime(coupon.getEndTime());
        }
        // 生效时间类型为领取后生效
        if (coupon.getValidTimeType() == ValidTimeTypeEnum.RECEIVE.getValue()) {
            if (coupon.getAfterReceiveDays() == null) {
                coupon.setAfterReceiveDays(0);
            }
            if (coupon.getValidDays() == null) {
                coupon.setValidDays(0);
            }
            couponUser.setUserStartTime(DateUtils.addDays(DateUtil.beginOfDay(nowTime), coupon.getAfterReceiveDays()));
            if(coupon.getTimeType() == 1){
                couponUser.setUserEndTime(DateUtils.addDays(couponUser.getUserStartTime(), coupon.getValidDays() + 1));
            }else if(coupon.getTimeType() == 2){
                couponUser.setUserStartTime(nowTime);
                couponUser.setUserEndTime(DateUtils.addHours(couponUser.getUserStartTime(), coupon.getValidDays()));
            }else{
                couponUser.setUserStartTime(nowTime);
                couponUser.setUserEndTime(DateUtils.addMinutes(couponUser.getUserStartTime(), coupon.getValidDays()));
            }
        }
        couponUser.setIsDelete(0);
        return couponUser;
    }

    /**
     * 单线程设置值
     *
     * @param key
     * @param value
     * @return
     */
    private Boolean setRedisIfAbsent(String key, String value) {
        Boolean aBoolean = stringRedisTemplate.opsForValue().setIfAbsent(key, value, 30, TimeUnit.DAYS);//设置10天过期
        return aBoolean;
    }

    private void deleteRedisKey(String key) {
        stringRedisTemplate.delete(key);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeCoupon(Date now) {
        // 设置店铺的过期优惠券为过期状态
        couponMapper.updateOverdueStatusByTime(now);
        // 设置店铺库存为0优惠券的状态(设为未投放状态) 没库存就消失么？
        // 像京东会有没库存的优惠券，一直展示大半天，也不消失，所以消失是用户手动下线的
//        couponMapper.updatePutonStatusByStocks();
    }

    @Override
    public void putonCoupon(Date now) {
        couponMapper.putonCoupon(now);
    }


    @Override
    public void cancelPutCoupon(Date now) {
        couponMapper.cancelPutCoupon(now);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByCouponId(Long couponId) {
        // 获取未被删除的优惠券数量
        Integer useCount = couponUserMapper.selectCount(new LambdaQueryWrapper<CouponUser>().eq(CouponUser::getCouponId, couponId).eq(CouponUser::getIsDelete, 0));
        if (useCount > 0) {
            // 该优惠券已被领取，删除失败
            throw new YamiShopBindException("yami.coupon.delete.check1");
        }

        Coupon coupon = couponMapper.selectById(couponId);
        if (Objects.isNull(coupon)) {
            // 优惠券不存在
            throw new YamiShopBindException("yami.coupon.no.exist");
        }
        if (Objects.equals(coupon.getPutonStatus(), 1)) {
            //  该优惠券已投放，删除失败
            throw new YamiShopBindException("yami.coupon.delete.check2");
        }
        //删除优惠券关联的商品
        couponProdMapper.delete(new LambdaQueryWrapper<CouponProd>().eq(CouponProd::getCouponId, couponId));


        couponShopMapper.delete(new LambdaQueryWrapper<CouponShop>().eq(CouponShop::getCouponId, couponId));

        //删除优惠券
        couponMapper.deleteById(couponId);
    }

    @Override
    public List<CouponDto> generalCouponList(String userId,Coupon coupon) {
        return couponMapper.generalCouponList(userId,coupon);
    }

    @Override
    public List<CouponDto> generalCouponList(Coupon coupon) {
        return couponMapper.generalCouponListTourist(coupon);
    }

    @Override
    public IPage<CouponDto> getCouponListByStatus(IPage<CouponDto> page, String userId, Integer status) {
        IPage<CouponDto> couponPage = couponMapper.getCouponListByStatus(page, userId, status);
        List<CouponDto> couponDtoList = couponPage.getRecords();
        for (CouponDto couponDto : couponDtoList) {
            if (couponDto.getSuitableProdType() == 3) {
                QueryWrapper<CouponShop> queryWrapper = new QueryWrapper<>();
                queryWrapper.lambda().eq(CouponShop::getCouponId, couponDto.getCouponId());
                List<CouponShop> couponShops = couponShopMapper.selectList(queryWrapper);
                List<Long> shopIds = new ArrayList<>();
                for (CouponShop couponShop : couponShops) {
                    shopIds.add(couponShop.getShopId());
                }
                couponDto.setUseShopIds(shopIds);
            }
            if (Objects.equals(couponDto.getValidTimeType(), 2)) {
                Integer afterReceiveDays = couponDto.getAfterReceiveDays();
                if (Objects.isNull(couponDto.getAfterReceiveDays())) {
                    afterReceiveDays = 0;
                }
                Integer validDays = couponDto.getValidDays();
                if (validDays == null) {
                    validDays = 0;
                }
                if(couponDto.getTimeType() == 1){
                    couponDto.setStartTime(DateUtil.offsetDay(couponDto.getReceiveTime(), afterReceiveDays));
                    couponDto.setEndTime(DateUtil.offsetDay(couponDto.getStartTime(), validDays));
                }else{
                    couponDto.setStartTime(couponDto.getUserStartTime());
                    couponDto.setEndTime(couponDto.getUserEndTime());
                }
            }
        }
        return couponPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = "CouponAndCouponUser", key = "#userId")
    public void deleteUserCouponByCouponId(String userId, Long couponId) {
        couponUserMapper.deleteUserCoupon(userId, couponId);
    }

    @Override
    public List<CouponOrderDto> getCouponListByShopIds(String userId, Long shopId) {
        List<CouponOrderDto> couponOrderDtoList = couponMapper.getCouponListByShopIds(userId, shopId);
        for (CouponOrderDto couponOrderDto : couponOrderDtoList) {
            if (Objects.equals(couponOrderDto.getValidTimeType(), 2)) {
                Integer validDays = couponOrderDto.getValidDays();
                if (validDays == null) {
                    validDays = 0;
                }
                Integer afterReceiveDays = couponOrderDto.getAfterReceiveDays();
                if (afterReceiveDays == null) {
                    afterReceiveDays = 0;
                }
                couponOrderDto.setStartTime(DateUtil.offsetDay(couponOrderDto.getReceiveTime(), afterReceiveDays));
                couponOrderDto.setEndTime(DateUtil.offsetDay(couponOrderDto.getStartTime(), validDays));
            }
        }
        return couponOrderDtoList;
    }

    @Override
    @Cacheable(cacheNames = "couponAndCouponProds", key = "#shopId")
    public List<Coupon> listPutonByShopId(Long shopId) {
        return couponMapper.listPutonCouponAndCouponProdsByShopId(shopId);
    }

    @Override
    public List<CouponDto> listCouponIdsByUserId(String userId) {
        return couponMapper.listCouponIdsByUserId(userId);
    }

    @Override
    public IPage<ProductDto> prodListByCoupon(PageParam<ProductDto> page, Coupon coupon, Integer dbLang) {
        // 优惠券所有商品可用、指定商品可用、指定商品不可用 (0全部商品参与 1指定商品参与 2指定商品不参与)
        IPage<ProductDto> productPage;
        if (coupon.getSuitableProdType() == 0) {
            productPage = productMapper.listByShopId(page, coupon.getShopId(), dbLang);
        } else {
            productPage = productMapper.listBySuitableProdTypeAndCouponIdAndShopId(page, coupon.getShopId(), coupon.getSuitableProdType(),
                    coupon.getCouponId(), dbLang);
        }
        return productPage;
    }

    @Override
    public IPage<Coupon> getMultiShopPage(PageParam<Coupon> page, Coupon coupon) {
        IPage<Coupon> multiShopPage = couponMapper.getMultiShopPage(page, coupon);
        for (Coupon record : multiShopPage.getRecords()) {
            record.setUseNum(couponMapper.countUseNum(record.getCouponId()) == null ? 0 : couponMapper.countUseNum(record.getCouponId()));
            record.setTakeNum(couponMapper.countTakeNum(record.getCouponId(), record.getShopId()) == null ? 0 : couponMapper.countTakeNum(record.getCouponId(), record.getShopId()));
        }
        return multiShopPage;
    }

    @Override
    public IPage<Coupon> getShopCouponsPage(PageParam<Coupon> page, Coupon coupon) {
        return couponMapper.getShopCouponsPage(page, coupon);
    }

    @Override
    public List<Coupon> shopWriteOffDetail(Coupon coupon) {
        List<Coupon> multiShopWriteOff = couponMapper.shopWriteOffDetail(coupon);
        for (Coupon record : multiShopWriteOff) {
            record.setUseNum(couponMapper.countUseNum(record.getCouponId()) == null ? 0 : couponMapper.countUseNum(record.getCouponId()));
            record.setTakeNum(couponMapper.countTakeNum(record.getCouponId(), record.getShopId()) == null ? 0 : couponMapper.countTakeNum(record.getCouponId(), record.getShopId()));
        }
        return multiShopWriteOff;
    }

    @Override
    public Map<String, Long> getCouponCountByStatus(String userId) {
        return couponMapper.getCouponCountByStatus(userId);
    }

    @Override
    public IPage<Coupon> getPlatformPage(PageParam<Coupon> page, Coupon coupon) {
        IPage<Coupon> couponPage = couponMapper.getPlatformPage(page, coupon);
        List<Coupon> couponList = couponPage.getRecords();
        for (Coupon temp : couponList) {
            temp.setTakeNum(couponMapper.countTakeNum(temp.getCouponId(), temp.getShopId()) == null ? 0 : couponMapper.countTakeNum(temp.getCouponId(), temp.getShopId()));
            temp.setUseNum(couponMapper.countUseNum(temp.getCouponId()) == null ? 0 : couponMapper.countUseNum(temp.getCouponId()));
        }
        return couponPage;
    }

    @Override
    public IPage<Coupon> getCouponsPage(PageParam<Coupon> page, Coupon coupon) {
        IPage<Coupon> couponPage = couponMapper.getCouponsPage(page, coupon);
        return couponPage;
    }

    @Override
    public Coupon statisticCoupon(Coupon coupon) {
        Coupon statisticCoupon = couponMapper.statisticCoupon(coupon);
        List<Coupon> coupons = couponMapper.getCoupons(coupon);
        if (coupons.size() > 0) {
            List<Long> couponIds = coupons.stream().map(Coupon::getCouponId).collect(Collectors.toList());
            statisticCoupon.setTakeNumTotal(couponMapper.countTakeNumTotal(couponIds, coupon.getShopId()) == null ? 0 : couponMapper.countTakeNumTotal(couponIds, coupon.getShopId()));
            statisticCoupon.setUseNumTotal(couponMapper.countUseNumTotal(couponIds) == null ? 0 : couponMapper.countUseNumTotal(couponIds));
        }
        return statisticCoupon;
    }

    @Override
    public Coupon receiveAndUseStatistic(Coupon coupon) {
        Coupon receiveStatisticCoupon = couponMapper.receiveAndUseStatistic(coupon);
        return receiveStatisticCoupon;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = "couponAndCouponProds", key = "#coupon.shopId")
    public void offline(Coupon coupon, String offlineReason, Long sysUserId) {
        // 添加下线处理记录
        OfflineHandleEvent handleEvent = offlineHandleEventService.getBaseMapper().selectOne(new LambdaUpdateWrapper<OfflineHandleEvent>()
                .eq(OfflineHandleEvent::getHandleId, coupon.getCouponId())
                .eq(OfflineHandleEvent::getHandleType, OfflineHandleEventType.COUPON.getValue())
                .ne(OfflineHandleEvent::getStatus, OfflineHandleEventStatus.AGREE_BY_PLATFORM.getValue())
        );
        if (Objects.isNull(handleEvent)) {
            handleEvent = new OfflineHandleEvent();
        }
        Date now = new Date();
        handleEvent.setHandleId(coupon.getCouponId());
        handleEvent.setHandleType(OfflineHandleEventType.COUPON.getValue());
        handleEvent.setCreateTime(now);
        handleEvent.setOfflineReason(offlineReason);
        handleEvent.setHandlerId(sysUserId);
        handleEvent.setShopId(coupon.getShopId());
        handleEvent.setStatus(OfflineHandleEventStatus.OFFLINE_BY_PLATFORM.getValue());
        handleEvent.setUpdateTime(now);
        if (Objects.nonNull(handleEvent.getEventId())) {
            offlineHandleEventService.updateById(handleEvent);
        } else {
            offlineHandleEventService.save(handleEvent);
        }

        // 更新活动状态为下线
        couponMapper.updatePutOnStatusByCouponId(coupon.getCouponId(), CouponStatusEnum.OFFLINE.getValue());
        //更新优惠券过期状态为过期
        couponMapper.updateOverdueStatusByCouponId(coupon.getCouponId(), 0);


    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditCoupon(OfflineHandleEventAuditParam offlineHandleEventAuditParam, Long sysUserId) {
        // 审核通过
        if (Objects.equals(offlineHandleEventAuditParam.getStatus(), OfflineHandleEventStatus.AGREE_BY_PLATFORM.getValue())) {
            // 更新优惠券为暂不投放状态
            couponMapper.updatePutOnStatusByCouponId(offlineHandleEventAuditParam.getHandleId(), CouponStatusEnum.NOT_LAUNCH.getValue());
            //更新优惠券过期状态为未过期
            couponMapper.updateOverdueStatusByCouponId(offlineHandleEventAuditParam.getHandleId(), 1);
        }
        // 审核不通过
        else if (Objects.equals(offlineHandleEventAuditParam.getStatus(), OfflineHandleEventStatus.DISAGREE_BY_PLATFORM.getValue())) {
            couponMapper.updatePutOnStatusByCouponId(offlineHandleEventAuditParam.getHandleId(), CouponStatusEnum.OFFLINE.getValue());
        }
        offlineHandleEventService.auditOfflineEvent(offlineHandleEventAuditParam, sysUserId);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditApply(Long eventId, Long couponId, String reapplyReason) {
        // 更新活动为待审核状态
        couponMapper.updatePutOnStatusByCouponId(couponId, CouponStatusEnum.WAIT_AUDIT.getValue());
        // 更新事件状态
        offlineHandleEventService.updateToApply(eventId, reapplyReason);
    }

    private void checkInfo(Coupon coupon, Boolean isSave) {
        boolean checkProd = (Objects.equals(CouponProdType.PROD_IN.value(), coupon.getSuitableProdType())
                || Objects.equals(CouponProdType.PROD_NO_IN.value(), coupon.getSuitableProdType()))
                && CollectionUtils.isEmpty(coupon.getCouponProds());
        if (checkProd) {
            // 商品不能为空
            throw new YamiShopBindException("yami.product.no.exist");
        }


        boolean checkShop = (Objects.equals(CouponProdType.SHOP_IN.value(), coupon.getSuitableProdType()) && CollectionUtils.isEmpty(coupon.getCouponShops()));
        if (checkShop) {
            // 商品不能为空
            throw new YamiShopBindException("店铺不能为空");
        }


        if (Objects.equals(coupon.getPutonStatus(), CouponStatusEnum.AUTO_LAUNCH.getValue()) && Objects.isNull(coupon.getLaunchTime())) {
            // 优惠券投放时间不能为空
            throw new YamiShopBindException("yami.coupon.launch.time.check");
        }
        Date nowTime = new Date();
        if (Objects.nonNull(coupon.getLaunchTime()) && coupon.getLaunchTime().getTime() < nowTime.getTime()) {
            // 投放时间需大于当前时间
            throw new YamiShopBindException("yami.coupon.launch.time.check1");
        }
        if (isSave) {
            if (coupon.getLimitNum() <= 0) {
                // 优惠券限领数量需大于0
                throw new YamiShopBindException("yami.coupon.limit.check");
            }
            if (coupon.getValidTimeType() == 1 && coupon.getStartTime().getTime() > coupon.getEndTime().getTime()) {
                // 开始时间需大于结束时间
                throw new YamiShopBindException("yami.coupon.time.check");
            }
        }
        // 结束时间小于等于当前时间
        if (Objects.equals(coupon.getValidTimeType(), ValidTimeTypeEnum.FIXED.value())) {
            if (coupon.getEndTime().getTime() < nowTime.getTime()) {
                coupon.setOverdueStatus(0);
                coupon.setPutonStatus(CouponStatusEnum.CANCEL.getValue());
            } else {
                coupon.setOverdueStatus(1);
            }
        }
    }


    @Override
    public List<CouponDto> queryCouponUserDetail(Long couponId, String userId, Long couponUserId) {
        List<Long> couponIds = Arrays.asList(couponId);
        Coupon coupon = couponMapper.selectById(couponId);
        CouponUser couponUser = null;
        if(couponUserId != null){
            couponUser = couponUserService.getOne(new LambdaQueryWrapper<CouponUser>()
                    .eq(CouponUser::getUserId, userId)
                    .eq(CouponUser::getCouponId, couponId)
                    .eq(CouponUser::getCouponUserId,couponUserId));
        }else{
            couponUser = couponUserService.getOne(new LambdaQueryWrapper<CouponUser>()
                    .eq(CouponUser::getUserId, userId)
                    .eq(CouponUser::getCouponId, couponId));
        }

        List<CouponDto> couponDtoList = couponMapper.getCouponList(couponIds, I18nMessage.getDbLang());
        List<CouponUser> couponUsers = couponUserMapper.listByCouponIdsAndUserId(couponIds, userId);

        Map<Long, Integer> couponUserMap = couponUsers.stream().collect(Collectors.toMap(CouponUser::getCouponId, CouponUser::getCurUserReceiveCount));
        Map<Long, Integer> couponUserCountMap = couponUsers.stream().collect(Collectors.toMap(CouponUser::getCouponId, CouponUser::getUserHasCount));

        for (CouponDto couponDto : couponDtoList) {
            if (couponUserMap.containsKey(couponDto.getCouponId())) {
                couponDto.setCurUserReceiveCount(couponUserMap.get(couponDto.getCouponId()));
                couponDto.setUserHasCount(couponUserCountMap.get(couponDto.getCouponId()));
                if (couponUser != null) {
                    couponDto.setCouponUserId(couponUser.getCouponUserId());
                }
                continue;
            }
            couponDto.setCurUserReceiveCount(0);
            couponDto.setUserHasCount(0);
        }

        CouponDto couponDto = couponMapper.queryCouponUserDetail(userId, couponId, coupon.getShopId());
        if (couponDto != null && couponUser != null) {
            couponDto.setCouponUserId(couponUser.getCouponUserId());
            couponDto.setStatus(couponUser.getStatus());
            couponDtoList.add(couponDto);
        }
        return couponDtoList;
    }

    @Override
    public List<CouponDto> getShopAllCouponList(Coupon coupon) {
        return couponMapper.getShopAllCouponList(coupon);
    }

    @Override
    public int updateCouponStocksAndVersion(Long couponId) {
        return couponMapper.updateCouponStocksAndVersion(couponId);
    }

    @Override
    public List<CouponOrder> queryNoUseCouponList() {
        return couponMapper.queryNoUseCouponList();
    }
}

