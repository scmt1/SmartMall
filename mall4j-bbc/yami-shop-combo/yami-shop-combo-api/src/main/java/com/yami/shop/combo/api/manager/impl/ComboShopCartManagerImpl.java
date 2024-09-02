package com.yami.shop.combo.api.manager.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.BooleanUtil;
import com.yami.shop.bean.app.dto.*;
import com.yami.shop.bean.app.param.ChangeShopCartParam;
import com.yami.shop.bean.enums.DiscountRule;
import com.yami.shop.bean.enums.OrderActivityType;
import com.yami.shop.bean.model.Basket;
import com.yami.shop.bean.vo.GiveawayProdVO;
import com.yami.shop.bean.vo.GiveawayVO;
import com.yami.shop.bean.vo.ShopCartWithAmountVO;
import com.yami.shop.bean.vo.ShopTransFeeVO;
import com.yami.shop.combo.multishop.dto.ComboSkuDto;
import com.yami.shop.combo.multishop.model.Combo;
import com.yami.shop.combo.multishop.model.ComboProd;
import com.yami.shop.combo.multishop.model.ComboProdSku;
import com.yami.shop.combo.multishop.service.ComboService;
import com.yami.shop.combo.multishop.service.GiveawayService;
import com.yami.shop.common.config.Constant;
import com.yami.shop.common.exception.YamiShopBindException;
import com.yami.shop.common.i18n.I18nMessage;
import com.yami.shop.common.response.ServerResponseEntity;
import com.yami.shop.common.util.Arith;
import com.yami.shop.manager.ComboShopCartManager;
import com.yami.shop.security.api.util.SecurityUtils;
import com.yami.shop.service.BasketService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 组装套餐 + 赠品的购物车操作
 * @author 菠萝凤梨
 */
@Component
@AllArgsConstructor
public class ComboShopCartManagerImpl implements ComboShopCartManager {

    private final GiveawayService giveawayService;

    private final ComboService comboService;

    private final BasketService basketService;

    @Override
    public void calculateComboAndMakeUpShopCartAndAmount(ShopCartWithAmountVO shopCartWithAmount) {
        List<ShopCartDto> shopCarts = shopCartWithAmount.getShopCarts();
        shopCarts = calculateDiscountAndMakeUpShopCart(shopCarts);

        double total = 0.0;
        int count = 0;
        double reduce = 0.0;
        double totalTransFee = 0L;
        double totalFreeTransFee = 0L;

        // 计算满减+套餐的金额
        for (ShopCartDto shopCart : shopCarts) {
            List<ShopCartItemDiscountDto> shopCartItemDiscounts = shopCart.getShopCartItemDiscounts();
            for (ShopCartItemDiscountDto shopCartItemDiscount : shopCartItemDiscounts) {
                List<ShopCartItemDto> shopCartItems = shopCartItemDiscount.getShopCartItems();
                ChooseComboItemDto chooseComboItem = shopCartItemDiscount.getChooseComboItemDto();
                ChooseDiscountItemDto chooseDiscountItem = shopCartItemDiscount.getChooseDiscountItemDto();
                if (Objects.nonNull(chooseDiscountItem)) {
                    Double needPiece = chooseDiscountItem.getNeedAmount();
                    // 如果满足优惠活动
                    if (Objects.equals(DiscountRule.M2M.value(), chooseDiscountItem.getDiscountRule()) && needPiece <= chooseDiscountItem.getProdsPrice()) {
                        //满钱减钱
                        reduce = Arith.add(reduce, chooseDiscountItem.getReduceAmount());
                    } else if (Objects.equals(DiscountRule.P2D.value(), chooseDiscountItem.getDiscountRule()) && needPiece <= chooseDiscountItem.getProdCount()) {
                        //满件打折
                        reduce = Arith.add(reduce, chooseDiscountItem.getReduceAmount());
                    } else if (Objects.equals(DiscountRule.M2D.value(), chooseDiscountItem.getDiscountRule()) && needPiece <= chooseDiscountItem.getProdsPrice()) {
                        //满钱打折
                        reduce = Arith.add(reduce, chooseDiscountItem.getReduceAmount());
                    }
                }
                if (Objects.nonNull(chooseComboItem) && BooleanUtil.isTrue(shopCartItems.get(0).getIsChecked())) {
                    reduce = Arith.add(reduce, chooseComboItem.getPreferentialAmount());
                }
                for (ShopCartItemDto shopCartItem : shopCartItems) {
                    if (!BooleanUtil.isTrue(shopCartItem.getIsChecked()) && Objects.nonNull(shopCartItem.getIsChecked())) {
                        continue;
                    }
                    count += shopCartItem.getProdCount();
                    total = Arith.add(total, shopCartItem.getProductTotalAmount());
                }
            }
            // 计算运费
            calculateTransFee(shopCartWithAmount, shopCart);
            totalFreeTransFee = Arith.add(totalFreeTransFee, shopCart.getFreeTransFee());
            totalTransFee = Arith.add(totalTransFee, shopCart.getTransFee());
        }
        shopCartWithAmount.setCount(count);
        shopCartWithAmount.setTotalMoney(total);
        shopCartWithAmount.setSubtractMoney(Arith.add(reduce, totalFreeTransFee));
        shopCartWithAmount.setFinalMoney(Arith.sub(Arith.add(total, totalTransFee), Arith.add(reduce, totalFreeTransFee)));
        shopCartWithAmount.setShopCarts(shopCarts);
        shopCartWithAmount.setFreightAmount(totalTransFee);
        shopCartWithAmount.setFreeTransFee(totalFreeTransFee);
    }

