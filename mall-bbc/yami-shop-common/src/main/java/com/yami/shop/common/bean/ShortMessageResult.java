package com.yami.shop.common.bean;

import lombok.Data;

@Data
public class ShortMessageResult {
    private String code;
    private String msg;
    private String tpId;
    private String msgId;
    private Object invalidList;
}
