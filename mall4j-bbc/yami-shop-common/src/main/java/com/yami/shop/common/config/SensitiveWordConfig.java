/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.common.config;

import java.util.HashSet;

/**
 * @author Citrus
 * @date 2021/8/6 14:32
 */
public class SensitiveWordConfig {

    private static final String SENSITIVE_WORD_SEARCH_URL = "/sys/pconfig/info/SENSITIVE_WORDS";
    private static final String WEBJARS_URL = "/webjars/**";
    private static final String SWAGGER_URL = "/swagger/**";
    private static final String API_DOCS_URL = "/v2/api-docs";
    private static final String DOC_URL = "/doc.html";
    private static final String SWAGGER_UI_URL = "/swagger-ui.html";
    private static final String SWAGGER_RESOURCES_URL = "/swagger-resources/**";
    private static final String CAPTCHA_URL = "/captcha.jpg";
    private static final String SAVE_CONFIG = "/sys/pconfig/save";
    /**
     * excel
     */
    private static final String SOLD_EXCEL = "/order/order/soldExcel";
    private static final String PL_SOLD_EXCEL = "/platform/order/soldExcel";
    private static final String SCORE_SOLD_EXCEL = "/score/order/soldExcel";
    private static final String IMPORT_USER_MODEL = "/admin/user/importUserModel";
    private static final String ADMIN_FORM_EXCEL = "/admin/form/formExcel";
    private static final String PLATFORM_FORM_EXCEL = "/platform/form/formExcel";
    private static final String EXPORT_USER = "/admin/user/exportUser";
    private static final String UN_DELIVERY_ORDER_EXCEL = "/order/order/unDeliveryOrderExcel";
    private static final String DOWNLOAD_MODEL = "/prod/prod/downloadModel";
    private static final String PLATFORM_EXPORT_PROD = "/platform/search/prod/prodExport";
    private static final String MULTISHOP_EXPORT_PROD = "/admin/search/prod/prodExport";
    private static final String GET_FIN_DETAIL_FORM = "/platform/financialManagement/getFinanDetailForm";
    private static final String GET_PAY_INFO_FORM = "/platform/account_detail/get_pay_info_form";
    private static final String GET_REFUND_INFO_FORM = "/platform/account_detail/get_refund_info_form";
    private static final String EXPORT_SHOP = "/platform/shopDetail/exportShop";
    private static final String EXPORT_SUPPLIER = "/supplier/supplier/exportSupplier";
    private static final String IMPORT_SUPPLIER = "/supplier/supplier/importExcel";
    private static final String MODEL_SUPPLIER = "/supplier/supplier/downLoadModel";
    private static final String MODEL_SUPPLIER_PROD = "/supplier/supplierProd/downLoadModel";
    private static final String IMPORT_SUPPLIER_PROD = "/supplier/supplierProd/importExcel";
    private static final String EXPORT_SUPPLIER_PROD = "/supplier/supplierProd/exportSupplierProd";
    private static final String EXPORT_STOCK_BILL_LOG = "/shop/stockBillLog/exportStockBillLog";
    private static final String EXPORT_STOCK_BILL_LOG_ITEM = "/shop/stockBillLogItem/downloadModel";
    private static final String IMPORT_STOCK_BILL_LOG_ITEM = "/shop/stockBillLogItem/importExcel";
    private static final String STOCK_FLOW_EXPORT_EXCEL = "/shop/stockBillLogItem/exportFlow";
    private static final String EXPORT_SKU = "/sku/exportSkuList";
    private static final String MODEL_TAKE_PROD = "/stock/takeStockProd/downLoadModel";
    private static final String IMPORT_TAKE_PROD = "/stock/takeStockProd/importExcel";
    private static final String EXPORT_TAKE_PROD = "/stock/takeStockProd/exportTakeStockProd";
    private static final String EXPORT_TAKE_STOCK = "/stock/takeStock/exportTakeStock";
    private static final String PURCHASES_ORDER_DOWNLOAD_MODEL = "/purchase/order/downloadModel";
    private static final String PURCHASES_ORDER_EXPORT_EXCEL = "/purchase/order/exportExcel";
    private static final String PURCHASES_ORDER_INBOUND_EXPORT = "/purchase/order/inbound/export";
    private static final String PURCHASES_ORDER_INBOUND_DOWNLOAD_MODEL = "/purchase/order/inbound/downloadModel";
    private static final String PURCHASES_ORDER_INBOUND_EXPORT_EXCEL = "/purchase/order/inbound/exportExcel";
    private static final String DISTRIBUTION_QR_CODE_INVITATION = "/p/distribution/qrCode/invitation";
    private static final String MINI_QR_CODE = "/qrcodeTicket/miniQrCode";
    private static final String CATEGORY_EXPORT = "/prod/category/export";
    private static final String MEMBER_TREND_EXPORT = "/platform/flowCustomerAnalysis/memberTrendExport";
    private static final String FLOW_ANALYSIS_EXPORT = "/platform/flowAnalysis/analysisDataExport";
    private static final String FLOW_TREND_EXPORT = "/platform/flowAnalysis/flowTrendExport";
    private static final String FLOW_SOUR_EXPORT = "/platform/flowAnalysis/flowSourExport";
    private static final String FLOW_USER_ANALYSIS_EXPORT = "/platform/flowUserAnalysis/userAnalysisDataExport";
    private static final String ACCOUNT_DETAIL_EXPORT = "/platform/accountDetail/getPayAndRefundInfoForm";
    private static final String SHOP_WALLET_PLATFORM_EXPORT = "/shop/shopWallet/getShopWalletLogForm";
    private static final String FINANCE_DETAIL_FORM_EXPORT = "/platform/financialManagement/getFinanceDetailForm";
    private static final String MULTISHOP_PROD_EFFECT_EXPORT = "/multishop/prodAnalysis/prodEffectExport";
    private static final String PLATFORM_PROD_EFFECT_EXPORT = "/platform/prodAnalysis/prodEffectExport";
    private static final String PLATFORM_PAY_AMOUNT_TOP_EXPORT = "/platform/prodAnalysis/payAmountTopExport";
    private static final String MULTISHOP_PAY_AMOUNT_TOP_EXPORT = "/multishop/prodAnalysis/payAmountTopExport";
    private static final String PROD_SURVEY_EXPORT = "/platform/prodAnalysis/prodSurveyExport";
    /**
     * 商家提现信息
     */
    private static final String EXPORT_SHOPWITHDRAWCASH = "/shop/shopWithdrawCash/exportShopWithdrawCash";
    /**
     * 权限
     */
    private static final String GET_SHOP_MENU = "/sys/shopMenu/nav";
    /**
     * jsapi签名
     */
    private static final String CREATE_JSAPI_SIGNATURE = "/p/wx/jsapi/createJsapiSignature";
    /**
     * 导出待发货的积分订单
     */
    private static final String EXPORT_SCORE_ORDER = "/score/order/unDeliveryOrderExcel";
    /**
     * 导入待发货的积分订单
     */
    private static final String IMPORT_SCORE_ORDER = "/score/order/exportOrderExcel";
    /**
     * 导出待发货的普通订单
     */
    private static final String EXPORT_COMMON_ORDER = "/platform/order/unDeliveryOrderExcel";
    /**
     * 导入待发货的普通订单
     */
    private static final String IMPORT_COMMON_ORDER = "/platform/order/exportOrderExcel";