    private void calculateTransFee(ShopCartWithAmountVO shopCartWithAmount, ShopCartDto shopCart) {
        Map<Long, ShopTransFeeVO> shopIdWithShopTransFee = null;
        if (Objects.nonNull(shopCartWithAmount.getUserDeliveryInfo())) {
            shopIdWithShopTransFee = shopCartWithAmount.getUserDeliveryInfo().getShopIdWithShopTransFee();
        }
        if (Objects.nonNull(shopIdWithShopTransFee) && shopIdWithShopTransFee.containsKey(shopCart.getShopId())) {
            ShopTransFeeVO shopTransFeeVO = shopIdWithShopTransFee.get(shopCart.getShopId());
            // 店铺的实付 = 购物车实付 + 运费
            shopCart.setActualTotal(Arith.add(shopCart.getActualTotal(), shopTransFeeVO.getTransFee()));
            // 店铺免运费金额
            shopCart.setFreeTransFee(shopTransFeeVO.getFreeTransFee());
            // 店铺优惠金额
            shopCart.setShopReduce(shopCart.getShopReduce());
            // 运费
            shopCart.setTransFee(shopTransFeeVO.getTransFee());
        } else {
            shopCart.setTransFee(0.0);
            shopCart.setFreeTransFee(0.0);
        }
    }

    @Override
    public List<ShopCartDto> calculateDiscountAndMakeUpShopCart(List<ShopCartDto> shopCarts) {
        for (ShopCartDto shopCart : shopCarts) {
            List<ShopCartItemDto> shopCartItemList = new ArrayList<>();
            shopCart.getShopCartItemDiscounts().forEach(item -> shopCartItemList.addAll(item.getShopCartItems()));
            reBuildShopCart(shopCart, shopCartItemList);
        }
        return shopCarts;
    }


    /**
     * 重新组装购物车
     */
    private void reBuildShopCart(ShopCartDto shopCart, List<ShopCartItemDto> shopCartItemList) {
        List<Long> skuIds = new ArrayList<>();
        Set<Long> comboIds = new HashSet<>();
        Map<Long, List<ShopCartItemDto>> shopCartItemMap = new HashMap<>(shopCartItemList.size());
        for (ShopCartItemDto shopCartItem : shopCartItemList) {
            if (Objects.isNull(shopCartItem.getComboId()) || shopCartItem.getComboId() == 0) {
                continue;
            }
            comboIds.add(shopCartItem.getComboId());
            Long basketId = shopCartItem.getBasketId();
            if (!Objects.equals(shopCartItem.getParentBasketId(), Constant.ZERO_LONG)) {
                basketId = shopCartItem.getParentBasketId();
            }
            List<ShopCartItemDto> shopCartItemDtoList = shopCartItemMap.get(basketId);
            if (CollUtil.isEmpty(shopCartItemDtoList)) {
                shopCartItemMap.put(basketId, new ArrayList<>());
                shopCartItemDtoList = shopCartItemMap.get(basketId);
            }
            shopCartItemDtoList.add(shopCartItem);
            skuIds.add(shopCartItem.getSkuId());
        }
        // 组装购物车套餐金额信息
        buildShopCartItemCombo(shopCart, shopCartItemMap, skuIds, comboIds);
        // 组装赠品信息
        buildGiveaway(shopCartItemList);
    }

