package com.yami.shop.bean.model;

import com.yami.shop.common.util.IsNeeded;
import lombok.Data;

import java.util.Date;

/**
 * 风险地区
 */
@Data
public class CardImportEntity {
    @IsNeeded
    private String cardCode;
}
