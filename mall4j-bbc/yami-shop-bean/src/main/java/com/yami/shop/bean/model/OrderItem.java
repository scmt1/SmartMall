/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.bean.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yami.shop.bean.vo.GiveawayVO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author Yami
 */
@Data
@TableName("tz_order_item")
public class OrderItem implements Serializable {

    private static final long serialVersionUID = 7307405761190788407L;

    @ApiModelProperty("订单项ID")
    @TableId(type = IdType.AUTO)
    private Long orderItemId;

    @ApiModelProperty("店铺id")
    private Long shopId;

    @ApiModelProperty("订单编号")
    private String orderNumber;

    @ApiModelProperty("产品ID")
    private Long prodId;

    @ApiModelProperty("产品SkuID")
    private Long skuId;

    @ApiModelProperty("分类id")
    private Long categoryId;

    @ApiModelProperty("购物车产品个数")
    private Integer prodCount;

    @ApiModelProperty("产品名称")
    private String prodName;

    @ApiModelProperty("sku名称")
    private String skuName;

    @ApiModelProperty("产品主图片路径")
    private String pic;

    @ApiModelProperty("产品价格")
    private Double price;

    @ApiModelProperty("用户Id")
    private String userId;

    @ApiModelProperty("商品总金额")
    private Double productTotalAmount;

    @ApiModelProperty("购物时间")
    private Date recTime;

    @ApiModelProperty("评论状态： 0 未评价  1 已评价")
    private Integer commSts;

    @ApiModelProperty("推广员使用的推销卡号")
    private String distributionCardNo;

    @ApiModelProperty("加入购物车的时间")
    private Date basketDate;

    @ApiModelProperty("商品实际金额 = 商品总金额 - 分摊的优惠金额")
    private Double actualTotal;

    @ApiModelProperty("分摊的优惠金额")
    private Double shareReduce;

    @ApiModelProperty("使用积分")
    private Long useScore;

    @ApiModelProperty("状态 -1待发货 0全部发货 其他数量为剩余待发货数量")
    private Integer status;

    @ApiModelProperty("订单确认收货获取的积分")
    private Long gainScore;

    @ApiModelProperty("平台补贴的优惠金额")
    private Double platformShareReduce;

    @ApiModelProperty("分销佣金")
    private Double distributionAmount;

    @ApiModelProperty("上级分销佣金")
    private Double distributionParentAmount;

    @ApiModelProperty("积分抵扣金额")
    private Double scoreAmount;

    @ApiModelProperty("会员折扣金额")
    private Double memberAmount;

    @ApiModelProperty("平台优惠券优惠金额")
    private Double platformCouponAmount;

    @ApiModelProperty("商家优惠券优惠金额")
    private Double shopCouponAmount;

    @ApiModelProperty("满减优惠金额")
    private Double discountAmount;

    @ApiModelProperty("店铺改价优惠金额")
    private Double shopChangeFreeAmount;

    @ApiModelProperty("店铺改价平台优惠减少金额")
    private Double platformShopChangeAmount;

    @ApiModelProperty("店铺套餐优惠金额")
    private Double comboAmount;

    @ApiModelProperty("赠品金额")
    private Double giveawayAmount;

    @ApiModelProperty(value = "分类扣率",required=true)
    private Double rate;

    @ApiModelProperty(value = "平台佣金",required=true)
    private Double platformCommission;

    @ApiModelProperty("运费减免金额")
    private Double freeFreightAmount;

    @ApiModelProperty("平台运费减免金额")
    private Double platformFreeFreightAmount;

    @ApiModelProperty("单个orderItem的配送类型 1:快递 2:自提 3：无需快递")
    private Integer dvyType;

    @ApiModelProperty("是否有赠品")
    private Integer isGiveaway;

    @ApiModelProperty("赠送主订单项id")
    private Long giveawayOrderItemId;

    @ApiModelProperty(value = "主商品关联退款赠品id")
    @TableField(exist = false)
    private String returnGiveawayIds;

    @ApiModelProperty("发货改变的数量")
    @TableField(exist = false)
    private Integer changeNum;

    /**
     * 退款编号（退款编号为null时，说明订单为正常状态）
     */
    @ApiModelProperty("退款编号")
    @TableField(exist = false)
    private String refundSn;

    @ApiModelProperty("退款id")
    @TableField(exist = false)
    private Long refundId;

    @ApiModelProperty("退款单类型（1:整单退款,2:单个物品退款）")
    @TableField(exist = false)
    private Integer refundType;

    @ApiModelProperty("退款状态")
    @TableField(exist = false)
    private Integer returnMoneySts;

    @ApiModelProperty("订单减少金额")
    @TableField(exist = false)
    private Double chageAmount;

    @ApiModelProperty(value = "产品中文名称",required=true)
    @TableField(exist = false)
    private String prodNameCn;

    @ApiModelProperty(value = "产品英文名称",required=true)
    @TableField(exist = false)
    private String prodNameEn;

    @ApiModelProperty(value = "sku中文名称",required=true)
    @TableField(exist = false)
    private String skuNameCn;

    @ApiModelProperty(value = "sku英文名称",required=true)
    @TableField(exist = false)
    private String skuNameEn;

    @ApiModelProperty(value = "分类名称",required=true)
    @TableField(exist = false)
    private String categoryName;

    @ApiModelProperty("支付金额版本号")
    private Integer changeAmountVersion;

    /**
     * 赠品信息
     */
    @TableField(exist = false)
    private GiveawayVO giveaway;

    /**
     * 0:实物商品 1:虚拟商品
     */
    @TableField(exist = false)
    private Integer mold;

    @ApiModelProperty(value = "订单项赠品列表", required = true)
    @TableField(exist = false)
    private List<OrderItem> giveawayList;

    /**
     * 预售时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(exist = false)
    private Date preSaleTime;

    @ApiModelProperty(value = "支付时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(exist = false)
    private Date payTime;
}