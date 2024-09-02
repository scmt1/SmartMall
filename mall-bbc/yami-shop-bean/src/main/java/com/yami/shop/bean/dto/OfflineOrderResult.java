package com.yami.shop.bean.dto;

import lombok.Data;

import java.util.List;

@Data
public class OfflineOrderResult {
    private Integer total;
    private Integer current;
    private List<OfflinePaymentOrderDto> records;
    private TotalData totalData;

    @Data
    public static class TotalData {
        private Integer mchFeeAmount;
        private Integer failPayAmount;
        private Integer failPayCount;
        private Integer refundCount;
        private Integer allPayAmount;
        private Integer allPayCount;
        private Integer payAmount;
        private Integer payCount;
        private Integer refundAmount;
    }
}
