/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.combo.multishop.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yami.shop.combo.multishop.model.ComboProd;
import com.yami.shop.combo.multishop.service.ComboProdService;
import com.yami.shop.common.annotation.SysLog;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.common.util.PageParam;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


/**
 * 套餐商品项
 *
 * @author LGH
 * @date 2021-11-02 10:35:08
 */
@RestController
@AllArgsConstructor
@RequestMapping("/seckill/comboProd" )
public class ComboProdController {

    private final ComboProdService comboProdService;

    /**
     * 分页查询
     * @param page 分页对象
     * @param comboProd 套餐商品项
     * @return 分页数据
     */
    @GetMapping("/page" )
    public ServerResponseEntity<IPage<ComboProd>> getComboProdPage(PageParam<ComboProd> page, ComboProd comboProd) {
        return ServerResponseEntity.success(comboProdService.page(page, new LambdaQueryWrapper<ComboProd>()));
    }


    /**
     * 通过id查询套餐商品项
     * @param comboProdId id
     * @return 单个数据
     */
    @GetMapping("/info/{comboProdId}" )
    public ServerResponseEntity<ComboProd> getById(@PathVariable("comboProdId") Long comboProdId) {
        return ServerResponseEntity.success(comboProdService.getById(comboProdId));
    }

    /**
     * 新增套餐商品项
     * @param comboProd 套餐商品项
     * @return 是否新增成功
     */
    @SysLog("新增套餐商品项" )
    @PostMapping
    @PreAuthorize("@pms.hasPermission('seckill:comboProd:save')" )
    public ServerResponseEntity<Boolean> save(@RequestBody @Valid ComboProd comboProd) {
        return ServerResponseEntity.success(comboProdService.save(comboProd));
    }

    /**
     * 修改套餐商品项
     * @param comboProd 套餐商品项
     * @return 是否修改成功
     */
    @SysLog("修改套餐商品项" )
    @PutMapping
    @PreAuthorize("@pms.hasPermission('seckill:comboProd:update')" )
    public ServerResponseEntity<Boolean> updateById(@RequestBody @Valid ComboProd comboProd) {
        return ServerResponseEntity.success(comboProdService.updateById(comboProd));
    }

    /**
     * 通过id删除套餐商品项
     * @param comboProdId id
     * @return 是否删除成功
     */
    @SysLog("删除套餐商品项" )
    @DeleteMapping("/{comboProdId}" )
    @PreAuthorize("@pms.hasPermission('seckill:comboProd:delete')" )
    public ServerResponseEntity<Boolean> removeById(@PathVariable Long comboProdId) {
        return ServerResponseEntity.success(comboProdService.removeById(comboProdId));
    }

}