    private void buildShopCartItemCombo(ShopCartDto shopCart, Map<Long, List<ShopCartItemDto>> shopCartItemMap, List<Long> skuIds, Set<Long> comboIds) {
        if (MapUtil.isEmpty(shopCartItemMap)) {
            return;
        }
        // 购物车列表进行分类
        List<ShopCartItemDiscountDto> shopCartItemComboList = new ArrayList<>();

        // 获取套餐信息
        List<ComboSkuDto> combos = comboService.listComboByComboIds(comboIds, skuIds);
        Map<Long, List<ComboSkuDto>> comboMap = combos.stream().collect(Collectors.groupingBy(ComboSkuDto::getComboId));
        List<Long> deleteList = new ArrayList<>();

        double reduce = 0.0;
        double total = 0.0;
        int totalCount = 0;
        for (List<ShopCartItemDto> shopCartItemList : shopCartItemMap.values()) {
            double amount = 0.0;
            double comboAmount = 0.0;
            ShopCartItemDiscountDto shopCartItemDiscount = new ShopCartItemDiscountDto();
            ChooseComboItemDto chooseComboItem = new ChooseComboItemDto();
            int comboCount = 0;
            Iterator<ShopCartItemDto> iterator = shopCartItemList.iterator();
            Map<Long, ComboSkuDto> comboSkuMap = new HashMap<>(shopCartItemList.size());
            while (iterator.hasNext()) {
                ShopCartItemDto shopCartItem = iterator.next();
                if (MapUtil.isEmpty(comboSkuMap)) {
                    List<ComboSkuDto> comboSkuList = comboMap.get(shopCartItem.getComboId());
                    if (CollUtil.isEmpty(comboSkuList)) {
                        break;
                    }
                    comboSkuMap = comboSkuList.stream().collect(Collectors.toMap(ComboSkuDto::getSkuId, c -> c));
                }
                ComboSkuDto comboSkuItem = comboSkuMap.get(shopCartItem.getSkuId());
                if (Objects.isNull(comboSkuItem)) {
                    if (Objects.equals(shopCartItem.getIsMainProd(), 1)) {
                        // 套餐不存在，或已失效
                        throw new YamiShopBindException("yami.prod.combo.expired");
                    }
                    deleteList.add(shopCartItem.getBasketId());
                    iterator.remove();
                    continue;
                }
                if (comboCount == 0) {
                    comboCount = shopCartItem.getComboCount();
                }
                if (Objects.isNull(chooseComboItem.getName())) {
                    chooseComboItem.setName(comboSkuItem.getName());
                    chooseComboItem.setComboId(comboSkuItem.getComboId());
                }
                if (Objects.equals(shopCartItem.getParentBasketId(), Constant.ZERO_LONG)) {
                    chooseComboItem.setMainProdBasketId(shopCartItem.getBasketId());
                }
                amount = Arith.add(amount, Arith.mul(comboSkuItem.getPrice(), comboSkuItem.getLeastNum()));
                shopCartItem.setProdCount(comboSkuItem.getLeastNum() * comboCount);
                shopCartItem.setComboPrice(comboSkuItem.getMatchingPrice());
                shopCartItem.setProductTotalAmount(Arith.mul(shopCartItem.getPrice(), shopCartItem.getProdCount()));
                shopCartItem.setActualTotal(Arith.mul(shopCartItem.getComboPrice(), shopCartItem.getProdCount()));
                shopCartItem.setComboAmount(Arith.sub(shopCartItem.getProductTotalAmount(), shopCartItem.getActualTotal()));
                shopCartItem.setShareReduce(shopCartItem.getComboAmount());
                comboAmount = Arith.add(comboAmount, Arith.mul(comboSkuItem.getMatchingPrice(), comboSkuItem.getLeastNum()));
                total = Arith.add(total, shopCartItem.getProductTotalAmount());
                totalCount += shopCartItem.getProdCount();
            }
            chooseComboItem.setComboCount(comboCount);
            chooseComboItem.setComboAmount(comboAmount);
            chooseComboItem.setComboTotalAmount(Arith.mul(comboAmount, comboCount));
            chooseComboItem.setPreferentialAmount(Arith.mul(Arith.sub(amount, comboAmount), comboCount));
            reduce = Arith.add(reduce, chooseComboItem.getPreferentialAmount());
            shopCartItemList.sort(Comparator.comparingLong(ShopCartItemDto::getBasketId));
            shopCartItemDiscount.setChooseComboItemDto(chooseComboItem);
            shopCartItemDiscount.setActivityType(OrderActivityType.COMBO.value());
            shopCartItemDiscount.setShopCartItems(shopCartItemList);
            shopCartItemComboList.add(shopCartItemDiscount);
        }
        // 删除无效的购物车项
        if (CollUtil.isNotEmpty(deleteList)) {
            basketService.deleteShopCartItemsByBasketIds(SecurityUtils.getUser().getUserId(), deleteList);
        }
        setShopCartInfo(shopCart, shopCartItemComboList, reduce, total, totalCount);
    }