    /**
     * 商家核销记录导出
     */
    private static final String SHOP_COUPON_EXPORT = "/admin/couponUseRecord/download";

    /**
     * 平台核销记录导出
     */
    private static final String PLATFORM_COUPON_EXPORT = "/platform/couponUseRecord/download";

    /**
     * 店铺核销平台优惠券记录导出
     */
    private static final String SHOP_WRITE_OFF_PLATFORM_COUPON_EXPORT = "/platform/couponUseRecord/shopDownloadWriteOff";

    /**
     * 店铺停车记录导出
     */
    private static final String SHOP_CAR_EXPORT = "/multishop/carManage/downloadCarRecord";

    /**
     * 店铺停车记录导出
     */
    private static final String PLATFORM_CAR_EXPORT = "/platform/carManage/downloadCarRecord";

    /**
     * 商家核销记录导出
     */
    private static final String SHOP_CARD_USE_RECORD_EXPORT = "/admin/card/download";

    /**
     * 商家核销记录导出
     */
    private static final String PLATFORM_CARD_USE_RECORD_EXPORT = "/platform/card/download";

    /**
     * 平台提货卡(券)券核销记录导出
     */
    private static final String PLATFORM_WRITE_OFF_CARD_EXPORT = "/platform/cardUseRecord/shopDownloadWriteOff";

    /**
     * 平台购买提货卡券记录导出
     */
    private static final String PLATFORM_BUY_CARD_EXPORT = "/platform/cardUser/downloadBuyRecord";

    /**
     * 店铺购买提货卡券记录导出
     */
    private static final String SHOP_BUY_CARD_EXPORT = "/admin/cardUser/downloadBuyRecord";

    /**
     * 平台红云订单记录导出
     */
    private static final String PLATFORM_HY_EXPORT = "/platform/order/downloadHyRecord";

    /**
     * 店铺红云订单记录导出
     */
    private static final String SHOP_HY_EXPORT = "/order/order/downloadHyRecord";