    private void setShopCartInfo(ShopCartDto shopCart, List<ShopCartItemDiscountDto> shopCartItemComboList, double reduce, double total, int totalCount) {
        // 套餐商品放在最上面
        for (ShopCartItemDiscountDto shopCartItemDiscount : shopCart.getShopCartItemDiscounts()) {
            if (CollUtil.isEmpty(shopCartItemDiscount.getShopCartItems())) {
                continue;
            }
            if (Objects.nonNull(shopCartItemDiscount.getShopCartItems().get(0).getComboId()) && shopCartItemDiscount.getShopCartItems().get(0).getComboId() != 0L) {
                continue;
            }
            shopCartItemComboList.add(shopCartItemDiscount);
        }
        shopCart.setShopCartItemDiscounts(shopCartItemComboList);
        shopCart.setTotal(Arith.add(shopCart.getTotal(), total));
        shopCart.setTotalCount(shopCart.getTotalCount() + totalCount);
        shopCart.setActualTotal(Arith.sub(shopCart.getTotal(), Arith.add(reduce, shopCart.getShopReduce())));
        shopCart.setComboReduce(reduce);
        shopCart.setShopReduce(Arith.add(shopCart.getShopReduce(), reduce));
    }

    private void buildGiveaway(List<ShopCartItemDto> shopCartItemList) {
        Map<Long, Integer> skuMap = new HashMap<>(16);
        // 把订单中其他商品的库存减掉才能去判断赠品的库存
        for (ShopCartItemDto shopCartItem : shopCartItemList) {
            Integer num = skuMap.get(shopCartItem.getSkuId());
            if (Objects.isNull(num)) {
                skuMap.put(shopCartItem.getSkuId(), 0);
                num = shopCartItem.getProdCount();
            } else {
                num += shopCartItem.getProdCount();
            }
            skuMap.put(shopCartItem.getSkuId(), num);
        }

        // 进行赠品库存判断
        for (ShopCartItemDto shopCartItem : shopCartItemList) {
            GiveawayVO giveaway = giveawayService.getGiveawayProdAndStockByProdId(shopCartItem.getProdId());
            if (Objects.isNull(giveaway)) {
                continue;
            }
            if (shopCartItem.getProdCount() >= giveaway.getBuyNum() && CollUtil.isNotEmpty(giveaway.getGiveawayProds())) {
                // 满足赠品条件
                Integer num = shopCartItem.getProdCount() / giveaway.getBuyNum();
                List<GiveawayProdVO> giveawayProdList = new ArrayList<>();
                for (GiveawayProdVO giveawayProd : giveaway.getGiveawayProds()) {
                    Long skuId = giveawayProd.getSkuId();
                    Integer stock = skuMap.get(skuId);
                    if (Objects.isNull(stock)) {
                        stock = 0;
                    }
                    // 库存不足，不再显示
                    giveawayProd.setSkuStock(giveawayProd.getSkuStock() - stock);
                    if (giveawayProd.getSkuStock() < 1) {
                        continue;
                    }

                    Integer giveawayNum = num * giveawayProd.getGiveawayNum();
                    if (giveawayProd.getSkuStock() < giveawayNum) {
                        giveawayNum = giveawayProd.getSkuStock();
                    }
                    stock += giveawayNum;
                    skuMap.put(skuId, stock);
                    giveawayProd.setSkuStock(giveawayProd.getSkuStock() - giveawayNum);
                    giveawayProd.setGiveawayNum(giveawayNum);
                    giveawayProdList.add(giveawayProd);
                }
                giveaway.setGiveawayProds(giveawayProdList);
                shopCartItem.setGiveaway(giveaway);
            }
        }
    }

    //=====================加购=====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ServerResponseEntity<String> comboAddCart(ChangeShopCartParam param, String userId, List<ShopCartItemDto> shopCartItems) {
        // 过滤掉不是该套餐的商品，并组装成map
        Map<Long, ShopCartItemDto> mainMap = new HashMap<>(shopCartItems.size());
        Map<Long, List<ShopCartItemDto>> basketMap = new HashMap<>(shopCartItems.size());
        setAddMapData(param, shopCartItems, mainMap, basketMap);
        if (Objects.isNull(param.getMatchingSkuIds())) {
            param.setMatchingSkuIds(new ArrayList<>());
        }
        ShopCartItemDto shopCartItemDto = mainMap.get(param.getSkuId());
        // 获取商品套餐数据
        Combo combo = comboService.getComboWithProdInfoById(param.getComboId());
        // 减少套餐的数量
        if (param.getCount() < 0) {
            if (Objects.isNull(shopCartItemDto)) {
                // 商品已从购物车移除
                return ServerResponseEntity.showFailMsg(I18nMessage.getMessage("yami.product.remove"));
            }
            subComboNum(param, userId, basketMap, shopCartItemDto);
            return ServerResponseEntity.success();
        }
        // 添加新的套餐
        if (!mainMap.containsKey(param.getSkuId()) || Objects.isNull(shopCartItemDto)) {
            addCombo(param, userId, combo);
            return ServerResponseEntity.success(I18nMessage.getMessage("yami.activity.add.success"));
        }
        List<ShopCartItemDto> shopCartItemDtoList = basketMap.get(shopCartItemDto.getBasketId());
        List<Long> skuIds = shopCartItemDtoList.stream().map(ShopCartItemDto::getSkuId).collect(Collectors.toList());
        skuIds.remove(param.getSkuId());
        // 加购的套餐与购物车中的套餐不匹配
        if (!comboEqual(skuIds, param.getMatchingSkuIds())) {
            throw new YamiShopBindException("yami.combo.prod.already.exists");
        }
        // 购物车已存在该套餐，增加套餐数量
        int comboCount = param.getCount() + shopCartItemDto.getComboCount();
        String message = I18nMessage.getMessage("yami.insufficient.inventory");
        for (ComboProd comboProd : combo.getMatchingProds()) {
            ComboProdSku comboProdSku = getComboProdSkuByList(comboProd.getSkuList(), param.getMatchingSkuIds());
            if(Objects.isNull(comboProdSku)) {
                continue;
            }
            if (comboProd.getLeastNum() * comboCount > comboProdSku.getStocks()) {
                // 库存不足
                return ServerResponseEntity.showFailMsg(comboProd.getProdName() + message);
            }
        }
        ComboProd mainComboProd = combo.getMainProd();
        ComboProdSku comboProdSku = getComboProdSkuByList(mainComboProd.getSkuList(), Collections.singletonList(param.getSkuId()));
        if (Objects.isNull(comboProdSku)) {
            // 主商品信息错误，请刷新后重试
            throw new YamiShopBindException("yami.main.prod.wrong");
        }
        if (mainComboProd.getLeastNum() * comboCount > comboProdSku.getStocks()) {
            // 库存不足
            return ServerResponseEntity.showFailMsg(mainComboProd.getProdName() + message);
        }
        List<Basket> baskets = new ArrayList<>();
        for (ShopCartItemDto shopCartItem : shopCartItemDtoList) {
            Basket basket = new Basket();
            basket.setBasketId(shopCartItem.getBasketId());
            basket.setComboCount(comboCount);
            basket.setIsChecked(shopCartItem.getIsChecked());
            baskets.add(basket);
        }
        basketService.updateShopCartItemBatch(userId, baskets);
        return ServerResponseEntity.success();
    }

    private void setAddMapData(ChangeShopCartParam param, List<ShopCartItemDto> shopCartItems, Map<Long, ShopCartItemDto> mainMap, Map<Long, List<ShopCartItemDto>> basketMap) {
        for (ShopCartItemDto shopCartItem : shopCartItems) {
            if (!Objects.equals(shopCartItem.getComboId(), param.getComboId())) {
                continue;
            }
            if (Objects.equals(shopCartItem.getParentBasketId(), Constant.ZERO_LONG)) {
                mainMap.put(shopCartItem.getSkuId(), shopCartItem);
            }
            Long basketId = shopCartItem.getBasketId();
            if (!Objects.equals(shopCartItem.getParentBasketId(), Constant.ZERO_LONG)) {
                basketId = shopCartItem.getParentBasketId();
            }
            List<ShopCartItemDto> shopCartItemList = basketMap.get(basketId);
            if (CollUtil.isEmpty(shopCartItemList)) {
                basketMap.put(basketId, new ArrayList<>());
                shopCartItemList = basketMap.get(basketId);
            }
            shopCartItemList.add(shopCartItem);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ServerResponseEntity<String> comboChangeCart(ChangeShopCartParam param, String userId, List<ShopCartItemDto> shopCartItems) {
        // 根据套餐和购物车id 找到旧sku信息
        // 如果原有一模一样的套餐就数量+count，没有的话判断主商品相同规格的已经存在就不让新增，不存在就新增
        Map<Long, ShopCartItemDto> prodBasketMap = new HashMap<>(shopCartItems.size());
        Map<Long, ShopCartItemDto> mainMap = new HashMap<>(shopCartItems.size());
        Map<Long, List<ShopCartItemDto>> basketMap = new HashMap<>(shopCartItems.size());
        setChangeMapData(param, shopCartItems, prodBasketMap, mainMap, basketMap);

        ShopCartItemDto oldShopCartItem = prodBasketMap.get(param.getBasketId());
        // 获取套餐信息
        Combo combo = comboService.getComboWithProdInfoById(param.getComboId());
        if (!Objects.equals(oldShopCartItem.getParentBasketId(), Constant.ZERO_LONG)) {
            // 不是主商品更改sku，无需判断规格重复，判断库存即可
            List<ComboProd> matchingProds = combo.getMatchingProds();
            for (ComboProd comboProd : matchingProds) {
                if (!Objects.equals(comboProd.getProdId(), param.getProdId())) {
                    continue;
                }
                ComboProdSku comboProdSku = getComboProdSkuByList(comboProd.getSkuList(), Collections.singletonList(param.getSkuId()));
                if (Objects.isNull(comboProdSku)) {
                    continue;
                }
                if (comboProd.getLeastNum() * oldShopCartItem.getComboCount() > comboProdSku.getStocks()) {
                    // 库存不足
                    return ServerResponseEntity.showFailMsg(comboProd.getProdName() + I18nMessage.getMessage("yami.insufficient.inventory"));
                }
                // 一切正常则更改skuId
                Basket basket = new Basket();
                basket.setBasketId(oldShopCartItem.getBasketId());
                basket.setSkuId(param.getSkuId());
                basket.setUserId(userId);
                basketService.updateShopCartItem(basket);
                return ServerResponseEntity.success();
            }
        } else {
            ShopCartItemDto shopCartItem = mainMap.get(param.getSkuId());
            if (!mainMap.containsKey(param.getSkuId()) || Objects.isNull(shopCartItem)) {
                // 该主商品规格未曾加入购物车
                // 删除已有的再新增
                basketService.deleteShopCartItemsByBasketIds(userId, Collections.singletonList(param.getBasketId()));
                addCombo(param, userId, combo);
                return ServerResponseEntity.success();
            }
            List<ShopCartItemDto> shopCartItemList = basketMap.get(shopCartItem.getBasketId());
            List<Long> skuIds = shopCartItemList.stream().map(ShopCartItemDto::getSkuId).collect(Collectors.toList());
            skuIds.remove(param.getSkuId());
            if (!comboEqual(skuIds, param.getMatchingSkuIds())) {
                // 购物车中已存在该规格不同搭配的套餐， 请勿重复加购
                return ServerResponseEntity.showFailMsg(I18nMessage.getMessage("yami.combo.prod.already.exists"));
            }
            // 购物车已存在该套餐，增加套餐数量
            int comboCount = param.getCount() + shopCartItem.getComboCount();
            for (ComboProd comboProd : combo.getMatchingProds()) {
                ComboProdSku comboProdSku = getComboProdSkuByList(comboProd.getSkuList(), param.getMatchingSkuIds());
                if (Objects.isNull(comboProdSku)) {
                    continue;
                }
                if (comboProd.getLeastNum() * comboCount > comboProdSku.getStocks()) {
                    // 库存不足
                    return ServerResponseEntity.showFailMsg(comboProd.getProdName() + I18nMessage.getMessage("yami.insufficient.inventory"));
                }
            }
            ComboProd mainProd = combo.getMainProd();
            ComboProdSku comboProdSku = getComboProdSkuByList(mainProd.getSkuList(), Collections.singletonList(param.getSkuId()));
            if (Objects.isNull(comboProdSku)) {
                // 主商品信息错误，请刷新后重试
                return ServerResponseEntity.showFailMsg(I18nMessage.getMessage("yami.main.prod.wrong"));
            }
            if (mainProd.getLeastNum() * comboCount > comboProdSku.getStocks()) {
                // 库存不足
                return ServerResponseEntity.showFailMsg(mainProd.getProdName() + I18nMessage.getMessage("yami.insufficient.inventory"));
            }
            List<Basket> baskets = new ArrayList<>();
            for (ShopCartItemDto shopCartItemDto : shopCartItemList) {
                Basket basket = new Basket();
                basket.setBasketId(shopCartItemDto.getBasketId());
                basket.setComboCount(comboCount);
                if (Objects.equals(shopCartItemDto.getSkuId(), param.getOldSkuId())) {
                    basket.setSkuId(param.getSkuId());
                }
                baskets.add(basket);
            }
            basketService.updateShopCartItemBatch(userId, baskets);
            // 删除原本的
            basketService.deleteShopCartItemsByBasketIds(userId, Collections.singletonList(param.getBasketId()));
            return ServerResponseEntity.success();
        }
        return ServerResponseEntity.success();
    }

    private void setChangeMapData(ChangeShopCartParam param, List<ShopCartItemDto> shopCartItems, Map<Long, ShopCartItemDto> prodMap,Map<Long, ShopCartItemDto> mainMap, Map<Long, List<ShopCartItemDto>> basketMap) {
        for (ShopCartItemDto shopCartItem : shopCartItems) {
            if (!Objects.equals(shopCartItem.getComboId(), param.getComboId())) {
                continue;
            }
            if (Objects.equals(shopCartItem.getParentBasketId(), Constant.ZERO_LONG)) {
                mainMap.put(shopCartItem.getSkuId(), shopCartItem);
            }
            prodMap.put(shopCartItem.getBasketId(), shopCartItem);
            Long basketId = shopCartItem.getBasketId();
            if (!Objects.equals(shopCartItem.getParentBasketId(), Constant.ZERO_LONG)) {
                basketId = shopCartItem.getParentBasketId();
            }
            List<ShopCartItemDto> shopCartItemList = basketMap.get(basketId);
            if (CollUtil.isEmpty(shopCartItemList)) {
                basketMap.put(basketId, new ArrayList<>());
                shopCartItemList = basketMap.get(basketId);
            }
            shopCartItemList.add(shopCartItem);
        }
    }

    private void subComboNum(ChangeShopCartParam param, String userId, Map<Long, List<ShopCartItemDto>> basketMap, ShopCartItemDto shopCartItemDto) {
        // 套餐数量小于零，删除该套餐
        if (shopCartItemDto.getComboCount() <= 0) {
            basketService.deleteShopCartItemsByBasketIds(userId, Collections.singletonList(shopCartItemDto.getBasketId()));
        } else {
            int comboCount = param.getCount() + shopCartItemDto.getComboCount();
            List<ShopCartItemDto> shopCartItemDtos = basketMap.get(shopCartItemDto.getBasketId());
            List<Basket> baskets = new ArrayList<>();
            for (ShopCartItemDto cartItemDto : shopCartItemDtos) {
                Basket basket = new Basket();
                basket.setUserId(userId);
                basket.setComboCount(comboCount);
                basket.setBasketId(cartItemDto.getBasketId());
                baskets.add(basket);
            }
            basketService.updateShopCartItemBatch(userId, baskets);
        }
    }

    private void addCombo(ChangeShopCartParam param, String userId, Combo combo) {
        Basket mainBasket = getMainBasket(combo, param, userId);
        mainBasket.setParentBasketId(Constant.ZERO_LONG);
        basketService.save(mainBasket);
        if(CollUtil.isNotEmpty(param.getMatchingSkuIds())) {
            List<Basket> matchingBasket = getMatchingBasket(combo, param, userId, mainBasket.getBasketId());
            basketService.saveBatch(matchingBasket);
        }
        //清除购物车缓存
        basketService.removeCacheByUserIds(Collections.singletonList(userId));
    }

    private Basket getMainBasket(Combo combo, ChangeShopCartParam param, String userId) {
        ComboProd comboProd = combo.getMainProd();
        if ( Objects.isNull(comboProd)) {
            throw new YamiShopBindException("yami.activity.cannot.find");
        }
        ComboProdSku comboProdSku = null;
        for (ComboProdSku item : comboProd.getSkuList()) {
            if (Objects.equals(param.getSkuId(), item.getSkuId())) {
                comboProdSku = item;
                break;
            }
        }
        if ( Objects.isNull(comboProdSku)) {
            throw new YamiShopBindException("yami.activity.cannot.find");
        }
        Basket basket = new Basket();
        basket.setShopId(combo.getShopId());
        basket.setProdId(param.getProdId());
        basket.setSkuId(param.getSkuId());
        basket.setUserId(userId);
        basket.setBasketCount(comboProd.getLeastNum());
        basket.setBasketDate(new Date());
        basket.setComboId(combo.getComboId());
        basket.setComboCount(param.getCount());
        if (comboProd.getLeastNum() * param.getCount() > comboProdSku.getStocks()) {
            // 库存不足
            throw new YamiShopBindException("yami.insufficient.inventory");
        }
        return basket;
    }

    private List<Basket> getMatchingBasket(Combo combo, ChangeShopCartParam param, String userId, Long basketId) {
        List<Basket> baskets = new ArrayList<>();
        for (ComboProd comboProd : combo.getMatchingProds()) {
            ComboProdSku comboProdSku = getComboProdSkuByList(comboProd.getSkuList(), param.getMatchingSkuIds());
            if (Objects.isNull(comboProdSku)) {
                if (Objects.equals(comboProd.getRequired(), 1)) {
                    // 您的信息有误，请尝试刷新后再进行操作
                    throw new YamiShopBindException("yami.information.is.wrong");
                }
                continue;
            }
            Basket basket = new Basket();
            basket.setShopId(combo.getShopId());
            basket.setProdId(comboProd.getProdId());
            basket.setSkuId(comboProdSku.getSkuId());
            basket.setUserId(userId);
            basket.setBasketCount(comboProd.getLeastNum());
            basket.setBasketDate(new Date());
            basket.setComboId(combo.getComboId());
            basket.setComboCount(param.getCount());
            basket.setParentBasketId(basketId);
            if (comboProd.getLeastNum() * param.getCount() > comboProdSku.getStocks()) {
                // 库存不足
                String message = I18nMessage.getMessage("yami.insufficient.inventory");
                throw new YamiShopBindException(comboProd.getProdName() + message);
            }
            baskets.add(basket);
        }
        return baskets;
    }

    private ComboProdSku getComboProdSkuByList(List<ComboProdSku> skuList, List<Long> matchingSkuIds) {
        for (ComboProdSku comboProdSku : skuList) {
            if (matchingSkuIds.contains(comboProdSku.getSkuId())) {
                return comboProdSku;
            }
        }
        return null;
    }

    private boolean comboEqual(List<Long> skuIds, List<Long> matchingSkuIds) {
        if (skuIds.size() != matchingSkuIds.size()) {
            return false;
        }
        for (Long skuId : skuIds) {
            if(!matchingSkuIds.contains(skuId)) {
                return false;
            }
        }
        return true;
    }
}