    /**
     * 店铺导出优惠券领取记录
     */
    private static final String SHOP_COUPON_USER_EXPORT = "/admin/couponUser/downloadBuyRecord";

    /**
     * 平台导出店铺优惠券领取记录
     */
    private static final String PLATFORM_COUPON_USER_EXPORT = "/platform/couponUser/downloadCouponUser";

    /**
     * 商家会员卡使用记录导出
     */
    private static final String SHOP_BALANCE_USE_RECORD_EXPORT = "/multishop/userBalanceLog/download";

    /**
     * 平台会员卡使用记录导出
     */
    private static final String PLATFORM_BALANCE_USE_RECORD_EXPORT = "/user/userBalanceLog/download";
    /**
     * 平台会员卡充值记录导出
     */
    private static final String PLATFORM_BALANCE_RECHARGE_RECORD_EXPORT = "/user/userBalanceLog/RechargeRecordDownload";

    /**
     * 平台提货卡/券记录导出
     */
    private static final String PLATFORM_CARD_RECORD_EXPORT = "/platform/card/downLoadCardRecord";

    static final HashSet<String> SENSITIVE_WHITE_SET;
    static {
        SENSITIVE_WHITE_SET = new HashSet<>();
        SENSITIVE_WHITE_SET.add(SENSITIVE_WORD_SEARCH_URL);
        SENSITIVE_WHITE_SET.add(WEBJARS_URL);
        SENSITIVE_WHITE_SET.add(SWAGGER_URL);
        SENSITIVE_WHITE_SET.add(API_DOCS_URL);
        SENSITIVE_WHITE_SET.add(DOC_URL);
        SENSITIVE_WHITE_SET.add(SWAGGER_UI_URL);
        SENSITIVE_WHITE_SET.add(SWAGGER_RESOURCES_URL);
        SENSITIVE_WHITE_SET.add(CAPTCHA_URL);
        SENSITIVE_WHITE_SET.add(SAVE_CONFIG);
        SENSITIVE_WHITE_SET.add(SOLD_EXCEL);
        SENSITIVE_WHITE_SET.add(PL_SOLD_EXCEL);
        SENSITIVE_WHITE_SET.add(SCORE_SOLD_EXCEL);
        SENSITIVE_WHITE_SET.add(IMPORT_USER_MODEL);
        SENSITIVE_WHITE_SET.add(ADMIN_FORM_EXCEL);
        SENSITIVE_WHITE_SET.add(PLATFORM_FORM_EXCEL);
        SENSITIVE_WHITE_SET.add(EXPORT_USER);
        SENSITIVE_WHITE_SET.add(UN_DELIVERY_ORDER_EXCEL);
        SENSITIVE_WHITE_SET.add(DOWNLOAD_MODEL);
        SENSITIVE_WHITE_SET.add(PLATFORM_EXPORT_PROD);
        SENSITIVE_WHITE_SET.add(MULTISHOP_EXPORT_PROD);
        SENSITIVE_WHITE_SET.add(GET_FIN_DETAIL_FORM);
        SENSITIVE_WHITE_SET.add(GET_PAY_INFO_FORM);
        SENSITIVE_WHITE_SET.add(GET_REFUND_INFO_FORM);
        SENSITIVE_WHITE_SET.add(GET_SHOP_MENU);
        SENSITIVE_WHITE_SET.add(EXPORT_SHOP);
        SENSITIVE_WHITE_SET.add(IMPORT_SUPPLIER);
        SENSITIVE_WHITE_SET.add(EXPORT_SUPPLIER);
        SENSITIVE_WHITE_SET.add(MODEL_SUPPLIER);
        SENSITIVE_WHITE_SET.add(MODEL_SUPPLIER_PROD);
        SENSITIVE_WHITE_SET.add(IMPORT_SUPPLIER_PROD);
        SENSITIVE_WHITE_SET.add(EXPORT_SUPPLIER_PROD);
        SENSITIVE_WHITE_SET.add(EXPORT_STOCK_BILL_LOG);
        SENSITIVE_WHITE_SET.add(EXPORT_STOCK_BILL_LOG_ITEM);
        SENSITIVE_WHITE_SET.add(IMPORT_STOCK_BILL_LOG_ITEM);
        SENSITIVE_WHITE_SET.add(CREATE_JSAPI_SIGNATURE);
        SENSITIVE_WHITE_SET.add(STOCK_FLOW_EXPORT_EXCEL);
        SENSITIVE_WHITE_SET.add(EXPORT_SKU);
        SENSITIVE_WHITE_SET.add(MODEL_TAKE_PROD);
        SENSITIVE_WHITE_SET.add(IMPORT_TAKE_PROD);
        SENSITIVE_WHITE_SET.add(EXPORT_TAKE_PROD);
        SENSITIVE_WHITE_SET.add(EXPORT_TAKE_STOCK);
        SENSITIVE_WHITE_SET.add(PURCHASES_ORDER_DOWNLOAD_MODEL);
        SENSITIVE_WHITE_SET.add(PURCHASES_ORDER_EXPORT_EXCEL);
        SENSITIVE_WHITE_SET.add(PURCHASES_ORDER_INBOUND_EXPORT);
        SENSITIVE_WHITE_SET.add(PURCHASES_ORDER_INBOUND_DOWNLOAD_MODEL);
        SENSITIVE_WHITE_SET.add(PURCHASES_ORDER_INBOUND_EXPORT_EXCEL);
        SENSITIVE_WHITE_SET.add(DISTRIBUTION_QR_CODE_INVITATION);
        SENSITIVE_WHITE_SET.add(MINI_QR_CODE);
        SENSITIVE_WHITE_SET.add(EXPORT_SHOPWITHDRAWCASH);
        SENSITIVE_WHITE_SET.add(EXPORT_SCORE_ORDER);
        SENSITIVE_WHITE_SET.add(IMPORT_SCORE_ORDER);
        SENSITIVE_WHITE_SET.add(CATEGORY_EXPORT);
        SENSITIVE_WHITE_SET.add(MEMBER_TREND_EXPORT);
        SENSITIVE_WHITE_SET.add(FLOW_ANALYSIS_EXPORT);
        SENSITIVE_WHITE_SET.add(FLOW_TREND_EXPORT);
        SENSITIVE_WHITE_SET.add(FLOW_SOUR_EXPORT);
        SENSITIVE_WHITE_SET.add(FLOW_USER_ANALYSIS_EXPORT);
        SENSITIVE_WHITE_SET.add(ACCOUNT_DETAIL_EXPORT);
        SENSITIVE_WHITE_SET.add(SHOP_WALLET_PLATFORM_EXPORT);
        SENSITIVE_WHITE_SET.add(FINANCE_DETAIL_FORM_EXPORT);
        SENSITIVE_WHITE_SET.add(MULTISHOP_PROD_EFFECT_EXPORT);
        SENSITIVE_WHITE_SET.add(PLATFORM_PROD_EFFECT_EXPORT);
        SENSITIVE_WHITE_SET.add(PLATFORM_PAY_AMOUNT_TOP_EXPORT);
        SENSITIVE_WHITE_SET.add(MULTISHOP_PAY_AMOUNT_TOP_EXPORT);
        SENSITIVE_WHITE_SET.add(PROD_SURVEY_EXPORT);
        SENSITIVE_WHITE_SET.add(SHOP_COUPON_EXPORT);
        SENSITIVE_WHITE_SET.add(PLATFORM_COUPON_EXPORT);
        SENSITIVE_WHITE_SET.add(SHOP_WRITE_OFF_PLATFORM_COUPON_EXPORT);
        SENSITIVE_WHITE_SET.add(SHOP_CAR_EXPORT);
        SENSITIVE_WHITE_SET.add(PLATFORM_CAR_EXPORT);
        SENSITIVE_WHITE_SET.add(SHOP_CARD_USE_RECORD_EXPORT);
        SENSITIVE_WHITE_SET.add(PLATFORM_CARD_USE_RECORD_EXPORT);
        SENSITIVE_WHITE_SET.add(PLATFORM_WRITE_OFF_CARD_EXPORT);
        SENSITIVE_WHITE_SET.add(PLATFORM_BUY_CARD_EXPORT);
        SENSITIVE_WHITE_SET.add(PLATFORM_HY_EXPORT);
        SENSITIVE_WHITE_SET.add(SHOP_HY_EXPORT);
        SENSITIVE_WHITE_SET.add(SHOP_BUY_CARD_EXPORT);
        SENSITIVE_WHITE_SET.add(SHOP_COUPON_USER_EXPORT);
        SENSITIVE_WHITE_SET.add(PLATFORM_COUPON_USER_EXPORT);
        SENSITIVE_WHITE_SET.add(SHOP_BALANCE_USE_RECORD_EXPORT);
        SENSITIVE_WHITE_SET.add(PLATFORM_BALANCE_USE_RECORD_EXPORT);
        SENSITIVE_WHITE_SET.add(PLATFORM_BALANCE_RECHARGE_RECORD_EXPORT);
        SENSITIVE_WHITE_SET.add(PLATFORM_CARD_RECORD_EXPORT);
    }

    private SensitiveWordConfig() {

    }

    public static HashSet<String> getSensitiveWhiteSet() {
        return SENSITIVE_WHITE_SET;
    }
}
